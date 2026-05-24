package com.example.musafir;

import static android.app.Activity.RESULT_OK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import jp.wasabeef.blurry.Blurry;

//import jp.wasabeef.blurry.Blurry;

public class MoreDetails extends Fragment {

    TextView daysRemaining, availableSeatsText, dateText, driver_names, driver_notes, locationText, locationDetails, locationDetails2, timeText, driver_companys,
            textPrice, originalPrice, makeText, colorText, yearText, modelText, vehicle_name, bookingText, IdTrip, tvStatus, trip_vip;
    Button btnBook, btnBack, btnBackArrow;
    View linevehicle_name;
    RatingBar ratingText;
    int tripId, availableSeats;
    LinearLayout notcon, routeContainer, vehicle_Container, container_address, container_address2;
    CardView cardRoute, vehicle_card;
    String pricePerSeat;
    //    ProgressBar progressBar;
    int pickupOrders;
    int company_no;
    String driver_id, Attendance_time;
    int dropoffOrders;
    String BASE_URL = UserUtils.BASE_URL;
    String ImageUrl = UserUtils.ImageUrl;

    ImageView vehicleImage, iconPrice;
    Button btnCallDriver;
    int v_reception_car = 0;

    ScrollView scrollContents;
    String car_code;
    String car_codes;
    String car_codes_id;
    String discountPricePerSeat;
    String LocationTrip, DateTrip;
    String trip_id2;
    int visa_required;

    public MoreDetails() {
        // Required empty constructor
    }

    String deviceId = UserUtils.getDeviceID(getContext());
    String deviceInfo = UserUtils.getDeviceInfo();

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    LinearLayout servicesSection;
    ChipGroup servicesChipGroup;

    LottieAnimationView lottieWave;

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
        View view = inflater.inflate(R.layout.activity_more_details, container, false);
        setHasOptionsMenu(true);
        daysRemaining = view.findViewById(R.id.daysRemaining);
        availableSeatsText = view.findViewById(R.id.availableSeatsText);
        textPrice = view.findViewById(R.id.textPrice);
        trip_vip = view.findViewById(R.id.trip_vip);
        dateText = view.findViewById(R.id.dateText);
        iconPrice = view.findViewById(R.id.iconPrice);
        linevehicle_name = view.findViewById(R.id.linevehicle_name);
        ratingText = view.findViewById(R.id.ratingText);
        driver_names = view.findViewById(R.id.driver_name);
        bookingText = view.findViewById(R.id.bookingText);
        driver_notes = view.findViewById(R.id.driver_notes);
        notcon = view.findViewById(R.id.notcon);
        locationText = view.findViewById(R.id.locationText);
        timeText = view.findViewById(R.id.timeText);
        IdTrip = view.findViewById(R.id.IdTrip);
        driver_companys = view.findViewById(R.id.driver_companys);
        originalPrice = view.findViewById(R.id.originalPrice);
        modelText = view.findViewById(R.id.modelText);
        vehicle_name = view.findViewById(R.id.vehicle_name);
        colorText = view.findViewById(R.id.colorText);
        yearText = view.findViewById(R.id.yearText);
        makeText = view.findViewById(R.id.makeText);
        scrollContents = view.findViewById(R.id.scrollContent);
        locationDetails = view.findViewById(R.id.locationDetails);
        locationDetails2 = view.findViewById(R.id.locationDetails2);
        routeContainer = view.findViewById(R.id.routeContainer);
        vehicle_card = view.findViewById(R.id.vehicle_card);
        cardRoute = view.findViewById(R.id.cardRoute);
        vehicle_Container = view.findViewById(R.id.vehicle_Container);
        container_address = view.findViewById(R.id.container_address);
        container_address2 = view.findViewById(R.id.container_address2);
        tvStatus = view.findViewById(R.id.tvStatus);
        Button btn_whatsapp = view.findViewById(R.id.btn_whatsapp);
        servicesChipGroup = view.findViewById(R.id.servicesChipGroup);
        servicesSection = view.findViewById(R.id.services_section);
        btnBook = view.findViewById(R.id.btnBook);
        vehicleImage = view.findViewById(R.id.vehicleImage);
        btnCallDriver = view.findViewById(R.id.btn_call_driver);
        ImageView arrowIcon = view.findViewById(R.id.arrowIcon);
        ImageView arrowIcon2 = view.findViewById(R.id.arrowIcon2);
        DBHelper dbHelper = new DBHelper(getContext());
        TextView NotePrice = view.findViewById(R.id.NotePrice);
        NotePrice.setText(UserUtils.getMessageFromLocalNew(361, dbHelper));
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

        btn_whatsapp.setOnClickListener(v -> {
            String countryCode = prefs.getString("country_code", "967785050270");

            int messageId = "YE".equals(countryCode) ? 349 : 362;
            String phone = UserUtils.getMessageFromLocalNew(messageId, dbHelper);
            String message = UserUtils.getMessageFromLocalNew(334, dbHelper) + " " + trip_id2;

            String url = "https://wa.me/" + phone + "?text=" + Uri.encode(message);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            intent.setPackage("com.whatsapp");

            try {
                v.getContext().startActivity(intent);
            } catch (Exception e) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                v.getContext().startActivity(browserIntent);
            }
        });
        lottieWave = view.findViewById(R.id.lottieWaveDetails);
        cardRoute.setOnClickListener(v -> {
            if (routeContainer.getVisibility() == View.GONE) {
                routeContainer.setVisibility(View.VISIBLE);
                locationDetails.setVisibility(View.VISIBLE);
                locationDetails2.setVisibility(View.VISIBLE);
                container_address.setVisibility(View.VISIBLE);
                container_address2.setVisibility(View.VISIBLE);
                arrowIcon.setImageResource(R.drawable.baseline_keyboard_arrow_up_24);
            } else {
                routeContainer.setVisibility(View.GONE);
                locationDetails.setVisibility(View.GONE);
                locationDetails2.setVisibility(View.GONE);
                container_address.setVisibility(View.GONE);
                container_address2.setVisibility(View.GONE);
                arrowIcon.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
            }
        });

        vehicle_card.setOnClickListener(v -> {
            if (vehicle_Container.getVisibility() == View.GONE) {
                vehicle_Container.setVisibility(View.VISIBLE);
                arrowIcon2.setImageResource(R.drawable.baseline_keyboard_arrow_up_24);
            } else {
                vehicle_Container.setVisibility(View.GONE);
                arrowIcon2.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
            }
        });

        if (getArguments() != null) {
            tripId = getArguments().getInt("trip_id", -1);
        }

        if (tripId != -1) {
            FetchData(tripId);
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Toolbar toolbar = requireActivity().findViewById(R.id.main_toolbar);
        toolbar.setNavigationIcon(null);
//        toolbar.setTitle(R.string.app_name);
    }

    @SuppressLint("SetTextI18n")
    private void FetchData(int p_tripId) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                lottieWave.playAnimation();
                lottieWave.setVisibility(View.VISIBLE);
                scrollContents.setVisibility(View.GONE);
                btnCallDriver.setVisibility(View.GONE);
            });
        }
        String apiUrl = BASE_URL + "trips/" + p_tripId + "/";
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Accept", "application/json");
                String token2 = "1";

                if (token2 != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token2);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject trip = new JSONObject(result.toString());

                JSONObject vehicle = trip.getJSONObject("vehicle_info");
//                String vehicleMake = vehicle.optString("make");
                String vehicleModel = vehicle.optString("model");
//                String vehicleColor = vehicle.optString("color");
                final String[] vehicleImageUrl = {vehicle.optString("vehicle_image")};
                String vehicleYear = vehicle.optString("year");

                pricePerSeat = trip.optString("price_per_seat");
                availableSeats = trip.optInt("remaining_seats");
                Attendance_time = trip.optString("Attendance_time");

                String notes = trip.optString("additional_notes");
                String trip_id = trip.optString("trip_id");
                trip_id2 = trip.optString("trip_id2");
                String driver_name = trip.optString("driver_name");
                discountPricePerSeat = trip.optString("discount_price");
                pickupOrders = trip.optInt("start_point_order");
                dropoffOrders = trip.optInt("end_point_order");
                String driver_rating = trip.optString("driver_rating");
                car_code = trip.getString("car_code");
                String driver_number = trip.getString("driver_number");
                car_codes = trip.optString("car_codes", "YER");
                String company_name = trip.getString("company_name");
                int vtrip_vip = trip.optInt("trip_vip", 0);
                car_codes_id = trip.getString("car_codes_id");
                String dt_display = trip.getString("dt_display");
                String departure_time = trip.getString("departure_time");
                String departure_date = trip.optString("departure_date");
                LocationTrip = trip.optString("route_name");
                DateTrip = trip.optString("departure_date");
                String show_number = trip.optString("show_number");
                driver_id = trip.getString("driver");
                company_no = trip.optInt("company_no", 0);
                visa_required = trip.optInt("visa_required", 0);
                String vehicleType = vehicle.optString("vehicle_type");
                String trip_status = trip.optString("trip_status", "");
                String route_city_ids = trip.optString("route_city", "");
                String vehicleName = vehicle.optString("vehicle_name", "");
                List<String> cityNames = new ArrayList<>();
                GradientDrawable background = (GradientDrawable) tvStatus.getBackground();
                passport_required = trip.optInt("passport_required", 0);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        String status = trip_status;
                        switch (status) {
                            case "in Way":
                                tvStatus.setText("في الطريق");
                                tvStatus.setTextColor(Color.parseColor("#2E7D32")); // أخضر غامق
                                background.setColor(Color.parseColor("#C8E6C9")); // أخضر فاتح
                                break;

                            case "cancelled":
                                tvStatus.setText("ملغية");
                                tvStatus.setTextColor(Color.parseColor("#ef4444"));
                                background.setColor(Color.parseColor("#fdecec")); // أحمر
                                break;

                            case "scheduled":
                                tvStatus.setText("مجدولة");
                                tvStatus.setTextColor(Color.parseColor("#CC9407"));
                                background.setColor(Color.parseColor("#fef5e6")); // برتقالي
                                break;

                            default:
                                tvStatus.setText("مغلقة");
                                tvStatus.setTextColor(Color.parseColor("#1E3A8A")); // نص أزرق داكن
                                background.setColor(Color.parseColor("#DBEAFE")); // خلفية أزرق فاتح

                                break;
                        }
//                if (!route_city_ids.isEmpty()) {
//                    String[] ids = route_city_ids.split(",");
//                    for (String idStr : ids) {
//                        idStr = idStr.trim();
//                        if (!idStr.isEmpty()) {
//                            try {
//                                int cityId = Integer.parseInt(idStr);
//                                String cityName = dbHelper.getCityNameById(cityId); // دالة في DBHelper ترجع الاسم
//                                if (cityName != null) {
//                                    cityNames.add(cityName);
//                                }
//                            } catch (NumberFormatException ignored) {
//                            }
//                        }
//                    }
//                }
                        if (!route_city_ids.isEmpty()) {
                            String[] ids = route_city_ids.split(",");
                            for (String idStr : ids) {
                                idStr = idStr.trim();
                                if (!idStr.isEmpty()) {
                                    try {
                                        int cityId = Integer.parseInt(idStr);
                                        String cityName = dbHelper.getCityNameById(cityId);
                                        if (cityName != null) cityNames.add(cityName);
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }
                        }

                        routeContainer.removeAllViews(); // تنظيف الحاوية قبل الإضافة

                        for (int i = 0; i < cityNames.size(); i++) {
                            View stepView = LayoutInflater.from(getContext()).inflate(R.layout.item_step, routeContainer, false);
                            TextView cityNameText = stepView.findViewById(R.id.cityName);
                            View lineView = stepView.findViewById(R.id.lineView);

                            cityNameText.setText(cityNames.get(i));

                            if (i == cityNames.size() - 1) {
                                lineView.setVisibility(View.GONE);
                            }

                            routeContainer.addView(stepView);
                        }

                        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                        String formattedTime = "";
                        try {
                            Date times = inputFormat.parse(departure_time);
                            formattedTime = outputFormat.format(times);

                            formattedTime = formattedTime.replace("AM", "ص").replace("PM", "م");
                        } catch (ParseException e) {
                            formattedTime = departure_time;
                        }
                        String finalFormattedTime = formattedTime;
                        lottieWave.setVisibility(View.GONE);
                        lottieWave.cancelAnimation();
                        scrollContents.setVisibility(View.VISIBLE);
                        if (!show_number.equals("false")) {
                            btnCallDriver.setVisibility(View.VISIBLE);
                        } else {
                            btnCallDriver.setVisibility(View.GONE);
                        }
                        if (discountPricePerSeat.equals("null")) {
                            originalPrice.setVisibility(View.GONE);
                        } else {
                            originalPrice.setVisibility(View.VISIBLE);
                            originalPrice.setText(discountPricePerSeat + " " + car_codes);
                        }
                        if (notes.equals("null") || notes.isEmpty()) {
                            notcon.setVisibility(View.GONE);
                        } else {
                            notcon.setVisibility(View.VISIBLE);
                            driver_notes.setText(notes);

                        }
                        if (company_name.equals("null")) {
                            driver_companys.setVisibility(View.GONE);
                            driver_names.setTextColor(Color.BLACK);
                            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
                            driver_names.setTypeface(typeface, Typeface.BOLD);
                        } else {
                            driver_companys.setVisibility(View.VISIBLE);
                            driver_companys.setText(company_name);
                        }
                        if (discountPricePerSeat.equals("null") || discountPricePerSeat.isEmpty()) {
                            // لا يوجد خصم
                            textPrice.setVisibility(View.GONE);
                            iconPrice.setVisibility(View.GONE);
                            originalPrice.setVisibility(View.VISIBLE);
                            originalPrice.setText(pricePerSeat + " " + car_codes);
                            originalPrice.setTextColor(Color.BLACK);
                            originalPrice.setPaintFlags(0); // بدون شطب
                        } else {
                            // يوجد خصم
                            textPrice.setVisibility(View.VISIBLE);
                            iconPrice.setVisibility(View.GONE);
                            originalPrice.setVisibility(View.VISIBLE);

                            // السعر الأصلي مع شطب
                            originalPrice.setText(pricePerSeat + " " + car_codes);
                            originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                            textPrice.setText(discountPricePerSeat + " " + car_codes);
                        }
                        String dateTimeString = departure_time;
                        Date tripDateTime = null;

                        try {
                            if (dateTimeString.contains("T")) {
                                SimpleDateFormat isoSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                                tripDateTime = isoSdf.parse(dateTimeString);
                            } else if (dateTimeString.contains(" ")) {
                                SimpleDateFormat spaceSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                tripDateTime = spaceSdf.parse(dateTimeString);
                            } else {
                                SimpleDateFormat fallbackSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                tripDateTime = fallbackSdf.parse(dateTimeString);
                            }
                        } catch (ParseException e) {
                            tripDateTime = new Date();
                        }

                        Date now = new Date();
                        long diffInMillis = tripDateTime.getTime() - now.getTime();
                        long diffDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                        GradientDrawable background2 = (GradientDrawable) daysRemaining.getBackground().mutate();

                        daysRemaining.setTextColor(Color.WHITE);
                        background2.setColor(Color.parseColor("#6EA0B3"));
                        if (diffDays == 1) {
                            daysRemaining.setText("يوم واحد");
                        } else if (diffDays == 0) {
                            long diffHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                            long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60;
                            if (diffHours > 0) {
                                String hourText = (diffHours == 1) ? "ساعة واحدة" : diffHours + " ساعات";
                                daysRemaining.setText(hourText);
                                background2.setColor(Color.parseColor("#6EA0B3"));
//                                daysRemaining.setBackgroundColor(Color.TRANSPARENT); // خلفية عادية
                            } else if (diffMinutes > 0) {
                                String minuteText = (diffMinutes == 1) ? "دقيقة واحدة" : diffMinutes + " دقائق";
                                daysRemaining.setText(minuteText);
                                background2.setColor(Color.parseColor("#6EA0B3"));
//                                daysRemaining.setBackgroundColor(Color.TRANSPARENT); // خلفية عادية
                            } else {
                                daysRemaining.setText("الرحلة بدأت");
                                daysRemaining.setTextColor(Color.parseColor("#af0516")); // أخضر غامق
                                background2.setColor(Color.parseColor("#FFCDD2"));
                            }

                        } else if (diffDays == 2) {
                            daysRemaining.setText("يومان");
                        } else if (diffDays >= 3 && diffDays <= 10) {
                            daysRemaining.setText(diffDays + " أيام");
                        } else {
                            daysRemaining.setText(diffDays + " يوم");
                        }
                        timeText.setText(Attendance_time);
                        IdTrip.setText("رقم الرحلة: " + trip_id2);
                        driver_names.setText(driver_name);
                        if (vtrip_vip == 1) {
                            trip_vip.append(" (VIP)");
                        }

                        JSONArray servicesArray = trip.optJSONArray("trip_services");

                        if (servicesArray != null && servicesArray.length() > 0) {
                            servicesSection.setVisibility(View.VISIBLE);
                            servicesChipGroup.removeAllViews();

                            for (int i = 0; i < servicesArray.length(); i++) {
                                JSONObject serviceObject = servicesArray.optJSONObject(i);

                                if (serviceObject != null) {
                                    String serviceName = serviceObject.optString("ts_name");

                                    Chip chip = new Chip(getContext());
                                    chip.setText(serviceName);

                                    chip.setChipBackgroundColorResource(R.color.card_bg_selector);
                                    chip.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                    chip.setChipIcon(ContextCompat.getDrawable(getContext(), R.drawable.check2));
                                    chip.setChipIconTintResource(R.color.primary);
                                    float iconSizeInDp = 20f;
                                    float density = getResources().getDisplayMetrics().density;
                                    chip.setChipIconSize(iconSizeInDp * density);
                                    chip.setCheckable(false);
                                    chip.setClickable(false);

                                    servicesChipGroup.addView(chip);
                                }
                            }
                        } else {
                            servicesSection.setVisibility(View.GONE);
                        }


//                        makeText.setText(vehicleMake);
//                        colorText.setText(vehicleColor);
                        yearText.setText(vehicleYear);
                        modelText.setText(vehicleType);
                        if (vehicleName != null && !vehicleName.isEmpty() && !vehicleName.equals("null")) {
                            vehicle_name.setVisibility(View.VISIBLE);
                            vehicle_name.setText(vehicleName);
                        } else {
                            vehicle_name.setVisibility(View.GONE);
                            linevehicle_name.setVisibility(View.GONE);
                        }
//                        ratingText.setText(driver_rating);

                        float ratingValue = 0f;
                        if (driver_rating != null && !driver_rating.isEmpty() && !driver_rating.equals("null")) {
                            try {
                                ratingValue = Float.parseFloat(driver_rating);
                            } catch (NumberFormatException e) {
                                throw new RuntimeException(e);
                            }
                        }


                        ratingText.setRating(ratingValue);
                        ratingText.setIsIndicator(true);
                        dateText.setText(departure_date);
                        locationText.setText(trip.optString("route_name"));
                        locationDetails.setText("عنوان الوصول: " + trip.optString("destination_address"));
                        locationDetails2.setText("عنوان الإنطلاق: " + trip.optString("departure_address"));

                        if (availableSeats == 1) {
                            availableSeatsText.setText("1 مقعد متاح");
                        } else if (availableSeats >= 11) {
                            availableSeatsText.setText(availableSeats + " مقعد متاح");
                        } else {
                            availableSeatsText.setText(availableSeats + " مقاعد متاحة");
                        }
//                        availableSeatsText.setText(availableSeats + " مقاعد متاحة");

                        btnCallDriver.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + driver_number));
                            startActivity(intent);
                        });

                        // تحميل الصورة المصغرة
                        if (vehicleImageUrl[0] != null && !vehicleImageUrl[0].isEmpty()) {
                            if (!vehicleImageUrl[0].startsWith("http")) {
                                vehicleImageUrl[0] = ImageUrl + vehicleImageUrl[0];
                            }
                            Glide.with(requireContext())
                                    .load(vehicleImageUrl[0])
                                    .placeholder(R.drawable.empty2)
                                    .into(vehicleImage);

                        }

                        vehicleImage.setOnClickListener(v -> {
                            String carName = vehicleType;

                            String[] vehicleImages = {
                                    vehicle.optString("vehicle_image"),
                                    vehicle.optString("vehicle_image1"),
                                    vehicle.optString("vehicle_image2"),
                                    vehicle.optString("vehicle_image3"),
                                    vehicle.optString("vehicle_image4")
                            };

                            ArrayList<String> imageList = new ArrayList<>();

                            for (String img : vehicleImages) {
                                if (img != null && !img.isEmpty() && !img.equals("null")) {
                                    if (!img.startsWith("http")) {
                                        img = ImageUrl + img;
                                    }
                                    imageList.add(img);
                                }
                            }

                            if (!imageList.isEmpty()) {
                                FullscreenImageFragment fragment = FullscreenImageFragment.newInstance(imageList, carName);
                                requireActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(android.R.id.content, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                        SharedPreferences sharedPreferences = SharedPrefsHelper.get(getContext());

//                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                        String user_type = sharedPreferences.getString("user_type", "");
                        int passengerId = sharedPreferences.getInt("user_id", -1);
                        int bookingId = dbHelper.getBookingId(tripId, passengerId);

                        if ("driver".equals(user_type)) {
                            btnBook.setText("تعديل الرحلة");
                            tvStatus.setVisibility(View.VISIBLE);

                        } else {
                            tvStatus.setVisibility(View.GONE);
                            if (bookingId != -1) {
                                bookingText.setVisibility(View.VISIBLE);
                                btnBook.setText("تم الحجز");
                            } else {
                                bookingText.setVisibility(View.GONE);
                                btnBook.setText("احجز الآن");
                            }
                        }

                        btnBook.setOnClickListener(new UserUtils.SingleClickListener() {
                            @Override
                            public void onSingleClick(View v) {
                                if ("driver".equals(user_type)) {
                                    showStatusDialog(getContext(), Integer.parseInt(trip_id), availableSeats);
                                } else {
                                    if (availableSeats == 0 && bookingId == -1) {
                                        UserUtils.showErrorDialog(getActivity(), UserUtils.getMessageFromLocalNew(481, dbHelper), null, null,
                                                "الرحلة ممتلئة", 2, null);
                                    } else {
                                        if (diffInMillis <= 0) {

                                            UserUtils.getMessageFromLocal(47, dbHelper, new UserUtils.MessageCallback() {
                                                @Override
                                                public void onSuccess(String message) {
                                                    UserUtils.ToastMessages(getActivity(), message);
                                                }

                                                @Override
                                                public void onError(String error) {
                                                }

                                            });
                                        } else {
                                            if (dbHelper.isBooked(tripId, passengerId)) {
                                                int bookingId = dbHelper.getBookingId(tripId, passengerId);

                                                UserUtils.getMessageFromLocal(35, dbHelper, new UserUtils.MessageCallback() {
                                                    @Override
                                                    public void onSuccess(String message) {
                                                        UserUtils.ToastMessages(getActivity(), message);
                                                    }

                                                    @Override
                                                    public void onError(String error) {
                                                    }
                                                });

                                                Fragment fragment = new BookingDetailsFragment();
                                                Bundle args = new Bundle();
                                                args.putString("related_object_id", String.valueOf(bookingId));
                                                fragment.setArguments(args);
                                                ((FragmentActivity) getContext()).getSupportFragmentManager()
                                                        .beginTransaction()
                                                        .replace(R.id.full_screen_container, fragment)
                                                        .addToBackStack(null)
                                                        .commit();


                                            } else {
                                                Fragment custombookings = new CustomBooking();
                                                Bundle args = new Bundle();

                                                args.putString("car_code", car_code);
                                                args.putInt("trip_id", Integer.parseInt(trip_id2));
                                                String cleanPrice = pricePerSeat != null ? pricePerSeat.replace(",", "") : "0";
                                                int priceInt = (int) Double.parseDouble(cleanPrice.isEmpty() ? "0" : cleanPrice);
                                                args.putInt("pricePerSeat", priceInt);

                                                args.putInt("pickup_orders", pickupOrders);
                                                args.putInt("dropoff_orders", dropoffOrders);
                                                args.putInt("passenger_id", passengerId);
                                                args.putString("DateTrip", dateTimeString);
                                                args.putString("carCodes", car_codes);
                                                args.putString("driver_id", driver_id);
                                                args.putString("car_codes_id", car_codes_id);
                                                String cleanPrice2 = (discountPricePerSeat != null && !discountPricePerSeat.equalsIgnoreCase("null"))
                                                        ? discountPricePerSeat.replace(",", "") : "0";

                                                int discountPricePerSeatint;
                                                try {
                                                    discountPricePerSeatint = (int) Double.parseDouble(cleanPrice2.isEmpty() || cleanPrice2.equalsIgnoreCase("null") ? "0" : cleanPrice2);
                                                } catch (NumberFormatException e) {
                                                    discountPricePerSeatint = 0;
                                                }

                                                args.putInt("discountPricePerSeat", discountPricePerSeatint);
//                                                int discountPricePerSeatint = (int) Double.parseDouble(discountPricePerSeat != null && !discountPricePerSeat.isEmpty() ? discountPricePerSeat : "0");
//                                                args.putInt("discountPricePerSeat", discountPricePerSeatint);
                                                args.putInt("availableSeats", availableSeats);
                                                args.putInt("passport_required", passport_required);
                                                args.putInt("visa_required", visa_required);
                                                args.putInt("company_no", company_no);
                                                args.putInt("v_reception_car", v_reception_car);
                                                args.putString("location_trip", LocationTrip);

                                                custombookings.setArguments(args);

                                                ((HomePage) getContext()).openFullScreenFragment(custombookings, "حجز رحلة", R.drawable.booking, 2);

                                            }
                                        }
                                    }
                                }
                            }

                        });


                    });
                }
            } catch (Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        lottieWave.setVisibility(View.GONE);
                        lottieWave.cancelAnimation();
                        UserUtils.getMessageFromLocal(38, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        UserUtils.sendLog(getContext(), "FetchData", e.toString(), e.toString(), "More Details");

                    });
                }
            }
        }).start();
    }

    private void showStatusDialog(Context context, int trip_id, int availableSeats) {
        Dialog dialog = new Dialog(context, R.style.KeyboardAwareDialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_trip_status, null);
        dialog.setContentView(dialogView);

        Spinner spinnerStatus = dialog.findViewById(R.id.spinnerStatus);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmStatus);
        LinearLayout closeBtn = dialog.findViewById(R.id.btnCloseDialog);
        ImageView minusBtn = dialog.findViewById(R.id.btnMinus);
        ImageView plusBtn = dialog.findViewById(R.id.btnPlus);
        TextView passengersTextView = dialog.findViewById(R.id.passengersTextView);

        final int[] remainingSeats = {availableSeats};
        passengersTextView.setText(String.valueOf(remainingSeats[0]));

        plusBtn.setOnClickListener(v -> {
            if (remainingSeats[0] < availableSeats) {
                remainingSeats[0]++;
                passengersTextView.setText(String.valueOf(remainingSeats[0]));
            }
        });

        minusBtn.setOnClickListener(v -> {
            if (remainingSeats[0] > 0) {
                remainingSeats[0]--;
                passengersTextView.setText(String.valueOf(remainingSeats[0]));
            } else {
                DBHelper db = new DBHelper(context);
                UserUtils.getMessageFromLocal(34, db, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages((Activity) context, message);
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
            }
        });

        String[] statusArabic = {"مجدولة", "في الطريق", "ملغية", "مغلقة"};
        String[] statusEnglish = {"scheduled", "in Way", "cancelled", "closed"};

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statusArabic);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        btnConfirm.setOnClickListener(v -> {
            int selectedPosition = spinnerStatus.getSelectedItemPosition();
            String selectedEnglish = statusEnglish[selectedPosition];

            this.tripId = trip_id;
            updateTripStatus(selectedEnglish, remainingSeats[0]);

            dialog.dismiss();
        });

        // ضبط خصائص الزر بعد العرض
        dialog.show();
        btnConfirm.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button));
        btnConfirm.setAllCaps(false);
        btnConfirm.setTextColor(ContextCompat.getColor(context, R.color.primary));
        btnConfirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
        btnConfirm.setTypeface(typeface, Typeface.BOLD);
        btnConfirm.setElevation(0);
        btnConfirm.setHeight(dpToPx(context, 40));

        // ضبط أبعاد الديالوج وموقعه
        Window window = dialog.getWindow();
        if (window != null) {
            int marginInDp = 20;
            float scale = context.getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale);

            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);

            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            window.getAttributes().windowAnimations = R.style.DialogSlideUpAnimation;
        }

        // ✅ إضافة الضبابية Blurry للديالوج
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }
    }

    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private void updateTripStatus(String statusEnglish, int available_seats) {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        String url = BASE_URL + "update_status/?device_id=" + deviceId + "&device_info=" + deviceInfo;

        JSONObject params = new JSONObject();
        try {
            params.put("token", token);
            params.put("trip_id", tripId);
            params.put("trip_status", statusEnglish);
            params.put("available_seats", available_seats);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DBHelper dbHelper = new DBHelper(getContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, params,
                response -> {

                    UserUtils.getMessageFromLocal(162, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                    if (getArguments() != null) {
                        tripId = getArguments().getInt("trip_id", -1);
                    }

                    if (tripId != -1) {
                        FetchData(tripId);
                    }
                },
                error -> {
                    UserUtils.getMessageFromLocal(163, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(request);
    }

    private AlertDialog exitDialog;
    private static final int PICK_IMAGE_REQUEST = 1;
    int passport_required;
    private View currentViewForImage;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void addNameField(LinearLayout container, String hint, String initialText, InputFilter[] filters) {
        Context context = getContext();
        if (context == null) return;
        int dp8 = (int) (8 * context.getResources().getDisplayMetrics().density);
        int dp2 = (int) (2 * context.getResources().getDisplayMetrics().density);

        int dp30 = (int) (30 * context.getResources().getDisplayMetrics().density);
        int dp48 = (int) (48 * context.getResources().getDisplayMetrics().density);
        int dp12 = (int) (12 * context.getResources().getDisplayMetrics().density);

        com.google.android.material.card.MaterialCardView mainCard = new com.google.android.material.card.MaterialCardView(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        cardParams.setMargins(dp2, dp8, dp2, dp8);
        mainCard.setLayoutParams(cardParams);
        mainCard.setRadius(dp8);
        mainCard.setCardElevation(0f);
        mainCard.setCardBackgroundColor(Color.parseColor("#FFFFFF")); // لون الكرت الرئيسي أبيض
        mainCard.setStrokeWidth(2);
        mainCard.setStrokeColor(Color.parseColor("#E0E0E0"));

        LinearLayout verticalLayout = new LinearLayout(context);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setPadding(20, 20, 20, 20);

        LinearLayout nameRow = new LinearLayout(context);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);

        EditText nameInput = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        nameInput.setLayoutParams(lp);
        nameInput.setHint(hint);
        nameInput.setText(initialText);
        nameInput.setTextSize(16);
        nameInput.setBackground(null); // إلغاء الخط الافتراضي
        nameInput.setFilters(filters);
        UserUtils.setEditTextState(nameInput, false);
        nameInput.setPadding(20, 20, 20, 20);

        com.google.android.material.card.MaterialCardView uploadCard = new com.google.android.material.card.MaterialCardView(context);
        LinearLayout.LayoutParams uploadCardParams = new LinearLayout.LayoutParams(WRAP_CONTENT, 110);
        uploadCardParams.setMarginStart(dp8);
        uploadCard.setLayoutParams(uploadCardParams);
        uploadCard.setRadius(dp8);
        uploadCard.setCardElevation(0f);
        uploadCard.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        uploadCard.setStrokeWidth(0);

        LinearLayout uploadContent = new LinearLayout(context);
        uploadContent.setOrientation(LinearLayout.HORIZONTAL);
        uploadContent.setGravity(Gravity.CENTER);
        uploadContent.setPadding(20, 0, 25, 0);

// الأيقونة (المجلد الذهبي)
        ImageView folderIcon = new ImageView(context);
        folderIcon.setImageResource(R.drawable.ic_upload); // تأكد من اسم الأيقونة لديك
        folderIcon.setColorFilter(Color.parseColor("#CC9407")); // لون ذهبي
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(50, 50);
        iconParams.setMarginEnd(5);
        folderIcon.setLayoutParams(iconParams);

        TextView tvAttach = new TextView(context);
        tvAttach.setText("صورة الجواز");
        tvAttach.setTextSize(12);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
//        tvAttach.setTypeface(typeface);
        tvAttach.setPadding(20, 20, 20, 20);
        tvAttach.setTextColor(Color.BLACK);
        tvAttach.setTypeface(typeface, Typeface.BOLD);

        uploadContent.addView(folderIcon);
        uploadContent.addView(tvAttach);
        uploadCard.addView(uploadContent);

        ImageView statusIcon = new ImageView(context);
        statusIcon.setImageResource(R.drawable.upload_success);
        statusIcon.setVisibility(View.GONE);
        statusIcon.setColorFilter(Color.parseColor("#4CAF50"));
        statusIcon.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
        int uploadVisibility = (passport_required == 1 || visa_required == 1) ? View.VISIBLE : View.GONE;
        uploadCard.setVisibility(uploadVisibility);

        uploadCard.setOnClickListener(v -> {
            currentViewForImage = nameInput;
            openGallery();
        });

// 5. إضافة العناصر إلى الصف (الترتيب: الاسم -> الأيقونة -> زر الرفع)
        nameRow.addView(nameInput);
        nameRow.addView(statusIcon);
        nameRow.addView(uploadCard); // إضافة الكارد المنسق

        verticalLayout.addView(nameRow);

// حفظ المراجع في التاجات للتعامل معها برمجياً عند نجاح الرفع
        nameInput.setTag(R.id.tag_status_icon, statusIcon);
        nameInput.setTag(R.id.tag_upload_button, uploadCard);
//        ImageView statusIcon = new ImageView(context);
//        statusIcon.setImageResource(R.drawable.upload_success);
//        statusIcon.setVisibility(View.GONE);
//        statusIcon.setColorFilter(Color.parseColor("#4CAF50"));
//        statusIcon.setLayoutParams(new LinearLayout.LayoutParams(30, 30));
//
//        TextView tvAttachLabel = new TextView(context);
//        tvAttachLabel.setText("صورة الجواز");
//        tvAttachLabel.setTextSize(12);
//        tvAttachLabel.setTextColor(ContextCompat.getColor(context, R.color.secondary));
//        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
//        labelParams.setMarginEnd(5); // مسافة بسيطة قبل الأيقونة
//        tvAttachLabel.setLayoutParams(labelParams);
//
//        ImageButton btnUpload = new ImageButton(context);
//        btnUpload.setImageResource(R.drawable.ic_upload);
//        btnUpload.setBackgroundResource(android.R.color.transparent);
//        btnUpload.setColorFilter(ContextCompat.getColor(context, R.color.primary));
//        btnUpload.setLayoutParams(new LinearLayout.LayoutParams(dp30, dp30));

//        int uploadVisibility = (passport_required == 1 || visa_required == 1) ? View.VISIBLE : View.GONE;
//        btnUpload.setVisibility(uploadVisibility);
//        tvAttachLabel.setVisibility(uploadVisibility);
//
//        tvAttachLabel.setOnClickListener(v -> {
//            currentViewForImage = nameInput;
//            openGallery();
//        });
//
//        btnUpload.setVisibility((passport_required == 1 || visa_required == 1) ? View.VISIBLE : View.GONE);
//
//        btnUpload.setOnClickListener(v -> {
//            currentViewForImage = nameInput;
//            openGallery();
//        });
//
//        nameRow.addView(nameInput);
//        nameRow.addView(statusIcon);
//        nameRow.addView(btnUpload);
//        nameRow.addView(tvAttachLabel);
//        verticalLayout.addView(nameRow);
        DBHelper dbHelper = new DBHelper(getContext());

        TextView notePrice = new TextView(context);
        notePrice.setId(R.id.NotePrice); // تعيين الـ ID الذي طلبته
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        noteParams.setMargins(20, 0, 20, 5); // هوامش جانبية لتتناسق مع الاسم
        notePrice.setLayoutParams(noteParams);
        notePrice.setTextSize(11);
        notePrice.setTextColor(Color.GRAY);
        notePrice.setText(UserUtils.getMessageFromLocalNew(363, dbHelper));

        verticalLayout.addView(notePrice);
        if (passport_required == 1) {
            TextView passportNote = new TextView(context);
            LinearLayout.LayoutParams passportNoteParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            passportNoteParams.setMargins(20, 0, 20, 5);
            passportNote.setLayoutParams(passportNoteParams);
            passportNote.setTextSize(11);
            passportNote.setTextColor(Color.GRAY);

            passportNote.setText(UserUtils.getMessageFromLocalNew(364, dbHelper));

            verticalLayout.addView(passportNote);
        }
        nameInput.setTag(R.id.NotePrice, notePrice);
        if (passport_required == 1) {
            uploadCard.setVisibility(View.VISIBLE);
        } else {
            uploadCard.setVisibility(View.GONE);
        }
        if (visa_required == 1) {
            // 1. إنشاء حاوية أفقية لتجمع النص والقائمة في صف واحد
            LinearLayout horizontalRow = new LinearLayout(context);
            horizontalRow.setOrientation(LinearLayout.HORIZONTAL);
            horizontalRow.setGravity(Gravity.CENTER_VERTICAL); // لضمان توسط العناصر رأسياً
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            rowParams.setMargins(0, 20, 0, 10);
            horizontalRow.setLayoutParams(rowParams);

            // 2. إعداد نص "نوع التأشيرة *"
//            TextView visaLabel = new TextView(context);
//            String labelText = "نوع التأشيرة *";
//            SpannableStringBuilder builder = new SpannableStringBuilder(labelText);
//            int starIndex = labelText.indexOf("*");
//            if (starIndex != -1) {
//                builder.setSpan(new ForegroundColorSpan(Color.RED),
//                        starIndex, starIndex + 1,
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//            visaLabel.setText(builder);
//            visaLabel.setTextSize(14);
//            visaLabel.setTypeface(null, Typeface.BOLD);
//            visaLabel.setTextColor(Color.BLACK);

            // إعطاء النص مساحة عرض ثابتة أو وزن بسيط ليكون بجانب السبينر
//            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
//            labelParams.setMarginEnd(20); // مسافة بين النص والسبينر
//            visaLabel.setLayoutParams(labelParams);

            // 3. إعداد الـ MaterialCardView الرمادي للسبينر
            com.google.android.material.card.MaterialCardView spinnerCard = new com.google.android.material.card.MaterialCardView(context);
            // العرض هنا سيكون 0 مع وزن (weight) ليأخذ باقي مساحة الصف
            LinearLayout.LayoutParams spinnerCardParams = new LinearLayout.LayoutParams(0, dp48, 1f);
            spinnerCard.setLayoutParams(spinnerCardParams);
            spinnerCard.setRadius(dp8);
            spinnerCard.setCardElevation(0f);
            spinnerCard.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            spinnerCard.setStrokeWidth(0);

            // 4. إعداد الـ Spinner
            Spinner visaSpinner = new Spinner(context);
            visaSpinner.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            visaSpinner.setPadding(dp12, 0, dp12, 0);

            String[] visaTypes = {"نوع التأشيرة", "مقيم", "زيارة", "حج", "عمرة", "مرافق عائلة"};
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, visaTypes);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, visaTypes) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    if (position == 0) {
                        ((TextView) v).setTextColor(Color.GRAY); // جعل الخيار الأول باهت كأنه Hint
                    } else {
                        ((TextView) v).setTextColor(Color.BLACK);
                    }
                    return v;
                }
            };
            visaSpinner.setAdapter(adapter);
            spinnerCard.addView(visaSpinner);
            visaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if (position > 0) {
                        View container = (View) visaSpinner.getParent();
                        if (container instanceof MaterialCardView) {
                            ((MaterialCardView) container).setStrokeWidth(0);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            horizontalRow.addView(spinnerCard);

            verticalLayout.addView(horizontalRow);

            nameInput.setTag(R.id.tag_visa_spinner, visaSpinner);
        }
        mainCard.addView(verticalLayout);
        container.addView(mainCard);

        nameInput.setTag(R.id.tag_status_icon, statusIcon);
        nameInput.setTag(R.id.tag_upload_button, uploadCard);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (currentViewForImage != null && selectedImageUri != null) {

                currentViewForImage.setTag(selectedImageUri);

                View statusIcon = (View) currentViewForImage.getTag(R.id.tag_status_icon);
                if (statusIcon != null) {
                    statusIcon.setVisibility(View.INVISIBLE);
                }


                View btnUpload = (View) currentViewForImage.getTag(R.id.tag_upload_button);

                if (statusIcon != null) {
                    statusIcon.setVisibility(View.GONE);
                }

                if (btnUpload instanceof MaterialCardView) {
                    MaterialCardView card = (MaterialCardView) btnUpload;

                    // الغاء الحدود الحمراء (تصفير سماكة الخط)
                    card.setStrokeWidth(0);

                    ViewGroup cardContent = (ViewGroup) card.getChildAt(0);
                    if (cardContent != null && cardContent.getChildAt(0) instanceof ImageView) {
                        ImageView folderIcon = (ImageView) cardContent.getChildAt(0);

                        folderIcon.setImageResource(R.drawable.upload_success);
                        folderIcon.setColorFilter(Color.parseColor("#4CAF50"));
                    }
                }

            }
        }
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void showBookingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DBHelper dbHelper = new DBHelper(getContext());

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_booking, null);
        builder.setView(dialogView);

        // العناصر من XML
        TextView passengersTextView = dialogView.findViewById(R.id.inputSeats);
        TextView totalPriceTextView = dialogView.findViewById(R.id.totalPriceTextView);
//        TextView NotePrice = dialogView.findViewById(R.id.NotePrice);
        TextView noteReceptionCar = dialogView.findViewById(R.id.noteReceptionCar);
        EditText inputNotes = dialogView.findViewById(R.id.inputNotes);
        noteReceptionCar.setText(UserUtils.getMessageFromLocalNew(441, dbHelper));
        ImageView btnMinus = dialogView.findViewById(R.id.btnMinus);
        ImageView btnPlus = dialogView.findViewById(R.id.btnPlus);

        Button btnAdd = dialogView.findViewById(R.id.btnYes);
        // Button btnCancel = dialogView.findViewById(R.id.btnNo);
        LinearLayout dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        TextView inputChildren = dialogView.findViewById(R.id.inputSeatschild);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvDayName = dialogView.findViewById(R.id.tvDayName);
        TextView tvLocation = dialogView.findViewById(R.id.tvLocation);
        TextView IdTrip = dialogView.findViewById(R.id.IdTrip2);
        TextView tripDuration = dialogView.findViewById(R.id.tripDuration);
        ImageView btnMinusChild = dialogView.findViewById(R.id.btnMinuschild);
        ImageView btnPlusChild = dialogView.findViewById(R.id.btnPluschild);
        CheckBox reception_car = dialogView.findViewById(R.id.reception_car);
        Date now = new Date();

        String dateTimeString = DateTrip;
        SimpleDateFormat sdf;

        if (dateTimeString != null && dateTimeString.contains(" ")) {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        } else {
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        }

        Date tripDateTime = null;
        try {
            tripDateTime = sdf.parse(dateTimeString);
        } catch (ParseException e) {
            tripDateTime = new Date();
            e.printStackTrace();
        }
        long diffInMillis = tripDateTime.getTime() - now.getTime();
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        if (diffDays == 1) {
            tripDuration.setText("يوم واحد");
        } else if (diffDays == 0) {
            long diffHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60;

            if (diffHours > 0) {
                String hourText = (diffHours == 1) ? "ساعة واحدة" : diffHours + " ساعات";
                tripDuration.setText(hourText);
            } else if (diffMinutes > 0) {
                String minuteText = (diffMinutes == 1) ? "دقيقة واحدة" : diffMinutes + " دقائق";
                tripDuration.setText(minuteText);
            } else {
                tripDuration.setText("الرحلة بدأت");
                tripDuration.setTextColor(Color.parseColor("#af0516")); // أخضر غامق
//                background2.setColor(Color.parseColor("#FFCDD2"));
            }
        } else if (diffDays == 2) {
            tripDuration.setText("يومان");
        } else if (diffDays >= 3 && diffDays <= 10) {
            tripDuration.setText(diffDays + " أيام");
        } else {
            tripDuration.setText(diffDays + " يوم");
        }
        reception_car.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                v_reception_car = 1;
            } else {
                v_reception_car = 0;
            }
        });
        LinearLayout paymentContainer = dialogView.findViewById(R.id.paymentMethodsContainerInBooking);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

        LinearLayout childrenNamesContainer = dialogView.findViewById(R.id.childrenNamesContainer);
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("en"));

        SimpleDateFormat outputSdf = new SimpleDateFormat("dd-MM-yyyy", new Locale("en"));

        try {
            Date date = inputSdf.parse(DateTrip);

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ar"));
            String dayOfWeek = dayFormat.format(date);

            String formattedDate = outputSdf.format(date);
            tvDate.setText(formattedDate);
            tvDayName.setText(dayOfWeek);

        } catch (ParseException e) {
            tvDate.setText(DateTrip);
        }

        IdTrip.setText("رقم الرحلة: " + trip_id2);
        tvLocation.setText(LocationTrip);
        LinearLayout adultsNamesContainer = new LinearLayout(getContext());
        adultsNamesContainer.setOrientation(LinearLayout.VERTICAL);
        adultsNamesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT
        ));
        childrenNamesContainer.addView(adultsNamesContainer, 0); // فوق الأطفال


        final int[] numSeats = {1};
        final int[] numChildren = {0};


        String discountStr = (discountPricePerSeat != null && !discountPricePerSeat.equals("null"))
                ? discountPricePerSeat.replace(",", "")
                : "0";

        String priceStr = (pricePerSeat != null && !pricePerSeat.equals("null"))
                ? pricePerSeat.replace(",", "")
                : "0";
        double seatPrice = (Double.parseDouble(discountStr) > 0)
                ? Double.parseDouble(discountStr)
                : Double.parseDouble(priceStr);
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMaximumFractionDigits(0);

        totalPriceTextView.setText(numberFormat.format(seatPrice) + " " + car_codes);

        btnMinus.setOnClickListener(v -> {
            if (numSeats[0] > 1) {
                numSeats[0]--;
                passengersTextView.setText(String.valueOf(numSeats[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText(numberFormat.format(totalPrice) + " " + car_codes);

                int count = adultsNamesContainer.getChildCount();
                if (count > 0) adultsNamesContainer.removeViewAt(count - 1);
            }
        });
        InputFilter arabicFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!((c >= 0x0621 && c <= 0x064A) || c == ' ')) {
                    return "";
                }
            }
            return null;
        };

        String full_name = prefs.getString("full_name", "");
        addNameField(adultsNamesContainer, "الاسم الكامل للمسافر 1", full_name, new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});
        btnPlus.setOnClickListener(v -> {
            if (numSeats[0] + numChildren[0] < availableSeats) {
                numSeats[0]++;
                passengersTextView.setText(String.valueOf(numSeats[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText(numberFormat.format(totalPrice) + " " + car_codes);

                addNameField(adultsNamesContainer, "الاسم الكامل للمسافر " + numSeats[0], "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});

            } else {
                UserUtils.getMessageFromLocal(34, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(getActivity(), message);
                    }

                    @Override
                    public void onError(String error) {
                    }

                });
            }
        });

        btnPlusChild.setOnClickListener(v -> {
            if (numSeats[0] + numChildren[0] < availableSeats) {
                numChildren[0]++;
                inputChildren.setText(String.valueOf(numChildren[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText(numberFormat.format(totalPrice) + " " + car_codes);
                addNameField(childrenNamesContainer, "الاسم الكامل للطفل " + numChildren[0], "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});

            } else {
                UserUtils.getMessageFromLocal(34, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(getActivity(), message);
                    }

                    @Override
                    public void onError(String error) {
                    }

                });
            }
        });

        btnMinusChild.setOnClickListener(v -> {
            if (numChildren[0] > 0) {
                numChildren[0]--;
                inputChildren.setText(String.valueOf(numChildren[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText(numberFormat.format(totalPrice) + " " + car_codes);
                int count = childrenNamesContainer.getChildCount();
                if (count > 0) {
                    childrenNamesContainer.removeViewAt(count - 1);
                }
            }
        });

        exitDialog = builder.create();
        ViewGroup decorView = requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
        exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));

        exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        exitDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        exitDialog.show();

        dialogCancelButton.setOnClickListener(v -> {
            exitDialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> {
            exitDialog.dismiss();
        });
        btnAdd.setOnClickListener(new UserUtils.SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                int passengerId = prefs.getInt("user_id", -1);
                String notes = inputNotes.getText().toString().trim();

                StringBuilder adultNamesBuilder = new StringBuilder();
                StringBuilder childNamesBuilder = new StringBuilder();

                for (int i = 0; i < adultsNamesContainer.getChildCount(); i++) {
                    View card = adultsNamesContainer.getChildAt(i);
                    LinearLayout mainLatout = (LinearLayout) ((MaterialCardView) card).getChildAt(0);
                    LinearLayout nameRow = (LinearLayout) mainLatout.getChildAt(0);
                    EditText nameField = (EditText) nameRow.getChildAt(0);

                    String name = nameField.getText().toString().trim();

                    if (name.isEmpty()) {
                        nameField.setError("يرجى إدخال الاسم الكامل");
                        return;
                    }

                    if (passport_required == 1) {
                        if (nameField.getTag() == null || !(nameField.getTag() instanceof Uri)) {
                            UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(346, dbHelper) + " " + name);
                            View btnUpload = (View) nameField.getTag(R.id.tag_upload_button);

                            if (btnUpload != null) {
                                if (btnUpload instanceof MaterialCardView) {
                                    MaterialCardView card2 = (MaterialCardView) btnUpload;
                                    card2.setStrokeColor(ColorStateList.valueOf(Color.RED));
                                    card2.setStrokeWidth(4);
                                } else if (btnUpload instanceof ImageButton) {
                                    ((ImageButton) btnUpload).setColorFilter(Color.RED);
                                }
                            }
                            return;
                        }
                    }

                    if (visa_required == 1) {
                        Spinner visaSpinner = (Spinner) nameField.getTag(R.id.tag_visa_spinner);
                        if (visaSpinner != null && visaSpinner.getSelectedItemPosition() == 0) {
                            UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(345, dbHelper) + " " + name);

                            View parent = (View) visaSpinner.getParent();
                            if (parent instanceof MaterialCardView) {
                                MaterialCardView card3 = (MaterialCardView) parent;
                                card3.setStrokeColor(ColorStateList.valueOf(Color.RED));
                                card3.setStrokeWidth(4);
                            }
                            return;
                        }
                    }

                    adultNamesBuilder.append(name).append(i < adultsNamesContainer.getChildCount() - 1 ? ", " : "");
                }

                for (int i = 0; i < childrenNamesContainer.getChildCount(); i++) {
                    View view = childrenNamesContainer.getChildAt(i);
                    if (view instanceof MaterialCardView) {
                        LinearLayout mainLayout = (LinearLayout) ((MaterialCardView) view).getChildAt(0);
                        LinearLayout nameRow = (LinearLayout) mainLayout.getChildAt(0);
                        EditText nameField = (EditText) nameRow.getChildAt(0);
                        String name = nameField.getText().toString().trim();

                        if (name.isEmpty()) {
                            nameField.setError("يرجى إدخال اسم الطفل");
                            return;
                        }

                        if (nameField.getTag() == null || !(nameField.getTag() instanceof Uri)) {
                            UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(343, dbHelper) + " " + name);
                            View btnUpload = (View) nameField.getTag(R.id.tag_upload_button);

                            if (btnUpload != null) {
                                if (btnUpload instanceof MaterialCardView) {
                                    MaterialCardView card2 = (MaterialCardView) btnUpload;
                                    card2.setStrokeColor(ColorStateList.valueOf(Color.RED));
                                    card2.setStrokeWidth(4);
                                } else if (btnUpload instanceof ImageButton) {
                                    ((ImageButton) btnUpload).setColorFilter(Color.RED);
                                }
                            }
                            return;
                        }

                        if (visa_required == 1) {
                            Spinner visaSpinner = (Spinner) nameField.getTag(R.id.tag_visa_spinner);
                            if (visaSpinner != null && visaSpinner.getSelectedItemPosition() == 0) {
                                UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(344, dbHelper) + " " + name);

                                View parent = (View) visaSpinner.getParent();
                                if (parent instanceof MaterialCardView) {
                                    MaterialCardView card3 = (MaterialCardView) parent;
                                    card3.setStrokeColor(ColorStateList.valueOf(Color.RED));
                                    card3.setStrokeWidth(4);
                                }
                                return;
                            }
                        }
                        childNamesBuilder.append(name).append(", ");
                    }
                }

                String childNames = childNamesBuilder.toString().replaceAll(", $", "");
                String adultNames = adultNamesBuilder.toString();
                int totalSeats = numSeats[0] + numChildren[0];
                double totalPrice = totalSeats * seatPrice;

                sendBookingRequest(
                        car_code, Integer.parseInt(trip_id2), totalSeats, pickupOrders, dropoffOrders,
                        passengerId, totalPrice, notes, driver_id,
                        car_codes_id, adultNames, childNames,
                        0, "waiting", 0, company_no, v_reception_car,
                        adultsNamesContainer, childrenNamesContainer, LocationTrip
                );

                exitDialog.dismiss();
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
            if (result != null && result.contains("/")) {
                result = result.substring(result.lastIndexOf("/") + 1);
            }
        }

        return result;
    }

    private void writeFileField(DataOutputStream out, String fieldName, Uri fileUri, String boundary) throws IOException {

        String originalName = getFileNameFromUri(fileUri);
        String uniqueID = UUID.randomUUID().toString();
        String fileName = uniqueID + "_" + originalName;

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        out.writeBytes("Content-Type: image/jpeg\r\n");
        out.writeBytes("\r\n");

        InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);

        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);

        inputStream.close();

        if (bitmap != null) {

            int maxWidth = 1280;
            int maxHeight = 1280;

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float ratio = Math.min(
                    (float) maxWidth / width,
                    (float) maxHeight / height
            );

            if (ratio < 1) {
                width = Math.round(width * ratio);
                height = Math.round(height * ratio);

                bitmap = android.graphics.Bitmap.createScaledBitmap(
                        bitmap,
                        width,
                        height,
                        true
                );
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, baos);

            out.write(baos.toByteArray());

            baos.close();
            bitmap.recycle();
        }

        out.writeBytes("\r\n");
    }

//    private void writeFileField(DataOutputStream out, String fieldName, Uri fileUri, String boundary) throws IOException {
//        String originalName = getFileNameFromUri(fileUri); // الاسم الأصلي
//        String uniqueID = UUID.randomUUID().toString();   // جزء فريد
//        String fileName = uniqueID + "_" + originalName;   // الاسم النهائي الفريد
//        String mimeType = getContext().getContentResolver().getType(fileUri);
//
//        out.writeBytes("--" + boundary + "\r\n");
//        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
//        out.writeBytes("Content-Type: " + mimeType + "\r\n");
//        out.writeBytes("\r\n");
//        InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
//        byte[] buffer = new byte[4096];
//        int bytesRead;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            out.write(buffer, 0, bytesRead);
//        }
//        inputStream.close();
//
//        out.writeBytes("\r\n");
//    }

    private void extractDataFromContainer(LinearLayout container, JSONArray jsonArray, int isChildStatus) {
        if (container == null) return;

        for (int i = 0; i < container.getChildCount(); i++) {
            View card = container.getChildAt(i);
            if (card instanceof CardView) {
                // الوصول للـ LinearLayout الرأسي داخل الكارد
                LinearLayout verticalLayout = (LinearLayout) ((CardView) card).getChildAt(0);

                // الوصول للصف الأول (الذي يحتوي على الاسم)
                LinearLayout nameRow = (LinearLayout) verticalLayout.getChildAt(0);
                EditText nameInput = (EditText) nameRow.getChildAt(0);

                JSONObject person = new JSONObject();
                try {
                    person.put("passenger_name", nameInput.getText().toString());
                    person.put("is_child", isChildStatus);
                    Spinner visaSpinner = (Spinner) nameInput.getTag(R.id.tag_visa_spinner);
                    if (visaSpinner != null) {
                        person.put("visa_type", visaSpinner.getSelectedItem().toString());
                    } else {
                        person.put("visa_type", ""); // أو قيمة افتراضية
                    }

                    Uri imageUri = (Uri) nameInput.getTag(); // تأكدي أن openGallery تضع الـ Uri هنا
                    if (imageUri != null) {
                        person.put("has_image", true);
                        // هنا نضع اسم افتراضي، والرفع الفعلي سيتم عبر Multipart
                        person.put("passport_image", "passport_" + System.currentTimeMillis() + ".jpg");
                    }

                    jsonArray.put(person);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
//                    e.printStackTrace();
                }
            }
        }
    }

    private void sendBookingRequest(String car_code, int tripId, int numSeats, int pickupOrder,
                                    int dropoffOrder, int passengerId,
                                    double totalPrice, String notes, String driver_id, String car_codes,
                                    String adult_names_str, String child_names_str, int pay_type, String payment_status,
                                    int request_id, int company_no, int reception_car,
                                    LinearLayout adultsContainer, LinearLayout childrenContainer, String booking_name) { // أضفنا الحاويات هنا

        DBHelper dbHelper = new DBHelper(getContext());
        UserUtils.showSuccessGif(3, requireActivity(), null);

        new Thread(() -> {
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();

                URL url = new URL(BASE_URL + "bookings/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                SharedPreferences prefs = SharedPrefsHelper.get(getContext());
                String token = prefs.getString("auth_token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // 1. إرسال الحقول النصية
                writeFormField(dos, "trip", String.valueOf(tripId), boundary);
                writeFormField(dos, "number_of_seats", String.valueOf(numSeats), boundary);
                writeFormField(dos, "pickup_point_order", String.valueOf(pickupOrder), boundary);
                writeFormField(dos, "dropoff_point_order", String.valueOf(dropoffOrder), boundary);
                writeFormField(dos, "passenger", String.valueOf(passengerId), boundary);
                writeFormField(dos, "adult_names", adult_names_str, boundary);
                writeFormField(dos, "child_names", child_names_str, boundary);
                writeFormField(dos, "total_price", String.valueOf(totalPrice), boundary);
                writeFormField(dos, "passenger_notes", notes, boundary);
                writeFormField(dos, "driver_id", driver_id, boundary);
                writeFormField(dos, "car_code", car_code, boundary);
                writeFormField(dos, "car_codes", car_codes, boundary);
                writeFormField(dos, "pay_type", String.valueOf(pay_type), boundary);
                writeFormField(dos, "payment_status", payment_status, boundary);
                writeFormField(dos, "reception_car", String.valueOf(reception_car), boundary);
                writeFormField(dos, "request_id", String.valueOf(request_id), boundary);
                writeFormField(dos, "company_no", String.valueOf(company_no), boundary);

                JSONArray passengersDetails = new JSONArray();

                extractDataFromContainer(adultsContainer, passengersDetails, 0);
                extractDataFromContainer(childrenContainer, passengersDetails, 1);

                writeFormField(dos, "passengers_details", passengersDetails.toString(), boundary);
                int imageCounter = 0;

                if (adultsContainer != null) {
                    for (int i = 0; i < adultsContainer.getChildCount(); i++) {
                        View view = adultsContainer.getChildAt(i);
                        // التحقق: هل العنصر فعلاً CardView؟
                        if (view instanceof CardView) {
                            CardView card = (CardView) view;
                            // الحصول على العناصر بالترتيب الذي أنشأناه في addNameField
                            LinearLayout verticalLayout = (LinearLayout) card.getChildAt(0);
                            LinearLayout nameRow = (LinearLayout) verticalLayout.getChildAt(0);
                            EditText nameInput = (EditText) nameRow.getChildAt(0);

                            Uri imageUri = (Uri) nameInput.getTag(R.id.tag_image_uri); // يفضل استخدام ID محدد للـ URI
                            if (imageUri == null) imageUri = (Uri) nameInput.getTag(); // كاحتياط

                            if (imageUri != null) {
                                writeFileField(dos, "passport_image_" + imageCounter, imageUri, boundary);
                                imageCounter++;
                            }
                        }
                    }
                }

                if (childrenContainer != null) {
                    for (int i = 0; i < childrenContainer.getChildCount(); i++) {
                        View view = childrenContainer.getChildAt(i);
                        if (view instanceof CardView) {
                            CardView card = (CardView) view;
                            LinearLayout verticalLayout = (LinearLayout) card.getChildAt(0);
                            LinearLayout nameRow = (LinearLayout) verticalLayout.getChildAt(0);
                            EditText nameInput = (EditText) nameRow.getChildAt(0);

                            Uri imageUri = (Uri) nameInput.getTag(R.id.tag_image_uri);
                            if (imageUri == null) imageUri = (Uri) nameInput.getTag();

                            if (imageUri != null) {
                                writeFileField(dos, "passport_image_" + imageCounter, imageUri, boundary);
                                imageCounter++;
                            }
                        }
                    }
                }


                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                // 4. معالجة الرد
                int status = conn.getResponseCode();
                InputStream inputStream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                getActivity().runOnUiThread(() -> {
                    UserUtils.hideSuccessGif(getActivity());
                    if (status >= 200 && status < 300) {
                        try {
                            JSONObject respJson = new JSONObject(response.toString());
                            int bookingIdFromServer = respJson.getInt("booking_id");
//                            dbHelper.addBooking(tripId, bookingIdFromServer, passengerId);
                            String currentDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ENGLISH).format(new java.util.Date());
                            dbHelper.addBooking(tripId, bookingIdFromServer, passengerId, numSeats, currentDateTime, "pending", booking_name);

                            UserUtils.getMessageFromLocal(48, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            if (getActivity() != null) {
                                ViewGroup decorView = (ViewGroup) getActivity().getWindow().getDecorView();
                                jp.wasabeef.blurry.Blurry.delete(decorView);
                            }

                            Fragment fragment = new BookingDetailsFragment();
                            Bundle args = new Bundle();
                            args.putString("related_object_id", String.valueOf(bookingIdFromServer));
                            fragment.setArguments(args);

                            if (getContext() instanceof HomePage) {
                                ((HomePage) getContext()).openFullScreenFragment(fragment, "تفاصيل الحجز", R.drawable.checklist, 2);
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
//                            e.printStackTrace();
                        }
                    } else {
                        UserUtils.showErrorDialog(getActivity(), UserUtils.getMessageFromLocalNew(36, dbHelper), null, null, "تعذر إتمام الحجز", 1, null);
                        UserUtils.sendLog(getContext(), "sendBookingRequest", response.toString(), "Error Status: " + status, "Home Fragment");
                    }
                });
                conn.disconnect();

            } catch (Exception e) {
                UserUtils.showErrorDialog(getActivity(), UserUtils.getMessageFromLocalNew(36, dbHelper), null, null, "تعذر إتمام الحجز", 1, null);
                getActivity().runOnUiThread(() -> UserUtils.hideSuccessGif(getActivity()));
            }
        }).start();
    }

    private void writeFormField(DataOutputStream request, String fieldName, String fieldValue, String boundary) throws IOException {
        request.writeBytes("--" + boundary + "\r\n");
        request.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n");
        request.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        request.write(fieldValue.getBytes(StandardCharsets.UTF_8));
        request.writeBytes("\r\n");
    }


}
