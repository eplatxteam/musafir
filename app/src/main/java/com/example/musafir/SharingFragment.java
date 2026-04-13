package com.example.musafir;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class SharingFragment extends Fragment {

    String BASE_URL = UserUtils.BASE_URL;
    LottieAnimationView lottieWave;
    RecyclerView recyclerView;
    private LinearLayout noDataText, noInternet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sharing, container, false);
        recyclerView = view.findViewById(R.id.RecyclerViewSharing);
        lottieWave = view.findViewById(R.id.lottieWaveNot);
        noDataText = view.findViewById(R.id.noDataText);
        noInternet = view.findViewById(R.id.noInternet);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        loadReferralData();
        return view;
    }

    List<JSONObject> referralList = new ArrayList<>();
    RecyclerView.Adapter adapter;

    private void loadReferralData() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");
        String myPhone = prefs.getString("phone_number", "");

        lottieWave.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "my_referrals/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // 1. تغيير الطريقة إلى POST
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                // 2. إنشاء جسم الطلب (JSON Body) وإرسال الرقم
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("phone", myPhone);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                // 3. قراءة الرد
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    recyclerView.setVisibility(View.VISIBLE);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) result.append(line);
                    reader.close();

                    JSONArray jsonArray = new JSONArray(result.toString());
                    referralList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        referralList.add(jsonArray.getJSONObject(i));
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            lottieWave.setVisibility(View.GONE);
                            if (referralList.isEmpty()) {
                                noDataText.setVisibility(View.VISIBLE);
                            } else {
                                noDataText.setVisibility(View.GONE);
                                setupAdapter();
                            }
                        });
                    }
                } else {

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                        {
                            noDataText.setVisibility(View.VISIBLE);
                            lottieWave.setVisibility(View.GONE);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                                noInternet.setVisibility(View.VISIBLE);
                                lottieWave.setVisibility(View.GONE);
                            }
                    );
                }
            }
        }).start();
    }

    private void setupAdapter() {
        adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // استخدم نفس تصميم الأسطر الذي لديك أو سطر بسيط
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_referral, parent, false);
                return new RecyclerView.ViewHolder(v) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                JSONObject data = referralList.get(position);

                TextView name = holder.itemView.findViewById(R.id.refName);
                TextView date = holder.itemView.findViewById(R.id.refDate);
                TextView type = holder.itemView.findViewById(R.id.refType);

                name.setText(data.optString("full_name", "بدون اسم"));
                date.setText(data.optString("created_at", "").split("T")[0]);

                String userType = data.optString("user_type", "");
                type.setText(userType.equals("driver") ? "سائق" : "مسافر");
            }

            @Override
            public int getItemCount() {
                return referralList.size();
            }

        };
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}