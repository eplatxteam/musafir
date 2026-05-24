package com.example.musafir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.scottyab.rootbeer.RootBeer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity {
    String BASE_URL = UserUtils.BASE_URL;
    TextView fullNameError, phoneError, passwordError, confirmpasswordError;
    DBHelper dbHelper = new DBHelper(this);
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    private void scheduleLocationUpdate() {
        PeriodicWorkRequest locationWorkRequest =
                new PeriodicWorkRequest.Builder(LocationWorker.class, 2, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "updateCity",
                ExistingPeriodicWorkPolicy.REPLACE,
                locationWorkRequest
        );
    }

    EditText usernameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        LinearLayout loginText = findViewById(R.id.loginText);
        fullNameError = findViewById(R.id.fullNameError);
        phoneError = findViewById(R.id.phoneError);
        passwordError = findViewById(R.id.passwordError);
        confirmpasswordError = findViewById(R.id.confirmpasswordError);

        loginText.setOnClickListener(view -> finish());
//        TextView code = findViewById(R.id.code);
//        String inviteCode = getIntent().getStringExtra("invite");
//        code.setText("كود الدعوة الخاص بك: " + inviteCode);

        // بطاقات الاختيار
        MaterialCardView passengerCard = findViewById(R.id.radioPassenger);
        MaterialCardView driverCard = findViewById(R.id.radioDriver);
        ImageView iconPassenger = findViewById(R.id.iconPassenger);
        ImageView iconDriver = findViewById(R.id.iconDriver);
        TextView Driver = findViewById(R.id.driver);
        TextView Passenger = findViewById(R.id.Passenger);
        TextView textname = findViewById(R.id.textname);
        TextView textpass = findViewById(R.id.textpass);
        textpass.setText(UserUtils.getMessageFromLocalNew(407, dbHelper));
        String nameErrorMessage = UserUtils.getMessageFromLocalNew(406, dbHelper);

        if (nameErrorMessage.contains("حاول مرة اخرى") ) {
            nameErrorMessage = "يرجى كتابة اسمك الرباعي كما في الهوية (الجواز ).";
        }
        textname.setText(nameErrorMessage);

        // تعيين الافتراضي (passenger محدد)
        selectPassenger(passengerCard, driverCard, iconPassenger, iconDriver, Passenger, Driver);
        usernameEditText = findViewById(R.id.fullNameEditText);

        passengerCard.setOnClickListener(v -> {
            selectPassenger(passengerCard, driverCard, iconPassenger, iconDriver, Passenger, Driver);
            usernameEditText.setHint("اسم المسافر");
        });

        driverCard.setOnClickListener(v -> {
            selectDriver(passengerCard, driverCard, iconPassenger, iconDriver, Passenger, Driver);
            usernameEditText.setHint("اسم السائق");
        });
        scheduleLocationUpdate();
        EditText phoneEditText = findViewById(R.id.phoneEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        EditText confirmPasswordEditText = findViewById(R.id.confirmpasswordEditText);

        ImageView ivToggleNew = findViewById(R.id.ivToggleNew);
        ImageView ivToggleConfirm = findViewById(R.id.ivToggleConfirm);

        ivToggleNew.setOnClickListener(v2 -> {
            if (isNewVisible) {
                passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
                ivToggleNew.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                passwordEditText.setTransformationMethod(new HideReturnsTransformationMethod());
                ivToggleNew.setImageResource(R.drawable.baseline_remove_red_eye_24);
            }
            isNewVisible = !isNewVisible;
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        ivToggleConfirm.setOnClickListener(v3 -> {
            if (isConfirmVisible) {
                confirmPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
                ivToggleConfirm.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                confirmPasswordEditText.setTransformationMethod(new HideReturnsTransformationMethod());
                ivToggleConfirm.setImageResource(R.drawable.baseline_remove_red_eye_24);
            }
            isConfirmVisible = !isConfirmVisible;
            confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
        });
        usernameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String trimmed = usernameEditText.getText().toString().trim();
                usernameEditText.setText(trimmed);
            }
        });
        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String input = s.toString();
                if (input.startsWith("05")) {
                    phoneEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                } else {
                    phoneEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
                }

                if (input.startsWith("05") && input.length() == 10) {
                    passwordEditText.requestFocus();
                } else if (!input.startsWith("05") && input.length() == 9) {
                    passwordEditText.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    confirmPasswordEditText.requestFocus(); // ينتقل لحقل كلمة المرور
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        UserUtils.setEditTextState(usernameEditText, false);
        UserUtils.setEditTextState(phoneEditText, false);
        UserUtils.setEditTextState(passwordEditText, false);
        UserUtils.setEditTextState(confirmPasswordEditText, false);

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(view -> {
            clearErrors();

            String username = usernameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            boolean valid = true;
            if (username.isEmpty()) {
                fullNameError.setVisibility(View.VISIBLE);
                fullNameError.setText("يرجى إدخال الاسم الكامل");
                usernameEditText.setError("يرجى إدخال الاسم الكامل");
                UserUtils.setEditTextState(usernameEditText, true);
                valid = false;
            } else if (!username.trim().contains(" ")) {
                fullNameError.setVisibility(View.VISIBLE);
                fullNameError.setText("يرجى إدخال الاسم الأول والأخير على الأقل");
                usernameEditText.setError("يجب إدخال اسمين أو أكثر");
                UserUtils.setEditTextState(usernameEditText, true);
                valid = false;
            } else {
                fullNameError.setVisibility(View.GONE);
                UserUtils.setEditTextState(usernameEditText, false);
            }
//            if (username.isEmpty()) {
//                fullNameError.setVisibility(View.VISIBLE);
//                fullNameError.setText("يرجى إدخال الاسم الكامل");
//                usernameEditText.setError("يرجى إدخال الاسم الكامل");
//                UserUtils.setEditTextState(usernameEditText, true);
//                valid = false;
//            } else {
//                fullNameError.setVisibility(View.GONE);
//                UserUtils.setEditTextState(usernameEditText, false);
//            }

            if (phone.isEmpty()) {
                phoneError.setVisibility(View.VISIBLE);
                phoneError.setText("يرجى إدخال رقم الهاتف");
                phoneEditText.setError("يرجى إدخال رقم الهاتف");
                UserUtils.setEditTextState(phoneEditText, true);
                valid = false;
            } else if (!(phone.startsWith("7") || phone.startsWith("05"))) {
                phoneError.setVisibility(View.VISIBLE);
                phoneError.setText("يجب أن يبدأ الرقم بـ 7 أو 05 ");
                phoneEditText.setError("رقم غير صحيح");
                UserUtils.setEditTextState(phoneEditText, true);
                valid = false;
            } else if (phone.startsWith("05") && phone.length() != 10) {
                phoneError.setVisibility(View.VISIBLE);
                phoneError.setText("رقم غير صحيح. يجب أن يتكون من 10 أرقام ويبدأ بـ 05");
                phoneEditText.setError("يرجى إدخال رقم صحيح");
                UserUtils.setEditTextState(phoneEditText, true);
                valid = false;
            } else if (phone.startsWith("7") && !phone.matches("7[013789]\\d{7}")) {
                phoneError.setVisibility(View.VISIBLE);
                phoneError.setText("رقم غير صحيح. يجب أن يبدأ بـ 7 ويتكون من 9 أرقام");
                phoneEditText.setError("رقم غير صحيح");
                UserUtils.setEditTextState(phoneEditText, true);
                valid = false;
            } else {
                phoneError.setVisibility(View.GONE);
                UserUtils.setEditTextState(phoneEditText, false);
            }


            if (password.isEmpty()) {
                passwordError.setVisibility(View.VISIBLE);
                passwordError.setText("يرجى إدخال كلمة المرور");
                UserUtils.setEditTextState(passwordEditText, true);
                valid = false;
            } else if (password.length() != 6) {
                passwordError.setVisibility(View.VISIBLE);
                passwordError.setText("يجب أن تتكون كلمة المرور من 6 أرقام");
                UserUtils.setEditTextState(passwordEditText, true);
                valid = false;
            } else {
                passwordError.setVisibility(View.GONE);
                UserUtils.setEditTextState(passwordEditText, false);

            }

            if (confirmPassword.isEmpty()) {
                confirmpasswordError.setVisibility(View.VISIBLE);
                confirmpasswordError.setText("يرجى تأكيد كلمة المرور");
                UserUtils.setEditTextState(confirmPasswordEditText, true);

                valid = false;
            } else if (!password.equals(confirmPassword)) {
                confirmpasswordError.setVisibility(View.VISIBLE);
                confirmpasswordError.setText("كلمتا السر غير متطابقتين");
                UserUtils.setEditTextState(confirmPasswordEditText, true);
                valid = false;
            } else {
                confirmpasswordError.setVisibility(View.GONE);
                UserUtils.setEditTextState(confirmPasswordEditText, false);

            }
            String finalPhone;
            int p_otp_typ;
            if (phone.startsWith("05")) {
                finalPhone = "966" + phone.substring(1);
                p_otp_typ = 2;
            } else if (phone.startsWith("7")) {
                finalPhone = "967" + phone;
                p_otp_typ = 3;
            } else {
                finalPhone = phone;
                p_otp_typ = 2;
            }
            if (valid) {
                checkPhoneExists(finalPhone, password, p_otp_typ);

            }
        });
    }

    private void clearErrors() {
        fullNameError.setText("");
        phoneError.setText("");
        passwordError.setText("");
        confirmpasswordError.setText("");
    }

    private void selectPassenger(MaterialCardView passengerCard, MaterialCardView driverCard,
                                 ImageView iconPassenger, ImageView iconDriver,
                                 TextView textPassenger, TextView textDriver) {

        passengerCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.secondary2));
        iconPassenger.setColorFilter(ContextCompat.getColor(this, R.color.primary2));
        textPassenger.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        driverCard.setCardBackgroundColor(Color.parseColor("#F6F6F6"));
        iconDriver.setColorFilter(Color.parseColor("#666666"));
        textDriver.setTextColor(Color.parseColor("#666666"));

        driverCard.setTag("unselected");
        passengerCard.setTag("selected");
    }

    private void selectDriver(MaterialCardView passengerCard, MaterialCardView driverCard,
                              ImageView iconPassenger, ImageView iconDriver,
                              TextView textPassenger, TextView textDriver) {

        driverCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.secondary2));
        iconDriver.setColorFilter(ContextCompat.getColor(this, R.color.primary2));
        textDriver.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        passengerCard.setCardBackgroundColor(Color.parseColor("#F6F6F6"));
        iconPassenger.setColorFilter(Color.parseColor("#666666"));
        textPassenger.setTextColor(Color.parseColor("#666666"));

        passengerCard.setTag("unselected");
        driverCard.setTag("selected");

    }

    private void checkPhoneExists(String phone, String password, int p_otp_typ) {
        if (!UserUtils.isNetworkAvailable(this)) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(MainActivity2.this, message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري التحقق من الرقم...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "check_phone/"); // أنشئ endpoint في السيرفر للتحقق
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                String finalPhone;


                JSONObject jsonParam = new JSONObject();
                jsonParam.put("phone", phone);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) ? conn.getErrorStream() : conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject responseJson = new JSONObject(result.toString());
                        boolean exists = responseJson.optBoolean("exists", false);
                        if (exists) {
                            UserUtils.getMessageFromLocal(44, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(MainActivity2.this, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                        } else {
                            RootBeer rootBeer = new RootBeer(this);
                            if (rootBeer.isRooted()) {
                                UserUtils.getMessageFromLocal(221, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(MainActivity2.this, message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                            } else {

                                sendOtp(phone, password, p_otp_typ);
                            }
                        }

                    } catch (JSONException e) {
                        UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(MainActivity2.this, message);
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(MainActivity2.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                });
            }
        }).start();
    }


    private void sendOtp(String phone, String password, int p_otp_typ) {
        if (!UserUtils.isNetworkAvailable(this)) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(MainActivity2.this, message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
        SharedPreferences otpPrefs = getSharedPreferences("OTPLimits", MODE_PRIVATE);
        long firstAttemptTime = otpPrefs.getLong("first_attempt_time", 0);
        int count = otpPrefs.getInt("otp_count", 0);
        long currentTime = System.currentTimeMillis();
        long oneHour = TimeUnit.HOURS.toMillis(1);

        if (count >= 4 && (currentTime - firstAttemptTime) < oneHour) {
            SharedPreferences mainPrefs = SharedPrefsHelper.get(this);
            String lastToken = mainPrefs.getString("otp_token", "");

            if (!lastToken.isEmpty()) {
                navigateToOtp(phone, password, lastToken);
                return;
            } else {
                long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(oneHour - (currentTime - firstAttemptTime));

                UserUtils.ToastMessages(MainActivity2.this, "تجاوزت الحد الأقصى، حاول بعد " + minutesLeft + " دقيقة");
                return;
            }
        }

        if (firstAttemptTime != 0 && (currentTime - firstAttemptTime) > oneHour) {
            count = 0;
            firstAttemptTime = 0;
            otpPrefs.edit().putInt("otp_count", 0).putLong("first_attempt_time", 0).apply();
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري إرسال رمز التحقق...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        int finalCount = count;

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "send_otp/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                String postData = "P_mobile=" + phone + "&p_otp_typ=" + p_otp_typ;

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
//                    String otpToken = result.toString().replace("\"", "").trim();

                    String rawResult = result.toString();
                    if (responseCode == 200 && !rawResult.isEmpty() && !rawResult.contains("error")) {
                        String otpToken = rawResult.replace("\"", "").trim();
                        SharedPreferences.Editor limitEditor = otpPrefs.edit();
                        int newCount = finalCount + 1;
                        if (newCount == 1) {
                            limitEditor.putLong("first_attempt_time", System.currentTimeMillis());
                        }
                        limitEditor.putInt("otp_count", newCount);
                        limitEditor.apply();
                        // ---------------------------------------

                        SharedPreferences prefs = SharedPrefsHelper.get(this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("otp_token", otpToken);
                        editor.apply(); // أو commit()
                        Intent intent = new Intent(MainActivity2.this, otp_activity.class);

                        // تحديد نوع المستخدم
                        String user_type = "passenger";
                        MaterialCardView driverCard = findViewById(R.id.radioDriver);
                        if ("selected".equals(driverCard.getTag())) user_type = "driver";

                        String username = usernameEditText.getText().toString().trim();
                        String inviteCode = getIntent().getStringExtra("invite");

                        intent.putExtra("user_name", username);
                        intent.putExtra("inviteCode", inviteCode);
                        intent.putExtra("user_type", user_type);
                        intent.putExtra("phone", phone);
                        intent.putExtra("otp_token", otpToken);
                        intent.putExtra("page", "0");
                        intent.putExtra("password", password);
                        startActivity(intent);
//                        finish();
                    } else {
                        UserUtils.sendLog(this, "sendOtp", String.valueOf(responseCode), result.toString(), "Main2 Register");
                        try {
                            JSONObject jsonResponse = new JSONObject(rawResult);

                            String serverMessage = jsonResponse.optString("msg_txt", "");

                            if (!serverMessage.isEmpty()) {
                                UserUtils.ToastMessages(MainActivity2.this, serverMessage);
                            } else {
                                UserUtils.getMessageFromLocal(46, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(MainActivity2.this, message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                            }

                        } catch (Exception e) {
                            UserUtils.getMessageFromLocal(46, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(MainActivity2.this, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                        }


                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    UserUtils.sendLog(this, "sendOtp", e.toString(), e.toString(), "Main2 Register");
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(MainActivity2.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                });
            }
        }).start();
    }

    private void navigateToOtp(String phone, String password, String otpToken) {
        Intent intent = new Intent(MainActivity2.this, otp_activity.class);

        String user_type = "passenger";
        MaterialCardView driverCard = findViewById(R.id.radioDriver);
        if ("selected".equals(driverCard.getTag())) {
            user_type = "driver";
        }

        String username = usernameEditText.getText().toString().trim();
        String inviteCode = getIntent().getStringExtra("invite");

        intent.putExtra("user_name", username);
        intent.putExtra("inviteCode", inviteCode);
        intent.putExtra("user_type", user_type);
        intent.putExtra("phone", phone);
        intent.putExtra("otp_token", otpToken);
        intent.putExtra("page", "0");
        intent.putExtra("password", password);

        startActivity(intent);
        finish();
    }
}