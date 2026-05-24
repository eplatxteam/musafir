package com.example.musafir;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

//import jp.wasabeef.blurry.Blurry;


public class SettingFragment extends Fragment {

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false); // ❌ يخفي سهم الرجوع
        }
    }


    LottieAnimationView lottieWave;
    String BASE_URL = UserUtils.BASE_URL;
    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        LinearLayout addVehicleBtn = view.findViewById(R.id.add_vehicle);
//        LinearLayout addTravelBtn = view.findViewById(R.id.add_travel);
        LinearLayout addtripBtn = view.findViewById(R.id.add_trip);
        LinearLayout about = view.findViewById(R.id.about);
        LinearLayout phoneNumber = view.findViewById(R.id.phoneNumber);
        LinearLayout profileBtn = view.findViewById(R.id.profile);
        LinearLayout logout = view.findViewById(R.id.logout);
        LinearLayout guide = view.findViewById(R.id.guide);
//        LinearLayout deleteAccount = view.findViewById(R.id.deleteAccount);
//        TextView shortname = view.findViewById(R.id.shortname);
        TextView appVersion = view.findViewById(R.id.appVersion);
        TextView add_trip_text = view.findViewById(R.id.add_trip_text);
        LinearLayout Favorite = view.findViewById(R.id.Favorite);
        LinearLayout DriverTripRequest = view.findViewById(R.id.DriverTripRequest);
//        View driverFieldsContainer = view.findViewById(R.id.driverFieldsContainer);
        LinearLayout supportHeader = view.findViewById(R.id.supportHeader);
        LinearLayout changepass = view.findViewById(R.id.changepass);
//        LinearLayout supportMenu = view.findViewById(R.id.supportMenu);
//        LinearLayout share = view.findViewById(R.id.share);
        LinearLayout sharing = view.findViewById(R.id.sharing);
        LinearLayout refresh = view.findViewById(R.id.refresh);
        LinearLayout balance = view.findViewById(R.id.balance);
        ImageView arrowIcon = view.findViewById(R.id.arrowIcon);
        View line_vehicle = view.findViewById(R.id.line_vehicle);
        View lineDriverTrip = view.findViewById(R.id.lineDriverTrip);
        View line_notification = view.findViewById(R.id.line_notification);
        View line_balance = view.findViewById(R.id.line_balance);
        LinearLayout btnStartTutorial = view.findViewById(R.id.btnStartTutorial);
        DBHelper dbHelper = new DBHelper(getContext());
        guide.setOnClickListener(v -> {
            ((HomePage) requireActivity()).openFullScreenFragment(new GuideFragment(), "دليل المسافر", R.drawable.solo_traveller, 0);
        });
        btnStartTutorial.setOnClickListener(v -> {
            SharedPreferences appPrefs = getContext().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
            // استخدام commit لضمان الحفظ الفوري
            appPrefs.edit().putBoolean("FORCE_SHOW_TUTORIAL", true).commit();

            if (getActivity() != null) {
                // العودة للرئيسية وتحديد التبويب الأول
                ((HomePage) getActivity()).selectTab(R.id.nav_home);
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        lottieWave = view.findViewById(R.id.lottieWaveRef);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        LinearLayout supportSubMenu = view.findViewById(R.id.supportSubMenu);

        phoneNumber.setOnClickListener(v -> {
            String countryCode = prefs.getString("country_code", "967785050270");

            int messageId = "YE".equals(countryCode) ? 349 : 362;
            String phone = UserUtils.getMessageFromLocalNew(messageId, dbHelper);

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });
        supportHeader.setOnClickListener(v -> {
            if (supportSubMenu.getVisibility() == View.GONE) {
                supportSubMenu.setVisibility(View.VISIBLE);
                arrowIcon.setRotation(-90f);
            } else {
                supportSubMenu.setVisibility(View.GONE);
                arrowIcon.setRotation(0f);
            }
        });

        view.findViewById(R.id.ai_chat_support).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ai_chat.class);
            startActivity(intent);
        });

        view.findViewById(R.id.contact_support).setOnClickListener(v -> {
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
//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        appVersion.append(UserUtils.app_version + ".0524");
//
//        share.setOnClickListener(v -> {
//            SharedPreferences prefsLink = requireActivity().getSharedPreferences("prefsLink", Context.MODE_PRIVATE);
//            SharedPreferences prefs2 = SharedPrefsHelper.get(getContext());
////            SharedPreferences prefs2 = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
//            String linkApp = prefsLink.getString("link_app", "");
//            String user_phone = prefs2.getString("user_phone", "");
//            String playStoreLink = linkApp + "&referrer=invite%3D" + user_phone;
//
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("text/plain");
//            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "تطبيق مسافر");
//            shareIntent.putExtra(Intent.EXTRA_TEXT, "حمّل تطبيق مسافر الآن واحصل على أفضل الرحلات:\n" + playStoreLink);
//
//            startActivity(Intent.createChooser(shareIntent, "دعوة صديق عبر"));
//        });
//        share.setOnClickListener(v -> {
//            SharedPreferences prefsLink = requireActivity().getSharedPreferences("prefsLink", Context.MODE_PRIVATE);
//            SharedPreferences prefs2 = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
//            String linkApp = prefsLink.getString("link_app", "");
//            String user_phone = prefs2.getString("user_phone", "");
////            String inviteCode = user_phone;
//
//            String linkWithCode = linkApp + "&invite=" + user_phone;
////            String linkWithCode =
////            linkApp + "&invite=" + inviteCode;
//
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("text/plain");
//            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "تطبيق مسافر");
//            shareIntent.putExtra(Intent.EXTRA_TEXT, "حمّل التطبيق الآن:\n" + linkWithCode);
//
//            startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق عبر"));
//        });


        refresh.setOnClickListener(v -> {
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
            UserUtils.fetchBalance(getContext());

            UserUtils.checkAppUpdate(getContext());
            UserUtils.app_Pages(getContext());
            UserUtils.fetchAndSavePayTypes(getContext(), new UserUtils.GenericCallback() {

                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onError(String error) {

                }
            });
            UserUtils.fetchAndSavecities(getContext(), new UserUtils.citiesCallback() {
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
            UserUtils.fetchAndSaveCountry(getContext(), new UserUtils.FetchCallback() {
                @Override
                public void onSuccess(String message) {
                    prefs.edit().putBoolean("messages_fetched", true).apply();
//                    UserUtils.ToastMessages(getContext(), message);
                }

                @Override
                public void onError(String error) {

                }
            });
            UserUtils.fetchCashBankData(getContext(), dbHelper, new UserUtils.OnCashBankFetchedListener() {
                @Override
                public void onFetched(List<DBHelper.CashBank> types) {
                }

                @Override
                public void onError(String error) {

                }
            });
            UserUtils.syncDayTimesFromServer(getContext(), new UserUtils.DayTimeCallback() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(String error) {

                }
            });
            UserUtils.fetchAndSaveMessages(getContext(), new UserUtils.FetchCallback() {
                @Override
                public void onSuccess(String message) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("messages_fetched", true);
                    editor.apply();
                    UserUtils.ToastMessages(getActivity(), message);
                    lottieWave.cancelAnimation();
                    lottieWave.setProgress(0f); // مهم
                    lottieWave.setVisibility(View.GONE);
                }

                @Override
                public void onError(String error) {
                    lottieWave.cancelAnimation();
                    lottieWave.setProgress(0f); // مهم
                    lottieWave.setVisibility(View.GONE);


                }


            });
            UserUtils.fetchServiceHome(getContext(), dbHelper, new PageHome.OnServiceHomeFetchedListener() {
                @Override
                public void onFetched(List<DBHelper.ServiceHome> types) {

                }

                @Override
                public void onError(String error) {

                }
            });
            UserUtils.fetchTypeTravelerRequests(getContext(), dbHelper, new TravelerRequests.OnTypeRequestsFetchedListener() {
                @Override
                public void onFetched(List<DBHelper.TypeTravelerRequest> types) {

                }

                @Override
                public void onError(String error) {

                }
            });
            UserUtils.fetchAndSaveContactInfo(getContext(), dbHelper);

            UserUtils.fetchRoutes(getContext(), new UserUtils.FetchCallback() {
                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onError(String error) {
                }
            });
            UserUtils.loadVehicleTypesToDB(getContext());
        });
        UserUtils.fetchCodeDetails(getContext(), 5, null, new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {

            }
        });

        UserUtils.fetchCompany(getContext(), new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {

            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        ((HomePage) requireActivity()).selectTab(R.id.nav_home);
                    }
                }
        );
        String userType = prefs.getString("user_type", "");

        if (userType.equalsIgnoreCase("driver")) {
            add_trip_text.setText("إضافة رحلة");
            DriverTripRequest.setVisibility(View.VISIBLE);
            addVehicleBtn.setVisibility(View.VISIBLE);
            Favorite.setVisibility(View.VISIBLE);
            line_vehicle.setVisibility(View.VISIBLE);
            lineDriverTrip.setVisibility(View.VISIBLE);
            line_notification.setVisibility(View.VISIBLE);
            line_balance.setVisibility(View.GONE);
            balance.setVisibility(View.GONE);
        } else {
            add_trip_text.setText("طلب رحلة خاصة");
            DriverTripRequest.setVisibility(View.GONE);
            Favorite.setVisibility(View.GONE);
            addVehicleBtn.setVisibility(View.GONE);
            line_vehicle.setVisibility(View.GONE);
            lineDriverTrip.setVisibility(View.GONE);
            line_notification.setVisibility(View.GONE);
            line_balance.setVisibility(View.VISIBLE);
            balance.setVisibility(View.VISIBLE);
        }

        addVehicleBtn.setOnClickListener(v -> {
            UserUtils.app_Page(getContext(), 5);
            ((HomePage) requireActivity()).openFullScreenFragment(new VehicleFragment(), "ييانات المركبات", R.drawable.local, 0);
//            openFullScreenFragment(new VehicleFragment(), "ييانات المركبات", R.drawable.local, 0);
        });

        balance.setOnClickListener(v -> {
            UserUtils.app_Page(getContext(), 121);
            ((HomePage) requireActivity()).openFullScreenFragment(new BalanceFragment(), "رصيدي", R.drawable.wallet, 0);

//            openFullScreenFragment(new BalanceFragment(), "رصيدي", R.drawable.wallet, 0);
        });

        sharing.setOnClickListener(v -> {
            UserUtils.app_Page(getContext(), 5);
            ((HomePage) requireActivity()).openFullScreenFragment(new SharingFragment(), "الأعضاء المنضمون", R.drawable.frame, 0);

//            openFullScreenFragment(new SharingFragment(), "الأعضاء المنضمون", R.drawable.frame, 0);
        });

        Favorite.setOnClickListener(v -> {
            UserUtils.app_Page(getContext(), 7);
            ((HomePage) requireActivity()).openFullScreenFragment(new Setting_Notification(), "إدارة الإشعارات", R.drawable.notification_new, 0);

//            openFullScreenFragment(new Setting_Notification(), "إدارة الإشعارات", R.drawable.notification_new, 0);

        });
        changepass.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater2 = getLayoutInflater();
            View dialogView = inflater2.inflate(R.layout.dialog_change_password, null);
            builder.setView(dialogView);

            ViewGroup decorView = (ViewGroup) getActivity().getWindow().getDecorView();

            AlertDialog exitDialog = builder.create();

            if (exitDialog.getWindow() != null) {
                exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            exitDialog.setOnShowListener(d -> {
                Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
            });

            exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));

            exitDialog.show();

            if (exitDialog.getWindow() != null) {
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.80);
                exitDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }

            EditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
            EditText etNew = dialogView.findViewById(R.id.etNewPassword);
            EditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);
            Button btnChange = dialogView.findViewById(R.id.btnChange);
            RelativeLayout dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);

            ImageView ivToggleCurrent = dialogView.findViewById(R.id.ivToggleCurrent);
            ImageView ivToggleNew = dialogView.findViewById(R.id.ivToggleNew);
            ImageView ivToggleConfirm = dialogView.findViewById(R.id.ivToggleConfirm);

            etCurrent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 6) {
                        etNew.requestFocus(); // الانتقال للحقل الجديد
                    }
                }
            });

            etNew.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 6) {
                        etConfirm.requestFocus(); // الانتقال لحقل التأكيد
                    }
                }
            });
            // ---------------------------------------------------

            dialogCancelButton.setOnClickListener(view1 -> exitDialog.dismiss());

            ivToggleCurrent.setOnClickListener(v1 -> {
                if (isCurrentVisible) {
                    etCurrent.setTransformationMethod(new PasswordTransformationMethod());
                    ivToggleCurrent.setImageResource(R.drawable.baseline_visibility_off_24);
                } else {
                    etCurrent.setTransformationMethod(new HideReturnsTransformationMethod());
                    ivToggleCurrent.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }
                isCurrentVisible = !isCurrentVisible;
                etCurrent.setSelection(etCurrent.getText().length());
            });

            ivToggleNew.setOnClickListener(v2 -> {
                if (isNewVisible) {
                    etNew.setTransformationMethod(new PasswordTransformationMethod());
                    ivToggleNew.setImageResource(R.drawable.baseline_visibility_off_24);
                } else {
                    etNew.setTransformationMethod(new HideReturnsTransformationMethod());
                    ivToggleNew.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }
                isNewVisible = !isNewVisible;
                etNew.setSelection(etNew.getText().length());
            });

            ivToggleConfirm.setOnClickListener(v3 -> {
                if (isConfirmVisible) {
                    etConfirm.setTransformationMethod(new PasswordTransformationMethod());
                    ivToggleConfirm.setImageResource(R.drawable.baseline_visibility_off_24);
                } else {
                    etConfirm.setTransformationMethod(new HideReturnsTransformationMethod());
                    ivToggleConfirm.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }
                isConfirmVisible = !isConfirmVisible;
                etConfirm.setSelection(etConfirm.getText().length());
            });

            btnChange.setOnClickListener(v1 -> {
                String current = etCurrent.getText().toString().trim();
                String newPass = etNew.getText().toString().trim();
                String confirm = etConfirm.getText().toString().trim();

                String savedPassword = prefs.getString("password", "");
                if (current.isEmpty()) {
                    etCurrent.setError("الرجاء إدخال كلمة المرور الحالية");
                    return;
                } else if (current.length() < 6) {
                    etCurrent.setError("كلمة المرور يجب أن لا تقل عن 6 خانات");
                    return;
                }

                if (newPass.isEmpty()) {
                    etNew.setError("الرجاء إدخال كلمة المرور الجديدة");
                    return;
                } else if (newPass.length() < 6) {
                    etNew.setError("كلمة المرور الجديدة يجب أن لا تقل عن 6 خانات");
                    return;
                }

                if (confirm.isEmpty()) {
                    etConfirm.setError("الرجاء تأكيد كلمة المرور الجديدة");
                    return;
                } else if (confirm.length() < 6) {
                    etConfirm.setError("تأكيد كلمة المرور يجب أن لا تقل عن 6 خانات");
                    return;
                }

                if (!current.equals(savedPassword)) {
                    UserUtils.getMessageFromLocal(165, dbHelper, new UserUtils.MessageCallback() {
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

                if (current.equals(newPass)) {
                    UserUtils.getMessageFromLocal(166, dbHelper, new UserUtils.MessageCallback() {
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

                if (!newPass.equals(confirm)) {
                    UserUtils.getMessageFromLocal(167, dbHelper, new UserUtils.MessageCallback() {
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

                sendPasswordToServer(newPass);
                exitDialog.dismiss();
            });
        });
//        changepass.setOnClickListener(v -> {
//            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//            LayoutInflater inflater2 = getLayoutInflater();
//            View dialogView = inflater2.inflate(R.layout.dialog_change_password, null);
//            builder.setView(dialogView);
//            ViewGroup decorView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
//
//
//            exitDialog = builder.create();
//
//            Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
//            exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));
//
//            if (exitDialog.getWindow() != null) {
//                exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
//            }
//            exitDialog.show();
//            EditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
//            EditText etNew = dialogView.findViewById(R.id.etNewPassword);
//            EditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);
//            Button btnChange = dialogView.findViewById(R.id.btnChange);
//            RelativeLayout dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);
//
//            ImageView ivToggleCurrent = dialogView.findViewById(R.id.ivToggleCurrent);
//            ImageView ivToggleNew = dialogView.findViewById(R.id.ivToggleNew);
//            ImageView ivToggleConfirm = dialogView.findViewById(R.id.ivToggleConfirm);
//            dialogCancelButton.setOnClickListener(view1 -> exitDialog.dismiss());
//            ivToggleCurrent.setOnClickListener(v1 -> {
//                if (isCurrentVisible) {
//                    etCurrent.setTransformationMethod(new PasswordTransformationMethod());
//                    ivToggleCurrent.setImageResource(R.drawable.baseline_visibility_off_24);
//                } else {
//                    etCurrent.setTransformationMethod(new HideReturnsTransformationMethod());
//                    ivToggleCurrent.setImageResource(R.drawable.baseline_remove_red_eye_24);
//                }
//                isCurrentVisible = !isCurrentVisible;
//                etCurrent.setSelection(etCurrent.getText().length());
//            });
//
//            ivToggleNew.setOnClickListener(v2 -> {
//                if (isNewVisible) {
//                    etNew.setTransformationMethod(new PasswordTransformationMethod());
//                    ivToggleNew.setImageResource(R.drawable.baseline_visibility_off_24);
//                } else {
//                    etNew.setTransformationMethod(new HideReturnsTransformationMethod());
//                    ivToggleNew.setImageResource(R.drawable.baseline_remove_red_eye_24);
//                }
//                isNewVisible = !isNewVisible;
//                etNew.setSelection(etNew.getText().length());
//            });
//
//            ivToggleConfirm.setOnClickListener(v3 -> {
//                if (isConfirmVisible) {
//                    etConfirm.setTransformationMethod(new PasswordTransformationMethod());
//                    ivToggleConfirm.setImageResource(R.drawable.baseline_visibility_off_24);
//                } else {
//                    etConfirm.setTransformationMethod(new HideReturnsTransformationMethod());
//                    ivToggleConfirm.setImageResource(R.drawable.baseline_remove_red_eye_24);
//                }
//                isConfirmVisible = !isConfirmVisible;
//                etConfirm.setSelection(etConfirm.getText().length());
//            });
//
//
//            btnChange.setOnClickListener(v1 -> {
//                String current = etCurrent.getText().toString().trim();
//                String newPass = etNew.getText().toString().trim();
//                String confirm = etConfirm.getText().toString().trim();
//
//                String savedPassword = prefs.getString("password", "");
//                if (current.isEmpty()) {
//                    etCurrent.setError("الرجاء إدخال كلمة المرور الحالية");
//                    return;
//                } else if (current.length() < 6) {
//                    etCurrent.setError("كلمة المرور يجب أن لا تقل عن 6 خانات");
//                    return;
//                }
//
//                if (newPass.isEmpty()) {
//                    etNew.setError("الرجاء إدخال كلمة المرور الجديدة");
//                    return;
//                } else if (newPass.length() < 6) {
//                    etNew.setError("كلمة المرور الجديدة يجب أن لا تقل عن 6 خانات");
//                    return;
//                }
//
//                if (confirm.isEmpty()) {
//                    etConfirm.setError("الرجاء تأكيد كلمة المرور الجديدة");
//                    return;
//                } else if (confirm.length() < 6) {
//                    etConfirm.setError("تأكيد كلمة المرور يجب أن لا تقل عن 6 خانات");
//                    return;
//                }
//
//                if (!current.equals(savedPassword)) {
//                    UserUtils.getMessageFromLocal(165, dbHelper, new UserUtils.MessageCallback() {
//                        @Override
//                        public void onSuccess(String message) {
//                            UserUtils.ToastMessages(getActivity(), message);
//                        }
//
//                        @Override
//                        public void onError(String error) {
//                        }
//                    });
//                    return;
//                }
//
//                if (current.equals(newPass)) {
//                    UserUtils.getMessageFromLocal(166, dbHelper, new UserUtils.MessageCallback() {
//                        @Override
//                        public void onSuccess(String message) {
//                            UserUtils.ToastMessages(getActivity(), message);
//                        }
//
//                        @Override
//                        public void onError(String error) {
//                        }
//                    });
//                    return;
//                }
//
//                if (!newPass.equals(confirm)) {
//                    UserUtils.getMessageFromLocal(167, dbHelper, new UserUtils.MessageCallback() {
//                        @Override
//                        public void onSuccess(String message) {
//                            UserUtils.ToastMessages(getActivity(), message);
//                        }
//
//                        @Override
//                        public void onError(String error) {
//                        }
//                    });
//                    return;
//                }
//
//                sendPasswordToServer(newPass);
//                exitDialog.dismiss();
//            });
//
//        });


        about.setOnClickListener(v -> {
            UserUtils.app_Page(getContext(), 9);
            ((HomePage) requireActivity()).openFullScreenFragment(new about(), "معلومات التطبيق", R.drawable.icons8_info_1, 0);

//            openFullScreenFragment(new about(), "معلومات التطبيق", R.drawable.icons8_info_1, 0);
        });

// زر "طلبات المسافرين"
        DriverTripRequest.setOnClickListener(v -> {
            UserUtils.app_Page(getContext(), 8);
            ((HomePage) requireActivity()).openFullScreenFragment(new DriverTripRequest(), "طلبات المسافرين", R.drawable.solo_traveller, 0);

//            openFullScreenFragment(new DriverTripRequest(), "طلبات المسافرين", R.drawable.solo_traveller, 0);
        });

// زر "إضافة/طلب رحلة"
        addtripBtn.setOnClickListener(v -> {
            if (userType.equalsIgnoreCase("driver")) {
                UserUtils.app_Page(getContext(), 6);
                ((HomePage) requireActivity()).openFullScreenFragment(new AddTripFragment(), "إضافة رحلة", R.drawable.locations, 0);

//                openFullScreenFragment(new AddTripFragment(), "إضافة رحلة", R.drawable.solo_traveller, 1);

                // تحديث حالة الـ Bottom Nav والـ FAB
                ((HomePage) requireActivity()).selectTab(R.id.fab);
                ((HomePage) requireActivity()).fab.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.primary));
            } else {
                ((HomePage) requireActivity()).openFullScreenFragment(new AddTripRequests(), "طلب رحلة خاصة", R.drawable.locations, 0);

//                openFullScreenFragment(new AddTripRequests(), "طلب رحلة خاصة", R.drawable.locations, 1);

                ((HomePage) requireActivity()).selectTab(R.id.fab);
                ((HomePage) requireActivity()).fab.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.primary));
            }
        });
//        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
//            if (getActivity() != null && getActivity() instanceof HomePage) {
//                HomePage activity = (HomePage) getActivity();
//
//                // البحث عن الفراجمنت داخل الحاوية الجديدة
//                Fragment currentFragment = activity.getSupportFragmentManager()
//                        .findFragmentById(R.id.full_screen_container);
//
//                if (currentFragment != null) {
//                    if (currentFragment instanceof HomeFragment) {
//                        Bundle args = currentFragment.getArguments();
//                        String tripType = args != null ? args.getString("trip_type", "1") : "1";
//                        switch (tripType) {
//                            case "1":
//                                updateToolbar("رحلات تشاركية", false, R.drawable.big_car, 0);
//                                break;
//                            case "2":
//                                updateToolbar("نقل دولي", false, R.drawable.world_new, 0);
//                                break;
//                            case "3":
//                                updateToolbar("رحلات محلية", false, R.drawable.bus_2, 0);
//                                break;
//                        }
//                    } else if (currentFragment instanceof BookingFragment) {
//                        updateToolbar("رحلاتي", false, R.drawable.airplane_t, 1);
//                    } else if (currentFragment instanceof DriverTripRequest) {
//                        updateToolbar("طلبات المسافرين", false, R.drawable.solo_traveller, 0);
//                    } else if (currentFragment instanceof AddTripFragment) {
//                        updateToolbar("إضافة رحلة", false, R.drawable.locations, 0);
//                    } else if (currentFragment instanceof NotificationFragment) {
//                        updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);
//                    } else if (currentFragment instanceof AddTripRequests) {
//                        updateToolbar("طلب رحلة خاصة", false, R.drawable.locations, 0);
//                    } else if (currentFragment instanceof TravelerRequests) {
//                        updateToolbar("خدمات المسافرين", false, R.drawable.solo_traveller, 0);
//                    } else if (currentFragment instanceof BookingDetailsFragment) {
//                        updateToolbar("تفاصيل الحجز", false, R.drawable.checklist, 0);
//                    } else if (currentFragment instanceof CustomBooking) {
//                        updateToolbar("حجز رحلة", false, R.drawable.booking, 0);
//                    } else if (currentFragment instanceof VehicleFragment) {
//                        updateToolbar("بيانات المركبات", false, R.drawable.local, 0);
//                    } else if (currentFragment instanceof AddVehicleFragment) {
//                        Bundle args = currentFragment.getArguments();
//                        String title = (args != null && args.containsKey("vehicle_id")) ? "تعديل المركبة" : "إضافة مركبة";
//                        updateToolbar(title, false, R.drawable.local, 0);
//                    } else if (currentFragment instanceof SharingFragment) {
//                        updateToolbar("الأعضاء المنضمون", false, R.drawable.frame, 0);
//                    } else if (currentFragment instanceof BalanceFragment) {
//                        updateToolbar("رصيدي", false, R.drawable.wallet, 0);
//                    } else if (currentFragment instanceof TripDetailsFragment) {
//                        updateToolbar("تفاصيل الطلب", false, R.drawable.add_trip, 0);
//                    } else if (currentFragment instanceof AllTravelerRequests) {
//                        updateToolbar("طلبات الخدمات", false, R.drawable.solo_traveller, 0);
//                    } else if (currentFragment instanceof ProfileFragment) {
//                        updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 0);
//                    } else if (currentFragment instanceof TravelerRequestsDetails) {
//                        updateToolbar("تفاصيل الخدمة", false, R.drawable.solo_traveller, 0);
//                    } else if (currentFragment instanceof AddTravelerRequests) {
//                        Bundle args = currentFragment.getArguments();
//                        String title = (args != null) ? args.getString("type_tr_name", "طلب خدمة") : "طلب خدمة";
//                        int icon = (args != null) ? args.getInt("icon_tr", 0) : 0;
//                        updateToolbar(title, false, icon, 0);
//                    } else if (currentFragment instanceof AllImagesFragment) {
//                        updateToolbar("الإعلانات", false, R.drawable.ads, 0);
//                    } else if (currentFragment instanceof Advertisements) {
//                        updateToolbar("تفاصيل الإعلان", false, R.drawable.ads, 0);
//                    } else if (currentFragment instanceof MoreDetails) {
//                        updateToolbar("تفاصيل الرحلة", false, R.drawable.booking, 0);
//                    } else if (currentFragment instanceof GuideFragment) {
//                        updateToolbar("دليل المسافر", false, R.drawable.solo_traveller, 0);
//                    } else if (currentFragment instanceof SettingFragment) {
//                        updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 1);
//                    } else {
//                        String full_name = prefs.getString("full_name", "");
//                        Toolbar toolbar = requireActivity().findViewById(R.id.main_toolbar);
//                        String fullNames = full_name.trim();
//                        String firstName = "";
//                        if (!fullNames.isEmpty()) {
//                            String[] parts = fullNames.split("\\s+");
//                            if (parts.length > 0) {
//                                firstName = parts[0];
//                                if (!firstName.isEmpty()) {
//                                    firstName = firstName.substring(0, 1).toUpperCase() +
//                                            firstName.substring(1).toLowerCase();
//                                }
//                            }
//                        }
//                        for (int i = 0; i < toolbar.getChildCount(); i++) {
//                            View child = toolbar.getChildAt(i);
//                            if (child.getId() == R.id.textGreeting || (child.findViewById(R.id.textGreeting) != null)) {
//                                toolbar.removeViewAt(i);
//                                break;
//                            }
//                        }
//
//                        View customView = getLayoutInflater().inflate(R.layout.toolbar_custom, null);
//                        TextView textGreeting = customView.findViewById(R.id.textGreeting);
//                        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//                        String greeting = (hour >= 5 && hour < 12) ? "صباح الخير" : "مساء الخير";
//                        String fullText = greeting + " " + firstName;
//                        textGreeting.setText(fullText);
//                        toolbar.addView(customView);
//                    }
//
//
//                } else {
//                    activity.findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
//                    activity.findViewById(R.id.full_screen_container).setVisibility(View.GONE);
//                    activity.updateHomeToolbar();
//                }
//            }
//        });
//
        profileBtn.setOnClickListener(v -> {
            UserUtils.app_Page(getContext(), 4);
            ((HomePage) requireActivity()).openFullScreenFragment(new ProfileFragment(), "الملف الشخصي", R.drawable.profile_new, 0);

//            openFullScreenFragment(new ProfileFragment(), "الملف الشخصي", R.drawable., 0);

        });

        logout.setOnClickListener(v -> {
            showExitConfirmationDialog(getContext());
        });
//        deleteAccount.setOnClickListener(v -> {
//            showDeleteConfirmationDialog(getContext());
//        });
        UserUtils.updateProfile(getActivity(), new UserUtils.ProfileUpdateCallback() {
            @Override
            public void onProfileUpdated(boolean isVerified, boolean isActive) {

                if (!isActive) {
                    if (isAdded() && getActivity() != null) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        getActivity().finish();
                    }
                    UserUtils.getMessageFromLocal(22, dbHelper, new UserUtils.MessageCallback() {
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
        });
        return view;
    }


//    private void openFullScreenFragment(Fragment fragment, String title, int iconRes, int fragmentId) {
//        if (getActivity() != null && getActivity() instanceof HomePage) {
//            HomePage home = (HomePage) getActivity();
//
//            View viewPager = home.findViewById(R.id.viewPager);
//            View fullContainer = home.findViewById(R.id.full_screen_container);
//
//            if (viewPager != null) viewPager.setVisibility(View.GONE);
//            if (fullContainer != null) {
//                fullContainer.setVisibility(View.VISIBLE);
//
//                home.getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.full_screen_container, fragment)

    /// /                        .addToBackStack(null)
//                        .commitNowAllowingStateLoss();
//
//                ((HomePage) getActivity()).updateToolbar(title, false, iconRes, fragmentId);
//            }
//        }
//    }
    private void updateToolbar(String title, boolean showBackArrow, int iconRes, int fragmentId) {
        HomePage activity = (HomePage) getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
        }
        activity.updateToolbar(title, showBackArrow, iconRes, fragmentId);

    }

    private void sendPasswordToServer(String newPassword) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("جاري تحديث كلمة المرور...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String token = prefs.getString("auth_token", null);
        String deviceId = UserUtils.getDeviceID(getContext());
        String deviceInfo = UserUtils.getDeviceInfo();
        String url = BASE_URL + "auth/profile/?device_id=" + deviceId + "&device_info=" + deviceInfo;
        DBHelper dbHelper = new DBHelper(getContext());

        StringRequest request = new StringRequest(Request.Method.PATCH, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("user_id")) {
                            // ✅ حفظ كلمة المرور الجديدة في SharedPreferences
                            editor.putString("password", newPassword);
                            editor.apply();

                            UserUtils.getMessageFromLocal(24, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });

                        } else {
                            UserUtils.sendLog(getContext(), "sendPasswordToServer", jsonObject.toString(), jsonObject.toString(), "SettingFragment");
                            UserUtils.getMessageFromLocal(23, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                        }
                    } catch (Exception e) {
                        UserUtils.sendLog(getContext(), "sendPasswordToServer", e.toString(), e.toString(), "SettingFragment");
                        UserUtils.getMessageFromLocal(25, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
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
                        UserUtils.sendLog(getContext(), "sendPasswordToServer",
                                String.valueOf(statusCode), responseBody, "Change Password");
                    }

                    UserUtils.sendLog(getContext(), "sendPasswordToServer",
                            error.toString(), error.toString(), "SettingFragment");
                    UserUtils.getMessageFromLocal(25, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", newPassword);

                String userPhone = getActivity().getIntent().getStringExtra("user_phone");
                if (userPhone != null) {
                    params.put("phone", userPhone);
                }

                if (token != null) {
                    params.put("token", token);
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(request);
    }


    private AlertDialog exitDialog;

    private void showExitConfirmationDialog(Context context) {
        if (getActivity() == null || getActivity().isFinishing()) return;

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_confirm, null);
        builder.setView(dialogView);

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setTextSize(18);
        btnNo.setTextSize(18);

        // الأحداث
        btnYes.setOnClickListener(v -> {
            String fileName = "MyAppPrefs_Secure";
            try {
                SharedPreferences prefs2 = SharedPrefsHelper.get(getContext());
                prefs2.edit().clear().apply();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getContext().deleteSharedPreferences(fileName);
            } else {
                File dir = new File(getContext().getApplicationInfo().dataDir, "shared_prefs");
                File file = new File(dir, fileName + ".xml");
                if (file.exists()) {
                    file.delete();
                }
            }

            // إغلاق الدايلوج والتوجه لشاشة البداية
            if (exitDialog != null && exitDialog.isShowing()) {
                exitDialog.dismiss();
            }

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        btnNo.setOnClickListener(v -> {
            if (exitDialog != null && exitDialog.isShowing()) {
                exitDialog.dismiss();
            }
        });

        exitDialog = builder.create();


        ViewGroup decorView = requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);

        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }

        if (isAdded() && !getActivity().isFinishing()) {
            exitDialog.show();
            if (exitDialog.getWindow() != null) {
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.80);
                exitDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        }
    }

//    private void showDeleteConfirmationDialog(Context context) {
//        if (getActivity() == null || getActivity().isFinishing()) return;
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_account, null);
//        builder.setView(dialogView);
//
//        Button btnYes = dialogView.findViewById(R.id.btnYes);
//        Button btnNo = dialogView.findViewById(R.id.btnNo);
//        RadioGroup rgReason = dialogView.findViewById(R.id.rgDeleteReason);
//        EditText etNotes = dialogView.findViewById(R.id.etDeleteNotes);
//
//        ViewGroup decorView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
//
//        btnYes.setOnClickListener(v -> {
//            int selectedId = rgReason.getCheckedRadioButtonId();
//            if (selectedId == -1) {
//                UserUtils.ToastMessages(getActivity(), "يرجى اختيار سبب الحذف أولاً");
//                return;
//            }
//
//            RadioButton rb = dialogView.findViewById(selectedId);
//            String reason = rb.getText().toString();
//            String notes = etNotes.getText().toString();
//
//            deleteUser(reason, notes);
//        });
//
//        btnNo.setOnClickListener(v -> {
//            if (exitDialog != null && exitDialog.isShowing()) {
//                exitDialog.dismiss();
//            }
//        });
//
//        exitDialog = builder.create();
//
//        Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
//        exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));
//
//        if (exitDialog.getWindow() != null) {
//            exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
//        }
//        exitDialog.show();
//    }
//
//
//    private void deleteUser(String reason, String notes) {
//        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
//        String token = prefs.getString("auth_token", "");
//        int userId = prefs.getInt("user_id", -1);
//        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.ENGLISH);
//        String currentDate = sdf.format(new java.util.Date());
//        DBHelper dbHelper = new DBHelper(getContext());
//        if (token.isEmpty() || userId == -1) return;
//
//        showLoading();
//        String deviceId = UserUtils.getDeviceID(getContext());
//        String deviceInfo = UserUtils.getDeviceInfo();
//        String url = BASE_URL + "auth/profile/?device_id=" + deviceId + "&device_info=" + deviceInfo;
//
//        JSONObject postData = new JSONObject();
//        try {
//            postData.put("reason_deletion", reason);
//            postData.put("token", token);
//            postData.put("note_deletion", notes);
//            postData.put("date_request", currentDate);
//            postData.put("device_info", deviceInfo);
//        } catch (JSONException e) {

    /// /            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//
//        JsonObjectRequest request = new JsonObjectRequest(
//                Request.Method.PATCH,
//                url,
//                postData,
//                response -> {
//                    hideLoading();
//                    if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss();
//
//                    prefs.edit().clear().apply();
//
//                    if (getActivity() != null) {
//                        Intent intent = new Intent(getActivity(), MainActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                        getActivity().finish();
//                        UserUtils.getMessageFromLocal(241, dbHelper, new UserUtils.MessageCallback() {
//                            @Override
//                            public void onSuccess(String message) {
//                                UserUtils.ToastMessages(getActivity(), message);
//                            }
//
//                            @Override
//                            public void onError(String error) {
//                            }
//
//                        });
//                    }
//                },
//                error -> {
//                    hideLoading();
//                    UserUtils.ToastMessages(getActivity(), "حدث خطأ أثناء حذف الحساب");
//                    UserUtils.sendLog(getContext(), "deleteUser", error.toString(), "user_id = " + userId, "SettingFragment");
//
//                }
//        ) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + token);
//                headers.put("Accept", "application/json");
//                return headers;
//            }
//        };
//
//        Volley.newRequestQueue(getContext()).add(request);
//    }
//
//    private ProgressDialog progressDialog;
//
//    private void showLoading() {
//        progressDialog = new ProgressDialog(getContext());
//        progressDialog.setMessage("جاري حذف الحساب...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//    }
//
//    private void hideLoading() {
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exitDialog != null && exitDialog.isShowing()) {
            exitDialog.dismiss();
        }
    }
}
