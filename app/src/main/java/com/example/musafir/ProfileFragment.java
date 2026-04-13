package com.example.musafir;

import static android.app.Activity.RESULT_OK;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

//import jp.wasabeef.blurry.Blurry;

public class ProfileFragment extends Fragment {
    View view;
    RadioGroup genderRadioGroup;
    MaterialCardView radioMale, radioFemale;
    EditText dateOfBirthEditText;
    EditText licenseExpireDateEditText;
    ImageView licenseImageView, nationalIdImageView, passportImage;
    private static final int PICK_NATIONAL_ID_IMAGE = 1;
    private static final int PICK_LICENSE_IMAGE = 2;
    private static final int PASSPORT_IMAGE = 3;

    Uri nationalIdImageUri = null;
    Uri licenseImageUri = null;
    Uri passportImageUri = null;

    EditText driverLicenseEditText;
    EditText nationalIdEditText;
    View driverFieldsContainer;
    String BASE_URL = UserUtils.BASE_URL;
    String ImageUrl = UserUtils.ImageUrl;
    //    TextView placeholderText,placeholderText2;
    LinearLayout frame_img2, frame_img, passengerFieldsContainer, frame_img_passport;
    ProgressBar progressnationalImage, progressLicenseImage, progresspassportImage;
    TextView fullNameError, phoneError;
    View firstErrorView = null;
    ScrollView scrollViewcon;

    public ProfileFragment() {
    }


    private boolean isDataChanged = false;
    EditText fullName, phoneNumber, address;
    String oldFullName, oldPhone, oldNationalId, oldLicense, oldAddress;
    String oldNationalImagePath, oldLicenseImagePath, oldPassportImagePath;

    private boolean isLoadingData = false;
    TextView textMan, textWoman;

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
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);
        progressLicenseImage = view.findViewById(R.id.progressLicenseImage);
        progressnationalImage = view.findViewById(R.id.progressNationalImage);
        frame_img2 = view.findViewById(R.id.frame_img2);
        frame_img = view.findViewById(R.id.frame_img);
        frame_img_passport = view.findViewById(R.id.frame_img_passport);
        driverLicenseEditText = view.findViewById(R.id.driverLicense);
        nationalIdEditText = view.findViewById(R.id.nationalId);
        nationalIdImageView = view.findViewById(R.id.nationalIdImage);
        licenseImageView = view.findViewById(R.id.licenseImage);
        driverFieldsContainer = view.findViewById(R.id.driverFieldsContainer);
        address = view.findViewById(R.id.address);
        progresspassportImage = view.findViewById(R.id.progresspassportImage);
        passportImage = view.findViewById(R.id.passportImage);

        fullNameError = view.findViewById(R.id.fullNameError);
        phoneError = view.findViewById(R.id.phoneError);
        passengerFieldsContainer = view.findViewById(R.id.passengerFieldsContainer);
        textWoman = view.findViewById(R.id.textWoman);
        textMan = view.findViewById(R.id.textMan);
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup);

        dateOfBirthEditText = view.findViewById(R.id.dateOfBirth);
        dateOfBirthEditText.setOnClickListener(v -> showDatePickerDialog(dateOfBirthEditText));
        if (driverFieldsContainer != null) driverFieldsContainer.setVisibility(View.GONE);
        if (passengerFieldsContainer != null) passengerFieldsContainer.setVisibility(View.GONE);

        radioMale = view.findViewById(R.id.radioMale);
        radioFemale = view.findViewById(R.id.radioFemale);
        ImageView iconPassenger = view.findViewById(R.id.iconPassenger);
        ImageView iconDriver = view.findViewById(R.id.iconDriver);
        fullName = view.findViewById(R.id.fullName);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String savedRole = prefs.getString("selected_role", "male");
        oldFullName = prefs.getString("full_name", "");
        oldPhone = prefs.getString("phone_number", "");
        oldNationalId = prefs.getString("national_id", "");
        oldLicense = prefs.getString("driver_license", "");
        oldAddress = prefs.getString("address", "");
        oldNationalImagePath = prefs.getString("national_id_image", "");
        oldLicenseImagePath = prefs.getString("license_image", "");
        oldPassportImagePath = prefs.getString("passport_image", "");

        fullName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String trimmed = fullName.getText().toString().trim();
                fullName.setText(trimmed);
            }
        });

        scrollViewcon = view.findViewById(R.id.scrollViewcon);

        // ====== إضافة علم لتجاهل التغييرات أثناء تحميل البيانات ======
        isLoadingData = true;
        UserUtils.setEditTextState(fullName, false);
        UserUtils.setEditTextState(phoneNumber, false);
        UserUtils.setEditTextState(dateOfBirthEditText, false);
        UserUtils.setEditTextState(nationalIdEditText, false);
        UserUtils.setEditTextState(address, false);
        UserUtils.setEditTextState(driverLicenseEditText, false);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isLoadingData) checkIfDataChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        fullName.addTextChangedListener(watcher);
        phoneNumber.addTextChangedListener(watcher);
        nationalIdEditText.addTextChangedListener(watcher);
        driverLicenseEditText.addTextChangedListener(watcher);
        address.addTextChangedListener(watcher);
        nationalIdImageView.setOnClickListener(v -> {
            checkImagePermission(PICK_NATIONAL_ID_IMAGE);
            isDataChanged = true;
        });

        licenseImageView.setOnClickListener(v -> {
            checkImagePermission(PICK_LICENSE_IMAGE);
            isDataChanged = true;
        });

        passportImage.setOnClickListener(v -> {
            checkImagePermission(PASSPORT_IMAGE);
            isDataChanged = true;
        });

//        nationalIdImageView.setOnClickListener(v -> {
//            selectImageFromGallery(PICK_NATIONAL_ID_IMAGE);
//            isDataChanged = true;
//        });
//        licenseImageView.setOnClickListener(v -> {
//            selectImageFromGallery(PICK_LICENSE_IMAGE);
//            isDataChanged = true;
//        });
//        passportImage.setOnClickListener(v -> {
//            selectImageFromGallery(PASSPORT_IMAGE);
//            isDataChanged = true;
//        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (isDataChanged) {
                            showExitConfirmationDialog(getContext());
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                });

        view.findViewById(R.id.editProfileButton).setOnClickListener(v -> validateAndSendData());

        if ("female".equals(savedRole)) {
            selectFemale(radioMale, radioFemale, iconPassenger, iconDriver, textMan, textWoman);
        } else {
            selectMale(radioMale, radioFemale, iconPassenger, iconDriver, textMan, textWoman);
        }

        radioMale.setOnClickListener(v ->
                selectMale(radioMale, radioFemale, iconPassenger, iconDriver, textMan, textWoman));

        radioFemale.setOnClickListener(v ->
                selectFemale(radioMale, radioFemale, iconPassenger, iconDriver, textMan, textWoman));

        loadCachedProfileData();

        // بعد انتهاء تحميل البيانات، إيقاف علم التحميل
        isLoadingData = false;
        isDataChanged = false; // لا يعتبر أي شيء تم تغييره بعد التحميل

        return view;
    }


    private String newNationalImagePath = null;
    private String newLicenseImagePath = null;
    private String passportImagePath = null;

    private void checkIfDataChanged() {
        boolean fullNameChanged = !fullName.getText().toString().trim().equals(oldFullName);
        boolean phoneChanged = !phoneNumber.getText().toString().trim().equals(oldPhone);
        boolean nationalIdChanged = !nationalIdEditText.getText().toString().trim().equals(oldNationalId);
        boolean licenseChanged = !driverLicenseEditText.getText().toString().trim().equals(oldLicense);
        boolean addressChanged = !address.getText().toString().trim().equals(oldAddress);

        boolean nationalImageChanged = newNationalImagePath != null && !newNationalImagePath.equals(oldNationalImagePath);
        boolean licenseImageChanged = newLicenseImagePath != null && !newLicenseImagePath.equals(oldLicenseImagePath);
        boolean passportImageChanged = passportImagePath != null && !passportImagePath.equals(oldPassportImagePath);

        isDataChanged = fullNameChanged || phoneChanged
                || nationalIdChanged || licenseChanged || addressChanged
                || nationalImageChanged || licenseImageChanged || passportImageChanged;
    }

    private void loadCachedProfileData() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        String licenseImageUrl = prefs.getString("license_image", "");
        String passportImageUrl = prefs.getString("passport_image", "");
        String nationalIdImageUrl = prefs.getString("national_id_image", "");
        String userType = prefs.getString("user_type", "");

        if (userType.equalsIgnoreCase("driver")) {
            if (driverFieldsContainer != null) driverFieldsContainer.setVisibility(View.VISIBLE);
        }
        if (userType.equalsIgnoreCase("passenger")) {
            if (passengerFieldsContainer != null)
                passengerFieldsContainer.setVisibility(View.VISIBLE);
        }

        String baseUrl = ImageUrl;

        if (licenseImageUri != null) {
            // إذا اختار المستخدم صورة جديدة، اعرضها مباشرة
            licenseImageView.setImageURI(licenseImageUri);
            licenseImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            frame_img2.setVisibility(View.GONE);
            progressLicenseImage.setVisibility(View.GONE);
        } else if (!licenseImageUrl.isEmpty()) {
            progressLicenseImage.setVisibility(View.VISIBLE);

            Glide.with(requireContext())
                    .load(baseUrl + licenseImageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, @androidx.annotation.Nullable Object model, @NonNull com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                            progressLicenseImage.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, com.bumptech.glide.request.target.Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            progressLicenseImage.setVisibility(View.GONE);
                            frame_img2.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .error(R.drawable.baseline_image_24)
                    .into(licenseImageView);

            frame_img2.setVisibility(View.GONE);
        }

        // ---- الهوية الوطنية ----
        if (nationalIdImageUri != null) {
            nationalIdImageView.setImageURI(nationalIdImageUri);
            nationalIdImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            frame_img.setVisibility(View.GONE);
            progressnationalImage.setVisibility(View.GONE);
        } else if (!nationalIdImageUrl.isEmpty()) {
            progressnationalImage.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(baseUrl + nationalIdImageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, @androidx.annotation.Nullable Object model, @NonNull com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                            progressnationalImage.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, com.bumptech.glide.request.target.Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            progressnationalImage.setVisibility(View.GONE);
                            frame_img.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .error(R.drawable.baseline_image_24)
                    .into(nationalIdImageView);

            frame_img.setVisibility(View.GONE);
        }

        if (passportImageUri != null) {
            passportImage.setImageURI(passportImageUri);
            passportImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

            frame_img_passport.setVisibility(View.GONE);
            progresspassportImage.setVisibility(View.GONE);
        } else if (!passportImageUrl.isEmpty()) {
            progresspassportImage.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(baseUrl + passportImageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, @androidx.annotation.Nullable Object model, @NonNull com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                            progresspassportImage.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, com.bumptech.glide.request.target.Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            progresspassportImage.setVisibility(View.GONE);
                            frame_img_passport.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .error(R.drawable.baseline_image_24)
                    .into(passportImage);

            frame_img_passport.setVisibility(View.GONE);
        }
        // ---- باقي البيانات ----
        setProfileField(R.id.fullName, "الاسم الكامل", prefs.getString("full_name", ""));
        setProfileField(R.id.phoneNumber, "رقم الهاتف", prefs.getString("phone_number", ""));
        setProfileField(R.id.nationalId, "رقم الهوية الوطنية", prefs.getString("national_id", ""));
        setProfileField(R.id.dateOfBirth, "تاريخ الميلاد", prefs.getString("date_of_birth", ""));
//        setProfileField(R.id.licenseExpireDate, "تاريخ انتهاء الرخصة", prefs.getString("license_expire_date", ""));

        String gender = prefs.getString("gender", "");
        if (gender.equalsIgnoreCase("ذكر") || gender.equalsIgnoreCase("male")) {
            radioMale.setChecked(true);
        } else if (gender.equalsIgnoreCase("أنثى") || gender.equalsIgnoreCase("female")) {
            radioFemale.setChecked(true);
        } else {
            genderRadioGroup.clearCheck();
        }

        setProfileField(R.id.address, "العنوان", prefs.getString("address", ""));
        setProfileField(R.id.driverLicense, "رخصة القيادة", prefs.getString("driver_license", ""));
        isDataChanged = false; // بعد تحميل البيانات، لا يوجد تغيير
        isLoadingData = false;
    }

    private void validateAndSendData() {
        firstErrorView = null;
        // تحقق من المسار
        boolean valid = true;

        if (fullName.getText().toString().trim().isEmpty()) {
            fullNameError.setVisibility(View.VISIBLE);
            fullNameError.setText("يرجى إدخال الاسم الكامل");
            fullName.setError("يرجى إدخال الاسم الكامل");
            UserUtils.setEditTextState(fullName, true);
            valid = false;
            if (firstErrorView == null) firstErrorView = fullNameError;
        } else {
            fullNameError.setVisibility(View.GONE);
            UserUtils.setEditTextState(fullName, false);

        }
        if (phoneNumber.getText().toString().trim().isEmpty()) {
            phoneError.setVisibility(View.VISIBLE);
            phoneError.setText("يرجى إدخال رقم الهاتف");
            phoneNumber.setError("يرجى إدخال رقم الهاتف");
            UserUtils.setEditTextState(phoneNumber, true);
            valid = false;
            if (firstErrorView == null) firstErrorView = phoneError;

        } else if (phoneNumber.length() != 9) {
            phoneError.setVisibility(View.VISIBLE);
            phoneError.setText("يجب أن يتكون رقم الهاتف من 9 أرقام");
            phoneNumber.setError("يجب أن يتكون رقم الهاتف من 9 أرقام");
            UserUtils.setEditTextState(phoneNumber, true);
            valid = false;
            if (firstErrorView == null) firstErrorView = phoneError;

        } else if (!phoneNumber.getText().toString().trim().matches("7[013789]\\d{7}")) {
            phoneError.setVisibility(View.VISIBLE);
            phoneError.setText("يرجى إدخال رقم صحيح يبدأ بـ 7");
            phoneNumber.setError("يرجى إدخال رقم صحيح يبدأ بـ 7");
            UserUtils.setEditTextState(phoneNumber, true);
            valid = false;
            if (firstErrorView == null) firstErrorView = phoneError;
        } else {
            phoneError.setVisibility(View.GONE);
            UserUtils.setEditTextState(phoneNumber, false);

        }


        if (firstErrorView != null) {
            scrollViewcon.post(() -> scrollViewcon.smoothScrollTo(0, firstErrorView.getTop()));
        }
        if (valid) {

            updateProfile();
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

        // إعداد الضبابية
        ViewGroup decorView = requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);
//        Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
//        exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));

        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }

        if (isAdded() && !getActivity().isFinishing()) {
            exitDialog.show();
        }
    }

    private void selectMale(MaterialCardView maleCard, MaterialCardView femaleCard,
                            ImageView iconMale, ImageView iconFemale, TextView textMan, TextView textWoman) {
//        maleCard.setStrokeColor(ContextCompat.getColor(getContext(), R.color.primary));
        maleCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary3));
//        iconMale.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_logo));
        iconMale.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary2));
        textMan.setTextColor(ContextCompat.getColor(getContext(), R.color.primary2));
        textWoman.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

        femaleCard.setStrokeColor(ContextCompat.getColor(getContext(), R.color.gray));
        femaleCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray));
        iconFemale.setBackground(null);
        iconFemale.setColorFilter(Color.parseColor("#666666"));

        femaleCard.setTag("unselected");
        maleCard.setTag("selected");
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("selected_role", "male").apply();
    }

    private void selectFemale(MaterialCardView maleCard, MaterialCardView femaleCard,
                              ImageView iconMale, ImageView iconFemale, TextView textMan, TextView textWoman) {
//        femaleCard.setStrokeColor(ContextCompat.getColor(getContext(), R.color.primary));
        femaleCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary3));
//        iconFemale.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_logo));
        iconFemale.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary2));
        textMan.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        textWoman.setTextColor(ContextCompat.getColor(getContext(), R.color.primary2));

        maleCard.setStrokeColor(ContextCompat.getColor(getContext(), R.color.gray));
        maleCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray));
        iconMale.setBackground(null);
        iconMale.setColorFilter(Color.parseColor("#666666"));

        maleCard.setTag("unselected");
        femaleCard.setTag("selected");
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("selected_role", "female").apply();
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
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (requestCode == PICK_NATIONAL_ID_IMAGE) {
                nationalIdImageUri = imageUri;
                nationalIdImageView.setImageURI(imageUri);
                frame_img.setVisibility(View.GONE);
                newNationalImagePath = imageUri.toString();
            } else if (requestCode == PICK_LICENSE_IMAGE) {
                licenseImageUri = imageUri;
                licenseImageView.setImageURI(imageUri);
                frame_img2.setVisibility(View.GONE);
                newLicenseImagePath = imageUri.toString();
            } else if (requestCode == PASSPORT_IMAGE) {
                passportImageUri = imageUri;
                passportImage.setImageURI(imageUri);
                frame_img_passport.setVisibility(View.GONE);
                passportImagePath = imageUri.toString();
            }
            checkIfDataChanged();
        }
    }


    private void updateProfile() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");
        DBHelper dbHelper = new DBHelper(getContext());

        if (token.isEmpty()) {
//            UserUtils.getMessageFromLocal(41, dbHelper, new UserUtils.MessageCallback() {
//                @Override
//                public void onSuccess(String message) {
//                    UserUtils.ToastMessages(getActivity(), message);
//                }
//
//                @Override
//                public void onError(String error) {
//                }
//
//            });
            return;
        }

        String fullName = ((EditText) view.findViewById(R.id.fullName)).getText().toString().trim();
        String phoneNumber = ((EditText) view.findViewById(R.id.phoneNumber)).getText().toString().trim();
        String nationalId = ((EditText) view.findViewById(R.id.nationalId)).getText().toString().trim();
        String dateOfBirth = ((EditText) view.findViewById(R.id.dateOfBirth)).getText().toString().trim();

        String gender = prefs.getString("selected_role", "male");

        String address = ((EditText) view.findViewById(R.id.address)).getText().toString().trim();
        String driverLicense = ((EditText) view.findViewById(R.id.driverLicense)).getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("جاري تحديث البيانات...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String finalGender = gender;
        new Thread(() -> {
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "auth/profile/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setDoOutput(true);
                conn.setDoInput(true);


                String boundary = "===" + System.currentTimeMillis() + "===";
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                DataOutputStream request = new DataOutputStream(conn.getOutputStream());

                // إرسال الحقول النصية
                if (!fullName.isEmpty()) writeFormField(request, "full_name", fullName, boundary);
                if (!phoneNumber.isEmpty())
                    writeFormField(request, "phone_number", phoneNumber, boundary);
                if (!nationalId.isEmpty())
                    writeFormField(request, "national_id", nationalId, boundary);
                if (!dateOfBirth.isEmpty())
                    writeFormField(request, "date_of_birth", dateOfBirth, boundary);
                if (!finalGender.isEmpty())
                    writeFormField(request, "gender", finalGender, boundary);
                if (!address.isEmpty()) writeFormField(request, "address", address, boundary);
                if (!driverLicense.isEmpty())
                    writeFormField(request, "driver_license", driverLicense, boundary);
                if (!token.isEmpty()) writeFormField(request, "token", token, boundary);
                String s = "{ full_name: " + fullName +
                        " , phoneNumber: " + phoneNumber +
                        " , nationalId: " + nationalId +
                        " , dateOfBirth: " + dateOfBirth +
                        " , finalGender: " + finalGender +
                        " , address: " + address +
                        " , driverLicense: " + driverLicense +
                        " , token: " + token +
                        " }";
                if (licenseImageUri != null)
                    writeFileField(request, "license_image", licenseImageUri, boundary);

                if (nationalIdImageUri != null)
                    writeFileField(request, "national_id_image", nationalIdImageUri, boundary);

                if (passportImageUri != null)
                    writeFileField(request, "passport_image", passportImageUri, boundary);

                request.writeBytes("--" + boundary + "--\r\n");
                request.flush();
                request.close();

                int responseCode = conn.getResponseCode();

                InputStream inputStream = (responseCode >= 200 && responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();

                    try {
                        JSONObject json = new JSONObject(result.toString());
                        if (responseCode == 200) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("full_name", json.optString("full_name", ""));
                            editor.putString("phone_number", json.optString("phone_number", ""));
                            editor.putString("national_id", json.optString("national_id", ""));
                            String dateOfBirthResp = json.optString("date_of_birth", "");
                            if ("null".equalsIgnoreCase(dateOfBirthResp)) dateOfBirthResp = "";
                            editor.putString("date_of_birth", dateOfBirthResp);
                            editor.putString("gender", json.optString("gender", ""));
                            editor.putString("address", json.optString("address", ""));
                            editor.putString("driver_license", json.optString("driver_license", ""));
                            editor.putString("passport_image", json.optString("passport_image", ""));
                            editor.putString("token", json.optString(token, ""));
                            editor.apply();
                            UserUtils.getMessageFromLocal(61, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }

                            });
                            Toolbar toolbar = requireActivity().findViewById(R.id.main_toolbar);
                            toolbar.setNavigationIcon(null);
                            SharedPreferences prefss = SharedPrefsHelper.get(getContext());

//                            SharedPreferences preferences = requireActivity().getSharedPreferences("MyAppPrefs", requireActivity().MODE_PRIVATE);
                            String full_name = prefss.getString("full_name", "");
                            String fullNames = full_name.trim();
                            String firstName = "";
                            if (!fullNames.isEmpty()) {
                                String[] parts = fullNames.split("\\s+");
                                if (parts.length > 0) {
                                    firstName = parts[0];
                                    if (!firstName.isEmpty()) {
                                        firstName = firstName.substring(0, 1).toUpperCase() +
                                                firstName.substring(1).toLowerCase();
                                    }
                                }
                            }

                            // int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
                            // String greeting = (hour >= 5 && hour < 12) ? "صباح الخير" : "مساء الخير";

                            // String fullText = greeting + " " + firstName;

                            // SpannableString spannable = new SpannableString(fullText);
                            // int start = greeting.length();
                            // int end = fullText.length();

                            // spannable.setSpan(new RelativeSizeSpan(1.2f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            // spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // toolbar.setTitle(spannable);
                            // toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);

                            isDataChanged = false;
                            oldFullName = fullName;
                            oldPhone = phoneNumber;
                            oldNationalId = nationalIdEditText.getText().toString().trim();
                            oldLicense = driverLicenseEditText.getText().toString().trim();
                            oldAddress = address;
                            oldNationalImagePath = newNationalImagePath != null ? newNationalImagePath : oldNationalImagePath;
                            oldLicenseImagePath = newLicenseImagePath != null ? newLicenseImagePath : oldLicenseImagePath;
                            oldPassportImagePath = passportImagePath != null ? passportImagePath : oldPassportImagePath;

                            loadCachedProfileData();
                        } else {
                            UserUtils.getMessageFromLocal(62, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }

                            });
                            UserUtils.sendLog(getContext(), "updateProfile", result.toString(), s, "Profile Fragment");
                        }
                    } catch (Exception e) {
                        UserUtils.sendLog(getContext(), "updateProfile", e.toString(), s, "Profile Fragment");
                        UserUtils.getMessageFromLocal(62, dbHelper, new UserUtils.MessageCallback() {
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
                UserUtils.sendLog(getContext(), "updateProfile", e.toString(), e.toString(), "Profile Fragment");
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    UserUtils.getMessageFromLocal(63, dbHelper, new UserUtils.MessageCallback() {
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

    private void writeFormField(DataOutputStream request, String name, String value, String boundary) throws Exception {
        request.writeBytes("--" + boundary + "\r\n");
        request.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
        request.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n");
        request.writeBytes("\r\n");
        request.write(value.getBytes(StandardCharsets.UTF_8));
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
            // إذا لم يكن من نوع content، نأخذ الاسم من آخر جزء في المسار
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

    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();

        // نحسب الحد الأقصى (اليوم - 16 سنة)
        Calendar maxDateCalendar = Calendar.getInstance();
        maxDateCalendar.add(Calendar.YEAR, -16);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    // ضبط التاريخ على الكائن Calendar
                    calendar.set(year, month, dayOfMonth);

                    // تنسيق التاريخ باللغة الإنجليزية
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    String formattedDate = sdf.format(calendar.getTime());

                    // عرض التاريخ في EditText
                    targetEditText.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // تحديد أقصى تاريخ = اليوم - 16 سنة
        datePickerDialog.getDatePicker().setMaxDate(maxDateCalendar.getTimeInMillis());

        datePickerDialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();

        loadProfileData();
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }


    private void loadProfileData() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");
        DBHelper dbHelper = new DBHelper(getContext());

        if (token.isEmpty()) {
//            UserUtils.getMessageFromLocal(41, dbHelper, new UserUtils.MessageCallback() {
//                @Override
//                public void onSuccess(String message) {
//                    UserUtils.ToastMessages(getActivity(), message);
//                }
//
//                @Override
//                public void onError(String error) {
//                }
//
//            });
            return;
        }

        new Thread(() -> {
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "auth/profile/?device_id=" + deviceId + "&device_info=" + deviceInfo);                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                JSONObject body = new JSONObject();
                body.put("token", token);
                conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
                conn.connect();

                int responseCode = conn.getResponseCode();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream()));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                if (responseCode == 200) {
                    JSONObject json = new JSONObject(result.toString());

                    // ✅ تخزين البيانات محليًا
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("user_type", json.optString("user_type", ""));
                    editor.putString("full_name", json.optString("full_name", ""));
                    editor.putString("phone_number", json.optString("phone_number", ""));
                    editor.putString("national_id", json.optString("national_id", ""));
                    editor.putString("date_of_birth", json.optString("date_of_birth", "").equals("null") ? "" : json.optString("date_of_birth", ""));
                    editor.putString("gender", json.optString("gender", ""));
                    editor.putString("address", json.optString("address", ""));
                    editor.putString("driver_license", json.optString("driver_license", ""));
                    editor.putString("license_expire_date", json.optString("license_expire_date", "").equals("null") ? "" : json.optString("date_of_birth", ""));
                    editor.putString("license_image", json.optString("license_image", "").equals("null") ? "" : json.optString("license_image", ""));
                    editor.putString("national_id_image", json.optString("national_id_image", "").equals("null") ? "" : json.optString("national_id_image", ""));
                    editor.putString("passport_image", json.optString("passport_image", "").equals("null") ? "" : json.optString("passport_image", ""));
                    editor.putString("visit_document", json.optString("visit_document", "").equals("null") ? "" : json.optString("visit_document", ""));
                    editor.apply();
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            loadCachedProfileData();
                        });
                    }
                } else {
                    String errorMsg = result.toString();
                    try {
                        JSONObject errorJson = new JSONObject(errorMsg);
                        if (errorJson.has("message"))
                            errorMsg = errorJson.getString("message");
                        else if (errorJson.has("detail"))
                            errorMsg = errorJson.getString("detail");
                    } catch (Exception e) {
                        UserUtils.sendLog(getContext(), "loadProfileData", e.toString(), e.toString(), "Profile Fragment");
                    }

                    final String displayMsg = errorMsg.length() > 100 ? errorMsg.substring(0, 100) + "..." : errorMsg;
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }

                            });
                            UserUtils.sendLog(getContext(), "loadProfileData", displayMsg, displayMsg, "Profile Fragment");
                        });
                    }
                }
            } catch (Exception e) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.sendLog(getContext(), "loadProfileData", e.toString(), e.toString(), "Profile Fragment");
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

    private void setProfileField(int viewId, String label, String value) {
        EditText editText = view.findViewById(viewId);
        editText.setHint(label);
        editText.setText(value);
    }
}
