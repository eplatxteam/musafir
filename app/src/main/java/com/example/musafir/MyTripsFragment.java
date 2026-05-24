package com.example.musafir;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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

    public static RecyclerView recyclerView;
    public static TripAdapter adapter;
    //    private ProgressBar progressBar;
    public static boolean isLoading = false;
    public static int currentPage = 1;
    public static LinearLayout noDataText, noInternet;
    public static boolean isLastPage = false;
    public static List<JSONObject> tripList = new ArrayList<>();
    int totalItemCount;
    static String BASE_URL = UserUtils.BASE_URL;
    public static ProgressBar paginationLoader;
    int lastVisibleItemPosition;
    public static LottieAnimationView lottieWave;

    @Override
    public void onResume() {
        super.onResume();
        // إعادة ضبط عداد الصفحات والحالة
        currentPage = 1;
        isLastPage = false;
        isLoading = false;

        // مسح البيانات القديمة لضمان عدم التكرار
        if (tripList != null) {
            tripList.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        // إظهار مؤشر التحميل
        if (lottieWave != null) {
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
        }

        // طلب الصفحة الأولى من السيرفر
        loadTrips(1, getContext());
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
        paginationLoader = view.findViewById(R.id.paginationLoader); // ربط اللودر السفلي</selection>
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                totalItemCount = layoutManager.getItemCount();
                lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItemPosition + 5)) {
                    // إظهار اللودر السفلي عند بدء تحميل صفحة جديدة
                    if (paginationLoader != null) {
                        paginationLoader.setVisibility(View.VISIBLE);
                    }
                    loadTrips(++currentPage, getContext());
                }
//                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItemPosition + 5)) {
//                    loadTrips(++currentPage, getContext());
//                }
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
        loadTrips(currentPage, getContext());
        return view;
    }


    public static void loadTrips(int page, Context context) {
        isLoading = true;
        DBHelper dbHelper = new DBHelper(context);

        new Thread(() -> {
            try {
                SharedPreferences prefs = SharedPrefsHelper.get(context);

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

                int responseCode = conn.getResponseCode();
                StringBuilder result = new StringBuilder();
                BufferedReader reader;

                if (responseCode >= 200 && responseCode < 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                String responseBody = result.toString();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject responseObject = new JSONObject(responseBody);
                    JSONArray resultsArray = responseObject.getJSONArray("results");

                    List<JSONObject> newTrips = new ArrayList<>();
                    for (int i = 0; i < resultsArray.length(); i++) {
                        newTrips.add(resultsArray.getJSONObject(i));
                    }

                    isLastPage = responseObject.isNull("next");

                    // تحديث UI عبر الـ main thread
                    ((FragmentActivity) context).runOnUiThread(() -> {
                        if (lottieWave != null) {
                            lottieWave.setVisibility(View.GONE);
                            lottieWave.cancelAnimation();
                        }
                        if (paginationLoader != null) {
                            paginationLoader.setVisibility(View.GONE);
                        }
                        if (page == 1) {
                            adapter.clearTrips();
                        }

                        if (newTrips.isEmpty() && adapter.getItemCount() == 0) {
                            if (noDataText != null) noDataText.setVisibility(View.VISIBLE);
                        } else {
                            if (noDataText != null) noDataText.setVisibility(View.GONE);
                            adapter.addTrips(newTrips);
                            currentPage = page;
                        }
                        isLoading = false;
                    });

                } else {
                    ((FragmentActivity) context).runOnUiThread(() -> {
                        if (lottieWave != null) {
                            lottieWave.setVisibility(View.GONE);
                            lottieWave.cancelAnimation();
                        }
                        if (paginationLoader != null) {
                            paginationLoader.setVisibility(View.GONE);
                        }
                    });

                    UserUtils.getMessageFromLocal(38, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            ((FragmentActivity) context).runOnUiThread(() ->
                                    UserUtils.ToastMessages((Activity) context, message));
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });

                    String logBody = responseBody != null && responseBody.length() > 4000
                            ? responseBody.substring(0, 4000) : responseBody;
                    UserUtils.sendLog(context, "loadTrips", String.valueOf(responseCode), logBody, "My Trips");
                }
                conn.disconnect();
            } catch (Exception e) {
                if (context != null && context instanceof FragmentActivity) {
                    ((FragmentActivity) context).runOnUiThread(() -> {
                        if (lottieWave != null) {
                            lottieWave.setVisibility(View.GONE);
                            lottieWave.cancelAnimation();
                        }
                    });
                }
                UserUtils.sendLog(context, "loadTrips", e.toString(), e.toString(), "My Trips");
            }
        }).start();
    }

//    public static void loadTrips(int page, Context context) {
//        isLoading = true;
//
//        if (page > 1) {
//            requireActivity().runOnUiThread(() -> recyclerView.post(adapter::addLoadingFooter));
//        } else {
//            requireActivity().runOnUiThread(() -> lottieWave.playAnimation());
//        }
//
//        new Thread(() -> {
//
//            try {
//                SharedPreferences prefs = SharedPrefsHelper.get(context);
//

    /// /                SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
//                int passengerId = prefs.getInt("user_id", -1);
//                String user_type = prefs.getString("user_type", "");
//
//                String urlStr;
//                if (user_type.equals("driver")) {
//                    urlStr = BASE_URL + "trip-requests/?driver_id=" + passengerId + "&page=" + page + "&limit=6";
//                } else {
//                    urlStr = BASE_URL + "trip-requests/?passenger=" + passengerId + "&page=" + page + "&limit=6";
//                }
//                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
//                conn.setRequestMethod("GET");
//                conn.setRequestProperty("Accept", "application/json");
//
//                String token = prefs.getString("auth_token", null);
//
//                if (token != null) {
//                    conn.setRequestProperty("Authorization", "Bearer " + token);
//                }
//                InputStream inputStream = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                StringBuilder response = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//                reader.close();
//                conn.disconnect();
//
//                JSONObject json = new JSONObject(response.toString());
//
//                List<JSONObject> newTrips = new ArrayList<>();
//                if (json.has("results")) {
//                    JSONArray results = json.getJSONArray("results");
//                    for (int i = 0; i < results.length(); i++) {
//                        newTrips.add(results.getJSONObject(i));
//                    }
//                }
//
//                MyTripsFragment.this.isLastPage = json.isNull("next");
//                ((FragmentActivity) context).runOnUiThread(() -> {
//                    if (isAdded()) {
//                        // 1. إخفاء وإيقاف الأنيميشن عند وصول أي بيانات
//                        lottieWave.setVisibility(View.GONE);
//                        lottieWave.cancelAnimation();
//
//                        if (page > 1) {
//                            adapter.clearTrips();
//                        }
//
//                        // 2. التعديل الجوهري: مسح القائمة فقط إذا كنا في الصفحة الأولى
//                        if (page == 1) {
//                            tripList.clear();
//                            noDataText.setVisibility(newTrips.isEmpty() ? View.VISIBLE : View.GONE);
//                        }
//
//                        // 3. إضافة البيانات الجديدة للقائمة الأصلية
//                        if (!newTrips.isEmpty()) {
//                            tripList.addAll(newTrips);
//                            noDataText.setVisibility(View.GONE);
//                        }
//
//                        // 4. تحديث الأدابتر وإبلاغه بتغيير البيانات
//                        adapter.notifyDataSetChanged();
//
//                        // 5. تحديث رقم الصفحة الحالي وحالة التحميل
//                        currentPage = page;
//                        isLoading = false;
//                        // تم أخذ قيمة isLastPage مسبقاً من الـ JSON
//                    }
//                });
//            } catch (Exception e) {
//                if (context != null && context instanceof FragmentActivity) {
//                    ((FragmentActivity) context).runOnUiThread(() -> {
//                        DBHelper dbHelper = new DBHelper(context);
//
//                        if (isAdded()) {
//                            UserUtils.sendLog(getContext(), "loadTrips", e.toString(), e.toString(), "My Trips");
//                            requireActivity().runOnUiThread(() -> {
//                                if (page > 1) {
//                                    adapter.removeLoadingFooter();
//                                } else {
//                                    lottieWave.setVisibility(View.GONE);
//                                    lottieWave.cancelAnimation();
//                                }
//                                UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
//                                    @Override
//                                    public void onSuccess(String message) {
//                                        UserUtils.ToastMessages(getActivity(), message);
//                                    }
//
//                                    @Override
//                                    public void onError(String error) {
//                                    }
//
//                                });
//                                isLoading = false;
//                            });
//                        }
//                    });
//                }
//            }
//        }).start();
//    }
    public static void showLoading() {
        if (lottieWave != null) {
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
            noDataText.setVisibility(View.GONE);
        }
    }
}

