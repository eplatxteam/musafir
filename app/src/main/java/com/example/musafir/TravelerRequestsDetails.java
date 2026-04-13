package com.example.musafir;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TravelerRequestsDetails extends Fragment {

    TextView tvNotes, nameCompany, numberSeat, nameReq, tvTripStatus, name_passenger;
    ImageView iconReq;
    CardView countryContent;
    LinearLayout notesContent;
    LinearLayout requestContainer, containerPassengers;
    //    ProgressBar progressBar;
    CardView cardRequest;
    String BASE_URL = UserUtils.BASE_URL;
    LottieAnimationView lottieWaveVeh;
    LinearLayout container_req;

    public TravelerRequestsDetails() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomePage) {
            ActionBar actionBar = ((HomePage) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_traveler_requests_details, container, false);
        tvNotes = view.findViewById(R.id.tvNotes);
        nameCompany = view.findViewById(R.id.nameCompany);
        notesContent = view.findViewById(R.id.notesContent);
        countryContent = view.findViewById(R.id.countryContent);
        tvTripStatus = view.findViewById(R.id.tvTripStatus);
        numberSeat = view.findViewById(R.id.numberSeat);
        nameReq = view.findViewById(R.id.nameReq);
        iconReq = view.findViewById(R.id.iconReq);
        cardRequest = view.findViewById(R.id.cardRequest);
        requestContainer = view.findViewById(R.id.requestContainer);
        containerPassengers = view.findViewById(R.id.containerPassengers);
        lottieWaveVeh = view.findViewById(R.id.lottieWaveVeh);
        container_req = view.findViewById(R.id.container_req);
        ImageView arrowIcon = view.findViewById(R.id.arrowIcon);
        cardRequest.setOnClickListener(v -> {
            if (requestContainer.getVisibility() == GONE) {
                requestContainer.setVisibility(VISIBLE);
                arrowIcon.setImageResource(R.drawable.baseline_keyboard_arrow_up_24);
            } else {
                requestContainer.setVisibility(GONE);
                arrowIcon.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
            }
        });
//        name_passenger = view.findViewById(R.id.name_passenger);
// استلام البيانات من الـ Bundle

        Bundle bundle = getArguments();
        if (bundle == null) return null;

        String relatedObjectId = bundle.getString("related_object_id", null);

        if (relatedObjectId != null) {
            fetchTravelerRequestById(relatedObjectId);
        } else {
            bindFromBundle(bundle);
        }


//        Bundle bundle = getArguments();
//        if (bundle != null) {
//
//
//        }

        return view;
    }

    private void showLoading(boolean show) {
        if (show) {
            lottieWaveVeh.setVisibility(VISIBLE);
            lottieWaveVeh.playAnimation();
        } else {
            lottieWaveVeh.setVisibility(View.GONE);
            lottieWaveVeh.cancelAnimation();
        }
    }

    private void fetchTravelerRequestById(String trId) {
        showLoading(true);
        container_req.setVisibility(GONE);
        String url = BASE_URL + "TravelerRequests/?tr_id=" + trId;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject item = response.getJSONObject(0);
                            bindFromApi(item);
                            showLoading(false);
                            container_req.setVisibility(VISIBLE);
                        }
                    } catch (JSONException e) {
                        showLoading(false);
                        container_req.setVisibility(GONE);
                    }
                },
                error -> {
                    showLoading(false);
                    container_req.setVisibility(GONE);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = requireContext()
//                        .getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    private void bindFromApi(JSONObject item) throws JSONException {

        String type_tr_name = item.optString("type_tr_name", "");
        String tr_status = item.optString("tr_status", "");
        int number_passenger = item.optInt("number_passenger", 0);
        String type_icon = item.optString("type_icon", "");
        String notes = item.optString("notes", "");
        String country = item.optString("country", "");
//        String name_passenger = item.optString("name_passenger", "");
        int number_status = item.optInt("number_status", 0);
        requestContainer.removeAllViews(); // تنظيف الحاوية قبل الإضافة

        String[] stagesNames;
        if (number_status == 0) {
            stagesNames = new String[]{"ملغى"};
            requestContainer.setVisibility(View.VISIBLE);
        } else {
            stagesNames = new String[]{"قيد المعالجة", "مؤكد"};
            requestContainer.setVisibility(View.VISIBLE);
        }
        requestContainer.removeAllViews();

        for (int i = 0; i < stagesNames.length; i++) {
            View stepView = LayoutInflater.from(getContext()).inflate(R.layout.item_request, requestContainer, false);

            TextView cityNameText = stepView.findViewById(R.id.statusName);
            ImageView circleIcon = stepView.findViewById(R.id.circleIcon);
            View lineView = stepView.findViewById(R.id.lineView);

            cityNameText.setText(stagesNames[i]);
            if (number_status == 0) {
                requestContainer.setVisibility(GONE);
                cardRequest.setVisibility(GONE);
            } else {
                requestContainer.setVisibility(GONE);
                if (i < number_status) {
                    circleIcon.setImageResource(R.drawable.check1);
                    lineView.setBackgroundResource(R.drawable.bg_logo);
                    cityNameText.setTextColor(getResources().getColor(android.R.color.black));
                } else {
                    circleIcon.setImageResource(R.drawable.uncheck1);
                    lineView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    cityNameText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
            }
            if (i == stagesNames.length - 1) {
                lineView.setVisibility(GONE);
            }

            requestContainer.addView(stepView);
        }


        ArrayList<String> passengerNames = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            String nameKey = "name_passenger" + i;
            String name = item.optString(nameKey, "").trim();
            if (!name.isEmpty()) {
                passengerNames.add(name);
            }
        }


        containerPassengers.removeAllViews();

        for (String name : passengerNames) {
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tv.setBackgroundResource(R.color.gray2);
            tv.setTextColor(getResources().getColor(R.color.text));
            tv.setTextSize(16);
            tv.setText(name);

            int paddingDp = 10;
            final float scale = getResources().getDisplayMetrics().density;
            int paddingPx = (int) (paddingDp * scale + 0.5f);
            tv.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
            params.setMargins(0, 10, 0, 10);
            tv.setLayoutParams(params);

            containerPassengers.addView(tv);
        }


        nameReq.setText(type_tr_name);
        tvTripStatus.setText(tr_status);
        setStatusStyle2(tvTripStatus, tr_status);
        int number = 0;
        if (!item.isNull("number_passenger")) {
            number = item.optInt("number_passenger", 0);
        }

        String text;
        if (number == 0) {
            text = "لا يوجد أشخاص";
        } else if (number == 1) {
            text = "1 شخص";
        } else {
            text = number + " أشخاص";
        }
        numberSeat.setText(text);

        if (notes.isEmpty()) {
            notesContent.setVisibility(View.GONE);
        } else {
            notesContent.setVisibility(VISIBLE);
            tvNotes.setText(notes);
        }
        if (country.isEmpty()) {
            countryContent.setVisibility(View.GONE);
        } else {
            countryContent.setVisibility(VISIBLE);
            nameCompany.setText(country);
        }

        int iconRes = getResources().getIdentifier(type_icon, "drawable", getContext().getPackageName());
        if (iconRes != 0) {
            iconReq.setImageResource(iconRes);
        }

    }

    private void bindFromBundle(Bundle bundle) {
        String type_tr_name = bundle.getString("type_tr_name", "");
        String tr_status = bundle.getString("tr_status", "");
        String number_passenger = bundle.getString("number_passenger", "");
        String type_icon = bundle.getString("type_icon", "");
        String notes = bundle.getString("notes", "");
        String country = bundle.getString("country", "");
        int number_status = bundle.getInt("number_status", 0);

        requestContainer.removeAllViews(); // تنظيف الحاوية قبل الإضافة

        String[] stagesNames = {"قيد المعالجة", "مؤكد"};
        if (number_status == 0) {
            requestContainer.setVisibility(GONE);
        } else {
            for (int i = 0; i < stagesNames.length; i++) {
                View stepView = LayoutInflater.from(getContext()).inflate(R.layout.item_request, requestContainer, false);

                TextView cityNameText = stepView.findViewById(R.id.statusName);
                ImageView circleIcon = stepView.findViewById(R.id.circleIcon);
                View lineView = stepView.findViewById(R.id.lineView);

                cityNameText.setText(stagesNames[i]);

                if (i < number_status) {
                    // المراحل المكتملة أو المرحلة الحالية
                    circleIcon.setImageResource(R.drawable.check1);
                    lineView.setBackgroundResource(R.drawable.bg_logo);
                    cityNameText.setTextColor(getResources().getColor(android.R.color.black));
                } else {
                    // المراحل غير المكتملة
                    circleIcon.setImageResource(R.drawable.uncheck1);
                    lineView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    cityNameText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }

                // إخفاء الخط للمرحلة الأخيرة
                if (i == stagesNames.length - 1) {
                    lineView.setVisibility(GONE);
                }

                requestContainer.addView(stepView);
            }
        }


        ArrayList<String> passengerNames = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            String name = getArguments().getString("name_passenger" + i, "");
            if (!name.isEmpty()) {
                passengerNames.add(name);
            }
        }

        for (String name : passengerNames) {
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tv.setBackgroundResource(R.color.gray2);
            tv.setTextColor(getResources().getColor(R.color.text));
            tv.setTextSize(16);
            tv.setText(name);

            int paddingDp = 10;
            final float scale = getResources().getDisplayMetrics().density;
            int paddingPx = (int) (paddingDp * scale + 0.5f);
            tv.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
            params.setMargins(0, 10, 0, 10);
            tv.setLayoutParams(params);

            containerPassengers.addView(tv);
        }


        nameReq.setText(type_tr_name);
        tvTripStatus.setText(tr_status);
        setStatusStyle(tvTripStatus, tr_status);

        numberSeat.setText(number_passenger + "");
        if (notes.isEmpty()) {
            notesContent.setVisibility(View.GONE);
        } else {
            notesContent.setVisibility(VISIBLE);
            tvNotes.setText(notes);
        }
        if (country.isEmpty()) {
            countryContent.setVisibility(View.GONE);
        } else {
            countryContent.setVisibility(VISIBLE);
            nameCompany.setText(country);
        }

        int iconRes = getResources().getIdentifier(type_icon, "drawable", getContext().getPackageName());
        if (iconRes != 0) {
            iconReq.setImageResource(iconRes);
        }
    }

    private void setStatusStyle(TextView txtStatus, String statusArabic) {
        GradientDrawable background = (GradientDrawable) txtStatus.getBackground();

        switch (statusArabic) {
            case "مؤكد":
                txtStatus.setTextColor(Color.parseColor("#2E7D32")); // أخضر
                background.setColor(Color.parseColor("#C8E6C9"));
                break;
            case "ملغي":
                txtStatus.setTextColor(Color.parseColor("#EF4444")); // أحمر
                background.setColor(Color.parseColor("#FDECEC"));
                break;
            case "قيد المعالجة":
                txtStatus.setTextColor(Color.parseColor("#CC9407")); // برتقالي
                background.setColor(Color.parseColor("#FEF5E6"));
                break;
        }
    }

    private void setStatusStyle2(TextView txtStatus, String statusArabic) {
        GradientDrawable background = (GradientDrawable) txtStatus.getBackground();

        switch (statusArabic) {
            case "verified":
                txtStatus.setText("مؤكد");
                txtStatus.setTextColor(Color.parseColor("#2E7D32")); // أخضر
                background.setColor(Color.parseColor("#C8E6C9"));
                break;
            case "cancel":
                txtStatus.setText("ملغي");
                txtStatus.setTextColor(Color.parseColor("#EF4444")); // أحمر
                background.setColor(Color.parseColor("#FDECEC"));
                break;
            case "pending":
                txtStatus.setText("قيد المعالجة");
                txtStatus.setTextColor(Color.parseColor("#CC9407")); // برتقالي
                background.setColor(Color.parseColor("#FEF5E6"));
                break;
        }
    }
}