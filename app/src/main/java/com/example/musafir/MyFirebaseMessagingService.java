package com.example.musafir;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String BASE_URL = UserUtils.BASE_URL;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = null;
        String body = null;
        String iconName = "ic_launcher_foreground";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if (remoteMessage.getData().containsKey("icon")) {
            iconName = remoteMessage.getData().get("icon");
        }

        // أولًا، حدث عدد الإشعارات في SharedPreferences
        incrementBadgeCount();

        // ثم عرض الإشعار
        showNotification(title, body, iconName);
    }

    private void incrementBadgeCount() {
        SharedPreferences prefs = SharedPrefsHelper.get(this);

//        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int currentCount = prefs.getInt("unread_count", 0);
        currentCount++;
        prefs.edit().putInt("unread_count", currentCount).apply();

        // إرسال broadcast ليحدث الـ badge مباشرة لو النشاط مفتوح
        Intent intent = new Intent("UPDATE_BADGE_COUNT");
        intent.putExtra("unread_count", currentCount);
        sendBroadcast(intent);
    }

    private void showNotification(String title, String message, String iconName) {
        int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        Context context;
        String CHANNEL_ID = "chat_channel_v2"; // قناة الإشعارات
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // إنشاء الـ Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH // أهم نقطة لجعل الإشعار منبثق
            );
            channel.setDescription("Notifications for chat messages");
            channel.enableLights(true);
            channel.enableVibration(true);

            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, HomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.putExtra("fragment_to_load", "notifications");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(iconResId != 0 ? iconResId : R.drawable.msafernotification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
