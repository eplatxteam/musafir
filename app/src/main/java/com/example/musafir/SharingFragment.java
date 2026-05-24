package com.example.musafir;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
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
    private LinearLayout noDataText, noInternet, details_share;
    Button share;
    TextView textShare, textPerson;
    MaterialCardView cardshare;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sharing, container, false);
        recyclerView = view.findViewById(R.id.RecyclerViewSharing);
        lottieWave = view.findViewById(R.id.lottieWaveNot);
        noDataText = view.findViewById(R.id.noDataText);
        noInternet = view.findViewById(R.id.noInternet);
        cardshare = view.findViewById(R.id.cardshare);
        details_share = view.findViewById(R.id.details_share);
        share = view.findViewById(R.id.share);
        textShare = view.findViewById(R.id.textShare);
        textPerson = view.findViewById(R.id.textPerson);
        DBHelper dbHelper = new DBHelper(getContext());

        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        textPerson.setText(UserUtils.getMessageFromLocalNew(381, dbHelper));
        share.setOnClickListener(v -> {
            SharedPreferences prefsLink = requireActivity().getSharedPreferences("prefsLink", Context.MODE_PRIVATE);
            SharedPreferences prefs2 = SharedPrefsHelper.get(getContext());
//            SharedPreferences prefs2 = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String linkApp = prefsLink.getString("link_app", "");
            String user_phone = prefs2.getString("user_phone", "");
            String playStoreLink = linkApp + "&referrer=invite%3D" + user_phone;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "تطبيق مسافر");

            shareIntent.putExtra(Intent.EXTRA_TEXT, UserUtils.getMessageFromLocalNew(312, dbHelper) + "\n" + playStoreLink);

            startActivity(Intent.createChooser(shareIntent, "دعوة صديق عبر"));
        });
        loadReferralData();
        return view;
    }

    List<JSONObject> referralList = new ArrayList<>();
    RecyclerView.Adapter adapter;

    private void loadReferralData() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        String token = prefs.getString("auth_token", "");
        String myPhone = prefs.getString("user_phone", "");
        DBHelper dbHelper = new DBHelper(getContext());

        lottieWave.setVisibility(View.VISIBLE);
        cardshare.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        noDataText.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "my_referrals/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("phone", myPhone);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                InputStream is;
                if (responseCode >= 200 && responseCode < 300) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }

                if (is != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) result.append(line);
                    reader.close();

                    if (responseCode == 200) {
                        JSONArray jsonArray = new JSONArray(result.toString());
                        referralList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            referralList.add(jsonArray.getJSONObject(i));
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                lottieWave.setVisibility(View.GONE);
                                if (referralList.isEmpty()) {
                                    cardshare.setVisibility(View.GONE);
                                    noDataText.setVisibility(View.VISIBLE);
                                    details_share.setVisibility(View.VISIBLE);
                                    textShare.setText(UserUtils.getMessageFromLocalNew(313, dbHelper));
                                } else {
                                    cardshare.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    noDataText.setVisibility(View.GONE);
                                    details_share.setVisibility(View.GONE);
                                    setupAdapter();
                                }
                            });
                        }
                    } else {
                        UserUtils.sendLog(getContext(), "loadReferralData_Error", String.valueOf(responseCode), result.toString(), "SharingFragment");
                        handleErrorUI(dbHelper);
                    }
                } else {
                    handleErrorUI(dbHelper);
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.sendLog(getContext(), "loadReferralData_Exception", e.toString(), e.getMessage(), "SharingFragment");
                        noInternet.setVisibility(View.VISIBLE);
                        cardshare.setVisibility(View.GONE);
                        lottieWave.setVisibility(View.GONE);
                    });
                }
            }
        }).start();
    }

    private void handleErrorUI(DBHelper dbHelper) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                cardshare.setVisibility(View.GONE);
                noDataText.setVisibility(View.VISIBLE);
                details_share.setVisibility(View.VISIBLE);
                textShare.setText(UserUtils.getMessageFromLocalNew(313, dbHelper));
                lottieWave.setVisibility(View.GONE);
            });
        }
    }


    private void setupAdapter() {
        adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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