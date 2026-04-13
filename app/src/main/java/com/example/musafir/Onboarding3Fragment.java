package com.example.musafir;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class Onboarding3Fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_onboarding3, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        TextView titleText = view.findViewById(R.id.titleText);
        TextView descText = view.findViewById(R.id.descriptionText);
        ImageButton nextBtn = view.findViewById(R.id.nextBtn);

        nextBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences("AppPrefs", getActivity().MODE_PRIVATE);
                prefs.edit().putBoolean("onboardingShown", true).apply();

                Intent intent = new Intent(getActivity(), PrivacyPolicy.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }
}
