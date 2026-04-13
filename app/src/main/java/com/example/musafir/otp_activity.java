package com.example.musafir;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
    TextView btnResend, tvTimer;

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
        otp1.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        Button verifyOtpButton = findViewById(R.id.verifyOtpButton);
        btnResend = findViewById(R.id.registerText);
        tvTimer = findViewById(R.id.tvTimer);
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
        token = getIntent().getStringExtra("otp_token");
        phones = getIntent().getStringExtra("phone");
        String inviteCode = getIntent().getStringExtra("inviteCode");
        password = getIntent().getStringExtra("password");
        page = getIntent().getStringExtra("page");
        user_type = getIntent().getStringExtra("user_type");
        user_name = getIntent().getStringExtra("user_name");

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
        }

        verifyOtpButton.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (EditText et : otpFields) {
                sb.append(et.getText().toString().trim());
            }
            String otpCode = sb.toString();

            if (otpCode.length() < 6) {
                Toast.makeText(this, "يرجى إدخال الرمز كاملاً", Toast.LENGTH_SHORT).show();
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

        btnResend.setOnClickListener(v -> resendOtp(phones));
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
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
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

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري إنشاء حساب...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        @SuppressLint("HardwareIds") String deviceSerial = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        new Thread(() -> {
            try {
                String finalPhone;
                URL url = new URL(BASE_URL + "auth/register/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);
//                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences prefs = SharedPrefsHelper.get(this);

                String defaultCity = prefs.getString("default_city", "حدد المدينة");
                int defaultCityId = prefs.getInt("default_city_id", -1);

                // بناء JSON
                if (phone.startsWith("05")) {
                    finalPhone = "966" + phone.substring(1);
                } else if (phone.startsWith("7")) {
                    finalPhone = phone;
                } else {
                    finalPhone = phone;
                }
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("full_name", fullName);
                jsonParam.put("inviteCode", inviteCode);
                jsonParam.put("phone_number", finalPhone);
                jsonParam.put("password", password);
                jsonParam.put("user_type", user_type);
                jsonParam.put("device_serial", deviceSerial);
                jsonParam.put("default_city", defaultCity);
                jsonParam.put("city_id", defaultCityId);
                jsonParam.put("register_otp_token", register_otp_token);
                jsonParam.put("otp_code", otp_code);
                String s = "{ full_name: " + fullName +
                        " , phone_number: " + finalPhone +
                        " , inviteCode: " + inviteCode +
                        " , password: " + password +
                        " , user_type: " + user_type +
                        " , city_id: " + defaultCityId +
                        " , device_serial: " + deviceSerial +
                        " , register_otp_token: " + register_otp_token +
                        " , otp_code: " + otp_code +
                        " , default_city: " + defaultCity +
                        " }";
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

                            UserUtils.checkAppUpdate(this);
                            UserUtils.fetchCompany(this, new UserUtils.OnCodesFetchedListener() {
                                @Override
                                public void onFetched(JSONArray response) {
                                }

                                @Override
                                public void onError(String error) {
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
                            });
                            UserUtils.fetchCodeDetails(this, 5, null, new UserUtils.OnCodesFetchedListener() {
                                @Override
                                public void onFetched(JSONArray response) {
                                }

                                @Override
                                public void onError(String error) {
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
                            });
                            UserUtils.fetchAndSaveCountry(this, new UserUtils.FetchCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    prefs.edit().putBoolean("messages_fetched", true).apply();
//                    UserUtils.ToastMessages(getContext(), message);
                                }

                                @Override
                                public void onError(String error) {
//                                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
//                                        @Override
//                                        public void onSuccess(String message) {
//                                            UserUtils.ToastMessages(this, message);
//                                        }
//
//                                        @Override
//                                        public void onError(String error) {
//                                        }
//                                    });
                                }
                            });
                            UserUtils.fetchCashBankData(this, dbHelper, new UserUtils.OnCashBankFetchedListener() {
                                @Override
                                public void onFetched(List<DBHelper.CashBank> types) {
                                }

                                @Override
                                public void onError(String error) {
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
                            });

                            UserUtils.fetchAndSaveMessages(this, new UserUtils.FetchCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean("messages_fetched", true);
                                    editor.apply();
//                                    UserUtils.ToastMessages(getActivity(), message);
//                                    lottieWave.cancelAnimation();
//                                    lottieWave.setProgress(0f); // مهم
//                                    lottieWave.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(String error) {
//                                    lottieWave.cancelAnimation();
//                                    lottieWave.setProgress(0f); // مهم
//                                    lottieWave.setVisibility(View.GONE);

                                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
//                                            UserUtils.ToastMessages(getActivity(), message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }
                                    });
                                }


                            });
                            UserUtils.fetchServiceHome(this, dbHelper, new PageHome.OnServiceHomeFetchedListener() {
                                @Override
                                public void onFetched(List<DBHelper.ServiceHome> types) {

                                }

                                @Override
                                public void onError(String error) {
                                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
//                                            UserUtils.ToastMessages(getActivity(), message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }
                                    });
                                }
                            });
                            UserUtils.fetchTypeTravelerRequests(this, dbHelper, new TravelerRequests.OnTypeRequestsFetchedListener() {
                                @Override
                                public void onFetched(List<DBHelper.TypeTravelerRequest> types) {

                                }

                                @Override
                                public void onError(String error) {
                                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
//                                            UserUtils.ToastMessages(getActivity(), message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }
                                    });
                                }
                            });
                            UserUtils.fetchRoutes(this, new UserUtils.FetchCallback() {
                                @Override
                                public void onSuccess(String message) {
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            UserUtils.loadVehicleTypesToDB(this);

                            Intent go = new Intent(otp_activity.this, HomePage.class);
                            go.putExtra("user_phone", finalPhone);
                            startActivity(go);
                            finish();

                        } else {
                            String errorReason;

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

                            } else {
                                errorReason = "Registration failed";

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
                            UserUtils.sendLog(this, "Register", errorReason, s, "otp_activity");
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
                runOnUiThread(() -> UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
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
                    tvTimer.setText("تم تجاوز الحد. حاول بعد: " + timeString);
                } else {
                    tvTimer.setText("إعادة الإرسال متاحة خلال: " + timeString);
                }
            }

            @Override
            public void onFinish() {
                btnResend.setEnabled(true);
                btnResend.setAlpha(1.0f);
                tvTimer.setVisibility(View.GONE);
                isTimerRunning = false;
            }
        }.start();
    }

    private void resendOtp(String phone) {
        if (isTimerRunning) return;

        SharedPreferences otpPrefs = getSharedPreferences("OTPLimits", MODE_PRIVATE);
        long firstAttemptTime = otpPrefs.getLong("first_attempt_time", 0);
        int count = otpPrefs.getInt("otp_count", 0);
        long currentTime = System.currentTimeMillis();
        long oneHour = TimeUnit.HOURS.toMillis(1);

        // التحقق من تصفير العداد (فقط إذا انتهت الساعة فعلاً)
        if (firstAttemptTime != 0 && (currentTime - firstAttemptTime) > oneHour) {
            count = 0;
            firstAttemptTime = 0; // سنعيد ضبطه عند أول إرسال جديد
            otpPrefs.edit().putInt("otp_count", 0).putLong("first_attempt_time", 0).apply();
        }

        // المنع إذا وصل للحد الأقصى
        if (count >= 5) {
            long timeLeftMillis = oneHour - (currentTime - firstAttemptTime);
            if (timeLeftMillis > 0) {
                startResendTimer(timeLeftMillis, true);
                UserUtils.ToastMessages(this, "انتظر انتهاء الساعة للمحاولة مجدداً");

                return;
            }
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "send_otp/",
                response -> {
                    progressDialog.dismiss();

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
                error -> { /* handle error */ }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("P_mobile", phone);
                return params;
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