package com.example.musafir;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.Nullable;


public class Onboarding2Fragment extends Fragment {



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_onboarding2, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        TextView titleText = view.findViewById(R.id.titleText);
        TextView descText = view.findViewById(R.id.descriptionText);

        ImageButton nextBtn = view.findViewById(R.id.nextBtn);
        Button skipBtn = view.findViewById(R.id.skipBtn);
        ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);

        nextBtn.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            int next = current + 1;

            if (next < viewPager.getAdapter().getItemCount()) {
                viewPager.setCurrentItem(next, true); // true => تحريك سلس
            }
        });

// زر تخطي: ينقل مباشرة للصفحة الأخيرة
        skipBtn.setOnClickListener(v -> {
            int last = viewPager.getAdapter().getItemCount() - 1;
            viewPager.setCurrentItem(last, true);
        });


        return view;
    }
}
