package com.example.musafir;

import static android.app.Activity.RESULT_OK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class AddTravelerRequests extends Fragment {

    EditText country, fromAddress, notes;
    Button addRequest;
    int passport_required;

    String BASE_URL = UserUtils.BASE_URL;
    //    ----------------------------------------------------------------
    int type_tr_id = -1;

    private static final int PICK_IMAGE_REQUEST = 1001;
    private ImageView currentUploadTarget;

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
        DBHelper dbHelper = new DBHelper(getContext());
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if (id == downloadId) {
                    lottieWaveRef.cancelAnimation();
                    lottieWaveRef.setVisibility(View.GONE);

                    UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(316, dbHelper));

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
        });
        radioisHosting.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.Guarantee) {
                is_hosting = 1;
            } else if (checkedId == R.id.host) {
                is_hosting = 2;
            }
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
        InputFilter arabicFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!((c >= 0x0621 && c <= 0x064A) || c == ' ')) {
                    return "";
                }
            }
            return null;
        };
        passengerCount = 1;
//        addNameField(1);
        addNameField(namesContainer, "اسم المسافر 1", "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});

        TextView txtPlus = view.findViewById(R.id.txtPlus);
        txtPlus.setOnClickListener(v -> {
            if (passengerCount < 6) {
                passengerCount++;
                passengersTextView.setText(String.valueOf(passengerCount));
                addNameField(namesContainer, "الاسم الكامل للمسافر " + passengerCount, "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});
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
        btnPlus.setOnClickListener(v -> {
            if (passengerCount < 6) {
                passengerCount++;
                passengersTextView.setText(String.valueOf(passengerCount));
                addNameField(namesContainer, "اسم المسافر " + passengerCount, "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});

//                addNameField(passengerCount);
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

        addRequest.setOnClickListener(v -> {
            if (!isAdded() || getActivity() == null) return;

            boolean valid = true;
            View firstErrorView = null;

            for (int i = 0; i < namesContainer.getChildCount(); i++) {
                View mainCard = namesContainer.getChildAt(i);
                LinearLayout vLayout = (LinearLayout) ((MaterialCardView) mainCard).getChildAt(0);
                LinearLayout nRow = (LinearLayout) vLayout.getChildAt(0);

                EditText nameField = (EditText) nRow.getChildAt(0);

                View btnUpload = (View) nameField.getTag(R.id.tag_upload_button);

                String name = nameField.getText().toString().trim();
                if (name.isEmpty()) {
                    nameField.setError("يرجى إدخال الاسم");
                    return;
                }

                if (nameField.getTag() == null || !(nameField.getTag() instanceof Uri)) {
                    UserUtils.ToastMessages(getActivity(), "يرجى ارفاق صورة الجواز لـ " + name);
                    if (btnUpload instanceof MaterialCardView) {
                        ((MaterialCardView) btnUpload).setStrokeColor(ColorStateList.valueOf(Color.RED));
                        ((MaterialCardView) btnUpload).setStrokeWidth(4);
                    }
                    return;
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
                if (isAdded()) {
                    sendTravelerRequest();
                }
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
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            lottieWaveRef.cancelAnimation();
                            lottieWaveRef.setVisibility(View.GONE);
                            UserUtils.ToastMessages(getActivity(), "تم تحميل الملف بنجاح");
                            openDownloadedFile(id);
                        });
                    }
                    context.unregisterReceiver(this);
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(onComplete,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED);
        }
    }

    private void openDownloadedFile(long downloadId) {
        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = manager.getUriForDownloadedFile(downloadId);
        DBHelper dbHelper = new DBHelper(getContext());
        if (uri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(intent);
            } catch (Exception e) {
                UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(314, dbHelper));
            }
        } else {
            UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(315, dbHelper));
        }
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

    private void addNameField(LinearLayout container, String hint, String initialText, InputFilter[] filters) {
        Context context = getContext();
        if (context == null) return;
        int dp8 = (int) (8 * context.getResources().getDisplayMetrics().density);
        int dp2 = (int) (2 * context.getResources().getDisplayMetrics().density);
        int dp30 = (int) (30 * context.getResources().getDisplayMetrics().density);
        int dp48 = (int) (48 * context.getResources().getDisplayMetrics().density);
        int dp12 = (int) (12 * context.getResources().getDisplayMetrics().density);

        com.google.android.material.card.MaterialCardView mainCard = new com.google.android.material.card.MaterialCardView(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        cardParams.setMargins(dp2, dp8, dp2, dp8);
        mainCard.setLayoutParams(cardParams);
        mainCard.setRadius(dp8);
        mainCard.setCardElevation(0f); // كما طلبتِ 0dp
        mainCard.setCardBackgroundColor(Color.parseColor("#FFFFFF")); // لون الكرت الرئيسي أبيض
        mainCard.setStrokeWidth(2); // إضافة إطار خفيف ليعطي شكل Modern
        mainCard.setStrokeColor(Color.parseColor("#E0E0E0"));

        LinearLayout verticalLayout = new LinearLayout(context);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setPadding(20, 20, 20, 20);

        LinearLayout nameRow = new LinearLayout(context);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);

        EditText nameInput = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        nameInput.setLayoutParams(lp);
        nameInput.setHint(hint);
        nameInput.setText(initialText);
        nameInput.setTextSize(16);
        nameInput.setBackground(null); // إلغاء الخط الافتراضي
        nameInput.setFilters(filters);
        UserUtils.setEditTextState(nameInput, false);
        nameInput.setPadding(20, 20, 20, 20);

        com.google.android.material.card.MaterialCardView uploadCard = new com.google.android.material.card.MaterialCardView(context);
        LinearLayout.LayoutParams uploadCardParams = new LinearLayout.LayoutParams(WRAP_CONTENT, 110);
        uploadCardParams.setMarginStart(dp8);
        uploadCard.setLayoutParams(uploadCardParams);
        uploadCard.setRadius(dp8);
        uploadCard.setCardElevation(0f);
        uploadCard.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        uploadCard.setStrokeWidth(0);

        LinearLayout uploadContent = new LinearLayout(context);
        uploadContent.setOrientation(LinearLayout.HORIZONTAL);
        uploadContent.setGravity(Gravity.CENTER);
        uploadContent.setPadding(20, 0, 25, 0);
        uploadCard.setTag(nameInput);
// الأيقونة (المجلد الذهبي)
        ImageView folderIcon = new ImageView(context);
        folderIcon.setImageResource(R.drawable.ic_upload); // تأكد من اسم الأيقونة لديك
        folderIcon.setColorFilter(Color.parseColor("#CC9407")); // لون ذهبي
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(50, 50);
        iconParams.setMarginEnd(5);
        folderIcon.setLayoutParams(iconParams);

        TextView tvAttach = new TextView(context);
        tvAttach.setText("صورة الجواز");
        tvAttach.setTextSize(12);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
//        tvAttach.setTypeface(typeface);
        tvAttach.setPadding(20, 20, 20, 20);
        tvAttach.setTextColor(Color.BLACK);
        tvAttach.setTypeface(typeface, Typeface.BOLD);

        uploadContent.addView(folderIcon);
        uploadContent.addView(tvAttach);
        uploadCard.addView(uploadContent);

        folderIcon.setTag(nameInput);
        folderIcon.setTag(R.id.tag_upload_card, uploadCard);
        uploadCard.setOnClickListener(v -> {
            currentUploadTarget = folderIcon;
            openGallery();
        });

        nameRow.addView(nameInput);
//        nameRow.addView(statusIcon);
        nameRow.addView(uploadCard);

        verticalLayout.addView(nameRow);

//        nameInput.setTag(R.id.tag_status_icon, statusIcon);
        nameInput.setTag(R.id.tag_upload_button, uploadCard);

        DBHelper dbHelper = new DBHelper(getContext());

        TextView notePrice = new TextView(context);
        notePrice.setId(R.id.NotePrice); // تعيين الـ ID الذي طلبته
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        noteParams.setMargins(20, 0, 20, 5); // هوامش جانبية لتتناسق مع الاسم
        notePrice.setLayoutParams(noteParams);
        notePrice.setTextSize(11);
        notePrice.setTextColor(Color.GRAY);
        notePrice.setText(UserUtils.getMessageFromLocalNew(363, dbHelper));

        verticalLayout.addView(notePrice);
//        if (passport_required == 1) {
//        TextView passportNote = new TextView(context);
//        LinearLayout.LayoutParams passportNoteParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
//        passportNoteParams.setMargins(20, 0, 20, 5);
//        passportNote.setLayoutParams(passportNoteParams);
//        passportNote.setTextSize(11);
//        passportNote.setTextColor(Color.GRAY);

//        passportNote.setText(UserUtils.getMessageFromLocalNew(364, dbHelper));

//        verticalLayout.addView(passportNote);
//        }
        nameInput.setTag(R.id.NotePrice, notePrice);
        mainCard.addView(verticalLayout);
        container.addView(mainCard);

//        nameInput.setTag(R.id.tag_status_icon, statusIcon);
        nameInput.setTag(R.id.tag_upload_button, uploadCard);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri == null || currentUploadTarget == null) return;

            // 1. تحديث الأيقونة التي ضغط عليها المستخدم
            currentUploadTarget.setImageResource(R.drawable.upload_success);
            currentUploadTarget.setColorFilter(Color.parseColor("#4CAF50"));

            // 2. الوصول للـ nameField المرتبط بهذا الصف بطريقة آمنة
            View uploadContent = (View) currentUploadTarget.getParent();
            View uploadCard = (View) uploadContent.getParent();
            LinearLayout nameRow = (LinearLayout) uploadCard.getParent();

            // الاسم دائماً أول عنصر في nameRow
            if (nameRow.getChildAt(0) instanceof EditText) {
                EditText nameField = (EditText) nameRow.getChildAt(0);
                nameField.setTag(imageUri); // تخزين مسار الصورة

                // إظهار علامة الصح الجانبية (statusIcon) باستخدام الـ Tag
                ImageView statusIcon = (ImageView) nameField.getTag(R.id.tag_status_icon);
                if (statusIcon != null) {
                    statusIcon.setVisibility(View.VISIBLE);
                }

                // إزالة حدود الخطأ الحمراء
                if (uploadCard instanceof MaterialCardView) {
                    ((MaterialCardView) uploadCard).setStrokeWidth(0);
                }
            }
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


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

                String token = prefs.getString("auth_token", null);
                int userId = prefs.getInt("user_id", -1);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                writeFormField(dos, "passenger_id", String.valueOf(userId), boundary);
                writeFormField(dos, "type_tr_id", String.valueOf(type_tr_id), boundary);
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

                for (int i = 0; i < namesContainer.getChildCount(); i++) {
                    MaterialCardView mainCard = (MaterialCardView) namesContainer.getChildAt(i);
                    LinearLayout vLayout = (LinearLayout) mainCard.getChildAt(0);
                    LinearLayout nameRow = (LinearLayout) vLayout.getChildAt(0);
                    EditText nameField = (EditText) nameRow.getChildAt(0);

                    Uri imageUri = (Uri) nameField.getTag();
                    int index = i + 1;

                    writeFormField(dos, "name_passenger" + index, nameField.getText().toString().trim(), boundary);

                    if (imageUri != null) {
                        writeFileField(dos, "passport_image" + index, imageUri, boundary);
                    }
                }

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
                            Bundle bundle = new Bundle();
                            bundle.putString("type_tr_name", type_tr_name);
                            bundle.putString("tr_status", "قيد المعالجة");
                            bundle.putString("number_passenger", passengersTextView.getText().toString().trim());
                            bundle.putString("type_icon", String.valueOf(icon_tr));
                            bundle.putInt("number_status", 1);
                            bundle.putString("notes", notes.getText().toString().trim());
                            bundle.putString("country", country.getText().toString().trim());

                            for (int i = 0; i < namesContainer.getChildCount(); i++) {
                                MaterialCardView mainCard = (MaterialCardView) namesContainer.getChildAt(i);
                                LinearLayout vLayout = (LinearLayout) mainCard.getChildAt(0);
                                LinearLayout nameRow = (LinearLayout) vLayout.getChildAt(0);
                                EditText nameField = (EditText) nameRow.getChildAt(0);

                                String key = "name_passenger" + (i + 1);
                                String value = nameField.getText().toString().trim();

                                bundle.putString(key, value);
                            }

                            Fragment fragment = new TravelerRequestsDetails();


                            fragment.setArguments(bundle);

                            if (getActivity() instanceof HomePage) {
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                ((HomePage) getContext()).openFullScreenFragment(fragment, "تفاصيل الحجز", R.drawable.checklist, 2);
                            }

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

                    } else {
                        UserUtils.sendLog(getContext(), "sendTravelerRequest", response, s, "AddTravelerRequests");
                        UserUtils.showErrorDialog(getActivity(), UserUtils.getMessageFromLocalNew(141, dbHelper), null, null,
                                "تعذر إتمام الطلب", 1,null);

                    }
                });
                UserUtils.hideSuccessGif(getActivity());

            } catch (Exception e) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.hideSuccessGif(getActivity());
                        UserUtils.sendLog(getContext(), "sendTravelerRequest", String.valueOf(e), e.getMessage(), "AddTravelerRequests");
                        UserUtils.showErrorDialog(getActivity(), UserUtils.getMessageFromLocalNew(5, dbHelper), null,
                                null, "تعذر إتمام الطلب", 1,null);

                    });
                }
            }
        }).start();
    }

    private void writeFileField(DataOutputStream out, String fieldName, Uri fileUri, String boundary) throws IOException {

        String originalName = getFileNameFromUri(fileUri);
        String uniqueID = UUID.randomUUID().toString();
        String fileName = uniqueID + "_" + originalName;

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        out.writeBytes("Content-Type: image/jpeg\r\n");
        out.writeBytes("\r\n");

        InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);

        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);

        inputStream.close();

        if (bitmap != null) {

            int maxWidth = 1280;
            int maxHeight = 1280;

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float ratio = Math.min(
                    (float) maxWidth / width,
                    (float) maxHeight / height
            );

            if (ratio < 1) {
                width = Math.round(width * ratio);
                height = Math.round(height * ratio);

                bitmap = android.graphics.Bitmap.createScaledBitmap(
                        bitmap,
                        width,
                        height,
                        true
                );
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, baos);

            out.write(baos.toByteArray());

            baos.close();
            bitmap.recycle();
        }

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
