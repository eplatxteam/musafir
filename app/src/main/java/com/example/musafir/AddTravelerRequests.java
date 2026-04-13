package com.example.musafir;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

public class AddTravelerRequests extends Fragment {

    EditText country, fromAddress, notes;
    Button addRequest;
    String BASE_URL = UserUtils.BASE_URL;
    //    ----------------------------------------------------------------
    int type_tr_id = -1;

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomePage) {
            ((HomePage) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    ImageView btnMinus, btnPlus;
    LinearLayout namesContainer, countryContent, FlightContainer, contentGo, contentBack, date_request, title_serv, containerHosting;
    int passengerCount = 1; // العدد الابتدائي
    TextView passengersTextView;
    RadioButton radio1, radio2, goBack, go, Guarantee, host;
    RadioGroup radioRequestType, radioTripType, radioisHosting;
    private int type_service = 1;
    private int is_hosting;
    private int Trip_Type = 1;
    public String type_tr_name = "";
    EditText DateGo, DateBack, DateRequest;
    int icon_tr = 1;
    Button downloadDocument;
    LottieAnimationView lottieWaveRef;
    long downloadId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_traveler_requests, container, false);

        if (getArguments() != null) {
            type_tr_id = getArguments().getInt("type_tr_id", -1);
            type_tr_name = getArguments().getString("type_tr_name", "");
            icon_tr = getArguments().getInt("icon_tr", -1);
//            int home = getArguments().getInt("home", -1);
//            if (home == 1) {
            if (getActivity() instanceof HomePage) {
                ((HomePage) getActivity()).updateToolbar(type_tr_name, false, icon_tr, 0);
            }
//            }
        }
        setHasOptionsMenu(true);

        date_request = view.findViewById(R.id.date_request);
        radioisHosting = view.findViewById(R.id.radioisHosting);
        Guarantee = view.findViewById(R.id.Guarantee);
        host = view.findViewById(R.id.host);
        title_serv = view.findViewById(R.id.title_serv);
        contentGo = view.findViewById(R.id.contentGo);
        contentBack = view.findViewById(R.id.contentBack);
        country = view.findViewById(R.id.country);
        containerHosting = view.findViewById(R.id.containerHosting);
        DateBack = view.findViewById(R.id.DateBack);
        DateGo = view.findViewById(R.id.DateGo);
        FlightContainer = view.findViewById(R.id.FlightContainer);
        radioTripType = view.findViewById(R.id.radioTripType);
        DateRequest = view.findViewById(R.id.DateRequest);
        goBack = view.findViewById(R.id.goBack);
//        back = view.findViewById(R.id.back);
        go = view.findViewById(R.id.go);
        countryContent = view.findViewById(R.id.countryContent);
        passengersTextView = view.findViewById(R.id.passengersTextView);
        notes = view.findViewById(R.id.notes);
        addRequest = view.findViewById(R.id.addRequest);
        btnMinus = view.findViewById(R.id.btnMinus);
        btnPlus = view.findViewById(R.id.btnPlus);
        namesContainer = view.findViewById(R.id.namesContainer);
        radioRequestType = view.findViewById(R.id.radioRequestType);
        radio1 = view.findViewById(R.id.radio1);
        radio2 = view.findViewById(R.id.radio2);
        downloadDocument = view.findViewById(R.id.downloadDocument);

        lottieWaveRef = view.findViewById(R.id.lottieWaveRef);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if (id == downloadId) {
                    lottieWaveRef.cancelAnimation();
                    lottieWaveRef.setVisibility(View.GONE);

                    UserUtils.ToastMessages(getActivity(), "تم تحميل الملف بنجاح");

                    getActivity().unregisterReceiver(this);
                }

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getActivity().registerReceiver(
                    onComplete,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            ContextCompat.registerReceiver(getActivity(), onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_NOT_EXPORTED);
        }


        SharedPreferences prefs = getActivity().getSharedPreferences("prefsLink", getActivity().MODE_PRIVATE);
        String link_document = prefs.getString("link_document", "");

        downloadDocument.setOnClickListener(v -> {
            downloadFile(link_document);
        });


        UserUtils.setEditTextState(country, false);
        UserUtils.setEditTextState(notes, false);
        UserUtils.setEditTextState(DateGo, false);
        UserUtils.setEditTextState(DateBack, false);
        UserUtils.setEditTextState(DateRequest, false);


        radioRequestType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio1) {
                type_service = 1;

            } else if (checkedId == R.id.radio2) {
                type_service = 2;
            }
        });

        radioTripType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.goBack) {
                Trip_Type = 1;
                contentGo.setVisibility(View.VISIBLE);
                contentBack.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.go) {
                Trip_Type = 2;
                contentGo.setVisibility(View.VISIBLE);
                contentBack.setVisibility(View.GONE);
            }
//            else if (checkedId == R.id.back) {
//                Trip_Type = 3;
//                contentGo.setVisibility(View.GONE);
//                contentBack.setVisibility(View.VISIBLE);
//            }
        });
        radioisHosting.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.Guarantee) {
                is_hosting = 1;
            } else if (checkedId == R.id.host) {
                is_hosting = 2;
            }
//            else if (checkedId == R.id.back) {
//                Trip_Type = 3;
//                contentGo.setVisibility(View.GONE);
//                contentBack.setVisibility(View.VISIBLE);
//            }
        });

        DateGo.setOnClickListener(v -> showDatePicker(DateGo));
        DateBack.setOnClickListener(v -> showDatePicker(DateBack));
        DateRequest.setOnClickListener(v -> showDatePicker(DateRequest));

        if (type_tr_id == 81) {
            countryContent.setVisibility(View.GONE);
            radioRequestType.setVisibility(View.VISIBLE);
            radioisHosting.setVisibility(View.VISIBLE);
            containerHosting.setVisibility(View.VISIBLE);
            title_serv.setVisibility(View.VISIBLE);
            FlightContainer.setVisibility(View.GONE);
            date_request.setVisibility(View.VISIBLE);
            downloadDocument.setVisibility(View.VISIBLE);
        } else if (type_tr_id == 83) {
            FlightContainer.setVisibility(View.VISIBLE);
            radioRequestType.setVisibility(View.GONE);
            radioisHosting.setVisibility(View.GONE);
            containerHosting.setVisibility(View.GONE);
            title_serv.setVisibility(View.GONE);
            date_request.setVisibility(View.GONE);
            downloadDocument.setVisibility(View.GONE);
        } else {
            countryContent.setVisibility(View.VISIBLE);
            radioRequestType.setVisibility(View.GONE);
            title_serv.setVisibility(View.GONE);
            radioisHosting.setVisibility(View.GONE);
            FlightContainer.setVisibility(View.GONE);
            date_request.setVisibility(View.VISIBLE);
            downloadDocument.setVisibility(View.GONE);
            containerHosting.setVisibility(View.GONE);
        }

        passengerCount = 1;
        addNameField(1);

        DBHelper dbHelper = new DBHelper(getContext());

        btnPlus.setOnClickListener(v -> {
            if (passengerCount < 6) {
                passengerCount++;
                passengersTextView.setText(String.valueOf(passengerCount));
                addNameField(passengerCount);
            } else {
                UserUtils.getMessageFromLocal(161, dbHelper, new UserUtils.MessageCallback() {
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

        btnMinus.setOnClickListener(v -> {
            if (passengerCount > 1) {
                removeLastNameField();
                passengerCount--;
                passengersTextView.setText(String.valueOf(passengerCount));
            }
        });

        // زر الإرسال
        addRequest.setOnClickListener(v -> {

            boolean valid = true;
            View firstErrorView = null;
            for (int i = 0; i < namesContainer.getChildCount(); i++) {

                LinearLayout item = (LinearLayout) namesContainer.getChildAt(i);

                EditText nameField = (EditText) item.getChildAt(0);

                // الوصول الصحيح لنص الحالة
                LinearLayout iconContainer = (LinearLayout) item.getChildAt(1);
                TextView statusText = (TextView) iconContainer.getChildAt(1);

                Uri imageUri = (Uri) nameField.getTag();
                if (imageUri == null) {
                    statusText.setText("يرجى رفع صورة الجواز");
                    statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    valid = false;
                    if (firstErrorView == null) firstErrorView = statusText;
                } else {
                    statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }

                // تحقق من الاسم
                if (nameField.getText().toString().trim().isEmpty()) {
                    nameField.setError("يرجى إدخال اسم المسافر");
                    UserUtils.setEditTextState(nameField, true);
                    valid = false;
                    if (firstErrorView == null) firstErrorView = nameField;
                } else {
                    UserUtils.setEditTextState(nameField, false);
                }
            }


            if (type_tr_id != 81 && type_tr_id != 83) {
                if (country.getText().toString().trim().isEmpty()) {
                    country.setError("يرجى إدخال الدولة");
                    UserUtils.setEditTextState(country, true);
                    valid = false;
                    if (firstErrorView == null) firstErrorView = country;
                } else {
                    country.setError(null);
                    UserUtils.setEditTextState(country, false);
                }
            }
            if (type_tr_id != 83) {
                if (DateRequest.getText().toString().trim().isEmpty()) {
                    DateRequest.setError("يرجى اختيار التاريخ");
                    UserUtils.setEditTextState(DateRequest, true);
                    valid = false;
                    if (firstErrorView == null) firstErrorView = DateRequest;
                } else {
                    DateRequest.setError(null);
                    UserUtils.setEditTextState(DateRequest, false);
                }
            }
            if (type_tr_id == 83) {
                if (country.getText().toString().trim().isEmpty()) {
                    country.setError("يرجى إدخال الدولة");
                    UserUtils.setEditTextState(country, true);
                    valid = false;
                    if (firstErrorView == null) firstErrorView = country;
                } else {
                    country.setError(null);
                    UserUtils.setEditTextState(country, false);
                }
                switch (Trip_Type) {
                    case 1: // ذهاب وعودة
                        if (DateGo.getText().toString().trim().isEmpty()) {
                            DateGo.setError("الرجاء إدخال تاريخ الذهاب");
                            UserUtils.setEditTextState(DateGo, true);
                            valid = false;
                            if (firstErrorView == null) firstErrorView = DateGo;
                        } else {
                            DateGo.setError(null);
                            UserUtils.setEditTextState(DateGo, false);
                        }

                        if (DateBack.getText().toString().trim().isEmpty()) {
                            DateBack.setError("الرجاء إدخال تاريخ العودة");
                            UserUtils.setEditTextState(DateBack, true);
                            valid = false;
                            if (firstErrorView == null) firstErrorView = DateBack;
                        } else {
                            DateBack.setError(null);
                            UserUtils.setEditTextState(DateBack, false);
                        }
                        break;

                    case 2: // ذهاب فقط
                        if (DateGo.getText().toString().trim().isEmpty()) {
                            DateGo.setError("الرجاء إدخال تاريخ الذهاب");
                            UserUtils.setEditTextState(DateGo, true);
                            valid = false;
                        } else {
                            DateGo.setError(null);
                            UserUtils.setEditTextState(DateGo, false);
                        }
                        break;

                    case 3: // عودة فقط
                        if (DateBack.getText().toString().trim().isEmpty()) {
                            DateBack.setError("الرجاء إدخال تاريخ العودة");
                            UserUtils.setEditTextState(DateBack, true);
                            valid = false;
                        } else {
                            DateBack.setError(null);
                            UserUtils.setEditTextState(DateBack, false);
                        }
                        break;
                }
            }
            if (firstErrorView != null) {
                firstErrorView.requestFocus();
                if (firstErrorView instanceof EditText) {
                    ((EditText) firstErrorView).setSelection(((EditText) firstErrorView).getText().length());
                }
                // تمرير ScrollView إذا موجود
                View parentScroll = view.findViewById(R.id.scrollViewcon); // ضع ID الخاص بالـ ScrollView
                if (parentScroll instanceof ScrollView) {
                    View finalFirstErrorView = firstErrorView;
                    parentScroll.post(() -> ((ScrollView) parentScroll).smoothScrollTo(0, finalFirstErrorView.getTop()));
                }
            }
            if (valid) {
                sendTravelerRequest();
            }
        });

        return view;
    }

    private void downloadFile(String url) {

        // إظهار اللوتي عند بدء التحميل
        lottieWaveRef.setVisibility(View.VISIBLE);
        lottieWaveRef.playAnimation(); // تشغيل التحريك

        String fileName = "الضمان.pdf";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("الضمان");
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        // حفظ رقم التحميل
        downloadId = manager.enqueue(request);

        registerDownloadReceiver();
    }

    private void registerDownloadReceiver() {
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    // إيقاف وتحجيم اللوتي
                    lottieWaveRef.cancelAnimation();
                    lottieWaveRef.setVisibility(View.GONE);

                    UserUtils.ToastMessages(getActivity(), "تم تحميل الملف بنجاح");

                    // إلغاء التسجيل بعد الاكتمال
                    getActivity().unregisterReceiver(this);
                }
            }
        };

        ContextCompat.registerReceiver(getActivity(), onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    @SuppressLint("DefaultLocale") String selectedDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, dayOfMonth);

                    targetEditText.setText(selectedDate);

                    // تحديث Calendar بقيمة التاريخ الجديد
                    calendar.set(year, month, dayOfMonth);

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // منع اختيار تاريخ قبل اليوم
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        datePickerDialog.show();
    }

    private void addNameField(int position) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        container.setPadding(0, 10, 0, 10);

        EditText nameField = new EditText(getContext());
        UserUtils.setEditTextState(nameField, false);

        nameField.setHint("اسم المسافر " + position);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        nameField.setLayoutParams(nameParams);
        nameField.setTextColor(getResources().getColor(android.R.color.black));
        nameField.setBackgroundResource(R.drawable.edittext_background);
        nameField.setPadding(35, 35, 35, 35);
        nameField.setSingleLine(true);
        nameField.setTextSize(16);
        nameField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});

        LinearLayout iconContainer = new LinearLayout(getContext());
        iconContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        iconContainer.setLayoutParams(iconContainerParams);

        ImageView uploadIcon = new ImageView(getContext());
        uploadIcon.setImageResource(R.drawable.ic_upload);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        uploadIcon.setLayoutParams(iconParams);
        uploadIcon.setPadding(0, 0, 5, 0);

        // نص الحالة تحت الأيقونة
        TextView statusText = new TextView(getContext());
        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        statusText.setTextSize(12);
        statusText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        statusText.setText("صورة الجواز");
        statusText.setTextColor(getResources().getColor(R.color.black));
        statusText.setPadding(0, 5, 0, 0);


        uploadIcon.setTag(position);
        uploadIcon.setOnClickListener(v -> {
            currentUploadTarget = uploadIcon;
            openGallery();
        });


        iconContainer.addView(uploadIcon);
        iconContainer.addView(statusText);

        container.addView(nameField);
        container.addView(iconContainer);

        namesContainer.addView(container);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();

            LinearLayout iconContainer = (LinearLayout) currentUploadTarget.getParent();

            LinearLayout container = (LinearLayout) iconContainer.getParent();

            EditText nameField = (EditText) container.getChildAt(0);

            TextView statusText = (TextView) iconContainer.getChildAt(1);

            nameField.setTag(imageUri);

            currentUploadTarget.setImageResource(R.drawable.upload_success);

            statusText.setVisibility(View.GONE);
        }
    }


    private static final int PICK_IMAGE_REQUEST = 1001;
    private ImageView currentUploadTarget;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            Uri selectedImage = data.getData();
//            if (currentUploadTarget != null) {
//                currentUploadTarget.setImageResource(R.drawable.upload_success);
//
//                // حفظ URI في EditText المرتبط
//                LinearLayout parent = (LinearLayout) currentUploadTarget.getParent();
//                EditText nameField = (EditText) parent.getChildAt(0);
//                nameField.setTag(selectedImage);
//            }
//        }
//    }


    private void removeLastNameField() {
        int count = namesContainer.getChildCount();
        if (count > 0) {
            namesContainer.removeViewAt(count - 1);
        }
    }


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


    private void sendTravelerRequest() {
        DBHelper dbHelper = new DBHelper(getContext());

        UserUtils.showSuccessGif(2, requireActivity(), null);

        new Thread(() -> {
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();

                URL url = new URL(BASE_URL + "TravelerRequests/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);
                int userId = prefs.getInt("user_id", -1);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // الحقول الأساسية
                writeFormField(dos, "passenger_id", String.valueOf(userId), boundary);
                writeFormField(dos, "type_tr_id", String.valueOf(type_tr_id), boundary);
//                writeFormField(dos, "type_service", String.valueOf(type_service), boundary);
                writeFormField(dos, "number_passenger", String.valueOf(passengerCount), boundary);
                writeFormField(dos, "country", country.getText().toString().trim(), boundary);
                writeFormField(dos, "notes", notes.getText().toString().trim(), boundary);
                writeFormField(dos, "daterequest", DateRequest.getText().toString().trim(), boundary);
                writeFormField(dos, "go_date", DateGo.getText().toString().trim(), boundary);
                writeFormField(dos, "back_date", DateBack.getText().toString().trim(), boundary);
                String s = "{ type_tr_name: " + type_tr_name +
                        " , number_passenger: " + passengersTextView.getText().toString().trim() +
                        " , notes: " + notes.getText().toString().trim() +
                        " , country: " + country.getText().toString().trim() +
                        " , passenger_id: " + userId +
                        " , type_tr_id: " + type_tr_id +
                        " , daterequest: " + DateRequest.getText().toString().trim() +
                        " , go_date: " + DateGo.getText().toString().trim() +
                        " , back_date: " + DateBack.getText().toString().trim() +
                        " , country: " + country.getText().toString().trim() +
                        " }";
                if (type_tr_id == 81) {
                    writeFormField(dos, "type_service", String.valueOf(type_service), boundary);
                    writeFormField(dos, "is_hosting", String.valueOf(is_hosting), boundary);
                }
                if (type_tr_id == 83) {
                    writeFormField(dos, "type_flight", String.valueOf(Trip_Type), boundary);
                }

                // أسماء وصور المسافرين
                for (int i = 0; i < namesContainer.getChildCount(); i++) {
                    LinearLayout child = (LinearLayout) namesContainer.getChildAt(i);
                    EditText nameField = (EditText) child.getChildAt(0);
                    Uri imageUri = (Uri) nameField.getTag();
                    int index = i + 1;
                    writeFormField(dos, "name_passenger" + index, nameField.getText().toString().trim(), boundary);

                    if (imageUri != null) {
                        writeFileField(dos, "passport_image" + index, imageUri, boundary);
                    }
                }

                // إنهاء boundary
                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode == 200 || responseCode == 201) ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                String response = builder.toString();

                getActivity().runOnUiThread(() -> {
                    UserUtils.hideSuccessGif(getActivity());

                    if (responseCode == 200 || responseCode == 201) {
                        if (getActivity() != null) {
                            if (getActivity() != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString("type_tr_name", type_tr_name);
                                bundle.putString("tr_status", "قيد المعالجة");
                                bundle.putString("number_passenger", passengersTextView.getText().toString().trim());
                                bundle.putInt("type_icon", icon_tr);
                                bundle.putInt("number_status", 1);
                                bundle.putString("notes", notes.getText().toString().trim());
                                bundle.putString("country", country.getText().toString().trim());

                                for (int i = 0; i < namesContainer.getChildCount(); i++) {
                                    LinearLayout child = (LinearLayout) namesContainer.getChildAt(i);
                                    EditText nameField = (EditText) child.getChildAt(0);

                                    String key = "name_passenger" + (i + 1);
                                    String value = nameField.getText().toString().trim();

                                    bundle.putString(key, value);
                                }
                                Fragment fragment = new TravelerRequestsDetails();
                                fragment.setArguments(bundle);
                                ((FragmentActivity) getContext()).getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.full_screen_container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                                UserUtils.getMessageFromLocal(142, dbHelper, new UserUtils.MessageCallback() {
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


                    } else {
                        UserUtils.sendLog(getContext(), "sendTravelerRequest", response, s, "AddTravelerRequests");
                        UserUtils.getMessageFromLocal(141, dbHelper, new UserUtils.MessageCallback() {
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
                UserUtils.hideSuccessGif(getActivity());

            } catch (Exception e) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.hideSuccessGif(getActivity());
                        UserUtils.sendLog(getContext(), "sendTravelerRequest", String.valueOf(e), e.getMessage(), "AddTravelerRequests");
                        UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
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
            }
        }).start();
    }

    private void writeFileField(DataOutputStream out, String fieldName, Uri fileUri, String boundary) throws IOException {
        String originalName = getFileNameFromUri(fileUri); // الاسم الأصلي
        String uniqueID = UUID.randomUUID().toString();   // جزء فريد
        String fileName = uniqueID + "_" + originalName;   // الاسم النهائي الفريد
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

    private void writeFormField(DataOutputStream request, String fieldName, String fieldValue, String boundary) throws IOException {
        request.writeBytes("--" + boundary + "\r\n");
        request.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n");
        request.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        request.write(fieldValue.getBytes(StandardCharsets.UTF_8));
        request.writeBytes("\r\n");
    }

}
