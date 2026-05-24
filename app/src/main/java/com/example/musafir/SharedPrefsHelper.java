package com.example.musafir;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.File;

public class SharedPrefsHelper {

    private static final String SECURE_FILE_NAME = "MyAppPrefs_Secure_V2";
    private static final String FALLBACK_FILE_NAME = "MyAppPrefs_Backup";

    public static SharedPreferences get(Context context) {
        Context appContext = context.getApplicationContext();

        try {
            return createEncryptedPrefs(appContext, SECURE_FILE_NAME);
        } catch (Exception e) {
            UserUtils.sendLog(context, "get", "Critical: Decryption failed for V2. Attempting recovery.", e.getMessage(), "SharedPrefsHelper");

            deletePrefsFile(appContext, SECURE_FILE_NAME);

            try {
                return createEncryptedPrefs(appContext, SECURE_FILE_NAME + "_retry");
            } catch (Exception ex) {
                UserUtils.sendLog(context, "get", "Final Fallback: Using unencrypted prefs.", ex.getMessage(), "SharedPrefsHelper");

                return appContext.getSharedPreferences(FALLBACK_FILE_NAME, Context.MODE_PRIVATE);
            }
        }
    }

    private static SharedPreferences createEncryptedPrefs(Context context, String fileName) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    private static void deletePrefsFile(Context context, String fileName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.deleteSharedPreferences(fileName);
            }
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit().clear().apply();
            File dir = new File(context.getApplicationInfo().dataDir, "shared_prefs");
            File file = new File(dir, fileName + ".xml");
            if (file.exists()) file.delete();
        } catch (Exception e) {
            UserUtils.sendLog(context, "deletePrefsFile", "Final Fallback: Using unencrypted prefs.", e.getMessage(), "SharedPrefsHelper");

        }
    }
}
