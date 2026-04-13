package com.example.musafir;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TravelerRequestsAdapter extends RecyclerView.Adapter<TravelerRequestsAdapter.ViewHolder> {

    private Context context;
    private List<JSONObject> dataList;

    public TravelerRequestsAdapter(Context context, List<JSONObject> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_traveler_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject item = dataList.get(position);

        try {

            // العنوان
            holder.txtTitle.setText(item.getString("type_tr_name"));
            // عدد الأشخاص
            int number = 0; // قيمة افتراضية
            if (!item.isNull("number_passenger")) {
                number = item.optInt("number_passenger", 0); // سيأخذ 0 إذا لم يكن موجودًا
            }

            String text;
            if (number == 0) {
                text = "لا يوجد أشخاص";
            } else if (number == 1) {
                text = "1 شخص";
            } else {
                text = number + " أشخاص";
            }
            // الحالة
            String status = item.getString("tr_status");
            holder.txtStatus.setText(getStatusArabic(status));
            holder.allRequest.setOnClickListener(v -> {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("tr_id", item.getString("tr_id"));
                    bundle.putString("type_tr_name", item.getString("type_tr_name"));
                    bundle.putString("tr_status", getStatusArabic(status));
                    bundle.putString("number_passenger", text);
                    bundle.putString("type_icon", item.getString("type_icon"));
                    bundle.putString("name_passenger1", item.optString("name_passenger1", ""));
                    bundle.putString("name_passenger2", item.optString("name_passenger2", ""));
                    bundle.putString("name_passenger3", item.optString("name_passenger3", ""));
                    bundle.putString("name_passenger4", item.optString("name_passenger4", ""));
                    bundle.putString("name_passenger5", item.optString("name_passenger5", ""));
                    bundle.putString("name_passenger6", item.optString("name_passenger6", ""));
                    bundle.putString("notes", item.getString("notes"));
                    bundle.putInt("number_status", item.optInt("number_status", 0));
                    bundle.putString("country", item.getString("country"));

                    Fragment fragment = new TravelerRequestsDetails();
                    fragment.setArguments(bundle);

                    if (context instanceof HomePage) {
                        ((HomePage) context).openFullScreenFragment(fragment, "تفاصيل الخدمة", R.drawable.solo_traveller, 0);
                    }
                } catch (Exception e) {
                }
            });


            setStatusStyle(holder, status);


            holder.txtPersons.setText(text);
//            int numStatus = item.optInt("number_status", 0);
//            holder.numberStatus.setText(numStatus + "");

            String date = item.optString("created_at", "");
            try {

                String dateOnly = date.split("T")[0];

                holder.txtDate.setText(dateOnly);

            } catch (Exception e) {
                holder.txtDate.setText(""); // إذا فشل العرض
            }


            // الأيقونة
            String iconName = item.getString("type_icon");
            holder.imgIcon.setImageResource(getIconResId(iconName));

        } catch (Exception e) {
        }
    }

    private void setStatusStyle(ViewHolder holder, String status) {
        GradientDrawable background = (GradientDrawable) holder.txtStatus.getBackground();

        switch (status) {

            case "verified":   // مؤكد
                holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));   // أخضر
                background.setColor(Color.parseColor("#C8E6C9"));
                break;

            case "cancelled":  // ملغي
                holder.txtStatus.setTextColor(Color.parseColor("#EF4444"));   // أحمر
                background.setColor(Color.parseColor("#FDECEC"));
                break;

            case "pending":    // قيد المعالجة
                holder.txtStatus.setTextColor(Color.parseColor("#CC9407"));   // برتقالي
                background.setColor(Color.parseColor("#FEF5E6"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        CardView allRequest;
        TextView txtTitle, txtStatus, txtPersons, txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgIcon);
//            numberStatus = itemView.findViewById(R.id.numberStatus);
            allRequest = itemView.findViewById(R.id.allRequest);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtPersons = itemView.findViewById(R.id.txtPersons);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }

    private String getStatusArabic(String status) {
        switch (status) {
            case "verified":
                return "مؤكد";
            case "cancelled":
                return "ملغي";
            case "pending":
                return "قيد المعالجة";
            default:
                return status;
        }
    }


    private String formatDate(String dateTime) {
        try {
            String datePart = dateTime.substring(0, 10);
            String[] arr = datePart.split("-");
            return arr[2] + "-" + arr[1] + "-" + arr[0] + " م";
        } catch (Exception e) {
            return dateTime;
        }
    }

    private int getIconResId(String iconName) {
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }
}
