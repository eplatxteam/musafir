package com.example.musafir;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import static com.example.musafir.LocationWorker.getCityNameFromIp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TravelerRequests extends Fragment {


    String BASE_URL = UserUtils.BASE_URL;
    String ImageUrl = UserUtils.ImageUrl;
    RecyclerView recyclerView2, recyclerView;
    private LinearLayout dotsLayout;
    private int dotsCount;
    private ImageView[] dots;
    NestedScrollView NestedScrollView2;
    LinearLayout allRequest, noDataTextTrip;
    GridLayout gridCardsReligious;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_traveler_requests, container, false);

        gridCardsReligious = view.findViewById(R.id.gridCardsReligious);
        recyclerView2 = view.findViewById(R.id.horizontalRecyclerView);
        noDataTextTrip = view.findViewById(R.id.noDataTextTrip);
        recyclerView = view.findViewById(R.id.recyclerViewTrv);
        setHasOptionsMenu(true);
        DBHelper dbHelper = new DBHelper(getContext());
        dotsLayout = view.findViewById(R.id.dotsLayout);
        allRequest = view.findViewById(R.id.allRequest);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        NestedScrollView2 = view.findViewById(R.id.NestedScrollViewReq);

        recyclerView2.setOnTouchListener((v, event) -> {
            NestedScrollView2.requestDisallowInterceptTouchEvent(true);

            if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                NestedScrollView2.requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
        SharedPreferences preferences = SharedPrefsHelper.get(getContext());

//        SharedPreferences preferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        updateServiceCards();

        allRequest.setOnClickListener(v -> {
            Fragment fragment = new AllTravelerRequests();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.full_screen_container, fragment)
                    .addToBackStack(null)
                    .commit();

            ((HomePage) requireActivity()).updateToolbar("طلبات الخدمات", false, R.drawable.solo_traveller, 0);
        });

        UserUtils.fetchTravelerRequests(getContext(), dbHelper, getPassengerId(), new fetchTravelerRequestsListener() {
            @Override
            public void onFetched(List<JSONObject> types) {
                if (types.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noDataTextTrip.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noDataTextTrip.setVisibility(View.GONE);

                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    TravelerRequestsAdapter adapter = new TravelerRequestsAdapter(getContext(), types);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String error) {
                // في حالة فشل الاتصال
                recyclerView.setVisibility(View.GONE);
                noDataTextTrip.setVisibility(View.GONE);
            }
        });

        loadImages();


        List<JSONObject> localList = dbHelper.getLatestRequests();

        if (localList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noDataTextTrip.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noDataTextTrip.setVisibility(View.GONE);

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            TravelerRequestsAdapter adapter = new TravelerRequestsAdapter(getContext(), localList);
            recyclerView.setAdapter(adapter);
        }


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
        recyclerView2.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    swipeRefreshLayout.setEnabled(false);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    swipeRefreshLayout.setEnabled(true);
                    break;
            }
            // نعيد false حتى يستمر الـ RecyclerView باستقبال اللمسات (التمرير)
            return false;
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setEnabled(true);

            refreshHomeData(swipeRefreshLayout, preferences, dbHelper, new RefreshCallback() {
                @Override
                public void onDataUpdated(List<DBHelper.TypeTravelerRequest> types) {
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
            getCityNameFromIp(getContext(), (cityAr, cityId, country_code) -> {
                preferences.edit().putString("default_city", cityAr).apply();
                preferences.edit().putInt("default_city_id", cityId).apply();
                preferences.edit().putString("country_code", country_code).apply();
            });
        });

//        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
//            FragmentActivity activity = getActivity();
//            if (activity != null) {
//
//                Fragment currentFragment = activity.getSupportFragmentManager()
//                        .findFragmentById(R.id.full_screen_container);
//
//                if (currentFragment instanceof HomeFragment) {
//                    Bundle args = currentFragment.getArguments();
//                    String tripType = args != null ? args.getString("trip_type", "1") : "1";
//                    switch (tripType) {
//                        case "1":
//                            updateToolbar("رحلات تشاركية", false, R.drawable.big_car, 0);
//                            break;
//                        case "2":
//                            updateToolbar("نقل دولي", false, R.drawable.world_new, 0);
//                            break;
//                        case "3":
//                            updateToolbar("رحلات محلية", false, R.drawable.bus_2, 0);
//                            break;
//                    }
//                } else if (currentFragment instanceof BookingFragment) {
//                    updateToolbar("رحلاتي", false, R.drawable.airplane_t, 1);
//                } else if (currentFragment instanceof DriverTripRequest) {
//                    updateToolbar("طلبات المسافرين", false, R.drawable.solo_traveller, 0);
//                } else if (currentFragment instanceof AddTripFragment) {
//                    updateToolbar("إضافة رحلة", false, R.drawable.locations, 0);
//                } else if (currentFragment instanceof NotificationFragment) {
//                    updateToolbar("الإشعارات", false, R.drawable.notification_new, 1);
//                } else if (currentFragment instanceof AddTripRequests) {
//                    updateToolbar("طلب رحلة خاصة", false, R.drawable.locations, 0);
//                } else if (currentFragment instanceof TravelerRequests) {
//                    updateToolbar("خدمات المسافرين", false, R.drawable.solo_traveller, 0);
//                } else if (currentFragment instanceof BookingDetailsFragment) {
//                    updateToolbar("تفاصيل الحجز", false, R.drawable.checklist, 0);
//                } else if (currentFragment instanceof CustomBooking) {
//                    updateToolbar("حجز رحلة", false, R.drawable.booking, 0);
//                } else if (currentFragment instanceof VehicleFragment) {
//                    updateToolbar("بيانات المركبات", false, R.drawable.local, 0);
//                } else if (currentFragment instanceof AddVehicleFragment) {
//                    Bundle args = currentFragment.getArguments();
//                    String title = (args != null && args.containsKey("vehicle_id")) ? "تعديل المركبة" : "إضافة مركبة";
//                    updateToolbar(title, false, R.drawable.local, 0);
//                } else if (currentFragment instanceof SharingFragment) {
//                    updateToolbar("الأعضاء المنضمون", false, R.drawable.frame, 0);
//                } else if (currentFragment instanceof BalanceFragment) {
//                    updateToolbar("رصيدي", false, R.drawable.wallet, 0);
//                } else if (currentFragment instanceof TripDetailsFragment) {
//                    updateToolbar("تفاصيل الطلب", false, R.drawable.add_trip, 0);
//                } else if (currentFragment instanceof AllTravelerRequests) {
//                    updateToolbar("طلبات الخدمات", false, R.drawable.solo_traveller, 0);
//                } else if (currentFragment instanceof ProfileFragment) {
//                    updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 0);
//                } else if (currentFragment instanceof SettingFragment) {
//                    updateToolbar("الملف الشخصي", false, R.drawable.profile_new, 0);
//                } else if (currentFragment instanceof TravelerRequestsDetails) {
//                    updateToolbar("تفاصيل الخدمة", false, R.drawable.solo_traveller, 0);
//                } else if (currentFragment instanceof AddTravelerRequests) {
//                    Bundle args = currentFragment.getArguments();
//                    String title = (args != null) ? args.getString("type_tr_name", "طلب خدمة") : "طلب خدمة";
//                    int icon = (args != null) ? args.getInt("icon_tr", 0) : 0;
//                    updateToolbar(title, false, icon, 0);
//                } else if (currentFragment instanceof AllImagesFragment) {
//                    updateToolbar("الإعلانات", false, R.drawable.ads, 0);
//                } else if (currentFragment instanceof Advertisements) {
//                    updateToolbar("تفاصيل الإعلان", false, R.drawable.ads, 0);
//                } else if (currentFragment instanceof MoreDetails) {
//                    updateToolbar("تفاصيل الرحلة", false, R.drawable.booking, 0);
//                } else if (currentFragment instanceof GuideFragment) {
//                    updateToolbar("دليل المسافر", false, R.drawable.solo_traveller, 0);
//                } else {
//                    String full_name = preferences.getString("full_name", "");
//                    Toolbar toolbar = requireActivity().findViewById(R.id.main_toolbar);
//                    String fullNames = full_name.trim();
//                    String firstName = "";
//                    if (!fullNames.isEmpty()) {
//                        String[] parts = fullNames.split("\\s+");
//                        if (parts.length > 0) {
//                            firstName = parts[0];
//                            if (!firstName.isEmpty()) {
//                                firstName = firstName.substring(0, 1).toUpperCase() +
//                                        firstName.substring(1).toLowerCase();
//                            }
//                        }
//                    }
//                    for (int i = 0; i < toolbar.getChildCount(); i++) {
//                        View child = toolbar.getChildAt(i);
//                        if (child.getId() == R.id.textGreeting || (child.findViewById(R.id.textGreeting) != null)) {
//                            toolbar.removeViewAt(i);
//                            break;
//                        }
//                    }
//
//                    View customView = getLayoutInflater().inflate(R.layout.toolbar_custom, null);
//                    TextView textGreeting = customView.findViewById(R.id.textGreeting);
//                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//                    String greeting = (hour >= 5 && hour < 12) ? "صباح الخير" : "مساء الخير";
//                    String fullText = greeting + " " + firstName;
//                    textGreeting.setText(fullText);
//                    toolbar.addView(customView);
//                }
//            }
//        });

        return view;
    }
    private void updateToolbar(String title, boolean showBackArrow, int iconRes, int fragmentId) {
        HomePage activity = (HomePage) getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
        }
        activity.updateToolbar(title, showBackArrow, iconRes, fragmentId);

    }
    public void updateServiceCards() {

        DBHelper dbHelper = new DBHelper(getContext());

        List<DBHelper.TypeTravelerRequest> types = dbHelper.getAllTypeTravelerRequests();
        gridCardsReligious.removeAllViews();
        gridCardsReligious.setColumnCount(3);
        SharedPreferences preferences = SharedPrefsHelper.get(getContext());

//        SharedPreferences preferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int userId = preferences.getInt("user_id", -1);

        for (DBHelper.TypeTravelerRequest type : types) {
            boolean isActive = (type.inactive != 0);

            // 1. إنشاء الكارد الأساسي (MaterialCardView)
            MaterialCardView cardView = new MaterialCardView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 102, getResources().getDisplayMetrics());
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            cardView.setLayoutParams(params);
            cardView.setRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
            cardView.setCardElevation(0);
//            cardView.setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.card_bg_selector));

            FrameLayout frameLayout = new FrameLayout(getContext());
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

            LinearLayout contentLayout = new LinearLayout(getContext());
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            contentLayout.setGravity(Gravity.CENTER);
            contentLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

            ImageView imageView = new ImageView(getContext());
            int imgWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
            int imgHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(imgWidth, imgHeight));
            int iconResId = getResources().getIdentifier(type.type_icon, "drawable", requireContext().getPackageName());
            imageView.setImageResource(iconResId != 0 ? iconResId : R.drawable.msafer_empty1);
//            imageView.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.icon_tint_selector));

            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            tvParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            textView.setLayoutParams(tvParams);
            textView.setText(type.type_tr_name);
            textView.setTextSize(12);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.text_color_selector));

            contentLayout.addView(imageView);
            contentLayout.addView(textView);
            frameLayout.addView(contentLayout);
            if (!isActive) {
//                contentLayout.setAlpha(0.4f);0

                cardView.setStrokeWidth(0);

                TextView tvSoon = new TextView(getContext());
                tvSoon.setText("قريباً");
                tvSoon.setTextSize(8f);
                tvSoon.setTextColor(Color.WHITE);
                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
                tvSoon.setTypeface(typeface, Typeface.NORMAL);
                tvSoon.setGravity(Gravity.CENTER);

                GradientDrawable badgeShape = new GradientDrawable();
                badgeShape.setShape(GradientDrawable.RECTANGLE);
                badgeShape.setColor(Color.parseColor("#9E9E9E"));
                badgeShape.setCornerRadius(15f);
                tvSoon.setBackground(badgeShape);

                int pH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
                int pV = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                tvSoon.setPadding(pH, pV, pH, pV);

                FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                badgeParams.gravity = Gravity.TOP | Gravity.CENTER;
                tvSoon.setLayoutParams(badgeParams);

                tvSoon.setZ(25f);
                frameLayout.addView(tvSoon);
                tvSoon.bringToFront();

//                cardView.setEnabled(false);
//                cardView.setClickable(false);
            }

//            if (!isActive) {
//                cardView.setCardBackgroundColor(Color.WHITE);
//                contentLayout.setAlpha(0.3f);
//
//                LinearLayout blurOverlay = new LinearLayout(getContext());
//                blurOverlay.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
//                blurOverlay.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray3));
//                blurOverlay.setAlpha(0.6f);
//
//                frameLayout.addView(blurOverlay);
//
//                TextView tvSoon = new TextView(getContext());
//
//                GradientDrawable shape = new GradientDrawable();
//                shape.setShape(GradientDrawable.RECTANGLE);
//                shape.setColor(ContextCompat.getColorStateList(getContext(), R.color.primary));
//                shape.setCornerRadius(50f);
//
//                tvSoon.setBackground(shape);
//                int paddingVertical = 8;
//                int paddingHorizontal = 30;
//                tvSoon.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
//
//                tvSoon.setText("قريباً");
//                tvSoon.setTextSize(14);
//                tvSoon.setTextColor(Color.WHITE);
//                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
//
//                tvSoon.setTypeface(typeface, Typeface.BOLD);
//                tvSoon.setGravity(Gravity.CENTER);
//
//                FrameLayout.LayoutParams soonParams = new FrameLayout.LayoutParams(
//                        FrameLayout.LayoutParams.WRAP_CONTENT,
//                        FrameLayout.LayoutParams.WRAP_CONTENT
//                );
//                soonParams.gravity = Gravity.CENTER;
//                tvSoon.setLayoutParams(soonParams);
//
//                frameLayout.addView(tvSoon);
//
//                cardView.setClickable(false);
//                cardView.setFocusable(false);
//            }

            cardView.addView(frameLayout);


            cardView.setOnClickListener(v -> {
                if (!isActive) {
                    UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(462, dbHelper));
                    return;
                }

                if (userId == -1) {
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
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putInt("type_tr_id", type.type_tr_id);
                bundle.putString("type_tr_name", type.type_tr_name);
                bundle.putInt("icon_tr", iconResId);
                Fragment fragment = new AddTravelerRequests();
                fragment.setArguments(bundle);

                ((HomePage) requireActivity()).openFullScreenFragment(fragment, type.type_tr_name, iconResId, 0);
                UserUtils.app_Page(getContext(), type.app_Page);
            });

            gridCardsReligious.addView(cardView);
        }

    }

    public interface RefreshCallback {
        void onDataUpdated(List<DBHelper.TypeTravelerRequest> types);
    }

    private void refreshHomeData(SwipeRefreshLayout swipeRefreshLayout, SharedPreferences prefs, DBHelper dbHelper, TravelerRequests.RefreshCallback callback) {
        if (!UserUtils.isNetworkAvailable(getContext())) {
            UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
//                        UserUtils.ToastMessages(getActivity(), message);
                }

                @Override
                public void onError(String error) {
                }

            });
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        UserUtils.checkAppUpdate(getContext());

        UserUtils.fetchServiceHome(getContext(), dbHelper, new PageHome.OnServiceHomeFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.ServiceHome> types) {

            }

            @Override
            public void onError(String error) {

            }
        });

        // جلب باقي البيانات (تجري في الخلفية)
        UserUtils.fetchTypeTravelerRequests(getContext(), dbHelper, new TravelerRequests.OnTypeRequestsFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.TypeTravelerRequest> types) {
                if (callback != null) {
                    callback.onDataUpdated(types);
                }
            }

            @Override
            public void onError(String error) {
            }
        });
        UserUtils.fetchCashBankData(getContext(), dbHelper, new UserUtils.OnCashBankFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.CashBank> types) {
            }

            @Override
            public void onError(String error) {

            }
        });

        UserUtils.fetchAndSaveMessages(getContext(), new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
                prefs.edit().putBoolean("messages_fetched", true).apply();
            }

            @Override
            public void onError(String error) {

            }
        });

        UserUtils.fetchAndSaveCountry(getContext(), new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });
        UserUtils.fetchAndSaveContactInfo(getContext(), dbHelper);

        UserUtils.fetchRoutes(getContext(), new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });

        UserUtils.loadVehicleTypesToDB(getContext());
        UserUtils.fetchCompany(getContext(), new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {

            }
        });

        UserUtils.fetchCodeDetails(getContext(), 5, null, new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {

            }
        });
        UserUtils.fetchAndSavePayTypes(getContext(), new UserUtils.GenericCallback() {

            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {

            }
        });
        UserUtils.fetchAndSavecities(getContext(), new UserUtils.citiesCallback() {
            @Override
            public void onSuccess(String message) {
                prefs.edit().putBoolean("cities_fetched", true).apply();
            }

            @Override
            public void onError(String error) {
            }
        });

        loadImages();

        swipeRefreshLayout.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 2000);
    }


    private int getPassengerId() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private void setupDotsIndicator(Context context, int size) {
        if (context == null) return;

        dotsCount = size;
        dots = new ImageView[dotsCount];
        dotsLayout.removeAllViews();

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(context);
            dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dot_inactive));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);

            dotsLayout.addView(dots[i], params);
        }

        if (dotsCount > 0) {
            dots[0].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dot_active));
        }
    }

    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dot_inactive));
        }
        if (position < dotsCount) {
            dots[position].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dot_active));
        }
    }

    private void loadImages() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String savedUrls = prefs.getString("home_image_urls", null);
        if (savedUrls != null) {
            List<String> urls = new ArrayList<>(Arrays.asList(savedUrls.split(",")));
            ImageAdapter adapter = new ImageAdapter(getActivity(), urls, 0);
            recyclerView2.setAdapter(adapter);

            // ✅ تجهيز النقاط
            setupDotsIndicator(getContext(), Math.min(urls.size(), 5));
            recyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null) {
                        int position = lm.findFirstCompletelyVisibleItemPosition();
                        if (position == RecyclerView.NO_POSITION) {
                            position = lm.findFirstVisibleItemPosition();
                        }
                        updateDotsIndicator(position);

                    }
                }
            });
        }

        String url = BASE_URL + "ImagesHome/?in_home_page=0";
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
                            UserUtils.sendLog(getContext(), "loadImages", e.toString(), e.toString(), "TravelerRequests");
                        }
                    }

                    String joinedUrls = String.join(",", imageUrls);
                    prefs.edit().putString("home_image_urls", joinedUrls).apply();

                    ImageAdapter adapter = new ImageAdapter(getActivity(), imageUrls, 0);
                    recyclerView2.setAdapter(adapter);
                    if (!isAdded()) return;
                    Context context = getContext();
                    if (context != null) {
                        setupDotsIndicator(context, Math.min(imageUrls.size(), 5));

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

                    // ✅ نرجع الشاشة لأول عنصر عشان الصور والهيدر يبانوا
//                    recyclerView.smoothScrollToPosition(0);
                },
                error -> {
                    UserUtils.sendLog(getContext(), "loadImages", error.toString(), error.toString(), "TravelerRequests");
                }
        );
        Volley.newRequestQueue(getActivity()).add(request);
    }

    // واجهة للرد بعد جلب البيانات
    public interface OnTypeRequestsFetchedListener {
        void onFetched(List<DBHelper.TypeTravelerRequest> types);

        void onError(String error);
    }

    public interface fetchTravelerRequestsListener {
        void onFetched(List<JSONObject> types);

        void onError(String error);
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

}