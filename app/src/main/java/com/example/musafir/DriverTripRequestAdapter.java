package com.example.musafir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

import jp.wasabeef.blurry.Blurry;

public class DriverTripRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<JSONObject> requestList;
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private boolean isLoadingAdded = false;

    // Listener للتعامل مع حدث قبول أو رفض الطلب
    public interface OnRequestActionListener {
        void onAcceptRequest(int requestId, int driver_id, String passenger,String car_Code);
    }

    private OnRequestActionListener listener;
    private Context context;

    public DriverTripRequestAdapter(Context context, List<JSONObject> requestList, DriverTripRequestAdapter.OnRequestActionListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return requestList.size() + (isLoadingAdded ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == requestList.size() && isLoadingAdded) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_trip_request, parent, false);
            return new RequestViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    private AlertDialog exitDialog;

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RequestViewHolder) {
            RequestViewHolder vh = (RequestViewHolder) holder;
            JSONObject request = requestList.get(position);

            try {
                int requestId = request.optInt("request_id", -1);
                String fromCity = request.optString("start_city_name", "");
                String toCity = request.optString("end_city_name", "");
                String date = request.optString("preferred_departure_date", "");
                String additional_notes = request.optString("additional_notes", "");
                String dt_display = request.optString("dt_display", "");
                String passenger = request.optString("passenger", "");
                int privates = request.getInt("private");
                String passenger_name = request.optString("passenger_name", "");
                String seats = request.optString("number_of_seats", "");
                String vehicletypename = request.optString("vehicle_type_name", "");
                String car_codes_id = request.optString("car_codes_id", "");
                SharedPreferences prefs = SharedPrefsHelper.get(context);

//                SharedPreferences preferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                vh.title.setText("من " + fromCity + " إلى " + toCity);
                vh.tvSeats.setText(seats);
                vh.tvPersonName.setText(passenger_name);
                vh.tvDate.setText(date);
                vh.tvTime.setText(dt_display);
//                vh.tvNotes.setText(additional_notes);
                vh.vehicle_type_name.setText(vehicletypename);
                // التأكد من وجود الملاحظات
                if (additional_notes.isEmpty() || additional_notes.equals("null")) {
                    vh.notes.setVisibility(View.GONE);
                } else {
                    vh.notes.setVisibility(View.VISIBLE);
                    vh.notes.setOnClickListener(v -> {
                        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notes, null);
                        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);
                        Button btnClose = dialogView.findViewById(R.id.btnDialogClose);
                        LinearLayout dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);

                        // تعيين نص الملاحظات
                        tvMessage.setText(additional_notes);

                        Dialog dialog = new Dialog(context, R.style.KeyboardAwareDialog);
                        dialog.setContentView(dialogView);

                        if (dialog.getWindow() != null) {
                            Window window = dialog.getWindow();
                            window.setGravity(Gravity.CENTER);

                            int marginInDp = 20;
                            float scale = context.getResources().getDisplayMetrics().density;
                            int marginInPx = (int) (marginInDp * scale);

                            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                            int dialogWidth = screenWidth - (2 * marginInPx);

                            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                            window.setBackgroundDrawableResource(R.drawable.bg_dialog);
                            window.getAttributes().windowAnimations = R.style.DialogSlideUpAnimation;
                        }

                        ViewGroup decorView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
                        Blurry.with(context).radius(15).sampling(2).onto(decorView);

                        dialog.setOnDismissListener(d -> Blurry.delete(decorView));

                        btnClose.setOnClickListener(view -> dialog.dismiss());
                        if (dialogCancelButton != null) {
                            dialogCancelButton.setOnClickListener(view -> dialog.dismiss());
                        }

                        dialog.show();
                    });

                }

                if (privates == 0) {
                    vh.tvTripStatus.setText("تشاركية");
                } else {
                    vh.tvTripStatus.setText("خاصة");
                }
                vh.btnAccept.setOnClickListener(v -> {
                    if (listener != null && requestId != -1) {
                        listener.onAcceptRequest(requestId, userId, passenger,car_codes_id);
                    }
                });
            } catch (Exception e) {
                UserUtils.sendLog(context, "DriverTripRequestAdapter", e.toString(), e.toString(), "Driver Trip Request Adapter");
                vh.btnAccept.setVisibility(View.GONE);
            }
        }
    }


    public void addLoadingFooter() {
        if (!isLoadingAdded) {
            isLoadingAdded = true;
            notifyItemInserted(requestList.size());
        }
    }

    public void removeLoadingFooter() {
        if (isLoadingAdded) {
            isLoadingAdded = false;
            notifyItemRemoved(requestList.size());
        }
    }

    public void clear() {
        requestList.clear();
        notifyDataSetChanged();
        isLoadingAdded = false;
    }

    // ViewHolder للطلبات
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView title, tvTripStatus, tvPersonName, tvDate, tvSeats, tvTime, vehicle_type_name;

        Button notes;
        Button btnAccept;

        public RequestViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTripStatus = itemView.findViewById(R.id.tvTripStatus);
            tvPersonName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            notes = itemView.findViewById(R.id.notes);
            vehicle_type_name = itemView.findViewById(R.id.vehicle_type_name);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
