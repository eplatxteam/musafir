package com.example.musafir;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MyTripsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TripAdapter adapter;
    //    private ProgressBar progressBar;
    private boolean isLoading = false;
    private int currentPage = 1;
    private LinearLayout noDataText, noInternet;
    private boolean isLastPage = false;
    private final List<JSONObject> tripList = new ArrayList<>();
    int totalItemCount;
    String BASE_URL = UserUtils.BASE_URL;

    int lastVisibleItemPosition;
    LottieAnimationView lottieWave;

    @Override
    public void onResume() {
        super.onResume();
        currentPage = 1;
        isLastPage = false;
        isLoading = false;

        if (tripList != null) {
            tripList.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        lottieWave.setVisibility(View.VISIBLE);
        lottieWave.playAnimation();

        loadTrips(1);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTrips);
//        progressBar = view.findViewById(R.id.progressBar);
        noDataText = view.findViewById(R.id.noDataTextTrip);
        adapter = new TripAdapter(tripList, tripId -> {
            adapter.updateTripStatusToCancelled(tripId, "cancelled");
        });
        adapter.setOnTripCancelListener(getContext(), tripId -> {
            adapter.updateTripStatusToCancelled(tripId, "cancelled");
        });
        lottieWave = view.findViewById(R.id.lottieWavetrip);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        adapter = new TripAdapter(tripList);
//        lottieWave = view.findViewById(R.id.lottieWavetrip);
//
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter.setOnTripCancelListener(getContext(), tripId -> {
//            adapter.updateTripStatusToCancelled(tripId, "cancelled");
//        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                totalItemCount = layoutManager.getItemCount();
                lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItemPosition + 5)) {
                    loadTrips(++currentPage);
                }
            }
        });
        noInternet = view.findViewById(R.id.noInternet);

        if (!UserUtils.isNetworkAvailable(requireContext())) {

            requireActivity().runOnUiThread(() -> {
                noInternet.setVisibility(View.VISIBLE);
                noDataText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                lottieWave.cancelAnimation();
                lottieWave.setVisibility(View.GONE);
            });
            isLoading = false;
        }
        loadTrips(currentPage);
        return view;
    }

    private void loadTrips(int page) {
        isLoading = true;

        if (page > 1) {
            requireActivity().runOnUiThread(() -> recyclerView.post(adapter::addLoadingFooter));
        } else {
            requireActivity().runOnUiThread(() -> lottieWave.playAnimation());
        }

        new Thread(() -> {

            try {
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int passengerId = prefs.getInt("user_id", -1);
                String user_type = prefs.getString("user_type", "");

                String urlStr;
                if (user_type.equals("driver")) {
                    urlStr = BASE_URL + "trip-requests/?driver_id=" + passengerId + "&page=" + page + "&limit=6";
                } else {
                    urlStr = BASE_URL + "trip-requests/?passenger=" + passengerId + "&page=" + page + "&limit=6";
                }
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                InputStream inputStream = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONObject json = new JSONObject(response.toString());

                List<JSONObject> newTrips = new ArrayList<>();
                if (json.has("results")) {
                    JSONArray results = json.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        newTrips.add(results.getJSONObject(i));
                    }
                }

                MyTripsFragment.this.isLastPage = json.isNull("next");
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        lottieWave.setVisibility(View.GONE);
                        lottieWave.cancelAnimation();
                        if (page > 1) {
                            adapter.removeLoadingFooter();
                        } else {
                            lottieWave.setVisibility(View.GONE);
                            lottieWave.cancelAnimation();
                        }

                        if (newTrips.isEmpty() && adapter.getItemCount() == 0) {
                            // ما في ولا رحلة
                            lottieWave.playAnimation();
                            noDataText.setVisibility(View.VISIBLE);

                        } else {
                            lottieWave.setVisibility(View.GONE);
                            lottieWave.cancelAnimation();

                            if (page == 1) {
                                noDataText.setVisibility(View.GONE);
                                adapter.setTrips(newTrips);
                            } else {
                                noDataText.setVisibility(View.GONE);
                                adapter.addTrips(newTrips);
                            }

                            currentPage = page;
                        }

                        MyTripsFragment.this.isLastPage = isLastPage;
                        isLoading = false;
                    });
                }

            } catch (Exception e) {
                DBHelper dbHelper = new DBHelper(getContext());

                if (isAdded()) {
                    UserUtils.sendLog(getContext(), "loadTrips", e.toString(), e.toString(), "My Trips");
                    requireActivity().runOnUiThread(() -> {
                        if (page > 1) {
                            adapter.removeLoadingFooter();
                        } else {
                            lottieWave.setVisibility(View.GONE);
                            lottieWave.cancelAnimation();
                        }
                        UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        isLoading = false;
                    });
                }
            }
        }).start();
    }
}

