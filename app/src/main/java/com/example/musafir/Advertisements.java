package com.example.musafir;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class Advertisements extends Fragment {

    TextView cardTitle, detailsText;
    ScrollView scrollContent;
    PhotoView vehicleImage;
    LinearLayout noInternet;

    String BASE_URL = UserUtils.BASE_URL;
    String ImageUrl = UserUtils.ImageUrl;

    LottieAnimationView lottieWave;

    String imgId = "";

    public static Advertisements newInstance(String imgId) {
        Advertisements fragment = new Advertisements();
        Bundle args = new Bundle();
        args.putString("img_id", imgId);
        fragment.setArguments(args);
        return fragment;
    }

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

        View view = inflater.inflate(R.layout.activity_advertisements, container, false);
        setHasOptionsMenu(true);
        cardTitle = view.findViewById(R.id.cardTitle);
        detailsText = view.findViewById(R.id.detailsText);
        scrollContent = view.findViewById(R.id.scrollContent);
        vehicleImage = view.findViewById(R.id.vehicleImage);
        lottieWave = view.findViewById(R.id.lottieWaveAd);
        noInternet = view.findViewById(R.id.noInternet);



        if (getArguments() != null) {
            imgId = getArguments().getString("img_id", "");
        }

        if (!UserUtils.isNetworkAvailable(requireContext())) {
            noInternet.setVisibility(View.VISIBLE);
            scrollContent.setVisibility(View.GONE);
            lottieWave.cancelAnimation();
            lottieWave.setVisibility(View.GONE);
        } else {
            if (!imgId.isEmpty()) {
                loadAdvertisement(imgId);
            }
        }

        return view;
    }


    private void loadAdvertisement(String imgId) {
        lottieWave.playAnimation();
        lottieWave.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);

        String url = BASE_URL + "ImagesHome/" + imgId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {

                        String title = response.optString("title", "بدون عنوان");
                        String details = response.optString("details", "لا توجد تفاصيل");
                        String imgName = response.optString("img_name", "");

                        cardTitle.setText(title);
                        detailsText.setText(details);

                        if (!imgName.isEmpty() && !imgName.equals("null")) {
                            String fullUrl = ImageUrl + "/media/" + imgName;


                                    Glide.with(requireContext())
                                            .load(fullUrl)
                                            .placeholder(R.drawable.empty2)
                                            .error(R.drawable.empty2)
                                            .into(vehicleImage);

//                            vehicleImage.setOnClickListener(v -> {
//                                ArrayList<String> images = new ArrayList<>();
//                                images.add(fullUrl);
//
//                                FullscreenImageFragment fragment = FullscreenImageFragment.newInstance(images, title);
//
//                                getParentFragmentManager().beginTransaction()
//                                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
//                                        .add(android.R.id.content, fragment)
//                                        .addToBackStack(null)
//                                        .commit();
//                            });

                        }

                        lottieWave.setVisibility(View.GONE);
                        lottieWave.cancelAnimation();
                        scrollContent.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        lottieWave.setVisibility(View.GONE);
                        lottieWave.cancelAnimation();
                        if (isAdded()) {
                            UserUtils.sendLog(requireContext(),
                                    "loadAdvertisement",
                                    e.toString(),
                                    e.toString(),
                                    "AdvertisementsFragment");
                        }
                    }
                },
                error -> {
                    lottieWave.setVisibility(View.GONE);
                    lottieWave.cancelAnimation();
                    if (isAdded()) {
                        UserUtils.sendLog(requireContext(),
                                "loadAdvertisement",
                                error.toString(),
                                error.toString(),
                                "AdvertisementsFragment");
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }
}
