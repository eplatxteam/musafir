package com.example.musafir;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.bumptech.glide.Glide;

import java.util.concurrent.TimeUnit;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    ImageView lottieWave;
    private String inviteCodeFromReferrer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        Firebase.initialize(this);

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.EplatX.musafir",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//            }
//        } catch (Exception e) {
//        }
        // 1. تهيئة الـ SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        lottieWave = findViewById(R.id.lottieWave);
        scheduleLocationUpdate();

        checkInstallReferrer();

        Glide.with(this)
                .asGif()
                .load(R.drawable.msafergif)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        resource.setLoopCount(1);
                        return false;
                    }
                })
                .into(lottieWave);
        if (!BuildConfig.DEBUG &&
                (android.os.Debug.isDebuggerConnected() || isEmulator())) {

            finishAffinity();
            System.exit(0);
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                UserUtils.sendLog(SplashActivity.this, "APP_CRASH",
                        "The application crashed. Device ID = " + UserUtils.getDeviceID(SplashActivity.this) + " Device Info= " + UserUtils.getDeviceInfo(), e.getMessage(),
                        "SplashActivity");

                System.exit(1);
            }
        });
        new Handler().postDelayed(this::goNext, 5500);
    }

    public static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.contains("generic")
                || Build.FINGERPRINT.contains("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }

    private void checkInstallReferrer() {
        InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    try {
                        ReferrerDetails response = referrerClient.getInstallReferrer();
                        String referrerUrl = response.getInstallReferrer();

                        if (referrerUrl != null && referrerUrl.contains("invite=")) {
                            // فك تشفير الرابط لاستخراج الكود
                            Uri uri = Uri.parse("musafir://invite?" + referrerUrl);
                            inviteCodeFromReferrer = uri.getQueryParameter("invite");

                            if (inviteCodeFromReferrer != null) {
                                saveInviteCode(inviteCodeFromReferrer);
                            }
                        }
                        referrerClient.endConnection();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
            }
        });
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

private void goNext() {
        Uri data = getIntent().getData();
        String inviteCode = null;

        // 1. استخراج الكود من الرابط العميق
        if (data != null) {
            inviteCode = data.getQueryParameter("invite");
        }

        if (inviteCode == null || inviteCode.isEmpty()) {
            inviteCode = inviteCodeFromReferrer;
        }

        SharedPreferences userPrefs = SharedPrefsHelper.get(this);
        int userId = userPrefs.getInt("user_id", 0);

        SharedPreferences onboardingPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean onboardingShown = onboardingPrefs.getBoolean("onboardingShown", false);

        // الشرط: إذا وجد كود دعوة والمستخدم جديد
        if (inviteCode != null && !inviteCode.isEmpty() && userId == 0) {
            saveInviteCode(inviteCode);

            // إذا لم يشاهد الـ onboarding بعد، نرسله إليها أولاً
            if (!onboardingShown) {
                Intent i = new Intent(SplashActivity.this, onboarding.class);
                i.putExtra("invite", inviteCode); // نمرر الكود لكي تستخدمه صفحة الـ onboarding لاحقاً
                startActivity(i);
            } else {
                // إذا شاهد الـ onboarding سابقاً ولكن لم يسجل، نرسله للتسجيل مباشرة
                Intent i = new Intent(SplashActivity.this, MainActivity2.class);
                i.putExtra("invite", inviteCode);
                startActivity(i);
            }
            finish();
            return;
        }

        proceedToApp();
    }

    private void proceedToApp() {
        SharedPreferences onboardingPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean onboardingShown = onboardingPrefs.getBoolean("onboardingShown", false);

        Intent intent;
        if (!onboardingShown) {
            intent = new Intent(SplashActivity.this, onboarding.class);
        } else {
            intent = new Intent(SplashActivity.this, HomePage.class);
        }
        startActivity(intent);
        finish();
    }

    private void saveInviteCode(String code) {
        SharedPreferences invitePrefs = getSharedPreferences("InvitePrefs", MODE_PRIVATE);
        invitePrefs.edit().putString("invite", code).apply();
    }
}