package com.example.musafir;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.File;

public class SharedPrefsHelper {

    private static final String SECURE_FILE_NAME = "MyAppPrefs_Secure";

    private static final String OLD_FILE_NAME = "MyAppPrefs";

    public static SharedPreferences get(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                return EncryptedSharedPreferences.create(
                        SECURE_FILE_NAME,
                        masterKeyAlias,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (Exception e) {
                deletePrefsFile(context, SECURE_FILE_NAME);
                try {
                    String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                    return EncryptedSharedPreferences.create(SECURE_FILE_NAME, masterKeyAlias, context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
                } catch (Exception ex) {
                    return context.getSharedPreferences(SECURE_FILE_NAME, Context.MODE_PRIVATE);
                }
            }
        } else {
            return context.getSharedPreferences(SECURE_FILE_NAME, Context.MODE_PRIVATE);
        }
    }

    private static void deletePrefsFile(Context context, String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.deleteSharedPreferences(fileName);
        } else {
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit().clear().apply();
            File dir = new File(context.getApplicationInfo().dataDir, "shared_prefs");
            File file = new File(dir, fileName + ".xml");
            if (file.exists()) file.delete();
        }
    }
}