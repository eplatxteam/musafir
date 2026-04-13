package com.example.musafir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;
import org.json.JSONException;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.blurry.Blurry;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<JSONObject> bookingList;
    private OnCancelBookingListener cancelListener;
    private OnApproveBookingListener approveListener; // أضف هذا السطر

    public interface OnCancelBookingListener {
        void onCancelBooking(int bookingId);
    }

    // واجهة الموافقة
    public interface OnApproveBookingListener {
        void onApproveBooking(int bookingId);
    }

    public void updateItemStatus(int bookingId, String newStatus) {
        for (int i = 0; i < bookingList.size(); i++) {
            try {
                JSONObject booking = bookingList.get(i);
                if (booking.getInt("booking_id") == bookingId) {
                    booking.put("booking_status", newStatus);
                    notifyItemChanged(i);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Constructor
    public BookingAdapter(List<JSONObject> bookingList, OnCancelBookingListener cancelListener, OnApproveBookingListener approveListener) {
        this.bookingList = bookingList != null ? bookingList : new ArrayList<>();
        this.cancelListener = cancelListener;
        this.approveListener = approveListener;
    }

    public void addBooking(List<JSONObject> newBooking) {
        int start = bookingList.size();
        bookingList.addAll(newBooking);
        notifyItemRangeInserted(start, newBooking.size());
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_booking, parent, false);
        return new BookingViewHolder(view);
    }

    public void clearBookings() {
        bookingList.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        JSONObject booking = bookingList.get(position);
        JSONObject bookingInfo = booking.optJSONObject("trip_info");
        int bookingId = booking.optInt("booking_id", -1);
        String bookingStatus = booking.optString("booking_status", "");
        int pay_type = booking.optInt("pay_type", -1);
        Context ctx = holder.itemView.getContext();
        DBHelper dbHelper = new DBHelper(ctx);
        SharedPreferences prefs = SharedPrefsHelper.get(ctx);

//        SharedPreferences sharedPreferences = ctx.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        int passengerId = prefs.getInt("user_id", -1);


        holder.btnCancelBooking.setOnClickListener(v -> {
            Activity activity = (Activity) v.getContext();

            if (pay_type == 1 || "verified".equals(bookingStatus)) {
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
                Dialog customDialog = new Dialog(activity);
                customDialog.setContentView(dialogView);

                if (customDialog.getWindow() != null) {
                    customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                Blurry.with(ctx)
                        .radius(15)
                        .sampling(2)
                        .onto(decorView);

                customDialog.setOnDismissListener(d -> Blurry.delete(decorView));

                LinearLayout btnWhatsapp = dialogView.findViewById(R.id.btnWhatsapp);
                Button btnCall = dialogView.findViewById(R.id.btnCall);
                RelativeLayout btnCloseHeader = dialogView.findViewById(R.id.dialogCancelButton);
                TextView tvMessage = dialogView.findViewById(R.id.tvMessage);

//                tvMessage.setText("لقد قمت بتأكيد أو دفع رسوم هذه الرحلة، سوف يتم رفع طلبك للإدارة، يرجى التواصل مع خدمة العملاء لإتمام الإلغاء.");
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

                btnWhatsapp.setOnClickListener(v1 -> {
                    String whatsappNo = prefs.getString("whatsapp_no", "967785050270");
                    String msg = "أرغب في إلغاء الحجز رقم: " + bookingId;
                    try {
                        String url = "https://api.whatsapp.com/send?phone=" + whatsappNo + "&text=" + URLEncoder.encode(msg, "UTF-8");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        activity.startActivity(intent);
                        customDialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(ctx, "تطبيق واتساب غير مثبت", Toast.LENGTH_SHORT).show();
                    }
                });

                btnCall.setOnClickListener(v1 -> {
                    String phoneNo = prefs.getString("phone_no", "785050270");
                    try {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phoneNo));
                        activity.startActivity(intent);
                        customDialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(ctx, "فشل فتح لوحة الاتصال", Toast.LENGTH_SHORT).show();
                    }
                });

                if (btnCloseHeader != null) {
                    btnCloseHeader.setOnClickListener(v1 -> customDialog.dismiss());
                }

                customDialog.show();
                return;
            }
            if ("cancelled".equals(bookingStatus) || "cancelled_by_driver".equals(bookingStatus) || "cancelled_by_passenger".equals(bookingStatus)) {

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
                holder.btnCancelBooking.setClickable(false);
            } else {
                if (bookingId != -1 && cancelListener != null) {
                    dbHelper.deleteBooking(bookingId, passengerId);
                    cancelListener.onCancelBooking(bookingId);
                }
            }
        });

        String startCity = bookingInfo != null ? bookingInfo.optString("start_city", "") : "";
        String endCity = bookingInfo != null ? bookingInfo.optString("end_city", "") : "";
        String departureDate = bookingInfo != null ? bookingInfo.optString("departure_date", "") : "";
        String departureTime = bookingInfo != null ? booking.optString("Attendance_time", "") : "";
        String driverName = bookingInfo != null ? bookingInfo.optString("driver_name", "") : "";
        String company_name = booking != null ? booking.optString("company_name", "") : "";
        String car_code = booking != null ? booking.optString("car_code", "") : "";
        if (company_name == null || company_name.isEmpty() || "null".equals(company_name)) {
            holder.containerCompany.setVisibility(View.GONE);
        } else {
            holder.containerCompany.setVisibility(View.VISIBLE);
            holder.tvDriverCompany.setText(company_name);
        }

        String passengerNotes = booking.optString("passenger_notes", "لا توجد");

        String formattedTime;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            Date time = inputFormat.parse(departureTime);
            formattedTime = outputFormat.format(time).replace("AM", "ص").replace("PM", "م");
        } catch (Exception e) {
            formattedTime = departureTime;
        }


        String numberOfSeatsStr = booking.optString("number_of_seats", "0");
        int numberOfSeats = Integer.parseInt(numberOfSeatsStr);

        String pricePerSeat = booking.optString("total_price", "0");

        pricePerSeat = pricePerSeat.replace(",", "");

        double price = Double.parseDouble(pricePerSeat);

        double totalPrice = price;

        holder.tvPrice.setText(String.format(Locale.ENGLISH, "%,.0f %s", totalPrice, car_code));

        if (numberOfSeats == 1) {
            holder.tvSeats.setText("1 مقعد");
        } else if (numberOfSeats >= 11) {
            holder.tvSeats.setText(numberOfSeats + " مقعد");
        } else {
            holder.tvSeats.setText(numberOfSeats + " مقاعد");
        }
//        holder.tvSeats.setText(numberOfSeats);

        holder.tvDriverName.setText(driverName.isEmpty() ? " " : driverName);
        holder.tvRoute.setText("من " + (startCity.isEmpty() ? "?" : startCity) + "  إلى " + (endCity.isEmpty() ? "?" : endCity));
        holder.tvDate.setText(departureDate.isEmpty() ? "-" : departureDate);
        holder.tvTime.setText(formattedTime.isEmpty() ? "-" : formattedTime);
//        holder.conBooking.setOnClickListener(v -> {
//            Fragment fragment = null;
//            Bundle args = new Bundle();
//            args.putString("related_object_id", String.valueOf(bookingId));
//            fragment = new BookingDetailsFragment();
//            fragment.setArguments(args);
//            ((androidx.fragment.app.FragmentActivity) v.getContext()).getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.full_screen_container, fragment)
//                    .addToBackStack(null)
//                    .commit();
//            ((HomePage) v.getContext()).updateToolbar("تفاصيل الحجز", false, R.drawable.booking, 2);
//        });
//        holder.btnDetails.setOnClickListener(v -> {
//            Fragment fragment = null;
//            Bundle args = new Bundle();
//            args.putString("related_object_id", String.valueOf(bookingId));
//            fragment = new BookingDetailsFragment();
//            fragment.setArguments(args);
//            ((androidx.fragment.app.FragmentActivity) v.getContext()).getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.full_screen_container, fragment)
//                    .addToBackStack(null)
//                    .commit();
//            ((HomePage) v.getContext()).updateToolbar("تفاصيل الحجز", false, R.drawable.booking, 2);
//        });
        View.OnClickListener openDetailsListener = v -> {
            Fragment fragment = new BookingDetailsFragment();
            Bundle args = new Bundle();
            args.putString("related_object_id", String.valueOf(bookingId));
            fragment.setArguments(args);

            if (v.getContext() instanceof HomePage) {
                ((HomePage) v.getContext()).openFullScreenFragment(fragment, "تفاصيل الحجز", R.drawable.checklist, 2);
            }
        };

        holder.conBooking.setOnClickListener(openDetailsListener);
        holder.btnDetails.setOnClickListener(openDetailsListener);
        String status = bookingStatus;

        GradientDrawable background = (GradientDrawable) holder.tvStatus.getBackground();

        switch (status) {
            case "verified":
                holder.tvStatus.setText("مؤكد");
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                background.setColor(Color.parseColor("#C8E6C9"));
                break;

            case "cancelled":
            case "cancelled_by_driver":
            case "cancelled_by_passenger":
                holder.tvStatus.setText("ملغي");
                holder.tvStatus.setTextColor(Color.parseColor("#ef4444"));
                background.setColor(Color.parseColor("#fdecec"));
                dbHelper.deleteBooking(bookingId, passengerId);
                break;

            case "pending":
                holder.tvStatus.setText("قيد المعالجة");
                holder.tvStatus.setTextColor(Color.parseColor("#CC9407"));
                background.setColor(Color.parseColor("#fef5e6")); // برتقالي
                break;

            case "expired":
                holder.tvStatus.setText("منتهي");
                holder.tvStatus.setTextColor(Color.parseColor("#9CA3AF")); // رمادي داكن
                background.setColor(Color.parseColor("#F3F4F6")); // رمادي فاتح
                break;

            default:
                holder.tvStatus.setText("مغلقة");
                holder.tvStatus.setTextColor(Color.parseColor("#1E3A8A")); // نص أزرق داكن
                background.setColor(Color.parseColor("#DBEAFE")); // خلفية أزرق فاتح
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvDriverName, tvRoute, tvTime, tvDate, tvSeats, tvPrice, tvStatus, tvDriverCompany;
        ImageView btnCancelBooking, btnDetails;
        LinearLayout containerCompany;

        CardView conBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
//            notes = itemView.findViewById(R.id.notes);
            containerCompany = itemView.findViewById(R.id.containerCompany);
            tvDriverName = itemView.findViewById(R.id.tvName);
            tvDriverCompany = itemView.findViewById(R.id.tvDriverCompany);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            conBooking = itemView.findViewById(R.id.conBooking);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}


