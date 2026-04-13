package com.example.musafir;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static NotificationManager instance;
    private int unreadCount = 0;
    String BASE_URL = UserUtils.BASE_URL;

    private List<UnreadCountListener> listeners = new ArrayList<>();
    private Context context;

    public interface UnreadCountListener {
        void onUnreadCountChanged(int newCount);
    }

    private NotificationManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context);
        }
        return instance;
    }

    public void addListener(UnreadCountListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(UnreadCountListener listener) {
        listeners.remove(listener);
    }
}

