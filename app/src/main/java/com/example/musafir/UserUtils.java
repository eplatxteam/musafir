package com.example.musafir;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.parser.IntegerParser;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

public class UserUtils {

    public static String app_version = com.example.musafir.BuildConfig.VERSION_NAME;

    public static String BASE_URL = "https://msafer.eplatx.com/" + app_version + "/api/";

    public static String ImageUrl = "https://msafer.eplatx.com";

    public static String getDeviceID(Context context) {
        if (context == null) return "unknown_id";
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId != null ? androidId : "null_id";
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
                            String name = obj.getString("code_l_nm");
                            String icon = obj.optString("code_icon", "");

                            dbHelper.saveCodeDetails(typeNo, cNo, name, icon, show_in_app);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                        e.printStackTrace();
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

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray results = response.getJSONArray("results");

                            dbHelper.clearVehicleTypes();

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                int id = item.getInt("id_vehicle_type");
                                int inactive = item.getInt("inactive");
                                String name = item.getString("vehicles_type");
                                dbHelper.insertVehicleType(id, name, inactive);
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

//                                    for (int i = 0; i < response.length(); i++) {
//                                        JSONObject item = response.getJSONObject(i);
//                                        int tr_id = item.getInt("tr_id");
//
//                                        if (tr_id > lastId) {
//                                            hasNew = true;
//                                            dbHelper.insertOrUpdate(item); // حفظ أو تحديث
//                                        }
//                                    }
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject item = response.getJSONObject(i);
                                        dbHelper.insertOrUpdate(item); // تحديث أو إدخال دائمًا
                                    }

                                    // جلب آخر 5 طلبات من DB
                                    List<JSONObject> list = dbHelper.getLatestRequests();

                                    // إعادة البيانات عبر listener
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
                callback.onSuccess("تم تحديث البيانات بنجاح");
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

        tvMessage.setText(mandatory ?
                "انتهت صلاحية هذا الإصدار من التطبيق.\n قم بالتحديث للمتابعة." :
                "يتوفر تحديث جديد للتطبيق، هل تريد التحديث الآن؟");

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(!mandatory);
        dialog.setCancelable(!mandatory);

        btnYes.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkApp));
                context.startActivity(intent);
            } catch (Exception e) {
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
                        // إرسال الـ JSON كامل كسلسلة نصية
                        callback.onIpReceived(result.toString(), null);

                    }
                } else {
                    if (callback != null) callback.onIpReceived(null, null);
                }
            } catch (Exception e) {
                if (callback != null) callback.onIpReceived(null, null);
            } finally {
                if (reader != null) try {
                    reader.close();
                } catch (IOException ignored) {
                }
                if (connection != null) connection.disconnect();
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
        void onIpReceived(String cityName, Integer cityId);
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

    public static void showGenericOptionsBottomSheet(Context context, int typeNo, double price, int trip_id,
                                                     int showFirstItem, int booking_id, GenericOptionsCallback callback) {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;
        DBHelper dbHelper = new DBHelper(context);
        List<Map<String, Object>> localData = dbHelper.getCodeDetailsByType(typeNo);

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.TransparentBottomDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_cash_bank, null);
        dialog.setContentView(view);

        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        LinearLayout container = view.findViewById(R.id.paymentMethodsContainer);
        view.findViewById(R.id.step2_Inputs).setVisibility(View.GONE);

        dialogTitle.setText("وسيلة الدفع");

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        Blurry.with(context).radius(15).sampling(2).onto(decorView);
        dialog.setOnDismissListener(d -> Blurry.delete(decorView));

        try {
            for (int i = 0; i < localData.size(); i++) {
                Map<String, Object> item = localData.get(i);
                int codeNo = (int) item.get("code_no");
                String name = (String) item.get("code_l_nm");
                String iconName = (String) item.get("code_icon");

                if (showFirstItem == 0 && i == 0) continue;
                if (showFirstItem == 2 && i == 0) continue;

                View itemView = LayoutInflater.from(context).inflate(R.layout.item_cash_bank, container, false);
                TextView txtName = itemView.findViewById(R.id.txtName);
                ImageView imgIcon = itemView.findViewById(R.id.imgIcon);

                txtName.setText(name);

                int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                if (resId != 0) {
                    imgIcon.setImageResource(resId);
                } else if (!iconName.isEmpty()) {
                    String finalIconUrl = iconName.startsWith("http") ? iconName : ImageUrl + iconName;
                    Glide.with(context).load(finalIconUrl).placeholder(R.drawable.wallet_setting).into(imgIcon);
                } else {
                    imgIcon.setImageResource(R.drawable.wallet_setting);
                }

                itemView.setOnClickListener(v -> {
                    if (codeNo == 2) {

                        if (typeNo == 5) {

                            dialog.dismiss();
                            showCashBankBottomSheet(context, trip_id, price, booking_id, 0, (walletId, success, requestId) -> {
                                if (callback != null) {
                                    callback.onOptionSelected(2, "on_verfy", success, requestId);
                                }
                            });
                        }
                    }
//                            else if (codeNo == 1) {
//                                if (showFirstItem == 2) {
//                                    return;
//                                }
//
//                                dialog.dismiss();
//                                if (callback != null)
//                                    callback.onOptionSelected(0, "waiting", true, 0);
//                            }
                    else if (codeNo == 1) {
                        dialog.dismiss();
                        if (callback != null)
                            callback.onOptionSelected(0, "waiting", true, 0);
                    } else {
                        dialog.dismiss();
                        showCashBankBottomSheet(context, trip_id, price, booking_id, 1, (walletId, success, requestId) -> {
                            if (callback != null) {
                                callback.onOptionSelected(2, "on_verfy", success, requestId);
                            }
                        });
                        if (callback != null)
                            callback.onOptionSelected(2, "on_verfy", false, 0);
//                            }
//                        if (codeNo == 2) {
//                            if (typeNo == 5) {
//                                dialog.dismiss();
//                                showCashBankBottomSheet(context, trip_id, price, 0, (walletId, success, requestId) -> {
//                                    if (callback != null) callback.onOptionSelected(2, "on_verfy", success, requestId);
//                                });
//                            }
//                        } else {
//                            dialog.dismiss();
//                            showCashBankBottomSheet(context, trip_id, price, 1, (walletId, success, requestId) -> {
//                                if (callback != null) callback.onOptionSelected(2, "on_verfy", success, requestId);
//                            });
                    }
                });


                container.addView(itemView);
            }
        } catch (Exception e) {
        }

        if (showFirstItem == 1 || showFirstItem == 2) {
            View balanceView = LayoutInflater.from(context).inflate(R.layout.item_cash_bank, container, false);
            TextView txtName = balanceView.findViewById(R.id.txtName);
            ImageView imgIcon = balanceView.findViewById(R.id.imgIcon);

            SharedPreferences prefs = SharedPrefsHelper.get(context);
            String balance = prefs.getString("user_balance", "0");

            txtName.setText("الدفع من رصيدي\n (" + balance + ")");
            imgIcon.setImageResource(R.drawable.wallet_setting);

            balanceView.setOnClickListener(v -> {
                double currentBalance = 0;
                try {
                    String cleanBalance = balance.replace(",", "").trim();
                    currentBalance = Double.parseDouble(cleanBalance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                double balance2 = Double.parseDouble(balance.replace(",", "").trim());
//                if (balance2 < 0) {
//                    ToastMessages(activity, "رصيدك سالب، يجب دفع كامل المبلغ");
//                }
                if (currentBalance < price) {
                    dialog.dismiss();
                    ToastMessages(activity, "رصيدك غير كافٍ، يرجى تعبئة الرصيد أولاً");
                    if (context instanceof HomePage) {
                        ((HomePage) context).openFullScreenFragment(new BalanceFragment(), "رصيدي", R.drawable.wallet, 7);
                    }
                } else {
                    dialog.dismiss();
                    if (callback != null)
                        callback.onOptionSelected(2, "on_verfy", true, 0);
                }
            });

            // إضافة زر "إضافة رصيد"
            TextView btnAdd = new TextView(context);
            btnAdd.setText("إضافة رصيد");
            btnAdd.setTextColor(context.getResources().getColor(R.color.primary));
            btnAdd.setPadding(15, 5, 15, 5);
            btnAdd.setTextSize(12);
            if (balanceView instanceof LinearLayout) {
                LinearLayout itemLayout = (LinearLayout) balanceView;
                itemLayout.addView(btnAdd, 3);
            }
            btnAdd.setOnClickListener(v -> {
                dialog.dismiss();
                if (context instanceof HomePage) {
                    ((HomePage) context).openFullScreenFragment(new BalanceFragment(), "رصيدي", R.drawable.wallet, 7);
                }
            });

            container.addView(balanceView);
        }

        dialog.show();


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

    public static void exeBuy(Context context, int cbId, String mobileNo, String code, double amount, String curCode, int userId, String notes,
                              Integer bookingId, int trip_id, int pay_type, String requestId, CashBankCallback callback) {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;
//        ProgressDialog progressDialog = new ProgressDialog(context);
//        if (showGif == 1) {
        showSuccessGif(1, activity, null);
//        } else {
//            progressDialog.setMessage("جاري إرسال رمز التحقق...");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//        }
        String deviceId = getDeviceID(context);
        String deviceInfo = getDeviceInfo();
        String url = BASE_URL + "exe-buy/" + cbId + "/?device_id=" + deviceId + "&device_info=" + deviceInfo;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("mobile_no", mobileNo);
            jsonBody.put("code", code);
            jsonBody.put("amount", amount);
            jsonBody.put("cur_code", curCode);
            jsonBody.put("user_id", userId);
            jsonBody.put("notes", notes);
            jsonBody.put("booking_id", bookingId);
            jsonBody.put("trip_id", trip_id);
            jsonBody.put("pay_type", pay_type);

            if (requestId != null && !requestId.isEmpty()) {
                jsonBody.put("requestID", requestId);
            }
        } catch (JSONException e) {
            sendLog(context, "exeBuy", String.valueOf(e.getMessage()), "user_id = " + userId, "UserUtils");

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
                            callback.onMethodSelected(cbId, success, newIdFromSuccess); // فقط عند النجاح
                        }
                    } else {
                        ToastMessages(activity, msg);

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

                    String finalErrorMsg = "حدث خطأ غير متوقع";
                    String finalRequestId = requestId;
                    String errorLogMessage = "Unknown Error";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorStr = new String(error.networkResponse.data, "UTF-8");
                            errorLogMessage = new String(error.networkResponse.data, "UTF-8");

                            if (errorStr.trim().startsWith("{")) {
                                JSONObject errorObj = new JSONObject(errorStr);
                                finalErrorMsg = errorObj.optString("msg", "خطأ في السيرفر");
                                finalRequestId = errorObj.optString("requestID", requestId);
                            } else {
                                finalErrorMsg = "تأكد من اتصالك بالإنترنت.";
                            }
                        } catch (Exception e) {
                            errorLogMessage = "فشل في قراءة تفاصيل الخطأ";
                            finalErrorMsg = "فشل في قراءة تفاصيل الخطأ";
                        }
                    } else if (error.getMessage() != null) {
                        errorLogMessage = error.getMessage();
                    } else {
                        errorLogMessage = error.toString();
                        finalErrorMsg = "لا يوجد اتصال بالسيرفر، تأكد من الإنترنت.";
                    }
                    sendLog(context, "exeBuy", errorLogMessage, "user_id = " + userId, "UserUtils");
                    String finalErrorMsg1 = finalErrorMsg;
                    String finalRequestId1 = finalRequestId;
                    activity.runOnUiThread(() -> {
                        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
                        android.app.Dialog customDialog = new android.app.Dialog(activity);
                        customDialog.setContentView(dialogView);

                        if (customDialog.getWindow() != null) {
                            customDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                        }

                        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                        jp.wasabeef.blurry.Blurry.with(context)
                                .radius(15)
                                .sampling(2)
                                .onto(decorView);

                        customDialog.setOnDismissListener(d -> jp.wasabeef.blurry.Blurry.delete(decorView));

                        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
                        TextView tvMsg = dialogView.findViewById(R.id.tvMessage);
                        LinearLayout btnRetry = dialogView.findViewById(R.id.btnWhatsapp);
                        Button btnClose = dialogView.findViewById(R.id.btnCall);

                        tvTitle.setText("فشلت العملية");
                        tvMsg.setText(finalErrorMsg1);

                        if (btnRetry.getChildAt(0) instanceof TextView)
                            ((TextView) btnRetry.getChildAt(0)).setText("إعادة المحاولة");

                        btnClose.setVisibility(View.GONE);
                        btnRetry.setOnClickListener(new SingleClickListener() {
                            @Override
                            public void onSingleClick(View v) {
                                if (finalRequestId1 != null && !finalRequestId1.isEmpty()) {
                                    if (callback != null) {
                                        try {
                                            callback.onMethodSelected(cbId, false, Integer.parseInt(finalRequestId1));
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                customDialog.dismiss();
                            }
                        });

                        btnClose.setOnClickListener(v -> customDialog.dismiss());

                        View closeX = dialogView.findViewById(R.id.dialogCancelButton);
                        if (closeX != null) closeX.setOnClickListener(v -> customDialog.dismiss());

                        customDialog.show();
                    });
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


    public static void floosakexe(Context context, int cbId, String mobileNo, double amount, int userId, String notes,
                                  Integer bookingId, int trip_id, int pay_type, CashBankCallback2 callback) {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;
        ProgressDialog progressDialog = new ProgressDialog(context);

//        if (showGif == 1) {
//            showSuccessGif(1, activity, null);
//        } else {
        progressDialog.setMessage("جاري إرسال رمز التحقق...");
        progressDialog.setCancelable(false);
        progressDialog.show();
//        }
        String deviceId = getDeviceID(context);
        String deviceInfo = getDeviceInfo();
        String url = BASE_URL + "floosak-exe/" + cbId + "/?device_id=" + deviceId + "&device_info=" + deviceInfo;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("mobile_no", mobileNo);
            jsonBody.put("amount", amount);
//            jsonBody.put("cur_code", curCode);
            jsonBody.put("user_id", userId);
            jsonBody.put("notes", notes);
            jsonBody.put("booking_id", bookingId);
            jsonBody.put("trip_id", trip_id);
            jsonBody.put("pay_type", pay_type);

//            if (requestId != null && !requestId.isEmpty()) {
//                jsonBody.put("requestID", requestId);
//            }
        } catch (JSONException e) {
            sendLog(context, "floosakexe", String.valueOf(e.getMessage()), "user_id = " + userId, "UserUtils");

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
//                    hideSuccessGif(activity);
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

                        if (callback != null) {
                            callback.onMethodSelected(cbId, success, newIdFromSuccess);
                        }
                    }
                },
                error -> {
//                    hideSuccessGif(activity);
                    progressDialog.dismiss();
//                    sendLog(context, "exeBuy", String.valueOf(error.getMessage()), "user_id = " + userId, "UserUtils");

                    String finalErrorMsg = "حدث خطأ غير متوقع";
                    String finalRequestId = null;
                    String errorLogMessage = "Unknown Error";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorStr = new String(error.networkResponse.data, "UTF-8");
                            errorLogMessage = new String(error.networkResponse.data, "UTF-8");

                            if (errorStr.trim().startsWith("{")) {
                                JSONObject errorObj = new JSONObject(errorStr);
                                finalErrorMsg = errorObj.optString("msg", "خطأ في السيرفر");
//                                finalRequestId = errorObj.optString("requestID", requestId);
                            } else {
                                finalErrorMsg = "تأكد من اتصالك بالإنترنت.";
                            }
                        } catch (Exception e) {
                            errorLogMessage = "فشل في قراءة تفاصيل الخطأ";
                            finalErrorMsg = "فشل في قراءة تفاصيل الخطأ";
                        }
                    } else if (error.getMessage() != null) {
                        errorLogMessage = error.getMessage();
                    } else {
                        errorLogMessage = error.toString();
                        finalErrorMsg = "لا يوجد اتصال بالسيرفر، تأكد من الإنترنت.";
                    }
                    sendLog(context, "floosakexe", errorLogMessage, "user_id = " + userId, "UserUtils");
                    String finalErrorMsg1 = finalErrorMsg;
//                    String finalRequestId1 = finalRequestId;
                    activity.runOnUiThread(() -> {
                        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
                        android.app.Dialog customDialog = new android.app.Dialog(activity);
                        customDialog.setContentView(dialogView);

                        if (customDialog.getWindow() != null) {
                            customDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                        }

                        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                        jp.wasabeef.blurry.Blurry.with(context)
                                .radius(15)
                                .sampling(2)
                                .onto(decorView);

                        customDialog.setOnDismissListener(d -> jp.wasabeef.blurry.Blurry.delete(decorView));

                        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
                        TextView tvMsg = dialogView.findViewById(R.id.tvMessage);
                        LinearLayout btnRetry = dialogView.findViewById(R.id.btnWhatsapp);
                        Button btnClose = dialogView.findViewById(R.id.btnCall);

                        tvTitle.setText("فشلت العملية");
                        tvMsg.setText(finalErrorMsg1);

                        if (btnRetry.getChildAt(0) instanceof TextView)
                            ((TextView) btnRetry.getChildAt(0)).setText("إعادة المحاولة");

                        btnClose.setVisibility(View.GONE);
                        btnRetry.setOnClickListener(new SingleClickListener() {
                            @Override
                            public void onSingleClick(View v) {
//                                if (finalRequestId1 != null && !finalRequestId1.isEmpty()) {
                                if (callback != null) {
                                    try {
//                                            callback.onMethodSelected(cbId, false, Integer.parseInt(finalRequestId1));
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }
//                                }
                                customDialog.dismiss();
                            }
                        });

                        btnClose.setOnClickListener(v -> customDialog.dismiss());

                        View closeX = dialogView.findViewById(R.id.dialogCancelButton);
                        if (closeX != null) closeX.setOnClickListener(v -> customDialog.dismiss());

                        customDialog.show();
                    });
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

    public static void showCashBankBottomSheet(Context context, int trip_id, double price, int booking_id,
                                               int is_wallet_flg, CashBankCallback callback) {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.TransparentBottomDialog);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cash_bank, null);
        dialog.setContentView(dialogView);

        LinearLayout step1 = dialogView.findViewById(R.id.step1_Selection);
        LinearLayout step2 = dialogView.findViewById(R.id.step2_Inputs);
        LinearLayout container = dialogView.findViewById(R.id.paymentMethodsContainer);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etPurchaseCode = dialogView.findViewById(R.id.etPurchaseCode);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        etAmount.setText(String.valueOf(price));

        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        final String[] currentRequestId = {null};

        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                if (step2.getVisibility() == View.VISIBLE) {
                    step2.setVisibility(View.GONE);
                    step1.setVisibility(View.VISIBLE);

                    if (is_wallet_flg == 1) {
                        dialogTitle.setText("وسيلة الدفع");
                    } else {
                        dialogTitle.setText("الدفع عبر محفظة إلكترونية");
                    }
                    return true;
                } else if (step1.getVisibility() == View.VISIBLE) {
                    dialog.dismiss();
                    showGenericOptionsBottomSheet(context, 5, price, trip_id, 2, booking_id, callback != null ? (option, msg, success, req_id) -> {
                        if (callback instanceof GenericOptionsCallback) {
                            ((GenericOptionsCallback) callback).onOptionSelected(option, msg, success, req_id);
                        }
                    } : null);

                    return true;
                }
            }
            return false;
        });
        setEditTextState(etAmount, false);
        setEditTextState(etPurchaseCode, false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        }

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        Blurry.with(context).radius(15).sampling(2).onto(decorView);
        dialog.setOnDismissListener(d -> Blurry.delete(decorView));

        DBHelper dbHelper = new DBHelper(context);
        List<Map<String, Object>> methods = dbHelper.getAllCashBank(is_wallet_flg);
        final int[] selectedWalletId = {-1};

        for (Map<String, Object> method : methods) {
            int is_active = (int) method.get("is_active");

            // 2. التحقق مما إذا كانت المحفظة مفعلة
            // إذا كانت 0، نستخدم 'continue' لتخطي هذه المحفظة والانتقال للتالية
            if (is_active != 1) {
                continue;
            }
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_cash_bank, container, false);
            TextView txtName = itemView.findViewById(R.id.txtName);
            ImageView imgIcon = itemView.findViewById(R.id.imgIcon);

            int id = (int) method.get("cb_id");
            int comfirm_wallet_pay_flag = (int) method.get("comfirm_wallet_pay_flag");
            String name = (String) method.get("cb_name");
            String iconName = (String) method.get("wallet_icon");

            txtName.setText(name);

            if (is_wallet_flg == 1) {
                dialogTitle.setText("وسيلة الدفع");
                etNotes.setVisibility(View.VISIBLE);
                SharedPreferences prefs = SharedPrefsHelper.get(context);
                String full_name = prefs.getString("full_name", "");
                etNotes.setText(full_name + " (مستخدم التطبيق) ");
                setEditTextState(etNotes, false);
//                etPurchaseCode.setHint("رقم العملية");
            } else {
                dialogTitle.setText("الدفع عبر محفظة إلكترونية");
                etNotes.setVisibility(View.GONE);
//                etPurchaseCode.setHint("كود الشراء");
            }
//            int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
//            imgIcon.setImageResource(resId != 0 ? resId : R.drawable.wallet_setting);

            if (iconName != null && !iconName.isEmpty()) {
                String finalIconUrl = iconName.startsWith("http") ? iconName : ImageUrl + iconName;

                Glide.with(context)
                        .load(finalIconUrl)
                        .placeholder(R.drawable.wallet_setting)
                        .error(R.drawable.wallet_setting)
                        .into(imgIcon);
            } else {
                imgIcon.setImageResource(R.drawable.wallet_setting);
            }

            itemView.setOnClickListener(v -> {
                selectedWalletId[0] = id;

                String amountStr = etAmount.getText().toString().trim();
                if (amountStr.isEmpty()) amountStr = "0";
                double amount = Double.parseDouble(amountStr);

                SharedPreferences prefs = SharedPrefsHelper.get(context);
                int user_id = prefs.getInt("user_id", 0);
                String phone = prefs.getString("user_phone", "");
                String balanceStr = prefs.getString("user_balance", "0");

                int payTypeForServer = (is_wallet_flg == 1) ? 3 : 2;
                if (comfirm_wallet_pay_flag == 1) {
                    View dialogView2 = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
                    android.app.Dialog customDialog = new android.app.Dialog(activity);
                    customDialog.setContentView(dialogView2);

                    if (customDialog.getWindow() != null) {
                        customDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                    }

                    ViewGroup decorView2 = (ViewGroup) activity.getWindow().getDecorView();
                    jp.wasabeef.blurry.Blurry.with(context)
                            .radius(15)
                            .sampling(2)
                            .onto(decorView2);

                    customDialog.setOnDismissListener(d -> jp.wasabeef.blurry.Blurry.delete(decorView2));

                    TextView tvTitle = dialogView2.findViewById(R.id.tvTitle);
                    TextView tvMsg = dialogView2.findViewById(R.id.tvMessage);
                    LinearLayout btnRetry = dialogView2.findViewById(R.id.btnWhatsapp);
                    Button btnClose = dialogView2.findViewById(R.id.btnCall);

                    tvTitle.setText("تأكيد عملية الدفع");
                    btnClose.setText("إلغاء");
                    tvMsg.setText("دفع مبلغ " + amount + " ريال من محفظة \"" + name + "\" المرتبطة بالرقم \"" + phone + "\". هل تود الاستمرار؟");

                    if (btnRetry.getChildAt(0) instanceof TextView)
                        ((TextView) btnRetry.getChildAt(0)).setText("تأكيد");

//                    btnClose.setVisibility(View.GONE);
                    btnRetry.setOnClickListener(new SingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            floosakexe(context, selectedWalletId[0], phone, amount, user_id, "",
                                    booking_id, trip_id, payTypeForServer, new CashBankCallback2() {

                                        @Override
                                        public void onMethodSelected(int id, boolean success, String request_id) {
                                            if (success) {
                                                currentRequestId[0] = String.valueOf(request_id);
                                                step1.setVisibility(View.GONE);
                                                step2.setVisibility(View.VISIBLE);

                                            } else {
                                                ToastMessages(activity, "فشل طلب العملية، يرجى المحاولة لاحقاً");
                                            }
                                        }
                                    });


                            if (id == 3) {
                                etPurchaseCode.setHint("كود الشراء");
                            } else {
                                etPurchaseCode.setHint("رمز التحقق");
                            }
                            dialogTitle.setText("تأكيد الدفع عبر " + name);
                            customDialog.dismiss();
                        }
                    });

                    btnClose.setOnClickListener(v2 -> customDialog.dismiss());

                    View closeX = dialogView2.findViewById(R.id.dialogCancelButton);
                    if (closeX != null) closeX.setOnClickListener(v2 -> customDialog.dismiss());

                    customDialog.show();
//                    new androidx.appcompat.app.AlertDialog.Builder(context)
//                            .setTitle("تأكيد عملية الدفع")
//                            .setMessage("سيتم دفع مبلغ " + amount + " ريال من محفظة \"" + name + "\" المرتبطة بالرقم \"" + phone + "\". هل تود الاستمرار؟")
//                            .setCancelable(false)
//                            .setPositiveButton("تأكيد", (confirmDialog, which) -> {
//
//                            })
//                            .setNegativeButton("إلغاء", (confirmDialog, which) -> {
//                                confirmDialog.dismiss();
//                                currentRequestId[0] = null;
//                            })
//                            .show();

                } else {
                    step1.setVisibility(View.GONE);
                    step2.setVisibility(View.VISIBLE);
                    etPurchaseCode.setHint("رقم العملية");
                    dialogTitle.setText("تأكيد الدفع عبر " + name);
                }
                if (id == 3) {
                    etPurchaseCode.setHint("كود الشراء");
                }
//                dialogTitle.setText("تأكيد الدفع عبر " + name);
            });
            container.addView(itemView);
        }


        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String amountStr = etAmount.getText().toString().trim();
                String code = etPurchaseCode.getText().toString().trim();
                etPurchaseCode.setError(null);
                etAmount.setError(null);

                if (amountStr.isEmpty()) {
                    etAmount.setError("يرجى إدخال المبلغ");
                    etAmount.requestFocus();
                    return;
                }
                if (amountStr.equals("0.0") || amountStr.equals("0")) {
                    etAmount.setError("المبلغ يجب أن يكون أكبر من صفر");
                    etAmount.requestFocus();
                    return;
                }

                if (code.isEmpty()) {
                    String errorMsg = (is_wallet_flg == 1) ? "يرجى إدخال رقم العملية" : "يرجى إدخال كود الشراء";
                    etPurchaseCode.setError(errorMsg);
                    etPurchaseCode.requestFocus();
                    return;
                }

                if (amountStr.isEmpty() || code.isEmpty()) return;

                double amount = Double.parseDouble(amountStr);
                SharedPreferences prefs = SharedPrefsHelper.get(context);
                int user_id = prefs.getInt("user_id", 0);
                String phone = prefs.getString("user_phone", "");

                String balanceStr = prefs.getString("user_balance", "0");
                double balance = Double.parseDouble(balanceStr.replace(",", "").trim());
                double minAmount;

                if (balance < 0) {
                    minAmount = price;
                } else {
                    minAmount = price - balance;
                    if (minAmount < 0) minAmount = 0;
                }

                if (amount < minAmount) {
                    ToastMessages(activity, "المبلغ يجب أن لا يقل عن " + minAmount);
                    return;
                }
//                if (selectedWalletId[0] == 1 && balance < 0) {
//                    ToastMessages(activity, "رصيدك سالب، يجب دفع كامل المبلغ");
//                }
                int payTypeForServer = (is_wallet_flg == 1) ? 3 : 2;
                exeBuy(context, selectedWalletId[0], phone, code, amount, "", user_id, "",
                        booking_id, trip_id, payTypeForServer, currentRequestId[0], new CashBankCallback() {

                            @Override
                            public void onMethodSelected(int id, boolean success, int request_id) {

                                currentRequestId[0] = String.valueOf(request_id);
//
                                if (success) {
//                                    if (id == 6) {
////                                            ToastMessages(activity, "تم إرسال رمز التحقق بنجاح");
//                                    } else {
                                    if (callback != null) {
                                        callback.onMethodSelected(id, true, Integer.parseInt(currentRequestId[0]));
                                    }
                                    dialog.dismiss();
//                                    }
                                }

                            }
                        });
            }
        });

        dialog.show();

    }

//    private static void showTransferInputDialog(Context context, int trip_id, int codeNo,
//                                                double price, CashBankCallback callback) {
//        if (!(context instanceof Activity)) return;
//
//        Activity activity = (Activity) context;
//
//        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.TransparentBottomDialog);
//
//        View view = LayoutInflater.from(context).inflate(R.layout.dialog_manual_transfer, null);
//        dialog.setContentView(view);
//
//        EditText etAmount = view.findViewById(R.id.etAmount);
//        EditText etTransferNo = view.findViewById(R.id.etTransferNo);
//        EditText etNotes = view.findViewById(R.id.etNotes);
//        Button btnSave = view.findViewById(R.id.btnSave);
//        SharedPreferences prefs = SharedPrefsHelper.get(context);
//

    /// /                SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
//        String full_name = prefs.getString("full_name", "");
//        etAmount.setText("" + price);
//        etNotes.setText(full_name + " (مستخدم التطبيق) ");
//        setEditTextState(etAmount, false);
//        setEditTextState(etTransferNo, false);
//        setEditTextState(etNotes, false);
//        if (dialog.getWindow() != null) {
//            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
//            if (bottomSheet != null) {
//                bottomSheet.setBackgroundResource(android.R.color.transparent);
//            }
//        }
//        final String[] currentRequestId = {null};
//
//        btnSave.setOnClickListener(v -> {
//            String amountStr = etAmount.getText().toString().trim();
//            String refNo = etTransferNo.getText().toString().trim();
//            String notes = etNotes.getText().toString().trim();
//            int user_id = prefs.getInt("user_id", 0);
//            String phone = prefs.getString("user_phone", "");
//
//            if (amountStr.isEmpty() || refNo.isEmpty()) {
//                ToastMessages(activity, "يرجى ملء كافة الحقول");
//                return;
//            }
//
//            double amount = Double.parseDouble(amountStr);
//            double balance = Double.parseDouble(prefs.getString("user_balance", "0").replace(",", ""));
//
//            double minAmount = (balance < 0) ? price : Math.max(0, price - balance);
//
//            if (amount < minAmount) {
//                ToastMessages(activity, "المبلغ يجب أن لا يقل عن " + minAmount);
//                return;
//            }
//
//            showSuccessGif(2, activity, null);
//
//            exeBuy(context, 3, phone, refNo, amount, "", user_id, notes,
//                    null, trip_id, 2, currentRequestId[0], new CashBankCallback() {
//
//                        @Override
//                        public void onMethodSelected(int id, boolean success, int request_id) {
//                            hideSuccessGif(activity);
//
//                            if (success) {
//                                if (callback != null) {
//                                    callback.onMethodSelected(id, true, request_id);
//                                }
//                                dialog.dismiss();
//                            } else {
//                                currentRequestId[0] = String.valueOf(request_id);
//                            }
//                        }
//                    });
//        });
//
//        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
//        Blurry.with(context).radius(15).sampling(2).onto(decorView);
//        dialog.setOnDismissListener(d -> Blurry.delete(decorView));
//
//        dialog.show();
//    }
//
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
                        callback.onSuccess("تم تحديث البيانات بنجاح");
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

                            dbHelper.addCountry(id, country_name);
                        }
                        callback.onSuccess("تم تحديث البيانات بنجاح");
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

                            // حفظ الرسائل في SQLite
                            dbHelper.insertOrUpdateCity(id, city_name, city_name_en, country_id, city_code);
                        }
                        callback.onSuccess("تم تحديث البيانات بنجاح");
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
//                String manufacturer = Build.MANUFACTURER;
//                String model = Build.MODEL;
//                String deviceName = model.toLowerCase().startsWith(manufacturer.toLowerCase()) ? model : manufacturer + " " + model;
//                String osVersion = Build.VERSION.RELEASE;
//                int sdkVersion = Build.VERSION.SDK_INT;
//                String deviceInfo = "Device: " + deviceName + ", OS: " + osVersion + " (SDK " + sdkVersion + ")";

                URL url = new URL(BASE_URL + "s_log/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Bearer " + 1);
                SharedPreferences prefs = SharedPrefsHelper.get(context);

//                SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int user_id = prefs.getInt("user_id", 0);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("user_id", user_id);
                jsonBody.put("log_name", "sendLog2");
                jsonBody.put("log_text", logText);
                jsonBody.put("log_body", getDeviceInfo());
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

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("user_id", user_id);
                jsonBody.put("log_name", logName);
                jsonBody.put("log_text", logText);
                jsonBody.put("log_body", "app_version = " + app_version + " " + logBody);
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
                            String cb_name = obj.getString("cb_name");
                            String wallet_icon = obj.optString("wallet_icon", "");
                            dbHelper.saveCashBank(cb_id, cb_name, wallet_icon, is_wallet_flg, comfirm_wallet_pay_flag, is_active);
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

    public interface OnMessageSentListener {
        void onSuccess(String message);

        void onError(String error);
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

            // حذف الضبابية باستخدام الـ Activity الممررة
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

//    public static void showSuccessGif(int type_img, Activity activity, Runnable onDismiss) {
//        if (activity == null || activity.isFinishing()) return;
//
//        Dialog dialog = new Dialog(activity);
//        dialog.setContentView(R.layout.dialog_gif_loading);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//
//        if (dialog.getWindow() != null) {
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
//            Blurry.with(activity).radius(25).sampling(2).onto(decorView);
//        }
//
//        ImageView gifImageView = dialog.findViewById(R.id.gifImageView);
//        TextView tvCountdown = dialog.findViewById(R.id.tvCountdown);
//
//        if (type_img == 1) {
//            Glide.with(activity).asGif().load(R.drawable.saved_successfully).into(gifImageView);
//        } else {
//            Glide.with(activity).asGif().load(R.drawable.saved_successfully2).into(gifImageView);
//        }
//        dialog.show();
//
//        final int[] count = {1};
//        Handler handler = new Handler(Looper.getMainLooper());
//        Runnable countdownRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (count[0] > 0) {
//                    tvCountdown.setText(String.valueOf(count[0]));
//                    count[0]++;
//                    handler.postDelayed(this, 1000);
//                } else {
//                    try {
//                        if (dialog.isShowing() && !activity.isFinishing()) {
//                            dialog.dismiss();
//                            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
//                            Blurry.delete(decorView);
//
//                            if (onDismiss != null) {
//                                onDismiss.run();
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        handler.post(countdownRunnable);
//    }

    public static void app_Page(Context context, int pageId) {
        SharedPreferences prefs = SharedPrefsHelper.get(context);
//        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int user_id = prefs.getInt("user_id", 0);
        String token = prefs.getString("auth_token", null);

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


                    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        sendLog(context, "app_Page", String.valueOf(responseCode), "user_id = " + user_id + " pageId = " + pageId, "UserUtils");
                    } else {
                        sendLog(context, "app_Page", String.valueOf(responseCode), "user_id = " + user_id + " pageId = " + pageId, "UserUtils");
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    sendLog(context, "app_Page", e.toString(), "user_id = " + user_id + " pageId = " + pageId, "UserUtils");
                }
            }).start();
        } else {

        }
    }

}
