package com.example.musafir;

import static com.example.musafir.LocationWorker.getCityNameFromIp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.blurry.Blurry;

public class HomePage extends AppCompatActivity implements NotificationManager.UnreadCountListener {
    private TextView badgeTextView;
    String BASE_URL = UserUtils.BASE_URL;

    private int unreadCount = 0;
    BottomNavigationView nav;
    private BroadcastReceiver badgeReceiver;
    //    Toolbar toolbar;
    Fragment selectedFragment = null;
    DBHelper dbHelper = new DBHelper(this);
    ImageView fab;

    public void selectTab(int menuItemId) {
        BottomNavigationView nav = findViewById(R.id.bottomnavigation);
        nav.setSelectedItemId(menuItemId);
    }

    ViewPager2 viewPager;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        SharedPreferences preferences = SharedPrefsHelper.get(this);
        int userId = preferences.getInt("user_id", -1);
        String full_name = preferences.getString("full_name", "");

        String phone_number = preferences.getString("phone_number", "");
        if (getIntent().getBooleanExtra("is_crash", false)) {
            String errorMsg = getIntent().getStringExtra("error_message");
            String errorType = getIntent().getStringExtra("errorType");
            String crash_message = getIntent().getStringExtra("crash_message");
            if (errorMsg != null) {

                UserUtils.ToastMessages(this, errorMsg);
                UserUtils.sendLogClob(this, "handleError " + phone_number, errorType, errorMsg, "Application", crash_message);

            }
        }
//        Button crashButton = new Button(this);
//        crashButton.setText("Test Crash");
//        crashButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                throw new RuntimeException("Test Crash"); // Force a crash
//            }
//        });

        if (userId != -1) {

            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

            crashlytics.setUserId(String.valueOf(userId));
            FirebaseCrashlytics.getInstance().setCustomKey("userId", userId);
            FirebaseCrashlytics.getInstance().setCustomKey("user_phone", phone_number);
            FirebaseCrashlytics.getInstance().setCustomKey("user_name", full_name);
        }
        fab = findViewById(R.id.nav_add);

        nav = findViewById(R.id.bottomnavigation);
        nav.setOnItemReselectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                findViewById(R.id.full_screen_container).setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                viewPager.setCurrentItem(0);
                updateHomeToolbar();
            } else if (id == R.id.nav_reservation) {
                viewPager.setCurrentItem(1, false);
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f1");
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                viewPager.setCurrentItem(1);

                updateUIForPosition(1);
                if (currentFragment instanceof BookingFragment) {
                    ((BookingFragment) currentFragment).refreshCurrentTab();
                }
            } else if (id == R.id.nav_profile) {
                findViewById(R.id.full_screen_container).setVisibility(View.GONE);

                viewPager.setVisibility(View.VISIBLE);

                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                viewPager.setCurrentItem(4);

                updateUIForPosition(4);
            }
            // -------------------------
        });
//        nav.setOnItemReselectedListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.nav_home) {
//                findViewById(R.id.full_screen_container).setVisibility(View.GONE);
//                viewPager.setVisibility(View.VISIBLE);
//
//                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//
//                viewPager.setCurrentItem(0);
//
//                updateHomeToolbar();
//            } else if (id == R.id.nav_reservation) {
//                viewPager.setCurrentItem(1, false);
//
//
//                Fragment currentFragment = getSupportFragmentManager()
//                        .findFragmentByTag("f1");
//
//                if (currentFragment instanceof BookingFragment) {
//                    ((BookingFragment) currentFragment).refreshCurrentTab();
//                }
//            }
//        });
//        nav.setItemIconTintList(null);
        viewPager = findViewById(R.id.viewPager);
        MyPagerAdapter adapter = new MyPagerAdapter(this);
        viewPager.setAdapter(adapter);


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                validateSession();
                switch (position) {
                    case 0:
                        nav.setSelectedItemId(R.id.nav_home);
                        break;

                    case 1:
                        nav.setSelectedItemId(R.id.nav_reservation);
                        break;

                    case 2:
                        nav.setSelectedItemId(R.id.nav_notifications);
                        break;

                    case 3:
                        nav.setSelectedItemId(R.id.nav_profile);
                        break;
                }

                updateUIForPosition(position);
            }
        });
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            View fullContainer = findViewById(R.id.full_screen_container);
            View viewPager = findViewById(R.id.viewPager);

            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (fullContainer != null) fullContainer.setVisibility(View.GONE);
                if (viewPager != null) viewPager.setVisibility(View.VISIBLE);
            }
        });
        nav.setOnItemSelectedListener(item -> {
            validateSession();
            findViewById(R.id.full_screen_container).setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary)); // استبدل secondary بلونك غير النشط
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            int id = item.getItemId();
            if (id == R.id.nav_home) viewPager.setCurrentItem(0, false);
            else if (id == R.id.nav_reservation) {
                viewPager.setCurrentItem(1, false);


                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentByTag("f1");

                if (currentFragment instanceof BookingFragment) {
                    ((BookingFragment) currentFragment).refreshCurrentTab();
                }
            }
//            else if (id == R.id.fab) viewPager.setCurrentItem(2, false);
            else if (id == R.id.nav_notifications) viewPager.setCurrentItem(2, false);
            else if (id == R.id.nav_profile) viewPager.setCurrentItem(3, false);
            return true;
        });

        // جزء من الكود المحدث في HomePage.java
        fab.setOnClickListener(v -> {
            showFabMenu(); // استدعاء دالة القائمة الجديدة
        });

        viewPager.setCurrentItem(0, false);


        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                UserUtils.sendLog(HomePage.this, "APP_CRASH",
                        "The application crashed. Device ID = " + UserUtils.getDeviceID(HomePage.this) + " Device Info= " + UserUtils.getDeviceInfo(), e.getMessage(),
                        "Home page");

                System.exit(1);
            }
        });


        getCityNameFromIp(this, (cityAr, cityId, country_code) -> {
            preferences.edit().putString("default_city", cityAr).apply();
            preferences.edit().putString("country_code", country_code).apply();
            preferences.edit().putInt("default_city_id", cityId).apply();
        });
        UserUtils.checkAppUpdate(this);
        scheduleLocationUpdate();

        validateSession();

        handleIntent(getIntent());
        //-----------FirebaseMessaging-------------------------

        if (userId != -1) {
            requestNotificationPermission();
            try {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                return;
                            }
                            String token = task.getResult();
                            updateProfile(token);
                        });
            } catch (Exception e) {
                UserUtils.sendLog(this, "FirebaseMessaging", "FCM init error", e.toString(), "Home page");
            }
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
//        UserUtils.fetchBalance(this);
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.full_screen_container);

                if (currentFragment instanceof HomeFragment) {
                    Bundle args = currentFragment.getArguments();
                    String tripType = args != null ? args.getString("trip_type", "1") : "1";
                    switch (tripType) {
                        case "1":
                            updateToolbar("رحلات تشاركية", false, R.drawable.big_car, 0);
                            break;
                        case "2":
                            updateToolbar("نقل دولي", false, R.drawable.world_new, 0);
                            break;
                        case "3":
                            updateToolbar("رحلات محلية", false, R.drawable.bus_2, 0);
                            break;
                    }
                } else if (currentFragment instanceof BookingFragment) {
                    updateToolbar("رحلاتي", false, R.drawable.airplane_t, 1);
                } else if (currentFragment instanceof DriverTripRequest) {
                    updateToolbar("طلبات المسافرين", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof AddTripFragment) {
                    updateToolbar("إضافة رحلة", false, R.drawable.locations, 0);
                } else if (currentFragment instanceof NotificationFragment) {
                    updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);
                } else if (currentFragment instanceof AddTripRequests) {
                    updateToolbar("طلب رحلة خاصة", false, R.drawable.locations, 0);
                } else if (currentFragment instanceof TravelerRequests) {
                    updateToolbar("خدمات المسافرين", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof BookingDetailsFragment) {
                    updateToolbar("تفاصيل الحجز", false, R.drawable.checklist, 0);
                } else if (currentFragment instanceof CustomBooking) {
                    updateToolbar("حجز رحلة", false, R.drawable.booking, 0);
                } else if (currentFragment instanceof VehicleFragment) {
                    updateToolbar("بيانات المركبات", false, R.drawable.local, 0);
                } else if (currentFragment instanceof AddVehicleFragment) {
                    Bundle args = currentFragment.getArguments();
                    String title = (args != null && args.containsKey("vehicle_id")) ? "تعديل المركبة" : "إضافة مركبة";
                    updateToolbar(title, false, R.drawable.local, 0);
                } else if (currentFragment instanceof SharingFragment) {
                    updateToolbar("الأعضاء المنضمون", false, R.drawable.frame, 0);
                } else if (currentFragment instanceof BalanceFragment) {
                    updateToolbar("رصيدي", false, R.drawable.wallet, 0);
                } else if (currentFragment instanceof TripDetailsFragment) {
                    updateToolbar("تفاصيل الطلب", false, R.drawable.add_trip, 0);
                } else if (currentFragment instanceof AllTravelerRequests) {
                    updateToolbar("طلبات الخدمات", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof ProfileFragment) {
                    updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 0);
                } else if (currentFragment instanceof SettingFragment) {
                    updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 0);
                } else if (currentFragment instanceof TravelerRequestsDetails) {
                    updateToolbar("تفاصيل الخدمة", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof AddTravelerRequests) {
                    Bundle args = currentFragment.getArguments();
                    String title = (args != null) ? args.getString("type_tr_name", "طلب خدمة") : "طلب خدمة";
                    int icon = (args != null) ? args.getInt("icon_tr", 0) : 0;
                    updateToolbar(title, false, icon, 0);
                } else if (currentFragment instanceof AllImagesFragment) {
                    updateToolbar("الإعلانات", false, R.drawable.ads, 0);
                } else if (currentFragment instanceof Advertisements) {
                    updateToolbar("تفاصيل الإعلان", false, R.drawable.ads, 0);
                } else if (currentFragment instanceof MoreDetails) {
                    updateToolbar("تفاصيل الرحلة", false, R.drawable.booking, 0);
                } else if (currentFragment instanceof GuideFragment) {
                    updateToolbar("دليل المسافر", false, R.drawable.solo_traveller, 0);
                } else {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        updateUIForPosition(viewPager.getCurrentItem());
                    }
                }

        });

    }

    private void validateSession() {
        SharedPreferences preferences = SharedPrefsHelper.get(this);
        String device_serial = preferences.getString("device_serial", "");
        String tokens = preferences.getString("auth_token", "");
        if (!device_serial.isEmpty() && !tokens.isEmpty()) {
            if (isNetworkAvailable()) {
                UserUtils.check_serial(this, success -> {
                    if (!success) {
                        preferences.edit().clear().apply();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        UserUtils.getMessageFromLocal(40, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(HomePage.this, message);
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });
                        finish();
                    }
                });
            }
        }
    }

    private void showFabMenu() {
        if (isFinishing()) return;

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        View menuView = getLayoutInflater().inflate(R.layout.layout_fab_menu, null);
        dialog.setContentView(menuView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1A000000")));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        SharedPreferences prefs = SharedPrefsHelper.get(this);
        String user_type = prefs.getString("user_type", "");
        View item1 = menuView.findViewById(R.id.menu_item_1);
        View item2 = menuView.findViewById(R.id.menu_item_2);
        View item3 = menuView.findViewById(R.id.menu_item_3);
        View item4 = menuView.findViewById(R.id.menu_item_4);
        View btnCancel = menuView.findViewById(R.id.btnCancelMenu);
        TextView textlocal = menuView.findViewById(R.id.textlocal);
        TextView textworld = menuView.findViewById(R.id.textworld);
        TextView textShare = menuView.findViewById(R.id.textShare);
        TextView textaddtrip = menuView.findViewById(R.id.textaddtrip);
        if ("driver".equals(user_type)) {
            textlocal.setText("رحلات محلية");
            textworld.setText("نقل دولي");
            textShare.setText("رحلات تشاركية");
            textaddtrip.setText("إضافة رحلة");
        } else {
            textlocal.setText("حجز رحلة محلية");
            textworld.setText("حجز نقل دولي");
            textShare.setText("حجز رحلة تشاركية");
            textaddtrip.setText("طلب رحلة خاصة");
        }
        final View[] items = {item1, item2, item3, item4};
        final ViewGroup contentContainer = findViewById(android.R.id.content);

        Runnable dismissWithAnimation = () -> {
            long delay = 0;
            for (int i = items.length - 1; i >= 0; i--) {
                items[i].animate()
                        .scaleX(0f).scaleY(0f)
                        .alpha(0f)
                        .setDuration(300)
                        .setStartDelay(delay)
                        .setInterpolator(new AnticipateInterpolator())
                        .start();
                delay += 50;
            }

            new Handler(Looper.getMainLooper()).postDelayed(dialog::dismiss, delay + 300);
        };

        menuView.setOnClickListener(v -> dismissWithAnimation.run());

        for (View item : items) {
            item.setOnClickListener(v -> {
            });
        }

        for (View item : items) {
            item.setScaleX(0);
            item.setScaleY(0);
            item.setAlpha(0);
        }

        long openDelay = 50;
        for (View item : items) {
            item.animate()
                    .scaleX(1f).scaleY(1f)
                    .alpha(1f)
                    .setDuration(450)
                    .setStartDelay(openDelay)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
            openDelay += 60;
        }

        Blurry.with(this).radius(15).sampling(2).onto(contentContainer);
        dialog.setOnDismissListener(d -> Blurry.delete(contentContainer));
        int userId = prefs.getInt("user_id", -1);

        item1.setOnClickListener(v -> {
            if (userId == -1) {
                DBHelper dbHelper = new DBHelper(this);
                UserUtils.getMessageFromLocal(39, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(HomePage.this, message);
                    }
                    @Override
                    public void onError(String error) {
                    }
                });
                startActivity(new Intent(this, MainActivity.class));
            } else {
                dismissWithAnimation.run();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if ("driver".equals(user_type)) {
                        openFullScreenFragment(new AddTripFragment(), "إضافة رحلة", R.drawable.locations, 0);
                    } else {
                        openFullScreenFragment(new AddTripRequests(), "طلب رحلة خاصة", R.drawable.locations, 0);
                    }
                }, 400);
            }
        });

        item2.setOnClickListener(v ->

        {
            dismissWithAnimation.run();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                HomeFragment fragment = new HomeFragment();
                Bundle b = new Bundle();
                b.putString("trip_type", "2");
                fragment.setArguments(b);
                openFullScreenFragment(fragment, "نقل دولي", R.drawable.world_new, 0);
            }, 400);
        });

        item4.setOnClickListener(v ->

        {
            dismissWithAnimation.run();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                HomeFragment fragment = new HomeFragment();
                Bundle b = new Bundle();
                b.putString("trip_type", "3");
                fragment.setArguments(b);
                openFullScreenFragment(fragment, "رحلات محلية", R.drawable.bus_2, 0);
            }, 400);
        });

        item3.setOnClickListener(v ->

        {
            dismissWithAnimation.run();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                HomeFragment fragment = new HomeFragment();
                Bundle b = new Bundle();
                b.putString("trip_type", "1");
                fragment.setArguments(b);
                openFullScreenFragment(fragment, "رحلات تشاركية", R.drawable.big_car, 0);
            }, 400);
        });

        btnCancel.setOnClickListener(v -> dismissWithAnimation.run());

        dialog.show();
    }


    private void updateUIForPosition(int position) {
        SharedPreferences prefs = SharedPrefsHelper.get(this);

        viewPager.setVisibility(View.VISIBLE);
        findViewById(R.id.full_screen_container).setVisibility(View.GONE);

        switch (position) {
            case 0:
                updateHomeToolbar();
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                break;
            case 1:
                updateToolbar("رحلاتي", false, R.drawable.airplane_t, 1);
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                break;
//            case 2:
//                String title = prefs.getString("user_type", "").equals("driver") ? "إضافة رحلة" : "طلب رحلة";
//                updateToolbar(title, false, R.drawable.locations, 1);
////                fab.setImageTintList(null);
//                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.primary));
//                break;
            case 2:
                updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                break;
            case 3:
                updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 1);
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                break;
//            case 4:
//                String title = prefs.getString("user_type", "").equals("driver") ? "إضافة رحلة" : "طلب رحلة";
//                updateToolbar(title, false, R.drawable.locations, 1);
//                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.primary));
//                break;
            default:
                updateHomeToolbar();
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void openFullScreenFragment(Fragment fragment, String title, int iconRes, int fragmentId) {

        validateSession();
        View viewPager = findViewById(R.id.viewPager);
        View fullContainer = findViewById(R.id.full_screen_container);

        if (viewPager != null) viewPager.setVisibility(View.GONE);
        if (fullContainer != null) {
            fullContainer.setVisibility(View.VISIBLE);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.full_screen_container, fragment)
                    .addToBackStack(null)
                    .commit();


            updateToolbar(title, false, iconRes, fragmentId);
        }

    }

    private void showExitDialog() {
        new ExitConfirmationDialog().show(getSupportFragmentManager(), "exit_dialog");
    }

    public void updateHomeToolbar() {
        SharedPreferences prefs = SharedPrefsHelper.get(this);
        String full_name = prefs.getString("full_name", "");
        int userId = prefs.getInt("user_id", -1);

        String firstName = "";

        if (!full_name.trim().isEmpty()) {
            String[] parts = full_name.trim().split("\\s+");
            firstName = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase();
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (hour >= 5 && hour < 12) ? "صباح الخير" : "مساء الخير";
        String fullText = greeting + " " + firstName;
        updateToolbar(fullText, false, R.drawable.profile_new, 1);

        Toolbar toolbar = findViewById(R.id.main_toolbar);

        toolbar.post(() -> {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View child = toolbar.getChildAt(i);
                LinearLayout toolbar_container = child.findViewById(R.id.toolbar_container);
                if (toolbar_container != null) {

                    toolbar_container.setOnClickListener(v -> {
                        if (userId == -1) {
                            DBHelper dbHelper = new DBHelper(this);

                            UserUtils.getMessageFromLocal(39, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(HomePage.this, message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            startActivity(new Intent(this, MainActivity.class));
//            finish();
                        } else {
                            viewPager.setVisibility(View.GONE);
                            FrameLayout fullContainer = findViewById(R.id.full_screen_container);
                            fullContainer.setVisibility(View.VISIBLE);

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.full_screen_container, new ProfileFragment())
                                    .addToBackStack("profile")
                                    .commit();

                            updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 0);
                        }
                    });
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void updateToolbar(String title, boolean showBackIcon, int iconResId, int fragmentId) {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child.getId() == R.id.textGreeting || (child.findViewById(R.id.textGreeting) != null)) {
                toolbar.removeViewAt(i);
                break;
            }
        }

        View customView = getLayoutInflater().inflate(R.layout.toolbar_custom, null);
        TextView textGreeting = customView.findViewById(R.id.textGreeting);
        ImageView iconTool = customView.findViewById(R.id.iconTool);
        ImageView iconHi = customView.findViewById(R.id.iconHi);
        ImageView action_placeholder = customView.findViewById(R.id.action_placeholder);

        textGreeting.setText(title);
        iconTool.setImageResource(iconResId);
        iconTool.setVisibility(View.VISIBLE);
        if (title != null && (title.startsWith("صباح الخير") || title.startsWith("مساء الخير"))) {

//            iconHi.setVisibility(View.VISIBLE);
        } else {
//            iconHi.setVisibility(View.GONE);
        }


        if (fragmentId != 0) {
            action_placeholder.setVisibility(View.GONE);
        } else {
            action_placeholder.setVisibility(View.VISIBLE);
        }

        toolbar.addView(customView);

        if (showBackIcon) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            toolbar.setNavigationIcon(null);
        }

        action_placeholder.setOnClickListener(v -> onBackPressed());
    }


    @Override
    public void onBackPressed() {

//        super.onBackPressed();
        FrameLayout fullContainer = findViewById(R.id.full_screen_container);
        FragmentManager fm = getSupportFragmentManager();

        if (fullContainer != null && fullContainer.getVisibility() == View.VISIBLE) {
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();

                fm.executePendingTransactions();

                if (fm.getBackStackEntryCount() == 0) {
                    fullContainer.setVisibility(View.GONE);
                    findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
                    updateUIForPosition(viewPager.getCurrentItem());
                }
            } else {
                fullContainer.setVisibility(View.GONE);
                findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
            }
        } else if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        } else {
            showExitDialog();
        }
    }

    public void onUnreadCountChanged(int newCount) {
        runOnUiThread(() -> {
            if (newCount > 0) {
                badgeTextView.setVisibility(View.VISIBLE);
                badgeTextView.setText(String.valueOf(newCount));
            } else {
                badgeTextView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        NotificationManager.getInstance(this).removeListener(this);
    }


    public void updateBadge(int count) {
        if (badgeTextView != null) {
            if (count != 0) {
                badgeTextView.setVisibility(View.VISIBLE);
                badgeTextView.setText(String.valueOf(count));
            } else {
                badgeTextView.setVisibility(View.GONE);
            }
        }
    }

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && "notifications".equals(intent.getStringExtra("fragment_to_load"))) {
            nav.setSelectedItemId(R.id.nav_notifications);
        }
    }

    private void updateProfile(String tokenFirebase) {
        SharedPreferences prefs = SharedPrefsHelper.get(this);

//        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");
        String default_city = prefs.getString("default_city", "");
        String country = prefs.getString("country_code", "");
        int defaultCityId = prefs.getInt("default_city_id", -1);


        if (token.isEmpty()) {
            return;
        }
        String deviceId = UserUtils.getDeviceID(this);
        String deviceInfo = UserUtils.getDeviceInfo();
        String url = BASE_URL + "auth/profile/?device_id=" + deviceId + "&device_info=" + deviceInfo;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("token", token);
            requestBody.put("firebase_token", tokenFirebase);
            requestBody.put("default_city", default_city);
            requestBody.put("country", country);
            requestBody.put("city_id", defaultCityId);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                requestBody,
                response -> {
                },
                error -> {
                    UserUtils.sendLog(this, "UPDATE_PROFILE", error.toString(), error.toString(), "Home page");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private static final int REQUEST_CODE_NOTIFICATIONS = 1001;

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 وما فوق
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATIONS);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem notificationsItem = menu.findItem(R.id.action_notifications);
        View actionViewNotifications = notificationsItem.getActionView();
        if (actionViewNotifications != null) {
            badgeTextView = actionViewNotifications.findViewById(R.id.badge);
            actionViewNotifications.setOnClickListener(v -> {
                viewPager.setVisibility(View.VISIBLE);
                findViewById(R.id.full_screen_container).setVisibility(View.GONE);

                nav.setSelectedItemId(R.id.nav_notifications);
            });
        }
        loadNotificationCount();
        return true;
    }


    private void loadNotificationCount() {
        SharedPreferences prefs = SharedPrefsHelper.get(this);

//        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");
        if (token != null && !token.isEmpty()) {
            String url = BASE_URL + "notifications/unread/?token=" + token;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    response -> {
                        try {
                            int count = response.getInt("count");
                            unreadCount = count;
                            updateBadge(unreadCount);

                        } catch (JSONException e) {
                            UserUtils.sendLog(this, "loadNotificationCount", e.toString(), e.toString(), "Home page");
                        }
                    },
                    error -> {
                        // UserUtils.sendLog(this, "loadNotificationCount", error.toString(), error.toString(), "Home page");
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    if (token != null) {
                        headers.put("Authorization", "Bearer " + token);
                    }
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            Volley.newRequestQueue(this).add(jsonObjectRequest);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        SharedPreferences preferences = SharedPrefsHelper.get(this);

        int userId = preferences.getInt("user_id", -1);
        if (userId == -1) {
            DBHelper dbHelper = new DBHelper(this);

            UserUtils.getMessageFromLocal(39, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(HomePage.this, message);
                }

                @Override
                public void onError(String error) {
                }
            });
            startActivity(new Intent(this, MainActivity.class));
//            finish();
        } else {
            if (id == R.id.action_notifications) {

                viewPager.setVisibility(View.VISIBLE);
                findViewById(R.id.full_screen_container).setVisibility(View.GONE);

                nav.setSelectedItemId(R.id.nav_notifications);
                return true;
            }
            if (id == R.id.ai_chat) {
                Intent intent = new Intent(this, ai_chat.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}