package com.example.musafir;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

public class BookingDetailsFragment extends Fragment {

    //    private TextView booking_id,startCityView,endCityView,dateView,timeView,drivercompany;
    String BASE_URL = UserUtils.BASE_URL;
    TextView IdBooking, tvDriverName, tvRoute, tvTime, tvDate, tvSeats, tvPrice,
            tvNotes, tvStatus, tvDriverCompany, cancellationReason, tvPaymentStatusBadge, detailsPay;
    LinearLayout notes, passengerContainer;
    //    ProgressBar progressBar;
    LinearLayout cancellationReasonCon;
    String ImageUrl = UserUtils.ImageUrl;

    ScrollView detailsCard;

    public BookingDetailsFragment() {
        // Required empty public constructor
    }

    LottieAnimationView lottieWave;

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false); // ❌ يخفي سهم الرجوع
        }
    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//
//        Toolbar toolbar = requireActivity().findViewById(R.id.main_toolbar);
//
//        // استرجاع الحالة الأصلية
//        toolbar.setNavigationIcon(oldNavigationIcon);
//        toolbar.setTitle(oldTitle);
//        toolbar.setNavigationOnClickListener(null); // إزالة حدث الرجوع
//    }

    CardView namePassengers;
    Button rateBtn, downloadTicketLayout, btnPayNow, btnContactSupport;
    ImageView vehicleImage;
    LinearLayout requestContainer, cardCancelBooking;
    //    ProgressBar progressBar;
    CardView cardRequest;
    Button btnCallDriver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_booking_details, container, false);
        setHasOptionsMenu(true);
        notes = itemView.findViewById(R.id.notes);
//        containerCompany = itemView.findViewById(R.id.containerCompany);
        tvDriverName = itemView.findViewById(R.id.tvName);
        tvDriverCompany = itemView.findViewById(R.id.tvDriverCompany);
        IdBooking = itemView.findViewById(R.id.IdBooking);
        tvRoute = itemView.findViewById(R.id.tvRoute);
        namePassengers = itemView.findViewById(R.id.namePassengers);
        tvTime = itemView.findViewById(R.id.tvTime);
        tvDate = itemView.findViewById(R.id.tvDate);
        tvSeats = itemView.findViewById(R.id.tvSeats);
        tvPrice = itemView.findViewById(R.id.tvPrice);
        tvNotes = itemView.findViewById(R.id.tvNotes);
        tvStatus = itemView.findViewById(R.id.tvStatus);
        lottieWave = itemView.findViewById(R.id.lottieWaveBookingD);
        detailsCard = itemView.findViewById(R.id.detailsCardBooking);
        vehicleImage = itemView.findViewById(R.id.vehicleImage);

        tvPaymentStatusBadge = itemView.findViewById(R.id.tvPaymentStatusBadge);
        cardCancelBooking = itemView.findViewById(R.id.cardCancelBooking);
        btnPayNow = itemView.findViewById(R.id.btnPayNow);
        btnContactSupport = itemView.findViewById(R.id.btnContactSupport);
        detailsPay = itemView.findViewById(R.id.detailsPay);


//        rateText = itemView.findViewById(R.id.rateText);
        rateBtn = itemView.findViewById(R.id.rateBtn);
        downloadTicketLayout = itemView.findViewById(R.id.downloadTicketLayout);
        cancellationReason = itemView.findViewById(R.id.cancellationReason);
        cancellationReasonCon = itemView.findViewById(R.id.cancellationReasonCon);
        passengerContainer = itemView.findViewById(R.id.passengerContainer);

        ImageView arrowIcon = itemView.findViewById(R.id.arrowIcon);
        ImageView arrowIcon2 = itemView.findViewById(R.id.arrowIcon2);
        btnCallDriver = itemView.findViewById(R.id.btn_call_driver);
        cardRequest = itemView.findViewById(R.id.cardRequest);
        requestContainer = itemView.findViewById(R.id.requestContainer);
        cardRequest.setOnClickListener(v -> {
            if (requestContainer.getVisibility() == GONE) {
                requestContainer.setVisibility(View.VISIBLE);
                arrowIcon2.setImageResource(R.drawable.baseline_keyboard_arrow_up_24);
            } else {
                requestContainer.setVisibility(GONE);
                arrowIcon2.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
            }
        });
        namePassengers.setOnClickListener(v -> {
            if (passengerContainer.getVisibility() == GONE) {
                passengerContainer.setVisibility(View.VISIBLE);
                arrowIcon.setImageResource(R.drawable.baseline_keyboard_arrow_up_24);
            } else {
                passengerContainer.setVisibility(GONE);
                arrowIcon.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
            }
        });
        String booking_id2;
        if (getArguments() != null) {
            booking_id2 = getArguments().getString("related_object_id");
            fetchBookingDetails(booking_id2);

        } else {
            booking_id2 = "";
        }
        DBHelper dbHelper = new DBHelper(getContext());

        UserUtils.fetchCashBankData(getContext(), dbHelper, new UserUtils.OnCashBankFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.CashBank> types) {
            }

            @Override
            public void onError(String error) {
            }
        });
        UserUtils.fetchCodeDetails(getContext(), 5, null, new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {
            }
        });

        btnContactSupport.setOnClickListener(v -> showContactSupportDialog(Integer.parseInt(booking_id2), 0));

        return itemView;
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

    private void fetchBookingDetails(String bookingId) {
        String url = BASE_URL + "bookings/" + bookingId;
        lottieWave.playAnimation();
        lottieWave.setVisibility(View.VISIBLE);
        detailsCard.setVisibility(GONE);
        DBHelper dbHelper = new DBHelper(getContext());
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("otp_token", otpToken);
//        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        @SuppressLint("SetTextI18n") JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        lottieWave.setVisibility(GONE);
                        lottieWave.cancelAnimation();
                        detailsCard.setVisibility(View.VISIBLE);
                        String seats = response.getString("number_of_seats");
                        String price2 = response.getString("total_price");
                        String car_codes = response.getString("car_codes_display");
                        String bookingStatus = response.getString("booking_status");
                        int booking_id = response.getInt("booking_id");
                        JSONObject tripInfo = response.getJSONObject("trip_info");
                        String startCity = tripInfo.getString("start_city");
                        String trip_id = response.getString("trip");
                        String endCity = tripInfo.getString("end_city");
                        String date = tripInfo.getString("departure_date");
                        String times = response.getString("Attendance_time");
                        String driverName = tripInfo.getString("driver_name");
                        String company_name = response.getString("company_name");
                        String booking_url = response.getString("booking_document");
                        String passengerNotes = response.optString("passenger_notes", "لا توجد");
                        String adult_names = response.optString("adult_names", "");
                        String child_names = response.optString("child_names", "");
                        String cancellation_reason = response.optString("cancellation_reason", "لا توجد");
                        String rating = response.optString("rating", "لا توجد");
                        String vehicle_image = tripInfo.optString("vehicle_image", "");
                        String vehicleMake = tripInfo.optString("make", "");
                        String vehicleType = tripInfo.optString("vehicle_type", "");
                        String driver_phone = tripInfo.optString("driver_phone", "");
                        int driver_id = response.optInt("driver_id", 0);
                        int pay_type = response.optInt("pay_type", 0);
                        int passenger_id = response.optInt("passenger", 0);

                        cardCancelBooking.setOnClickListener(v -> {
                            Activity activity = (Activity) v.getContext();

                            if (pay_type == 1 || "verified".equals(bookingStatus)) {
//                                cardCancelBooking.setVisibility(GONE);

                                showContactSupportDialog(booking_id, 1);
//                                return;
                            } else if ("cancelled".equals(bookingStatus) || "cancelled_by_driver".equals(bookingStatus) || "cancelled_by_passenger".equals(bookingStatus)) {

                                UserUtils.getMessageFromLocal(102, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(activity, message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }

                                });
                            } else if ("expired".equals(bookingStatus) || "closed".equals(bookingStatus)) {

                                cardCancelBooking.setClickable(false);
                            } else {

                                if (!bookingId.equals("-1")) {
                                    dbHelper.deleteBooking(Integer.parseInt(bookingId), passenger_id);
//                                    cancelListener.onCancelBooking(bookingId);
                                    showCancelReasonDialog(Integer.parseInt(bookingId));
                                }
                            }
                        });
                        switch (pay_type) {
                            case 0: // بانتظار الدفع (Initial/Pending)
                                tvPaymentStatusBadge.setText("انتظار الدفع");
                                tvPaymentStatusBadge.setTextColor(Color.parseColor("#CC9407")); // برتقالي داكن للنص
                                tvPaymentStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF5E6"))); // خلفية برتقالية فاتحة
                                btnPayNow.setVisibility(View.VISIBLE);
                                UserUtils.getMessageFromLocal(292, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        detailsPay.setText(message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        detailsPay.setText("حجزك غير مؤكد بعد، يرجى إتمام عملية الدفع لتأكيد حجزك.");
                                    }
                                });
                                break;

                            case 1: // تم التحقق (Verified/Success)
                                tvPaymentStatusBadge.setText("تم التحقق");
                                tvPaymentStatusBadge.setTextColor(Color.parseColor("#2E7D32")); // أخضر داكن
                                tvPaymentStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9"))); // خلفية خضراء فاتحة
                                btnPayNow.setVisibility(GONE);
//                                detailsPay.setText("تم استلام مبلغ الحجز بنجاح، نتمنى لك رحلة سعيدة.");
                                UserUtils.getMessageFromLocal(291, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        detailsPay.setText(message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        detailsPay.setText("تم استلام مبلغ الحجز بنجاح، نتمنى لك رحلة سعيدة.");
                                    }
                                });

                                break;

                            case 2: // قيد التحقق (Under Review)
                                tvPaymentStatusBadge.setText("قيد التحقق");
                                tvPaymentStatusBadge.setTextColor(Color.parseColor("#1E3A8A")); // أزرق داكن
                                tvPaymentStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DBEAFE"))); // خلفية زرقاء فاتحة
                                btnPayNow.setVisibility(GONE);
                                UserUtils.getMessageFromLocal(290, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        detailsPay.setText(message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        detailsPay.setText("تم استلام بيانات الدفع، جاري التحقق من قبل الإدارة حالياً.");
                                    }
                                });
                                break;

                            case 3: // دفع جزئي (Partial Payment)
                                tvPaymentStatusBadge.setText("دفع جزئي");
                                tvPaymentStatusBadge.setTextColor(Color.parseColor("#1E3A8A"));
                                tvPaymentStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F2FE")));
                                btnPayNow.setVisibility(View.VISIBLE);
                                UserUtils.getMessageFromLocal(289, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        detailsPay.setText(message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        detailsPay.setText("تم دفع جزء من المبلغ، يرجى سداد المتبقي لتأكيد الحجز بالكامل.");
                                    }
                                });
                                break;

                            case 4: // نقداً (Cash)
                                tvPaymentStatusBadge.setText("دفع نقداً");
                                tvPaymentStatusBadge.setTextColor(Color.parseColor("#4B5563")); // رمادي غامق
                                tvPaymentStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F3F4F6"))); // خلفية رمادية فاتحة
                                btnPayNow.setVisibility(GONE);
                                UserUtils.getMessageFromLocal(288, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        detailsPay.setText(message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        detailsPay.setText("تم اختيار الدفع نقداً عند الركوب، يرجى التواجد في الموعد المحدد.");
                                    }
                                });
                                break;

                            default: // حالة غير معروفة أو خطأ
                                tvPaymentStatusBadge.setText("غير معروف");
                                tvPaymentStatusBadge.setTextColor(Color.parseColor("#DC2626")); // أحمر
                                tvPaymentStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
                                btnPayNow.setVisibility(GONE);
                                UserUtils.getMessageFromLocal(287, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        detailsPay.setText(message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        detailsPay.setText("حدث خطأ في جلب حالة الدفع، يرجى التواصل مع الدعم الفني.");
                                    }
                                });
                                break;
                        }


                        double totalPriceDouble = Double.parseDouble(price2.replace(",", ""));
                        btnPayNow.setOnClickListener(new UserUtils.SingleClickListener() {
                            @Override
                            public void onSingleClick(View v) {

                                UserUtils.showGenericOptionsBottomSheet(getContext(), 5, totalPriceDouble, Integer.parseInt(trip_id), 2, booking_id,
                                        (payType, paymentStatus, success, request_id) -> {

                                            if (success) {
                                                fetchBookingDetails(bookingId);
                                                UserUtils.ToastMessages(getActivity(), "تمت عملية الدفع بنجاح");
                                            }
                                        });
                            }
                        });
                        int trip = response.optInt("trip", 0);
                        int number_status = response.getInt("number_status");
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

                            // إخفاء الخط للمرحلة الأخيرة
                            if (i == stagesNames.length - 1) {
                                lineView.setVisibility(GONE);
                            }

                            requestContainer.addView(stepView);
                        }
                        int numberOfSeats = Integer.parseInt(seats);
                        Glide.with(requireContext())
                                .load(ImageUrl + vehicle_image)
                                .placeholder(R.drawable.empty2)
                                .into(vehicleImage);

                        vehicleImage.setOnClickListener(v -> {
                            String carName = vehicleType + " " + vehicleMake;

                            String[] vehicleImages = {
                                    tripInfo.optString("vehicle_image"),
                                    tripInfo.optString("vehicle_image1"),
                                    tripInfo.optString("vehicle_image2"),
                                    tripInfo.optString("vehicle_image3"),
                                    tripInfo.optString("vehicle_image4")
                            };

                            ArrayList<String> imageList = new ArrayList<>();

                            for (String img : vehicleImages) {
                                // تحقق من null، فارغ، أو "null"
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
                        if ((adult_names == null || adult_names.isEmpty()) && (child_names == null || child_names.isEmpty())) {
                            namePassengers.setVisibility(GONE);
                        } else {
                            namePassengers.setVisibility(View.VISIBLE);
                        }

                        String pricePerSeat = response.optString("total_price", "0");

                        pricePerSeat = pricePerSeat.replace(",", "");

                        double price = Double.parseDouble(pricePerSeat);

                        double totalPrice = price;


                        TextView namesTextView = new TextView(getContext());
                        namesTextView.setTextSize(16);
                        namesTextView.setTextColor(Color.BLACK);
                        namesTextView.setPadding(8, 8, 8, 2);

                        StringBuilder namesBuilder = new StringBuilder();

                        if (!adult_names.isEmpty()) {
                            String[] adults = adult_names.split(",\\s*");
                            namesBuilder.append("البالغين:\n");
                            for (String name : adults) {
                                namesBuilder.append(name).append("\n");
                            }
                        }

                        if (!child_names.isEmpty()) {
                            String[] children = child_names.split(",\\s*");
                            namesBuilder.append("\nالأطفال:\n");
                            for (String name : children) {
                                namesBuilder.append(name).append("\n");
                            }
                        }

                        namesTextView.setText(namesBuilder.toString());
                        passengerContainer.addView(namesTextView);

                        if (bookingStatus.equals("closed") || bookingStatus.equals("in Way")) {
                            rateBtn.setVisibility(View.VISIBLE);

                            if (rating.equals("false")) {
                                rateBtn.setOnClickListener(v -> {

                                    UserUtils.showRatingDialog(getContext(), driver_id, trip);

                                });
                            } else if (rating.equals("true")) {
                                rateBtn.setOnClickListener(v -> {
                                    UserUtils.getMessageFromLocal(21, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            UserUtils.ToastMessages(getActivity(), message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }

                                    });

                                });
                            }
                        }
                        if (bookingStatus.equals("verified")) {
                            downloadTicketLayout.setVisibility(View.VISIBLE);

                            downloadTicketLayout.setOnClickListener(v -> {

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(booking_url));

                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                v.getContext().startActivity(intent);

                            });
                        }
                        if (company_name == null || company_name.isEmpty() || "null".equals(company_name)) {
                            tvDriverCompany.setVisibility(GONE);
                        } else {
                            tvDriverCompany.setVisibility(View.VISIBLE);
                            tvDriverCompany.setText(company_name);
                        }
                        tvDriverName.setText(driverName);
                        IdBooking.setText("رقم الرحلة: " + trip_id);
                        tvRoute.setText((startCity.isEmpty() ? "?" : startCity) + "  - " + (endCity.isEmpty() ? "?" : endCity));
                        String formattedTime;
                        try {
                            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                            Date time = inputFormat.parse(times);
                            formattedTime = outputFormat.format(time).replace("AM", "ص").replace("PM", "م");
                        } catch (Exception e) {
                            formattedTime = times; // إذا كان فارغ أو خطأ
                        }
                        tvTime.setText(formattedTime);

                        tvDate.setText(date);
                        if (numberOfSeats == 1) {
                            tvSeats.setText("1 مقعد");
                        } else if (numberOfSeats >= 11) {
                            tvSeats.setText(numberOfSeats + " مقعد");
                        } else {
                            tvSeats.setText(numberOfSeats + " مقاعد");
                        }
//                        tvSeats.setText(seats);
                        tvPrice.setText(String.format(Locale.ENGLISH, "%,.0f %s", totalPrice, car_codes));
                        if (cancellation_reason.isEmpty()) {
                            cancellationReasonCon.setVisibility(GONE);
                        } else {
                            cancellationReasonCon.setVisibility(View.VISIBLE);
                            cancellationReason.setText(cancellation_reason);

                        }
                        if (passengerNotes.isEmpty()) {
                            notes.setVisibility(GONE);
                        } else {
                            notes.setVisibility(View.VISIBLE);
                            tvNotes.setText(passengerNotes);

                        }

                        GradientDrawable background = (GradientDrawable) tvStatus.getBackground();

                        switch (bookingStatus) {
                            case "verified":
                                tvStatus.setText("مؤكد");
                                tvStatus.setTextColor(Color.parseColor("#2E7D32")); // أخضر غامق
                                background.setColor(Color.parseColor("#C8E6C9")); // أخضر فاتح
                                btnCallDriver.setVisibility(View.VISIBLE);
                                btnCallDriver.setOnClickListener(v2 -> {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + driver_phone));
                                    startActivity(intent);
                                });
                                break;

                            case "cancelled":
                            case "cancelled_by_driver":
                            case "cancelled_by_passenger":
                                btnCallDriver.setVisibility(View.GONE);

                                tvStatus.setText("ملغي");
                                tvStatus.setTextColor(Color.parseColor("#ef4444"));
                                background.setColor(Color.parseColor("#fdecec")); // أحمر
                                btnPayNow.setVisibility(GONE);
                                UserUtils.getMessageFromLocal(286, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        detailsPay.setText(message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        detailsPay.setText("تم إلغاء هذه الرحلة. إذا كنت قد قمت بدفع الرسوم مسبقاً، يرجى التواصل مع خدمة العملاء لاسترداد المبلغ أو تحويله لرحلة أخرى.");
                                    }
                                });
                                detailsPay.setTextColor(Color.parseColor("#991b1b"));
                                btnContactSupport.setVisibility(View.VISIBLE);
                                break;

                            case "pending":
                                btnCallDriver.setVisibility(View.GONE);

                                tvStatus.setText("قيد المعالجة");
                                tvStatus.setTextColor(Color.parseColor("#CC9407"));
                                background.setColor(Color.parseColor("#fef5e6")); // برتقالي
                                break;

                            case "expired":
                                btnCallDriver.setVisibility(View.GONE);

                                tvStatus.setText("منتهي");
                                tvStatus.setTextColor(Color.parseColor("#9CA3AF")); // رمادي داكن
                                background.setColor(Color.parseColor("#F3F4F6")); // رمادي فاتح
                                break;


                            default:
                                btnCallDriver.setVisibility(View.GONE);

                                tvStatus.setText("مغلقة");
                                tvStatus.setTextColor(Color.parseColor("#1E3A8A")); // نص أزرق داكن
                                background.setColor(Color.parseColor("#DBEAFE")); // خلفية أزرق فاتح

                                break;
                        }
                    } catch (Exception e) {
                        UserUtils.sendLog(getContext(), "fetchBookingDetails", e.toString(), e.toString(), "Booking Details");
                    }
                },
                error -> {
                    lottieWave.setVisibility(GONE);
                    lottieWave.cancelAnimation();
                    detailsCard.setVisibility(GONE);
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

    private AlertDialog exitDialog;

    private void showCancelReasonDialog(int bookingId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        DBHelper dbHelper = new DBHelper(getContext());

        // --- الديالوج الأول ---
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_cancel_booking, null);
        builder.setView(dialogView);
        EditText input = dialogView.findViewById(R.id.etReason);
        Button btnAdd = dialogView.findViewById(R.id.btnYes);
        Button btnCancel = dialogView.findViewById(R.id.btnNo);

        AlertDialog dialog = builder.create();

        // إضافة ضبابية للديالوج الأول
//        Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
//        dialog.setOnDismissListener(d -> Blurry.delete(decorView));

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialog.show();

        UserUtils.setEditTextState(input, false);

        btnAdd.setOnClickListener(v -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) {
                input.setError("الرجاء إدخال سبب الإلغاء");
                UserUtils.setEditTextState(input, true);
                return;
            } else {
                UserUtils.setEditTextState(input, false);
            }
            if (getActivity() == null || getActivity().isFinishing()) return;

            // --- الديالوج الثاني (تأكيد الإلغاء) ---
            AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
            View dialogView2 = inflater.inflate(R.layout.dialog_custom_confirmationt, null);
            builder2.setView(dialogView2);

            Button btnYes = dialogView2.findViewById(R.id.btnYes);
            Button btnNo = dialogView2.findViewById(R.id.btnNo);
            TextView tvMessage = dialogView2.findViewById(R.id.tvMessage);
            tvMessage.setText("هل أنت متأكد أنك تريد إلغاء الحجز؟");
            btnYes.setTextSize(18);
            btnNo.setTextSize(18);

            exitDialog = builder2.create();

            // إضافة ضبابية للديالوج الثاني
//            Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
//            exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));

            dialog.dismiss(); // إخفاء الأول

            btnYes.setOnClickListener(v2 -> {
                String cancellationDate = getCurrentDateTime();
                UserUtils.getMessageFromLocal(50, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(getActivity(), message);
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
                sendCancelRequest(requireContext(), bookingId, reason, cancellationDate);
                exitDialog.dismiss();
            });

            btnNo.setOnClickListener(v2 -> exitDialog.dismiss());

            if (exitDialog.getWindow() != null) {
                exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
            }

            if (isAdded() && !getActivity().isFinishing()) {
                exitDialog.show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static int currentPage = 1;


    private void sendCancelRequest(Context context, int bookingsId, String reason, String
            cancellationDate) {
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "bookings/" + bookingsId + "/cancel/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                conn.setDoOutput(true);
                SharedPreferences prefs = SharedPrefsHelper.get(context);

//                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cancellation_reason", reason);
                jsonObject.put("cancellation_date", cancellationDate);
                String s = "{ cancellation_reason: " + reason +
                        " , cancellation_date: " + cancellationDate +
                        " }";
                String user_type = prefs.getString("user_type", "");
                if ("driver".equals(user_type)) {
                    jsonObject.put("booking_status", "cancelled_by_driver");
                } else {
                    jsonObject.put("booking_status", "cancelled_by_passenger");
                }
                String jsonString = jsonObject.toString();

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }


                int responseCode = conn.getResponseCode();
                InputStream inputStream = (responseCode >= 400) ? conn.getErrorStream() : conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line.trim());
                }
                reader.close();

                String responseStr = responseBuilder.toString();

                ((Activity) context).runOnUiThread(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        UserUtils.getMessageFromLocal(51, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        tvStatus.setText("ملغي");
                        tvStatus.setTextColor(Color.parseColor("#ef4444"));

                        GradientDrawable background = (GradientDrawable) tvStatus.getBackground();
                        if (background != null) {
                            background.setColor(Color.parseColor("#fdecec")); // خلفية حمراء فاتحة
                        }
                        UserUtils.getMessageFromLocal(286, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                detailsPay.setText(message);
                            }

                            @Override
                            public void onError(String error) {
                                detailsPay.setText("تم إلغاء هذه الرحلة. إذا كنت قد قمت بدفع الرسوم مسبقاً، يرجى التواصل مع خدمة العملاء لاسترداد المبلغ أو تحويله لرحلة أخرى.");
                            }
                        });
                        detailsPay.setTextColor(Color.parseColor("#991b1b"));
                        btnPayNow.setVisibility(View.GONE);
                        btnContactSupport.setVisibility(View.VISIBLE);

                        if (cardCancelBooking != null) {
                            cardCancelBooking.setVisibility(View.GONE);
                        }

                        currentPage = 1;


//                        fetchBookingDetails(String.valueOf(bookingsId));
                    } else {
                        UserUtils.sendLog(getContext(), "sendCancelRequest", responseStr, s, "My Bookings");
                        UserUtils.getMessageFromLocal(52, dbHelper, new UserUtils.MessageCallback() {
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

            } catch (Exception e) {
                ((Activity) context).runOnUiThread(() -> {
                    UserUtils.sendLog(getContext(), "sendCancelRequest", e.toString(), e.toString(), "My Bookings");
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private void showContactSupportDialog(int bookingId, int verified) {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) return;

        // إعداد الديالوج المخصص
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
        Dialog customDialog = new Dialog(activity);
        customDialog.setContentView(dialogView);

        if (customDialog.getWindow() != null) {
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // تطبيق تأثير البلر
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        Blurry.with(getActivity()).radius(15).sampling(2).onto(decorView);
        customDialog.setOnDismissListener(d -> Blurry.delete(decorView));

        LinearLayout btnWhatsapp = dialogView.findViewById(R.id.btnWhatsapp);
        Button btnCall = dialogView.findViewById(R.id.btnCall);
        View btnClose = dialogView.findViewById(R.id.dialogCancelButton);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        DBHelper dbHelper = new DBHelper(getContext());
        if (verified == 0) {
            UserUtils.getMessageFromLocal(293, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    tvMessage.setText(message);
                }

                @Override
                public void onError(String error) {
                    tvMessage.setText("تم الغاء الجحز. يرجى التواصل مع خدمة العملاء للمساعدة.");
                }
            });
        } else {
            UserUtils.getMessageFromLocal(294, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    tvMessage.setText(message);
                }

                @Override
                public void onError(String error) {
                    tvMessage.setText("حجزك مؤكد. لمزيد من التفاصيل أو للاستفسار، يسعدنا تواصلك مع خدمة العملاء.");
                }
            });
        }
        btnWhatsapp.setOnClickListener(v -> {
            SharedPreferences prefs = SharedPrefsHelper.get(getContext());
            String whatsappNo = prefs.getString("whatsapp_no", "967785050270");
            String message = "بخصوص الحجز الملغي رقم: " + bookingId;
            try {
                String url = "https://api.whatsapp.com/send?phone=" + whatsappNo + "&text=" + URLEncoder.encode(message, "UTF-8");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "واتساب غير مثبت", Toast.LENGTH_SHORT).show();
            }
        });

        btnCall.setOnClickListener(v -> {
            SharedPreferences prefs = SharedPrefsHelper.get(getContext());
            String phoneNo = prefs.getString("phone_no", "785050270");
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNo));
            startActivity(intent);
        });

        if (btnClose != null) btnClose.setOnClickListener(v -> customDialog.dismiss());

        customDialog.show();
    }
}