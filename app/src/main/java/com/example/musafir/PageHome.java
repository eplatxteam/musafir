package com.example.musafir;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.example.musafir.LocationWorker.getCityNameFromIp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.Handler;
import android.os.Looper;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.request.target.CustomTarget;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.takusemba.spotlight.OnSpotlightListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.Target;
import com.takusemba.spotlight.shape.RoundedRectangle;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PageHome extends Fragment {

    public PageHome() {
        // Required empty public constructor
    }

    public interface OnServiceHomeFetchedListener {
        void onFetched(List<DBHelper.ServiceHome> types);

        void onError(String error);
    }

    String BASE_URL = UserUtils.BASE_URL;
    String ImageUrl = UserUtils.ImageUrl;

    ArrayList<String> cityNames = new ArrayList<>();
    ArrayList<Integer> cityIds = new ArrayList<>();

    private RecyclerView recyclerView2;


    private boolean isLoading = false;
    private boolean isTutorialRunning = false; // لمنع التكرار

    private LinearLayout dotsLayout;
    private int dotsCount;
    private ImageView[] dots;
    private int selectedCountryId = -1;
    GridLayout gridCardsReligious;
    //    TextView  name_Drivertrip;
//    ImageView icon_trip, icon1, icon2;
//    MaterialCardView ticket, Support, TripsRequest, trips, booking, TripPassenger, TripLocal, TripWorld, TripPrivate;
    NestedScrollView NestedScrollView2;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_home, container, false);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView2 = view.findViewById(R.id.horizontalRecyclerView);
        DBHelper dbHelper = new DBHelper(getContext());
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        AppBarLayout appBarLayout = view.findViewById(R.id.appBarLayout);
        gridCardsReligious = view.findViewById(R.id.gridCardsReligious);
        if (dbHelper.getAllServiceHome().isEmpty()) {
            refreshHomeData(swipeRefreshLayout, prefs, dbHelper, types -> {
                updateServiceCards();
            });
        } else {
            updateServiceCards();
        }

        SharedPreferences preferences = SharedPrefsHelper.get(getContext());
        int userId = preferences.getInt("user_id", -1);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                if (NestedScrollView2 != null && NestedScrollView2.canScrollVertically(-1)) {
                    return true;
                }
                if (recyclerView2 != null && recyclerView2.canScrollVertically(-1)) {
                    return true;
                }
                return false;
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                swipeRefreshLayout.setEnabled(verticalOffset == 0);
            }
        });
        recyclerView2.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            float startX = 0f;
            float startY = 0f;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv,
                                                 @NonNull MotionEvent e) {

                ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);

                switch (e.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        startX = e.getX();
                        startY = e.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(e.getX() - startX);
                        float dy = Math.abs(e.getY() - startY);

                        // إذا السحب أفقي
                        if (dx > dy) {
                            if (viewPager != null)
                                viewPager.setUserInputEnabled(false);

                            rv.getParent().requestDisallowInterceptTouchEvent(true);
                            swipeRefreshLayout.setEnabled(false);
                            return false; // خلّي الـ RecyclerView يستلم السحب
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (viewPager != null)
                            viewPager.setUserInputEnabled(true);

                        swipeRefreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });


        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (userId != -1) {

                swipeRefreshLayout.setEnabled(true);

                refreshHomeData(swipeRefreshLayout, prefs, dbHelper, new RefreshCallback() {
                    @Override
                    public void onDataUpdated(List<DBHelper.ServiceHome> types) {
                        updateServiceCards();
                        swipeRefreshLayout.setRefreshing(false);
                        UserUtils.getMessageFromLocal(61, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                if (isAdded() && getActivity() != null) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                    }
                });
                getCityNameFromIp(getContext(), (cityAr, cityId) -> {
                    prefs.edit().putString("default_city", cityAr).apply();
                    prefs.edit().putInt("default_city_id", cityId).apply();
                });
            } else {
                swipeRefreshLayout.setRefreshing(false);

            }
        });

        loadProfileData();
        scheduleLocationUpdate();


        getActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitConfirmationDialog();
                    }
                }
        );

        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                Fragment currentFragment = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.full_screen_container);

                if (currentFragment instanceof HomeFragment) {
                    Bundle args = currentFragment.getArguments();
                    String tripType = args != null ? args.getString("trip_type", "1") : "1";
                    switch (tripType) {
                        case "1":
                            updateToolbar("رحلات تشاركية", false, R.drawable.big_car, 0);
                            break;
                        case "2":
                            updateToolbar("نقل دولي", false, R.drawable.world_new, 0);
                            break;
                        case "3":
                            updateToolbar("رحلات محلية", false, R.drawable.bus_2, 0);
                            break;
                    }
                } else if (currentFragment instanceof BookingFragment) {
//                    BookingFragment bf = (BookingFragment) currentFragment;
//                    Bundle args = bf.getArguments();
//                    int tab = args != null ? args.getInt("tab_to_open", 0) : 0;
                    updateToolbar("رحلاتي", false, R.drawable.airplane_new, 1);
                } else if (currentFragment instanceof DriverTripRequest) {
                    updateToolbar("طلبات المسافرين", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof AddTripFragment) {
                    updateToolbar("إضافة رحلة", false, R.drawable.locations, 1);
                } else if (currentFragment instanceof NotificationFragment) {
                    updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);
                } else if (currentFragment instanceof AddTripRequests) {
                    updateToolbar("طلب رحلة", false, R.drawable.locations, 1);
                } else if (currentFragment instanceof TravelerRequests) {
                    updateToolbar("خدمات المسافرين", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof BookingDetailsFragment) {
                    updateToolbar("تفاصيل الحجز", false, R.drawable.checklist, 0);
                } else if (currentFragment instanceof VehicleFragment) {
                    updateToolbar("بيانات المركبات", false, R.drawable.local, 0);
                } else if (currentFragment instanceof SharingFragment) {
                    updateToolbar("الأعضاء المنضمون", false, R.drawable.frame_5__1_, 0);
                } else if (currentFragment instanceof BalanceFragment) {
                    updateToolbar("رصيدي", false, R.drawable.wallet, 0);
                } else if (currentFragment instanceof TripDetailsFragment) {
                    updateToolbar("تفاصيل الطلب", false, R.drawable.add_trip, 0);
                } else if (currentFragment instanceof AllTravelerRequests) {
                    updateToolbar("طلبات الخدمات", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof TravelerRequestsDetails) {
                    updateToolbar("تفاصيل الخدمة", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof AddTravelerRequests) {
                    Bundle args = currentFragment.getArguments();
                    String title = (args != null) ? args.getString("type_tr_name", "طلب خدمة") : "طلب خدمة";
//                    int typeId = (args != null) ? args.getInt("type_tr_id", 0) : 0;
//                    int icon = (typeId == 81) ? R.drawable.kaaba_new : R.drawable.solo_traveller;
                    int icon = (args != null) ? args.getInt("icon_tr", 0) : 0;
                    updateToolbar(title, false, icon, 0);
                } else if (currentFragment instanceof AllImagesFragment) {
                    updateToolbar("الإعلانات", false, R.drawable.ads, 0);
                } else if (currentFragment instanceof Advertisements) {
                    updateToolbar("تفاصيل الإعلان", false, R.drawable.ads, 0);
                } else if (currentFragment instanceof MoreDetails) {
                    updateToolbar("تفاصيل الرحلة", false, R.drawable.booking, 0);
                } else {
                    String full_name = prefs.getString("full_name", "");
                    Toolbar toolbar = requireActivity().findViewById(R.id.main_toolbar);
                    String fullNames = full_name.trim();
                    String firstName = "";
                    if (!fullNames.isEmpty()) {
                        String[] parts = fullNames.split("\\s+");
                        if (parts.length > 0) {
                            firstName = parts[0];
                            if (!firstName.isEmpty()) {
                                firstName = firstName.substring(0, 1).toUpperCase() +
                                        firstName.substring(1).toLowerCase();
                            }
                        }
                    }
                    for (int i = 0; i < toolbar.getChildCount(); i++) {
                        View child = toolbar.getChildAt(i);
                        if (child.getId() == R.id.textGreeting || (child.findViewById(R.id.textGreeting) != null)) {
                            toolbar.removeViewAt(i);
                            break;
                        }
                    }

                    View customView = getLayoutInflater().inflate(R.layout.toolbar_custom, null);
                    TextView textGreeting = customView.findViewById(R.id.textGreeting);
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    String greeting = (hour >= 5 && hour < 12) ? "صباح الخير" : "مساء الخير";
                    String fullText = greeting + " " + firstName;
                    textGreeting.setText(fullText);
                    toolbar.addView(customView);
                }
            }
        });


        dotsLayout = view.findViewById(R.id.dotsLayout);
        loadImages();
        UserUtils.fetchTravelerRequests(getContext(), dbHelper, userId, new TravelerRequests.fetchTravelerRequestsListener() {
            @Override
            public void onFetched(List<JSONObject> types) {

            }

            @Override
            public void onError(String error) {
                UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (isAdded() && getActivity() != null) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                    }

                    @Override
                    public void onError(String error) {
                    }

                });
            }
        });

        return view;
    }

    private void startTutorial() {
        SharedPreferences appPrefs = getContext().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        if (!appPrefs.getBoolean("isFirstRun_Tutorial_V2", true) || isTutorialRunning) return;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded() || getActivity() == null) return;

            Toolbar toolbar = getActivity().findViewById(R.id.main_toolbar);
            View aiChat = (toolbar != null) ? toolbar.findViewById(R.id.ai_chat) : null;
            View targetVip = getActivity().findViewById(R.id.card_vip);
            View targetWallet = getActivity().findViewById(R.id.card_wallet);
            View targetHelp = getActivity().findViewById(R.id.card_help);
            View cardTransport = getActivity().findViewById(R.id.cardTransport);

            List<Target> targets = new ArrayList<>();
            DBHelper dbHelper = new DBHelper(getContext());

            final int totalRequests = 5;
            final int[] completedRequests = {0};

            Runnable checkCompletion = () -> {
                completedRequests[0]++;
                if (completedRequests[0] == totalRequests) {
                    if (!targets.isEmpty() && isAdded()) {
                        startSpotlightNow(targets);
                    }
                }
            };

            if (cardTransport != null)
                UserUtils.getMessageFromLocal(281, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        targets.add(createTarget(cardTransport, "نقل دولي", message, 1));
                    }

                    @Override
                    public void onError(String error) {
                        targets.add(createTarget(cardTransport, "نقل دولي", "أبدأ من هنا لحجز رحلتك الدولية", 1));
                        checkCompletion.run();
                    }
                });
            if (targetVip != null)
                UserUtils.getMessageFromLocal(282, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        targets.add(createTarget(targetVip, "رحلات خاصة", message, 1));
                    }

                    @Override
                    public void onError(String error) {
                        targets.add(createTarget(targetVip, "رحلات خاصة", "إذا أردت رحلة خاصة اطلبها من هنا", 0));
                        checkCompletion.run();
                    }
                });
            if (targetWallet != null)
                UserUtils.getMessageFromLocal(283, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        targets.add(createTarget(targetWallet, "رصيدي", message, 1));
                    }

                    @Override
                    public void onError(String error) {
                        targets.add(createTarget(targetWallet, "رصيدي", "اطلع على رصيدك وحركاتك المالية من هنا", 0));
                        checkCompletion.run();
                    }
                });
            if (targetHelp != null)
                UserUtils.getMessageFromLocal(284, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        targets.add(createTarget(targetHelp, "تواصل معنا", message, 1));
                    }

                    @Override
                    public void onError(String error) {
                        targets.add(createTarget(targetHelp, "تواصل معنا", "إذا احتجت أي مساعدة فتواصل معنا من هنا", 0));
                        checkCompletion.run();
                    }
                });
            if (aiChat != null)
                UserUtils.getMessageFromLocal(285, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        targets.add(createTarget(aiChat, "مساعد مسافر", message, 1));
                    }

                    @Override
                    public void onError(String error) {
                        targets.add(createTarget(aiChat, "مساعد مسافر", "بإمكانك التواصل مع المساعد الذكي من هنا", 0));
                        checkCompletion.run();
                    }
                });
        }, 2000);
    }

    private void startSpotlightNow(List<Target> targets) {
        spotlight = new Spotlight.Builder(getActivity())
                .setTargets(targets.toArray(new Target[0]))
                .setBackgroundColor(Color.parseColor("#BF000000"))
                .setDuration(400L)
                .setOnSpotlightListener(new OnSpotlightListener() {
                    @Override
                    public void onStarted() {
                        isTutorialRunning = true;
                    }

                    @Override
                    public void onEnded() {
                        isTutorialRunning = false;
                        if (getContext() != null) {
                            getContext().getSharedPreferences("AppConfig", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("isFirstRun_Tutorial_V2", false)
                                    .apply();
                        }
                    }
                })
                .build();

        spotlight.start();
    }
//    private void startTutorial() {
//        SharedPreferences appPrefs = getContext().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
//
//        if (!appPrefs.getBoolean("isFirstRun_Tutorial_V2", true) || isTutorialRunning) return;
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            if (!isAdded() || getActivity() == null) return;
//
//            Toolbar toolbar = getActivity().findViewById(R.id.main_toolbar);
//            View aiChat = (toolbar != null) ? toolbar.findViewById(R.id.ai_chat) : null;
//            View targetVip = getActivity().findViewById(R.id.card_vip);
//            View targetWallet = getActivity().findViewById(R.id.card_wallet);
//            View targetHelp = getActivity().findViewById(R.id.card_help);
//            View cardTransport = getActivity().findViewById(R.id.cardTransport);
//            DBHelper dbHelper = new DBHelper(getContext());
//            List<Target> targets = new ArrayList<>();
//            if (cardTransport != null)
//                UserUtils.getMessageFromLocal(281, dbHelper, new UserUtils.MessageCallback() {
//                    @Override
//                    public void onSuccess(String message) {
//                        targets.add(createTarget(cardTransport, "نقل دولي", message, 1));
//                    }
//
//                    @Override
//                    public void onError(String error) {
//                        targets.add(createTarget(cardTransport, "نقل دولي", "أبدأ من هنا لحجز رحلتك الدولية", 1));
//
//                    }
//                });
//            if (targetVip != null)
//                UserUtils.getMessageFromLocal(282, dbHelper, new UserUtils.MessageCallback() {
//                    @Override
//                    public void onSuccess(String message) {
//                        targets.add(createTarget(targetVip, "رحلات خاصة", message, 1));
//                    }
//
//                    @Override
//                    public void onError(String error) {
//                        targets.add(createTarget(targetVip, "رحلات خاصة", "إذا أردت رحلة خاصة اطلبها من هنا", 0));
//
//                    }
//                });
//            if (targetWallet != null)
//                UserUtils.getMessageFromLocal(283, dbHelper, new UserUtils.MessageCallback() {
//                    @Override
//                    public void onSuccess(String message) {
//                        targets.add(createTarget(targetWallet, "رصيدي", message, 1));
//                    }
//
//                    @Override
//                    public void onError(String error) {
//                        targets.add(createTarget(targetWallet, "رصيدي", "اطلع على رصيدك وحركاتك المالية من هنا", 0));
//
//                    }
//                });
//            if (targetHelp != null)
//                UserUtils.getMessageFromLocal(284, dbHelper, new UserUtils.MessageCallback() {
//                    @Override
//                    public void onSuccess(String message) {
//                        targets.add(createTarget(targetHelp, "تواصل معنا", message, 1));
//                    }
//
//                    @Override
//                    public void onError(String error) {
//                        targets.add(createTarget(targetHelp, "تواصل معنا", "إذا احتجت أي مساعدة فتواصل معنا من هنا", 0));
//
//                    }
//                });
//            if (aiChat != null)
//                UserUtils.getMessageFromLocal(285, dbHelper, new UserUtils.MessageCallback() {
//                    @Override
//                    public void onSuccess(String message) {
//                        targets.add(createTarget(aiChat, "مساعد مسافر", message, 1));
//                    }
//
//                    @Override
//                    public void onError(String error) {
//                        targets.add(createTarget(aiChat, "مساعد مسافر", "بإمكانك التواصل مع المساعد الذكي من هنا", 0));
//
//                    }
//                });
//            if (targets.isEmpty()) return;
//            spotlight = new Spotlight.Builder(getActivity())
//                    .setTargets(targets.toArray(new Target[0]))
//                    .setBackgroundColor(Color.parseColor("#BF000000"))
//                    .setDuration(400L)
//                    .setOnSpotlightListener(new OnSpotlightListener() {
//                        @Override
//                        public void onStarted() {
//                            isTutorialRunning = true;
//                        }
//
//                        @Override
//                        public void onEnded() {
//                            isTutorialRunning = false;
//                            getContext().getSharedPreferences("AppConfig", Context.MODE_PRIVATE)
//                                    .edit()
//                                    .putBoolean("isFirstRun_Tutorial_V2", false)
//                                    .apply();
//                        }
//                    })
//                    .build();
//
//            spotlight.start();
//
//        }, 2000);
//    }

    private Spotlight spotlight;

    private Target createTarget(View view, String title, String description, int bottom_card) {
        View overlay = getLayoutInflater().inflate(R.layout.layout_tutorial_bus, null);

        ((TextView) overlay.findViewById(R.id.tvTutorialTitle)).setText(title);
        ((TextView) overlay.findViewById(R.id.tvTutorialDesc)).setText(description);

        View btnNext = overlay.findViewById(R.id.btnCloseTutorial);
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (spotlight != null) {
                    spotlight.next();
                }
            });
        }
//        if (bottom_card == 1) {
        view.post(() -> {
            int[] location = new int[2];
            view.getLocationInWindow(location);

            int viewBottom = location[1] + view.getHeight();
            View tooltipContainer = overlay.findViewById(R.id.tutorialContainer);

            if (tooltipContainer != null) {
                if (view.getId() == R.id.cardTransport) {
                    int offset = 40;
                    tooltipContainer.setTranslationY(viewBottom + offset);
                    tooltipContainer.animate().alpha(1f).setDuration(200).start();
                } else {
                    overlay.post(() -> {
                        int screenHeight = overlay.getHeight();
                        int containerHeight = tooltipContainer.getHeight();
                        float centerY = (screenHeight - containerHeight) / 2f;

                        tooltipContainer.setTranslationY(centerY);
                        tooltipContainer.animate().alpha(1f).setDuration(200).start();
                    });
                }
            }
        });
//        }

        return new Target.Builder()
                .setAnchor(view)
                .setShape(new RoundedRectangle(
                        view.getHeight() + 20,
                        view.getWidth() + 20,
                        25f))
                .setOverlay(overlay)
                .build();
    }

    public void updateServiceCards() {

        if (!isAdded() || getActivity() == null || gridCardsReligious == null) {
            return;
        }

        Context context = getContext();
        if (context == null) return;

        DBHelper dbHelper = new DBHelper(context);
        List<DBHelper.ServiceHome> types = dbHelper.getAllServiceHome();
        gridCardsReligious.removeAllViews();
        gridCardsReligious.setColumnCount(3);
        SharedPreferences preferences = SharedPrefsHelper.get(getContext());

//        SharedPreferences preferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("user_id", -1);
        String user_type = preferences.getString("user_type", "");

        for (DBHelper.ServiceHome type : types) {
            boolean isActive = (type.inactive != 0);

            MaterialCardView cardView = new MaterialCardView(context);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 102, getResources().getDisplayMetrics());
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            cardView.setLayoutParams(params);
            cardView.setRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
            cardView.setCardElevation(0);
            ImageView imageView = new ImageView(context);

//            if (type.type_tr_id == 2) {
//                // 1. تطبيق التصميم المميز (الحافة الصفراء والخلفية الفاتحة) كما في الصورة
//                cardView.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FFC107"))); // لون أصفر/ذهبي
//                cardView.setStrokeWidth(4); // عرض الحافة
//                cardView.setCardBackgroundColor(Color.parseColor("#FFFDE7")); // خلفية صفراء فاتحة جداً
//
//                // 2. تعيين ID برمجي للكارد لكي تستهدفه مكتبة Spotlight لاحقاً
//                cardView.setId(R.id.cardTransport); // يجب تعريف هذا الـ ID في ids.xml (انظر الخطوة 3)
//            } else {
            // التصميم الافتراضي لبقية الكاردات
            cardView.setStrokeWidth(0);
            cardView.setCardBackgroundColor(Color.WHITE);
//            }
//            cardView.setCardBackgroundColor(ContextCompat.getColorStateList(context, R.color.card_bg_selector));

            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

            LinearLayout contentLayout = new LinearLayout(context);
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            contentLayout.setGravity(Gravity.CENTER);
            contentLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

            int imgWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
            int imgHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(imgWidth, imgHeight));

            TextView textView = new TextView(context);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            tvParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            textView.setLayoutParams(tvParams);
            switch (type.type_tr_id) {
                case 6:
                    if (user_type.equals("driver")) {
                        textView.setText("طلبات المسافرين");
                        imageView.setImageResource(R.drawable.solo_traveller);
                    } else {
                        textView.setText("خدمات المسافرين");
                        imageView.setImageResource(R.drawable.solo_traveller);
                    }
                    break;

                case 5:
                    if (user_type.equals("driver")) {
                        textView.setText("بيانات المركبات");
                        imageView.setImageResource(R.drawable.local);
                    } else {
                        textView.setText("تذاكر الطيران");
                        imageView.setImageResource(R.drawable.travel_diary);
                    }
                    break;

                case 4:
                    if (user_type.equals("driver")) {
                        textView.setText("طلبات الرحلات");
                        imageView.setImageResource(R.drawable.airplanes);
                    } else {
                        textView.setText("تأشيرات الحج والعمرة");
                        imageView.setImageResource(R.drawable.kaaba_new);
                    }
                    break;

                case 8:
                    if (user_type.equals("driver")) {
                        textView.setText("إضافة رحلة");
//                        ((HomePage) requireActivity()).selectTab(R.id.fab);
                        imageView.setImageResource(R.drawable.locations);
                    } else {
                        textView.setText("طلب رحلة");
//                        ((HomePage) requireActivity()).selectTab(R.id.fab);
                        imageView.setImageResource(R.drawable.locations);
                    }
                    break;

                default:
                    int resId = getResources().getIdentifier(type.type_icon, "drawable", requireContext().getPackageName());
                    imageView.setImageResource(resId != 0 ? resId : R.drawable.msafer_empty1);
                    textView.setText(type.type_tr_name);
                    break;
            }
            textView.setTextSize(12);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(ContextCompat.getColorStateList(context, R.color.text_color_selector));

            contentLayout.addView(imageView);
            contentLayout.addView(textView);
            frameLayout.addView(contentLayout);

            if (!isActive) {
                cardView.setCardBackgroundColor(Color.WHITE);
                contentLayout.setAlpha(0.3f);

                LinearLayout blurOverlay = new LinearLayout(context);
                blurOverlay.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                blurOverlay.setBackgroundColor(ContextCompat.getColor(context, R.color.gray3));
                blurOverlay.setAlpha(0.6f);

                frameLayout.addView(blurOverlay);

                TextView tvSoon = new TextView(context);

                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setColor(ContextCompat.getColorStateList(context, R.color.primary));
                shape.setCornerRadius(50f);

                tvSoon.setBackground(shape);
                int paddingVertical = 8;
                int paddingHorizontal = 30;
                tvSoon.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

                tvSoon.setText("قريباً");
                tvSoon.setTextSize(14);
                tvSoon.setTextColor(Color.WHITE);
                tvSoon.setTypeface(null, Typeface.BOLD);
                tvSoon.setGravity(Gravity.CENTER);

                FrameLayout.LayoutParams soonParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                soonParams.gravity = Gravity.CENTER;
                tvSoon.setLayoutParams(soonParams);
                frameLayout.addView(tvSoon);
                cardView.setClickable(false);
                cardView.setFocusable(false);
            }
//            if (type.type_tr_id == 2) { // فقط لـ "نقل دولي"
//                TextView tvBadge = new TextView(context);
//                tvBadge.setText("الأكثر استخداماً 🔥");
//                tvBadge.setTextSize(10);
//                tvBadge.setTextColor(Color.WHITE);
//                tvBadge.setTypeface(null, Typeface.BOLD);
//                tvBadge.setGravity(Gravity.CENTER);
//
//                // خلفية برتقالية دائرية للحافة
//                GradientDrawable badgeShape = new GradientDrawable();
//                badgeShape.setShape(GradientDrawable.RECTANGLE);
//                badgeShape.setColor(Color.parseColor("#FF9800")); // برتقالي
//                badgeShape.setCornerRadii(new float[]{0f, 0f, 20f, 20f, 0f, 0f, 0f, 0f}); // زوايا دائرية سفلية فقط
//                tvBadge.setBackground(badgeShape);
//
//                int paddingH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
//                int paddingV = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
//                tvBadge.setPadding(paddingH, paddingV, paddingH, paddingV);
//
//                // وضع الشارة في أعلى المنتصف
//                FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
//                        FrameLayout.LayoutParams.WRAP_CONTENT,
//                        FrameLayout.LayoutParams.WRAP_CONTENT
//                );
//                badgeParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
//                tvBadge.setLayoutParams(badgeParams);
//
//                // إضافتها للـ frameLayout (الذي يحتوي أصلاً على المحتوى Blur أو Soon)
//                frameLayout.addView(tvBadge);
//            }
            if (type.type_tr_id == 2) {
                cardView.setId(R.id.cardTransport);
            } else if (type.type_tr_id == 8) {
                cardView.setId(R.id.card_vip); // كارد الرحلة الخاصة
            } else if (type.type_tr_id == 7) {
                cardView.setId(R.id.card_wallet); // كارد الرصيد
            } else if (type.type_tr_id == 9) {
                cardView.setId(R.id.card_help); // كارد المساعدة
            }
            if (type.type_tr_id == 2) {
                TextView tvBadge = new TextView(context);
                tvBadge.setText("الأكثر استخداماً 🔥");
                tvBadge.setTextSize(7.5f);
                tvBadge.setTextColor(Color.WHITE);
                tvBadge.setTypeface(null, Typeface.BOLD);
                tvBadge.setGravity(Gravity.CENTER);

                // تصميم الشارة ككبسولة صغيرة جداً
                GradientDrawable badgeShape = new GradientDrawable();
                badgeShape.setShape(GradientDrawable.RECTANGLE);
                badgeShape.setColor(Color.parseColor("#FF9800"));
                badgeShape.setCornerRadius(15f);
                tvBadge.setBackground(badgeShape);

                int paddingH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                int paddingV = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
                tvBadge.setPadding(paddingH, paddingV, paddingH, paddingV);

                FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

                badgeParams.gravity = Gravity.TOP | Gravity.CENTER;
//                badgeParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
//                badgeParams.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()));

                cardView.setClipChildren(true);
                frameLayout.setClipChildren(true);

                tvBadge.setLayoutParams(badgeParams);
                tvBadge.setZ(20f);

                frameLayout.addView(tvBadge);
                tvBadge.bringToFront();

                cardView.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FFB300")));
                cardView.setStrokeWidth(3);
            }
            cardView.addView(frameLayout);

//            imageView.setImageTintList(null);
//            imageView.setImageTintList(ContextCompat.getColorStateList(context, R.color.icon_tint_selector));

//            imageView.invalidate();
            cardView.setOnClickListener(v -> {

                if (!isActive) return;
//
//                int primaryColor = ContextCompat.getColor(context, R.color.primary);
//
//                imageView.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                int primaryColor = ContextCompat.getColor(context, R.color.primary);

                imageView.setImageTintList(ColorStateList.valueOf(primaryColor));
                v.postDelayed(() -> {
                    imageView.setImageTintList(null);
                }, 150);
                String title = type.type_tr_name;
                int icon = R.drawable.msafer_empty1;
                Fragment fragment = null;
                int fragmentId = 0;
                switch (type.type_tr_id) {
                    case 1:
                    case 2:
                    case 3:
                        openHomeFragment(String.valueOf(type.type_tr_id));
                        return;

                    case 4:
                        if (userId == -1) {
                            handleUnauthenticated();
                            return;
                        }
                        if (user_type.equals("driver")) {
                            ((HomePage) requireActivity()).selectTab(R.id.nav_reservation);
                            openBookingFragment(1, "رحلاتي");
                            UserUtils.app_Page(context, 2);
                            return;
                        } else {
                            fragment = new AddTravelerRequests();
                            Bundle b = new Bundle();
                            b.putInt("type_tr_id", 81);
                            b.putString("type_tr_name", "تأشيرات الحج والعمرة");
                            b.putInt("icon_tr", R.drawable.kaaba_new);
                            fragment.setArguments(b);
                            icon = R.drawable.kaaba_new;
                            UserUtils.app_Page(context, 81);
                        }
                        break;

                    case 5:
                        if (userId == -1) {
                            handleUnauthenticated();
                            return;
                        }
                        if (user_type.equals("driver")) {
                            fragment = new VehicleFragment();
                            title = "بيانات المركبات";
                            UserUtils.app_Page(context, 5);
                            icon = R.drawable.local;

                        } else {
                            fragment = new AddTravelerRequests();
                            Bundle b = new Bundle();
                            b.putInt("type_tr_id", 83);
                            b.putString("type_tr_name", "تذاكر الطيران");
                            fragment.setArguments(b);
                            b.putInt("icon_tr", R.drawable.travel_diary);

                            icon = R.drawable.travel_diary;
                            UserUtils.app_Page(context, 83);
                        }
                        break;
                    case 6:
                        if (userId == -1) {
                            handleUnauthenticated();
                            return;
                        }
                        if (user_type.equals("driver")) {
                            fragment = new DriverTripRequest();
                            title = "طلبات المسافرين";
                            UserUtils.app_Page(context, 6);
                        } else {
                            fragment = new TravelerRequests();
                            title = "خدمات المسافرين";
                            UserUtils.app_Page(context, 41);
                        }
                        break;

                    case 7:
                        if (userId == -1) {
                            handleUnauthenticated();
                            return;
                        }
                        fragment = new BalanceFragment();
                        title = "رصيدي";
                        icon = R.drawable.wallet;
                        UserUtils.app_Page(context, 121);
                        break;
                    case 8:
                        if (userId == -1) {
                            handleUnauthenticated();
                            return;
                        }

                        if (user_type.equals("driver")) {
                            fragment = new AddTripFragment();
                            title = "إضافة رحلة";
                            fragmentId = 1;
                            icon = R.drawable.locations;
                            ((HomePage) requireActivity()).selectTab(R.id.fab);

                            UserUtils.app_Page(context, 6);
                        } else {
                            fragment = new AddTripRequests();
                            title = "طلب رحلة";
                            fragmentId = 1;
                            icon = R.drawable.locations;
                            ((HomePage) requireActivity()).selectTab(R.id.fab);

                            UserUtils.app_Page(context, 11);
                        }
                        break;

                    case 9:
                        UserUtils.app_Page(context, 46);
                        openWhatsApp("967785050270");
                        return;

                    default:
                        if (userId == -1) {
                            handleUnauthenticated();
                            return;
                        }
                        break;
                }

                if (fragment != null) {
                    openFullScreenFragment(fragment, title, icon, fragmentId);

                }
            });
            gridCardsReligious.addView(cardView);
        }
        startTutorial();
    }


    private void openFullScreenFragment(Fragment fragment, String title, int iconRes, int fragmentId) {
        if (getActivity() != null && getActivity() instanceof HomePage) {
            HomePage home = (HomePage) getActivity();

            View viewPager = home.findViewById(R.id.viewPager);
            View fullContainer = home.findViewById(R.id.full_screen_container);

            // 1. إظهار الحاوية فوراً
            if (fullContainer != null) {
                fullContainer.setVisibility(View.VISIBLE);
                // نصيحة: تأكد في ملف XML أن full_screen_container له خلفية بيضاء android:background="#FFFFFF"
            }

            // 2. إخفاء الـ ViewPager فوراً بدون استخدام post
            if (viewPager != null) {
                viewPager.setVisibility(View.GONE);
            }

            // 3. تنفيذ الانتقال
            home.getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN) // إضافة حركة انتقال بسيطة لتنعيم الظهور
                    .replace(R.id.full_screen_container, fragment)
                    .addToBackStack(null)
                    .commit();

            home.updateToolbar(title, false, iconRes, fragmentId);
        }
    }


    private void openHomeFragment(String tripType) {
        Fragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("trip_type", tripType);
        fragment.setArguments(bundle);

        String title = "";
        int icon = R.drawable.local;
        int pageId = 0;

        switch (tripType) {
            case "1":
                title = "رحلات تشاركية";
                pageId = 43;
                icon = R.drawable.big_car;
                break;
            case "2":
                title = "نقل دولي";
                pageId = 44;
                icon = R.drawable.world_new;
                break;
            case "3":
                title = "رحلات محلية";
                pageId = 45;
                icon = R.drawable.bus_2;
                break;
        }

        UserUtils.app_Page(getContext(), pageId);
        openFullScreenFragment(fragment, title, icon, 0);
    }

    private void openBookingFragment(int tabIndex, String title) {
        if (getActivity() != null && getActivity() instanceof HomePage) {
            HomePage home = (HomePage) getActivity();

            home.findViewById(R.id.viewPager).setVisibility(View.GONE);
            home.findViewById(R.id.full_screen_container).setVisibility(View.VISIBLE);

            BookingFragment bookingFragment = new BookingFragment();
            Bundle args = new Bundle();
            args.putInt("tab_to_open", tabIndex);

            bookingFragment.setArguments(args);

            home.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.full_screen_container, bookingFragment)
//                    .addToBackStack(null)
                    .commitNowAllowingStateLoss();

            home.updateToolbar(title, false, R.drawable.airplane_new, 1);
        }
    }


    private void handleUnauthenticated() {
        DBHelper dbHelper = new DBHelper(getContext());

        UserUtils.getMessageFromLocal(39, dbHelper, new UserUtils.MessageCallback() {
            @Override
            public void onSuccess(String message) {
                UserUtils.ToastMessages(getActivity(), message);
            }

            @Override
            public void onError(String error) {
            }
        });
        startActivity(new Intent(getContext(), MainActivity.class));
    }

    private void openWhatsApp(String phoneNumber) {
        String url = "https://wa.me/" + phoneNumber;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.whatsapp");
        try {
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    private void refreshHomeData(SwipeRefreshLayout swipeRefreshLayout, SharedPreferences prefs, DBHelper dbHelper, RefreshCallback callback) {
        Context context = getContext();
        if (context == null || !isAdded()) {
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        UserUtils.checkAppUpdate(context);

        UserUtils.fetchServiceHome(context, dbHelper, new OnServiceHomeFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.ServiceHome> types) {
                if (isAdded() && getActivity() != null) {
                    updateServiceCards();
                    if (callback != null) callback.onDataUpdated(types);
                }
            }

            @Override
            public void onError(String error) {
            }
        });

        UserUtils.fetchTypeTravelerRequests(context, dbHelper, new TravelerRequests.OnTypeRequestsFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.TypeTravelerRequest> types) {
            }

            @Override
            public void onError(String error) {
                if (isAdded()) handleError(dbHelper);
            }
        });

        UserUtils.fetchAndSaveMessages(context, new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
                if (isAdded() && prefs != null) {
                    prefs.edit().putBoolean("messages_fetched", true).apply();
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) handleError(dbHelper);
            }
        });
        UserUtils.fetchCashBankData(getContext(), dbHelper, new UserUtils.OnCashBankFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.CashBank> types) {
            }

            @Override
            public void onError(String error) {
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
        });

        UserUtils.fetchAndSaveCountry(context, new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });

        UserUtils.fetchRoutes(context, new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });

        UserUtils.loadVehicleTypesToDB(context);
        UserUtils.fetchCompany(context, new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {
                if (isAdded() && getActivity() != null) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            if (getActivity() != null)
                                UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
            }
        });

        UserUtils.fetchCodeDetails(context, 5, null, new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {
                if (isAdded() && getActivity() != null) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            if (getActivity() != null)
                                UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
            }
        });

        UserUtils.fetchAndSavecities(context, new UserUtils.citiesCallback() {
            @Override
            public void onSuccess(String message) {
                if (isAdded() && prefs != null) {
                    prefs.edit().putBoolean("cities_fetched", true).apply();
                }
            }

            @Override
            public void onError(String error) {
            }
        });

        scheduleLocationUpdate();
        loadImages();
        fetchCities();

        // 3. التحقق من swipeRefreshLayout قبل استخدام postDelayed
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.postDelayed(() -> {
                if (isAdded()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 2000);
        }
    }


    private void updateToolbar(String title, boolean showBackArrow, int iconRes, int fragmentId) {
        HomePage activity = (HomePage) getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
        }
        activity.updateToolbar(title, showBackArrow, iconRes, fragmentId);

    }


    private void showExitConfirmationDialog() {
        if (!isAdded() || getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) {
            return;
        }
        new ExitConfirmationDialog().show(getParentFragmentManager(), "exit_dialog");
    }

    public interface RefreshCallback {
        void onDataUpdated(List<DBHelper.ServiceHome> types);
    }


    private void handleError(DBHelper dbHelper) {
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

    private void loadProfileData() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");
        DBHelper dbHelper = new DBHelper(getContext());

        if (token.isEmpty()) {
//            UserUtils.getMessageFromLocal(41, dbHelper, new UserUtils.MessageCallback() {
//                @Override
//                public void onSuccess(String message) {
//                    UserUtils.ToastMessages(getActivity(), message);
//                }
//
//                @Override
//                public void onError(String error) {
//                }
//
//            });
            return;
        }

        new Thread(() -> {
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "auth/profile/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                JSONObject body = new JSONObject();
                body.put("token", token);
                conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
                conn.connect();

                int responseCode = conn.getResponseCode();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream()));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                if (responseCode == 200) {
                    JSONObject json = new JSONObject(result.toString());

                    // ✅ تخزين البيانات محليًا
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("user_type", json.optString("user_type", ""));
                    editor.putString("full_name", json.optString("full_name", ""));
                    editor.putString("phone_number", json.optString("phone_number", ""));
                    editor.putString("national_id", json.optString("national_id", ""));
                    editor.putString("date_of_birth", json.optString("date_of_birth", "").equals("null") ? "" : json.optString("date_of_birth", ""));
                    editor.putString("gender", json.optString("gender", ""));
                    editor.putString("address", json.optString("address", ""));
                    editor.putString("driver_license", json.optString("driver_license", ""));
                    editor.putString("license_expire_date", json.optString("license_expire_date", "").equals("null") ? "" : json.optString("date_of_birth", ""));
                    editor.putString("license_image", json.optString("license_image", "").equals("null") ? "" : json.optString("license_image", ""));
                    editor.putString("national_id_image", json.optString("national_id_image", "").equals("null") ? "" : json.optString("national_id_image", ""));
                    editor.putString("passport_image", json.optString("passport_image", "").equals("null") ? "" : json.optString("passport_image", ""));
                    editor.putString("visit_document", json.optString("visit_document", "").equals("null") ? "" : json.optString("visit_document", ""));
                    editor.apply();
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
//                            loadCachedProfileData();
                        });
                    }
                } else {
                    String errorMsg = result.toString();
                    try {
                        JSONObject errorJson = new JSONObject(errorMsg);
                        if (errorJson.has("message"))
                            errorMsg = errorJson.getString("message");
                        else if (errorJson.has("detail"))
                            errorMsg = errorJson.getString("detail");
                    } catch (Exception e) {
                        UserUtils.sendLog(getContext(), "loadProfileData", e.toString(), e.toString(), "PageHome");
                    }

                    final String displayMsg = errorMsg.length() > 100 ? errorMsg.substring(0, 100) + "..." : errorMsg;
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }

                            });
                            UserUtils.sendLog(getContext(), "loadProfileData", displayMsg, displayMsg, "PageHome");
                        });
                    }
                }
            } catch (Exception e) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.sendLog(getContext(), "loadProfileData", e.toString(), e.toString(), "PageHome");
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
                }
            }
        }).start();
    }

    private void scheduleLocationUpdate() {
        PeriodicWorkRequest locationWorkRequest =
                new PeriodicWorkRequest.Builder(LocationWorker.class, 2, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(getContext()).enqueueUniquePeriodicWork(
                "updateCity",
                ExistingPeriodicWorkPolicy.REPLACE,
                locationWorkRequest
        );
    }

    private void fetchCities() {
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            try {
                List<DBHelper.Country> countries = dbHelper.getAllCountries();
                List<DBHelper.City> allCities = dbHelper.getAllCities();
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                int defaultCityId = prefs.getInt("default_city_id", -1);

                if (defaultCityId != -1) {
                    for (DBHelper.City c : allCities) {
                        if (c.id == defaultCityId) {
                            selectedCountryId = c.country_id;
                            break;
                        }
                    }
                }

                if (selectedCountryId == -1 && !countries.isEmpty()) {
                    selectedCountryId = countries.get(0).id;
                }

                List<DBHelper.City> filteredCities = dbHelper.getCitiesByCountry(selectedCountryId);
                cityNames.clear();
                cityIds.clear();
                for (DBHelper.City city : filteredCities) {
                    cityNames.add(city.name);
                    cityIds.add(city.id);
                }


            } catch (Exception e) {
            }
        }).start();
    }


    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dot_inactive));
        }
        if (position < dotsCount) {
            dots[position].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dot_active));
        }
    }

    private void setupDotsIndicator(Context context, int size) {
        if (context == null) return;

        dotsCount = size;
        dots = new ImageView[dotsCount];
        if (context == null || dotsLayout == null) return;
        dotsLayout.removeAllViews();

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(context);
            dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dot_inactive));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);

            dotsLayout.addView(dots[i], params);
        }

        if (dotsCount > 0) {
            dots[0].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dot_active));
        }
    }

    private Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private int currentPosition = 0;
    private Runnable autoScrollRunnable;

    private void startAutoScroll(RecyclerView recyclerView) {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (recyclerView.getAdapter() != null) {
                    int count = recyclerView.getAdapter().getItemCount();
                    if (count > 0) {
                        currentPosition = (currentPosition + 1) % count;
                        recyclerView.smoothScrollToPosition(currentPosition);

                        autoScrollHandler.postDelayed(this, 3000);
                    }
                }
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    private final PagerSnapHelper snapHelper = new PagerSnapHelper();

    private void loadImages() {
        if (!isAdded()) return;
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

        // --- إضافة الـ SnapHelper هنا ---
        if (recyclerView2.getOnFlingListener() == null) {
            snapHelper.attachToRecyclerView(recyclerView2);
        }
        // --------------------------------

        String savedUrls = prefs.getString("home_image_urls", null);
        if (savedUrls != null) {
            List<String> urls = new ArrayList<>(Arrays.asList(savedUrls.split(",")));
            ImageAdapter adapter = new ImageAdapter(getActivity(), urls, 1);
            recyclerView2.setAdapter(adapter);

            setupDotsIndicator(getContext(), Math.min(urls.size(), 6));
            recyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null) {
                        // للحصول على العنصر الموجود في المركز بدقة
                        View centerView = snapHelper.findSnapView(lm);
                        if (centerView != null) {
                            int position = lm.getPosition(centerView);
                            updateDotsIndicator(position);
                        }
                    }
                }
            });

        }
        int socketTimeout = 10000; // 10 ثواني
        RetryPolicy policy = new DefaultRetryPolicy(
                socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        String url = BASE_URL + "ImagesHome/?in_home_page=1";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<String> imageUrls = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String imgId = obj.getString("img_id");
                            String imgName = obj.getString("img_name");

                            if (imgName != null && !imgName.isEmpty() && !"null".equals(imgName)) {
                                String fullUrl = ImageUrl + "/media/" + imgName;
                                imageUrls.add(imgId + "|" + fullUrl);
                            }
                        } catch (JSONException e) {
                            UserUtils.sendLog(getContext(), "loadImages", e.toString(), e.toString(), "PageHome");
                        }
                    }

                    String joinedUrls = String.join(",", imageUrls);
                    prefs.edit().putString("home_image_urls", joinedUrls).apply();

                    ImageAdapter adapter = new ImageAdapter(getActivity(), imageUrls, 1);
                    recyclerView2.setAdapter(adapter);
                    if (!isAdded()) return;
                    Context context = getContext();
                    if (context != null) {
                        setupDotsIndicator(context, Math.min(imageUrls.size(), 6));
                    }

                    recyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                            if (lm != null) {
                                int position = lm.findFirstVisibleItemPosition();
                                updateDotsIndicator(position);
                            }
                        }
                    });

//                    recyclerView.smoothScrollToPosition(0);
                },
                error -> {
                    UserUtils.sendLog(getContext(), "loadImages", error.toString(), error.toString(), "PageHome");
                }
        );
        request.setRetryPolicy(policy);
        Volley.newRequestQueue(getActivity()).add(request);
        startAutoScroll(recyclerView2);

    }


}