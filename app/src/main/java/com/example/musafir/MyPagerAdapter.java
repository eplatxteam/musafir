package com.example.musafir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyPagerAdapter extends FragmentStateAdapter {

    private final Context context;

    public MyPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.context = fragmentActivity; // حفظ الـ Context لاستخدامه في الشيرد بريفرنس
    }

    private void handleUnauthenticated() {
        DBHelper dbHelper = new DBHelper(context);
        UserUtils.getMessageFromLocal(39, dbHelper, new UserUtils.MessageCallback() {
            @Override
            public void onSuccess(String message) {
                UserUtils.ToastMessages((Activity) context, message);
            }

            @Override
            public void onError(String error) {
            }
        });

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        SharedPreferences prefs = SharedPrefsHelper.get(context);
        int userId = prefs.getInt("user_id", -1);
        String userType = prefs.getString("user_type", "");


        switch (position) {
            case 0:
                return new PageHome();

            case 1:
                if (userId == -1 && position != 0) {
                    handleUnauthenticated();
                    return new PageHome();
                }
                BookingFragment bookingFragment = new BookingFragment();
                Bundle args = new Bundle();
//                args.putInt("tab_to_open", 0);
                bookingFragment.setArguments(args);
                return bookingFragment;

            case 2:
                if (userId == -1 && position != 0) {
                    handleUnauthenticated();
                    return new PageHome();
                }
                if ("driver".equals(userType)) {
                    return new AddTripFragment();
                } else {
                    return new AddTripRequests();
                }

            case 3:
                if (userId == -1 && position != 0) {
                    handleUnauthenticated();
                    return new PageHome();
                }
                return new NotificationFragment();

            case 4:
                if (userId == -1 && position != 0) {
                    handleUnauthenticated();
                    return new PageHome();
                }
                return new SettingFragment();

            default:
                return new PageHome();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}