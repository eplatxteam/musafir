package com.example.musafir;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.jetbrains.annotations.Nullable;
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

import jp.wasabeef.blurry.Blurry;

public class DriverTripRequest extends Fragment {

    private RecyclerView recyclerView;
    private DriverTripRequestAdapter adapter;
    private List<JSONObject> requestList = new ArrayList<>();
    // private ProgressBar loadingProgress;
    private LinearLayout norequestText;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private int currentPage = 0;
    private int nextPageToLoad = 1;
    private final int pageSize = 5;
    String BASE_URL = UserUtils.BASE_URL;
    LottieAnimationView lottieWave;
    DBHelper dbHelper = new DBHelper(getContext());

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_driver_trip_request, container, false);
        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.requestRecyclerView);
        norequestText = view.findViewById(R.id.norequestText);
        lottieWave = view.findViewById(R.id.lottieWavereq);

        dbHelper = new DBHelper(requireContext());

        adapter = new DriverTripRequestAdapter(requireContext(), requestList, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs =
//                requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        loadDriverTripRequest(1);

        // --- Scroll Listener (Pagination) ---
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager == null) return;

                int totalItemCount = layoutManager.getItemCount();
                int visibleItemCount = layoutManager.getChildCount();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && hasMoreData) {
                    if ((visibleItemCount + lastVisibleItemPosition) >= totalItemCount
                            && lastVisibleItemPosition >= 0
                            && totalItemCount >= pageSize) {

                        loadDriverTripRequest(nextPageToLoad);
                    }
                }
            }
        });

        UserUtils.updateProfile(getContext(), new UserUtils.ProfileUpdateCallback() {
            @Override
            public void onProfileUpdated(boolean isVerified, boolean isActive) {
                if (isVerified) {
                    setupAdapter();
                    loadDriverTripRequest(1);
                } else {
                    norequestText.setVisibility(View.VISIBLE);
                    UserUtils.getMessageFromLocal(26, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
                if (!isActive) {
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    UserUtils.getMessageFromLocal(22, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
            }
        });
        loadDriverTripRequest(1);

        return view;
    }

    private AlertDialog exitDialog;


    private void setupAdapter() {
        adapter = new DriverTripRequestAdapter(getContext(), requestList, new DriverTripRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptRequest(int requestId, int driver_id, String passenger,String car_Code) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                View dialogView2 = LayoutInflater.from(getContext()).inflate(R.layout.dialog_driver_trip, null);
                builder2.setView(dialogView2);

                Button btnYes = dialogView2.findViewById(R.id.btnYes);
                Button btnNo = dialogView2.findViewById(R.id.btnNo);
                EditText inputNotes = dialogView2.findViewById(R.id.inputNotes);
                EditText inputprice = dialogView2.findViewById(R.id.inputprice);
                TextView car_code = dialogView2.findViewById(R.id.car_code);
                car_code.setText(car_Code);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                UserUtils.setEditTextState(inputNotes, false);
                UserUtils.setEditTextState(inputprice, false);
                AlertDialog dialog = builder.create();
                exitDialog = builder2.create();

                dialog.dismiss();
                btnYes.setOnClickListener(v2 -> {
                    DBHelper dbHelper = new DBHelper(getContext());
                    if (inputprice.getText().toString().trim().isEmpty()) {
                        inputprice.setError("الرجاء إدخال اسم المسافر");
                        UserUtils.setEditTextState(inputprice, true);
                    } else {
                        inputprice.setError(null);
                        UserUtils.setEditTextState(inputprice, false);

                        String driverNotes = inputNotes.getText().toString().trim();
                        String driverPrice = inputprice.getText().toString().trim();

                        //   الإرسال مع الملاحظة
                        ProgressDialog progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("جاري تأكيد الطلب...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        new Thread(() -> {
                            try {
                                String deviceId = UserUtils.getDeviceID(getContext());
                                String deviceInfo = UserUtils.getDeviceInfo();
                                String urlStr = BASE_URL + "trip-requests/update_request_status/?device_id=" + deviceId + "&device_info=" + deviceInfo;
                                URL url = new URL(urlStr);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/json");
                                conn.setDoOutput(true);
                                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                                SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
                                String token = prefs.getString("auth_token", null);

                                if (token != null) {
                                    conn.setRequestProperty("Authorization", "Bearer " + token);
                                }
                                JSONObject postData = new JSONObject();
                                postData.put("request_id", requestId);
                                postData.put("driver_id", driver_id);
                                postData.put("passenger_id", passenger);
                                postData.put("price", driverPrice);
                                postData.put("driver_notes", driverNotes); // إضافة الملاحظة

                                OutputStream os = conn.getOutputStream();
                                os.write(postData.toString().getBytes("UTF-8"));
                                os.close();
                                String s = "{ request_id: " + requestId +
                                        " , driver_id: " + driver_id +
                                        " , passenger: " + passenger +
                                        " , driver_notes: " + driverNotes +
                                        " , price: " + driverPrice +
                                        " }";
                                int responseCode = conn.getResponseCode();
                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    getActivity().runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        UserUtils.getMessageFromLocal(27, dbHelper, new UserUtils.MessageCallback() {
                                            @Override
                                            public void onSuccess(String message) {
                                                UserUtils.ToastMessages(getActivity(), message);
                                            }

                                            @Override
                                            public void onError(String error) {
                                            }

                                        });
                                        ((HomePage) requireActivity()).selectTab(R.id.nav_reservation);
                                        openBookingFragment(1, "رحلاتي");
//                                    currentPage = 0;
//                                    nextPageToLoad = 1;
//                                    hasMoreData = true;
//                                    requestList.clear();
//                                    adapter.notifyDataSetChanged();
//                                    loadDriverTripRequest(1);
                                    });
                                } else {
                                    getActivity().runOnUiThread(() -> {
                                        UserUtils.sendLog(getContext(), "setupAdapter", s, String.valueOf(responseCode), "Driver Trip Request");
                                        progressDialog.dismiss();
                                        UserUtils.getMessageFromLocal(28, dbHelper, new UserUtils.MessageCallback() {
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
                                conn.disconnect();
                            } catch (Exception e) {
                                UserUtils.sendLog(getContext(), "setupAdapter", e.toString(), e.toString(), "Driver Trip Request");
                                getActivity().runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    UserUtils.getMessageFromLocal(29, dbHelper, new UserUtils.MessageCallback() {
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
                        }).start();
                        exitDialog.dismiss();
                    }
                });

                btnNo.setOnClickListener(v2 -> {
                    exitDialog.dismiss();
                });
                ViewGroup decorView = requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
                exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));
                if (exitDialog.getWindow() != null) {
                    exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
                }

                exitDialog.show();

            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void openBookingFragment(int tabIndex, String title) {
        BookingFragment bookingFragment = new BookingFragment();
        Bundle args = new Bundle();
        args.putInt("tab_to_open", tabIndex);
        bookingFragment.setArguments(args);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.full_screen_container, bookingFragment)
                .addToBackStack(null)
                .commit();

        ((HomePage) requireActivity()).updateToolbar(title, false, R.drawable.airplane_new, 1);
    }

    private void loadDriverTripRequest(int page) {
        if (isLoading || !hasMoreData) return;

        isLoading = true;

        if (page > 1) {
            getActivity().runOnUiThread(() -> recyclerView.post(() -> adapter.addLoadingFooter()));
        } else {
            getActivity().runOnUiThread(() -> {
                lottieWave.playAnimation();
                lottieWave.setVisibility(View.VISIBLE);
                norequestText.setVisibility(View.GONE);
            });
        }
        new Thread(() -> {
            try {
                String urlStr = BASE_URL + "trip-requests/?page=" + page;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();

                    JSONObject jsonObject = new JSONObject(responseBuilder.toString());
                    JSONArray resultsArray = jsonObject.getJSONArray("results");

                    List<JSONObject> notifications = new ArrayList<>();

                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject obj = resultsArray.optJSONObject(i);
                        if (obj != null && obj.length() > 0) {
                            notifications.add(obj);
                        }
                    }

                    boolean lastPage = jsonObject.isNull("next");

                    getActivity().runOnUiThread(() -> {
                        if (page > 1) {
                            recyclerView.post(() -> adapter.removeLoadingFooter());
                        } else {
                            lottieWave.setVisibility(View.GONE);
                            lottieWave.cancelAnimation();
                        }

                        if (page == 1) {
                            requestList.clear();
                        }

                        if (notifications.isEmpty() && (adapter == null || adapter.getItemCount() == 0)) {
                            norequestText.setVisibility(View.VISIBLE);
                        } else {
                            norequestText.setVisibility(View.GONE);
                            requestList.addAll(notifications);
                            adapter.notifyDataSetChanged();

                            currentPage = page;
                            nextPageToLoad = page + 1;
                            hasMoreData = !lastPage;
                        }
                    });

                    isLoading = false;
                    conn.disconnect();
                }

            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "loadDriverTripRequest", e.toString(), e.toString(), "Driver Trip Request");
                getActivity().runOnUiThread(() -> {
                    if (page > 1)
                        recyclerView.post(() -> adapter.removeLoadingFooter());
                    else {
                        lottieWave.setVisibility(View.GONE);
                        lottieWave.cancelAnimation();
                    }
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
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
        }).start();
    }
}