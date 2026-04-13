package com.example.musafir;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePassword extends AppCompatActivity {

    EditText newPasswordEditText, confirmPasswordEditText;
    Button sendResetButton;
    String BASE_URL = UserUtils.BASE_URL;
    TextView passwordError, confirmpasswordError;
    String userPhone;
    DBHelper dbHelper = new DBHelper(this);
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        newPasswordEditText = findViewById(R.id.newpassword);
        confirmPasswordEditText = findViewById(R.id.newpassword2);
        sendResetButton = findViewById(R.id.sendResetButton);
        passwordError = findViewById(R.id.passwordError);
        confirmpasswordError = findViewById(R.id.confirmpasswordError);
        userPhone = getIntent().getStringExtra("user_phone");
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
        ImageView ivToggleNew = findViewById(R.id.ivToggleNew);
        ImageView ivToggleConfirm = findViewById(R.id.ivToggleConfirm);
        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    confirmPasswordEditText.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ivToggleNew.setOnClickListener(v2 -> {
            if (isNewVisible) {
                newPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
                ivToggleNew.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                newPasswordEditText.setTransformationMethod(new HideReturnsTransformationMethod());
                ivToggleNew.setImageResource(R.drawable.baseline_remove_red_eye_24);
            }
            isNewVisible = !isNewVisible;
            newPasswordEditText.setSelection(newPasswordEditText.getText().length());
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
        UserUtils.setEditTextState(confirmPasswordEditText, false);
        UserUtils.setEditTextState(newPasswordEditText, false);

        sendResetButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            boolean valid = true;

            if (newPassword.isEmpty()) {
                passwordError.setVisibility(View.VISIBLE);
                passwordError.setText("يرجى إدخال كلمة المرور");
//                newPasswordEditText.setError("يرجى إدخال كلمة المرور");
                UserUtils.setEditTextState(newPasswordEditText, true);
                valid = false;
            } else if (newPassword.length() != 6) {
                passwordError.setVisibility(View.VISIBLE);
                passwordError.setText("يجب أن تتكون كلمة المرور من 6 أرقام");
//                newPasswordEditText.setError("يجب أن تتكون كلمة المرور من 6 أرقام");
                UserUtils.setEditTextState(newPasswordEditText, true);
                valid = false;
            } else {
                passwordError.setVisibility(View.GONE);
                UserUtils.setEditTextState(newPasswordEditText, false);

            }

            if (confirmPassword.isEmpty()) {
                confirmpasswordError.setVisibility(View.VISIBLE);
                confirmpasswordError.setText("يرجى تأكيد كلمة المرور");
//                confirmPasswordEditText.setError("يرجى تأكيد كلمة المرور");
                UserUtils.setEditTextState(confirmPasswordEditText, true);
                valid = false;
            } else if (!newPassword.equals(confirmPassword)) {
                confirmpasswordError.setVisibility(View.VISIBLE);
                confirmpasswordError.setText("كلمتا السر غير متطابقتين");
//                newPasswordEditText.setError("كلمتا السر غير متطابقتين");
                UserUtils.setEditTextState(newPasswordEditText, true);
                valid = false;
            } else {
                confirmpasswordError.setVisibility(View.GONE);
                UserUtils.setEditTextState(newPasswordEditText, false);

            }

            if (valid) {
                sendPasswordToServer(newPassword);
            }
        });
    }

    private void sendPasswordToServer(String newPassword) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري تحديث كلمة المرور...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        SharedPreferences prefs = SharedPrefsHelper.get(this);
        SharedPreferences.Editor editor = prefs.edit();
//        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();

        String url = BASE_URL + "auth/ChangePassword/";
        DBHelper dbHelper = new DBHelper(this);

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("password", newPassword);
            String userPhone = getIntent().getStringExtra("user_phone");
            if (userPhone != null) {
                jsonParams.put("user_phone", userPhone);
            }
        } catch (JSONException e) {
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, jsonParams,
                response -> {
                    progressDialog.dismiss();
                    try {
                        if (response.has("user_id")) {
                            editor.putString("password", newPassword);
                            editor.apply();
                            String changePass = getIntent().getStringExtra("changePass");
                            if ("1".equals(changePass)) {
                                finish();
                            } else {
                                Intent intent = new Intent(ChangePassword.this, MainActivity.class);
                                intent.putExtra("user_phone", userPhone);
                                startActivity(intent);
                                finish();
                            }
                            UserUtils.getMessageFromLocal(24, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(ChangePassword.this, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                        } else {
                            UserUtils.sendLog(this, "sendPasswordToServer", response.toString(), response.toString(), "ChangePassword");
                            UserUtils.getMessageFromLocal(23, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(ChangePassword.this, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                        }
                    } catch (Exception e) {
                        UserUtils.sendLog(this, "sendPasswordToServer", e.toString(), e.toString(), "ChangePassword");
                        UserUtils.getMessageFromLocal(25, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(ChangePassword.this, message);
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });
                    }
                },
                error -> {
                    progressDialog.dismiss();

                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null) {
                        int statusCode = networkResponse.statusCode;
                        String responseBody = "";
                        try {
                            responseBody = new String(networkResponse.data, "utf-8");
                        } catch (Exception ignored) {
                        }
                        UserUtils.sendLog(this, "sendPasswordToServer",
                                String.valueOf(statusCode), responseBody, "ChangePassword");
                    }

                    UserUtils.sendLog(this, "sendPasswordToServer",
                            error.toString(), error.toString(), "ChangePassword");
                    UserUtils.getMessageFromLocal(25, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(ChangePassword.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer 1");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

}
