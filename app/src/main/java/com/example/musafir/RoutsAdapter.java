package com.example.musafir;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RoutsAdapter extends RecyclerView.Adapter<RoutsAdapter.RouteViewHolder> {
    private List<JSONObject> routes;
    private Context context;

    public RoutsAdapter(Context context, List<JSONObject> routes) {
        this.context = context;
        this.routes = routes;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {
        JSONObject route = routes.get(position);
        try {
            holder.txtRouteName.setText(route.optString("route_name", ""));

            // إلغاء أي Listener قبل تغيير الحالة
            holder.switchNotification.setOnCheckedChangeListener(null);
            holder.switchNotification.setChecked(route.optBoolean("notify_flg", false));
            SharedPreferences prefs = SharedPrefsHelper.get(context);

//            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
            int user_id = prefs.getInt("user_id", -1);
            // إعادة Listener
            holder.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    route.put("notify_flg", isChecked);
                    int id_driver_route1 = route.optInt("id_driver_route", -1);

                    new Thread(() -> {
                        try {
                            String urlStr = UserUtils.BASE_URL + "DriverRoutes/" + id_driver_route1  + "/?driver_id="+user_id;
                            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                            conn.setRequestMethod("PATCH");
                            conn.setRequestProperty("Content-Type", "application/json");
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                            String token = prefs.getString("auth_token", null); 

                            if (token != null) {
                                conn.setRequestProperty("Authorization", "Bearer " + token);
                            }
                            JSONObject patchData = new JSONObject();
                            patchData.put("notify_flg", isChecked);

                            conn.getOutputStream().write(patchData.toString().getBytes("UTF-8"));
                            int responseCode = conn.getResponseCode();

                            conn.disconnect();
                        } catch (Exception e) {
                            UserUtils.sendLog(context, "RoutsAdapter", e.toString(), e.toString(), "Routs Adapter");
                        }
                    }).start();

                } catch (Exception e) {
                    UserUtils.sendLog(context, "RoutsAdapter", e.toString(), e.toString(), "Routs Adapter");
                }
            });
        } catch (Exception e) {
            UserUtils.sendLog(context, "RoutsAdapter", e.toString(), e.toString(), "Routs Adapter");
        }
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView txtRouteName;
        androidx.appcompat.widget.SwitchCompat switchNotification;

        public RouteViewHolder(View itemView) {
            super(itemView);
            txtRouteName = itemView.findViewById(R.id.txtRouteName);
            switchNotification = itemView.findViewById(R.id.switchNotificationrout);
        }
    }

}

