package com.example.musafir;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<JSONObject> notificationList = new ArrayList<>();
    //    private ProgressBar loadingProgress;
    private LinearLayout noNotificationsText, noInternet;

    private boolean isLoading = false;
    private boolean hasMoreData = true;

    private int currentPage = 0;
    private int nextPageToLoad = 1;
    private final int pageSize = 5;
    String BASE_URL = UserUtils.BASE_URL;
    LottieAnimationView lottieWave;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onPause() {
        super.onPause();

        Toolbar toolbar = requireActivity().findViewById(R.id.main_toolbar);
        toolbar.setNavigationIcon(null);
//        toolbar.setTitle(R.string.app_name);
    }


    public NotificationFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof HomePage) {
            ((HomePage) getActivity()).updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);
        }

        currentPage = 1;
        nextPageToLoad = 1;
        isLoading = false;
        hasMoreData = true;

        if (notificationList != null) {
            notificationList.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        if (lottieWave != null) {
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
        }

        if (noNotificationsText != null) {
            noNotificationsText.setVisibility(View.GONE);
        }

        loadNotifications(1);
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem notificationsItem = menu.findItem(R.id.action_notifications);
        if (notificationsItem != null) {
            notificationsItem.setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = view.findViewById(R.id.notificationRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setEnabled(true);
            loadNotifications(1);
        });
        adapter = new NotificationAdapter(notificationList, this);
        recyclerView.setAdapter(adapter);
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        ((HomePage) requireActivity()).selectTab(R.id.nav_home);
                    }
                }
        );


        lottieWave = view.findViewById(R.id.lottieWaveNot);
        noNotificationsText = view.findViewById(R.id.noNotificationsText);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        prefs.edit().putInt("unread_count", 0).apply();
        if (getActivity() instanceof HomePage) {
            ((HomePage) getActivity()).updateBadge(0);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int totalItemCount = layoutManager.getItemCount();
                int visibleItemCount = layoutManager.getChildCount();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && hasMoreData) {
                    // تحقق من وصول المستخدم لنهاية القائمة
                    if ((visibleItemCount + lastVisibleItemPosition) >= totalItemCount
                            && lastVisibleItemPosition >= 0
                            && totalItemCount >= pageSize) {
                        loadNotifications(nextPageToLoad);
                    }
                }
            }
        });
        DBHelper dbHelper = new DBHelper(getContext());

        // ابدأ تحميل الصفحة الأولى عند إنشاء الواجهة
        if (notificationList.isEmpty()) {
            loadNotifications(1);
//            recyclerView.setVisibility(View.VISIBLE);
        }
        UserUtils.updateProfile(getActivity(), new UserUtils.ProfileUpdateCallback() {
            @Override
            public void onProfileUpdated(boolean isVerified, boolean isActive) {
                if (!isActive) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
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
        noInternet = view.findViewById(R.id.noInternet);

        if (!UserUtils.isNetworkAvailable(requireContext())) {

            requireActivity().runOnUiThread(() -> {
                noInternet.setVisibility(View.VISIBLE);
                noNotificationsText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                lottieWave.cancelAnimation();
                lottieWave.setVisibility(View.GONE);
            });
            isLoading = false;
        }
        return view;
    }


    public void onNotificationClick(JSONObject notification) {
        Fragment fragment = null;
        Bundle args = new Bundle();

        String relatedObjectId = null;
        if (notification.has("related_object_id") && !notification.isNull("related_object_id")) {
            relatedObjectId = notification.optString("related_object_id", null);
            if (relatedObjectId != null) relatedObjectId = relatedObjectId.trim();
            if ("null".equalsIgnoreCase(relatedObjectId)) relatedObjectId = null;
        }

        String notificationType = null;
        if (notification.has("notification_type") && !notification.isNull("notification_type")) {
            notificationType = notification.optString("notification_type", null);
            if (notificationType != null) notificationType = notificationType.trim();
            if ("null".equalsIgnoreCase(notificationType)) notificationType = null;
        }

        if (notificationType != null && relatedObjectId != null && !relatedObjectId.isEmpty()) {
            args.putString("related_object_id", relatedObjectId);

            if ("trip_request".equalsIgnoreCase(notificationType)) {
                fragment = new TripDetailsFragment(); // صفحة تفاصيل الرحلة
                updateToolbar("تفاصيل الطلب", false, R.drawable.booking, 0);

            } else if ("booking".equalsIgnoreCase(notificationType)) {
                fragment = new BookingDetailsFragment(); // صفحة الحجز
                updateToolbar("تفاصيل الحجز", false, R.drawable.checklist, 0);
            } else if ("driver_request".equalsIgnoreCase(notificationType)) {
                fragment = new DriverTripRequest();
                updateToolbar("طلبات المسافرين", false, R.drawable.solo_traveller, 0);
            } else if ("TRAVELER_REQUESTS".equalsIgnoreCase(notificationType)) {
                fragment = new TravelerRequestsDetails();
                updateToolbar("تفاصيل الخدمة", false, R.drawable.solo_traveller, 0);
            } else if ("Sharing".equalsIgnoreCase(notificationType)) {
                fragment = new SharingFragment();
                updateToolbar("الأعضاء المنضمون", false, R.drawable.frame, 0);
            } else {
//                updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);

            }
        }

        if (fragment != null) {
            fragment.setArguments(args);

            // تأكد من أن الـ Activity هي HomePage لكي نتمكن من الوصول للدالة
            if (requireActivity() instanceof HomePage) {
                HomePage home = (HomePage) requireActivity();

                // جلب البيانات المطلوبة للتولبار بناءً على نوع الإشعار
                String title = "التفاصيل";
                int icon = R.drawable.notification_new;

                if ("trip_request".equalsIgnoreCase(notificationType)) {
                    title = "تفاصيل الطلب";
                    icon = R.drawable.booking;
                } else if ("booking".equalsIgnoreCase(notificationType)) {
                    title = "تفاصيل الحجز";
                    icon = R.drawable.checklist;
                } else if ("driver_request".equalsIgnoreCase(notificationType)) {
                    title = "طلبات المسافرين";
                    icon = R.drawable.solo_traveller;
                } else if ("TRAVELER_REQUESTS".equalsIgnoreCase(notificationType)) {
                    title = "تفاصيل الخدمة";
                    icon = R.drawable.solo_traveller;
                } else if ("Sharing".equalsIgnoreCase(notificationType)) {
                    title = "الأعضاء المنضمون";
                    icon = R.drawable.frame;
                }

                home.openFullScreenFragment(fragment, title, icon, 0);
            }
        } else {
            updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);
        }
    }

    private void updateToolbar(String title, boolean showBackArrow, int iconRes, int fragmentId) {
        HomePage activity = (HomePage) getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
        }
        activity.updateToolbar(title, showBackArrow, iconRes, fragmentId);

    }

    private void loadNotifications(int page) {
        if (!isAdded() || getActivity() == null || isLoading || !hasMoreData) {
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            return;
        }
        isLoading = true;

        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
//        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");
        if (page > 1) {
            requireActivity().runOnUiThread(() -> recyclerView.post(() -> adapter.addLoadingFooter()));
        } else {
            requireActivity().runOnUiThread(() -> {
                lottieWave.playAnimation();
                lottieWave.setVisibility(View.VISIBLE);
                noNotificationsText.setVisibility(View.GONE);
            });
        }
        DBHelper dbHelper = new DBHelper(getContext());
        if (!UserUtils.isNetworkAvailable(getContext())) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(getActivity(), message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
        new Thread(() -> {
            try {
                String urlStr = BASE_URL + "notifications/?page=" + page + "&token=" + token;
                URL url = new URL(urlStr);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

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

                            String title = obj.optString("title", "").trim();
                        } else {
                        }
                    }

                    boolean lastPage = jsonObject.isNull("next");
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                            if (page > 1) {
                                recyclerView.post(() -> adapter.removeLoadingFooter());
                            } else {
                                lottieWave.setVisibility(View.GONE);
                                lottieWave.cancelAnimation();
                                recyclerView.setVisibility(View.VISIBLE);
                            }

                            if (page == 1) {
                                notificationList.clear();
                            }

                            if (notifications.isEmpty() && adapter.getItemCount() == 0) {
                                noNotificationsText.setVisibility(View.VISIBLE);
                            } else {
                                noNotificationsText.setVisibility(View.GONE);
                                notificationList.addAll(notifications);
                                adapter.notifyDataSetChanged();
                                markAllRead();

                                currentPage = page;
                                nextPageToLoad = page + 1;
                                hasMoreData = !lastPage;
                            }

                            isLoading = false;
                        });
                    }
                } else {
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                            if (page > 1) recyclerView.post(() -> adapter.removeLoadingFooter());
                            else {
                                lottieWave.setVisibility(View.GONE);
                                lottieWave.cancelAnimation();
                            }
                            UserUtils.sendLog(getContext(), "loadNotifications", String.valueOf(responseCode), String.valueOf(responseCode), "Notification Fragment");
                            UserUtils.getMessageFromLocal(54, dbHelper, new UserUtils.MessageCallback() {
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

                conn.disconnect();
            } catch (Exception e) {
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                        if (page > 1) recyclerView.post(() -> adapter.removeLoadingFooter());
                        else {
                            lottieWave.setVisibility(View.GONE);
                            lottieWave.cancelAnimation();
                        }
                        UserUtils.sendLog(getContext(), "loadNotifications", e.toString(), e.toString(), "Notification Fragment");
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
            }
        }).start();
    }

    private void markAllRead() {
        if (isAdded()) {
            SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//            SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("auth_token", "");

            // تحديث محليًا أولاً
            for (JSONObject obj : notificationList) {
                try {
                    obj.put("is_read", true);  // تحديث حالة الإشعار محليًا
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

            // ثم إرسال الطلب للسيرفر
            new Thread(() -> {
                try {
                    URL url = new URL(BASE_URL + "notifications/mark_all_read/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    if (token != null) {
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                    }
                    // الوقت الحالي
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    String readDate = sdf.format(new Date());

                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("token", token);
                    jsonBody.put("read_date", readDate);
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                    os.close();

                    conn.getResponseCode(); // قراءة الكود فقط
                    conn.disconnect();
                } catch (Exception e) {
                    UserUtils.sendLog(getContext(), "markAllRead", e.toString(), e.toString(), "Notification Fragment");
                }
            }).start();
        }
    }

}
