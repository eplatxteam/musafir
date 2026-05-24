package com.example.musafir;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

public class LocationWorker extends Worker {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }


    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String city = null;

            // 1️⃣ جلب المدينة من IP أولًا
            CountDownLatch latch = new CountDownLatch(1);
            final String[] cityFromIp = {null};
            getCityNameFromIp(context, (cityAr, cityId, p_country) -> {
                cityFromIp[0] = cityAr;
                latch.countDown();
            });
            latch.await(10, TimeUnit.SECONDS); // انتظار أقصى 10 ثواني
            city = cityFromIp[0];

            // 2️⃣ إذا لم نتمكن من الحصول على المدينة من IP، جرب الموقع
            if (city == null && isLocationEnabled(context) && isGooglePlayServicesAvailable(context)) {

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    return Result.failure(); // إذن غير موجود
                }

                // استخدام getCurrentLocation مع Callback بدل Tasks.await
                fusedLocationClient.getCurrentLocation(
                        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                        new CancellationToken() {
                            @Override
                            public boolean isCancellationRequested() {
                                return false;
                            }

                            @NonNull
                            @Override
                            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener listener) {
                                return this;
                            }
                        }
                ).addOnSuccessListener(location -> {
                    if (location != null && Geocoder.isPresent()) {
                        try {
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String cityFromGps = addresses.get(0).getLocality();
                                if (cityFromGps != null) {
                                    SharedPreferences prefs = SharedPrefsHelper.get(context);

//                                    SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                                    prefs.edit().putString("default_city", cityFromGps).apply();
                                }
                            }
                        } catch (IOException e) {
                            UserUtils.sendLog(context, "doWork-geocoder", e.toString(), e.toString(), "LocationWorker");
                        }
                    }
                }).addOnFailureListener(e -> {
                    UserUtils.sendLog(context, "doWork-location", e.toString(), e.toString(), "LocationWorker");
                });

                // في هذه النقطة، Worker قد انتهى من المهمة ويمكن إعادة Result.success()
                // لأن الإعداد النهائي سيتم حفظه من Callback
            }

            // 3️⃣ حفظ المدينة إذا تم الحصول عليها من IP
            if (city != null) {
                SharedPreferences prefs = SharedPrefsHelper.get(context);
//                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                prefs.edit().putString("default_city", city).apply();
                return Result.success();
            } else {
                return Result.retry();
            }

        } catch (Exception e) {
            UserUtils.sendLog(context, "doWork", e.toString(), e.toString(), "LocationWorker");
            return Result.retry();
        }
    }

    private boolean isGooglePlayServicesAvailable(Context context) {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        return status == ConnectionResult.SUCCESS;
    }

    public static void getCityNameFromIp(Context context, UserUtils.PublicIpCallback callback) {
        UserUtils.getPublicIp((ipJson, city_id, p_country) -> {
            if (ipJson == null) {
                callback.onIpReceived("حدد المدينة", 0, "");
                return;
            }

            try {
                JSONObject ipObj = new JSONObject(ipJson);
                String cityEnFromIp = ipObj.optString("region_code");
                String country_code = ipObj.optString("country_code");
                if (cityEnFromIp == null || cityEnFromIp.isEmpty()) {
                    callback.onIpReceived("حدد المدينة", 0, "");
                    return;
                }

                DBHelper dbHelper = new DBHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                String query = "SELECT city_name_ar, city_id FROM cities WHERE LOWER(city_code) LIKE ? LIMIT 1";
                Cursor cursor = db.rawQuery(query, new String[]{"%-" + cityEnFromIp.toLowerCase()});

//                String query_country = "SELECT country_id FROM country WHERE LOWER(country_code) LIKE ?";
//                Cursor cursor_country = db.rawQuery(query_country, new String[]{"%-" + country_code.toLowerCase()});

                if (cursor.moveToFirst()) {
                    String cityAr = cursor.getString(cursor.getColumnIndexOrThrow("city_name_ar"));
                    int cityId = cursor.getInt(cursor.getColumnIndexOrThrow("city_id"));
                    SharedPreferences prefs = SharedPrefsHelper.get(context);

//                    SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("default_city", cityAr)
                            .putString("country_code", country_code)
                            .putInt("default_city_id", cityId)
                            .apply();

                    callback.onIpReceived(cityAr, cityId, country_code);

                } else {
                    callback.onIpReceived("حدد المدينة", 0, "");
                }

                cursor.close();
                db.close();

            } catch (Exception e) {
                callback.onIpReceived("حدد المدينة", 0, "");
            }
        });
    }
}
