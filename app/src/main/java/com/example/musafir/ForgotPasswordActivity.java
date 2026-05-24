package com.example.musafir;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForgotPasswordActivity extends AppCompatActivity {
    String BASE_URL = UserUtils.BASE_URL;
    TextView phoneError;
    DBHelper dbHelper = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        phoneError = findViewById(R.id.phoneError);
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
        EditText forgotPhoneEditText = findViewById(R.id.forgotPhoneEditText);
        Button resetPassword = findViewById(R.id.sendResetButton);
        forgotPhoneEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                resetPassword.performClick();
                return true;
            }
            return false;
        });
        UserUtils.setEditTextState(forgotPhoneEditText, false);
        forgotPhoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String input = s.toString();
                if (input.startsWith("05")) {
                    forgotPhoneEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                } else {
                    forgotPhoneEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
                }

                if (input.startsWith("05") && input.length() == 10) {
                    forgotPhoneEditText.requestFocus();
                } else if (!input.startsWith("05") && input.length() == 9) {
                    forgotPhoneEditText.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        String phone2 = getIntent().getStringExtra("phone");
        if (phone2 != null) {
            forgotPhoneEditText.setText(phone2);
        }
        resetPassword.setOnClickListener(view -> {
            String phone = forgotPhoneEditText.getText().toString().trim();
            boolean valid = true;
            if (phone.isEmpty()) {
                phoneError.setVisibility(View.VISIBLE);
                phoneError.setText("يرجى إدخال رقم الهاتف");
                forgotPhoneEditText.setError("يرجى إدخال رقم الهاتف");
                UserUtils.setEditTextState(forgotPhoneEditText, true);
                valid = false;
            } else if (!(phone.startsWith("7") || phone.startsWith("05"))) {
                phoneError.setVisibility(View.VISIBLE);
                phoneError.setText("يجب أن يبدأ الرقم بـ 7 أو 05 ");
                forgotPhoneEditText.setError("رقم غير صحيح");
                UserUtils.setEditTextState(forgotPhoneEditText, true);
                valid = false;
            } else if (phone.startsWith("05") && phone.length() != 10) {
                phoneError.setVisibility(View.VISIBLE);
                phoneError.setText("رقم غير صحيح. يجب أن يتكون من 10 أرقام ويبدأ بـ 05");
                forgotPhoneEditText.setError("يرجى إدخال رقم صحيح");
                UserUtils.setEditTextState(forgotPhoneEditText, true);
                valid = false;
            } else if (phone.startsWith("7") && !phone.matches("7[013789]\\d{7}")) {
                phoneError.setVisibility(View.VISIBLE);
                phoneError.setText("رقم غير صحيح. يجب أن يبدأ بـ 7 ويتكون من 9 أرقام");
                forgotPhoneEditText.setError("رقم غير صحيح");
                UserUtils.setEditTextState(forgotPhoneEditText, true);
                valid = false;
            } else {
                phoneError.setVisibility(View.GONE);
                UserUtils.setEditTextState(forgotPhoneEditText, false);
            }
            if (valid) {
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
                check_token(finalPhone, new TokenCallback() {
                    @Override
                    public void onTokenReceived(String token) {
                        sendOtp(finalPhone, token, p_otp_typ);
                    }

                    @Override
                    public void onError(String error) {
                        UserUtils.ToastMessages(ForgotPasswordActivity.this, error);
                    }
                });
            }
        });
    }

    public interface TokenCallback {
        void onTokenReceived(String token);

        void onError(String error);
    }

    private void sendOtp(String phone, String token, int p_otp_typ) {
        if (!UserUtils.isNetworkAvailable(this)) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(ForgotPasswordActivity.this, message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري إرسال رمز التحقق...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "send_otp/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                conn.setDoInput(true);
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
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                runOnUiThread(() -> {

                    progressDialog.dismiss();
                    String otpToken = result.toString().replace("\"", "").trim();
                    String s = "{ phone: " + phone +
                            " , otp_token: " + otpToken +
                            " , Tokenuser: " + token +
                            " }";
                    String rawResult = result.toString();
                    String serverMessage = "";
                    try {
                        if (rawResult.startsWith("{")) {
                            JSONObject jsonResponse = new JSONObject(rawResult);
                            serverMessage = jsonResponse.optString("msg_txt", "");
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    if (responseCode == 200 && !otpToken.isEmpty() && !otpToken.contains("error")) {
                        SharedPreferences prefs = SharedPrefsHelper.get(this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("otp_token", otpToken);

                        editor.apply();
                        Intent intent = new Intent(ForgotPasswordActivity.this, otp_activity.class);
                        intent.putExtra("phone", phone);
                        intent.putExtra("otp_token", otpToken);
                        intent.putExtra("Tokenuser", token);
                        intent.putExtra("page", "1");
                        startActivity(intent);
                        finish();

                    } else {

                        if (!serverMessage.isEmpty()) {
                            UserUtils.ToastMessages(ForgotPasswordActivity.this, serverMessage);
                        } else {
                            UserUtils.sendLog(this, "sendOtp", otpToken, s, "Forgot Password");
                            UserUtils.getMessageFromLocal(30, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(ForgotPasswordActivity.this, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                        }
                    }
                });
            } catch (Exception e) {
                UserUtils.sendLog(this, "sendOtp", e.toString(), e.toString(), "Forgot Password");
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(ForgotPasswordActivity.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                });
            }
        }).start();
    }


    private void check_token(String phone, TokenCallback callback) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري بدء التحقق...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                runOnUiThread(() -> progressDialog.setMessage("جاري التحقق من رقم الهاتف..."));
                URL urlCheckToken = new URL(BASE_URL + "auth/check_token/");
                HttpURLConnection conn1 = (HttpURLConnection) urlCheckToken.openConnection();
                conn1.setRequestMethod("POST");
                conn1.setRequestProperty("Content-Type", "application/json");
                conn1.setDoOutput(true);
                conn1.setDoInput(true);
                String finalPhone;
                if (phone.startsWith("05")) {
                    finalPhone = "966" + phone.substring(1);
                } else if (phone.startsWith("7")) {
                    finalPhone = "967" + phone;
                } else {
                    finalPhone = phone;
                }
                String jsonInput = "{\"username\":\"" + finalPhone + "\"}";
                OutputStream os1 = conn1.getOutputStream();
                os1.write(jsonInput.getBytes("UTF-8"));
                os1.flush();
                os1.close();

                int responseCode1 = conn1.getResponseCode();
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(
                        (responseCode1 == 200) ? conn1.getInputStream() : conn1.getErrorStream()));
                StringBuilder result1 = new StringBuilder();
                String line1;
                while ((line1 = reader1.readLine()) != null) {
                    result1.append(line1);
                }
                reader1.close();

                if (responseCode1 == 200) {
                    JSONObject jsonResponse1 = new JSONObject(result1.toString());
                    String token = jsonResponse1.getString("token");

                    runOnUiThread(() -> {
                        progressDialog.setMessage("تم التحقق من الرقم، جاري إرسال رمز التحقق...");
                        callback.onTokenReceived(token);
                        progressDialog.dismiss();
                    });
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        UserUtils.getMessageFromLocal(32, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                callback.onError(message);

                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                    });
                }

            } catch (Exception e) {
                UserUtils.sendLog(this, "check_token", e.toString(), e.toString(), "Forgot Password");
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            callback.onError(message);
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