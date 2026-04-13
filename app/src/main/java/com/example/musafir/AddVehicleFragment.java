package com.example.musafir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

public class AddVehicleFragment extends Fragment {

    EditText yearEditText, colorEditText, plateEditText,
            seatsEditText, availableSeatsEditText,
            makeEditText, vh_price, vehicle_nameEditText;
    Spinner fuelTypeSpinner, vehicleTypeSpinner;
    Button submitButton;
    String BASE_URL = UserUtils.BASE_URL;

    String[] fuelTypes = {"بنزين", "ديزل", "هجين", "كهربائي"};

    private static final int PICK_IMAGE_REQUEST = 1, PICK_IMAGE_REQUEST1 = 2, PICK_IMAGE_REQUEST2 = 3, PICK_IMAGE_REQUEST3 = 4, PICK_IMAGE_REQUEST4 = 5;
    private static final int PICK_REG_DOC_REQUEST = 6;
    private Uri selectedImageUri, smallImageUri, selectedImageUri1, selectedImageUri2, selectedImageUri3, selectedImageUri4;
    private Uri registrationDocUri;
    ImageView vehicleImageView, registrationDocImageView, vehicleImageView1, vehicleImageView2, vehicleImageView3, vehicleImageView4;
    List<String> vehicleTypeNames = new ArrayList<>();
    Map<String, Integer> vehicleTypeMap = new HashMap<>();
    private Integer vehicleTypeFromArgs = null;
    private Integer vehicleId = null;
    private boolean isEditMode = false;

    TextView placeholderText, placeholderText2, availableSeatsError, totalSeatsError,
            registrationDocImageViewError, vehicleImageViewError, licensePlateError, makeError,
            yearError, colorError, vh_priceError, vehicle_nameError;
    LinearLayout frame_doc, frame_img, frame_img1, frame_img2, frame_img3, frame_img4;
    ScrollView scrollViewcon;

    public AddVehicleFragment() {
        // Required empty public constructor
    }

    View firstErrorView = null;
    private boolean isDataChanged = false;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem placeholderItem = menu.findItem(R.id.action_placeholder);
        if (placeholderItem != null) {
            placeholderItem.setVisible(true);
            View actionView = placeholderItem.getActionView();
            if (actionView != null) {
                actionView.setPressed(true);
                actionView.postDelayed(() -> actionView.setPressed(false), 100);
            }
            actionView.setOnClickListener(v -> {
                requireActivity().onBackPressed();
            });
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_vehicle, container, false);
        scrollViewcon = view.findViewById(R.id.scrollViewcon);
        setHasOptionsMenu(true);
        // ======== Error TextViews ========
        availableSeatsError = view.findViewById(R.id.availableSeatsError);
        vehicle_nameError = view.findViewById(R.id.vehicle_nameError);
        vh_priceError = view.findViewById(R.id.vh_priceError);
        vh_price = view.findViewById(R.id.vh_price);
        totalSeatsError = view.findViewById(R.id.totalSeatsError);
        registrationDocImageViewError = view.findViewById(R.id.registrationDocImageViewError);
        vehicleImageViewError = view.findViewById(R.id.vehicleImageViewError);
        licensePlateError = view.findViewById(R.id.licensePlateError);
        makeError = view.findViewById(R.id.makeError);
        yearError = view.findViewById(R.id.yearError);
        colorError = view.findViewById(R.id.colorError);

        // ======== Placeholders ========
        placeholderText = view.findViewById(R.id.placeholderText);
        placeholderText2 = view.findViewById(R.id.placeholderText2);
        frame_doc = view.findViewById(R.id.frame_doc);
        frame_img = view.findViewById(R.id.frame_img);
        frame_img1 = view.findViewById(R.id.frame_img1);
        frame_img2 = view.findViewById(R.id.frame_img2);
        frame_img3 = view.findViewById(R.id.frame_img3);
        frame_img4 = view.findViewById(R.id.frame_img4);

        // ======== EditTexts ========
        makeEditText = view.findViewById(R.id.makeEditText);
        vehicle_nameEditText = view.findViewById(R.id.vehicle_nameEditText);
        yearEditText = view.findViewById(R.id.year);
        colorEditText = view.findViewById(R.id.color);
        plateEditText = view.findViewById(R.id.licensePlate);
        seatsEditText = view.findViewById(R.id.totalSeats);
        availableSeatsEditText = view.findViewById(R.id.availableSeats);
//        insuranceExpiryEditText = view.findViewById(R.id.insuranceExpiryDate);

        // ======== Spinners ========
        fuelTypeSpinner = view.findViewById(R.id.fuelTypeSpinner);
        vehicleTypeSpinner = view.findViewById(R.id.vehicleTypeSpinner);

        // ======== Buttons & ImageViews ========
        submitButton = view.findViewById(R.id.submitBtn);
        vehicleImageView = view.findViewById(R.id.vehicleImageView);
        vehicleImageView1 = view.findViewById(R.id.vehicleImageView1);
        vehicleImageView2 = view.findViewById(R.id.vehicleImageView2);
        vehicleImageView3 = view.findViewById(R.id.vehicleImageView3);
        vehicleImageView4 = view.findViewById(R.id.vehicleImageView4);
        registrationDocImageView = view.findViewById(R.id.registrationDocImageView);

        // ======== إعداد القوائم المنسدلة ========
        ArrayAdapter<String> fuelAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fuelTypes);
        fuelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuelTypeSpinner.setAdapter(fuelAdapter);
        loadVehicleTypes();

        // ======== اختيار التاريخ ========
//        insuranceExpiryEditText.setOnClickListener(v -> showDatePicker());
        yearEditText.setOnClickListener(v -> showYearDialog(getContext()));
        isDataChanged = false;

        // أي تغيير في الحقول يفعّل العلم
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isDataChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        makeEditText.addTextChangedListener(watcher);
        plateEditText.addTextChangedListener(watcher);
        yearEditText.addTextChangedListener(watcher);
        vehicle_nameEditText.addTextChangedListener(watcher);
        colorEditText.addTextChangedListener(watcher);
        vh_price.addTextChangedListener(watcher);
        seatsEditText.addTextChangedListener(watcher);
        availableSeatsEditText.addTextChangedListener(watcher);

        UserUtils.setEditTextState(makeEditText, false);
        UserUtils.setEditTextState(plateEditText, false);
        UserUtils.setEditTextState(yearEditText, false);
        UserUtils.setEditTextState(vehicle_nameEditText, false);
        UserUtils.setEditTextState(colorEditText, false);
        UserUtils.setEditTextState(vh_price, false);
//        vh_price.setBackgroundResource(R.drawable.edittext_background);
        UserUtils.setEditTextState(seatsEditText, false);
        UserUtils.setEditTextState(availableSeatsEditText, false);
        vehicleImageView.setOnClickListener(v -> {
            checkImagePermission(PICK_IMAGE_REQUEST);
//            openImagePicker();
            isDataChanged = true;
        });
        vehicleImageView1.setOnClickListener(v -> {
            checkImagePermission(PICK_IMAGE_REQUEST1);
//            openImagePicker1();
            isDataChanged = true;
        });
        vehicleImageView2.setOnClickListener(v -> {
            checkImagePermission(PICK_IMAGE_REQUEST2);
//            openImagePicker2();
            isDataChanged = true;
        });
        vehicleImageView3.setOnClickListener(v -> {
            checkImagePermission(PICK_IMAGE_REQUEST3);
//            openImagePicker3();
            isDataChanged = true;
        });
        vehicleImageView4.setOnClickListener(v -> {
            checkImagePermission(PICK_IMAGE_REQUEST4);
//            openImagePicker4();
            isDataChanged = true;
        });

        registrationDocImageView.setOnClickListener(v -> {
            checkImagePermission(PICK_REG_DOC_REQUEST);
//            openRegistrationDocPicker();
            isDataChanged = true;
        });

        // التعامل مع زر الرجوع في Toolbar (السهم)
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (isDataChanged) {
                            // عرض رسالة تأكيد الحفظ فقط إذا عدل المستخدم
                            showExitConfirmationDialog(getContext());
                        } else {
                            // لو ما في تعديل، رجوع عادي بدون رسالة
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                });
        // ======== ضبط وضع التعديل أو الإضافة ========
        Bundle args = getArguments();
        if (args != null && args.containsKey("vehicle_id")) {

            isEditMode = true;
            vehicleId = args.getInt("vehicle_id");
            makeEditText.setText(args.getString("make", ""));
            vh_price.setText(String.valueOf(args.getInt("vh_price", 0)));
            yearEditText.setText(args.getString("year", ""));
            vehicle_nameEditText.setText(args.getString("vehicle_name", ""));
            colorEditText.setText(args.getString("color", ""));
            plateEditText.setText(args.getString("license_plate", ""));
            seatsEditText.setText(String.valueOf(args.getInt("total_seats", 0)));
            availableSeatsEditText.setText(args.getString("available_seats", ""));

            String fuelType = args.getString("fuel_type", "");
            if (args.containsKey("vehicle_type_ref")) {
                try {
                    vehicleTypeFromArgs = Integer.parseInt(args.getString("vehicle_type_ref"));
                } catch (NumberFormatException e) {
                }
            }

            ArrayAdapter fuelAdapter2 = (ArrayAdapter) fuelTypeSpinner.getAdapter();
            int fuelPosition = fuelAdapter2.getPosition(fuelType);
            fuelTypeSpinner.setSelection(fuelPosition);

            String imageUrl = args.getString("vehicle_image", null);
            if (imageUrl != null) {
                Glide.with(requireContext()).load(imageUrl).into(vehicleImageView);
                frame_img.setVisibility(View.GONE);
            }
            String imageUrl1 = args.getString("vehicle_image1", null);
            if (imageUrl1 != null) {
                Glide.with(requireContext()).load(imageUrl1).into(vehicleImageView1);
                frame_img1.setVisibility(View.GONE);
            }
            String imageUrl2 = args.getString("vehicle_image2", null);
            if (imageUrl2 != null) {
                Glide.with(requireContext()).load(imageUrl2).into(vehicleImageView2);
                frame_img2.setVisibility(View.GONE);
            }
            String imageUrl3 = args.getString("vehicle_image3", null);
            if (imageUrl3 != null) {
                Glide.with(requireContext()).load(imageUrl3).into(vehicleImageView3);
                frame_img3.setVisibility(View.GONE);
            }
            String imageUrl4 = args.getString("vehicle_image4", null);
            if (imageUrl4 != null) {
                Glide.with(requireContext()).load(imageUrl4).into(vehicleImageView4);
                frame_img4.setVisibility(View.GONE);
            }

            String regDocUrl = args.getString("registration_document", null);
            if (regDocUrl != null) {
                Glide.with(requireContext()).load(regDocUrl).into(registrationDocImageView);
                frame_doc.setVisibility(View.GONE);
            }

            submitButton.setText("حفظ");
            isDataChanged = false;

        } else {
            isEditMode = false;
            submitButton.setText("إضافة المركبة");
        }

        submitButton.setOnClickListener(v -> {
            validateAndSendData();
        });
        return view;
    }

    private void validateAndSendData() {
        firstErrorView = null;
        boolean isValid = true;

        if (makeEditText.getText().toString().trim().isEmpty()) {
            makeError.setText("يرجى إدخال اسم الشركة");
            makeEditText.setError("يرجى إدخال اسم الشركة");
            UserUtils.setEditTextState(makeEditText, true);

            makeError.setVisibility(View.VISIBLE);
            isValid = false;
            firstErrorView = makeError;
        } else {
            makeError.setVisibility(View.GONE);
            UserUtils.setEditTextState(makeEditText, false);
        }

        if (vh_price.getText().toString().trim().isEmpty()) {
            vh_priceError.setText("يرجى إدخال السعر لكل مقعد");
            vh_price.setError("يرجى إدخال السعر لكل مقعد");
            UserUtils.setEditTextState(vh_price, true);
//            vh_price.setBackgroundResource(R.drawable.edittext_background);
            vh_priceError.setVisibility(View.VISIBLE);
            isValid = false;
            firstErrorView = vh_priceError;
        } else {
            vh_priceError.setVisibility(View.GONE);
            UserUtils.setEditTextState(vh_price, false);

        }

        if (yearEditText.getText().toString().trim().isEmpty()) {
            yearError.setText("يرجى إدخال سنة الصنع");
            yearEditText.setError("يرجى إدخال سنة الصنع");
            UserUtils.setEditTextState(yearEditText, true);

            yearError.setVisibility(View.VISIBLE);
            isValid = false;
            if (firstErrorView == null) firstErrorView = yearError;
        } else {
            yearError.setVisibility(View.GONE);
            UserUtils.setEditTextState(yearEditText, false);
        }
        if (vehicle_nameEditText.getText().toString().trim().isEmpty()) {
            vehicle_nameError.setText("يرجى إدخال اسم المركبة");
            vehicle_nameEditText.setError("يرجى إدخال اسم المركبة");
            UserUtils.setEditTextState(vehicle_nameEditText, true);

            vehicle_nameError.setVisibility(View.VISIBLE);
            isValid = false;
            if (firstErrorView == null) firstErrorView = vehicle_nameError;
        } else {
            vehicle_nameError.setVisibility(View.GONE);
            UserUtils.setEditTextState(vehicle_nameEditText, false);
        }

        if (colorEditText.getText().toString().trim().isEmpty()) {
            colorError.setText("يرجى إدخال لون المركبة");
            colorEditText.setError("يرجى إدخال لون المركبة");
            UserUtils.setEditTextState(colorEditText, true);

            colorError.setVisibility(View.VISIBLE);
            isValid = false;
            if (firstErrorView == null) firstErrorView = colorError;
        } else {
            colorError.setVisibility(View.GONE);
            UserUtils.setEditTextState(colorEditText, false);
        }

        if (plateEditText.getText().toString().trim().isEmpty()) {
            licensePlateError.setText("يرجى إدخال رقم اللوحة");
            plateEditText.setError("يرجى إدخال رقم اللوحة");
            UserUtils.setEditTextState(plateEditText, true);

            licensePlateError.setVisibility(View.VISIBLE);
            isValid = false;
            if (firstErrorView == null) firstErrorView = licensePlateError;
        } else {
            licensePlateError.setVisibility(View.GONE);
            UserUtils.setEditTextState(plateEditText, false);
        }

        if (seatsEditText.getText().toString().trim().isEmpty()) {
            totalSeatsError.setText("يرجى إدخال عدد المقاعد");
            seatsEditText.setError("يرجى إدخال عدد المقاعد");
            UserUtils.setEditTextState(seatsEditText, true);

            totalSeatsError.setVisibility(View.VISIBLE);
            isValid = false;
            if (firstErrorView == null) firstErrorView = totalSeatsError;
        } else {
            totalSeatsError.setVisibility(View.GONE);
            UserUtils.setEditTextState(seatsEditText, false);

        }

        if (availableSeatsEditText.getText().toString().trim().isEmpty()) {
            availableSeatsError.setText("يرجى إدخال عدد المقاعد المتاحة");
            availableSeatsEditText.setError("يرجى إدخال عدد المقاعد المتاحة");
            UserUtils.setEditTextState(availableSeatsEditText, true);

            availableSeatsError.setVisibility(View.VISIBLE);
            isValid = false;
            if (firstErrorView == null) firstErrorView = availableSeatsError;
        } else {
            availableSeatsError.setVisibility(View.GONE);
            UserUtils.setEditTextState(availableSeatsEditText, false);
        }

        if (vehicleImageView.getDrawable() == null) {
            vehicleImageViewError.setText("يرجى إضافة صورة المركبة");
            vehicleImageViewError.setVisibility(View.VISIBLE);
            isValid = false;
            if (firstErrorView == null) firstErrorView = vehicleImageViewError;
        } else vehicleImageViewError.setVisibility(View.GONE);

        if (registrationDocImageView.getDrawable() == null) {
            registrationDocImageViewError.setText("يرجى إضافة وثيقة التسجيل");
            registrationDocImageViewError.setVisibility(View.VISIBLE);
            isValid = false;
            if (firstErrorView == null) firstErrorView = registrationDocImageViewError;
        } else registrationDocImageViewError.setVisibility(View.GONE);

        if (firstErrorView != null) {
            scrollViewcon.post(() -> scrollViewcon.smoothScrollTo(0, firstErrorView.getTop()));
        }

        if (isValid) {
            sendVehicleData();
        }
    }

    private AlertDialog exitDialog;

    private void showExitConfirmationDialog(Context context) {
        if (getActivity() == null || getActivity().isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_confirmationt, null);
        builder.setView(dialogView);

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setTextSize(18);
        btnNo.setTextSize(18);

        // الأحداث
        btnYes.setOnClickListener(v -> {
            validateAndSendData();
            if (firstErrorView == null) {
                exitDialog.dismiss();
                isDataChanged = false;
            }
        });

        btnNo.setOnClickListener(v -> {
            isDataChanged = false;
            requireActivity().onBackPressed();
            exitDialog.dismiss();
        });

        exitDialog = builder.create();

        // ✅ إضافة الضبابية
        ViewGroup decorView = requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(context).radius(15).sampling(2).onto(decorView);
        exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));

        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }

        if (isAdded() && !getActivity().isFinishing()) {
            exitDialog.show();
        }
    }

    private void sendUpdateRequest(int vehicleId) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("جاري تحديث بيانات المركبة...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            try {
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "vehicles/" + vehicleId + "/?owner=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                String boundary = "===" + System.currentTimeMillis() + "===";
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                DataOutputStream request = new DataOutputStream(conn.getOutputStream());

                writeFormField(request, "owner", String.valueOf(userId), boundary);
                writeFormField(request, "make", makeEditText.getText().toString().trim(), boundary);
                writeFormField(request, "year", yearEditText.getText().toString().trim(), boundary);
                writeFormField(request, "vehicle_name", vehicle_nameEditText.getText().toString().trim(), boundary);
                writeFormField(request, "color", colorEditText.getText().toString().trim(), boundary);
                writeFormField(request, "license_plate", plateEditText.getText().toString().trim(), boundary);
                writeFormField(request, "total_seats", seatsEditText.getText().toString().trim(), boundary);
                writeFormField(request, "available_seats", availableSeatsEditText.getText().toString().trim(), boundary);
                writeFormField(request, "vh_price", vh_price.getText().toString().trim(), boundary);

                int fuelIndex = fuelTypeSpinner.getSelectedItemPosition();
                String fuelValue = fuelTypes[fuelIndex];
                writeFormField(request, "fuel_type", fuelValue, boundary);

                String selectedName = vehicleTypeSpinner.getSelectedItem().toString();
                int vehicleTypeId = vehicleTypeMap.get(selectedName);
                writeFormField(request, "vehicle_type", selectedName, boundary);

                writeFormField(request, "vehicle_type_ref", String.valueOf(vehicleTypeId), boundary);

                writeFormField(request, "is_active", "true", boundary);
                if (registrationDocUri != null) {
                    writeFileField(request, "registration_document", registrationDocUri, boundary);
                }

                // إضافة الصورة إذا موجودة
                if (selectedImageUri != null) {
                    writeFileField(request, "vehicle_image", selectedImageUri, boundary);
                }
                if (selectedImageUri1 != null) {
                    writeFileField(request, "vehicle_image1", selectedImageUri1, boundary);
                }
                if (selectedImageUri2 != null) {
                    writeFileField(request, "vehicle_image2", selectedImageUri2, boundary);
                }
                if (selectedImageUri3 != null) {
                    writeFileField(request, "vehicle_image3", selectedImageUri3, boundary);
                }
                if (selectedImageUri4 != null) {
                    writeFileField(request, "vehicle_image4", selectedImageUri4, boundary);
                }
//                if (selectedImageUri != null) {
//
//                    // استخراج الامتداد الأصلي
//                    String extension = ".jpg"; // افتراضي
//                    String mimeType = getActivity().getContentResolver().getType(selectedImageUri);
//                    if (mimeType != null) {
//                        String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
//                        if (extFromMime != null) {
//                            extension = "." + extFromMime; // مثل ".png" أو ".jpeg"
//                        }
//                    }
//
//
////                    writeFileField(request, "extra_files[]", smallImageUri, boundary);
//
//                }

                String s = "{ owner: " + userId +
                        " , make: " + makeEditText.getText().toString().trim() +
                        " , year: " + yearEditText.getText().toString().trim() +
                        " , vehicle_name: " + vehicle_nameEditText.getText().toString().trim() +
                        " , color: " + colorEditText.getText().toString().trim() +
                        " , license_plate: " + plateEditText.getText().toString().trim() +
                        " , total_seats: " + seatsEditText.getText().toString().trim() +
                        " , available_seats: " + availableSeatsEditText.getText().toString().trim() +
                        " , registration_document: " + registrationDocUri +
                        " , fuel_type: " + fuelValue +
                        " , vehicle_image: " + selectedImageUri +
                        " , vh_price: " + vh_price.getText().toString().trim() +
                        " , vehicle_type: " + selectedName +
                        " , vehicle_type_ref: " + vehicleTypeId +
                        " }";
                request.writeBytes("--" + boundary + "--" + "\r\n");
                request.flush();
                request.close();

                int responseCode = conn.getResponseCode();
                InputStream inputStream = (responseCode >= 200 && responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                String response = responseBuilder.toString();

                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        UserUtils.getMessageFromLocal(9, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        VehicleFragment vehicleFragment = new VehicleFragment();
                        isDataChanged = false;
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.full_screen_container, vehicleFragment) // معرف الحاوية في الـ Activity
                                .addToBackStack(null) // عشان الرجوع ممكن
                                .commit();

                    } catch (Exception e) {
                        UserUtils.sendLog(getContext(), "sendUpdateRequest", e.toString(), s, "add vehicles");
                        UserUtils.getMessageFromLocal(10, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                    }
                });

            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    UserUtils.getMessageFromLocal(11, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                    UserUtils.sendLog(getContext(), "sendUpdateRequest", e.toString(), e.toString(), "add vehicles");
                });
            }
        }).start();
    }


    private void loadVehicleTypes() {
        DBHelper dbHelper = new DBHelper(getContext());

        // مسح أي بيانات قديمة
        vehicleTypeNames.clear();
        vehicleTypeMap.clear();

        // جلب البيانات من جدول SQLite
        List<DBHelper.VehicleType> vehicleTypes = dbHelper.getVehicleTypes(1);

        // ملء المصفوفات
        for (DBHelper.VehicleType v : vehicleTypes) {
            vehicleTypeNames.add(v.getName());
            vehicleTypeMap.put(v.getName(), v.getId());
        }

        // تهيئة Spinner إذا كانت Fragment مضافة
        if (isAdded()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, vehicleTypeNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vehicleTypeSpinner.setAdapter(adapter);

            // تحديد العنصر من المتغير إذا كان موجود
            if (vehicleTypeFromArgs != null) {
                int positionToSelect = -1;
                for (int i = 0; i < vehicleTypeNames.size(); i++) {
                    String name = vehicleTypeNames.get(i);
                    int id = vehicleTypeMap.get(name);
                    if (id == vehicleTypeFromArgs) {
                        positionToSelect = i;
                        break;
                    }
                }
                if (positionToSelect >= 0) {
                    vehicleTypeSpinner.setSelection(positionToSelect);
                }
            }
        }

        if (vehicleTypes.isEmpty()) {
            UserUtils.getMessageFromLocal(3, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(getActivity(), message);
                }

                @Override
                public void onError(String error) {
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DBHelper dbHelper = new DBHelper(getContext());

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImageFromGallery(requestCode);
        } else {
            UserUtils.getMessageFromLocal(170, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(getActivity(), message);
                }

                @Override
                public void onError(String error) {
                }
            });
        }
    }


    private void checkImagePermission(int requestCode) {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ Photo Picker
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            // Android 12 وأقل
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

        startActivityForResult(intent, requestCode);
    }


    private void selectImageFromGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "اختر صورة"), requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DBHelper dbHelper = new DBHelper(getContext());

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri sourceUri = data.getData();
            if (sourceUri != null) {
                try {
                    // استخرج الامتداد (jpg / png ...) من نوع الملف
                    String mimeType = getContext().getContentResolver().getType(sourceUri);
                    String extension = ".jpg"; // افتراضي
                    if (mimeType != null) {
                        String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                        if (extFromMime != null) {
                            extension = "." + extFromMime;
                        }
                    }

                    // اسم مميز للملف
                    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
                    String fileName = "vhImg_" + date + extension;

                    File destFile = new File(getContext().getCacheDir(), fileName);
                    Uri destinationUri = Uri.fromFile(destFile);

                    // نسخ الصورة من المصدر إلى الوجهة (الأصلية)
                    InputStream inputStream = getContext().getContentResolver().openInputStream(sourceUri);
                    OutputStream outputStream = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    // تحديث الصورة
                    selectedImageUri = destinationUri;
                    vehicleImageView.setImageURI(destinationUri);
                    frame_img.setVisibility(View.GONE);


                } catch (Exception e) {
                    UserUtils.getMessageFromLocal(13, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST1 && resultCode == Activity.RESULT_OK) {
            Uri sourceUri = data.getData();
            if (sourceUri != null) {
                try {
                    // استخرج الامتداد (jpg / png ...) من نوع الملف
                    String mimeType = getContext().getContentResolver().getType(sourceUri);
                    String extension = ".jpg"; // افتراضي
                    if (mimeType != null) {
                        String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                        if (extFromMime != null) {
                            extension = "." + extFromMime;
                        }
                    }

                    // اسم مميز للملف
                    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
                    String fileName = "vhImg1_" + date + extension;

                    File destFile = new File(getContext().getCacheDir(), fileName);
                    Uri destinationUri = Uri.fromFile(destFile);

                    // نسخ الصورة من المصدر إلى الوجهة (الأصلية)
                    InputStream inputStream = getContext().getContentResolver().openInputStream(sourceUri);
                    OutputStream outputStream = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    // تحديث الصورة
                    selectedImageUri1 = destinationUri;
                    vehicleImageView1.setImageURI(destinationUri);
                    frame_img1.setVisibility(View.GONE);

                } catch (Exception e) {
                    UserUtils.getMessageFromLocal(13, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST2 && resultCode == Activity.RESULT_OK) {
            Uri sourceUri = data.getData();
            if (sourceUri != null) {
                try {
                    // استخرج الامتداد (jpg / png ...) من نوع الملف
                    String mimeType = getContext().getContentResolver().getType(sourceUri);
                    String extension = ".jpg"; // افتراضي
                    if (mimeType != null) {
                        String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                        if (extFromMime != null) {
                            extension = "." + extFromMime;
                        }
                    }

                    // اسم مميز للملف
                    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
                    String fileName = "vhImg2_" + date + extension;

                    File destFile = new File(getContext().getCacheDir(), fileName);
                    Uri destinationUri = Uri.fromFile(destFile);

                    // نسخ الصورة من المصدر إلى الوجهة (الأصلية)
                    InputStream inputStream = getContext().getContentResolver().openInputStream(sourceUri);
                    OutputStream outputStream = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    // تحديث الصورة
                    selectedImageUri2 = destinationUri;
                    vehicleImageView2.setImageURI(destinationUri);
                    frame_img2.setVisibility(View.GONE);


                } catch (Exception e) {
                    UserUtils.getMessageFromLocal(13, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST3 && resultCode == Activity.RESULT_OK) {
            Uri sourceUri = data.getData();
            if (sourceUri != null) {
                try {
                    // استخرج الامتداد (jpg / png ...) من نوع الملف
                    String mimeType = getContext().getContentResolver().getType(sourceUri);
                    String extension = ".jpg"; // افتراضي
                    if (mimeType != null) {
                        String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                        if (extFromMime != null) {
                            extension = "." + extFromMime;
                        }
                    }

                    // اسم مميز للملف
                    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
                    String fileName = "vhImg3_" + date + extension;

                    File destFile = new File(getContext().getCacheDir(), fileName);
                    Uri destinationUri = Uri.fromFile(destFile);

                    // نسخ الصورة من المصدر إلى الوجهة (الأصلية)
                    InputStream inputStream = getContext().getContentResolver().openInputStream(sourceUri);
                    OutputStream outputStream = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    // تحديث الصورة
                    selectedImageUri3 = destinationUri;
                    vehicleImageView3.setImageURI(destinationUri);
                    frame_img3.setVisibility(View.GONE);


                } catch (Exception e) {
                    UserUtils.getMessageFromLocal(13, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST4 && resultCode == Activity.RESULT_OK) {
            Uri sourceUri = data.getData();
            if (sourceUri != null) {
                try {
                    // استخرج الامتداد (jpg / png ...) من نوع الملف
                    String mimeType = getContext().getContentResolver().getType(sourceUri);
                    String extension = ".jpg"; // افتراضي
                    if (mimeType != null) {
                        String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                        if (extFromMime != null) {
                            extension = "." + extFromMime;
                        }
                    }

                    // اسم مميز للملف
                    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
                    String fileName = "vhImg4_" + date + extension;

                    File destFile = new File(getContext().getCacheDir(), fileName);
                    Uri destinationUri = Uri.fromFile(destFile);

                    // نسخ الصورة من المصدر إلى الوجهة (الأصلية)
                    InputStream inputStream = getContext().getContentResolver().openInputStream(sourceUri);
                    OutputStream outputStream = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    // تحديث الصورة
                    selectedImageUri4 = destinationUri;
                    vehicleImageView4.setImageURI(destinationUri);
                    frame_img4.setVisibility(View.GONE);


                } catch (Exception e) {
                    UserUtils.getMessageFromLocal(13, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            }
        } else if (requestCode == PICK_REG_DOC_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri docUri = data.getData();
            if (docUri != null) {
                try {
                    String mimeType = getContext().getContentResolver().getType(docUri);
                    String extension = ".jpg";
                    if (mimeType != null) {
                        String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                        if (extFromMime != null) {
                            extension = "." + extFromMime;
                        }
                    }

                    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
                    String fileName = "regdoc_" + date + extension;

                    File regDocFile = new File(getContext().getCacheDir(), fileName);
                    Uri destinationUri = Uri.fromFile(regDocFile);

                    InputStream inputStream = getContext().getContentResolver().openInputStream(docUri);
                    OutputStream outputStream = new FileOutputStream(regDocFile);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    registrationDocUri = destinationUri;
                    registrationDocImageView.setImageURI(destinationUri);
                    frame_doc.setVisibility(View.GONE);

                } catch (Exception e) {
                    UserUtils.getMessageFromLocal(14, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            }
        }
    }

    private void showYearDialog(Context context) {
        List<String> years = new ArrayList<>();
        for (int y = 2025; y >= 1996; y--) {
            years.add(String.valueOf(y));
        }

        View content = LayoutInflater.from(context).inflate(R.layout.dialog_year_list, null, false);
        ListView listView = content.findViewById(R.id.yearList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.item_year, R.id.text1, years);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                highlightCenterItem(listView);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                highlightCenterItem(listView);
            }
        });

        listView.post(() -> highlightCenterItem(listView));

        listView.setVerticalScrollBarEnabled(false);   // إخفاء شريط التمرير
        listView.setFastScrollEnabled(false);          // بدون فاست سكرول

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("سنة الصنع")
                .setView(content)
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedYear = adapter.getItem(position);
            if (selectedYear != null) {
                yearEditText.setText(selectedYear);
            }
            dialog.dismiss();
        });

        // ✅ تحديد السنة المحفوظة في EditText وعرضها في الوسط
        String currentYear = yearEditText.getText().toString();
        if (!currentYear.isEmpty()) {
            int index = years.indexOf(currentYear);
            if (index != -1) {
                listView.post(() -> {
                    int offset = listView.getHeight() / 2
                            - listView.getChildAt(0).getHeight() / 2;
                    listView.setSelectionFromTop(index, offset);
                    highlightCenterItem(listView); // يلون العنصر بالنص
                });
            }
        }

        // إضافة الضبابية
        ViewGroup decorView = ((AppCompatActivity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(context).radius(15).sampling(2).onto(decorView);
        dialog.setOnDismissListener(d -> Blurry.delete(decorView));

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialog.show();
    }

    private void highlightCenterItem(ListView listView) {
        int firstVisible = listView.getFirstVisiblePosition();
        int lastVisible = listView.getLastVisiblePosition();
        int centerPosition = (firstVisible + lastVisible) / 2;

        for (int i = firstVisible; i <= lastVisible; i++) {
            View child = listView.getChildAt(i - firstVisible);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                if (i == centerPosition) {
                    textView.setTextColor(ContextCompat.getColor(listView.getContext(), R.color.primary));
                    textView.setTextSize(22);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    textView.setTextColor(ContextCompat.getColor(listView.getContext(), R.color.text));
                    textView.setTextSize(20);
                    textView.setTypeface(Typeface.DEFAULT);
                }
            }
        }
    }

    private void writeFormField(DataOutputStream request, String fieldName, String fieldValue, String boundary) throws IOException {
        request.writeBytes("--" + boundary + "\r\n");
        request.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n");
        request.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n");
        request.writeBytes("\r\n");
        request.write(fieldValue.getBytes(StandardCharsets.UTF_8));
        request.writeBytes("\r\n");
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
            if (result != null && result.contains("/")) {
                result = result.substring(result.lastIndexOf("/") + 1);
            }
        }

        return result;
    }

    private void writeFileField(DataOutputStream out, String fieldName, Uri fileUri, String boundary) throws IOException {
        String fileName = getFileNameFromUri(fileUri); // دالة تجلب اسم الملف من الـ Uri
        String mimeType = getContext().getContentResolver().getType(fileUri);

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        out.writeBytes("Content-Type: " + mimeType + "\r\n");
        out.writeBytes("\r\n");

        InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        out.writeBytes("\r\n");
    }


    private void sendVehicleData() {
        if (isEditMode && vehicleId != null) {
            sendUpdateRequest(vehicleId);
        } else {
            sendCreateRequest();
        }
    }

    private void sendCreateRequest() {
//        ProgressDialog progressDialog = new ProgressDialog(getContext());
//        progressDialog.setMessage("جاري إرسال بيانات المركبة...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
        DBHelper dbHelper = new DBHelper(getContext());
        UserUtils.showSuccessGif(1, requireActivity(), null);

        new Thread(() -> {
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "vehicles/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                String boundary = "===" + System.currentTimeMillis() + "===";
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                int userId = prefs.getInt("user_id", -1);

                int totalSeats = Integer.parseInt(seatsEditText.getText().toString().trim());
                int availableSeats = Integer.parseInt(availableSeatsEditText.getText().toString().trim());
                int year = Integer.parseInt(yearEditText.getText().toString().trim());
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);


                if (availableSeats > totalSeats) {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.hideSuccessGif(getActivity());

                        UserUtils.getMessageFromLocal(15, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                    });
                    return;
                }

                int fuelIndex = fuelTypeSpinner.getSelectedItemPosition();
                String fuelValue = fuelTypes[fuelIndex];

                int vehicleIndex = vehicleTypeSpinner.getSelectedItemPosition();

                DataOutputStream request = new DataOutputStream(conn.getOutputStream());
                String selectedName = vehicleTypeSpinner.getSelectedItem().toString();
                int vehicleTypeId = vehicleTypeMap.get(selectedName);

                // دالة مساعدة لكتابة الحقول النصية
                writeFormField(request, "owner", String.valueOf(userId), boundary);
                writeFormField(request, "make", makeEditText.getText().toString().trim(), boundary);
                writeFormField(request, "vehicle_name", vehicle_nameEditText.getText().toString().trim(), boundary);
                writeFormField(request, "year", String.valueOf(year), boundary);
                writeFormField(request, "color", colorEditText.getText().toString().trim(), boundary);
                writeFormField(request, "license_plate", plateEditText.getText().toString().trim(), boundary);
                writeFormField(request, "total_seats", String.valueOf(totalSeats), boundary);
                writeFormField(request, "available_seats", String.valueOf(availableSeats), boundary);
                writeFormField(request, "fuel_type", fuelValue, boundary);
                writeFormField(request, "vehicle_type_ref", String.valueOf(vehicleTypeId), boundary);
                writeFormField(request, "is_active", "true", boundary);
                writeFormField(request, "vehicle_type", selectedName, boundary);
                writeFormField(request, "vh_price", vh_price.getText().toString().trim(), boundary);


                if (selectedImageUri != null) {
                    writeFileField(request, "vehicle_image", selectedImageUri, boundary);

                    // استخراج الامتداد الأصلي
                    String extension = ".jpg"; // افتراضي
                    String mimeType = getActivity().getContentResolver().getType(selectedImageUri);

                    if (mimeType != null) {
                        String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

                        if (extFromMime != null) {
                            extension = "." + extFromMime; // مثل ".png" أو ".jpeg"
                        }
                    }


//                    writeFileField(request, "extra_files[]", smallImageUri, boundary);

                }
                if (selectedImageUri1 != null) {
                    writeFileField(request, "vehicle_image1", selectedImageUri1, boundary);
                }
                if (selectedImageUri2 != null) {
                    writeFileField(request, "vehicle_image2", selectedImageUri2, boundary);
                }
                if (selectedImageUri3 != null) {
                    writeFileField(request, "vehicle_image3", selectedImageUri3, boundary);
                }
                if (selectedImageUri4 != null) {
                    writeFileField(request, "vehicle_image4", selectedImageUri4, boundary);
                }
                if (registrationDocUri != null) {
                    writeFileField(request, "registration_document", registrationDocUri, boundary);
                }

                request.writeBytes("--" + boundary + "--" + "\r\n");
                request.flush();
                request.close();
                String s = "{ owner: " + userId +
                        " , make: " + makeEditText.getText().toString().trim() +
                        " , vehicle_name: " + vehicle_nameEditText.getText().toString().trim() +
                        " , year: " + year +
                        " , color: " + colorEditText.getText().toString().trim() +
                        " , license_plate: " + plateEditText.getText().toString().trim() +
                        " , total_seats: " + totalSeats +
                        " , available_seats: " + availableSeats +
                        " , registration_document: " + registrationDocUri +
                        " , fuel_type: " + fuelValue +
                        " , vehicle_image: " + selectedImageUri +
                        " , vehicle_type: " + selectedName +
                        " , vh_price: " + vh_price.getText().toString().trim() +
                        " , vehicle_type_ref: " + vehicleTypeId +
                        " }";
                int responseCode = conn.getResponseCode();
                InputStream inputStream = (responseCode >= 200 && responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                String response = responseBuilder.toString();

                getActivity().runOnUiThread(() -> {
                    UserUtils.hideSuccessGif(getActivity());

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("vehicle_id")) {
                            if (getActivity() != null) {
                                if (getActivity() != null) {
//                                    ((HomePage) requireActivity()).selectTab(R.id.nav_reservation);
                                    VehicleFragment vehicleFragment = new VehicleFragment();
                                    isDataChanged = false;
                                    getActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.full_screen_container, vehicleFragment)
                                            .addToBackStack(null)
                                            .commit();
//                                    if (getActivity() != null) {
//                                        getActivity().getSupportFragmentManager().popBackStack();
//                                    }
                                    UserUtils.getMessageFromLocal(18, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            UserUtils.ToastMessages(getActivity(), message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }
                                    });
                                }
                            }
//                            UserUtils.getMessageFromLocal(18, dbHelper, new UserUtils.MessageCallback() {
//                                @Override
//                                public void onSuccess(String message) {
//                                    UserUtils.ToastMessages(getActivity(), message);
//                                }
//
//                                @Override
//                                public void onError(String error) {
//                                }
//
//                            });


                        } else {
                            handleServerErrors(jsonResponse);
                            UserUtils.hideSuccessGif(getActivity());

                            UserUtils.sendLog(getContext(), "sendCreateRequest", String.valueOf(jsonResponse), s, "add vehicles");
                        }
                    } catch (Exception e) {
                        UserUtils.sendLog(getContext(), "sendCreateRequest", e.toString(), s, "add vehicles");
                        UserUtils.getMessageFromLocal(19, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        UserUtils.hideSuccessGif(getActivity());

                    }
                });

            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "sendCreateRequest", e.toString(), e.toString(), "add vehicles");
                getActivity().runOnUiThread(() -> {
                    UserUtils.hideSuccessGif(getActivity());

                    UserUtils.getMessageFromLocal(19, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false); // ❌ يخفي سهم الرجوع
        }
    }

    private void handleServerErrors(JSONObject jsonResponse) {
        DBHelper dbHelper = new DBHelper(getContext());

        try {
            Iterator<String> keys = jsonResponse.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonResponse.get(key);
                if (value instanceof JSONArray) {
                    JSONArray arr = (JSONArray) value;
                    if (arr.length() > 0) {
                        JSONObject firstError = arr.getJSONObject(0);
                        String errorMessage = firstError.optString("message", "فشل في إضافة المركبة");
                        UserUtils.sendLog(getContext(), "handleServerErrors", String.valueOf(firstError), errorMessage, "add vehicles");
                        UserUtils.getMessageFromLocal(19, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        return;
                    }
                }
            }
            UserUtils.getMessageFromLocal(19, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(getActivity(), message);
                }

                @Override
                public void onError(String error) {
                }

            });
        } catch (Exception e) {
            UserUtils.sendLog(getContext(), "handleServerErrors", String.valueOf(e), e.toString(), "add vehicles");

        }
    }
}