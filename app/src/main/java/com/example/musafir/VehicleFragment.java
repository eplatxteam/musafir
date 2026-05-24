package com.example.musafir;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VehicleFragment extends Fragment {

    private RecyclerView recyclerView;
    private VehicleAdapter adapter;
    private final List<JSONObject> vehicleList = new ArrayList<>();
    private final String BASE_URL = UserUtils.BASE_URL;

    private int currentPage = 1;
    private boolean isLastPage = false;
    private boolean isLoading = false;

    private LottieAnimationView lottieWave;
    private LinearLayout noVehicleText;
    private boolean dataLoaded = false;

    // ==============================================================
    // إضافة زر مخصص في Toolbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem addVehicleItem = menu.findItem(R.id.action_placeholder2);
        if (addVehicleItem != null) {
            addVehicleItem.setIcon(R.drawable.baseline_add_24);
            addVehicleItem.setVisible(true);
//            addVehicleItem.setTitle("إضافة مركبة");
        }

        MenuItem placeholderItem = menu.findItem(R.id.action_placeholder);
        if (placeholderItem != null) {
            placeholderItem.setVisible(true);
            View actionView = placeholderItem.getActionView();
            if (actionView != null) {
                actionView.setPressed(true);
                actionView.postDelayed(() -> actionView.setPressed(false), 100);
            }
            actionView.setOnClickListener(v -> {
                requireActivity().onBackPressed();
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_placeholder2) {
            openAddVehicleFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void openAddVehicleFragment() {
        Fragment fragment = new AddVehicleFragment();
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.full_screen_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
//        ((HomePage) requireActivity()).updateToolbar("إضافة مركبة", false, R.drawable.local);
    }

    // ==============================================================
    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        if (dataLoaded) {
            lottieWave.setVisibility(View.GONE);
        }
    }


    // ==============================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicle, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recyclerViewVehicles);
        lottieWave = view.findViewById(R.id.lottieWaveVeh);
        noVehicleText = view.findViewById(R.id.noVehicleText);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new VehicleAdapter(getContext(), vehicleList, BASE_URL);
        recyclerView.setAdapter(adapter);

        fetchVehicles(1);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) && !isLoading && !isLastPage) {
                    fetchVehicles(currentPage + 1);
                }
            }
        });

        return view;
    }

    // ==============================================================
    private void fetchVehicles(int page) {
        isLoading = true;
        showLoading(true);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());
//                SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", requireActivity().MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                String token = prefs.getString("auth_token", null);

                if (userId == -1) {
                    throw new Exception("User ID not found in SharedPreferences");
                }

                URL url = new URL(BASE_URL + "vehicles/?owner=" + userId + "&page=" + page + "&limit=6");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                if (conn.getResponseCode() != 200) {
                    throw new Exception("HTTP error code: " + conn.getResponseCode());
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject jsonResponse = new JSONObject(result.toString());
                JSONArray resultsArray = jsonResponse.optJSONArray("results");

                List<JSONObject> newVehicles = new ArrayList<>();
                if (resultsArray != null) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        newVehicles.add(resultsArray.getJSONObject(i));
                    }
                }

                requireActivity().runOnUiThread(() -> {
                    if (page == 1) {
                        vehicleList.clear();
                    }

                    vehicleList.addAll(newVehicles);
                    adapter.notifyDataSetChanged();

                    boolean empty = vehicleList.isEmpty();
                    noVehicleText.setVisibility(empty ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);

                    isLastPage = jsonResponse.isNull("next");
                    currentPage = page;
                    isLoading = false;
                    showLoading(false);
                    dataLoaded = true;

                });

            } catch (Exception e) {
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        isLoading = false;
                        showLoading(false);
                        if (vehicleList.isEmpty()) {
                            noVehicleText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                        UserUtils.sendLog(getContext(), "VehicleFragment", e.toString(), e.toString(), "fetchVehicles");
                    });
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    // ==============================================================
    private void showLoading(boolean show) {
        if (show) {
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
        } else {
            lottieWave.setVisibility(View.GONE);
            lottieWave.cancelAnimation();
        }
    }
}
