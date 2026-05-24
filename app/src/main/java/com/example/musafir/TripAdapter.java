package com.example.musafir;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.ProgressBar;

import jp.wasabeef.blurry.Blurry;

//import jp.wasabeef.blurry.Blurry;


public class TripAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static List<JSONObject> tripList = null;

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private boolean isLoadingAdded = false;
    private OnTripCancelListener cancelListener;
    static String BASE_URL = UserUtils.BASE_URL;
    private Context context;

    public void setOnTripCancelListener(Context context, OnTripCancelListener listener) {
        this.context = context;
        this.cancelListener = listener;
    }

    public TripAdapter(List<JSONObject> tripList, OnTripCancelListener cancelListener) {
        this.tripList = tripList;
        this.cancelListener = cancelListener;
    }

    @Override
    public int getItemCount() {
        return tripList.size() + (isLoadingAdded ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == tripList.size() && isLoadingAdded) {
            return VIEW_TYPE_LOADING;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
            return new TripViewHolder(view, cancelListener);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            TripViewHolder tripHolder = (TripViewHolder) holder;
            JSONObject trip = tripList.get(position);
            try {
                int tripId = trip.getInt("request_id");
                String bookingStatus = trip.optString("request_status", "");

                tripHolder.bind(trip, tripId);

                tripHolder.btnCancelTrip.setVisibility(View.VISIBLE); // الزر دائمًا ظاهر

                tripHolder.btnCancelTrip.setOnClickListener(v -> {
                    if ("verified".equals(bookingStatus)
                            || "cancelled".equals(bookingStatus)
                            || "cancelled_by_driver".equals(bookingStatus)
                            || "cancelled_by_passenger".equals(bookingStatus)) {
                        DBHelper dbHelper = new DBHelper(context);
                        Activity activity = (Activity) v.getContext();

                        UserUtils.getMessageFromLocal(20, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(activity, message);

                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                    } else if ("expired".equals(bookingStatus) || "closed".equals(bookingStatus)) {
                        tripHolder.btnCancelTrip.setClickable(false);
                    } else {
                        showCancelReasonDialog(tripId);
                    }
                });

                String dt_no = trip.optString("dt_display", "");
                int number_of_seats = trip.getInt("number_of_seats");
                String preferred_departure_date = trip.getString("preferred_departure_date");
                int privates = trip.getInt("private");
                String creation_date = trip.optString("creation_date", "");

                if (creation_date != null && !creation_date.isEmpty()) {
                    tripHolder.tvOrderTime.setText(UserUtils.getTimeAgo(creation_date));
                } else {
                    tripHolder.tvOrderTime.setText("منذ قليل");
                }
                if (privates == 0) {
                    tripHolder.tripLocation.setText("تشاركية");
                } else {
                    tripHolder.tripLocation.setText("خاصة");
                }

                String status = bookingStatus;

                GradientDrawable background = (GradientDrawable) tripHolder.tvTripStatus.getBackground();

                switch (status) {
                    case "verified":
                        tripHolder.tvTripStatus.setText("مؤكد");
                        tripHolder.tvTripStatus.setTextColor(Color.parseColor("#2E7D32"));
                        background.setColor(Color.parseColor("#C8E6C9"));
                        break;

                    case "cancelled":
                    case "cancelled_by_driver":
                    case "cancelled_by_passenger":
                        tripHolder.tvTripStatus.setText("ملغي");
                        tripHolder.tvTripStatus.setTextColor(Color.parseColor("#ef4444"));
                        background.setColor(Color.parseColor("#fdecec"));
                        break;

                    case "pending":
                        tripHolder.tvTripStatus.setText("قيد المعالجة");
                        tripHolder.tvTripStatus.setTextColor(Color.parseColor("#CC9407"));
                        background.setColor(Color.parseColor("#fef5e6"));
                        break;

                    case "expired":
                        tripHolder.tvTripStatus.setText("منتهي");
                        tripHolder.tvTripStatus.setTextColor(Color.parseColor("#9CA3AF"));
                        background.setColor(Color.parseColor("#F3F4F6"));
                        break;


                    default:
                        tripHolder.tvTripStatus.setText("مغلق");
                        tripHolder.tvTripStatus.setTextColor(Color.parseColor("#1E3A8A"));
                        background.setColor(Color.parseColor("#DBEAFE"));
                        break;
                }

                tripHolder.tvTripTitle.setText("من " + trip.getString("start_city_name") + " إلى " + trip.getString("end_city_name"));
                tripHolder.tvTripDate.setText(preferred_departure_date);
                if (number_of_seats == 1) {
                    tripHolder.tvTripSeats.setText("1 مقعد");
                } else if (number_of_seats >= 11) {
                    tripHolder.tvTripSeats.setText(number_of_seats + " مقعد");
                } else {
                    tripHolder.tvTripSeats.setText(number_of_seats + " مقاعد");
                }
//                tripHolder.tvTripSeats.setText(String.valueOf(number_of_seats));
                tripHolder.tvTripHistory.setText(dt_no);
                View.OnClickListener openTripDetailsListener = v -> {
                    Fragment fragment = new TripDetailsFragment();
                    Bundle args = new Bundle();
                    args.putString("related_object_id", String.valueOf(tripId));
                    fragment.setArguments(args);

                    if (v.getContext() instanceof HomePage) {
                        ((HomePage) v.getContext()).openFullScreenFragment(fragment, "تفاصيل الطلب", R.drawable.booking, 2);
                    }
                };

                tripHolder.conTrip.setOnClickListener(openTripDetailsListener);
                tripHolder.btnDetails.setOnClickListener(openTripDetailsListener);
            } catch (Exception e) {
                UserUtils.sendLog(context, "RoutsAdapter", e.toString(), e.toString(), "Trip Adapter");

            }
        }
    }

    private AlertDialog exitDialog;

    private void showCancelReasonDialog(int tripId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        DBHelper dbHelper = new DBHelper(context);

        // 1. الحصول على الـ Root View لتطبيق التغبيش عليه
        ViewGroup rootLayout = (ViewGroup) ((Activity) context).getWindow().getDecorView().getRootView();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = inflater.inflate(R.layout.dialog_cancel_trip, null);
        builder.setView(dialogView);

        EditText input = dialogView.findViewById(R.id.etReason);
        Button btnAdd = dialogView.findViewById(R.id.btnYes);
        Button btnCancel = dialogView.findViewById(R.id.btnNo);

        AlertDialog dialog = builder.create();

        // جعل الخلفية شفافة للحفاظ على حواف bg_dialog الدائرية
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // إدارة التغبيش للديالوج الأول
        dialog.setOnShowListener(d -> Blurry.with(context).radius(15).sampling(2).onto(rootLayout));
        dialog.setOnDismissListener(d -> Blurry.delete(rootLayout));

        dialog.show();

        // ضبط قياسات العرض (85% من الشاشة)
        if (dialog.getWindow() != null) {
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
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

            AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
            View dialogView2 = inflater.inflate(R.layout.dialog_custom_confirmationt, null);
            builder2.setView(dialogView2);

            Button btnYes = dialogView2.findViewById(R.id.btnYes);
            Button btnNo = dialogView2.findViewById(R.id.btnNo);
            TextView tvMessage = dialogView2.findViewById(R.id.tvMessage);
            tvMessage.setText("هل أنت متأكد أنك تريد إلغاء هذه الرحلة؟");
            btnYes.setTextSize(18);
            btnNo.setTextSize(18);

            AlertDialog exitDialog = builder2.create();

            exitDialog.setOnShowListener(d -> Blurry.with(context).radius(15).sampling(2).onto(rootLayout));
            exitDialog.setOnDismissListener(d -> Blurry.delete(rootLayout));

            if (exitDialog.getWindow() != null) {
                exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            btnYes.setOnClickListener(v2 -> {
                String cancellationDate = getCurrentDateTime();
                UserUtils.getMessageFromLocal(50, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages((Activity) context, message);
                    }

                    @Override
                    public void onError(String error) {
                    }
                });

                sendCancelRequest(context, tripId, reason, cancellationDate, cancelListener);
                exitDialog.dismiss();
            });

            btnNo.setOnClickListener(v2 -> exitDialog.dismiss());
            exitDialog.show();

            // ضبط قياسات العرض للديالوج الثاني أيضاً
            if (exitDialog.getWindow() != null) {
                int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
                exitDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        RelativeLayout btnCloseHeader = dialogView.findViewById(R.id.dialogCancelButton);
        if (btnCloseHeader != null) {
            btnCloseHeader.setOnClickListener(v1 -> dialog.dismiss());
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public interface OnTripCancelListener {
        void onTripCancelled(int tripId);
    }

    public void updateTripStatusToCancelled(int bookingId, String newStatus) {
        for (int i = 0; i < tripList.size(); i++) {
            try {
                JSONObject booking = tripList.get(i);
                if (booking.getInt("request_id") == bookingId) {
                    booking.put("request_status", newStatus);
                    int finalI = i;
                    ((Activity) context).runOnUiThread(() -> notifyItemChanged(finalI));
                    break;
                }
            } catch (JSONException e) {
//                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }


    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripTitle, tvTripStatus, tvTripDate, tvTripSeats, tvTripHistory, tripLocation, tvOrderTime;
        ImageView btnCancelTrip, btnDetails;
        //        LinearLayout notes, notesdriver;
        int tripId;
        OnTripCancelListener cancelListener;
        CardView conTrip;

        public TripViewHolder(View itemView, OnTripCancelListener listener) {
            super(itemView);
            this.cancelListener = listener;
            conTrip = itemView.findViewById(R.id.conTrip);
            tvTripHistory = itemView.findViewById(R.id.tvTripHistory);
            tvTripSeats = itemView.findViewById(R.id.tvTripSeats);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvTripDate = itemView.findViewById(R.id.tvTripDate);
            tripLocation = itemView.findViewById(R.id.tripLocation);
            tvTripTitle = itemView.findViewById(R.id.tvTripTitle);
            tvTripStatus = itemView.findViewById(R.id.tvTripStatus);
            btnCancelTrip = itemView.findViewById(R.id.btnCancelTrip);
            btnDetails = itemView.findViewById(R.id.btnDetails);


        }

        public void bind(JSONObject trip, int tripId) throws JSONException {
            this.tripId = tripId;


        }

        private String getCurrentDateTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date());
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    public void addTrips(List<JSONObject> newTrips) {
        int start = tripList.size();
        tripList.clear();
        tripList.addAll(newTrips);
        notifyItemRangeInserted(start, newTrips.size());
    }

    public void setTrips(List<JSONObject> newTrips) {
        tripList.clear();
        tripList.addAll(newTrips);
        notifyDataSetChanged();
    }

    public void addLoadingFooter() {
        if (!isLoadingAdded) {
            isLoadingAdded = true;
            notifyItemInserted(tripList.size());
        }
    }

    public void clearTrips() {
        tripList.clear();
        notifyDataSetChanged();
    }

    public void removeLoadingFooter() {

        if (isLoadingAdded) {
            isLoadingAdded = false;

            notifyItemRemoved(tripList.size());
        }
    }

    private static void sendCancelRequest(Context context, int tripId, String reason, String cancellationDate, OnTripCancelListener listener) {
        DBHelper dbHelper = new DBHelper(context);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {

                String deviceId = UserUtils.getDeviceID(context);
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "trip-requests/" + tripId + "/cancel/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                conn = (HttpURLConnection) url.openConnection();

                // إعداد الاتصال
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                conn.setDoOutput(true);
                SharedPreferences prefs = SharedPrefsHelper.get(context);
//                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                // بناء جسم JSON للإرسال
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cancellation_reason", reason);
                jsonObject.put("cancellation_date", cancellationDate);
                String jsonString = jsonObject.toString();
                String s = "{ cancellation_reason: " + reason +
                        " , cancellation_date: " + cancellationDate +
                        " }";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // قراءة الرد
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

                // تحديث الواجهة حسب النتيجة
                ((Activity) context).runOnUiThread(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        if (listener != null) {
                            listener.onTripCancelled(tripId);
                        }

                        UserUtils.getMessageFromLocal(51, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages((Activity) context, message);
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });

                    } else {
                        UserUtils.getMessageFromLocal(52, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages((Activity) context, message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        UserUtils.sendLog(context, "sendCancelRequest", responseStr, s, "TripAdapter");
                    }
                });

            } catch (Exception e) {
                ((Activity) context).runOnUiThread(() -> {
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages((Activity) context, message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                    UserUtils.sendLog(context, "sendCancelRequest", e.toString(), e.toString(), "TripAdapter");
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

}
