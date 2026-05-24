package com.example.musafir;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//import jp.wasabeef.blurry.Blurry;

public class TripDetailsFragment extends Fragment {

    String BASE_URL = UserUtils.BASE_URL;

    public TripDetailsFragment() {
        // Required empty public constructor
    }

    private CharSequence oldTitle;
    private Drawable oldNavigationIcon;

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false); // ❌ يخفي سهم الرجوع
        }
    }

    TextView reception_car, tvPrice, requestIds, tvTripTitle, tvTripStatus, tvTripDate, vehicle_type_name,
            tvTripSeats, tvTripNotes, tvTripHistory, tripLocation, tvTripNotesDriver, cancellationReason,
            tvOrderTime;
    LinearLayout notes, notesdriver, cancellationReasonCon, requestContainer, reception_car_con;
    //    ProgressBar progressBar;
    ScrollView detailsCard;
    CardView content_price, cardRequest;

    LottieAnimationView lottieWave;
//    Button cancelPrice;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        pending =1 accepted=2 cancelled=3 closed =4 expired=5
        View itemView = inflater.inflate(R.layout.fragment_trip_details, container, false);
        setHasOptionsMenu(true);
        tvTripNotesDriver = itemView.findViewById(R.id.tvTripNotesDriver);
        reception_car = itemView.findViewById(R.id.reception_car);
        notes = itemView.findViewById(R.id.notes);
        tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
        reception_car_con = itemView.findViewById(R.id.reception_car_con);
        cardRequest = itemView.findViewById(R.id.cardRequest);
        requestContainer = itemView.findViewById(R.id.requestContainer);
        notesdriver = itemView.findViewById(R.id.notesdriver);
        tvTripNotes = itemView.findViewById(R.id.tvTripNotes);
        tvTripHistory = itemView.findViewById(R.id.tvTripHistory);
        content_price = itemView.findViewById(R.id.content_price);
//        cancelPrice = itemView.findViewById(R.id.cancelPrice);
        tvPrice = itemView.findViewById(R.id.tvPrice);
        tvTripSeats = itemView.findViewById(R.id.tvTripSeats);
        tvTripDate = itemView.findViewById(R.id.tvTripDate);
        requestIds = itemView.findViewById(R.id.requestId);
        tripLocation = itemView.findViewById(R.id.tripLocation);
        tvTripTitle = itemView.findViewById(R.id.tvTripTitle);
        tvTripStatus = itemView.findViewById(R.id.tvTripStatus);
        lottieWave = itemView.findViewById(R.id.lottieWaveTripD);
        detailsCard = itemView.findViewById(R.id.detailsCardTrip);
        vehicle_type_name = itemView.findViewById(R.id.vehicle_type_name);
        cancellationReason = itemView.findViewById(R.id.cancellationReason);
        cancellationReasonCon = itemView.findViewById(R.id.cancellationReasonCon);
        String tripIdValue = getArguments().getString("related_object_id");
        ImageView arrowIcon = itemView.findViewById(R.id.arrowIcon);
        cardRequest.setOnClickListener(v -> {
            if (requestContainer.getVisibility() == GONE) {
                requestContainer.setVisibility(View.VISIBLE);
                arrowIcon.setImageResource(R.drawable.baseline_keyboard_arrow_up_24);
            } else {
                requestContainer.setVisibility(GONE);
                arrowIcon.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
            }
        });
//        cancelPrice.setOnClickListener(v -> {
//            showCancelReasonDialog(tripIdValue);
//        });
        if (getArguments() != null) {
            fetchTripDetails(tripIdValue);
        }
        return itemView;
    }

    private AlertDialog exitDialog;


    private void fetchTripDetails(String tripId) {
        String url = BASE_URL + "trip-requests/?request_id=" + tripId;
        lottieWave.playAnimation();
        lottieWave.setVisibility(View.VISIBLE);
        detailsCard.setVisibility(GONE);

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        @SuppressLint("SetTextI18n")
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        lottieWave.setVisibility(GONE);
                        lottieWave.cancelAnimation();
                        detailsCard.setVisibility(View.VISIBLE);

                        JSONObject responseObject = new JSONObject(response.toString());
                        JSONArray resultsArray = responseObject.getJSONArray("results");
                        if (resultsArray.length() > 0) {
                            JSONObject trip = resultsArray.getJSONObject(0);
                            String requestId = trip.getString("request_id");
                            String startCity = trip.getString("start_city_name");
                            String endCity = trip.getString("end_city_name");
                            String date = trip.getString("preferred_departure_date");
                            String request_status = trip.getString("request_status");
                            String additional_notes = trip.getString("additional_notes");
                            String cancellation_reason = trip.getString("cancellation_reason");
                            String driver_notes = trip.getString("driver_notes");
                            int number_of_seats = trip.getInt("number_of_seats");
                            String dt_no = trip.optString("dt_display", "");
                            String price = trip.optString("price", "");
                            String vehicletypename = trip.optString("vehicle_type_name", "");
                            String address = trip.optString("address", "");
                            int v_reception_car = trip.getInt("reception_car");
                            int number_status = trip.getInt("number_status");
                            String creation_date = trip.optString("creation_date", "");

                            if (creation_date != null && !creation_date.isEmpty()) {
                                tvOrderTime.setText(UserUtils.getTimeAgo(creation_date));
                            } else {
                                tvOrderTime.setText("منذ قليل");
                            }
                            requestContainer.removeAllViews();

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


                            vehicle_type_name.setText(vehicletypename);
                            reception_car.setText(address);
                            int privates = trip.getInt("private");
                            if (additional_notes.isEmpty()) {
                                notes.setVisibility(GONE);
                            } else {
                                notes.setVisibility(View.VISIBLE);
                            }

                            if (v_reception_car == 0) {
                                reception_car_con.setVisibility(GONE);
                            } else {
                                reception_car_con.setVisibility(View.VISIBLE);
                            }
                            if (price.isEmpty() || price.equals("null")) {
                                content_price.setVisibility(GONE);
//                                cancelPrice.setVisibility(View.GONE);
                            } else {
                                content_price.setVisibility(View.VISIBLE);
//                                cancelPrice.setVisibility(View.VISIBLE);
                                tvPrice.setText(price);
                            }
                            if (driver_notes.isEmpty()) {
                                notesdriver.setVisibility(GONE);
                            } else {
                                notesdriver.setVisibility(View.VISIBLE);
                            }
                            if (cancellation_reason != null && !cancellation_reason.isEmpty()) {
                                String fullText = additional_notes + "\nسبب الإلغاء:\n" + cancellation_reason;

                                SpannableString spannable = new SpannableString(fullText);

                                int start = fullText.indexOf("سبب الإلغاء:");
                                int end = start + "سبب الإلغاء:".length();

                                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#B91C1C")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                tvTripNotes.setText(spannable);
                            } else {
                                tvTripNotes.setText(additional_notes);
                            }

                            if (privates == 0) {
                                tripLocation.setText("تشاركية");
                            } else {
                                tripLocation.setText("خاصة");
                            }
                            String status = request_status;
                            GradientDrawable background = (GradientDrawable) tvTripStatus.getBackground();
                            switch (status) {
                                case "verified":
                                    tvTripStatus.setText("مؤكد");
                                    tvTripStatus.setTextColor(Color.parseColor("#2E7D32")); // أخضر غامق
                                    background.setColor(Color.parseColor("#C8E6C9")); // أخضر فاتح
                                    break;

                                case "cancelled":
                                case "cancelled_by_driver":
                                case "cancelled_by_passenger":
                                    tvTripStatus.setText("ملغي");
                                    tvTripStatus.setTextColor(Color.parseColor("#ef4444"));
                                    background.setColor(Color.parseColor("#fdecec")); // أحمر
                                    break;

                                case "pending":
                                    tvTripStatus.setText("قيد المعالجة");
                                    tvTripStatus.setTextColor(Color.parseColor("#CC9407"));
                                    background.setColor(Color.parseColor("#fef5e6")); // برتقالي
                                    break;

                                case "expired":
                                    tvTripStatus.setText("منتهي");
                                    tvTripStatus.setTextColor(Color.parseColor("#9CA3AF")); // رمادي داكن
                                    background.setColor(Color.parseColor("#F3F4F6")); // رمادي فاتح
                                    break;


                                default:
                                    tvTripStatus.setText("مغلق");
                                    tvTripStatus.setTextColor(Color.parseColor("#1E3A8A")); // نص أزرق داكن
                                    background.setColor(Color.parseColor("#DBEAFE")); // خلفية أزرق فاتح

                                    break;
                            }

                            tvTripTitle.setText("من " + startCity + " إلى " + endCity);
                            tvTripNotesDriver.setText(driver_notes);
                            requestIds.setText("رقم الطلب: " + requestId);
                            tvTripDate.setText(date);
                            if (number_of_seats == 1) {
                                tvTripSeats.setText("1 مقعد");
                            } else if (number_of_seats >= 11) {
                                tvTripSeats.setText(number_of_seats + " مقعد");
                            } else {
                                tvTripSeats.setText(number_of_seats + " مقاعد");
                            }
//                            tvTripSeats.setText(String.valueOf(number_of_seats));
                            tvTripHistory.setText(dt_no);
                            if (additional_notes != null && !additional_notes.isEmpty()) {
                                tvTripNotes.setText(additional_notes);
                                notes.setVisibility(View.VISIBLE);
                            } else {
                                notes.setVisibility(GONE);
                            }
                            if (cancellation_reason.isEmpty()) {
                                cancellationReasonCon.setVisibility(GONE);
                            } else {
                                cancellationReasonCon.setVisibility(View.VISIBLE);
                                cancellationReason.setText(cancellation_reason);

                            }
                        }
                    } catch (JSONException e) {
                        UserUtils.sendLog(getContext(), "fetchTripDetails", e.toString(), e.toString(), "Trip Details Fragment");

                    }
                },
                error -> {
                    detailsCard.setVisibility(GONE);
                    lottieWave.setVisibility(GONE);
                    lottieWave.cancelAnimation();
                    UserUtils.sendLog(getContext(), "fetchTripDetails", error.toString(), error.toString(), "Trip Details Fragment");
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        queue.add(request);
    }
}