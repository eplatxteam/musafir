package com.example.musafir;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class MyNotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if (packageName.equals("com.whatsapp")) {
            CharSequence content = sbn.getNotification().extras.getCharSequence("android.text");

            if (content != null) {
                String message = content.toString();

                String otp = extractOtpCode(message);
                if (otp != null) {
                    Intent intent = new Intent("com.example.OTP_RECEIVED");
                    intent.putExtra("otp", otp);
                    sendBroadcast(intent);
                }
            }
        }
    }

    private String extractOtpCode(String text) {
        // استخراج أول رمز مكون من 6 أرقام
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b\\d{6}\\b").matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
