package com.example.musafir;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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
import java.util.Queue;

import jp.wasabeef.blurry.Blurry;

public class BookingDetailsFragment extends Fragment {

    //    private TextView booking_id,startCityView,endCityView,dateView,timeView,drivercompany;
    String BASE_URL = UserUtils.BASE_URL;
    TextView IdBooking, tvDriverName, tvRoute, tvTime, tvDate, tvSeats, tvPrice,
            tvNotes, tvStatus, tvDriverCompany, cancellationReason, tvPaymentStatusBadge, detailsPay,
            tvOrderTime;
    LinearLayout notes, passengerContainer;
    //    ProgressBar progressBar;
    LinearLayout cancellationReasonCon;
    String ImageUrl = UserUtils.ImageUrl;

    MaterialCardView state_card;
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
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    CardView namePassengers;
    Button rateBtn, btnPayNow, btnContactSupport, btnCallDriver;
    MaterialCardView downloadTicketLayout;
    ImageView vehicleImage, imgStatus;
    LinearLayout requestContainer, cardCancelBooking;
    //    ProgressBar progressBar;
    CardView cardRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_booking_details, container, false);
        setHasOptionsMenu(true);
        notes = itemView.findViewById(R.id.notes);
        DBHelper dbHelper = new DBHelper(getContext());
        tvDriverName = itemView.findViewById(R.id.tvName);
        tvDriverCompany = itemView.findViewById(R.id.tvDriverCompany);
        tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
        state_card = itemView.findViewById(R.id.state_card);
        IdBooking = itemView.findViewById(R.id.IdBooking);
        tvRoute = itemView.findViewById(R.id.tvRoute);
        namePassengers = itemView.findViewById(R.id.namePassengers);
        tvTime = itemView.findViewById(R.id.tvTime);
        tvDate = itemView.findViewById(R.id.tvDate);
        tvSeats = itemView.findViewById(R.id.tvSeats);
        tvPrice = itemView.findViewById(R.id.tvPrice);
        tvNotes = itemView.findViewById(R.id.tvNotes);
        imgStatus = itemView.findViewById(R.id.imgStatus);
        tvStatus = itemView.findViewById(R.id.tvStatus);
        lottieWave = itemView.findViewById(R.id.lottieWaveBookingD);
        detailsCard = itemView.findViewById(R.id.detailsCardBooking);
        vehicleImage = itemView.findViewById(R.id.vehicleImage);
        TextView NotePrice = itemView.findViewById(R.id.NotePrice);
        NotePrice.setText(UserUtils.getMessageFromLocalNew(361, dbHelper));
        tvPaymentStatusBadge = itemView.findViewById(R.id.tvPaymentStatusBadge);
        cardCancelBooking = itemView.findViewById(R.id.cardCancelBooking);
        btnPayNow = itemView.findViewById(R.id.btnPayNow);
        btnContactSupport = itemView.findViewById(R.id.btnContactSupport);
        detailsPay = itemView.findViewById(R.id.detailsPay);
        View dividerPayment = itemView.findViewById(R.id.dividerPayment);
        LinearLayout layoutPayAction = itemView.findViewById(R.id.layoutPayAction);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        String user_type = prefs.getString("user_type", "");

        if (user_type.equals("driver")) {
            dividerPayment.setVisibility(GONE);
            layoutPayAction.setVisibility(GONE);
        } else {
            dividerPayment.setVisibility(View.VISIBLE);
            layoutPayAction.setVisibility(View.VISIBLE);
        }

//        rateText = itemView.findViewById(R.id.rateText);
        rateBtn = itemView.findViewById(R.id.rateBtn);
        downloadTicketLayout = itemView.findViewById(R.id.downloadTicketLayout);
        cancellationReason = itemView.findViewById(R.id.cancellationReason);
        cancellationReasonCon = itemView.findViewById(R.id.cancellationReasonCon);
        passengerContainer = itemView.findViewById(R.id.passengerContainer);

        ImageView arrowIcon = itemView.findViewById(R.id.arrowIcon);
        ImageView arrowIcon2 = itemView.findViewById(R.id.arrowIcon2);
        btnCallDriver = itemView.findViewById(R.id.btnCallDriver);
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
        UserUtils.fetchBalance(getContext());

        String booking_id2;
        if (getArguments() != null) {
            booking_id2 = getArguments().getString("related_object_id");
            fetchBookingDetails(booking_id2);

        } else {
            booking_id2 = "";
        }

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
        lottieWave.setVisibility(View.VISIBLE);
        lottieWave.playAnimation();
        detailsCard.setVisibility(GONE);
        DBHelper dbHelper = new DBHelper(getContext());
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        String token = prefs.getString("auth_token", null);
        @SuppressLint("SetTextI18n")
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
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
//                        JSONObject passengers_details = response.getJSONObject("passengers_details");
                        String startCity = tripInfo.getString("start_city");
                        String trip_id = response.getString("trip");
                        String endCity = tripInfo.getString("end_city");
                        String date = tripInfo.getString("departure_date");
                        String times = response.getString("Attendance_time");
                        String driverName = tripInfo.getString("driver_name");
                        String company_name = response.getString("company_name");
                        String booking_url = response.getString("booking_document");
                        String passengerNotes = response.optString("passenger_notes", "لا توجد");
                        String cancellation_reason = response.optString("cancellation_reason", "لا توجد");
                        String rating = response.optString("rating", "لا توجد");
                        String vehicle_image = tripInfo.optString("vehicle_image", "");
                        String vehicleMake = tripInfo.optString("make", "");
                        String vehicleType = tripInfo.optString("vehicle_type", "");
                        String driver_phone = tripInfo.optString("driver_phone", "");
                        String payment_status = response.optString("payment_status", "");
                        String car_code = response.optString("car_code", "");
                        int driver_id = response.optInt("driver_id", 0);
                        int pay_type = response.optInt("pay_type", 0);
                        int passenger_id = response.optInt("passenger", 0);
                        String booking_date = response.optString("booking_date", "");
                        if (booking_date != null && !booking_date.isEmpty()) {
                            tvOrderTime.setText(UserUtils.getTimeAgo(booking_date));
                        } else {
                            tvOrderTime.setText("منذ قليل");
                        }
                        cardCancelBooking.setOnClickListener(v -> {
                            Activity activity = (Activity) v.getContext();

                            if ("verified".equals(bookingStatus)) {
                                showContactSupportDialog(booking_id, 1);
                            } else if ("verified".equals(payment_status)) {
                                showContactSupportDialog(booking_id, 2);
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

                        int totalPriceRaw = (int) Double.parseDouble(price2.replace(",", ""));
                        String balanceStr = SharedPrefsHelper.get(getContext()).getString("user_balance", "0");
                        double userBalance = Double.parseDouble(balanceStr.replace(",", "").trim());

                        if (userBalance < 0) {
                            userBalance = 0;
                        }

                        int totalPriceAfterBalance = (int) Math.max(0, totalPriceRaw - userBalance);

                        if (totalPriceAfterBalance == 0) {
                            if (car_code != null) {
                                if (car_code.contains("YER")) {
                                    totalPriceAfterBalance = 1000;
                                } else if (car_code.contains("SAR")) {
                                    totalPriceAfterBalance = 10;
                                }
                            }
                        }

                        int totalPriceDouble = totalPriceAfterBalance;


                        tvPaymentStatusBadge.setOnClickListener(null);
                        tvPaymentStatusBadge.setClickable(false);

                        boolean isCancelled = "cancelled".equals(bookingStatus) ||
                                "cancelled_by_driver".equals(bookingStatus) ||
                                "cancelled_by_passenger".equals(bookingStatus);

                         if ("waiting".equals(payment_status)) {
                             if (isCancelled) {
                                 tvPaymentStatusBadge.setClickable(false);
                                 tvPaymentStatusBadge.setOnClickListener(null);
                             }
                            else if ("verified".equals(bookingStatus) || "pending".equals(bookingStatus)) {
                                tvPaymentStatusBadge.setClickable(true);
                                tvPaymentStatusBadge.setOnClickListener(new UserUtils.SingleClickListener() {
                                    @Override
                                    public void onSingleClick(View v) {
                                        UserUtils.showUnifiedPaymentBottomSheet(getContext(),
                                                5,
                                                totalPriceDouble,
                                                Integer.parseInt(trip_id), car_code, 2,
                                                booking_id, 1,
                                                (payType, paymentStatus, success, request_id) -> {
                                                    if (success) {
                                                        fetchBookingDetails(bookingId);
//                                                        UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(325, dbHelper));
                                                    }
                                                });
                                    }
                                });
                            }
                        } else if ("verified".equals(payment_status)) {
                            int userId = prefs.getInt("user_id", 0);
                            tvPaymentStatusBadge.setClickable(true);
                            tvPaymentStatusBadge.setOnClickListener(new UserUtils.SingleClickListener() {
                                @Override
                                public void onSingleClick(View v) {
                                    fetchBalanceDetails(userId, car_code);
                                }
                            });
                        }
//                        else if ("on_verfy".equals(payment_status)) {
//                            tvPaymentStatusBadge.setClickable(true);
//                            tvPaymentStatusBadge.setOnClickListener(v -> {
//                                UserUtils.ToastMessages(getActivity(), "عملية الدفع قيد المراجعة حالياً، يرجى الانتظار.");
//                            });
//                        }
                        switch (payment_status) {
                            case "waiting":
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
//                                        detailsPay.setText(UserUtils.getMessageFromLocalNew(331, dbHelper));
                                    }
                                });
                                break;

                            case "verified": // تم التحقق (Verified/Success)
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
                                        detailsPay.setText(UserUtils.getMessageFromLocalNew(330, dbHelper));
                                    }
                                });

                                break;

                            case "on_verfy":
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
                                        detailsPay.setText(UserUtils.getMessageFromLocalNew(329, dbHelper));
                                    }
                                });
                                break;

                            case "partial": // دفع جزئي (Partial Payment)
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
                                        detailsPay.setText(UserUtils.getMessageFromLocalNew(328, dbHelper));
                                    }
                                });
                                break;

                            case "cash": // نقداً (Cash)
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
                                        detailsPay.setText(UserUtils.getMessageFromLocalNew(327, dbHelper));
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
                                        detailsPay.setText(UserUtils.getMessageFromLocalNew(326, dbHelper));
                                    }
                                });
                                break;
                        }

//                        GradientDrawable background = (GradientDrawable) state_card.getBackground();
                        switch (bookingStatus.trim().toLowerCase()) {
                            case "verified":
                                tvStatus.setText("مؤكد");
                                imgStatus.setImageTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
                                tvStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
//                                background.setColor(Color.parseColor("#C8E6C9")); // أخضر فاتح
                                state_card.setCardBackgroundColor(Color.parseColor("#C8E6C9"));
//                                if (type)
                                btnCallDriver.setVisibility(GONE);
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
                                imgStatus.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ef4444")));
                                tvStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#ef4444")));
                                state_card.setCardBackgroundColor(Color.parseColor("#fdecec"));
                                btnPayNow.setVisibility(GONE);
                                UserUtils.getMessageFromLocal(286, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        detailsPay.setText(message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        detailsPay.setText(UserUtils.getMessageFromLocalNew(324, dbHelper));
                                    }
                                });
                                detailsPay.setTextColor(Color.parseColor("#991b1b"));
                                btnContactSupport.setVisibility(View.VISIBLE);
                                break;
                            case "pending":
                                btnCallDriver.setVisibility(View.GONE);
                                tvStatus.setText("قيد المعالجة");
                                imgStatus.setImageTintList(ColorStateList.valueOf(Color.parseColor("#CC9407")));
                                tvStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#CC9407")));
//                                background.setColor(Color.parseColor("#fef5e6"));
                                state_card.setCardBackgroundColor(Color.parseColor("#fef5e6")); // التعديل هنا
                                break;
                            case "expired":
                                btnCallDriver.setVisibility(View.GONE);
                                tvStatus.setText("منتهي");
                                imgStatus.setImageTintList(ColorStateList.valueOf(Color.parseColor("#9CA3AF")));
                                tvStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#9CA3AF")));
//                                background.setColor(Color.parseColor("#F3F4F6"));
                                state_card.setCardBackgroundColor(Color.parseColor("#F3F4F6")); // التعديل هنا
                                break;
                            default:
                                btnCallDriver.setVisibility(View.GONE);
                                tvStatus.setText("مغلقة");
                                imgStatus.setImageTintList(ColorStateList.valueOf(Color.parseColor("#1E3A8A")));
                                tvStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#1E3A8A")));
//                                background.setColor(Color.parseColor("#DBEAFE"));
                                state_card.setCardBackgroundColor(Color.parseColor("#DBEAFE")); // التعديل هنا
                                break;
                        }

                        btnPayNow.setOnClickListener(new UserUtils.SingleClickListener() {
                            @Override
                            public void onSingleClick(View v) {
                                UserUtils.fetchAndSavePayTypes(getContext(), new UserUtils.GenericCallback() {

                                    @Override
                                    public void onSuccess(String message) {
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                                UserUtils.fetchCashBankData(getContext(), dbHelper, new UserUtils.OnCashBankFetchedListener() {
                                    @Override
                                    public void onFetched(List<DBHelper.CashBank> types) {
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });

                                UserUtils.showUnifiedPaymentBottomSheet(getContext(), 5, totalPriceDouble, Integer.parseInt(trip_id), car_code, 2, booking_id, 1,
                                        (payType, paymentStatus, success, request_id) -> {

                                            if (success) {
                                                fetchBookingDetails(bookingId);
//                                                UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(325, dbHelper));
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

                        String pricePerSeat = response.optString("total_price", "0");

                        pricePerSeat = pricePerSeat.replace(",", "");

                        double price = Double.parseDouble(pricePerSeat);

                        double totalPrice = price;
                        try {
                            JSONArray passengersArray = response.getJSONArray("passengers_details");

                            if (passengersArray.length() > 0) {
                                namePassengers.setVisibility(View.VISIBLE);
                                passengerContainer.removeAllViews(); // تنظيف الحاوية

                                StringBuilder adultsBuilder = new StringBuilder();
                                StringBuilder childrenBuilder = new StringBuilder();

                                for (int i = 0; i < passengersArray.length(); i++) {
                                    JSONObject passenger = passengersArray.getJSONObject(i);
                                    String name = passenger.optString("passenger_name", "غير معروف");
                                    String visa = passenger.optString("visa_type", "");
                                    int isChild = passenger.optInt("is_child", 0);

                                    // تجهيز نص الاسم مع التأشيرة بجانبه
                                    String personLine = "• " + name;
                                    if (!visa.isEmpty() && !visa.equals("null")) {
                                        personLine += " (" + visa + ")"; // التأشيرة بجانب الاسم
                                    }
                                    personLine += "\n";

                                    // فرز المسافرين بناءً على is_child
                                    if (isChild == 1) {
                                        childrenBuilder.append(personLine);
                                    } else {
                                        adultsBuilder.append(personLine);
                                    }
                                }

                                // بناء النص النهائي للعرض
                                StringBuilder finalOutput = new StringBuilder();

                                if (adultsBuilder.length() > 0) {
                                    finalOutput.append("البالغين:\n").append(adultsBuilder).append("\n");
                                }

                                if (childrenBuilder.length() > 0) {
                                    finalOutput.append("الأطفال:\n").append(childrenBuilder);
                                }

                                // إنشاء الـ TextView وعرض النص
                                TextView namesTextView = new TextView(getContext());
                                namesTextView.setTextSize(15);
                                namesTextView.setTextColor(Color.BLACK);
                                namesTextView.setLineSpacing(1.2f, 1.1f);
                                namesTextView.setText(finalOutput.toString().trim());

                                passengerContainer.addView(namesTextView);

                            } else {
                                namePassengers.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
//                            e.printStackTrace();
                            namePassengers.setVisibility(View.GONE);
                        }

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
                                if (booking_url != null && !booking_url.isEmpty() && !booking_url.equals("null")) {
                                    try {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(booking_url));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        v.getContext().startActivity(intent);
                                    } catch (Exception e) {
                                        UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(488, dbHelper));
                                    }
                                } else {
                                    UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(489, dbHelper));
                                }
                            });
                        }
//                        if (bookingStatus.equals("verified")) {
//                            downloadTicketLayout.setVisibility(View.VISIBLE);
//
//                            downloadTicketLayout.setOnClickListener(v -> {
//                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(booking_url));
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                v.getContext().startActivity(intent);
//                            });
//                        }
                        if (company_name == null || company_name.isEmpty() || "null".equals(company_name)) {
                            tvDriverCompany.setVisibility(GONE);
                        } else {
                            tvDriverCompany.setVisibility(View.VISIBLE);
                            tvDriverCompany.setText(company_name);
                        }
                        tvDriverName.setText(driverName);
                        IdBooking.setText(trip_id);
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

                    } catch (Exception e) {
                        UserUtils.sendLog(getContext(), "fetchBookingDetails", e.toString(), e.toString(), "Booking Details");
                    }
                },
                error -> {
                    lottieWave.setVisibility(GONE);
                    lottieWave.cancelAnimation();

                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });

                    UserUtils.sendLog(getContext(), "fetchBookingDetails", error.toString(), "bookingId: " + bookingId, "BookingDetails");
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
        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, // وقت الانتظار بالمللي ثانية (15 ثانية)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // عدد المحاولات (عادة 1)
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        queue.add(request);
    }

    private AlertDialog exitDialog;

    private void showCancelReasonDialog(int bookingId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        DBHelper dbHelper = new DBHelper(getContext());

        ViewGroup rootLayout = (ViewGroup) getActivity().getWindow().getDecorView().getRootView();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_cancel_booking, null);
        builder.setView(dialogView);

        EditText input = dialogView.findViewById(R.id.etReason);
        Button btnAdd = dialogView.findViewById(R.id.btnYes);
        Button btnCancel = dialogView.findViewById(R.id.btnNo);

        AlertDialog dialog = builder.create();

        // جعل خلفية النافذة شفافة لتظهر حواف bg_dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // تفعيل الضبابية عند العرض
        dialog.setOnShowListener(dialogInterface -> {
            Blurry.with(getContext()).radius(15).sampling(2).onto(rootLayout);
        });

        // حذف الضبابية عند الإغلاق
        dialog.setOnDismissListener(dialogInterface -> Blurry.delete(rootLayout));

        dialog.show();

        // ضبط عرض الديالوج
        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.80);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        UserUtils.setEditTextState(input, false);

        btnAdd.setOnClickListener(v -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) {
                input.setError("الرجاء إدخال سبب الإلغاء");
                UserUtils.setEditTextState(input, true);
                return;
            }
            dialog.dismiss();
            showConfirmationDialog(bookingId, reason, dbHelper, rootLayout);
        });

        RelativeLayout btnCloseHeader = dialogView.findViewById(R.id.dialogCancelButton);
        if (btnCloseHeader != null) {
            btnCloseHeader.setOnClickListener(v1 -> dialog.dismiss());
        }
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showConfirmationDialog(int bookingId, String reason, DBHelper dbHelper, ViewGroup rootLayout) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
        View dialogView2 = getLayoutInflater().inflate(R.layout.dialog_custom_confirmationt, null);
        builder2.setView(dialogView2);

        Button btnYes = dialogView2.findViewById(R.id.btnYes);
        Button btnNo = dialogView2.findViewById(R.id.btnNo);
        TextView tvMessage = dialogView2.findViewById(R.id.tvMessage);
        tvMessage.setText("هل أنت متأكد أنك تريد إلغاء الحجز؟");

        AlertDialog exitDialog = builder2.create();

        // تطبيق الـ Blur للديالوج الثاني أيضاً
        exitDialog.setOnShowListener(d -> Blurry.with(getContext()).radius(15).sampling(2).onto(rootLayout));
        exitDialog.setOnDismissListener(d -> Blurry.delete(rootLayout));

        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        exitDialog.show();
        if (exitDialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.80);
            exitDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
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
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static int currentPage = 1;


    private void sendCancelRequest(Context context, int bookingsId, String reason, String
            cancellationDate) {
        DBHelper dbHelper = new DBHelper(getContext());
        if (!UserUtils.isNetworkAvailable(getContext())) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(getActivity(), message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
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
                        tvPaymentStatusBadge.setClickable(false);
                        tvPaymentStatusBadge.setOnClickListener(null);
                        tvStatus.setText("ملغي");
                        imgStatus.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ef4444")));
                        tvStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#ef4444")));
                        state_card.setCardBackgroundColor(Color.parseColor("#fdecec"));
                        GradientDrawable background = (GradientDrawable) tvStatus.getBackground();
                        if (background != null) {
                            background.setColor(Color.parseColor("#fdecec"));
                        }
                        UserUtils.getMessageFromLocal(286, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {

                                detailsPay.setText(message);
                            }

                            @Override
                            public void onError(String error) {
                                detailsPay.setText(UserUtils.getMessageFromLocalNew(324, dbHelper));
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
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
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

    private void fetchBalanceDetails(int user_id, String curCode) {
        if (user_id == 0 || !isAdded()) return;

        Dialog loadingDialog = showEmptyDialog();
        if (loadingDialog == null) return;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        // ملاحظة: currentPage يجب تصفيره إلى 1 عند أول طلب، تأكد من ذلك في مكان الاستدعاء
        String url = UserUtils.BASE_URL + "user-balance-details/?ac_code_dtl=" + user_id + "&page=1";
        if (curCode != null && !curCode.isEmpty()) {
            url += "&cur_code=" + curCode;
        }
        String finalUrl = url;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    if (!isAdded()) return;
                    try {
                        JSONArray results = response.getJSONArray("results");

                        if (results.length() > 0) {
                            JSONObject firstDetail = results.getJSONObject(0);
                            // تحديث البيانات وإظهار المحتوى
                            updateDialogWithData(loadingDialog, firstDetail);
                        } else {
                            // إذا لم توجد نتائج
                            TextView tvTitle = loadingDialog.findViewById(R.id.tvTitle);
                            tvTitle.setText("لا توجد بيانات");
                            TextView tvMessage = loadingDialog.findViewById(R.id.tvMessage);
                            tvMessage.setText("لم يتم العثور على تفاصيل لهذه العملية حالياً.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        loadingDialog.dismiss();
                    }
                },
                error -> {
                    if (isAdded()) {
                        loadingDialog.dismiss();
                        UserUtils.ToastMessages(getActivity(), "فشل جلب تفاصيل الرصيد");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SharedPrefsHelper.get(getContext()).getString("auth_token", ""));
                return headers;
            }
        };
        request.setTag("balance_details");
        queue.add(request);
    }

    private Dialog showEmptyDialog() {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) return null;

        // استخدام نفس التصميم الموحد للرسائل في التطبيق
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
        Dialog customDialog = new Dialog(activity);
        customDialog.setContentView(dialogView);

        if (customDialog.getWindow() != null) {
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // جعل العرض يتناسب مع الشاشة

//            customDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // تأثير التغبيش
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        Blurry.with(activity).radius(15).sampling(2).onto(decorView);
        customDialog.setOnDismissListener(d -> Blurry.delete(decorView));

        // ضبط العناصر لتظهر كحالة تحميل
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        LinearLayout btnWhatsapp = dialogView.findViewById(R.id.btnWhatsapp);
        ImageView iconWhatsapp = dialogView.findViewById(R.id.iconWhatsapp);
        ImageView paymenticon = dialogView.findViewById(R.id.paymenticon);
        TextView textWhatsapp = dialogView.findViewById(R.id.textWhatsapp);
        MaterialButton btnCall = dialogView.findViewById(R.id.btnCall);
        FrameLayout dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);

        btnCall.setVisibility(View.GONE);
        iconWhatsapp.setVisibility(View.GONE);
        paymenticon.setImageResource(R.drawable.info_new);

        tvTitle.setText("جاري التحميل...");
        tvMessage.setText("يرجى الانتظار قليلاً لجلب تفاصيل العملية.");
        textWhatsapp.setText("إغلاق");

        btnWhatsapp.setOnClickListener(v -> customDialog.dismiss());
        dialogCancelButton.setOnClickListener(v -> customDialog.dismiss());

        customDialog.show();
        return customDialog;
    }

    private void updateDialogWithData(Dialog dialog, JSONObject detail) throws JSONException {
        if (dialog == null || !dialog.isShowing() || !isAdded()) return;

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        DBHelper dbHelper = new DBHelper(getContext());

        double amt = detail.optDouble("amt", 0.0);
        String curCode = detail.optString("cur_code", "");
        String doc_dsc = detail.optString("doc_dsc", "تفاصيل الرصيد");
        String doc_date = detail.optString("doc_date", "");

        tvTitle.setText(doc_dsc);

        StringBuilder message = new StringBuilder();
        message.append("المبلغ: ").append(amt).append(" ").append(curCode).append("\n");
//        message.append("التاريخ: ").append(doc_date).append("\n\n");

        tvMessage.setText(message.toString());
        tvMessage.setGravity(Gravity.CENTER);

        ImageView paymenticon = dialog.findViewById(R.id.paymenticon);
        paymenticon.setImageResource(R.drawable.info);
    }

    private void showContactSupportDialog(int bookingId, int verified) {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) return;

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
        Dialog customDialog = new Dialog(activity);
        customDialog.setContentView(dialogView);

        if (customDialog.getWindow() != null) {
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        Blurry.with(getActivity()).radius(15).sampling(2).onto(decorView);
        customDialog.setOnDismissListener(d -> Blurry.delete(decorView));

        LinearLayout btnWhatsapp = dialogView.findViewById(R.id.btnWhatsapp);
        Button btnCall = dialogView.findViewById(R.id.btnCall);
        View btnClose = dialogView.findViewById(R.id.dialogCancelButton);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        LinearLayout containerNote = dialogView.findViewById(R.id.containerNote);
        containerNote.setVisibility(View.GONE);
        tvTitle.setText("تنبيه هام");
        DBHelper dbHelper = new DBHelper(getContext());
        if (verified == 0) {
            UserUtils.getMessageFromLocal(323, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    tvMessage.setText(message);
                }

                @Override
                public void onError(String error) {
                    tvMessage.setText(UserUtils.getMessageFromLocalNew(323, dbHelper));
                }
            });
        } else if (verified == 1) {
            UserUtils.getMessageFromLocal(319, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    tvMessage.setText(message);
                }

                @Override
                public void onError(String error) {
                    tvMessage.setText(UserUtils.getMessageFromLocalNew(319, dbHelper));
                }
            });
        } else if (verified == 2) {
            UserUtils.getMessageFromLocal(493, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    tvMessage.setText(message);
                }

                @Override
                public void onError(String error) {
                    tvMessage.setText(UserUtils.getMessageFromLocalNew(319, dbHelper));
                }
            });
        }
        btnWhatsapp.setOnClickListener(v -> {
            SharedPreferences prefs = SharedPrefsHelper.get(getContext());
            String message = UserUtils.getMessageFromLocalNew(332, dbHelper) + " " + bookingId;
            String countryCode = prefs.getString("country_code", "967785050270");

            int messageId = "YE".equals(countryCode) ? 349 : 362;
            String phone = UserUtils.getMessageFromLocalNew(messageId, dbHelper);
            try {
                String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode(message, "UTF-8");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                UserUtils.ToastMessages(activity, UserUtils.getMessageFromLocalNew(321, dbHelper));
            }
        });

        btnCall.setOnClickListener(v -> {
            SharedPreferences prefs = SharedPrefsHelper.get(getContext());
            String countryCode = prefs.getString("country_code", "967785050270");

            int messageId = "YE".equals(countryCode) ? 349 : 362;
            String phone = UserUtils.getMessageFromLocalNew(messageId, dbHelper);
//            String phoneNo = prefs.getString("phone_no", UserUtils.getMessageFromLocalNew(349, dbHelper));
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        if (btnClose != null) btnClose.setOnClickListener(v -> customDialog.dismiss());

        customDialog.show();
    }
}