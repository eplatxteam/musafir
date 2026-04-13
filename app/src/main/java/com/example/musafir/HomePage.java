package com.example.musafir;

import static com.example.musafir.LocationWorker.getCityNameFromIp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.RoundedRectangle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        Button crashButton = new Button(this);
        crashButton.setText("Test Crash");
        crashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
//                Log.e("===", "Crash button clicked");
                throw new RuntimeException("Test Crash"); // Force a crash
            }
        });

        addContentView(crashButton, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

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
            }
//            if (id == R.id.nav_reservation) {
//                Fragment currentFragment = getSupportFragmentManager()
//                        .findFragmentByTag("f" + viewPager.getCurrentItem());
//
//                if (currentFragment instanceof BookingFragment) {
//                    ((BookingFragment) currentFragment).refreshCurrentTab();
//                }
//            }
            else if (id == R.id.nav_reservation) {
                viewPager.setCurrentItem(1, false);


                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentByTag("f1");

                if (currentFragment instanceof BookingFragment) {
                    ((BookingFragment) currentFragment).refreshCurrentTab();
                }
            }
        });
//        nav.setItemIconTintList(null);
        viewPager = findViewById(R.id.viewPager);
//        nav.setOnTouchListener((v, event) -> {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    v.performClick();
//                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
//                    break;
//                case MotionEvent.ACTION_CANCEL:
//                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
//                    break;
//            }
//            return false;
//        });
        MyPagerAdapter adapter = new MyPagerAdapter(this);
        viewPager.setAdapter(adapter);


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position >= 0 && position < nav.getMenu().size()) {
                    nav.getMenu().getItem(position).setChecked(true);
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
            findViewById(R.id.full_screen_container).setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);

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
            } else if (id == R.id.fab) viewPager.setCurrentItem(2, false);
            else if (id == R.id.nav_notifications) viewPager.setCurrentItem(3, false);
            else if (id == R.id.nav_profile) viewPager.setCurrentItem(4, false);
            return true;
        });

//        nav.setOnItemSelectedListener(item -> {
//            int id = item.getItemId();
//
//            // 1. أهم خطوة: إخفاء حاوية الفراجمنتس الكاملة وإظهار الـ ViewPager
//            findViewById(R.id.full_screen_container).setVisibility(View.GONE);
//            viewPager.setVisibility(View.VISIBLE);
//
//            // 2. تفريغ الـ BackStack لضمان عدم وجود فراجمنتس عالقة في الذاكرة
//            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//
//            // 3. التنقل الطبيعي بين التبويبات
//            if (id == R.id.nav_home) viewPager.setCurrentItem(0);
//            else if (id == R.id.nav_reservation) viewPager.setCurrentItem(1);
//            else if (id == R.id.fab) viewPager.setCurrentItem(2);
//            else if (id == R.id.nav_notifications) viewPager.setCurrentItem(3);
//            else if (id == R.id.nav_profile) viewPager.setCurrentItem(4);
//
//            return true;
//        });

        // 5. منطق الـ FAB
        fab.setOnClickListener(v -> {
            SharedPreferences preferences = SharedPrefsHelper.get(this);
            if (preferences.getInt("user_id", -1) == -1) {
                startActivity(new Intent(this, MainActivity.class));
                return;
            }
            viewPager.setCurrentItem(2);
        });

        // 6. تحميل الصفحة الافتراضية (الرئيسية)
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

        SharedPreferences preferences = SharedPrefsHelper.get(this);
        int userId = preferences.getInt("user_id", -1);
        String full_name = preferences.getString("full_name", "");
        String device_serial = preferences.getString("device_serial", "");
        int user_id = preferences.getInt("user_id", 0);
        String tokens = preferences.getString("auth_token", "");

        getCityNameFromIp(this, (cityAr, cityId) -> {
            preferences.edit().putString("default_city", cityAr).apply();
            preferences.edit().putInt("default_city_id", cityId).apply();
        });
        UserUtils.checkAppUpdate(this);
        scheduleLocationUpdate();


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
            } else {
                UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(HomePage.this, message);
                    }

                    @Override
                    public void onError(String error) {
                    }

                });
            }
        }

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

        fab.setOnClickListener(v -> {

            if (userId == -1) {
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
                finish();
                return;
            }

            String user_type = preferences.getString("user_type", "");

            int fabPageIndex = 2;

            if (user_type.equals("driver")) {
                viewPager.setCurrentItem(fabPageIndex);

                nav.setSelectedItemId(R.id.fab);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.primary));

                UserUtils.app_Page(this, 6);
                updateToolbar("إضافة رحلة", false, R.drawable.locations, 1);

            } else {

                viewPager.setCurrentItem(fabPageIndex);

                nav.setSelectedItemId(R.id.fab);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.primary));

                UserUtils.app_Page(this, 11);
                updateToolbar("طلب رحلة", false, R.drawable.locations, 1);
            }
        });

//        getOnBackPressedDispatcher().addCallback(
//                this,
//                new OnBackPressedCallback(true) {
//                    @Override
//                    public void handleOnBackPressed() {
//
//                        FragmentManager fm = getSupportFragmentManager();
//
//                        if (fm.getBackStackEntryCount() > 0) {
//                            fm.popBackStack();  // يرجع خطوة واحدة
//                        } else {
////                            showExitConfirmationDialog(); // لو مافيش صفحات مفتوحة
//                        }
//                    }
//                });
//        fab.setImageTintList(null);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);

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
                updateToolbar("رحلاتي", false, R.drawable.airplane_new, 1);
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                break;
            case 2:
                String title = prefs.getString("user_type", "").equals("driver") ? "إضافة رحلة" : "طلب رحلة";
                updateToolbar(title, false, R.drawable.locations, 1);
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.primary));
                break;
            case 3:
                updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                break;
            case 4:
                updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 1);
//                fab.setImageTintList(null);
                fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                break;
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
        String firstName = "";

        if (!full_name.trim().isEmpty()) {
            String[] parts = full_name.trim().split("\\s+");
            firstName = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase();
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (hour >= 5 && hour < 12) ? "صباح الخير" : "مساء الخير";

        updateToolbar(greeting + " " + firstName, false, R.drawable.profile_new, 1);

        Toolbar toolbar = findViewById(R.id.main_toolbar);

        toolbar.post(() -> {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View child = toolbar.getChildAt(i);
                LinearLayout toolbar_container = child.findViewById(R.id.toolbar_container);
                if (toolbar_container != null) {
                    toolbar_container.setOnClickListener(v -> {
                        viewPager.setVisibility(View.GONE);
                        FrameLayout fullContainer = findViewById(R.id.full_screen_container);
                        fullContainer.setVisibility(View.VISIBLE);

                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.full_screen_container, new ProfileFragment())
                                .addToBackStack("profile")
                                .commit();

                        updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 0);
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


    //    @Override
//    public void onBackPressed() {
//        FrameLayout fullContainer = findViewById(R.id.full_screen_container);
//        FragmentManager fm = getSupportFragmentManager();
//
//        // 1. إذا كان هناك صفحات داخل الـ Stack (مثل تفاصيل الإعلان)
//        if (fm.getBackStackEntryCount() > 0) {
//            fm.popBackStack();
//
//            // 2. إذا كان هذا الرجوع سيعيدنا للرئيسية (أي أن الـ Stack سيصبح فارغاً)
//            if (fm.getBackStackEntryCount() == 1) {
//                if (fullContainer != null) fullContainer.setVisibility(View.GONE);
//                viewPager.setVisibility(View.VISIBLE);
//                updateHomeToolbar(); // تحديث العنوان ليعود "صباح الخير" أو العنوان الرئيسي
//            }
//        }
//        // 3. إذا كنا في التبويبات العادية وليس في صفحة كاملة
//        else if (viewPager.getCurrentItem() != 0) {
//            viewPager.setCurrentItem(0);
//        }
//        // 4. الخروج من التطبيق
//        else {
//            super.onBackPressed();
//        }
//    }
//    @Override
//    public void onBackPressed() {
//        FrameLayout fullContainer = findViewById(R.id.full_screen_container);
//        FragmentManager fm = getSupportFragmentManager();
//
//        if (fm.getBackStackEntryCount() > 0) {
//            fm.popBackStack();
//
//            fm.executePendingTransactions();
//
//            if (fm.getBackStackEntryCount() == 0) {
//                if (fullContainer != null) fullContainer.setVisibility(View.GONE);
//                viewPager.setVisibility(View.VISIBLE);
//
//                updateUIForPosition(viewPager.getCurrentItem());
//            }
//        } else if (viewPager.getCurrentItem() != 0) {
//            viewPager.setCurrentItem(0);
//        } else {
//            super.onBackPressed();
//        }
//    }
//    @Override
//    public void onBackPressed() {
//        FrameLayout fullContainer = findViewById(R.id.full_screen_container);
//        FragmentManager fm = getSupportFragmentManager();
//
//        if (fullContainer != null && fullContainer.getVisibility() == View.VISIBLE) {
//            // إذا كان هناك أكثر من فراجمنت في السجل، ارجع خطوة واحدة فقط
//            if (fm.getBackStackEntryCount() > 1) {
//                fm.popBackStack();
//            } else {
//                // إذا كان هذا آخر فراجمنت، ارجع للـ ViewPager وحدث العنوان
//                fm.popBackStack();
//                fullContainer.setVisibility(View.GONE);
//                findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
//                updateUIForPosition(viewPager.getCurrentItem());
//            }
//        } else if (viewPager.getCurrentItem() != 0) {
//            viewPager.setCurrentItem(0);
//        } else {
//            super.onBackPressed();
//        }
//    }

//    @Override
//    public void onBackPressed() {
//
//        FrameLayout fullContainer = findViewById(R.id.full_screen_container);
//        FragmentManager fm = getSupportFragmentManager();
//        if (fm.getBackStackEntryCount() > 0) {
//            fm.popBackStack();
//        } else {
//            showExitDialog();
//        }
//        if (fullContainer != null && fullContainer.getVisibility() == View.VISIBLE) {
//            if (fm.getBackStackEntryCount() > 1) {
//                fm.popBackStack();
//            } else {
//                fm.popBackStack();
//                fullContainer.setVisibility(View.GONE);
//                viewPager.setVisibility(View.VISIBLE);
//                updateUIForPosition(viewPager.getCurrentItem());
//            }
//        } else if (viewPager.getCurrentItem() != 0) {
//            viewPager.setCurrentItem(0);
//        } else {
//            super.onBackPressed();
//        }
//    }

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
//        if (fab != null) {
//            fab.setImageTintList(ContextCompat.getColorStateList(this, R.color.secondary));
//        }
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
        int defaultCityId = prefs.getInt("default_city_id", -1);


        if (token.isEmpty()) {
//            UserUtils.getMessageFromLocal(41, dbHelper, new UserUtils.MessageCallback() {
//                @Override
//                public void onSuccess(String message) {
//                    UserUtils.ToastMessages(HomePage.this, message);
//                }
//
//                @Override
//                public void onError(String error) {
//                }
//
//            });
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
//        MenuItem aiChatMenu = menu.findItem(R.id.ai_chat);
//        View actionViewChat = aiChatMenu.getActionView();
//
//        if (actionViewChat != null) {
//            actionViewChat.setOnClickListener(v -> {
//                Intent intent = new Intent(this, ai_chat.class);
//                startActivity(intent);
//            });
//        }
        if (actionViewNotifications != null) {
            badgeTextView = actionViewNotifications.findViewById(R.id.badge);
            actionViewNotifications.setOnClickListener(v -> {
                viewPager.setVisibility(View.VISIBLE);
                findViewById(R.id.full_screen_container).setVisibility(View.GONE);

                nav.setSelectedItemId(R.id.nav_notifications);
            });
//            actionViewNotifications.setOnClickListener(v -> {
//                viewPager.setVisibility(View.VISIBLE);
//                View fullContainer = findViewById(R.id.full_screen_container);
//                if (fullContainer != null) fullContainer.setVisibility(View.GONE);
//
//                nav.setSelectedItemId(R.id.nav_notifications);
//            });
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
        return super.onOptionsItemSelected(item);
    }
}