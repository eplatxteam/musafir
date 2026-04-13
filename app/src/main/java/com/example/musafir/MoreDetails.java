package com.example.musafir;

import static android.app.Activity.RESULT_OK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.fonts.Font;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;


import org.jetbrains.annotations.Nullable;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    ScrollView scrollContents;
    String car_code;
    String car_codes;
    String car_codes_id;
    String discountPricePerSeat;
    String LocationTrip, DateTrip;
    String trip_id2;

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
        btn_whatsapp.setOnClickListener(v -> {
            String phoneNumber = "967785050270";
            String message = "لدي استفسار بخصوص الرحلة رقم: " + trip_id2.toString();

            String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);

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

//    @Override
//    public void onResume() {
//        super.onResume();
//        Toolbar toolbar = requireActivity().findViewById(R.id.main_toolbar);
//        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
//        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
//    }

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
                String vehicleMake = vehicle.optString("make");
                String vehicleModel = vehicle.optString("model");
                String vehicleColor = vehicle.optString("color");
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
                String vehicleType = vehicle.optString("vehicle_type");
                String trip_status = trip.optString("trip_status", "");
                String route_city_ids = trip.optString("route_city", "");
                String vehicleName = vehicle.optString("vehicle_name", "");
                List<String> cityNames = new ArrayList<>();
                GradientDrawable background = (GradientDrawable) tvStatus.getBackground();
                passport_required = trip.optInt("passport_required", 0);

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
                if (!route_city_ids.isEmpty()) {
                    String[] ids = route_city_ids.split(",");
                    for (String idStr : ids) {
                        idStr = idStr.trim();
                        if (!idStr.isEmpty()) {
                            try {
                                int cityId = Integer.parseInt(idStr);
                                String cityName = dbHelper.getCityNameById(cityId); // دالة في DBHelper ترجع الاسم
                                if (cityName != null) {
                                    cityNames.add(cityName);
                                }
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
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
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
                            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.linaround_bold);
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
                            iconPrice.setVisibility(View.VISIBLE);
                            originalPrice.setVisibility(View.VISIBLE);

                            // السعر الأصلي مع شطب
                            originalPrice.setText(pricePerSeat + " " + car_codes);
                            originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                            // السعر بعد الخصم
                            textPrice.setText(discountPricePerSeat + " " + car_codes);
                        }
                        String dateTimeString = departure_date + " " + departure_time;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date tripDateTime = null;
                        try {
                            tripDateTime =
                                    sdf.parse(dateTimeString);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        Date now = new Date();
                        long diffInMillis = tripDateTime.getTime() - now.getTime();
                        long diffDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
//                        GradientDrawable background2 = (GradientDrawable) daysRemaining.getBackground();
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
                                daysRemaining.setBackgroundColor(Color.TRANSPARENT); // خلفية عادية
                            } else if (diffMinutes > 0) {
                                String minuteText = (diffMinutes == 1) ? "دقيقة واحدة" : diffMinutes + " دقائق";
                                daysRemaining.setText(minuteText);
                                daysRemaining.setBackgroundColor(Color.TRANSPARENT); // خلفية عادية
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


                        makeText.setText(vehicleMake);
                        colorText.setText(vehicleColor);
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
                            String carName = vehicleType + " " + vehicleMake;

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
//                        if (bookingId != -1) {
//                            bookingText.setVisibility(View.VISIBLE);
//                        } else {
//                            bookingText.setVisibility(View.GONE);
//
//                        }
                        if ("driver".equals(user_type)) {

                            btnBook.setText("تعديل الرحلة");
                            tvStatus.setVisibility(View.VISIBLE);

                            btnBook.setOnClickListener(v -> {
                                showStatusDialog(getActivity(),
                                        Integer.parseInt(trip_id),
                                        availableSeats);
                            });

                        } else {

                            tvStatus.setVisibility(View.GONE);

                            if (bookingId != -1) {
                                bookingText.setVisibility(View.VISIBLE);
                                btnBook.setText("تم الحجز");
                                btnBook.setOnClickListener(null);

                            } else {
                                bookingText.setVisibility(View.GONE);
                                btnBook.setText("احجز الآن");

                                btnBook.setOnClickListener(v -> {

                                    if (passengerId == -1) {

                                        UserUtils.getMessageFromLocal(39, dbHelper,
                                                new UserUtils.MessageCallback() {
                                                    @Override
                                                    public void onSuccess(String message) {
                                                        UserUtils.ToastMessages(getActivity(), message);
                                                    }

                                                    @Override
                                                    public void onError(String error) {
                                                    }
                                                });

                                        startActivity(new Intent(getContext(), MainActivity.class));
                                        getActivity().finish();
                                        return;
                                    }

                                    if (diffInMillis <= 0) {

                                        UserUtils.getMessageFromLocal(47, dbHelper,
                                                new UserUtils.MessageCallback() {
                                                    @Override
                                                    public void onSuccess(String message) {
                                                        UserUtils.ToastMessages(getActivity(), message);
                                                    }

                                                    @Override
                                                    public void onError(String error) {
                                                    }
                                                });
                                        return;
                                    }

                                    showBookingDialog();
                                });
                            }
                        }


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
        btnConfirm.setTypeface(null, Typeface.BOLD);
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
//            Blurry.with(activity).radius(15).sampling(2).onto(decorView);
//
//            dialog.setOnDismissListener(d -> Blurry.delete(decorView));
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
        }
        DBHelper dbHelper = new DBHelper(getContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, params,
                response -> {

                    UserUtils.getMessageFromLocal(162, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages((Activity) getActivity(), message);
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
                            UserUtils.ToastMessages((Activity) getActivity(), message);
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

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        // row.setBackgroundResource(R.drawable.edittext_background);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        rowParams.setMargins(10, 15, 10, 0);
        row.setLayoutParams(rowParams);
        row.setPadding(15, 5, 15, 5);

        // 1. حقل إدخال الاسم
        EditText nameInput = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        nameInput.setLayoutParams(lp);
        nameInput.setHint(hint);
        nameInput.setText(initialText);
        nameInput.setBackground(null);
        nameInput.setFilters(filters);
        nameInput.setPadding(15, 25, 15, 25);
        UserUtils.setEditTextState(nameInput, false);

        // 2. أيقونة الحالة (سنحتاجها للتحقق، نجعلها مخفية دائماً)
        ImageView statusIcon = new ImageView(context);
        statusIcon.setImageResource(R.drawable.upload_success); // أيقونة صح للنجاح
        statusIcon.setVisibility(View.GONE); // مخفية دائماً، نستخدمها فقط كعلم للتحقق
        statusIcon.setColorFilter(Color.parseColor("#4CAF50"));
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(50, 50);
        statusIcon.setLayoutParams(iconLp);

        // 3. زر رفع صورة الجواز (هذا ما سنقوم بتغييره)
        ImageButton btnUpload = new ImageButton(context);
        btnUpload.setImageResource(R.drawable.ic_upload); // أيقونة الرفع الافتراضية
        btnUpload.setBackgroundResource(android.R.color.transparent);
        btnUpload.setPadding(10, 10, 10, 10);
        btnUpload.setColorFilter(ContextCompat.getColor(context, R.color.primary)); // لون افتراضي

        if (passport_required == 1) {
            btnUpload.setVisibility(View.VISIBLE);
        } else {
            btnUpload.setVisibility(View.GONE);
        }

        btnUpload.setOnClickListener(v -> {
            currentViewForImage = nameInput;
            openGallery();
        });

        row.addView(nameInput);
        row.addView(statusIcon);
        row.addView(btnUpload);

        nameInput.setTag(R.id.tag_status_icon, statusIcon);
        nameInput.setTag(R.id.tag_upload_button, btnUpload);

        container.addView(row);
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

                ImageButton btnUpload = (ImageButton) currentViewForImage.getTag(R.id.tag_upload_button);
                if (btnUpload != null) {
                    btnUpload.setImageResource(R.drawable.upload_success);
                    btnUpload.setColorFilter(Color.parseColor("#4CAF50"));
                }

//                UserUtils.ToastMessages(getActivity(), "تم اختيار صورة الجواز بنجاح");
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
        EditText inputNotes = dialogView.findViewById(R.id.inputNotes);

        ImageView btnMinus = dialogView.findViewById(R.id.btnMinus);
        ImageView btnPlus = dialogView.findViewById(R.id.btnPlus);
        Button btnAdd = dialogView.findViewById(R.id.btnYes);
        // Button btnCancel = dialogView.findViewById(R.id.btnNo);
        LinearLayout dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        TextView inputChildren = dialogView.findViewById(R.id.inputSeatschild);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvLocation = dialogView.findViewById(R.id.tvLocation);
        TextView IdTrip = dialogView.findViewById(R.id.IdTrip2);
        ImageView btnMinusChild = dialogView.findViewById(R.id.btnMinuschild);
        ImageView btnPlusChild = dialogView.findViewById(R.id.btnPluschild);
        UserUtils.setEditTextState(inputNotes, false);
        LinearLayout paymentContainer = dialogView.findViewById(R.id.paymentMethodsContainerInBooking);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

        LinearLayout childrenNamesContainer = dialogView.findViewById(R.id.childrenNamesContainer);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("en"));
        try {
            Date date = sdf.parse(DateTrip);

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ar"));
            String dayOfWeek = dayFormat.format(date);

            tvDate.setText("(" + dayOfWeek + ") " + DateTrip);

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
        childrenNamesContainer.addView(adultsNamesContainer, 0);

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


//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String full_name = prefs.getString("full_name", "");
//        EditText firstAdultName = new EditText(getContext());
//        firstAdultName.setHint("الاسم الكامل للراكب 1");
        // ابحث عن كود إضافة الراكب الأول واستبدله بـ:
        addNameField(adultsNamesContainer, "الاسم الكامل للراكب 1", full_name, new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});
//        UserUtils.setEditTextState(firstAdultName, false);

//        firstAdultName.setBackgroundResource(R.drawable.edittext_background);
//        firstAdultName.setFilters(new InputFilter[]{arabicFilter});
//        firstAdultName.setFilters(new InputFilter[]{
//                arabicFilter,
//                new InputFilter.LengthFilter(30)
//        });
//
//        firstAdultName.setText(full_name);
//        firstAdultName.setPadding(30, 30, 30, 30);
//        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
//                MATCH_PARENT,
//                WRAP_CONTENT
//        );
//        params2.setMargins(10, 15, 10, 0);
//        firstAdultName.setLayoutParams(params2);
//        adultsNamesContainer.addView(firstAdultName);
        btnPlus.setOnClickListener(v -> {
            if (numSeats[0] + numChildren[0] < availableSeats) {
                numSeats[0]++;
                passengersTextView.setText(String.valueOf(numSeats[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText(numberFormat.format(totalPrice) + " " + car_codes);

//                EditText adultName = new EditText(getContext());
//                adultName.setHint("الاسم الكامل للراكب " + numSeats[0]);
                addNameField(adultsNamesContainer, "الاسم الكامل للراكب " + numSeats[0], "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});
//                adultName.setBackgroundResource(R.drawable.edittext_background);
//                adultName.setFilters(new InputFilter[]{arabicFilter});
//                adultName.setFilters(new InputFilter[]{
//                        arabicFilter,
//                        new InputFilter.LengthFilter(30)
//                });
//                UserUtils.setEditTextState(adultName, false);

//                adultName.setPadding(30, 30, 30, 30);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                        MATCH_PARENT,
//                        WRAP_CONTENT
//                );
//                params.setMargins(10, 15, 10, 0);
//                adultName.setLayoutParams(params);
//                adultsNamesContainer.addView(adultName);

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
//                EditText childName = new EditText(getContext());
                addNameField(childrenNamesContainer, "الاسم الكامل للطفل " + numChildren[0], "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});
//                childName.setBackgroundResource(R.drawable.edittext_background);
//                childName.setPadding(30, 30, 30, 30);
//                childName.setFilters(new InputFilter[]{arabicFilter});
//                childName.setFilters(new InputFilter[]{
//                        arabicFilter,
//                        new InputFilter.LengthFilter(30)
//                });
//                UserUtils.setEditTextState(childName, false);
//
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                        MATCH_PARENT,
//                        WRAP_CONTENT
//                );
//                params.setMargins(10, 15, 10, 0);
//                childName.setLayoutParams(params);

//                childrenNamesContainer.addView(childName);
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
                boolean missingPassportImage = false; // متغير جديد للتحقق من الصور
                int passengerId = prefs.getInt("user_id", -1);
                String notes = inputNotes.getText().toString().trim();

                StringBuilder adultNamesBuilder = new StringBuilder();
                boolean missingAdultName = false;

                StringBuilder childNamesBuilder = new StringBuilder();
                boolean missingChildName = false;

                // 1. معالجة أسماء وصور البالغين
                for (int i = 0; i < adultsNamesContainer.getChildCount(); i++) {
                    View view = adultsNamesContainer.getChildAt(i);
                    if (view instanceof LinearLayout) {
                        LinearLayout row = (LinearLayout) view;
                        if (row.getChildCount() > 0 && row.getChildAt(0) instanceof EditText) {
                            EditText nameField = (EditText) row.getChildAt(0);
                            String name = nameField.getText().toString().trim();

                            // التحقق من الاسم
                            if (name.isEmpty()) {
                                missingAdultName = true;
                                nameField.setError("يرجى إدخال الاسم الكامل");
                                UserUtils.setEditTextState(nameField, true);
                            } else {
                                adultNamesBuilder.append(name);
                                UserUtils.setEditTextState(nameField, false);
                                if (i < adultsNamesContainer.getChildCount() - 1) {
                                    adultNamesBuilder.append(", ");
                                }
                            }

                            // --- التعديل: التحقق من صورة الجواز ---
                            if (passport_required == 1) {
                                if (nameField.getTag() == null || !(nameField.getTag() instanceof Uri)) {
                                    missingPassportImage = true;
                                    // تغيير لون زر الرفع للأحمر لتنبيه المستخدم
                                    ImageButton btnUpload = (ImageButton) nameField.getTag(R.id.tag_upload_button);
                                    if (btnUpload != null) {
                                        btnUpload.setColorFilter(Color.RED);
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. معالجة أسماء وصور الأطفال
                for (int i = 0; i < childrenNamesContainer.getChildCount(); i++) {
                    View view = childrenNamesContainer.getChildAt(i);
                    if (view instanceof LinearLayout) {
                        LinearLayout row = (LinearLayout) view;
                        if (row.getChildCount() > 0 && row.getChildAt(0) instanceof EditText) {
                            EditText nameField = (EditText) row.getChildAt(0);
                            String name = nameField.getText().toString().trim();

                            if (name.isEmpty()) {
                                missingChildName = true;
                                nameField.setError("يرجى إدخال الاسم الكامل");
                                UserUtils.setEditTextState(nameField, true);
                            } else {
                                childNamesBuilder.append(name);
                                UserUtils.setEditTextState(nameField, false);
                                childNamesBuilder.append(", ");
                            }

                            // --- التعديل: التحقق من صورة جواز الطفل ---
                            if (passport_required == 1) {
                                if (nameField.getTag() == null || !(nameField.getTag() instanceof Uri)) {
                                    missingPassportImage = true;
                                    ImageButton btnUpload = (ImageButton) nameField.getTag(R.id.tag_upload_button);
                                    if (btnUpload != null) {
                                        btnUpload.setColorFilter(Color.RED);
                                    }
                                }
                            }
                        }
                    }
                }

                if (missingAdultName || missingChildName) {
                    UserUtils.getMessageFromLocal(164, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                    return;
                }

                if (missingPassportImage) {
                    UserUtils.getMessageFromLocal(261, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                    UserUtils.ToastMessages(getActivity(), "يرجى رفع صور جوازات السفر لجميع الركاب");
                    return;
                }

                // إذا وصل الكود هنا، يعني كل البيانات مكتملة
                String childNames = childNamesBuilder.toString();
                if (childNames.endsWith(", ")) {
                    childNames = childNames.substring(0, childNames.length() - 2);
                }

                String adultNames = adultNamesBuilder.toString().replaceAll(", $", "");
                int totalSeats = numSeats[0] + numChildren[0];
                double totalPrice = totalSeats * seatPrice;

                sendBookingRequest(
                        car_code, Integer.parseInt(trip_id2), totalSeats, pickupOrders, dropoffOrders,
                        passengerId, totalPrice, notes, driver_id,
                        car_codes_id, adultNames, childNames,
                        0, "waiting", 0, company_no,
                        adultsNamesContainer, childrenNamesContainer
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
        String originalName = getFileNameFromUri(fileUri); // الاسم الأصلي
        String uniqueID = UUID.randomUUID().toString();   // جزء فريد
        String fileName = uniqueID + "_" + originalName;   // الاسم النهائي الفريد
        String mimeType = getContext().getContentResolver().getType(fileUri);

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        out.writeBytes("Content-Type: " + mimeType + "\r\n");
        out.writeBytes("\r\n");
        InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        out.writeBytes("\r\n");
    }

    private void sendBookingRequest(String car_code, int tripId, int numSeats, int pickupOrder,
                                    int dropoffOrder, int passengerId,
                                    double totalPrice, String notes, String driver_id, String car_codes,
                                    String adult_names_str, String child_names_str, int pay_type, String payment_status,
                                    int request_id, int company_no,
                                    LinearLayout adultsContainer, LinearLayout childrenContainer) { // أضفنا الحاويات هنا

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
                writeFormField(dos, "request_id", String.valueOf(request_id), boundary);
                writeFormField(dos, "company_no", String.valueOf(company_no), boundary);

                // 2. رفع صور جوازات البالغين
                // داخل دالة sendBookingRequest - الجزء الخاص برفع الصور (الخطوة 3 و 4)

// 3. رفع صور جوازات البالغين
                if (adultsContainer != null) {
                    for (int i = 0; i < adultsContainer.getChildCount(); i++) {
                        View row = adultsContainer.getChildAt(i);

                        // التحقق أن العنصر الحالي هو LinearLayout (الصف)
                        if (row instanceof LinearLayout) {
                            LinearLayout rowLayout = (LinearLayout) row;

                            // التحقق أن الصف يحتوي على عناصر وأن أول عنصر هو EditText
                            if (rowLayout.getChildCount() > 0 && rowLayout.getChildAt(0) instanceof EditText) {
                                EditText nameField = (EditText) rowLayout.getChildAt(0);
                                Uri imageUri = (Uri) nameField.getTag();

                                if (imageUri != null) {
                                    writeFileField(dos, "passport_image" + (i + 1), imageUri, boundary);
                                }
                            }
                        }
                    }
                }

                if (childrenContainer != null) {
                    for (int i = 0; i < childrenContainer.getChildCount(); i++) {
                        View row = childrenContainer.getChildAt(i);

                        if (row instanceof LinearLayout) {
                            LinearLayout rowLayout = (LinearLayout) row;

                            if (rowLayout.getChildCount() > 0 && rowLayout.getChildAt(0) instanceof EditText) {
                                EditText nameField = (EditText) rowLayout.getChildAt(0);
                                Uri imageUri = (Uri) nameField.getTag();

                                if (imageUri != null) {
                                    writeFileField(dos, "passport_image" + (i + 1), imageUri, boundary);
                                }
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
                            dbHelper.addBooking(tripId, bookingIdFromServer, passengerId);

                            UserUtils.getMessageFromLocal(48, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            Fragment detailsFragment = new BookingDetailsFragment();
                            Bundle args = new Bundle();
                            args.putString("related_object_id", String.valueOf(bookingIdFromServer));
                            detailsFragment.setArguments(args);

                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.full_screen_container, detailsFragment)
                                    .addToBackStack(null).commit();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        UserUtils.sendLog(getContext(), "sendBookingRequest", response.toString(), "Error Status: " + status, "Home Fragment");
                    }
                });
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
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
