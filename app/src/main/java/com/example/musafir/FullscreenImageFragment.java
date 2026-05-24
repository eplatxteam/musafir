package com.example.musafir;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class FullscreenImageFragment extends Fragment {

    private static final String ARG_IMAGES = "images";
    private static final String ARG_CAR_NAME = "car_name";

    public static FullscreenImageFragment newInstance(ArrayList<String> images, String carName) {
        FullscreenImageFragment fragment = new FullscreenImageFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_IMAGES, images);
        args.putString(ARG_CAR_NAME, carName);
        fragment.setArguments(args);
        return fragment;
    }

    private ViewPager2 viewPager;
    private LinearLayout imagesContainer;
    TextView carNameText;

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
        setHasOptionsMenu(true);
        viewPager = view.findViewById(R.id.viewPager);
        imagesContainer = view.findViewById(R.id.imagesContainer);
        carNameText = view.findViewById(R.id.carNameText);
        ImageView backButton = view.findViewById(R.id.backButton);

        ArrayList<String> images = getArguments() != null ?
                getArguments().getStringArrayList(ARG_IMAGES) : new ArrayList<>();

        String carName = getArguments() != null ?
                getArguments().getString(ARG_CAR_NAME) : "";

        carNameText.setText(carName);

        ImagePagerAdapter adapter = new ImagePagerAdapter(requireContext(), images);
        viewPager.setAdapter(adapter);
        ImageView arrowLeft = view.findViewById(R.id.arrowLeft);
        ImageView arrowRight = view.findViewById(R.id.arrowRight);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                for (int i = 0; i < imagesContainer.getChildCount(); i++) {
                    View child = imagesContainer.getChildAt(i);
                    if (child instanceof MaterialCardView) {
                        MaterialCardView card =
                                (MaterialCardView) child;

                        if (i == position) {
                            card.setStrokeColor(getResources().getColor(R.color.primary2));
                            card.setStrokeWidth(6);
                        } else {
                            card.setStrokeColor(Color.parseColor("#E0E0E0"));
                            card.setStrokeWidth(3);
                        }
                    }
                }
            }
        });

        arrowLeft.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos > 0) viewPager.setCurrentItem(pos - 1, true);
        });

        arrowRight.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < images.size() - 1) viewPager.setCurrentItem(pos + 1, true);
        });

        imagesContainer.removeAllViews();

        for (int i = 0; i < images.size(); i++) {
            String url = images.get(i);
            if (url != null && !url.isEmpty() && !url.equals("null")) {

                com.google.android.material.card.MaterialCardView cardView = new com.google.android.material.card.MaterialCardView(requireContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
                params.setMargins(12, 4, 12, 4);
                cardView.setLayoutParams(params);

                cardView.setRadius(15f); // زوايا دائرية بسيطة
                cardView.setCardElevation(0f); // إلغاء الظل
                cardView.setStrokeWidth(4); // سمك البرواز (زد الرقم لزيادة الوضوح)
                cardView.setStrokeColor(getResources().getColor(android.R.color.darker_gray)); // لون افتراضي
                cardView.setTag(i); // لتسهيل التعرف عليه عند التبديل

                ImageView thumb = new ImageView(requireContext());
                thumb.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(requireContext())
                        .load(url)
                        .centerCrop()
                        .placeholder(R.drawable.empty2)
                        .into(thumb);

                cardView.addView(thumb);

                final int index = i;
                cardView.setOnClickListener(v -> viewPager.setCurrentItem(index, true));

                if (i == viewPager.getCurrentItem()) {
                    cardView.setStrokeColor(getResources().getColor(R.color.primary2)); // اللون المحدد (أصفر/ذهبي)
                    cardView.setStrokeWidth(6);
                } else {
                    cardView.setStrokeColor(Color.parseColor("#E0E0E0")); // لون رمادي فاتح
                    cardView.setStrokeWidth(3);
                }


                imagesContainer.addView(cardView);
            }
        }
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }
        );

        return view;
    }
}
