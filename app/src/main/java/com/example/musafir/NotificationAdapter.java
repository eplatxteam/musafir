package com.example.musafir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.musafir.R;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<JSONObject> notificationList;
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private boolean isLoadingAdded = false;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(JSONObject notification);
    }

    public NotificationAdapter(List<JSONObject> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return notificationList.size() + (isLoadingAdded ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == notificationList.size() && isLoadingAdded) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NotificationViewHolder) {
            NotificationViewHolder vh = (NotificationViewHolder) holder;
            JSONObject notification = notificationList.get(position);
            Context context = vh.itemView.getContext();

            try {
                String title = notification.optString("title", "بدون عنوان");
                String content = notification.optString("content", "");
                boolean isRead = notification.optBoolean("is_read", false);
                String creationDate = notification.optString("creation_date", "");
                String notification_type = notification.optString("notification_type", "");
                String related_object_id = notification.optString("related_object_id", "");
                String rightIconName = notification.optString("not_icon", "");

                vh.title.setText(title);
                vh.message.setText(content);
                vh.date.setText(getTimeAgo(creationDate));

//                vh.readStatusIcon.setImageResource(isRead ? R.drawable.ic_circle_green : R.drawable.ic_circle_red);

            } catch (Exception e) {
                UserUtils.sendLog(context, "NotificationAdapter", e.toString(), e.toString(), "Notification Adapter");
                vh.title.setText("خطأ في البيانات");
            }
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }

    public void addNotifications(List<JSONObject> newNotifications) {
        int start = notificationList.size();
        notificationList.addAll(newNotifications);
        notifyItemRangeInserted(start, newNotifications.size());
    }

    public void addLoadingFooter() {
        if (!isLoadingAdded) {
            isLoadingAdded = true;
            notifyItemInserted(notificationList.size());
        }
    }

    public void removeLoadingFooter() {
        if (isLoadingAdded) {
            isLoadingAdded = false;
            notifyItemRemoved(notificationList.size());
        }
    }

    public void clear() {
        notificationList.clear();
        notifyDataSetChanged();
        isLoadingAdded = false;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, date;
//        ImageView  rightIcon;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
            date = itemView.findViewById(R.id.notificationDate);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private String getTimeAgo(String inputDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(inputDateStr);
            if (date == null) return "";

            long diff = new Date().getTime() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) return "الآن";
            else if (minutes < 60) return minutes + " دقيقة";
            else if (hours < 24) return hours + " ساعة";
            else return days + " يوم";

        } catch (ParseException e) {
            return inputDateStr;
        }
    }
}
