// FavoriteFragment.java
package com.example.musafir;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView noDataText;
    private String BASE_URL = UserUtils.BASE_URL;
    private List<JSONObject> routesList = new ArrayList<>();
   @Override
public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) { MenuItem placeholderItem = menu.findItem(R.id.action_placeholder); if (placeholderItem != null) { placeholderItem.setVisible(true); View actionView = placeholderItem.getActionView(); if (actionView != null) { actionView.setPressed(true); actionView.postDelayed(() -> actionView.setPressed(false), 100); } actionView.setOnClickListener(v -> { requireActivity().onBackPressed(); }); } }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recyclerViewRouts);
        progressBar = view.findViewById(R.id.progressBar);
        noDataText = view.findViewById(R.id.noDataText);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        -------------- تعطيل ------------------
        SwipeRefreshLayout swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setEnabled(false);
//        ------------------------------------------------
        progressBar.setVisibility(View.VISIBLE);
        loadRouts(); // جلب البيانات من السيرفر قبل عرضها

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomePage) {
            ((HomePage) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void loadRouts() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        int user_id = prefs.getInt("user_id", -1);
        new Thread(() -> {
            try {
                String urlStr = BASE_URL + "DriverRoutes/?driver_id=" + user_id;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
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

                String respStr = response.toString().trim();
                List<JSONObject> newRoutes = new ArrayList<>();

                if (respStr.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(respStr);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        newRoutes.add(jsonArray.getJSONObject(i));
                    }
                }

                routesList = newRoutes;

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (routesList.isEmpty()) {
                            noDataText.setVisibility(View.VISIBLE);
                        } else {
                            noDataText.setVisibility(View.GONE);
                            recyclerView.setAdapter(new RoutsAdapter(getContext(), routesList));
                        }
                    });
                }

            } catch (Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        UserUtils.sendLog(getContext(), "loadRouts", e.toString(), e.toString(), "Favorite Fragment");
                        progressBar.setVisibility(View.GONE);
                        DBHelper dbHelper = new DBHelper(getContext());

                        UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                    });
                }
            }
        }).start();
    }
}
