package com.example.musafir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

//import jp.wasabeef.blurry.Blurry;


public class TripSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<JSONObject> tripList;
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    String BASE_URL = UserUtils.BASE_URL;

    private boolean isLoadingAdded = false;

    String ImageUrl = UserUtils.ImageUrl;
    FragmentManager fragmentManager;
    private Context context;
    private String userToken = "";
    private int tripId = 0;

    @Override
    public int getItemCount() {
        return tripList.size() + (isLoadingAdded ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingAdded && position == tripList.size()) {
            return VIEW_TYPE_LOADING;
        }
        return VIEW_TYPE_ITEM;
    }

    private OnTripActionListener listener;

    public interface OnTripActionListener {
        void onBookTrip(String Location, String dateTrip, int tripId, int tripId2, int availableSeats, int pricePerSeat, int pickupOrder, int dropoffOrder,
                        String driver_id, String car_code, String car_codes, String car_codes_id, int discountPrice, int passport_required, int visa_required,
                        int company_no , String dateTimeString);

        void onTripDetails(int tripId);

        void onDetails(int tripId);
    }

    public TripSearchAdapter(Context context, List<JSONObject> tripList, OnTripActionListener listener, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.tripList = tripList;
        this.listener = listener;
        SharedPreferences prefs = SharedPrefsHelper.get(context);
//        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userToken = prefs.getString("auth_token", "");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_home, parent, false);
            return new TripSearchAdapter.TripViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new TripSearchAdapter.LoadingViewHolder(view);
        }
    }

    public ImageView btnCallDriver;

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            TripViewHolder tripHolder = (TripViewHolder) holder;
            JSONObject trip = tripList.get(position);
            Context context = tripHolder.itemView.getContext();

            TextView discountedPrice = tripHolder.itemView.findViewById(R.id.discountedPrice);
            TextView originalPrice = tripHolder.itemView.findViewById(R.id.originalPrice);

            try {
                DBHelper dbHelper = new DBHelper(context);

                String driverName = trip.optString("driver_name", "");
                String price_per_seat_str = trip.optString("price_per_seat", "0").replace(",", "");
                int price = (int) Double.parseDouble(price_per_seat_str);

                String routeName = trip.optString("route_name", "");
                String departureDate = trip.optString("departure_date", "");
                String departureTime = trip.optString("departure_time", "");
                String car_code = trip.optString("car_code", "");
                String car_codes = trip.optString("car_codes", "");
                String car_codes_id = trip.optString("car_code_id", "");

                int discount_price = 0;
                if (!trip.isNull("discount_price")) {
                    String discountPriceStr = trip.optString("discount_price", "0").replace(",", "");
                    discount_price = (int) Double.parseDouble(discountPriceStr);
                }

                int availableSeats = trip.optInt("remaining_seats", 0);
                int tripId2 = trip.optInt("trip_id2", 0);

                int pickupOrder = trip.optInt("start_point_order", 0);
                int dropoffOrder = trip.optInt("end_point_order", 0);
                int passport_required = trip.optInt("passport_required", 0);
                int visa_required = trip.optInt("visa_required", 0);
                String driver_id = trip.optString("driver_id", "");
                String trip_status = trip.optString("trip_status", "");
//                JSONObject vehicle = trip.getJSONObject("vehicle_info");
                int trip_id = trip.optInt("trip_id", 0);
                int vtrip_vip = trip.optInt("trip_vip", 0);
                int company_no = trip.optInt("company_no", 0);

                String driver_rating = trip.optString("driver_rating", "");
                String vehicleMake = trip.optString("make");
                String vehicleType = trip.optString("vehicle_type");
                String vehicle_image = trip.optString("vehicle_image", "");
                String vehicle_name = trip.optString("vehicle_name", "");


                if (vehicle_image != null && !vehicle_image.isEmpty() && !vehicle_image.equals("null")) {
                    if (!vehicle_image.startsWith("http")) {
                        vehicle_image = ImageUrl + "/media/" + vehicle_image;
                    }

                    Glide.with(tripHolder.itemView.getContext())
                            .load(vehicle_image)
                            .thumbnail(0.1f)
                            .placeholder(R.drawable.empty2)
                            .into(tripHolder.vehicleImage);

                    tripHolder.vehicleImage.setVisibility(View.VISIBLE);
                } else {
                    Glide.with(tripHolder.itemView.getContext())
                            .load(R.drawable.empty2)
                            .into(tripHolder.vehicleImage);
                }

                tripHolder.vehicleImage.setVisibility(View.VISIBLE);
//                tripHolder.IdTrip.setText("# "+trip_id);

                originalPrice.setText(String.format(Locale.ENGLISH, "%,d %s", price, car_code));

                if (discount_price > 0) {
                    tripHolder.disPrice.setVisibility(View.VISIBLE);
                    originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    discountedPrice.setText(String.format(Locale.ENGLISH, "%,d %s", discount_price, car_code));
                    if (tripHolder.icon_price != null) {
                        tripHolder.icon_price.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    }
                } else {
                    tripHolder.disPrice.setVisibility(View.GONE);
                    originalPrice.setTextColor(Color.BLACK);
                    if (tripHolder.icon_price != null) tripHolder.icon_price.clearColorFilter();

                }

                if (departureTime.contains("T")) {
                    departureTime = departureTime.split("T")[1];
                } else if (departureTime.contains(" ")) {
                    String[] parts = departureTime.split(" ");
                    departureTime = parts[parts.length - 1];
                }

                String dateTimeString = departureDate + " " + departureTime;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

                Date tripDateTime;
                try {
                    tripDateTime = sdf.parse(dateTimeString);
                } catch (Exception e) {
                    try {
                        tripDateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(departureDate);
                    } catch (Exception ex) {
                        tripDateTime = new Date(); // الوقت الحالي كخيار أخير
                    }
                }

                Date now = new Date();
                long diffInMillis = tripDateTime.getTime() - now.getTime();


//                String dateTimeString = departureDate + " " + departureTime;
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//                Date tripDateTime = sdf.parse(dateTimeString);
//
//                Date now = new Date();
//                long diffInMillis = tripDateTime.getTime() - now.getTime();
                long diffDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                float ratingValue = 0f;
                if (driver_rating != null && !driver_rating.isEmpty() && !driver_rating.equals("null")) {
                    try {
                        ratingValue = Float.parseFloat(driver_rating);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException(e);
                    }
                }

//                GradientDrawable background2 = (GradientDrawable) tripHolder.tripDuration.getBackground();
                GradientDrawable background2 = (GradientDrawable) tripHolder.tripDuration.getBackground().mutate();

                tripHolder.tripDuration.setTextColor(Color.WHITE);
                background2.setColor(Color.parseColor("#6EA0B3"));
                tripHolder.tripRating.setRating(ratingValue);
                tripHolder.tripRating.setIsIndicator(true);
                if (diffDays == 1) {
                    tripHolder.tripDuration.setText("يوم واحد");
                } else if (diffDays == 0) {
                    long diffHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                    long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60;

                    if (diffHours > 0) {
                        String hourText = (diffHours == 1) ? "ساعة واحدة" : diffHours + " ساعات";
                        tripHolder.tripDuration.setText(hourText);
                    } else if (diffMinutes > 0) {
                        String minuteText = (diffMinutes == 1) ? "دقيقة واحدة" : diffMinutes + " دقائق";
                        tripHolder.tripDuration.setText(minuteText);
                    } else {
                        tripHolder.tripDuration.setText("الرحلة بدأت");
                        tripHolder.tripDuration.setTextColor(Color.parseColor("#af0516")); // أخضر غامق
                        background2.setColor(Color.parseColor("#FFCDD2"));
                    }
                } else if (diffDays == 2) {
                    tripHolder.tripDuration.setText("يومان");
                } else if (diffDays >= 3 && diffDays <= 10) {
                    tripHolder.tripDuration.setText(diffDays + " أيام");
                } else {
                    tripHolder.tripDuration.setText(diffDays + " يوم");
                }

                if (availableSeats == 1) {
                    tripHolder.TripSeats.setText("1 مقعد متاح");
                } else if (availableSeats >= 11) {
                    tripHolder.TripSeats.setText(availableSeats + " مقعد متاح");
                } else {
                    tripHolder.TripSeats.setText(availableSeats + " مقاعد متاحة");
                }
                tripHolder.tripDate.setText(departureDate);
                if (vtrip_vip == 1) {
                    String fullText = vehicleType + " " + vehicleMake + " (VIP)";
                    SpannableString spannable = new SpannableString(fullText);

                    int start = fullText.indexOf("(VIP)");
                    int end = start + "(VIP)".length();

                    if (start != -1) {
                        int color = ContextCompat.getColor(context, R.color.primary);
                        spannable.setSpan(
                                new ForegroundColorSpan(color),
                                start,
                                end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );

                        spannable.setSpan(
                                new StyleSpan(android.graphics.Typeface.BOLD),
                                start,
                                end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }

//                    tripHolder.car_name.setText(vehicle_name);
                    if (vehicle_name != null && !vehicle_name.isEmpty() && !vehicle_name.equals("null")) {
                        tripHolder.car_name.setText(vehicle_name);
                    } else {
                        tripHolder.car_name.setText(spannable);
                    }
                } else {
                    if (vehicle_name != null && !vehicle_name.isEmpty() && !vehicle_name.equals("null")) {
                        tripHolder.car_name.setText(vehicle_name);
                    } else {
                        tripHolder.car_name.setText(vehicleType + ' ' + vehicleMake);
                    }
                }
                tripHolder.driverName.setText(driverName);
//                tripHolder.tripTime.setText(Attendance_time);
                tripHolder.tripLocation.setText(routeName);

                int finalPrice = price;
                int finalDiscount_price = discount_price;
                SharedPreferences prefs = SharedPrefsHelper.get(context);

//                SharedPreferences preferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int passengerId = prefs.getInt("user_id", -1);
                String userType = prefs.getString("user_type", "");
                GradientDrawable background = (GradientDrawable) tripHolder.tvStatus.getBackground();
                String status = trip_status;
                // القيم بالعربي
                String[] statusArabic = {"مجدولة", "في الطريق", "ملغية", "مغلقة"};
                String[] statusEnglish = {"scheduled", "in Way", "cancelled", "closed"};
                switch (status) {
                    case "in Way":
                        tripHolder.tvStatus.setText("في الطريق");
                        tripHolder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // أخضر غامق
                        background.setColor(Color.parseColor("#C8E6C9")); // أخضر فاتح
                        break;

                    case "cancelled":
                        tripHolder.tvStatus.setText("ملغية");
                        tripHolder.tvStatus.setTextColor(Color.parseColor("#ef4444"));
                        background.setColor(Color.parseColor("#fdecec")); // أحمر
                        break;

                    case "scheduled":
                        tripHolder.tvStatus.setText("مجدولة");
                        tripHolder.tvStatus.setTextColor(Color.parseColor("#CC9407"));
                        background.setColor(Color.parseColor("#fef5e6")); // برتقالي
                        break;

                    default:
                        tripHolder.tvStatus.setText("مغلقة");
                        tripHolder.tvStatus.setTextColor(Color.parseColor("#1E3A8A")); // نص أزرق داكن
                        background.setColor(Color.parseColor("#DBEAFE")); // خلفية أزرق فاتح

                        break;
                }
                int bookingId = dbHelper.getBookingId(trip_id, passengerId);

                if ("driver".equals(userType)) {
                    tripHolder.btnBookTrip.setText("تعديل الرحلة");
                    tripHolder.tvStatus.setVisibility(View.VISIBLE);

                } else {
                    tripHolder.tvStatus.setVisibility(View.GONE);
                    if (bookingId != -1) {
                        tripHolder.bookingText.setVisibility(View.VISIBLE);
                        tripHolder.btnBookTrip.setText("تم الحجز");
                    } else {
                        tripHolder.bookingText.setVisibility(View.GONE);
                        tripHolder.btnBookTrip.setText("احجز الآن");
                    }
                }

                tripHolder.btnBookTrip.setOnClickListener(new UserUtils.SingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if ("driver".equals(userType)) {
                            showStatusDialog(context, trip_id, availableSeats);
                        } else {
                            if (availableSeats == 0 && bookingId == -1) {
                                UserUtils.showErrorDialog((Activity) context, UserUtils.getMessageFromLocalNew(481, dbHelper), null, null,
                                        "الرحلة ممتلئة", 2,null);
                            } else {
                                if (listener != null) {
                                    if (diffInMillis <= 0) {

                                        UserUtils.getMessageFromLocal(47, dbHelper, new UserUtils.MessageCallback() {
                                            @Override
                                            public void onSuccess(String message) {
                                                UserUtils.ToastMessages((Activity) context, message);
                                            }

                                            @Override
                                            public void onError(String error) {
                                            }

                                        });
                                    } else {
                                        listener.onBookTrip(routeName, departureDate, trip_id, tripId2, availableSeats, finalPrice,
                                                pickupOrder, dropoffOrder, driver_id, car_code, car_codes, car_codes_id, finalDiscount_price,
                                                passport_required, visa_required, company_no, dateTimeString);
                                    }
                                }
                            }
                        }
                    }
                });


                tripHolder.tripLayout.setOnClickListener(v -> {
                    if ("driver".equals(userType)) {

                        if (listener != null) {
                            listener.onDetails(trip_id);
                        }
                    } else {
                        if (listener != null) {
                            listener.onTripDetails(trip_id);
                        }
                    }
                });

                tripHolder.tripDetails.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTripDetails(trip_id);
                    }
                });
            } catch (Exception e) {
                UserUtils.sendLog(context, "TripSearchAdapter", e.toString(), e.toString(), "Trip Search Adapter");
            }
        }
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
        Typeface typeface = ResourcesCompat.getFont(context, R.font.rptregular);

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
        String deviceId = UserUtils.getDeviceID(context);
        String deviceInfo = UserUtils.getDeviceInfo();
        String url = BASE_URL + "update_status/?device_id=" + deviceId + "&device_info=" + deviceInfo;

        JSONObject params = new JSONObject();
        try {
            params.put("token", userToken);
            params.put("trip_id", tripId);
            params.put("trip_status", statusEnglish);
            params.put("available_seats", available_seats);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DBHelper dbHelper = new DBHelper(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, params,
                response -> {

                    UserUtils.getMessageFromLocal(162, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages((Activity) context, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                },
                error -> {
                    UserUtils.getMessageFromLocal(163, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages((Activity) context, message);
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
                headers.put("Authorization", "Bearer " + userToken);
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }


    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView driverName,
                tripLocation,
                tripDate,
                TripSeats,
                tripDuration,
                discountedPrice,
                car_name,

        tvStatus,
                bookingText;

        LinearLayout details_card, disPrice, tripLayout;
        RatingBar tripRating;
        Button btnBookTrip, tripDetails;
        ImageView vehicleImage, icon_price;

        //        LinearLayout tripLocation;
        public TripViewHolder(View itemView) {
            super(itemView);
            bookingText = itemView.findViewById(R.id.bookingText);
            tripDetails = itemView.findViewById(R.id.btnDetails);
            btnBookTrip = itemView.findViewById(R.id.btnBookHome);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tripRating = itemView.findViewById(R.id.tripRating);
            driverName = itemView.findViewById(R.id.driver_names);
            vehicleImage = itemView.findViewById(R.id.tripImage);
            tripLocation = itemView.findViewById(R.id.tripLocation);
            details_card = itemView.findViewById(R.id.details_card);
            tripLayout = itemView.findViewById(R.id.tripLayout);
            tripDate = itemView.findViewById(R.id.tripDate);
            TripSeats = itemView.findViewById(R.id.TripSeats);
            icon_price = itemView.findViewById(R.id.icon_price);
            tripDuration = itemView.findViewById(R.id.tripDuration);
            discountedPrice = itemView.findViewById(R.id.discountedPrice);
            car_name = itemView.findViewById(R.id.car_name);
            disPrice = itemView.findViewById(R.id.disPrice);

            Context context = itemView.getContext();


        }
    }


    public void addLoadingFooter() {
        if (!isLoadingAdded) {
            isLoadingAdded = true;
            // نكتفي بتنبيه الريسايكلر بوجود عنصر إضافي في النهاية (الذي يمثل اللودر)
            notifyItemInserted(tripList.size());
        }
    }

    public void removeLoadingFooter() {
        if (isLoadingAdded) {
            isLoadingAdded = false;
            notifyItemRemoved(tripList.size());
        }
    }

    public void clear() {
        isLoadingAdded = false;
        tripList.clear();
        notifyDataSetChanged();
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    public void addTrips(List<JSONObject> newTrips) {
        int startPosition = tripList.size();
        tripList.addAll(newTrips);
        notifyItemRangeInserted(startPosition, newTrips.size());
    }

}
