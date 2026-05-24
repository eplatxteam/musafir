package com.example.musafir;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class otp_activity extends AppCompatActivity {
    String password, user_type, user_name, token, page, phones;
    String BASE_URL = UserUtils.BASE_URL;
    DBHelper dbHelper = new DBHelper(this);
    TextView tvTimer;
    LinearLayout btnResend, layoutResendWhatsapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

//        EditText otpEditText = findViewById(R.id.otpEditText);
        final EditText[] otpFields = {
                findViewById(R.id.otp1), findViewById(R.id.otp2),
                findViewById(R.id.otp3), findViewById(R.id.otp4),
                findViewById(R.id.otp5), findViewById(R.id.otp6)
        };
        EditText otp1 = findViewById(R.id.otp1);
        EditText otp2 = findViewById(R.id.otp2);
        EditText otp3 = findViewById(R.id.otp3);
        EditText otp4 = findViewById(R.id.otp4);
        EditText otp5 = findViewById(R.id.otp5);
        EditText otp6 = findViewById(R.id.otp6);
        otp1.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        Button verifyOtpButton = findViewById(R.id.verifyOtpButton);
        btnResend = findViewById(R.id.layoutResendSMS);
        layoutResendWhatsapp = findViewById(R.id.layoutResendWhatsapp);
        tvTimer = findViewById(R.id.tvTimer);
        ImageView backButton = findViewById(R.id.backButton);
        MaterialButton tvChangeNumber = findViewById(R.id.tvChangeNumber);
        TextView tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
//        tvChangeNumber.setOnClickListener(v -> finish());
        tvChangeNumber.setOnClickListener(v -> onBackPressed());
        backButton.setOnClickListener(v -> finish());

        token = getIntent().getStringExtra("otp_token");
        phones = getIntent().getStringExtra("phone");
        String inviteCode = getIntent().getStringExtra("inviteCode");
        password = getIntent().getStringExtra("password");
        page = getIntent().getStringExtra("page");
        user_type = getIntent().getStringExtra("user_type");
        user_name = getIntent().getStringExtra("user_name");
        tvPhoneNumber.setText(phones);
        for (int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;

            UserUtils.setEditTextState(otpFields[i], false);

            otpFields[i].addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (s.length() == 0 && currentIndex > 0) {
                        otpFields[currentIndex - 1].requestFocus();
                    }
                }
            });
            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // إذا كان الحقل الحالي فارغاً وهناك حقل سابق متوفر
                    if (otpFields[currentIndex].getText().length() == 0 && currentIndex > 0) {

                        // 1. نقل التركيز إلى الحقل السابق مباشرة
                        otpFields[currentIndex - 1].requestFocus();

                        // 2. نقل مؤشر الكتابة (Cursor) إلى نهاية النص في الحقل السابق (حتى لا يكتب في البداية)
                        if (otpFields[currentIndex - 1].getText().length() > 0) {
                            otpFields[currentIndex - 1].setSelection(otpFields[currentIndex - 1].getText().length());
                        }

                        return true;
                    }
                }
                return false;
            });
        }

        verifyOtpButton.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (EditText et : otpFields) {
                sb.append(et.getText().toString().trim());
            }
            String otpCode = sb.toString();

            if (otpCode.length() < 6) {
                UserUtils.ToastMessages(this, UserUtils.getMessageFromLocalNew(322, dbHelper));
                return;
            }

            if (page.equals("1")) {
                sendOtpVerification(token, otpCode, phones);
            } else {
                if (otpCode.length() == 6) {
                    register(user_name, phones, password, user_type, token, otpCode, inviteCode);
                } else {
                    UserUtils.getMessageFromLocal(55, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(otp_activity.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
            }
        });

        SharedPreferences otpPrefs = getSharedPreferences("OTPLimits", MODE_PRIVATE);
        long firstAttemptTime = otpPrefs.getLong("first_attempt_time", 0);
        int count = otpPrefs.getInt("otp_count", 0);
        long currentTime = System.currentTimeMillis();
        long oneHour = TimeUnit.HOURS.toMillis(1);

        if (count >= 5 && (currentTime - firstAttemptTime) < oneHour) {
            long timeLeft = oneHour - (currentTime - firstAttemptTime);
            startResendTimer(timeLeft, true);
        } else {
            btnResend.setEnabled(true);
        }

        layoutResendWhatsapp.setOnClickListener(v -> resendOtp(phones, 2));
        btnResend.setOnClickListener(v -> resendOtp(phones, 1));
//        setPressAnimation(btnResend);
//        setPressAnimation(layoutResendWhatsapp);
//        btnResend.setOnClickListener(v -> resendOtp(phones));

// الحقل الأول يقبل 6 مؤقتاً من أجل عملية اللصق (Paste)
        otp1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

// بقية الحقول مقفلة تماماً على رقم واحد فقط منذ البداية
        otp2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        otp3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        otp4.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        otp5.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        otp6.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        otp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // إذا تم لصق نص طوله 6 أرقام
                if (s.length() == 6) {
                    String pastedText = s.toString().trim();

                    // 1. توزيع الأرقام على المصفوفة (التوزيع يتجاهل قيود maxLength لأنه برمجي)
                    for (int i = 0; i < otpFields.length; i++) {
                        if (i < pastedText.length()) {
                            otpFields[i].setText(String.valueOf(pastedText.charAt(i)));
                        }
                    }

                    // 2. قفل الحقل الأول فوراً ليكون حده الأقصى 1 فقط مثل البقية
                    otp1.setFilters(new android.text.InputFilter[]{
                            new android.text.InputFilter.LengthFilter(1)
                    });

                    // 3. نقل التركيز للحقل الأخير
                    otp6.requestFocus();
                    if (otp6.getText().length() > 0) {
                        otp6.setSelection(otp6.getText().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // حماية إضافية: إذا دخل أكثر من رقم في أي وقت، خذ الرقم الأول فقط
                if (s.length() > 1) {
                    String firstChar = String.valueOf(s.charAt(0));
                    otp1.setText(firstChar);
                    otp1.setSelection(1);
                }
            }
        });
    }

    public void setPressAnimation(View view) {
        // إضافة تأثير التموج الشفاف (Ripple)
        TypedValue outValue = new TypedValue();
        view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setBackgroundResource(outValue.resourceId);

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.2f).scaleY(0.2f).alpha(0.8f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        if (!registerCompleted) {
            UserUtils.sendLog(this, "Register", "User exited before completing OTP verification",
                    getFullUserDataLog(), "otp_activity");
        }
        super.onBackPressed();
    }

    private void sendOtpVerification(String token, String code, String phone) {
        if (!UserUtils.isNetworkAvailable(this)) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(otp_activity.this, message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري التحقق من الرمز...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = BASE_URL + "otp_valid/";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.optString("result", "FALSE");


                        if (result.equalsIgnoreCase("TRUE")) {
                            UserUtils.getMessageFromLocal(56, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(otp_activity.this, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            Intent intent;
                            if ("0".equals(password)) {
                                intent = new Intent(otp_activity.this, HomePage.class);
                                intent.putExtra("user_phone", phone);
                                intent.putExtra("is_just_logged_in", true);

                            } else {
                                intent = new Intent(otp_activity.this, ChangePassword.class);
                                intent.putExtra("user_phone", phone);
                                String Tokenuser = getIntent().getStringExtra("Tokenuser");
                                intent.putExtra("Tokenuser", Tokenuser);

                                intent.putExtra("otp_token", token);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            UserUtils.getMessageFromLocal(57, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(otp_activity.this, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                        }

                    } catch (JSONException e) {
                        UserUtils.getMessageFromLocal(57, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(otp_activity.this, message);
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });
                        UserUtils.sendLog(this, "sendOtpVerification", e.toString(), e.toString(), "otp activity");
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(otp_activity.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                    UserUtils.sendLog(this, "sendOtpVerification", error.toString(), error.toString(), "otp activity");
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("p_token", token);
                params.put("p_otp_code", code);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private boolean registerCompleted = false;
    private boolean logSent = false;

    private void register(String fullName, String phone, String password, String user_type,
                          String register_otp_token, String otp_code, String inviteCode) {
        if (!UserUtils.isNetworkAvailable(this)) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(otp_activity.this, message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري إنشاء حساب...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        @SuppressLint("HardwareIds") String deviceSerial = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        new Thread(() -> {
            try {
                String finalPhone;
                String finalPhone2;
                URL url = new URL(BASE_URL + "auth/register/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);
//                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences prefs = SharedPrefsHelper.get(this);

                String defaultCity = prefs.getString("default_city", "حدد المدينة");
                String country = prefs.getString("country_code", "");
                int defaultCityId = prefs.getInt("default_city_id", -1);
                String safePhone = (phone == null) ? "" : phone;
                String safeInviteCode = (inviteCode == null) ? "" : inviteCode;

                // بناء JSON
                if (safePhone.startsWith("05")) {
                    finalPhone = "966" + safePhone.substring(1);
                } else if (safePhone.startsWith("7")) {
                    finalPhone = "967" + safePhone;
                } else {
                    finalPhone = safePhone;
                }

                // 3. معالجة كود الدعوة (الذي تسبب في الخطأ)
                if (safeInviteCode.startsWith("05")) {
                    finalPhone2 = "966" + safeInviteCode.substring(1);
                } else if (safeInviteCode.startsWith("7")) {
                    finalPhone2 = "967" + safeInviteCode;
                } else {
                    finalPhone2 = safeInviteCode;
                }
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("full_name", fullName);
                jsonParam.put("inviteCode", finalPhone2);
                jsonParam.put("phone_number", finalPhone);
                jsonParam.put("password", password);
                jsonParam.put("user_type", user_type);
                jsonParam.put("device_serial", deviceSerial);
                jsonParam.put("default_city", defaultCity);
                jsonParam.put("country", country);
                jsonParam.put("city_id", defaultCityId);
                jsonParam.put("register_otp_token", register_otp_token);
                jsonParam.put("otp_code", otp_code);
                String s = "{ full_name: " + fullName +
                        " , phone_number: " + finalPhone +
                        " , inviteCode: " + finalPhone2 +
                        " , password: " + password +
                        " , user_type: " + user_type +
                        " , city_id: " + defaultCityId +
                        " , device_serial: " + deviceSerial +
                        " , register_otp_token: " + register_otp_token +
                        " , otp_code: " + otp_code +
                        " , default_city: " + defaultCity +
                        " }";
                Log.e("=====--", s);
                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                InputStream is;
                if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    is = conn.getErrorStream();
                } else {
                    is = conn.getInputStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
//                        boolean success = jsonObject.optBoolean("success", false);
                        if (jsonObject.has("user_id")) {
                            registerCompleted = true;

                            int userId = jsonObject.getInt("user_id");
                            String token = jsonObject.getString("token");
                            String is_active = jsonObject.getString("is_active");
                            String is_verified = jsonObject.getString("is_verified");
                            String ip = jsonObject.getString("ip");
                            String notify_general = jsonObject.getString("notify_general");
                            String notify_primary = jsonObject.getString("notify_primary");
                            String default_city = jsonObject.getString("default_city");
                            String cur_code = jsonObject.getString("cur_code");

                            SharedPreferences.Editor editor = prefs.edit();

                            editor.putString("user_phone", finalPhone);
                            editor.putString("auth_token", token);
                            editor.putString("full_name", fullName);
                            editor.putString("is_active", is_active);
                            editor.putInt("user_id", userId);
                            editor.putString("is_verified", is_verified);
                            editor.putString("ip", ip);
                            editor.putString("notify_general", notify_general);
                            editor.putString("notify_primary", notify_primary);
                            editor.putString("default_city", default_city);
                            editor.putString("device_serial", deviceSerial);
                            editor.putString("cur_code", cur_code);
                            editor.putString("user_type", user_type);
                            String firstName = fullName.trim().split("\\s+")[0];
                            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase(); // Capitalize أول حرف فقط

                            editor.apply();
                            String finalFirstName = firstName;
                            SharedPreferences invitePrefs = getSharedPreferences("InvitePrefs", MODE_PRIVATE);
                            invitePrefs.edit().clear().apply();
                            UserUtils.getMessageFromLocal(43, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(otp_activity.this, message + finalFirstName);
                                }

                                @Override
                                public void onError(String error) {
                                }

                            });
                            UserUtils.fetchBalance(this);

                            UserUtils.checkAppUpdate(this);
                            UserUtils.fetchCompany(this, new UserUtils.OnCodesFetchedListener() {
                                @Override
                                public void onFetched(JSONArray response) {
                                }

                                @Override
                                public void onError(String error) {
                                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            UserUtils.ToastMessages(otp_activity.this, message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }
                                    });
                                }
                            });
                            UserUtils.fetchCodeDetails(this, 5, null, new UserUtils.OnCodesFetchedListener() {
                                @Override
                                public void onFetched(JSONArray response) {
                                }

                                @Override
                                public void onError(String error) {

                                }
                            });
                            UserUtils.fetchAndSavePayTypes(this, new UserUtils.GenericCallback() {

                                @Override
                                public void onSuccess(String message) {
                                }

                                @Override
                                public void onError(String error) {

                                }
                            });
                            UserUtils.fetchAndSavecities(this, new UserUtils.citiesCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean("messages_fetched", true);
                                    editor.apply();
                                }

                                @Override
                                public void onError(String error) {

                                }
                            });
                            UserUtils.fetchAndSaveCountry(this, new UserUtils.FetchCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    prefs.edit().putBoolean("messages_fetched", true).apply();
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            UserUtils.fetchCashBankData(this, dbHelper, new UserUtils.OnCashBankFetchedListener() {
                                @Override
                                public void onFetched(List<DBHelper.CashBank> types) {
                                }

                                @Override
                                public void onError(String error) {

                                }
                            });
                            UserUtils.syncDayTimesFromServer(this, new UserUtils.DayTimeCallback() {

                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(String error) {

                                }
                            });
                            UserUtils.fetchAndSaveMessages(this, new UserUtils.FetchCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean("messages_fetched", true);
                                    editor.apply();
                                }

                                @Override
                                public void onError(String error) {
                                }


                            });
                            UserUtils.fetchServiceHome(this, dbHelper, new PageHome.OnServiceHomeFetchedListener() {
                                @Override
                                public void onFetched(List<DBHelper.ServiceHome> types) {

                                }

                                @Override
                                public void onError(String error) {

                                }
                            });
                            UserUtils.fetchTypeTravelerRequests(this, dbHelper, new TravelerRequests.OnTypeRequestsFetchedListener() {
                                @Override
                                public void onFetched(List<DBHelper.TypeTravelerRequest> types) {

                                }

                                @Override
                                public void onError(String error) {

                                }
                            });
                            UserUtils.fetchAndSaveContactInfo(this, dbHelper);

                            UserUtils.fetchRoutes(this, new UserUtils.FetchCallback() {
                                @Override
                                public void onSuccess(String message) {
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            UserUtils.loadVehicleTypesToDB(this);

                            Intent intent = new Intent(otp_activity.this, HomePage.class);
                            intent.putExtra("is_just_logged_in", true);
                            intent.putExtra("user_phone", finalPhone);
                            startActivity(intent);
                            finish();

                        } else {
                            String errorReason;
                            String fullResponse = result.toString();
                            if (jsonObject.has("phone_number")) {

                                errorReason = "Phone already exists";

                                UserUtils.getMessageFromLocal(44, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(otp_activity.this, message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                                progressDialog.dismiss();

                            } else if (jsonObject.has("msg_txt")) {
                                UserUtils.ToastMessages(otp_activity.this, "رمز التحقق غير صحيح أو منتهي");

                            } else {
                                String msg_txt = jsonObject.has("msg_txt") ? UserUtils.getMessageFromLocalNew(45, dbHelper) : "Registration failed";
//
                                UserUtils.ToastMessages(otp_activity.this, msg_txt);
//

                                progressDialog.dismiss();

                            }
                            UserUtils.sendLog(this, "Register", fullResponse, s, "otp_activity");

                            progressDialog.dismiss();

                        }
                    } catch (JSONException e) {
                        UserUtils.sendLog(this, "Register", e.toString(), s, "otp_activity");
                        UserUtils.getMessageFromLocal(45, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(otp_activity.this, message);
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });
                        progressDialog.dismiss();

                    }
                });
            } catch (Exception e) {
                UserUtils.sendLog(this, "Register", e.toString(), e.toString(), "otp_activity");
                runOnUiThread(() -> UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(otp_activity.this, message);
                    }

                    @Override
                    public void onError(String error) {
                    }
                }));
                progressDialog.dismiss();
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!registerCompleted && !logSent) {
            logSent = true;
            UserUtils.sendLog(this, "Register", "User left app before completing registration",
                    getFullUserDataLog() + user_name, "otp_activity");
        }
    }

    private String getFullUserDataLog() {
        String name = getIntent().getStringExtra("user_name");
        String phone = getIntent().getStringExtra("phone");
        String pass = getIntent().getStringExtra("password");
        String type = getIntent().getStringExtra("user_type");
        String invite = getIntent().getStringExtra("inviteCode");
        return "Phone: " + phone +
                " | Name: " + name +
                " | Password: " + pass +
                " | UserType: " + type +
                " | Page: " + page +
                " | invite: " + invite +
                " | OTP_Token: " + token +
                " | DeviceInfo: " + UserUtils.getDeviceInfo() +
                " | DeviceID: " + UserUtils.getDeviceID(this) +
                " | InviteCode: " + getIntent().getStringExtra("inviteCode");
    }

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    private void startResendTimer(long durationMillis, boolean isLongWait) {
        if (countDownTimer != null) countDownTimer.cancel(); // إلغاء أي عداد سابق

        btnResend.setEnabled(false);
        btnResend.setAlpha(0.5f);
        layoutResendWhatsapp.setEnabled(false);
        layoutResendWhatsapp.setAlpha(0.5f);
        isTimerRunning = true;

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setVisibility(View.VISIBLE);

                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(hours);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished));

                String timeString = (hours > 0)
                        ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        : String.format("%02d:%02d", minutes, seconds);

                if (isLongWait) {
                    tvTimer.setText(UserUtils.getMessageFromLocalNew(335, dbHelper) + " " + timeString);
                } else {
                    tvTimer.setText(UserUtils.getMessageFromLocalNew(336, dbHelper) + " " + timeString);
                }
            }

            @Override
            public void onFinish() {
                btnResend.setEnabled(true);
                btnResend.setAlpha(1.0f);
                layoutResendWhatsapp.setEnabled(true);
                layoutResendWhatsapp.setAlpha(1.0f);
                tvTimer.setVisibility(View.GONE);
                isTimerRunning = false;
            }
        }.start();
    }


    private void resendOtp(String phone, int p_otp_typ) {
        if (isTimerRunning) return;

        SharedPreferences otpPrefs = getSharedPreferences("OTPLimits", MODE_PRIVATE);
        long firstAttemptTime = otpPrefs.getLong("first_attempt_time", 0);
        int count = otpPrefs.getInt("otp_count", 0);
        long currentTime = System.currentTimeMillis();
        long oneHour = TimeUnit.HOURS.toMillis(1);

        if (firstAttemptTime != 0 && (currentTime - firstAttemptTime) > oneHour) {
            count = 0;
            firstAttemptTime = 0;
            otpPrefs.edit().putInt("otp_count", 0).putLong("first_attempt_time", 0).apply();
        }

        if (count >= 5) {
            long timeLeftMillis = oneHour - (currentTime - firstAttemptTime);
            if (timeLeftMillis > 0) {
                startResendTimer(timeLeftMillis, true);
                UserUtils.ToastMessages(this, UserUtils.getMessageFromLocalNew(337, dbHelper));
                return;
            }
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري إرسال رمز التحقق...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "send_otp/",
                response -> {
                    progressDialog.dismiss();

                    // التحديث الهام: استخراج التوكن الجديد وتحديث المتغير العام للاكتيفيتي فوراً
                    if (response != null && !response.isEmpty() && !response.contains("error")) {
                        this.token = response.replace("\"", "").trim();
                    }

                    int newCount = otpPrefs.getInt("otp_count", 0) + 1;
                    SharedPreferences.Editor editor = otpPrefs.edit();
                    if (newCount == 1) {
                        editor.putLong("first_attempt_time", System.currentTimeMillis());
                    }
                    editor.putInt("otp_count", newCount);
                    editor.apply();

                    if (newCount >= 4) {
                        startResendTimer(oneHour, true);
                    } else {
                        startResendTimer(60000, false);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    UserUtils.sendLog(this, "resendOtp_Error", error.toString(), "", "otp_activity");
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("P_mobile", phone);
                params.put("p_otp_typ", String.valueOf(p_otp_typ));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}