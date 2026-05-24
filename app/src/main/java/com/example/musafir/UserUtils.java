package com.example.musafir;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import jp.wasabeef.blurry.Blurry;

public class UserUtils {

    public static String app_version = com.example.musafir.BuildConfig.VERSION_NAME;

    public static String BASE_URL = "https://api.msafer.app/" + app_version + "/api/";

    public static String ImageUrl = "https://api.msafer.app";

    public static String getDeviceID(Context context) {
        if (context == null) return "unknown_id";
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId != null ? androidId : "null_id";
    }

    public static String getTimeAgo(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";

        try {
            SimpleDateFormat sdf;

            if (dateStr.contains("T")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            }

            Date date = sdf.parse(dateStr);
            long time = date.getTime();
            long now = System.currentTimeMillis();

            long diff = now - time;
            if (diff < 0) return "منذ قليل";

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) {
                return "الآن";
            } else if (minutes < 60) {
                return "منذ " + minutes + " دقيقة";
            } else if (hours < 24) {
                return "منذ " + hours + " ساعة";
            } else if (days < 30) { // تم التعديل هنا ليدعم حتى شهر
                if (days == 1) return "أمس";
                if (days == 2) return "منذ يومين";
                if (days < 11) return "منذ " + days + " أيام";
                return "منذ " + days + " يوماً";
            } else {
                // إذا مر أكثر من شهر، يمكن إظهار عدد الشهور أو التاريخ
                long months = days / 30;
                if (months == 1) return "منذ شهر";
                if (months == 2) return "منذ شهرين";
                return "منذ " + months + " أشهر";
            }
        } catch (Exception e) {
            return dateStr;
        }
    }


    public static String getDeviceInfo() {
        try {
            String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER : "Unknown";
            String model = Build.MODEL != null ? Build.MODEL : "Unknown";
            String deviceName = model.toLowerCase().startsWith(manufacturer.toLowerCase()) ? model : manufacturer + " " + model;
            String osVersion = Build.VERSION.RELEASE;
            int sdkVersion = Build.VERSION.SDK_INT;
            return "Device: " + deviceName + ", OS: " + osVersion + " (SDK " + sdkVersion + ")";
        } catch (Exception e) {
            return "Device info unavailable";
        }
    }


    public interface ProfileUpdateCallback {
        void onProfileUpdated(boolean isVerified, boolean isActive);
    }

    public interface OnCodesFetchedListener {
        void onFetched(JSONArray response);

        void onError(String error);
    }

    public static void fetchCodeDetails(Context context, int typeNo, Integer codeNo, final OnCodesFetchedListener listener) {
        String url = BASE_URL + "code_dtl/?type_no=" + typeNo;
        if (codeNo != null) {
            url += "&code_no=" + codeNo;
        }

        DBHelper dbHelper = new DBHelper(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        dbHelper.getWritableDatabase().execSQL("UPDATE code_details SET show_in_app = 0");

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int cNo = obj.getInt("code_no");
                            int show_in_app = obj.getInt("show_in_app");
                            int code_ordr = obj.getInt("code_ordr");
                            String name = obj.getString("code_l_nm");
                            String icon = obj.optString("code_icon", "");
                            String sys_code = obj.optString("sys_code", "");

                            dbHelper.saveCodeDetails(typeNo, cNo, name, icon, show_in_app, code_ordr, sys_code);
                        }
                    } catch (JSONException e) {
//                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                    listener.onFetched(response);
                },
                error -> {
                    listener.onError(error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = SharedPrefsHelper.get(context).getString("auth_token", "");
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    public static void fetchCompany(Context context, final OnCodesFetchedListener listener) {
        String url = BASE_URL + "company/";

        DBHelper dbHelper = new DBHelper(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        dbHelper.saveCompaniesFromJson(response);
                        listener.onFetched(response);
                    } catch (Exception e) {
//                        e.printStackTrace();
                        listener.onError(e.getMessage());
                    }
                },
                error -> {
                    listener.onError(error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer 1");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    public static void loadVehicleTypesToDB(Context context) {
        DBHelper dbHelper = new DBHelper(context);

        try {
            String url = BASE_URL + "vehicle-types/";
            RequestQueue queue = Volley.newRequestQueue(context);

            // تم التغيير إلى JsonArrayRequest لأن الرد مصفوفة مباشرة
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            // تفريغ الجدول القديم قبل البدء بحفظ البيانات الجديدة
                            dbHelper.clearVehicleTypes();

                            // هنا الـ response هو نفسه الـ JSONArray مباشرة
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject item = response.getJSONObject(i);
                                int id = item.getInt("id_vehicle_type");
                                int inactive = item.getInt("inactive");
                                int has_local = item.getInt("has_local");
                                int has_global = item.getInt("has_global");
                                int has_shared = item.getInt("has_shared");
                                String name = item.getString("vehicles_type");

                                dbHelper.insertVehicleType(id, name, inactive, has_local, has_global, has_shared);
                            }

                        } catch (JSONException e) {
                            sendLog(context, "loadVehicleTypesToDB", e.toString(), e.toString(), "UserUtils");
                        }
                    },
                    error -> {
                        sendLog(context, "loadVehicleTypesToDB", error.toString(), error.toString(), "UserUtils");
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer 1");
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            queue.add(request);
        } catch (Exception e) {
            sendLog(context, "loadVehicleTypesToDB", e.toString(), e.toString(), "UserUtils");
        }
    }

    public static void fetchAndSaveContactInfo(Context context, DBHelper dbHelper) {
        String url = BASE_URL + "ContactInfo/";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        dbHelper.saveContactInfo(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        sendLog(context, "fetchAndSaveContactInfo", error.toString(), "فشل جلب بيانات التواصل", "ContactInfoAPI");
                    }
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(context).add(request);
    }


    public static void fetchTravelerRequests(Context context, DBHelper dbHelper, int passengerId,
                                             final TravelerRequests.fetchTravelerRequestsListener listener) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = SharedPrefsHelper.get(context);
                SharedPreferences.Editor editor = prefs.edit();
//                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int user_id = prefs.getInt("user_id", 0);
                if (user_id != 0) {
                    String url = BASE_URL + "TravelerRequests/?passenger_id=" + passengerId;

                    RequestQueue queue = Volley.newRequestQueue(context);

                    JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                            response -> {
                                try {
                                    boolean hasNew = false;
                                    int lastId = dbHelper.getLastTrId(); // آخر tr_id مخزن

                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject item = response.getJSONObject(i);
                                        dbHelper.insertOrUpdate(item); // تحديث أو إدخال دائمًا
                                    }

                                    List<JSONObject> list = dbHelper.getLatestRequests();

                                    listener.onFetched(list);

                                } catch (JSONException e) {
                                    listener.onError(e.getMessage());
                                }
                            },
                            error -> listener.onError(error.getMessage())
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            SharedPreferences prefs = SharedPrefsHelper.get(context);

//                            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                            String token = prefs.getString("auth_token", null);
                            headers.put("Authorization", "Bearer " + token);
                            return headers;
                        }
                    };
                    request.setRetryPolicy(new DefaultRetryPolicy(
                            50000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    ));
                    queue.add(request);
                }
            } catch (Exception e) {
                sendLog(context, "fetchTravelerRequests", e.toString(), e.toString(), "UserUtils");
            }
        }).start();
    }

    public static void fetchRoutes(Context context, final FetchCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "routes/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Authorization", "Bearer 1");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                JSONArray results = new JSONArray(builder.toString());

                // حفظ البيانات داخل SQLite فقط
                DBHelper db = new DBHelper(context);
                db.saveRoutes(results);
                callback.onSuccess(getMessageFromLocalNew(61, db));
            } catch (Exception e) {
                callback.onError(e.getMessage());
                sendLog(context, "fetchRoutes", e.toString(), e.toString(), "add trip");
            }
        }).start();
    }

    public static void ToastMessages(Activity activity, String msg) {
        if (activity == null || activity.isFinishing()) return;

        activity.runOnUiThread(() -> {
            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast, null);

            TextView text = layout.findViewById(R.id.toast_text);
            text.setText(msg);

            Toast toast = new Toast(activity.getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });
    }

    public static void fetchTypeTravelerRequests(Context context, final DBHelper dbHelper, final TravelerRequests.OnTypeRequestsFetchedListener listener) {
        String url = BASE_URL + "TypeTravelerRequests/";

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<DBHelper.TypeTravelerRequest> types = new ArrayList<>();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int typeId = obj.getInt("type_tr_id");
                            int inactive = obj.getInt("inactive");
                            int order_type = obj.getInt("order_type");
                            int app_Page = obj.getInt("app_page");
                            String name = obj.getString("type_tr_name");
                            String icon = obj.optString("type_icon", "");

                            dbHelper.insertOrUpdateTypeTravelerRequest(typeId, name, icon, inactive, order_type, app_Page);
                            types.add(new DBHelper.TypeTravelerRequest(typeId, name, icon, inactive, app_Page));
                        }

                        listener.onFetched(types);

                    } catch (JSONException e) {
                        listener.onError(e.getMessage());
                    }
                },
                error -> listener.onError(error.getMessage())
        );

        queue.add(jsonArrayRequest);
    }

    public static void fetchServiceHome(Context context, final DBHelper dbHelper, final PageHome.OnServiceHomeFetchedListener listener) {

        String url = BASE_URL + "ServiceHome/";

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<DBHelper.ServiceHome> types = new ArrayList<>();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int typeId = obj.getInt("service_id");
                            int inactive = obj.getInt("inactive");
                            int app_Page = obj.getInt("app_page");
                            int order_type = obj.getInt("order_type");
                            String name = obj.getString("service_name");
                            String icon = obj.optString("type_icon", "");

                            dbHelper.insertOrUpdateServiceHome(typeId, name, icon, inactive, order_type, app_Page);
                            types.add(new DBHelper.ServiceHome(typeId, name, icon, inactive, app_Page));

                        }


                        listener.onFetched(types);

                    } catch (JSONException e) {
                        listener.onError(e.getMessage());
                    }
                },
                error -> {

                    listener.onError(error.getMessage());
                }
        );

        queue.add(jsonArrayRequest);
    }

    public static void checkAppUpdate(Context context) {
        String url = BASE_URL + "SPara/";
        SharedPreferences prefs = SharedPrefsHelper.get(context);
//        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        prefs.edit().putBoolean("optional_update_shown", true).apply();
        String token = prefs.getString("auth_token", null);
        if (token != null) {
            RequestQueue queue = Volley.newRequestQueue(context);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            if (response.length() > 0) {
                                JSONObject obj = response.getJSONObject(0);

                                String lastMandatoryVersion = obj.getString("last_mandatory_version");
                                String lastVersion = obj.getString("last_version");
                                String linkApp = obj.getString("link_app");
                                String link_document = obj.getString("link_document");

                                SharedPreferences prefsLink = context.getSharedPreferences("prefsLink", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefsLink.edit();

                                editor.putString("link_app", linkApp);
                                editor.putString("link_document", link_document);
                                editor.apply();

                                if (compareVersions(app_version, lastMandatoryVersion) < 0) {
                                    showUpdateDialog(context, linkApp, true);
                                } else if (compareVersions(app_version, lastVersion) < 0) {
                                    showUpdateDialog(context, linkApp, false);
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    error -> {
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Authorization", "Bearer " + token);

                    return headers;
                }
            };

            queue.add(jsonArrayRequest);
        }
    }


    public static int compareVersions(String v1, String v2) {
        String[] arr1 = v1.split("\\.");
        String[] arr2 = v2.split("\\.");

        int length = Math.max(arr1.length, arr2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < arr1.length ? Integer.parseInt(arr1[i]) : 0;
            int num2 = i < arr2.length ? Integer.parseInt(arr2[i]) : 0;

            if (num1 < num2) return -1;
            if (num1 > num2) return 1;
        }
        return 0;
    }

    public static void showUpdateDialog(Context context, String linkApp, boolean mandatory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_update_app, null);
        builder.setView(view);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);
        DBHelper dbHelper = new DBHelper(context);

        tvMessage.setText(mandatory ?
                getMessageFromLocalNew(339, dbHelper) :
                getMessageFromLocalNew(340, dbHelper));

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(!mandatory);
        dialog.setCancelable(!mandatory);

        btnYes.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkApp));
                context.startActivity(intent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (!mandatory) dialog.dismiss();
        });

        if (mandatory) {
            btnNo.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 20, 0, 0);
            btnYes.setLayoutParams(params);
        } else {
            btnNo.setOnClickListener(v -> dialog.dismiss());
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            Blurry.with(activity).radius(15).sampling(2).onto(decorView);

            dialog.setOnDismissListener(d -> Blurry.delete(decorView));
        }

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialog.show();
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    public interface CheckSerialCallback {
        void onResult(boolean success);
    }

    public static void getPublicIp(PublicIpCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL("https://geoip.vuiz.net/geoip");
//                URL url = new URL("http://ipwho.is/");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    if (callback != null) {
                        callback.onIpReceived(result.toString(), null, "");

                    }
                } else {
                    if (callback != null) callback.onIpReceived(null, null, "");
                }
            } catch (Exception e) {
                if (callback != null) callback.onIpReceived(null, null, "");
            } finally {
                if (reader != null) try {
                    reader.close();
                } catch (IOException ignored) {
                }
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    public interface DayTimeCallback {
        void onSuccess();

        void onError(String error);
    }

    public static void syncDayTimesFromServer(Context context, DayTimeCallback callback) {
        new Thread(() -> {
            try {
                DBHelper dbHelper = new DBHelper(context);

                URL url = new URL(BASE_URL + "day-time");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                SharedPreferences prefs = SharedPrefsHelper.get(context);
                String token = prefs.getString("auth_token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream()));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                conn.disconnect();

                if (responseCode == 200) {
                    JSONArray serverArray = new JSONArray(result.toString());

                    dbHelper.saveDayTimes(serverArray);

                    if (callback != null) callback.onSuccess();
                } else {
                    if (callback != null) callback.onError("Response Code: " + responseCode);
                }

            } catch (Exception e) {
                sendLog(context, "syncDayTimesFromServer", e.toString(), e.toString(), "UserUtils");
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void setEditTextState(EditText editText, boolean hasError) {

        Runnable updateState = () -> {
            String text = editText.getText().toString().trim();

            if (hasError) {
                editText.setBackgroundResource(R.drawable.edittext_error);
            } else if (editText.isFocused()) {
                editText.setBackgroundResource(R.drawable.edittext_stroke);
            } else if (!text.isEmpty()) {
                editText.setBackgroundResource(R.drawable.edittext_stroke);
            } else {
                editText.setBackgroundResource(R.drawable.edittext_background);
            }
        };

        // TextWatcher للكتابة
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateState.run();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener للـ Focus
        editText.setOnFocusChangeListener((v, hasFocus) -> updateState.run());

        // تحديث الحالة فوراً
        updateState.run();
    }


    public interface PublicIpCallback {
        void onIpReceived(String cityName, Integer cityId, String country);
    }


    public static void showRatingDialog(Context context, int driverId, int tripId) {
        SharedPreferences prefs = SharedPrefsHelper.get(context);

//        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean hasRated = prefs.getBoolean("hasRated_trip_" + tripId, false);
        String token = prefs.getString("auth_token", null);

        int userId = prefs.getInt("user_id", -1);
        if (hasRated) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rate, null);
        builder.setView(view);
        DBHelper dbHelper = new DBHelper(context);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etReview = view.findViewById(R.id.etReview);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnPost = view.findViewById(R.id.btnPost);

        AlertDialog dialog = builder.create();

        // ✅ إضافة الضبابية Blurry
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            Blurry.with(activity).radius(15).sampling(2).onto(decorView);

            dialog.setOnDismissListener(d -> Blurry.delete(decorView));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnPost.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String review = etReview.getText().toString().trim();

            if (rating == 0) {
                getMessageFromLocal(168, dbHelper, new MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        ToastMessages((Activity) context, message);
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
                return;
            }

            String s = "{ driver:" + driverId +
                    " trip:" + tripId +
                    " rating_score:" + rating +
                    " passenger:" + userId +
                    " rate_comment:" + review +
                    "}";
            new Thread(() -> {
                try {
                    URL url = new URL(BASE_URL + "ratings/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    if (token != null) {
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                    }
                    JSONObject ratingData = new JSONObject();
                    ratingData.put("driver", driverId);
                    ratingData.put("trip", tripId);
                    ratingData.put("passenger", userId);
                    ratingData.put("rating_score", (int) rating);
                    ratingData.put("rate_comment", review);

                    OutputStream os = conn.getOutputStream();
                    os.write(ratingData.toString().getBytes("UTF-8"));
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 201) {
                        ((Activity) context).runOnUiThread(() -> {
                            getMessageFromLocal(169, dbHelper, new MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    ToastMessages((Activity) context, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            Intent intent = new Intent(context, HomePage.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        });
                    } else {
                        InputStream errorStream = conn.getErrorStream();
                        StringBuilder errorMsg = new StringBuilder();
                        if (errorStream != null) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                errorMsg.append(line);
                            }
                            reader.close();
                        }

                        String finalErrorMsg = errorMsg.length() > 0 ? errorMsg.toString() : "خطأ غير معروف";

                        ((Activity) context).runOnUiThread(() -> {
                            getMessageFromLocal(62, dbHelper, new MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    ToastMessages((Activity) context, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                        });

                        sendLog(context, "showRatingDialog", finalErrorMsg, ratingData.toString(), "UserUtils");
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    ((Activity) context).runOnUiThread(() ->
                            sendLog(context, "showRatingDialog", e.toString(), s, "UserUtils")
                    );
                }
            }).start();

            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }
        dialog.show();
    }

    public interface CashBankCallback {
        void onMethodSelected(int id, boolean success, int request_id);
    }

    public interface CashBankCallback2 {
        void onMethodSelected(int id, boolean success, String request_id);
    }

    public interface GenericOptionsCallback {
        void onOptionSelected(int payType, String paymentStatus, boolean success, int request_id);
    }

    public abstract static class SingleClickListener implements View.OnClickListener {
        private static final long THRESHOLD_TIME = 1000;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < THRESHOLD_TIME) {
                return;
            }
            lastClickTime = currentTime;
            onSingleClick(v);
        }

        public abstract void onSingleClick(View v);
    }

    public static void showErrorDialog(Activity activity, String errorMsg, String requestId, Integer cbId, String msg, int isbooking, CashBankCallback callback) {
        activity.runOnUiThread(() -> {
            DBHelper dbHelper = new DBHelper(activity);

            SharedPreferences prefs = SharedPrefsHelper.get(activity);

            View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
            Dialog customDialog = new Dialog(activity);
            customDialog.setContentView(dialogView);

            if (customDialog.getWindow() != null) {
                customDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            jp.wasabeef.blurry.Blurry.with(activity).radius(15).sampling(2).onto(decorView);

            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            TextView tvMsg = dialogView.findViewById(R.id.tvMessage);
            LinearLayout btnRetry = dialogView.findViewById(R.id.btnWhatsapp);
            Button btnClose = dialogView.findViewById(R.id.btnCall);
            ImageView iconWhatsapp = dialogView.findViewById(R.id.iconWhatsapp);
            TextView textWhatsapp = dialogView.findViewById(R.id.textWhatsapp);
            TextView tvNote = dialogView.findViewById(R.id.Note);
            LinearLayout containerNote = dialogView.findViewById(R.id.containerNote);
            ImageView paymenticon = dialogView.findViewById(R.id.paymenticon);

            containerNote.setVisibility(View.VISIBLE);
            if (isbooking == 1) {
                paymenticon.setImageResource(R.drawable.info_new);
            } else if (isbooking == 2) {
                paymenticon.setImageResource(R.drawable.info);
            } else {
                paymenticon.setImageResource(R.drawable.paymenticon);
            }
            tvTitle.setText(msg);
            tvMsg.setText(errorMsg);
            textWhatsapp.setText("إعادة المحاولة");
            iconWhatsapp.setVisibility(View.GONE);
            btnClose.setVisibility(View.GONE);

            // تنسيق نص الدعم الفني
            String fullText = "إذا كنت تعتقد أن هذا خطأ، يمكنك التواصل مع الدعم الفني لمساعدتك.";
            SpannableString spannableString = new SpannableString(fullText);
            int startIndex = fullText.indexOf("الدعم الفني");
            if (startIndex != -1) {
                int endIndex = startIndex + "الدعم الفني".length();
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CC9407")), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tvNote.setText(spannableString);

            // رابط واتساب الدعم الفني
            containerNote.setOnClickListener(v -> {
                String countryCode = prefs.getString("country_code", "YE");
                int messageId = "YE".equals(countryCode) ? 349 : 362;
                String phone = getMessageFromLocalNew(messageId, dbHelper);
                try {
                    String url = "https://wa.me/" + phone;
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    ToastMessages(activity, "واتساب غير مثبت");
                }
            });

            // التعامل مع الإغلاق وإعادة المحاولة
            Runnable handleDismiss = () -> {
                jp.wasabeef.blurry.Blurry.delete(decorView);
                if (requestId != null && !requestId.isEmpty() && callback != null) {
                    try {
                        callback.onMethodSelected(cbId, false, Integer.parseInt(requestId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            btnRetry.setOnClickListener(v -> {
                customDialog.dismiss();
                if ("floosak".equals(requestId)) {
                    callback.onMethodSelected(0, false, 0);
                }
            });

            View closeX = dialogView.findViewById(R.id.dialogCancelButton);
            if (closeX != null) closeX.setOnClickListener(v -> customDialog.dismiss());

            customDialog.setOnDismissListener(d -> handleDismiss.run());
            customDialog.show();

        });

    }

    public static void exeBuy(Context context, int cbId, String mobileNo, String code,
                              double amount, int userId, String notes, String car_code,
                              Integer bookingId, Integer trip_id, int pay_type, String requestId,
                              CashBankCallback callback) {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        showSuccessGif(1, activity, null);

        String deviceId = getDeviceID(context);
        String deviceInfo = getDeviceInfo();
        String url = BASE_URL + "exe-buy/" + cbId + "/?device_id=" + deviceId + "&device_info=" + deviceInfo;
        SharedPreferences prefs = SharedPrefsHelper.get(context);
        String cur_code = prefs.getString("cur_code", "");
        String full_name = prefs.getString("full_name", "");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("mobile_no", mobileNo);
            jsonBody.put("code", code);
            jsonBody.put("amount", amount);
            if (car_code != null && !car_code.isEmpty()) {
                jsonBody.put("cur_code", car_code);
            } else {
                jsonBody.put("cur_code", cur_code);
            }
            jsonBody.put("user_id", userId);
            jsonBody.put("notes", notes);
            jsonBody.put("full_name", full_name);
            if (bookingId != null) {
                jsonBody.put("booking_id", bookingId);
            }
            if (trip_id != null) {
                jsonBody.put("trip_id", trip_id);
            }
            jsonBody.put("pay_type", pay_type);

            if (requestId != null && !requestId.isEmpty()) {
                jsonBody.put("requestID", requestId);
            }

        } catch (JSONException e) {
            sendLog(context, "exeBuy", String.valueOf(e.getMessage()), String.valueOf(jsonBody), "UserUtils");

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
//                    if (showGif == 1) {
                    hideSuccessGif(activity);
//                    } else {
//                    progressDialog.dismiss();
//                    }
                    boolean success = response.optBoolean("success");
                    String msg = response.optString("msg");
                    int newIdFromSuccess = response.optInt("requestID");

                    if (success) {
                        ToastMessages(activity, msg);

                        if (callback != null) {
                            callback.onMethodSelected(cbId, success, newIdFromSuccess);
                        }
                    } else {
//                        ToastMessages(activity, msg);
                        showErrorDialog(activity, msg, String.valueOf(newIdFromSuccess), cbId, "تعذر إتمام العملية", 0, callback);
                        if (callback != null && newIdFromSuccess != 0) {
                            callback.onMethodSelected(cbId, success, newIdFromSuccess);
                        }
                    }
                },
                error -> {
//                    if (showGif == 1) {
                    hideSuccessGif(activity);
//                    } else {
//                        progressDialog.dismiss();
//                    }
//                    sendLog(context, "exeBuy", String.valueOf(error.getMessage()), "user_id = " + userId, "UserUtils");
                    DBHelper dbHelper = new DBHelper(context);

                    String finalErrorMsg = "حاول مرة اخرى";
                    String finalRequestId = requestId;
                    String errorLogMessage = "Unknown Error";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            errorLogMessage = new String(error.networkResponse.data, "UTF-8");

                            if (errorLogMessage.trim().startsWith("{")) {
                                JSONObject errorObj = new JSONObject(errorLogMessage);
                                finalErrorMsg = errorObj.optString("msg", "خطأ في معالجة الطلب");
                                finalRequestId = errorObj.optString("requestID", requestId);
                            } else {
                                finalErrorMsg = getMessageFromLocalNew(422, dbHelper);
                            }
                        } catch (Exception e) {
                            finalErrorMsg = getMessageFromLocalNew(347, dbHelper);
                        }
                    } else {
                        if (error instanceof com.android.volley.TimeoutError) {
                            finalErrorMsg = getMessageFromLocalNew(421, dbHelper);
                        } else if (error instanceof com.android.volley.NoConnectionError) {
                            finalErrorMsg = getMessageFromLocalNew(423, dbHelper);
                        } else if (error instanceof com.android.volley.NetworkError) {
                            finalErrorMsg = getMessageFromLocalNew(424, dbHelper);
                        } else {
                            finalErrorMsg = getMessageFromLocalNew(348, dbHelper);
                        }
                        errorLogMessage = (error.getMessage() != null) ? error.getMessage() : error.toString();
                    }

                    sendLog(context, "exeBuy", errorLogMessage, String.valueOf(jsonBody), "UserUtils");
                    showErrorDialog(activity, finalErrorMsg, finalRequestId, cbId, "تعذر إتمام العملية", 0, callback);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = SharedPrefsHelper.get(context);
                String token = prefs.getString("auth_token", "");
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        int socketTimeout = 30000;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(
                socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        );

        request.setRetryPolicy(policy);
        Volley.newRequestQueue(context).add(request);
    }


    public static void floosakexe(Context context, int cbId, String mobileNo, double amount,
                                  int userId, String notes,
                                  Integer bookingId, Integer trip_id, int pay_type, CashBankCallback2 callback) {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;
        ProgressDialog progressDialog = new ProgressDialog(context);
        SharedPreferences prefs = SharedPrefsHelper.get(context);

        progressDialog.setMessage("جاري إرسال رمز التحقق...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String deviceId = getDeviceID(context);
        String deviceInfo = getDeviceInfo();
        String url = BASE_URL + "floosak-exe/" + cbId + "/?device_id=" + deviceId + "&device_info=" + deviceInfo;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("mobile_no", mobileNo);
            jsonBody.put("amount", amount);
            jsonBody.put("user_id", userId);
            jsonBody.put("notes", notes);
            if (bookingId != null) {
                jsonBody.put("booking_id", bookingId);
            }
            if (trip_id != null) {
                jsonBody.put("trip_id", trip_id);
            }
            jsonBody.put("pay_type", pay_type);

        } catch (JSONException e) {
            sendLog(context, "floosakexe", String.valueOf(e.getMessage()), "user_id = " + userId, "UserUtils");

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    progressDialog.dismiss();
                    boolean success = response.optBoolean("success");
                    String msg = response.optString("msg");
                    String newIdFromSuccess = response.optString("purchase_id");

                    if (success) {
                        ToastMessages(activity, msg);

                        if (callback != null) {
                            callback.onMethodSelected(cbId, success, newIdFromSuccess); // فقط عند النجاح
                        }
                    } else {
                        ToastMessages(activity, msg);
                        showErrorDialog(activity, msg, newIdFromSuccess, cbId, "تعذر إتمام العملية", 0, null);
                        if (callback != null) {
                            callback.onMethodSelected(cbId, success, newIdFromSuccess);
                        }
                    }
                },
                error -> {
//                    hideSuccessGif(activity);
                    progressDialog.dismiss();
//                    sendLog(context, "exeBuy", String.valueOf(error.getMessage()), "user_id = " + userId, "UserUtils");
                    DBHelper dbHelper = new DBHelper(context);

                    String finalErrorMsg = "حاول مرة اخرى";
                    String finalRequestId = null;
                    String errorLogMessage = "Unknown Error";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        // حالة 1: السيرفر استلم الطلب ورد بخطأ (مثل 400 أو 500)
                        try {
                            errorLogMessage = new String(error.networkResponse.data, "UTF-8");

                            if (errorLogMessage.trim().startsWith("{")) {
                                JSONObject errorObj = new JSONObject(errorLogMessage);
                                finalErrorMsg = errorObj.optString("msg", "خطأ في معالجة الطلب");
//                                finalRequestId = errorObj.optString("requestID", requestId);
                            } else {
                                finalErrorMsg = getMessageFromLocalNew(422, dbHelper);
                            }
                        } catch (Exception e) {
                            finalErrorMsg = getMessageFromLocalNew(347, dbHelper);
                        }
                    } else {
                        if (error instanceof com.android.volley.TimeoutError) {
                            finalErrorMsg = getMessageFromLocalNew(421, dbHelper);
                        } else if (error instanceof com.android.volley.NoConnectionError) {
                            finalErrorMsg = getMessageFromLocalNew(423, dbHelper);
                        } else if (error instanceof com.android.volley.NetworkError) {
                            finalErrorMsg = getMessageFromLocalNew(424, dbHelper);
                        } else {
                            finalErrorMsg = getMessageFromLocalNew(348, dbHelper);
                        }

                        errorLogMessage = (error.getMessage() != null) ? error.getMessage() : error.toString();
                    }

                    sendLog(context, "floosakexe", errorLogMessage, "user_id = " + userId, "UserUtils");
                    showErrorDialog(activity, finalErrorMsg, finalRequestId, cbId, "تعذر إتمام العملية", 0, null);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = SharedPrefsHelper.get(context);
                String token = prefs.getString("auth_token", "");
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    private static class PaymentMethodAdapter extends ArrayAdapter<Map<String, Object>> {
        private final Context context;
        private final List<Map<String, Object>> items;

        public PaymentMethodAdapter(Context context, List<Map<String, Object>> items) {
            super(context, R.layout.item_spinner_payment, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        private View createView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_spinner_payment, parent, false);
            }
            ImageView imgIcon = convertView.findViewById(R.id.imgIcon);
            TextView txtName = convertView.findViewById(R.id.txtName);

            Map<String, Object> item = items.get(position);
            txtName.setText((String) item.get("cb_name"));

            String iconName = (String) item.get("wallet_icon");
            if (iconName != null && !iconName.isEmpty()) {
                String fullUrl = iconName.startsWith("http") ? iconName : ImageUrl + iconName;
                Glide.with(context).load(fullUrl).placeholder(R.drawable.wallet_setting).into(imgIcon);
            } else {
                imgIcon.setImageResource(R.drawable.wallet_setting);
            }
            return convertView;
        }
    }

    private static View addPaymentOptionCard(Context context, LinearLayout container, String
            title, String desc, int type) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_cash_bank, container, false);
        TextView txtName = itemView.findViewById(R.id.txtName);
        TextView txtDescription = itemView.findViewById(R.id.txtDescription);
        ImageView imgIcon = itemView.findViewById(R.id.imgIcon);
        MaterialCardView cardRoot = itemView.findViewById(R.id.cardRoot);
        RadioButton radioButton = itemView.findViewById(R.id.radioButton);
        txtName.setText(title);
        txtDescription.setText(desc);

        // تحميل الأيقونة (يمكنك استخدام Glide هنا)
//        if (icon != null && !icon.isEmpty()) {
//            int resId = context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
//            if (resId != 0) imgIcon.setImageResource(resId);
//        }

        itemView.setOnClickListener(v -> {
            // تصفير كل الكروت
            for (int j = 0; j < container.getChildCount(); j++) {
                View child = container.getChildAt(j);
                MaterialCardView c = child.findViewById(R.id.cardRoot);
                RadioButton rb = child.findViewById(R.id.radioButton);
                if (c != null) {
                    c.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray2)));
                    c.setCardBackgroundColor(Color.WHITE);
                }
                if (rb != null) rb.setChecked(false);
            }

            // تمييز الكرت المختار (أصفر باهت وحدود ذهبية)
            cardRoot.setCardBackgroundColor(Color.parseColor("#FFFBEB"));
            cardRoot.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary2)));
            if (radioButton != null) radioButton.setChecked(true);
        });

        container.addView(itemView);
        return itemView;
    }


    private static void clearContainerSelection(LinearLayout container) {
        for (int j = 0; j < container.getChildCount(); j++) {
            View child = container.getChildAt(j);
            updateCardSelectionVisuals(child, false);
        }
    }

    private static void updateCardSelectionVisuals(View view, boolean isSelected) {
        MaterialCardView card = view.findViewById(R.id.cardRoot);
        RadioButton rb = view.findViewById(R.id.radioButton);
        if (card != null) {
            if (isSelected) {
                card.setCardBackgroundColor(Color.parseColor("#FFFBEB"));
                card.setStrokeColor(Color.parseColor("#CC9407"));
                card.setStrokeWidth(3);
                if (rb != null) rb.setChecked(true);
            } else {
                card.setCardBackgroundColor(Color.WHITE);
                card.setStrokeColor(Color.parseColor("#E0E0E0"));
                card.setStrokeWidth(1);
                if (rb != null) rb.setChecked(false);
            }
        }
    }

    private static String extractUrl(String text) {
        if (text == null) return null;
        int startIndex = text.indexOf("http");
        if (startIndex != -1) {
            return text.substring(startIndex).split(" ")[0]; // جلب الرابط حتى أول مسافة
        }
        return null;
    }

    private static String removeUrl(String text) {
        if (text == null) return "";
        int startIndex = text.indexOf("http");
        if (startIndex != -1) {
            // نأخذ النص الذي يسبق الرابط فقط
            return text.substring(0, startIndex).trim();
        }
        return text;
    }


    public interface BalanceCallback {
        void onResult(String success);
    }

    public static void fetchBalanceNew(Context context, BalanceCallback callback) {

        SharedPreferences prefs = SharedPrefsHelper.get(context);

        int userId = prefs.getInt("user_id", 0);
        String token = prefs.getString("auth_token", "");

        RequestQueue queue = Volley.newRequestQueue(context);

        if (userId == 0) {
            if (callback != null) {
                callback.onResult("error");
            }
            return;
        }

        String url = BASE_URL + "user-balance/?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,

                response -> {

                    try {

                        JSONArray results = response.getJSONArray("results");

                        for (int i = 0; i < results.length(); i++) {

                            JSONObject item = results.getJSONObject(i);

                            String curCode = item.getString("cur_code");
                            double balance = item.getDouble("balance");

                            DecimalFormat formatter =
                                    (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);

                            formatter.applyPattern("#,###,##0");

                            String formattedBalance =
                                    formatter.format(balance);

                            if (curCode.equalsIgnoreCase("YER")) {

                                prefs.edit()
                                        .putString("user_balance", formattedBalance)
                                        .apply();

                            } else if (curCode.equalsIgnoreCase("SAR")) {

                                prefs.edit()
                                        .putString("user_balance_sa", formattedBalance)
                                        .apply();
                            }
                        }

                        if (callback != null) {
                            callback.onResult("success");
                        }

                    } catch (JSONException e) {

                        if (callback != null) {
                            callback.onResult("error");
                        }
                    }
                },

                error -> {

                    if (callback != null) {
                        callback.onResult("error");
                    }
                }

        ) {

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + token);

                return headers;
            }
        };

        queue.add(request);
    }

    public static void fetchBalance(Context context) {
        SharedPreferences prefs = SharedPrefsHelper.get(context);
        int userId = prefs.getInt("user_id", 0);
        String token = prefs.getString("auth_token", "");
        RequestQueue queue = Volley.newRequestQueue(context);

        if (userId == 0) return;

        String url = BASE_URL + "user-balance/?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.getJSONObject(i);
                            String curCode = item.getString("cur_code");
                            String curName = item.optString("cur_name", "");
                            double balance = item.getDouble("balance");
                            int user_id = item.optInt("user_id", 0);

                            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
                            formatter.applyPattern("#,###,##0");

                            String formattedBalance = formatter.format(balance);
                            if (curCode.equalsIgnoreCase("YER")) {
                                prefs.edit().putString("user_balance", formattedBalance).apply();
                            } else if (curCode.equalsIgnoreCase("SAR")) {
                                prefs.edit().putString("user_balance_sa", formattedBalance).apply();
                            }
                        }
                    } catch (JSONException e) {
//                        e.printStackTrace();
                    }
                },
                error -> {
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    public static void showUnifiedPaymentBottomSheet(Context context, int typeNo,
                                                     int price, Integer trip_id, String car_code,
                                                     int showFirstItem, Integer booking_id, int is_booking, GenericOptionsCallback callback) {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;
        DBHelper dbHelper = new DBHelper(context);

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.TransparentBottomDialog);
        View mainView = LayoutInflater.from(context).inflate(R.layout.dialog_cash_bank, null);
        dialog.setContentView(mainView);

        // 1. تعريف العناصر
        TextView dialogTitle = mainView.findViewById(R.id.dialogTitle);
        TextView textpay = mainView.findViewById(R.id.textpay);
        TextView textPurchaseCode = mainView.findViewById(R.id.textPurchaseCode);
        TextView send_otp = mainView.findViewById(R.id.send_otp);
        TextView t_note = mainView.findViewById(R.id.Note);
        LinearLayout containerNote = mainView.findViewById(R.id.containerNote);
        ImageView imgNoteLink = mainView.findViewById(R.id.imgNoteLink);
        EditText etName = mainView.findViewById(R.id.etName);
        FrameLayout containerNameText = mainView.findViewById(R.id.containerNameText);
        LinearLayout containerName = mainView.findViewById(R.id.containerName);
        LinearLayout containerBooking = mainView.findViewById(R.id.containerBooking);
        LinearLayout container = mainView.findViewById(R.id.paymentMethodsContainer);
        Spinner typePayment = mainView.findViewById(R.id.typePayment);
        View step2Inputs = mainView.findViewById(R.id.step2_Inputs);
        EditText etAmount = mainView.findViewById(R.id.etAmount);
        TextView AmountError = mainView.findViewById(R.id.AmountError);
        EditText etPurchaseCode = mainView.findViewById(R.id.etPurchaseCode);
        Button btnConfirm = mainView.findViewById(R.id.btnConfirm);
        textpay.setText(getMessageFromLocalNew(385, dbHelper));
        step2Inputs.setVisibility(View.VISIBLE);
        typePayment.setVisibility(View.VISIBLE);
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String amountStr = s.toString().trim();

                if (amountStr.isEmpty()) {
                    AmountError.setText("");
                    return;
                }
                if (booking_id != null) {
                    try {
                        double inputAmount = Double.parseDouble(amountStr);

                        if (car_code.contains("YER") && inputAmount < 1000) {
                            AmountError.setText(getMessageFromLocalNew(491, dbHelper));
                            AmountError.setVisibility(View.VISIBLE);
                        } else if (car_code.contains("SAR") && inputAmount < 10) {
                            AmountError.setText(getMessageFromLocalNew(492, dbHelper));
                            AmountError.setVisibility(View.VISIBLE);
                        } else {
                            AmountError.setText("");
                            AmountError.setVisibility(View.GONE);
                        }

                    } catch (NumberFormatException e) {
                        AmountError.setText("");
                    }
                } else {
                    try {
                        double inputAmount = Double.parseDouble(amountStr);
                        SharedPreferences prefs = SharedPrefsHelper.get(context);
                        String countryCode = prefs.getString("country_code", "YE");

                        if (countryCode != null) {
                            if (countryCode.contains("YE") && inputAmount < 1000) {
                                AmountError.setText(getMessageFromLocalNew(491, dbHelper));
                                AmountError.setVisibility(View.VISIBLE);
                            } else if (countryCode.contains("SA") && inputAmount < 10) {
                                AmountError.setText(getMessageFromLocalNew(492, dbHelper));
                                AmountError.setVisibility(View.VISIBLE);
                            } else {
                                AmountError.setText("");
                                AmountError.setVisibility(View.GONE);
                            }
                        }
                    } catch (NumberFormatException e) {
                        AmountError.setText("");
                    }
                }
            }
        });

        if (is_booking == 0) {
            containerBooking.setVisibility(View.GONE);
        } else {
            containerBooking.setVisibility(View.VISIBLE);
        }
        final int[] selectedWalletId = {-1};
        final String[] currentRequestId = {null};
        final int[] currentIsWalletFlg = {0};
        final List<Map<String, Object>> activeMethods = new ArrayList<>();


        String balanceStr = SharedPrefsHelper.get(context).getString("user_balance", "0");
        double userBalance = 0;
        try {
            userBalance = Double.parseDouble(balanceStr.replace(",", "").trim());
        } catch (NumberFormatException e) {
            userBalance = 0;
        }

        double finalPriceToPay;
        if (userBalance > 0) {
            finalPriceToPay = Math.max(0, price - userBalance);
        } else {
            finalPriceToPay = price;
        }

        etAmount.setText(finalPriceToPay <= 0 ? "" : String.valueOf((int) finalPriceToPay));

        setEditTextState(etAmount, false);

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (!input.isEmpty()) {
                    try {
                        if (Double.parseDouble(input) > 0) {
                            setEditTextState(etAmount, false);
                            etAmount.setError(null);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        List<Map<String, Object>> rawMethods = dbHelper.getAllCashBankNew();
        for (Map<String, Object> m : rawMethods) {
            if ((int) m.get("is_active") == 1) {
                activeMethods.add(m);
            }
        }
        PaymentMethodAdapter spinnerAdapter = new PaymentMethodAdapter(context, activeMethods);
        typePayment.setAdapter(spinnerAdapter);
        final int[] payTypeForServer = {3};

        typePayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!activeMethods.isEmpty()) {
                    Map<String, Object> method = activeMethods.get(position);
                    int wId = (int) method.get("cb_id");
                    String wName = (String) method.get("cb_name");
                    String link_code = (String) method.get("link_code");

                    selectedWalletId[0] = wId;
                    container.removeAllViews();

                    final View[] firstItem = {null}; // مصفوفة لتخزين أول عنصر نشط
                    List<Map<String, Object>> localPayTypes = dbHelper.getPayTypesByCbId(wId);
                    SharedPreferences prefs = SharedPrefsHelper.get(context);
                    for (int i = 0; i < localPayTypes.size(); i++) {
                        Map<String, Object> payType = localPayTypes.get(i);
                        String title = (String) payType.get("type_title");
                        String desc = (String) payType.get("type_desc");
                        String pay_code = (String) payType.get("pay_type_code");
                        String p_note = (String) payType.get("note");
                        int inactive = (int) payType.get("inactive");
                        int maxLength = (int) payType.get("maxlength");
                        InputFilter[] editFilters = new InputFilter[1];
                        editFilters[0] = new InputFilter.LengthFilter(maxLength);

                        // إنشاء الكارد
                        View cardView = addPaymentOptionCard(context, container, title, desc, (i == 0 && inactive == 0 ? 1 : 0));

                        if (inactive == 1) {
                            cardView.setEnabled(false);
                            cardView.setAlpha(0.5f);
                            if (cardView instanceof ViewGroup) {
                                ViewGroup group = (ViewGroup) cardView;
                                for (int j = 0; j < group.getChildCount(); j++) {
                                    group.getChildAt(j).setEnabled(false);
                                }
                            }
                            cardView.setOnClickListener(null);
                        } else {

                            cardView.setOnClickListener(v -> {
                                etPurchaseCode.setText("");
                                etPurchaseCode.setFilters(editFilters);
                                if (p_note != null && !p_note.isEmpty()) {
                                    containerNote.setVisibility(View.VISIBLE);
                                    String cleanNote = removeUrl(p_note);
                                    t_note.setText(cleanNote);
                                    t_note.setPaintFlags(t_note.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                                    String url = extractUrl(p_note);
                                    if (url != null) {
                                        imgNoteLink.setVisibility(View.VISIBLE);
                                        containerNote.setOnClickListener(vLink -> {
                                            try {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                context.startActivity(intent);
                                            } catch (Exception e) {
                                                ToastMessages(activity, "تعذر فتح الرابط");
                                            }
                                        });
                                    } else {
                                        imgNoteLink.setVisibility(View.GONE);
                                    }
                                } else {
                                    containerNote.setVisibility(View.GONE);
                                }
                                clearContainerSelection(container);
                                updateCardSelectionVisuals(cardView, true);
                                Typeface rptBold = ResourcesCompat.getFont(context, R.font.rptbold);
                                Typeface rptRegular = ResourcesCompat.getFont(context, R.font.rptregular);
                                if ("maunal".equalsIgnoreCase(pay_code)) {
                                    payTypeForServer[0] = 3;
                                    btnConfirm.setEnabled(true);
                                    btnConfirm.setAlpha(1.0f);
                                    send_otp.setVisibility(View.GONE);
                                    etPurchaseCode.setHint("رقم الإيداع أو الحوالة");
                                    textPurchaseCode.setText("رقم الإيداع أو الحوالة");
                                    etName.setVisibility(View.VISIBLE);
                                    containerNameText.setVisibility(View.VISIBLE);
                                    containerName.setVisibility(View.VISIBLE);
                                    etPurchaseCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                                    etPurchaseCode.setTypeface(rptRegular);
                                    textPurchaseCode.setTypeface(rptRegular);
//                                    etName.setText(prefs.getString("full_name", "") + " (مستخدم التطبيق)");
                                }
//                                payTypeForServer = "auto".equalsIgnoreCase(pay_code) ? 3 : 0;

                                if ("Floosak".equalsIgnoreCase(link_code) && "auto".equalsIgnoreCase(pay_code)) {
                                    payTypeForServer[0] = 2;
                                    send_otp.setVisibility(View.VISIBLE);
//                                    btnConfirm.setEnabled(false);
                                    btnConfirm.setAlpha(0.5f);
                                    etPurchaseCode.setHint("رمز التحقق");
                                    textPurchaseCode.setText("رمز التحقق");
                                    etName.setVisibility(View.GONE);
                                    containerNameText.setVisibility(View.GONE);
                                    containerName.setVisibility(View.GONE);

                                    etPurchaseCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                                    etPurchaseCode.setTypeface(rptRegular);
                                    textPurchaseCode.setTypeface(rptRegular);

                                    btnConfirm.setEnabled(true);
//                                    btnConfirm.setAlpha(1.0f);
                                    btnConfirm.setOnClickListener(vConfirm -> {
                                        String alertMessage = getMessageFromLocalNew(486, dbHelper);

                                        showErrorDialog(activity, alertMessage,
                                                "floosak", null, "تنبيه هام", 0, (id1, success, request_id) -> {
                                                    send_otp.requestFocus();

                                                    send_otp.animate()
                                                            .scaleX(1.15f)
                                                            .scaleY(1.15f)
                                                            .setDuration(300)
                                                            .withEndAction(() -> send_otp.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start())
                                                            .start();
                                                    if (textPurchaseCode != null) {
                                                        textPurchaseCode.animate()
                                                                .translationX(20f)
                                                                .setDuration(50)
//                                                                .setRepeatCount(5)
                                                                .withEndAction(() -> textPurchaseCode.setTranslationX(0f))
                                                                .start();

                                                        textPurchaseCode.setTextColor(Color.parseColor("#FF9800"));
                                                        new Handler(Looper.getMainLooper()).postDelayed(() ->
                                                                textPurchaseCode.setTextColor(Color.BLACK), 1500);
                                                    }
                                                });
                                    });


                                    send_otp.setOnClickListener(v2 -> {
                                        String currentAmountStr = etAmount.getText().toString().trim();
                                        if (currentAmountStr.isEmpty() || Double.parseDouble(currentAmountStr) <= 0) {
                                            setEditTextState(etAmount, true);
                                            etAmount.setError("يرجى ادخال المبلغ");
                                            ToastMessages(activity, "يرجى ادخال المبلغ");
                                            return;
                                        }
                                        int currentAmount = Integer.parseInt(currentAmountStr);
                                        showConfirmationDialog(activity, context, wName, currentAmount, isConfirmed -> {
                                            if (isConfirmed) {
                                                floosakexe(context, wId, prefs.getString("user_phone", ""),
                                                        currentAmount, prefs.getInt("user_id", 0), "",
                                                        booking_id, trip_id, (currentIsWalletFlg[0] == 1 ? 3 : 2),
                                                        (resId, success, requestId) -> {
                                                            if (success) {
                                                                currentRequestId[0] = requestId;
                                                                ToastMessages(activity, "تم إرسال رمز التحقق");
                                                                btnConfirm.setEnabled(true);
                                                                btnConfirm.setAlpha(1.0f);
                                                            }
                                                        });
                                            } else {
                                            }
                                        });
                                    });

                                } else if ("Jaib".equalsIgnoreCase(link_code) && "auto".equalsIgnoreCase(pay_code)) {
                                    payTypeForServer[0] = 2;
                                    etPurchaseCode.setHint("كود الشراء");
                                    textPurchaseCode.setText("كود الشراء");
                                    etName.setText("");
                                    etName.setVisibility(View.GONE);
                                    containerName.setVisibility(View.GONE);
                                    containerNameText.setVisibility(View.GONE);
                                    send_otp.setVisibility(View.GONE);
                                    btnConfirm.setEnabled(true);
                                    btnConfirm.setAlpha(1.0f);

                                    etPurchaseCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                                    etPurchaseCode.setTypeface(rptRegular);
                                    textPurchaseCode.setTypeface(rptRegular);

//                                    etPurchaseCode.setTypeface(rptRegular);
//                                    etPurchaseCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

                                } else if ("KJawal".equalsIgnoreCase(link_code) && "auto".equalsIgnoreCase(pay_code)) {
                                    payTypeForServer[0] = 2;
                                    etPurchaseCode.setHint("رمز PIN");
                                    textPurchaseCode.setText("رمز PIN");
                                    etName.setText("");
                                    etName.setVisibility(View.GONE);
                                    containerName.setVisibility(View.GONE);
                                    containerNameText.setVisibility(View.GONE);
                                    send_otp.setVisibility(View.GONE);
                                    btnConfirm.setEnabled(true);
                                    btnConfirm.setAlpha(1.0f);

                                    etPurchaseCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                                    etPurchaseCode.setTypeface(rptRegular);
                                    textPurchaseCode.setTypeface(rptRegular);
//                                    textPurchaseCode.setTypeface(rptRegular);
//                                    etPurchaseCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);

                                } else {
                                    payTypeForServer[0] = 3;
                                    btnConfirm.setEnabled(true);
                                    btnConfirm.setAlpha(1.0f);
                                    send_otp.setVisibility(View.GONE);
                                    etPurchaseCode.setHint("رقم الإيداع أو الحوالة");
                                    textPurchaseCode.setText("رقم الإيداع أو الحوالة");
                                    etName.setVisibility(View.VISIBLE);
                                    containerNameText.setVisibility(View.VISIBLE);
                                    containerName.setVisibility(View.VISIBLE);
//                                    etPurchaseCode.setTypeface(rptRegular);
//                                    etPurchaseCode.setInputType(InputType.TYPE_CLASS_NUMBER);

                                    etPurchaseCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                                    etPurchaseCode.setTypeface(rptRegular);
                                    textPurchaseCode.setTypeface(rptRegular);
//                                    etName.setText(prefs.getString("full_name", "") + " (مستخدم التطبيق)");
                                }
                            });

                            if (firstItem[0] == null) {
                                firstItem[0] = cardView;
                            }
                        }
                    }

                    // تفعيل أول كارد نشط تلقائياً
                    if (firstItem[0] != null) {
                        firstItem[0].performClick();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (showFirstItem == 1 || showFirstItem == 2) {
            String balance = SharedPrefsHelper.get(context).getString("user_balance", "0");
            View balanceView = LayoutInflater.from(context).inflate(R.layout.item_cash_bank, container, false);
            ((TextView) balanceView.findViewById(R.id.txtName)).setText("الدفع من رصيدي\n (" + balance + ")");
            ((ImageView) balanceView.findViewById(R.id.imgIcon)).setImageResource(R.drawable.wallet_setting);
            ((TextView) balanceView.findViewById(R.id.txtDescription)).setText(getMessageFromLocalNew(405, dbHelper));
            TextView AddBalance = balanceView.findViewById(R.id.AddBalance);
            AddBalance.setVisibility(View.VISIBLE);
            AddBalance.setOnClickListener(v -> {
                dialog.dismiss();
                if (context instanceof HomePage) {
                    ((HomePage) context).openFullScreenFragment(new BalanceFragment(), "رصيدي", R.drawable.wallet, 7);
                }
            });
            balanceView.setOnClickListener(v -> {
                double currentBalance = Double.parseDouble(balance.replace(",", "").trim());
                if (currentBalance < price) {
                    ToastMessages(activity, getMessageFromLocalNew(341, dbHelper));
                } else {
                    dialog.dismiss();
                    if (callback != null) callback.onOptionSelected(2, "on_verfy", true, 0);
                }
            });
            container.addView(balanceView);
        }


        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (selectedWalletId[0] == -1) {
                    ToastMessages(activity, "يرجى اختيار وسيلة دفع أولاً");
                    return;
                }
                String amountStr = etAmount.getText().toString().trim();
                String code = etPurchaseCode.getText().toString().trim();
                String notes = etName.getText().toString().trim();

                if (amountStr.isEmpty() || Double.parseDouble(amountStr) <= 0) {
                    etAmount.setError("يرجى ادخال المبلغ");
                    ToastMessages(activity, "يرجى ادخال المبلغ");
                    return;
                }
                try {
                    double inputAmount = Double.parseDouble(amountStr);

                    // 1. تحديد الدولة/العملة للتحقق من الحد الأدنى
                    String activeCurrency = "";
                    if (booking_id != null && car_code != null && !car_code.isEmpty()) {
                        activeCurrency = car_code; // في حال الحجز نعتمد على كود الرحلة
                    } else {
                        activeCurrency = SharedPrefsHelper.get(context).getString("country_code", "YE"); // في حال الشحن نعتمد على الإعدادات
                    }

                    // 2. التحقق من الحد الأدنى للمبلغ
                    if ((activeCurrency.contains("YE") || activeCurrency.contains("YER")) && inputAmount < 1000) {
                        String errorMsg = getMessageFromLocalNew(491, dbHelper);
                        AmountError.setText(errorMsg);
                        AmountError.setVisibility(View.VISIBLE);
                        showErrorDialog(activity, errorMsg, null, selectedWalletId[0], "تعذر إتمام العملية", 0, null);
//                        ToastMessages(activity, errorMsg);
                        return;
                    } else if ((activeCurrency.contains("SA") || activeCurrency.contains("SAR")) && inputAmount < 10) {
                        String errorMsg = getMessageFromLocalNew(492, dbHelper);
                        AmountError.setText(errorMsg);
                        AmountError.setVisibility(View.VISIBLE);
                        showErrorDialog(activity, errorMsg, null, selectedWalletId[0], "تعذر إتمام العملية", 0, null);
//                        ToastMessages(activity, errorMsg);
                        return;
                    } else {
                        AmountError.setText("");
                        AmountError.setVisibility(View.GONE);
                    }

                    if (booking_id != null) {
                        String balanceStr = SharedPrefsHelper.get(context).getString("user_balance", "0");
                        double userBalance = Double.parseDouble(balanceStr.replace(",", "").trim());

                        if (userBalance < 0) userBalance = 0;

                        if ((inputAmount + userBalance) < price) {
                            String errorMsg = getMessageFromLocalNew(487, dbHelper) + " (" + price + ")";
                            showErrorDialog(activity, errorMsg, null, selectedWalletId[0], "تعذر إتمام العملية", 0, null);
//                            ToastMessages(activity, errorMsg);
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    etAmount.setError("");
                    return;
                }
//                if (booking_id != null) {
//                    try {
//                        double inputAmount = Double.parseDouble(amountStr);
//
//                        if (car_code.contains("YER") && inputAmount < 1000) {
//                            AmountError.setText(getMessageFromLocalNew(491, dbHelper));
//                            AmountError.setVisibility(View.VISIBLE);
//                            return;
//                        } else if (car_code.contains("SAR") && inputAmount < 10) {
//                            AmountError.setText(getMessageFromLocalNew(492, dbHelper));
//                            AmountError.setVisibility(View.VISIBLE);
//                            return;
//                        } else {
//                            AmountError.setText("");
//                            AmountError.setVisibility(View.GONE);
//                        }
//                        String balanceStr = SharedPrefsHelper.get(context).getString("user_balance", "0");
//                        double userBalance = Double.parseDouble(balanceStr.replace(",", "").trim());
//
//                        if (userBalance < 0) {
//                            userBalance = 0;
//                        }
//
//
//                        if ((inputAmount + userBalance) < price) {
//                            String errorMsg = getMessageFromLocalNew(Integer.parseInt(487 + " (" + price + ")"), dbHelper);
//                            ToastMessages(activity, errorMsg);
//                            return;
//                        }
//                    } catch (NumberFormatException e) {
//                        AmountError.setText("");
//                        return;
//                    }
//                } else {
//                    try {
//                        double inputAmount = Double.parseDouble(amountStr);
//                        SharedPreferences prefs = SharedPrefsHelper.get(context);
//                        String countryCode = prefs.getString("country_code", "YE");
//
//                        if (countryCode != null) {
//                            if (countryCode.contains("YE") && inputAmount < 1000) {
//                                String errorMsg = getMessageFromLocalNew(491, dbHelper);
//                                AmountError.setText(errorMsg);
//                                AmountError.setVisibility(View.VISIBLE);
//                                ToastMessages(activity, errorMsg);
//                                return;
//                            } else if (countryCode.contains("SA") && inputAmount < 10) {
//                                String errorMsg = getMessageFromLocalNew(492, dbHelper);
//                                AmountError.setText(errorMsg);
//                                AmountError.setVisibility(View.VISIBLE);
//                                ToastMessages(activity, errorMsg);
//                                return;
//                            } else {
//                                AmountError.setText("");
//                                AmountError.setVisibility(View.GONE);
//                            }
//                        }
//                    } catch (NumberFormatException e) {
//                        etAmount.setError("");
//                        return;
//                    }
//                }
                if (code.isEmpty()) {
                    String fieldName = (etPurchaseCode.getHint() != null) ? etPurchaseCode.getHint().toString() : "البيانات المطلوبة";
                    String errorMessage = "يرجى إدخال " + fieldName;

                    etPurchaseCode.setError(errorMessage);
                    ToastMessages(activity, errorMessage);
                    etPurchaseCode.requestFocus();
                    return;
                }

                exeBuy(
                        context,
                        selectedWalletId[0],
                        SharedPrefsHelper.get(context).getString("user_phone", ""),
                        code,
                        Double.parseDouble(amountStr),
                        SharedPrefsHelper.get(context).getInt("user_id", 0),
                        notes,
                        car_code,
                        booking_id,
                        trip_id,
                        payTypeForServer[0],
                        currentRequestId[0],
                        new CashBankCallback() {
                            @Override
                            public void onMethodSelected(int id, boolean success, int request_id) {

                                if (request_id != 0) {
                                    currentRequestId[0] = String.valueOf(request_id);
                                }

                                if (success) {
                                    activity.runOnUiThread(() -> {
                                        if (callback != null) {
                                            callback.onOptionSelected(2, "on_verfy", true, request_id);
                                        }
                                        if (dialog != null && dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                    });
                                } else {
                                }
                            }
                        });
            }
        });

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        Blurry.with(context).radius(15).sampling(2).onto(decorView);
        dialog.setOnDismissListener(d -> Blurry.delete(decorView));

        dialog.show();
        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            bottomSheet.setLayoutParams(layoutParams);

            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);

            behavior.setPeekHeight(context.getResources().getDisplayMetrics().heightPixels);
        }
    }

    public static void fetchAndSavePayTypes(Context context, final GenericCallback callback) {
        String baseUrl = BASE_URL + "cash_bank_pay_type/";
        RequestQueue queue = Volley.newRequestQueue(context);
        DBHelper dbHelper = new DBHelper(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, baseUrl, null,
                response -> {
                    dbHelper.saveCashBankPayTypes(response);

                    if (callback != null) callback.onSuccess("done");
                },
                error -> {
                    sendLog(context, "fetchAndSavePayTypes", error.toString(), "Error fetching pay types", "UserUtils");
                    if (callback != null) callback.onError(error.getMessage());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = SharedPrefsHelper.get(context);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        }; // إغلاق تعريف الكلاس والطلب هنا

        // ضبط سياسة المحاولة
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // إضافة الطلب للطابور
        queue.add(request);
    }

    private static void showConfirmationDialog(Activity activity, Context context, String name,
                                               int amount, ActionCallback onConfirm) {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
        android.app.Dialog customDialog = new android.app.Dialog(activity);
        customDialog.setContentView(dialogView);
        if (customDialog.getWindow() != null)
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvMsg = dialogView.findViewById(R.id.tvMessage);
        Button btnClose = dialogView.findViewById(R.id.btnCall);
        TextView textWhatsapp = dialogView.findViewById(R.id.textWhatsapp);
        ImageView iconWhatsapp = dialogView.findViewById(R.id.iconWhatsapp);
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        LinearLayout btnRetry = dialogView.findViewById(R.id.btnWhatsapp);
        ImageView paymenticon = dialogView.findViewById(R.id.paymenticon);
        paymenticon.setImageResource(R.drawable.paymenticonsuccess);
        LinearLayout containerNote = dialogView.findViewById(R.id.containerNote);
        containerNote.setVisibility(View.GONE);
        if (btnClose instanceof com.google.android.material.button.MaterialButton) {
            ((com.google.android.material.button.MaterialButton) btnClose).setIcon(null);
        }
        SharedPreferences prefs = SharedPrefsHelper.get(context);
        int user_id = prefs.getInt("user_id", 0);
        String phone = prefs.getString("user_phone", "");
        textWhatsapp.setText("تأكيد");
        iconWhatsapp.setVisibility(View.GONE);
        View closeX = dialogView.findViewById(R.id.dialogCancelButton);
        if (closeX != null) closeX.setOnClickListener(v2 -> customDialog.dismiss());
        tvTitle.setText("تأكيد عملية الدفع");
        btnClose.setText("إلغاء");
        tvMsg.setText("دفع مبلغ " + amount + " ريال من محفظة \"" + name + "\" المرتبطة بالرقم \"" + phone + "\". هل تود الاستمرار؟");
        dialogView.findViewById(R.id.btnWhatsapp).setOnClickListener(v -> {
            customDialog.dismiss();
            onConfirm.onAction(true);
        });
        dialogView.findViewById(R.id.btnCall).setOnClickListener(v -> customDialog.dismiss());
        customDialog.show();
    }

    interface ActionCallback {
        void onAction(boolean success); // إضافة بارامتر هنا
    }

    public static void fetchAndSaveMessages(Context context, final FetchCallback callback) {
        String baseUrl = BASE_URL + "MessageApp/";
        RequestQueue queue = Volley.newRequestQueue(context);
        DBHelper dbHelper = new DBHelper(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, baseUrl, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject msgObj = response.getJSONObject(i);
                            int id = msgObj.getInt("message_id");
                            String message = msgObj.getString("messages");

                            dbHelper.insertMessage(id, message);
                        }
                        callback.onSuccess(getMessageFromLocalNew(61, dbHelper));
                    } catch (JSONException e) {
                        sendLog(context, "fetchAndSaveMessages", String.valueOf(e), String.valueOf(e), "UserUtils");

                        callback.onError(e.getMessage());
                    }
                },
                error -> {
                    sendLog(context, "fetchAndSaveMessages", String.valueOf(error), String.valueOf(error), "UserUtils");
                    callback.onError(error.getMessage());

                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        queue.add(request);
    }

    public static void fetchAndSaveCountry(Context context, final FetchCallback callback) {
        String baseUrl = BASE_URL + "Country/";
        RequestQueue queue = Volley.newRequestQueue(context);
        DBHelper dbHelper = new DBHelper(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, baseUrl, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject msgObj = response.getJSONObject(i);
                            int id = msgObj.getInt("country_id");
                            String country_name = msgObj.getString("country_name");
                            String country_code = msgObj.getString("country_code");

                            dbHelper.addCountry(id, country_name, country_code);
                        }
                        callback.onSuccess(getMessageFromLocalNew(61, dbHelper));
                    } catch (JSONException e) {
                        sendLog(context, "fetchAndSaveCountry", String.valueOf(e), String.valueOf(e), "UserUtils");
                        callback.onError(e.getMessage());
                    }
                },
                error -> {
                    sendLog(context, "fetchAndSaveCountry", String.valueOf(error), String.valueOf(error), "UserUtils");
                    callback.onError(error.getMessage());
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

    public interface FetchCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    public static void fetchAndSavecities(Context context, final citiesCallback callback) {
        String baseUrl = BASE_URL + "cities/";
        RequestQueue queue = Volley.newRequestQueue(context);
        DBHelper dbHelper = new DBHelper(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, baseUrl, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject msgObj = response.getJSONObject(i);
                            int id = msgObj.getInt("city_id");
                            String city_name = msgObj.getString("city_name");
                            String city_name_en = msgObj.getString("city_name_en");
                            String city_code = msgObj.getString("city_code");
                            int country_id = msgObj.getInt("country_id");


                            dbHelper.insertOrUpdateCity(id, city_name, city_name_en, country_id, city_code);
                        }
                        callback.onSuccess(getMessageFromLocalNew(61, dbHelper));
                    } catch (JSONException e) {
                        sendLog(context, "fetchAndSaveMessages", String.valueOf(e), String.valueOf(e), "UserUtils");

                        callback.onError(e.getMessage());
                    }
                },
                error -> {
                    sendLog(context, "fetchAndSaveMessages", String.valueOf(error), String.valueOf(error), "UserUtils");
                    callback.onError(error.getMessage());

                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        queue.add(request);
    }

    public interface citiesCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    public interface GenericCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    public static void getMessageFromLocal(int messageId, DBHelper dbHelper, MessageCallback
            callback) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT messages FROM messages WHERE message_id = ?",
                new String[]{String.valueOf(messageId)});

        if (cursor.moveToFirst()) {
            String message = cursor.getString(cursor.getColumnIndexOrThrow("messages"));
            callback.onSuccess(message);
        } else {
            callback.onError("Message not found");
        }

        cursor.close();
    }

    public static String getMessageFromLocalNew(int messageId, DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String message = "حاول مرة اخرى";

        try {
            cursor = db.rawQuery("SELECT messages FROM messages WHERE message_id = ?",
                    new String[]{String.valueOf(messageId)});

            if (cursor != null && cursor.moveToFirst()) {
                message = cursor.getString(cursor.getColumnIndexOrThrow("messages"));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return message;
    }


    public interface MessageCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    public static void sendLog2(Context context, String logText) {
        sendLog2(context, logText, 0);
    }

    private static void sendLog2(Context context, String logText, int attempt) {
        if (attempt > 1) return; // لا تزيد عن محاولتين
        new Thread(() -> {
            try {

                URL url = new URL(BASE_URL + "s_log/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Bearer " + 1);
                SharedPreferences prefs = SharedPrefsHelper.get(context);

//                SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int user_id = prefs.getInt("user_id", 0);
                String phone_number = prefs.getString("phone_number", "");

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("user_id", user_id);
                jsonBody.put("log_name", "sendLog2");
                jsonBody.put("log_text", logText);
                jsonBody.put("log_body", "app_version = " + app_version + " " + phone_number + " " + getDeviceInfo() + " " + getDeviceID(context));
                jsonBody.put("page_name", "UserUtils");
                jsonBody.put("log_app", "msafer.app");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonBody.toString());
                writer.flush();
                writer.close();
                os.close();

                conn.getResponseCode();
                conn.disconnect();

            } catch (Exception e) {
                sendLog(context, "sendLog", logText, e.toString(), "UserUtils"); // محاولة ثانية فقط
            }
        }).start();
    }

    public static void sendLogClob(Context context, String logName, String logText, String
            logBody, String pageName, String crash_message) {

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "s_log/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Bearer 1");
                SharedPreferences prefs = SharedPrefsHelper.get(context);

//                SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int user_id = prefs.getInt("user_id", 0);
                String phone_number = prefs.getString("phone_number", "");

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("user_id", user_id);
                jsonBody.put("log_name", logName);
                jsonBody.put("log_text", logText);
                jsonBody.put("log_body", "app_version = " + app_version + " " + phone_number + " " + getDeviceInfo() + " " + getDeviceID(context) + " " + logBody);
                jsonBody.put("page_name", pageName);
                jsonBody.put("crash_message", crash_message);
                jsonBody.put("log_app", "msafer.app");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonBody.toString());
                writer.flush();
                writer.close();
                os.close();

                conn.getResponseCode();
                conn.disconnect();

            } catch (Exception e) {
//                sendLog2(context, e.toString());

            }
        }).start();
    }

    public static void sendLog(Context context, String logName, String logText, String
            logBody, String pageName) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "s_log/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Bearer " + 1);
                SharedPreferences prefs = SharedPrefsHelper.get(context);

//                SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int user_id = prefs.getInt("user_id", 0);
                String phone_number = prefs.getString("phone_number", "");

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("user_id", user_id);
                jsonBody.put("log_name", logName);
                jsonBody.put("log_text", logText);
                jsonBody.put("log_body", "app_version = " + app_version + " " + phone_number + " " + getDeviceInfo() + " " + getDeviceID(context) + " " + logBody);
                jsonBody.put("page_name", pageName);
                jsonBody.put("log_app", "msafer.app");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonBody.toString());
                writer.flush();
                writer.close();
                os.close();

                conn.getResponseCode();
                conn.disconnect();

            } catch (Exception e) {
                sendLog2(context, e.toString());
            }
        }).start();
    }

    public static void check_serial(Context context, CheckSerialCallback callback) {
        SharedPreferences prefs = SharedPrefsHelper.get(context);

//        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String device_serial = prefs.getString("device_serial", "");
        String token = prefs.getString("auth_token", "");

        new Thread(() -> {
            boolean success = false;
            try {
                URL url = new URL(BASE_URL + "check_serial/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("device_serial", device_serial);
                jsonBody.put("token", token);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject responseJson = new JSONObject(response.toString());
                    success = responseJson.optBoolean("success", false);
                }

                conn.disconnect();

            } catch (Exception e) {
                sendLog(context, "check_serial", e.toString(), e.toString(), "UserUtils");
            }

            boolean finalSuccess = success;
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onResult(finalSuccess);
            });
        }).start();
    }

    public static void updateProfile(Context context, ProfileUpdateCallback callback) {
        SharedPreferences prefs = SharedPrefsHelper.get(context);
        String token = prefs.getString("auth_token", "");

        if (token.isEmpty()) {
            if (callback != null) {
                callback.onProfileUpdated(false, false);
            }
            return;
        }

        new Thread(() -> {
            try {
                String deviceId = getDeviceID(context);
                String deviceInfo = getDeviceInfo();
                URL url = new URL(BASE_URL + "auth/profile/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("token", token);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject responseJson = new JSONObject(response.toString());

                    boolean isVerified = responseJson.optBoolean("is_verified", false);
                    boolean isActive = responseJson.optBoolean("is_active", false);
                    SharedPreferences prefs2 = SharedPrefsHelper.get(context);
                    SharedPreferences.Editor editor = prefs2.edit();
//                    SharedPreferences.Editor editor = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit();
                    editor.putString("is_verified", String.valueOf(isVerified));
                    editor.putString("is_active", String.valueOf(isActive));

                    editor.apply();

                    if (callback != null && context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> callback.onProfileUpdated(isVerified, isActive));
                    }

                } else {
                    sendLog(context, "check_serial", String.valueOf(responseCode), token, "UserUtils");
                }

                conn.disconnect();

            } catch (Exception e) {
                sendLog(context, "updateProfile", e.toString(), e.toString(), "UserUtils");
            }
        }).start();
    }

    public static void fetchCashBankData(Context context, final DBHelper dbHelper,
                                         final OnCashBankFetchedListener listener) {

        String url = BASE_URL + "CashBank/";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<DBHelper.CashBank> cashBanks = new ArrayList<>();
                    try {
                        dbHelper.getWritableDatabase().execSQL("UPDATE cash_bank SET is_active = 0");
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            int cb_id = obj.getInt("cb_id");
                            int is_wallet_flg = obj.getInt("is_wallet_flg");
                            int comfirm_wallet_pay_flag = obj.getInt("comfirm_wallet_pay_flag");
                            int is_active = obj.getInt("is_active");
                            int manul_pay_flg = obj.getInt("manul_pay_flg");
                            int auto_pay_flg = obj.getInt("auto_pay_flg");
                            String cb_name = obj.getString("cb_name");
                            String wallet_icon = obj.optString("wallet_icon", "");
                            String manul_pay_title = obj.optString("manul_pay_title", "");
                            String manul_pay_desc = obj.optString("manul_pay_desc", "");
                            String auto_pay_title = obj.optString("auto_pay_title", "");
                            String auto_pay_desc = obj.optString("auto_pay_desc", "");
                            String link_code = obj.optString("link_code", "");
                            dbHelper.saveCashBank(cb_id, cb_name, wallet_icon, is_wallet_flg, comfirm_wallet_pay_flag, is_active,
                                    manul_pay_flg, auto_pay_flg, manul_pay_title, manul_pay_desc, auto_pay_title, auto_pay_desc, link_code);
                            cashBanks.add(new DBHelper.CashBank(cb_id, cb_name, wallet_icon, is_wallet_flg));
                        }

                        listener.onFetched(cashBanks);

                    } catch (JSONException e) {
                        listener.onError(e.getMessage());
                    }
                },
                error -> {
                    listener.onError(error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = SharedPrefsHelper.get(context);

                String token = prefs.getString("auth_token", null);

                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    public static void sendMessage(Context context, int userId, String message, boolean isMe) {
        String deviceId = getDeviceID(context);
        String deviceInfo = getDeviceInfo();

        String url = BASE_URL + "ChatMessage/?device_id=" + deviceId + "&device_info=" + deviceInfo;

        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
            postData.put("message", message);
            postData.put("is_me", isMe);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        String status = response.getString("status");
                        String msg = response.getString("message");

                        if (status.equals("success")) {

                        } else {
                            sendLog(context, "sendMessage", msg, "user_id = " + userId, "UserUtils");
                        }
                    } catch (JSONException e) {
//                        listener.onError(e.getMessage());
                        sendLog(context, "sendMessage", e.getMessage(), "user_id = " + userId, "UserUtils");
                    }
                },
                error -> sendLog(context, "sendMessage", error.toString(), "user_id = " + userId, "UserUtils")
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = SharedPrefsHelper.get(context).getString("auth_token", "");
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                60000, // رفع وقت الانتظار إلى 60 ثانية (60000 ملي ثانية)
                0,     // عدد مرات إعادة المحاولة (0) لمنع تكرار الرسالة إذا تأخر الرد
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(context).add(request);
    }

    public interface OnCashBankFetchedListener {
        void onFetched(List<DBHelper.CashBank> cashBanks);

        void onError(String error);
    }

    private static Dialog successDialog;

    public static void hideSuccessGif(Activity activity) {
        try {
            if (successDialog != null && successDialog.isShowing()) {
                successDialog.dismiss();
                successDialog = null;
            }

            if (activity != null) {
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                Blurry.delete(decorView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showSuccessGif(int type_img, Activity activity, Runnable onDismiss) {
        if (activity == null || activity.isFinishing()) return;

        hideSuccessGif(activity);

        successDialog = new Dialog(activity);
        successDialog.setContentView(R.layout.dialog_gif_loading);
        successDialog.setCancelable(false);
        successDialog.setCanceledOnTouchOutside(false);

        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            Blurry.with(activity).radius(25).sampling(2).onto(decorView);
        }

        ImageView gifImageView = successDialog.findViewById(R.id.gifImageView);
        TextView tvCountdown = successDialog.findViewById(R.id.tvCountdown);

        if (type_img == 1) {
            Glide.with(activity).asGif().load(R.drawable.saved_successfully).into(gifImageView);
        } else if (type_img == 3) {
            Glide.with(activity).asGif().load(R.drawable.saved_successfully3).into(gifImageView);
        } else {
            Glide.with(activity).asGif().load(R.drawable.saved_successfully2).into(gifImageView);
        }

        successDialog.show();

        final int[] secondsRemaining = {1};
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (secondsRemaining[0] > 0) {
                    if (tvCountdown != null)
                        tvCountdown.setText(String.valueOf(secondsRemaining[0]));
                    secondsRemaining[0]++;
                    handler.postDelayed(this, 1000);
                } else {
                    if (successDialog != null && successDialog.isShowing()) {
                        hideSuccessGif(activity);
                        if (onDismiss != null) {
                            onDismiss.run();
                        }
                    }
                }
            }
        };
        handler.post(countdownRunnable);
    }

    public static void app_Pages(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SharedPreferences prefs = SharedPrefsHelper.get(context);
        String token = prefs.getString("auth_token", null);

        List<Map<String, Object>> unsyncedVisits = dbHelper.getUnsyncedPageVisits();

        if (unsyncedVisits.isEmpty()) return;

        new Thread(() -> {
            for (Map<String, Object> visit : unsyncedVisits) {
                int pvId = (int) visit.get("pv_id");
                int userId = (int) visit.get("user_id");
                int pageId = (int) visit.get("page_id");
                int count = (int) visit.get("visit_count");

                try {
                    URL url = new URL(BASE_URL + "app_page_users/visit/?user_id=" + userId +
                            "&page_id=" + pageId + "&visit_count=" + count);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (token != null) {
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        dbHelper.markAsSynced(pvId);
                    } else {
                        sendLog(context, "app_Pages", "Code: " + responseCode, "pv_id: " + pvId, "UserUtils");
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    sendLog(context, "app_Pages_Error", e.toString(), "pv_id: " + pvId, "UserUtils");
                }
            }
        }).start();
    }

    public static void app_Page(Context context, int pageId) {
        SharedPreferences prefs = SharedPrefsHelper.get(context);
        int user_id = prefs.getInt("user_id", 0);
        String token = prefs.getString("auth_token", null);
        DBHelper dbHelper = new DBHelper(context);

        if (user_id != 0) {
            new Thread(() -> {
                try {
                    URL url = new URL(BASE_URL + "app_page_users/visit/?user_id=" + user_id + "&page_id=" + pageId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    if (token != null) {
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject responseJson = new JSONObject(response.toString());
                        int visitCount = responseJson.optInt("visit_count", 0);

                        // --- حفظ البيانات في الجدول المحلي ---
                        dbHelper.savePageVisit(user_id, pageId, visitCount);

                    } else {
                        sendLog(context, "app_Page", String.valueOf(responseCode), "user_id = " + user_id + " pageId = " + pageId, "UserUtils");
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    sendLog(context, "app_Page", e.toString(), "user_id = " + user_id + " pageId = " + pageId, "UserUtils");
                }
            }).start();
        }
    }

}
