package com.example.musafir;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import jp.wasabeef.blurry.Blurry;

public class MyBookingsFragment extends Fragment {

    //    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    public static BookingAdapter adapter;
    public static boolean isLoading = false;
    public static int currentPage = 1;
    public static LinearLayout noDataText, noInternet;
    public static boolean isLastPage = false;
    static String BASE_URL = UserUtils.BASE_URL;

    public static final List<JSONObject> bookingsList = new ArrayList<>();
    public static LottieAnimationView lottieWave;
//    public static SwipeRefreshLayout swipeRefreshLayout;

    public void onResume() {
        super.onResume();
        currentPage = 1;
        isLastPage = false;
        isLoading = false;

        if (bookingsList != null) {
            bookingsList.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        lottieWave.setVisibility(View.VISIBLE);
        lottieWave.playAnimation();
        noDataText.setVisibility(View.GONE);

        fetchBooking(1, getContext());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_bookings, container, false);
//        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewBooking);
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//            currentPage = 1;
//            isLastPage = false;
//            isLoading = false;
//
//            if (bookingsList != null) {
//                bookingsList.clear();
//                if (adapter != null) {
//                    adapter.notifyDataSetChanged();
//                }
//            }
//
//            lottieWave.setVisibility(View.VISIBLE);
//            lottieWave.playAnimation();
//            noDataText.setVisibility(View.GONE);
//
//        });

//        swipeRefreshLayout.post(() -> {
//            swipeRefreshLayout.setRefreshing(true);
//        });

//        progressBar = view.findViewById(R.id.progressBar);
        noDataText = view.findViewById(R.id.noDataTextbooking);
        lottieWave = view.findViewById(R.id.lottieWavebooking);

//        adapter = new BookingAdapter(bookingsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingAdapter(bookingsList,
                bookingId -> {
                    showCancelReasonDialog(bookingId);
                },
                bookingId -> {
                    sendApproveRequest(requireContext(), bookingId);
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && !isLastPage && totalItemCount <= (lastVisibleItemPosition + 5)) {
                    fetchBooking(currentPage + 1, getContext());
                }
            }
        });

        fetchBooking(currentPage, getContext());
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
        return view;
    }

    private void sendApproveRequest(Context context, int bookingId) {
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "bookings/" + bookingId + "/complete/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setDoOutput(true);
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", "");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("token", token);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                String jsonString = jsonObject.toString();
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                InputStream inputStream = (responseCode >= 400) ? conn.getErrorStream() : conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line.trim());
                }
                reader.close();

                String responseStr = responseBuilder.toString();
                final String[] msg = new String[1];
                try {
                    JSONObject respJson = new JSONObject(responseStr);
                    msg[0] = respJson.has("detail") ? respJson.getString("detail") : "تمت الموافقة على الحجز بنجاح";
                } catch (Exception e) {
                    UserUtils.getMessageFromLocal(53, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            msg[0] = message;
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }

                String finalMsg = msg[0];
                ((Activity) context).runOnUiThread(() -> {
                    UserUtils.ToastMessages((Activity) context, finalMsg);
                    if (responseCode >= 200 && responseCode < 300) {
                        currentPage = 1;
                        fetchBooking(currentPage, getContext());
                    }
                });

            } catch (Exception e) {
                ((Activity) context).runOnUiThread(() -> {
                    UserUtils.sendLog(getContext(), "sendApproveRequest", e.toString(), e.toString(), "My Bookings");
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private AlertDialog exitDialog;

    private void showCancelReasonDialog(int bookingId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        DBHelper dbHelper = new DBHelper(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_cancel_booking, null);
        builder.setView(dialogView);
        EditText input = dialogView.findViewById(R.id.etReason);
        Button btnAdd = dialogView.findViewById(R.id.btnYes);
        Button btnCancel = dialogView.findViewById(R.id.btnNo);

        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialog.show();

        UserUtils.setEditTextState(input, false);

        btnAdd.setOnClickListener(v -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) {
                input.setError("الرجاء إدخال سبب الإلغاء");
                UserUtils.setEditTextState(input, true);
                return;
            } else {
                UserUtils.setEditTextState(input, false);
            }
            if (getActivity() == null || getActivity().isFinishing()) return;

            AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
            View dialogView2 = inflater.inflate(R.layout.dialog_custom_confirmationt, null);
            builder2.setView(dialogView2);

            Button btnYes = dialogView2.findViewById(R.id.btnYes);
            Button btnNo = dialogView2.findViewById(R.id.btnNo);
            TextView tvMessage = dialogView2.findViewById(R.id.tvMessage);
            tvMessage.setText("هل أنت متأكد أنك تريد إلغاء الحجز؟");
            btnYes.setTextSize(18);
            btnNo.setTextSize(18);

            exitDialog = builder2.create();

//            Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
//            exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));

            dialog.dismiss();

            btnYes.setOnClickListener(v2 -> {
                String cancellationDate = getCurrentDateTime();
                UserUtils.getMessageFromLocal(50, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(getActivity(), message);
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
                sendCancelRequest(requireContext(), bookingId, reason, cancellationDate);
                exitDialog.dismiss();
            });

            btnNo.setOnClickListener(v2 -> exitDialog.dismiss());

            if (exitDialog.getWindow() != null) {
                exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
            }

            if (isAdded() && !getActivity().isFinishing()) {
                exitDialog.show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }


    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void sendCancelRequest(Context context, int bookingsId, String reason, String cancellationDate) {
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "bookings/" + bookingsId + "/cancel/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                conn.setDoOutput(true);
                SharedPreferences prefs = SharedPrefsHelper.get(context);

//                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cancellation_reason", reason);
                jsonObject.put("cancellation_date", cancellationDate);
                String s = "{ cancellation_reason: " + reason +
                        " , cancellation_date: " + cancellationDate +
                        " }";
                String user_type = prefs.getString("user_type", "");
                if ("driver".equals(user_type)) {
                    jsonObject.put("booking_status", "cancelled_by_driver");
                } else {
                    jsonObject.put("booking_status", "cancelled_by_passenger");
                }
                String jsonString = jsonObject.toString();

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }


                int responseCode = conn.getResponseCode();
                InputStream inputStream = (responseCode >= 400) ? conn.getErrorStream() : conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line.trim());
                }
                reader.close();

                String responseStr = responseBuilder.toString();

                ((Activity) context).runOnUiThread(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        UserUtils.getMessageFromLocal(51, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                if (isAdded()) UserUtils.ToastMessages(getActivity(), message);
                            }
                            @Override
                            public void onError(String error) {}
                        });

                        if (adapter != null) {
                            adapter.updateItemStatus(bookingsId, "cancelled");
                        }

                        // adapter.removeItem(bookingsId);

                    }else {
                        UserUtils.sendLog(getContext(), "sendCancelRequest", responseStr, s, "My Bookings");
                        UserUtils.getMessageFromLocal(52, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                    }
                });

            } catch (Exception e) {
                ((Activity) context).runOnUiThread(() -> {
                    UserUtils.sendLog(getContext(), "sendCancelRequest", e.toString(), e.toString(), "My Bookings");
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    public static void showLoading() {
        if (lottieWave != null) {
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
            noDataText.setVisibility(View.GONE);
        }
    }

    public static void fetchBooking(int page, Context context) {
        isLoading = true;
        DBHelper dbHelper = new DBHelper(context);

        new Thread(() -> {
            try {
                SharedPreferences prefs = SharedPrefsHelper.get(context);

                int passengerId = prefs.getInt("user_id", -1);
                String user_type = prefs.getString("user_type", "");

                String urlStr;
                if (user_type.equals("driver")) {
                    urlStr = BASE_URL + "bookings/?driver_id=" + passengerId + "&page=" + page + "&limit=6";
                } else {
                    urlStr = BASE_URL + "bookings/?passenger=" + passengerId + "&page=" + page + "&limit=6";
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

                        if (page == 1) {
                            adapter.clearBookings();
                        }

                        if (newTrips.isEmpty() && adapter.getItemCount() == 0) {
                            if (noDataText != null) noDataText.setVisibility(View.VISIBLE);
                        } else {
                            if (noDataText != null) noDataText.setVisibility(View.GONE);
                            adapter.addBooking(newTrips);
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
                    UserUtils.sendLog(context, "fetchBooking", String.valueOf(responseCode), logBody, "My Bookings");
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
                UserUtils.sendLog(context, "fetchBooking", e.toString(), e.toString(), "My Bookings");
            }
        }).start();
    }
}
