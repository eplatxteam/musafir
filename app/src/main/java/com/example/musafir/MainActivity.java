package com.example.musafir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.android.material.button.MaterialButton;
import com.scottyab.rootbeer.RootBeer;

public class MainActivity extends AppCompatActivity {
    EditText phoneEditText, passwordEditText;
    String BASE_URL = UserUtils.BASE_URL;
    TextView passwordError, phoneError;
    Button loginButton;
    DBHelper dbHelper = new DBHelper(this);

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

    private boolean isNewVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().hide();

        LinearLayout registerText = findViewById(R.id.registerText);
        TextView skipText = findViewById(R.id.skipText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        UserUtils.app_Page(this, 10);
        passwordError = findViewById(R.id.passwordError);
        phoneError = findViewById(R.id.phoneError);
        MaterialButton whatsappIcon = findViewById(R.id.btn_whatsapp_driver);
        MaterialButton btn_call = findViewById(R.id.btn_call);
        SharedPreferences prefs = SharedPrefsHelper.get(this);

        btn_call.setOnClickListener(v -> {
            String countryCode = prefs.getString("country_code", "967785050270");

            int messageId = "YE".equals(countryCode) ? 349 : 362;
            String phone = UserUtils.getMessageFromLocalNew(messageId, dbHelper);
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });
        whatsappIcon.setOnClickListener(v -> {
            String countryCode = prefs.getString("country_code", "967785050270");

            int messageId = "YE".equals(countryCode) ? 349 : 362;
            String phone = UserUtils.getMessageFromLocalNew(messageId, dbHelper);
            String url = "https://wa.me/" + phone;

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage("com.whatsapp");

            try {
                v.getContext().startActivity(intent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        ImageView ivToggleNew = findViewById(R.id.ivToggleNew);

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

        scheduleLocationUpdate();
        String phoneNumber = getIntent().getStringExtra("user_phone");
        if (phoneNumber != null) {
            phoneEditText.setText(phoneNumber); // وضع الرقم في EditText تلقائيًا
        }

        registerText.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
        });

        skipText.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
        });

        String phone_number = getIntent().getStringExtra("user_phone");
        if (phone_number != null) {
            phoneEditText.setText(phone_number);
        }

        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(view -> {
            UserUtils.app_Page(this, 16);
            String phone = phoneEditText.getText().toString().trim();
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            intent.putExtra("phone", phone); // إرسال الرقم
            startActivity(intent);
        });


        // زر الدخول
        EditText phoneEditText = findViewById(R.id.phoneEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        UserUtils.setEditTextState(phoneEditText, false);
        UserUtils.setEditTextState(passwordEditText, false);
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

        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginButton.performClick();
                return true;
            }
            return false;
        });

        loginButton.setOnClickListener(view -> {
            clearErrors();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            boolean valid = true;
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
            RootBeer rootBeer = new RootBeer(this);

            if (rootBeer.isRooted()) {
                UserUtils.getMessageFromLocal(221, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(MainActivity.this, message);
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
            } else {
                if (valid) {
                    login();
                }
            }
        });
    }

    private void clearErrors() {
        phoneError.setText("");
        passwordError.setText("");
    }

    private void login() {
        if (!UserUtils.isNetworkAvailable(this)) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(MainActivity.this, message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري تسجيل الدخول...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "auth/login/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                String deviceSerials = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                SharedPreferences prefs = SharedPrefsHelper.get(this);
                String defaultCity = prefs.getString("default_city", "حدد المدينة");
                String country = prefs.getString("country_code", "");
                int defaultCityId = prefs.getInt("default_city_id", -1);
                String finalPhone;
                if (phone.startsWith("05")) {
                    finalPhone = "966" + phone.substring(1);
                } else if (phone.startsWith("7")) {
                    finalPhone = "967" + phone;
                } else {
                    finalPhone = phone;
                }
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("password", password);
                jsonParam.put("username", finalPhone);
                jsonParam.put("device_serial", deviceSerials);
                jsonParam.put("default_city", defaultCity);
                jsonParam.put("country", country);
                jsonParam.put("city_id", defaultCityId);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                String s = "{ password: " + password +
                        " , username: " + finalPhone +
                        " , device_serial: " + deviceSerials +
                        " , default_city: " + defaultCity +
                        " , city_id: " + defaultCityId +
                        " }";
                int responseCode = conn.getResponseCode();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream()));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());

                        if (responseCode == 200 && jsonObject.has("token")) {
                            String token = jsonObject.getString("token");
                            String is_active = jsonObject.getString("is_active");
                            int userId = jsonObject.getInt("user_id");
                            String is_verified = jsonObject.getString("is_verified");
                            String user_type = jsonObject.getString("user_type");
                            String full_name = jsonObject.getString("full_name");
                            String ip = jsonObject.getString("ip");
                            String notify_general = jsonObject.getString("notify_general");
                            String notify_primary = jsonObject.getString("notify_primary");
                            String cur_code = jsonObject.getString("cur_code");

                            if (is_active.equals("false")) {
                                UserUtils.getMessageFromLocal(22, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(MainActivity.this, message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }

                                });
                                return;
                            }
                            String deviceSerial = jsonObject.getString("device_serial");

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("auth_token", token);
                            editor.putString("user_phone", finalPhone);
                            editor.putInt("user_id", userId);
                            editor.putString("ip", ip);
                            editor.putString("is_verified", is_verified);
                            editor.putString("is_active", is_active);
                            editor.putString("user_type", user_type);
                            editor.putString("notify_general", notify_general);
                            editor.putString("notify_primary", notify_primary);
                            editor.putString("device_serial", deviceSerial);
                            editor.putString("password", password);
                            editor.putString("default_city", defaultCity);
                            editor.putString("cur_code", cur_code);
                            String firstName = full_name.trim().split("\\s+")[0];
                            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase(); // Capitalize أول حرف فقط

                            editor.putString("full_name", full_name);
                            editor.apply();

                            String finalFirstName = firstName;
                            UserUtils.getMessageFromLocal(43, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(MainActivity.this, message + finalFirstName);

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

                                }
                            });
                            UserUtils.fetchBalance(this);
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
                            UserUtils.fetchCashBankData(this, dbHelper, new UserUtils.OnCashBankFetchedListener() {
                                @Override
                                public void onFetched(List<DBHelper.CashBank> types) {
                                }

                                @Override
                                public void onError(String error) {
                                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            UserUtils.ToastMessages(MainActivity.this, message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }

                                    });
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
                            UserUtils.fetchAndSaveCountry(this, new UserUtils.FetchCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    prefs.edit().putBoolean("messages_fetched", true).apply();
//                    UserUtils.ToastMessages(getContext(), message);
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


                            Intent intent = new Intent(MainActivity.this, HomePage.class);
                            intent.putExtra("is_just_logged_in", true);
                            startActivity(intent);
                            finish();
                        } else {
                            UserUtils.sendLog(this, "login", String.valueOf(jsonObject), s, "Main Login");

                            try {
                                JSONObject jsonResponse = new JSONObject(result.toString());

                                String serverMessage = jsonResponse.optString("msg_txt", "");

                                if (!serverMessage.isEmpty()) {
                                    UserUtils.ToastMessages(MainActivity.this, serverMessage);
                                } else {
                                    UserUtils.getMessageFromLocal(42, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            UserUtils.ToastMessages(MainActivity.this, message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                UserUtils.getMessageFromLocal(42, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                            }
//                            UserUtils.getMessageFromLocal(42, dbHelper, new UserUtils.MessageCallback() {
//                                @Override
//                                public void onSuccess(String message) {
//                                    UserUtils.ToastMessages(MainActivity.this, message);
//                                }
//
//                                @Override
//                                public void onError(String error) {
//                                }
//

                        }
                    } catch (JSONException e) {
                        UserUtils.sendLog(this, "login", s, s, "Main Login");
                        UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(MainActivity.this, message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                    }
                });

            } catch (Exception e) {
                UserUtils.sendLog(this, "login", e.toString(), e.toString(), "Main Login");
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(MainActivity.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                });
            }
        }).start();
    }

}
