package com.example.musafir;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Setting_Notification extends Fragment {
    LinearLayout routeNotfiy;
    private String BASE_URL = UserUtils.BASE_URL;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_setting_notification, container, false);
        setHasOptionsMenu(true);
        routeNotfiy = view.findViewById(R.id.routeNotfiy);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String notify_general = prefs.getString("notify_general", "0");
        String notify_primary = prefs.getString("notify_primary", "0");

        SwitchCompat switchPrimary = view.findViewById(R.id.switchNotification);
        SwitchCompat switchGeneral = view.findViewById(R.id.switchNotification3);

        switchPrimary.setChecked(notify_primary.equals("1"));
        switchGeneral.setChecked(notify_general.equals("1"));

        switchPrimary.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newValue = isChecked ? "1" : "0";
            editor.putString("notify_primary", newValue);
            editor.apply();

            // إرسال التغيير للسيرفر
            sendNotificationUpdateToServer("notify_primary", newValue);
        });

        switchGeneral.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newValue = isChecked ? "1" : "0";
            editor.putString("notify_general", newValue);
            editor.apply();

            // إرسال التغيير للسيرفر
            sendNotificationUpdateToServer("notify_general", newValue);
        });


        routeNotfiy.setOnClickListener(v -> {
            UserUtils.app_Page(getContext(), 7);
            Fragment fragment = new FavoriteFragment();

            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

//            transaction.runOnCommit(() -> {
            ((HomePage) requireActivity()).updateToolbar("إشعارات المسارات", false, R.drawable.notification_new, 0);
//            });

            transaction.commit();

        });
        return view;
    }

    private void sendNotificationUpdateToServer(String key, String value) {
        new Thread(() -> {
            try {
//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

                String token = prefs.getString("auth_token", "");
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "auth/profile/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                JSONObject json = new JSONObject();
                json.put(key, Integer.parseInt(value));
                json.put("token", token);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                conn.disconnect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomePage) {
            ((HomePage) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }


}