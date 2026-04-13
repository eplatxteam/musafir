package com.example.musafir;

import static android.app.Activity.RESULT_OK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.scottyab.rootbeer.RootBeer;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.musafir.LocationWorker.getCityNameFromIp;
import static com.example.musafir.R.color.primary;

import jp.wasabeef.blurry.Blurry;

public class HomeFragment extends Fragment {

    String BASE_URL = UserUtils.BASE_URL;
    String ImageUrl = UserUtils.ImageUrl;
    private static final int PICK_IMAGE_REQUEST = 1;
    private View currentViewForImage;
    ArrayList<String> cityNames = new ArrayList<>();
    ArrayList<Integer> cityIds = new ArrayList<>();
    String lastFromCityName, lastToCityName;
    private Integer lastVehicleId = null;
    private Integer lastFromCityId = null;
    private Integer lastToCityId = null;
    private String lastDate = null;
    private int lastPassengers = 1;
    private boolean lastFamiliesOnly = false;
    private RecyclerView recyclerView;
    HorizontalScrollView scrollView;

    private TripSearchAdapter adapter;
    private LinearLayout noDataText, noInternet;
    private boolean isLastPage = false;
    private final List<JSONObject> tripList = new ArrayList<>();
    private boolean isLoading = false;
    private int currentPage = 1;
    int tripId, availableSeats, tripId2;
    int pricePerSeat;

    int discountPricePerSeat;

    boolean firstLoadDone = false;
    int pickupOrders;
    int dropoffOrders;
    int passport_required;
    int company_no;
    String driver_id, carCode, carCodes, carCodesId, LocationTrip, DateTrip;

//    RadioGroup toCityRadioGroup;

    private LinearLayout fromLayout, toLayout;
    LottieAnimationView lottieWave;
    //    RadioGroup countriesRadioGroup;
    ImageView filterIcon;
    TextView textFromCity, textToCity;
    private String tripType;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView carIcon;
    private String latitude;
    private String longitude;
    int defaultCityId;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.recyclerViewTrips);
        carIcon = view.findViewById(R.id.carIcon);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        String defaultCity = prefs.getString("default_city", "حدد المدينة");
//        textFromCity.setText(defaultCity);
        defaultCityId = prefs.getInt("default_city_id", -1);
        carIcon.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });

        if (getArguments() != null) {
            tripType = getArguments().getString("trip_type");
        }

        noDataText = view.findViewById(R.id.noDataText);
        noInternet = view.findViewById(R.id.noInternet);
        scrollView = view.findViewById(R.id.searchContainer);
        filterIcon = view.findViewById(R.id.filterIcon);
        lottieWave = view.findViewById(R.id.lottieWaveHome);
        textFromCity = view.findViewById(R.id.textFromCity);
        textToCity = view.findViewById(R.id.textToCity);
        fromLayout = view.findViewById(R.id.fromLayout);
        toLayout = view.findViewById(R.id.toLayout);
        RootBeer rootBeer = new RootBeer(getContext());
        DBHelper dbHelper = new DBHelper(getContext());
        int passengerId = prefs.getInt("user_id", -1);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
//        initExtraFeatures(getView(), getLayoutInflater());
        if (rootBeer.isRooted()) {
            UserUtils.getMessageFromLocal(221, dbHelper, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(getActivity(), message);
                }

                @Override
                public void onError(String error) {
                }
            });
        } else {

            cityNames.clear();
            cityIds.clear();
            recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                            swipeRefreshLayout.setEnabled(false); // إيقاف السحب أثناء تحريك الصور
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            swipeRefreshLayout.postDelayed(() ->
                                    swipeRefreshLayout.setEnabled(true), 150
                            );
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
                if (passengerId != -1) {
                    refreshHomeData(swipeRefreshLayout, prefs, dbHelper);
                    getCityNameFromIp(getContext(), (cityAr, cityId) -> {
                        prefs.edit().putString("default_city", cityAr).apply();
                        prefs.edit().putInt("default_city_id", cityId).apply();
                    });
                } else {
                    swipeRefreshLayout.setRefreshing(false);

                }
            });
            adapter = new TripSearchAdapter(getContext(), tripList, new TripSearchAdapter.OnTripActionListener() {
                @Override
                public void onBookTrip(String Location, String dateTrip, int id, int trip_id2, int seats, int price, int pickupOrder, int dropoffOrder,
                                       String driverid, String car_code, String car_codes, String car_codes_id, int discountPrice,
                                       int passport_requireds, int p_company_no) {
                    tripId = id;
                    tripId2 = trip_id2;
                    LocationTrip = Location;
                    DateTrip = dateTrip;
                    availableSeats = seats;
                    pricePerSeat = price;
                    discountPricePerSeat = discountPrice;
                    pickupOrders = pickupOrder;
                    carCode = car_code;
                    carCodes = car_codes;
                    carCodesId = car_codes_id;
                    dropoffOrders = dropoffOrder;
                    driver_id = driverid;
                    passport_required = passport_requireds;
                    company_no = p_company_no;


                    if (passengerId != -1) {

                        if (dbHelper.isBooked(tripId, passengerId)) {
                            int bookingId = dbHelper.getBookingId(tripId, passengerId);

                            UserUtils.getMessageFromLocal(35, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });

                            Fragment fragment = new BookingDetailsFragment();
                            Bundle args = new Bundle();
                            args.putString("related_object_id", String.valueOf(bookingId));
                            fragment.setArguments(args);
                            ((FragmentActivity) getContext()).getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.full_screen_container, fragment)
                                    .addToBackStack(null)
                                    .commit();


                        } else {
                            showBookingDialog();
                        }
                    } else {
                        UserUtils.getMessageFromLocal(39, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getContext().startActivity(intent);
                    }
                }


                @Override
                public void onDetails(int tripId) {
                    ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage("جاري عرض البيانات...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    Context context = getContext();

                    String urlString = BASE_URL + "bookings/?trip=" + tripId;

                    new Thread(() -> {
                        StringBuilder result = new StringBuilder();
                        try {
                            URL url = new URL(urlString);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setRequestProperty("Accept", "application/json");
                            SharedPreferences prefs = SharedPrefsHelper.get(context);

//                            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
                            String token = prefs.getString("auth_token", null);

                            if (token != null) {
                                conn.setRequestProperty("Authorization", "Bearer " + token);
                            }
                            int responseCode = conn.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                String line;
                                while ((line = in.readLine()) != null) {
                                    result.append(line);
                                }
                                in.close();

                                JSONObject jsonResponse = new JSONObject(result.toString());
                                JSONArray resultsArray = jsonResponse.getJSONArray("results");

                                StringBuilder travelers = new StringBuilder();
                                for (int i = 0; i < resultsArray.length(); i++) {
                                    JSONObject booking = resultsArray.getJSONObject(i);
                                    String name = booking.getString("traveler_name");
                                    int seats = booking.getInt("number_of_seats");

                                    // كلمة مقعد أو مقاعد
                                    String seatWord = (seats == 1) ? "مقعد" : "مقاعد";

                                    travelers.append("- ").append(name)
                                            .append(" (").append(seats).append(" ").append(seatWord).append(")\n");
                                }

                                requireActivity().runOnUiThread(() -> {
                                    if (travelers.length() == 0) {
                                        progressDialog.dismiss();
                                        travelers.append("لا يوجد ركاب لهذه الرحلة");
                                    }
                                    progressDialog.dismiss();

                                    TextView textView = new TextView(context);
                                    textView.setText(travelers.toString());
                                    textView.setTextSize(18);
                                    textView.setPadding(32, 32, 32, 32);

                                    Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.linaround_bold);
                                    textView.setTypeface(typeface);

                                    textView.setGravity(Gravity.CENTER);
                                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                                    TextView titleView = new TextView(context);
                                    titleView.setText("المسافرون في الرحلة");
                                    titleView.setTextSize(24);
                                    titleView.setTypeface(typeface);
                                    titleView.setTextColor(ContextCompat.getColor(context, R.color.primary));
                                    textView.setTextColor(ContextCompat.getColor(context, R.color.text));
                                    titleView.setGravity(Gravity.CENTER);
                                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    titleView.setPadding(16, 16, 16, 16);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setCustomTitle(titleView)   // العنوان المخصص
                                            .setView(textView)          // النص
                                            .setPositiveButton("حسناً", null);

                                    AlertDialog dialog = builder.create();

                                    if (dialog.getWindow() != null) {
                                        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
                                    }
                                    dialog.show();

                                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                                    params.width = MATCH_PARENT; // الزر ياخذ العرض كامل
                                    positiveButton.setLayoutParams(params);
                                    positiveButton.setGravity(Gravity.CENTER);
                                    positiveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.primary3));
                                    positiveButton.setTextColor(ContextCompat.getColor(context, primary));
                                    positiveButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    positiveButton.setTextSize(20);
                                });
                            } else {
                                UserUtils.sendLog(getContext(), "onDetails", String.valueOf(responseCode), String.valueOf(responseCode), "Home Fragment");
                                requireActivity().runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    UserUtils.getMessageFromLocal(33, dbHelper, new UserUtils.MessageCallback() {
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

                        } catch (Exception e) {
                            UserUtils.sendLog(getContext(), "onDetails", e.toString(), e.toString(), "Home Fragment");
                            requireActivity().runOnUiThread(() ->
                            {
                                UserUtils.getMessageFromLocal(33, dbHelper, new UserUtils.MessageCallback() {
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
                }


                @Override
                public void onTripDetails(int tripId) {
                    if (passengerId != -1) {

                        Fragment fragment = new MoreDetails();
                        Bundle args = new Bundle();
                        args.putInt("trip_id", tripId); // أرسل الـ id
                        fragment.setArguments(args);

                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.full_screen_container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        ((HomePage) requireActivity()).updateToolbar("تفاصيل الرحلة", false, R.drawable.booking, 2);
                    } else {
                        UserUtils.getMessageFromLocal(39, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }

                        });
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getContext().startActivity(intent);
                    }
                }
            }, requireActivity().getSupportFragmentManager());

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager == null) return;
                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    if (firstLoadDone && !isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 2) {
                        recyclerView.post(() -> fetchTrips(currentPage + 1, lastFromCityId, lastToCityId, lastDate,
                                lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId, null));
                    } else {
                    }
                }
            });

            fetchTrips(1, null, lastToCityId, lastDate, lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId, null);

            if (tripList.size() > 0) {
                lottieWave.setVisibility(View.GONE);
            }
        }
        initExtraFeatures(view, inflater);

        return view;
    }


    private void showCityDialog(boolean isFromCity, TextView targetTextView, String currentTripType) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_city, null);

        EditText searchCity = dialogView.findViewById(R.id.searchBox);
        RecyclerView recyclerCities = dialogView.findViewById(R.id.recyclerCities);
        LinearLayout closeBtn = dialogView.findViewById(R.id.btnCloseDialog);

        Dialog dialog = new Dialog(requireContext(), R.style.KeyboardAwareDialog);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setGravity(Gravity.CENTER);

            int marginInDp = 20;
            float scale = requireContext().getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale);

            int screenWidth = requireContext().getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);

            window.setLayout(dialogWidth, WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogSlideUpAnimation;
        }

        // Blur الخلفية
        ViewGroup decorView = (ViewGroup) requireActivity().getWindow().getDecorView();
        Blurry.with(requireContext()).radius(15).sampling(2).onto(decorView);
        dialog.setOnDismissListener(d -> Blurry.delete(decorView));

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialog.show();

        // جلب المدن
        // 1. جلب البيانات الخام من قاعدة البيانات (هذه القائمة للمقارنة فقط)
        DBHelper db = new DBHelper(getContext());
        List<DBHelper.City> allCitiesFromDB = db.getAllCities2();

// 2. إنشاء قائمة فارغة للنتائج المفلترة التي ستعرض في الديالوج
        List<DBHelper.City> allCities = new ArrayList<>();

// 3. تحويل النوع لرقم
        int type = (currentTripType != null) ? Integer.parseInt(currentTripType) : 0;

// 4. الفلترة
        for (DBHelper.City city : allCitiesFromDB) {
            if (type == 3) {
                if (city.getCountryId() == 1) {
                    allCities.add(city);
                }
            } else if (type == 2) {
                if (!isFromCity) {
                    // "إلى": الدول الخارجية فقط
                    if (city.getCountryId() != 1) {
                        allCities.add(city);
                    }
                } else {
                    // "من": المدن المحلية فقط
                    if (city.getCountryId() == 1) {
                        allCities.add(city);
                    }
                }
            } else {
                // النوع 1 أو أي نوع آخر: أضف كل شيء
                allCities.add(city);
            }
        }


        if (allCities == null || allCities.isEmpty()) {
            UserUtils.getMessageFromLocal(5, db, new UserUtils.MessageCallback() {
                @Override
                public void onSuccess(String message) {
                    UserUtils.ToastMessages(getActivity(), message);
                }

                @Override
                public void onError(String error) {
                }
            });
            return;
        }

        // إعداد RecyclerView
        recyclerCities.setLayoutManager(new LinearLayoutManager(getContext()));

        List<DBHelper.City> filtered = new ArrayList<>(allCities);

        // Adapter المدن
        CityAdapter adapter2 = new CityAdapter(filtered, city -> {

            // عند اختيار مدينة
            targetTextView.setText(city.getNameAr());
            targetTextView.setTag(city.getId());


            if (isFromCity)
                lastFromCityId = city.getId();
            else
                lastToCityId = city.getId();

            currentPage = 1;
            isLastPage = false;
            tripList.clear();
            adapter.clear();
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            // --- استدعاء البحث ---
            fetchTrips(currentPage, lastFromCityId, lastToCityId, null,
                    lastPassengers, lastFamiliesOnly ? 1 : 0, null, null, null);
            dialog.dismiss();
        });

        recyclerCities.setAdapter(adapter2);

        // فلترة البحث
        searchCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim().toLowerCase();
                filtered.clear();

                for (DBHelper.City c : allCities) {
                    if (c.getNameAr().toLowerCase().contains(q)) {
                        filtered.add(c);
                    }
                }

                adapter2.updateList(filtered);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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

    private void refreshHomeData(SwipeRefreshLayout swipeRefreshLayout, SharedPreferences prefs, DBHelper dbHelper) {

        Context context = getContext();
        if (context == null || !isAdded()) return;
        lastToCityId = null;
        lastVehicleId = null;
//        familiesOnly = 0;
        lastPassengers = 1;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        lastDate = sdf.format(calendar.getTime());
        textToCity.setText("حدد المدينة");
        if (!isLoading) {
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
        }

        UserUtils.checkAppUpdate(context);

        UserUtils.fetchServiceHome(context, dbHelper, new PageHome.OnServiceHomeFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.ServiceHome> types) {
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

        UserUtils.fetchTypeTravelerRequests(context, dbHelper, new TravelerRequests.OnTypeRequestsFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.TypeTravelerRequest> types) {
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

        UserUtils.fetchRoutes(context, new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {
            }
        });

        UserUtils.loadVehicleTypesToDB(context);

        UserUtils.fetchCashBankData(getContext(), dbHelper, new UserUtils.OnCashBankFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.CashBank> types) {
            }

            @Override
            public void onError(String error) {
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

        UserUtils.fetchAndSaveCountry(context, new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
                if (isAdded() && prefs != null) {
                    prefs.edit().putBoolean("messages_fetched", true).apply();
                }
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
                    prefs.edit().putBoolean("messages_fetched", true).apply();
                }
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

        scheduleLocationUpdate();
        fetchCities();
        currentPage = 1;
        isLastPage = false;
        tripList.clear();

        if (adapter != null) {
            adapter.clear();
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 1));
        fetchTrips(1, null, lastToCityId, lastDate, lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId, null);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.postDelayed(() -> {
                if (isAdded()) {
                    swipeRefreshLayout.setRefreshing(false);
                    lottieWave.cancelAnimation();
                    lottieWave.setVisibility(View.GONE);
                }
            }, 1500);
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

    private AlertDialog exitDialog;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void addNameField(LinearLayout container, String hint, String initialText, InputFilter[] filters) {
        Context context = getContext();
        if (context == null) return;

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        // row.setBackgroundResource(R.drawable.edittext_background);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        rowParams.setMargins(10, 15, 10, 0);
        row.setLayoutParams(rowParams);
        row.setPadding(15, 5, 15, 5);

        // 1. حقل إدخال الاسم
        EditText nameInput = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        nameInput.setLayoutParams(lp);
        nameInput.setHint(hint);
        nameInput.setText(initialText);
        nameInput.setBackground(null);
        nameInput.setFilters(filters);
        nameInput.setPadding(15, 25, 15, 25);
        UserUtils.setEditTextState(nameInput, false);

        // 2. أيقونة الحالة (سنحتاجها للتحقق، نجعلها مخفية دائماً)
        ImageView statusIcon = new ImageView(context);
        statusIcon.setImageResource(R.drawable.upload_success); // أيقونة صح للنجاح
        statusIcon.setVisibility(View.GONE); // مخفية دائماً، نستخدمها فقط كعلم للتحقق
        statusIcon.setColorFilter(Color.parseColor("#4CAF50"));
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(50, 50);
        statusIcon.setLayoutParams(iconLp);

        ImageButton btnUpload = new ImageButton(context);
        btnUpload.setImageResource(R.drawable.ic_upload);
        btnUpload.setBackgroundResource(android.R.color.transparent);
        btnUpload.setPadding(10, 10, 10, 10);
        btnUpload.setColorFilter(ContextCompat.getColor(context, R.color.primary));

        if (passport_required == 1) {
            btnUpload.setVisibility(View.VISIBLE);
        } else {
            btnUpload.setVisibility(View.GONE);
        }

        btnUpload.setOnClickListener(v -> {
            currentViewForImage = nameInput;
            openGallery();
        });

        row.addView(nameInput);
        row.addView(statusIcon);
        row.addView(btnUpload);

        nameInput.setTag(R.id.tag_status_icon, statusIcon);
        nameInput.setTag(R.id.tag_upload_button, btnUpload);

        container.addView(row);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (currentViewForImage != null && selectedImageUri != null) {

                currentViewForImage.setTag(selectedImageUri);

                View statusIcon = (View) currentViewForImage.getTag(R.id.tag_status_icon);
                if (statusIcon != null) {
                    statusIcon.setVisibility(View.INVISIBLE);
                }

                 ImageButton btnUpload = (ImageButton) currentViewForImage.getTag(R.id.tag_upload_button);
                if (btnUpload != null) {
                    btnUpload.setImageResource(R.drawable.upload_success);
                    btnUpload.setColorFilter(Color.parseColor("#4CAF50"));
                }

//                UserUtils.ToastMessages(getActivity(), "تم اختيار صورة الجواز بنجاح");
            }
        }
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void showBookingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DBHelper dbHelper = new DBHelper(getContext());

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_booking, null);
        builder.setView(dialogView);

        // العناصر من XML
        TextView passengersTextView = dialogView.findViewById(R.id.inputSeats);
        TextView totalPriceTextView = dialogView.findViewById(R.id.totalPriceTextView);
        EditText inputNotes = dialogView.findViewById(R.id.inputNotes);

        ImageView btnMinus = dialogView.findViewById(R.id.btnMinus);
        ImageView btnPlus = dialogView.findViewById(R.id.btnPlus);
        Button btnAdd = dialogView.findViewById(R.id.btnYes);
        // Button btnCancel = dialogView.findViewById(R.id.btnNo);
        LinearLayout dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        TextView inputChildren = dialogView.findViewById(R.id.inputSeatschild);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvLocation = dialogView.findViewById(R.id.tvLocation);
        TextView IdTrip = dialogView.findViewById(R.id.IdTrip2);
        ImageView btnMinusChild = dialogView.findViewById(R.id.btnMinuschild);
        ImageView btnPlusChild = dialogView.findViewById(R.id.btnPluschild);
        UserUtils.setEditTextState(inputNotes, false);
        LinearLayout paymentContainer = dialogView.findViewById(R.id.paymentMethodsContainerInBooking);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

        LinearLayout childrenNamesContainer = dialogView.findViewById(R.id.childrenNamesContainer);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("en"));
        try {
            Date date = sdf.parse(DateTrip);

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ar"));
            String dayOfWeek = dayFormat.format(date);

            tvDate.setText("(" + dayOfWeek + ") " + DateTrip);

        } catch (ParseException e) {
            tvDate.setText(DateTrip); // لو حصل خطأ، يظهر التاريخ فقط
        }

        IdTrip.setText("رقم الرحلة: " + tripId2);
        tvLocation.setText(LocationTrip);
        LinearLayout adultsNamesContainer = new LinearLayout(getContext());
        adultsNamesContainer.setOrientation(LinearLayout.VERTICAL);
        adultsNamesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT
        ));
        childrenNamesContainer.addView(adultsNamesContainer, 0); // فوق الأطفال


        final int[] numSeats = {1};
        final int[] numChildren = {0};

        double seatPrice = (discountPricePerSeat > 0) ? discountPricePerSeat : pricePerSeat;
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMaximumFractionDigits(0);

        totalPriceTextView.setText(numberFormat.format(seatPrice) + " " + carCodes);


        btnMinus.setOnClickListener(v -> {
            if (numSeats[0] > 1) {
                numSeats[0]--;
                passengersTextView.setText(String.valueOf(numSeats[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText(numberFormat.format(totalPrice) + " " + carCodes);

                // إزالة آخر حقل اسم للكبار
                int count = adultsNamesContainer.getChildCount();
                if (count > 0) adultsNamesContainer.removeViewAt(count - 1);
            }
        });
        InputFilter arabicFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!((c >= 0x0621 && c <= 0x064A) || c == ' ')) {
                    return "";
                }
            }
            return null;
        };


//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String full_name = prefs.getString("full_name", "");
//        EditText firstAdultName = new EditText(getContext());
//        firstAdultName.setHint("الاسم الكامل للراكب 1");
        // ابحث عن كود إضافة الراكب الأول واستبدله بـ:
        addNameField(adultsNamesContainer, "الاسم الكامل للراكب 1", full_name, new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});
//        UserUtils.setEditTextState(firstAdultName, false);

//        firstAdultName.setBackgroundResource(R.drawable.edittext_background);
//        firstAdultName.setFilters(new InputFilter[]{arabicFilter});
//        firstAdultName.setFilters(new InputFilter[]{
//                arabicFilter,
//                new InputFilter.LengthFilter(30)
//        });
//
//        firstAdultName.setText(full_name);
//        firstAdultName.setPadding(30, 30, 30, 30);
//        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
//                MATCH_PARENT,
//                WRAP_CONTENT
//        );
//        params2.setMargins(10, 15, 10, 0);
//        firstAdultName.setLayoutParams(params2);
//        adultsNamesContainer.addView(firstAdultName);
        btnPlus.setOnClickListener(v -> {
            if (numSeats[0] + numChildren[0] < availableSeats) {
                numSeats[0]++;
                passengersTextView.setText(String.valueOf(numSeats[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText( numberFormat.format(totalPrice) + " " + carCodes);

//                EditText adultName = new EditText(getContext());
//                adultName.setHint("الاسم الكامل للراكب " + numSeats[0]);
                addNameField(adultsNamesContainer, "الاسم الكامل للراكب " + numSeats[0], "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});
//                adultName.setBackgroundResource(R.drawable.edittext_background);
//                adultName.setFilters(new InputFilter[]{arabicFilter});
//                adultName.setFilters(new InputFilter[]{
//                        arabicFilter,
//                        new InputFilter.LengthFilter(30)
//                });
//                UserUtils.setEditTextState(adultName, false);

//                adultName.setPadding(30, 30, 30, 30);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                        MATCH_PARENT,
//                        WRAP_CONTENT
//                );
//                params.setMargins(10, 15, 10, 0);
//                adultName.setLayoutParams(params);
//                adultsNamesContainer.addView(adultName);

            } else {
                UserUtils.getMessageFromLocal(34, dbHelper, new UserUtils.MessageCallback() {
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

        btnPlusChild.setOnClickListener(v -> {
            if (numSeats[0] + numChildren[0] < availableSeats) {
                numChildren[0]++;
                inputChildren.setText(String.valueOf(numChildren[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText(numberFormat.format(totalPrice) + " " + carCodes);
//                EditText childName = new EditText(getContext());
                addNameField(childrenNamesContainer, "الاسم الكامل للطفل " + numChildren[0], "", new InputFilter[]{arabicFilter, new InputFilter.LengthFilter(30)});
//                childName.setBackgroundResource(R.drawable.edittext_background);
//                childName.setPadding(30, 30, 30, 30);
//                childName.setFilters(new InputFilter[]{arabicFilter});
//                childName.setFilters(new InputFilter[]{
//                        arabicFilter,
//                        new InputFilter.LengthFilter(30)
//                });
//                UserUtils.setEditTextState(childName, false);
//
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                        MATCH_PARENT,
//                        WRAP_CONTENT
//                );
//                params.setMargins(10, 15, 10, 0);
//                childName.setLayoutParams(params);

//                childrenNamesContainer.addView(childName);
            } else {
                UserUtils.getMessageFromLocal(34, dbHelper, new UserUtils.MessageCallback() {
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

        btnMinusChild.setOnClickListener(v -> {
            if (numChildren[0] > 0) {
                numChildren[0]--;
                inputChildren.setText(String.valueOf(numChildren[0]));
                double totalPrice = (numSeats[0] + numChildren[0]) * seatPrice;
                totalPriceTextView.setText(numberFormat.format(totalPrice) + " " + carCodes);
                int count = childrenNamesContainer.getChildCount();
                if (count > 0) {
                    childrenNamesContainer.removeViewAt(count - 1);
                }
            }
        });

        exitDialog = builder.create();
        ViewGroup decorView = requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Blurry.with(getContext()).radius(15).sampling(2).onto(decorView);
        exitDialog.setOnDismissListener(d -> Blurry.delete(decorView));

        exitDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        exitDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        exitDialog.show();

        dialogCancelButton.setOnClickListener(v -> {
            exitDialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> {
            exitDialog.dismiss();
        });
        btnAdd.setOnClickListener(new UserUtils.SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                boolean missingPassportImage = false; // متغير جديد للتحقق من الصور
                int passengerId = prefs.getInt("user_id", -1);
                String notes = inputNotes.getText().toString().trim();

                StringBuilder adultNamesBuilder = new StringBuilder();
                boolean missingAdultName = false;

                StringBuilder childNamesBuilder = new StringBuilder();
                boolean missingChildName = false;

                // 1. معالجة أسماء وصور البالغين
                for (int i = 0; i < adultsNamesContainer.getChildCount(); i++) {
                    View view = adultsNamesContainer.getChildAt(i);
                    if (view instanceof LinearLayout) {
                        LinearLayout row = (LinearLayout) view;
                        if (row.getChildCount() > 0 && row.getChildAt(0) instanceof EditText) {
                            EditText nameField = (EditText) row.getChildAt(0);
                            String name = nameField.getText().toString().trim();

                            // التحقق من الاسم
                            if (name.isEmpty()) {
                                missingAdultName = true;
                                nameField.setError("يرجى إدخال الاسم الكامل");
                                UserUtils.setEditTextState(nameField, true);
                            } else {
                                adultNamesBuilder.append(name);
                                UserUtils.setEditTextState(nameField, false);
                                if (i < adultsNamesContainer.getChildCount() - 1) {
                                    adultNamesBuilder.append(", ");
                                }
                            }

                            // --- التعديل: التحقق من صورة الجواز ---
                            if (passport_required == 1) {
                                if (nameField.getTag() == null || !(nameField.getTag() instanceof Uri)) {
                                    missingPassportImage = true;
                                    // تغيير لون زر الرفع للأحمر لتنبيه المستخدم
                                    ImageButton btnUpload = (ImageButton) nameField.getTag(R.id.tag_upload_button);
                                    if (btnUpload != null) {
                                        btnUpload.setColorFilter(Color.RED);
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. معالجة أسماء وصور الأطفال
                for (int i = 0; i < childrenNamesContainer.getChildCount(); i++) {
                    View view = childrenNamesContainer.getChildAt(i);
                    if (view instanceof LinearLayout) {
                        LinearLayout row = (LinearLayout) view;
                        if (row.getChildCount() > 0 && row.getChildAt(0) instanceof EditText) {
                            EditText nameField = (EditText) row.getChildAt(0);
                            String name = nameField.getText().toString().trim();

                            if (name.isEmpty()) {
                                missingChildName = true;
                                nameField.setError("يرجى إدخال الاسم الكامل");
                                UserUtils.setEditTextState(nameField, true);
                            } else {
                                childNamesBuilder.append(name);
                                UserUtils.setEditTextState(nameField, false);
                                childNamesBuilder.append(", ");
                            }

                            // --- التعديل: التحقق من صورة جواز الطفل ---
                            if (passport_required == 1) {
                                if (nameField.getTag() == null || !(nameField.getTag() instanceof Uri)) {
                                    missingPassportImage = true;
                                    ImageButton btnUpload = (ImageButton) nameField.getTag(R.id.tag_upload_button);
                                    if (btnUpload != null) {
                                        btnUpload.setColorFilter(Color.RED);
                                    }
                                }
                            }
                        }
                    }
                }

                if (missingAdultName || missingChildName) {
                    UserUtils.getMessageFromLocal(164, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                    return;
                }

                if (missingPassportImage) {
                    UserUtils.getMessageFromLocal(261, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                    UserUtils.ToastMessages(getActivity(), "يرجى رفع صور جوازات السفر لجميع الركاب");
                    return;
                }

                // إذا وصل الكود هنا، يعني كل البيانات مكتملة
                String childNames = childNamesBuilder.toString();
                if (childNames.endsWith(", ")) {
                    childNames = childNames.substring(0, childNames.length() - 2);
                }

                String adultNames = adultNamesBuilder.toString().replaceAll(", $", "");
                int totalSeats = numSeats[0] + numChildren[0];
                double totalPrice = totalSeats * seatPrice;

                sendBookingRequest(
                        carCode, tripId2, totalSeats, pickupOrders, dropoffOrders,
                        passengerId, totalPrice, notes, driver_id,
                        carCodesId, adultNames, childNames,
                        0, "waiting", 0, company_no,
                        adultsNamesContainer, childrenNamesContainer
                );

                exitDialog.dismiss();
            }
        });

    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
            if (result != null && result.contains("/")) {
                result = result.substring(result.lastIndexOf("/") + 1);
            }
        }

        return result;
    }

    private void writeFileField(DataOutputStream out, String fieldName, Uri fileUri, String boundary) throws IOException {
        String originalName = getFileNameFromUri(fileUri); // الاسم الأصلي
        String uniqueID = UUID.randomUUID().toString();   // جزء فريد
        String fileName = uniqueID + "_" + originalName;   // الاسم النهائي الفريد
        String mimeType = getContext().getContentResolver().getType(fileUri);

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        out.writeBytes("Content-Type: " + mimeType + "\r\n");
        out.writeBytes("\r\n");
        InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        out.writeBytes("\r\n");
    }

    private void sendBookingRequest(String car_code, int tripId, int numSeats, int pickupOrder,
                                    int dropoffOrder, int passengerId,
                                    double totalPrice, String notes, String driver_id, String car_codes,
                                    String adult_names_str, String child_names_str, int pay_type, String payment_status,
                                    int request_id, int company_no,
                                    LinearLayout adultsContainer, LinearLayout childrenContainer) { // أضفنا الحاويات هنا

        DBHelper dbHelper = new DBHelper(getContext());
        UserUtils.showSuccessGif(3, requireActivity(), null);

        new Thread(() -> {
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();

                URL url = new URL(BASE_URL + "bookings/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                SharedPreferences prefs = SharedPrefsHelper.get(getContext());
                String token = prefs.getString("auth_token", null);
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // 1. إرسال الحقول النصية
                writeFormField(dos, "trip", String.valueOf(tripId), boundary);
                writeFormField(dos, "number_of_seats", String.valueOf(numSeats), boundary);
                writeFormField(dos, "pickup_point_order", String.valueOf(pickupOrder), boundary);
                writeFormField(dos, "dropoff_point_order", String.valueOf(dropoffOrder), boundary);
                writeFormField(dos, "passenger", String.valueOf(passengerId), boundary);
                writeFormField(dos, "adult_names", adult_names_str, boundary);
                writeFormField(dos, "child_names", child_names_str, boundary);
                writeFormField(dos, "total_price", String.valueOf(totalPrice), boundary);
                writeFormField(dos, "passenger_notes", notes, boundary);
                writeFormField(dos, "driver_id", driver_id, boundary);
                writeFormField(dos, "car_code", car_code, boundary);
                writeFormField(dos, "car_codes", car_codes, boundary);
                writeFormField(dos, "pay_type", String.valueOf(pay_type), boundary);
                writeFormField(dos, "payment_status", payment_status, boundary);
                writeFormField(dos, "request_id", String.valueOf(request_id), boundary);
                writeFormField(dos, "company_no", String.valueOf(company_no), boundary);

                // 2. رفع صور جوازات البالغين
                // داخل دالة sendBookingRequest - الجزء الخاص برفع الصور (الخطوة 3 و 4)

// 3. رفع صور جوازات البالغين
                if (adultsContainer != null) {
                    for (int i = 0; i < adultsContainer.getChildCount(); i++) {
                        View row = adultsContainer.getChildAt(i);

                        // التحقق أن العنصر الحالي هو LinearLayout (الصف)
                        if (row instanceof LinearLayout) {
                            LinearLayout rowLayout = (LinearLayout) row;

                            // التحقق أن الصف يحتوي على عناصر وأن أول عنصر هو EditText
                            if (rowLayout.getChildCount() > 0 && rowLayout.getChildAt(0) instanceof EditText) {
                                EditText nameField = (EditText) rowLayout.getChildAt(0);
                                Uri imageUri = (Uri) nameField.getTag();

                                if (imageUri != null) {
                                    writeFileField(dos, "passport_image" + (i + 1), imageUri, boundary);
                                }
                            }
                        }
                    }
                }

                if (childrenContainer != null) {
                    for (int i = 0; i < childrenContainer.getChildCount(); i++) {
                        View row = childrenContainer.getChildAt(i);

                        if (row instanceof LinearLayout) {
                            LinearLayout rowLayout = (LinearLayout) row;

                            if (rowLayout.getChildCount() > 0 && rowLayout.getChildAt(0) instanceof EditText) {
                                EditText nameField = (EditText) rowLayout.getChildAt(0);
                                Uri imageUri = (Uri) nameField.getTag();

                                if (imageUri != null) {
                                    writeFileField(dos, "passport_image" + (i + 1), imageUri, boundary);
                                }
                            }
                        }
                    }
                }


                dos.writeBytes("--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                // 4. معالجة الرد
                int status = conn.getResponseCode();
                InputStream inputStream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                getActivity().runOnUiThread(() -> {
                    UserUtils.hideSuccessGif(getActivity());
                    if (status >= 200 && status < 300) {
                        try {
                            JSONObject respJson = new JSONObject(response.toString());
                            int bookingIdFromServer = respJson.getInt("booking_id");
                            dbHelper.addBooking(tripId, bookingIdFromServer, passengerId);

                            UserUtils.getMessageFromLocal(48, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            Fragment detailsFragment = new BookingDetailsFragment();
                            Bundle args = new Bundle();
                            args.putString("related_object_id", String.valueOf(bookingIdFromServer));
                            detailsFragment.setArguments(args);

                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.full_screen_container, detailsFragment)
                                    .addToBackStack(null).commit();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        UserUtils.sendLog(getContext(), "sendBookingRequest", response.toString(), "Error Status: " + status, "Home Fragment");
                    }
                });
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> UserUtils.hideSuccessGif(getActivity()));
            }
        }).start();
    }

    private void writeFormField(DataOutputStream request, String fieldName, String fieldValue, String boundary) throws IOException {
        request.writeBytes("--" + boundary + "\r\n");
        request.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n");
        request.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        request.write(fieldValue.getBytes(StandardCharsets.UTF_8));
        request.writeBytes("\r\n");
    }

    private void initExtraFeatures(View rootView, LayoutInflater inflater) {

        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        DBHelper dbHelper = new DBHelper(getContext());

        carIcon.setOnClickListener(v -> {
            String fromCity = textFromCity.getText().toString().trim();
            String toCity = textToCity.getText().toString().trim();

            if (fromCity.equals("حدد المدينة") || toCity.equals("حدد المدينة")) {
                return;
            }

            textFromCity.setText(toCity);
            textToCity.setText(fromCity);
            currentPage = 1;
            isLastPage = false;
            tripList.clear();
            adapter.clear();
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
            fetchTrips(currentPage, lastFromCityId, lastToCityId, null,
                    lastPassengers, lastFamiliesOnly ? 1 : 0, null, null, null);

        });
        filterIcon.setOnClickListener(new UserUtils.SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showCustomSearchDialog();
            }
        });

        LinearLayout weekContainer = rootView.findViewById(R.id.week_container);

        Calendar calendar = Calendar.getInstance(new Locale("en"));
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ar"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", new Locale("en"));
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        HorizontalScrollView daysScroll = rootView.findViewById(R.id.days_scroll_view);

        daysScroll.post(() ->

        {
            daysScroll.fullScroll(View.FOCUS_RIGHT);
        });
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        LinearLayout[] dayViews = new LinearLayout[14];
//        LayoutInflater inflater;
        for (
                int i = 0;
                i < 14; i++) {
            View dayView = inflater.inflate(R.layout.week_days, weekContainer, false);
            dayViews[i] = (LinearLayout) dayView;

            TextView tvDayName = dayView.findViewById(R.id.day_name);
            TextView tvDayNumber = dayView.findViewById(R.id.day_number);
            View dayDot = dayView.findViewById(R.id.day_dot);

            Calendar tempCal = (Calendar) calendar.clone();

            String dayName = dayFormat.format(tempCal.getTime());
            String dayNumber = dateFormat.format(tempCal.getTime());
            String fullDate = fullDateFormat.format(tempCal.getTime());

            dayView.setTag(fullDate);

            tvDayName.setText(dayName);
            tvDayNumber.setText(dayNumber);

            if (Integer.parseInt(dayNumber) == today) {
                dayView.setSelected(true);
                tvDayName.setTextColor(getResources().getColor(R.color.black));
                tvDayNumber.setTextColor(getResources().getColor(R.color.black));
                dayDot.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.black)
                ));
            } else {
                tvDayName.setTextColor(getResources().getColor(R.color.secondary));
                tvDayNumber.setTextColor(getResources().getColor(R.color.secondary));
                dayDot.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.secondary)
                ));
            }
            dayView.setOnClickListener(v -> {
                boolean alreadySelected = v.isSelected();

                String dateToSend = alreadySelected ? null : (String) v.getTag();

                for (int j = 0; j < 14; j++) {
                    dayViews[j].setSelected(false);
                    ((TextView) dayViews[j].findViewById(R.id.day_name)).setTextColor(getResources().getColor(R.color.secondary));
                    ((TextView) dayViews[j].findViewById(R.id.day_number)).setTextColor(getResources().getColor(R.color.secondary));
                    dayViews[j].findViewById(R.id.day_dot).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.secondary)));
                }

                if (!alreadySelected) {
                    v.setSelected(true);
                    tvDayName.setTextColor(getResources().getColor(R.color.secondary));
                    tvDayNumber.setTextColor(getResources().getColor(R.color.secondary));
                    dayDot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.secondary)));
                }

                currentPage = 1;
                isLastPage = false;
                tripList.clear();
                adapter.notifyDataSetChanged();

                fetchTrips(currentPage, lastFromCityId, lastToCityId, dateToSend,
                        lastPassengers, lastFamiliesOnly ? 1 : 0, null, null, null);
            });
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            weekContainer.addView(dayView);
        }

        scheduleLocationUpdate();
        if (!UserUtils.isNetworkAvailable(

                requireContext())) {
            requireActivity().runOnUiThread(() -> {
                if (lottieWave.isAnimating()) {
                    lottieWave.cancelAnimation();
                }

                lottieWave.setVisibility(View.GONE);

                noInternet.setVisibility(View.VISIBLE);
                noDataText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            });

            isLoading = false;
        }


//        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            @Override
//            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//
//                switch (e.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                    case MotionEvent.ACTION_MOVE:
//                        swipeRefreshLayout.setEnabled(false); // إيقاف السحب أثناء تحريك الصور
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_CANCEL:
//                        swipeRefreshLayout.postDelayed(() ->
//                                swipeRefreshLayout.setEnabled(true), 150
//                        );
//                        break;
//                }
//
//                return false;
//            }
//
//            @Override
//            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//            }
//        });
        UserUtils.fetchAndSavecities(

                getContext(), new UserUtils.citiesCallback() {
                    @Override
                    public void onSuccess(String message) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("messages_fetched", true);
                        editor.apply();
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

        //        refreshHomeData(swipeRefreshLayout, prefs, dbHelper);
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//            refreshHomeData(swipeRefreshLayout, prefs, dbHelper);
//            getCityNameFromIp(getContext(), (cityAr, cityId) -> {
//                prefs.edit().putString("default_city", cityAr).apply();
//                prefs.edit().putInt("default_city_id", cityId).apply();
//            });
//            swipeRefreshLayout.setEnabled(false);
//        });
        String defaultCity = prefs.getString("default_city", "حدد المدينة");
//
        textFromCity.setText(defaultCity);
        fromLayout.setOnClickListener(new UserUtils.SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
//                                              v.setEnabled(false);
                showCityDialog(true, textFromCity, tripType);
            }
        });

        toLayout.setOnClickListener(new UserUtils.SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
//                                            v.setEnabled(false);
                showCityDialog(false, textToCity, tripType);
            }
        });

        UserUtils.getPublicIp((ipJson, city_id) ->

        {
            if (ipJson != null) {
                try {
                    JSONObject ipObject = new JSONObject(ipJson);
                    latitude = ipObject.optString("latitude", null);
                    longitude = ipObject.optString("longitude", null);

                } catch (Exception ignored) {
                }
            }
        });
    }

    private void fetchTrips(int page, @Nullable Integer fromCityId,
                            @Nullable Integer toCityId,
                            @Nullable String date, @Nullable Integer passengers,
                            @Nullable Integer familiesOnly, @Nullable Integer vehicle_type,
                            @Nullable Integer defaultCityId,
                            @Nullable Integer lastCompanyId) {

        if (!isAdded() || getActivity() == null || isLoading || isLastPage) return;
        if (page == 1) {
            tripList.clear();
            adapter.notifyDataSetChanged();
        }
        DBHelper dbHelper = new DBHelper(getContext());
        isLoading = true;

        if (!UserUtils.isNetworkAvailable(requireContext())) {
            requireActivity().runOnUiThread(() -> {
                if (lottieWave.isAnimating()) lottieWave.cancelAnimation();
                lottieWave.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                noDataText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            });
            isLoading = false;
            return;
        }

        requireActivity().runOnUiThread(() -> {
            noDataText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
            noInternet.setVisibility(View.GONE);
        });

        new Thread(() -> {
            try {
//                    if (ipJson == null) {
//                        isLoading = false;
//                        if (isAdded() && getActivity() != null) {
//                            requireActivity().runOnUiThread(() -> {
//                                noInternet.setVisibility(View.VISIBLE);
//                                recyclerView.setVisibility(View.GONE);
//                            });
//                        }
//                        return;
//                    }

                try {
//                        JSONObject ipObject = new JSONObject(ipJson);
//                        String latitude = ipObject.optString("latitude", null);
//                        String longitude = ipObject.optString("longitude", null);
                    SharedPreferences sharedPreferences = SharedPrefsHelper.get(getContext());

//                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    String userType = sharedPreferences.getString("user_type", "");
                    int driverId = sharedPreferences.getInt("user_id", -1);
                    String defaultCity = sharedPreferences.getString("default_city", "حدد المدينة");

                    StringBuilder urlBuilder;
                    if ("driver".equals(userType)) {
                        urlBuilder = new StringBuilder(BASE_URL + "trip-list/?driver=" + driverId);
                    } else {
                        urlBuilder = new StringBuilder(BASE_URL + "trip-list/?page=" + page + "&limit=5");
                    }

                    if (defaultCityId != null)
                        urlBuilder.append("&start_city_order=").append(defaultCityId);
                    if (latitude != null && longitude != null)
                        urlBuilder.append("&latitude=").append(latitude).append("&longitude=").append(longitude);
                    if (fromCityId != null && fromCityId > 0)
                        urlBuilder.append("&start_city=").append(fromCityId);
                    if (toCityId != null && toCityId > 0)
                        urlBuilder.append("&end_city=").append(toCityId);
                    if (date != null)
                        urlBuilder.append("&start_date=").append(date);
                    if (passengers != null && passengers != 1)
                        urlBuilder.append("&seats=").append(passengers);
                    if (familiesOnly != null && familiesOnly != 0)
                        urlBuilder.append("&family=").append(familiesOnly);
                    if (vehicle_type != null)
                        urlBuilder.append("&vehicle_type_id=").append(vehicle_type);
                    if (lastCompanyId != null)
                        urlBuilder.append("&company_no=").append(lastCompanyId);
                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder responseBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) responseBuilder.append(line);
                        reader.close();

                        JSONObject jsonObject = new JSONObject(responseBuilder.toString());
                        JSONArray resultsArray = jsonObject.getJSONArray("results");

                        List<JSONObject> trips = new ArrayList<>();
                        for (int i = 0; i < resultsArray.length(); i++) {
                            trips.add(resultsArray.getJSONObject(i));
                        }

                        if (!"driver".equals(userType)) { // فقط للركاب
                            List<JSONObject> activeTrips = new ArrayList<>();
                            for (JSONObject trip : trips) {
                                int inactive = trip.optInt("inactive", 1);
                                if (inactive != 0) {
                                    activeTrips.add(trip);
                                }
                            }
                            trips = activeTrips;
                        }
                        boolean lastPage = jsonObject.isNull("next");

                        List<JSONObject> finalTrips = trips;
                        requireActivity().runOnUiThread(() -> {
                            lottieWave.cancelAnimation();
                            lottieWave.setVisibility(View.GONE);

                            swipeRefreshLayout.setRefreshing(false);
                            swipeRefreshLayout.setEnabled(true);

                            if (finalTrips.isEmpty()) {
                                adapter.clear();
                                recyclerView.setVisibility(View.GONE);
                                noDataText.setVisibility(View.VISIBLE);
                            } else {
                                List<JSONObject> filteredTrips = new ArrayList<>();
                                int requestedType = Integer.parseInt(tripType);

                                for (JSONObject trip : finalTrips) {
                                    try {
                                        if (tripType == null || tripType.isEmpty()) {
                                            filteredTrips.add(trip);
                                            continue;
                                        }

                                        int currentTripType = trip.optInt("trip_type", 0);
                                        if (currentTripType == requestedType) {
                                            filteredTrips.add(trip);
                                        }

                                    } catch (Exception e) {
                                    }
                                }


                                if (filteredTrips.isEmpty()) {
                                    recyclerView.setVisibility(View.GONE);
                                    noDataText.setVisibility(View.VISIBLE);
                                    noInternet.setVisibility(View.GONE);
                                    swipeRefreshLayout.setEnabled(true);
                                } else {
                                    noDataText.setVisibility(View.GONE);
                                    noInternet.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    initExtraFeatures(getView(), getLayoutInflater());
                                    adapter.addTrips(filteredTrips);
                                    currentPage = page;
                                    isLastPage = lastPage;
                                }
                            }
                            isLoading = false;
                        });

                    } else {
                        requireActivity().runOnUiThread(() -> {
                            lottieWave.cancelAnimation();
                            lottieWave.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            noDataText.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setEnabled(true);
                            noInternet.setVisibility(View.GONE);
                            isLoading = false;
                        });
                    }

                    conn.disconnect();
                } catch (Exception e) {
                    handleError(e);
                }
            } catch (Exception e) {
                handleError(e);
            }
        }).start();
    }


    private void handleError(Exception e) {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(() -> {
                lottieWave.cancelAnimation();
                lottieWave.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                noDataText.setVisibility(View.VISIBLE);
                noInternet.setVisibility(View.GONE);
                isLoading = false;
            });
        }
    }


    private int selectedCountryId = -1;

    private void fetchCities() {
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            try {
                List<DBHelper.Country> countries = dbHelper.getAllCountries();
                List<DBHelper.City> allCities = dbHelper.getAllCities();

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

                int finalSelectedCountryId = selectedCountryId;
                requireActivity().runOnUiThread(() -> {
//                    populateCountryRadioButtons(countries, finalSelectedCountryId, countriesRadioGroup);
//                    populateCityRadioButtons();
                    currentPage = 1;
                    isLastPage = false;
                    tripList.clear();
                    adapter.clear();

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

//                    fetchTrips(currentPage, null, lastToCityId, lastDate, lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId);
                });

            } catch (Exception e) {
            }
        }).start();
    }


    Spinner vehicleTypeSpinner, companySpinner;
    List<String> vehicleTypeNames = new ArrayList<>();
    Map<String, Integer> vehicleTypeMap = new HashMap<>();
    private Integer vehicleTypeFromArgs = null;
    List<String> companyNames = new ArrayList<>();
    Map<String, Integer> companyMap = new HashMap<>();
    private Integer lastCompanyId = null;

    private void loadCompanies() {
        DBHelper dbHelper = new DBHelper(getContext());

        companyNames.clear();
        companyMap.clear();

        companyNames.add("");
        companyMap.put("", -1);

        List<Map<String, Object>> companies = dbHelper.getAllCompanies();

        for (Map<String, Object> c : companies) {
            String name = (String) c.get("company_name");
            int id = (int) c.get("company_no");
            companyNames.add(name);
            companyMap.put(name, id);
        }

        if (isAdded()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, companyNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            companySpinner.setAdapter(adapter);

            if (lastCompanyId != null) {
                for (int i = 0; i < companyNames.size(); i++) {
                    if (companyMap.get(companyNames.get(i)).equals(lastCompanyId)) {
                        companySpinner.setSelection(i);
                        break;
                    }
                }
            }
        }
    }


    private void loadVehicleTypes() {
        DBHelper dbHelper = new DBHelper(getContext());

        // مسح أي بيانات قديمة في المصفوفات
        vehicleTypeNames.clear();
        vehicleTypeMap.clear();

        // إضافة عنصر فارغ افتراضي
        vehicleTypeNames.add("");
        vehicleTypeMap.put("", -1);

        // جلب البيانات من جدول SQLite
        List<DBHelper.VehicleType> vehicleTypes = dbHelper.getVehicleTypes(1);

        for (DBHelper.VehicleType v : vehicleTypes) {
            vehicleTypeNames.add(v.getName());
            vehicleTypeMap.put(v.getName(), v.getId());
        }

        // ضبط Spinner إذا كانت Fragment مضافة
        if (isAdded()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, vehicleTypeNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vehicleTypeSpinner.setAdapter(adapter);

            vehicleTypeSpinner.setSelection(0);

            if (vehicleTypeFromArgs != null) {
                int positionToSelect = -1;
                for (int i = 0; i < vehicleTypeNames.size(); i++) {
                    String name = vehicleTypeNames.get(i);
                    int id = vehicleTypeMap.get(name);
                    if (id == vehicleTypeFromArgs) {
                        positionToSelect = i;
                        break;
                    }
                }
                if (positionToSelect >= 0) {
                    vehicleTypeSpinner.setSelection(positionToSelect);
                }
            }
        }
    }

    private void showCustomSearchDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_search_options, null);

        ImageView minusBtn = dialogView.findViewById(R.id.btnMinus);
        ImageView plusBtn = dialogView.findViewById(R.id.btnPlus);
        TextView passengersTextView = dialogView.findViewById(R.id.passengersTextView);
        Button dialogSearchButton = dialogView.findViewById(R.id.dialogSearchButton);
        ImageView btnResetFilter = dialogView.findViewById(R.id.btnResetFilter);
        LinearLayout dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        LinearLayout dateSelectorLayout = dialogView.findViewById(R.id.dateSelectorLayout);
        CheckBox checkboxFamiliesOnly = dialogView.findViewById(R.id.checkboxFamiliesOnly);
        vehicleTypeSpinner = dialogView.findViewById(R.id.vehicleTypeSpinner);
        companySpinner = dialogView.findViewById(R.id.companySpinner);
        loadCompanies();
        loadVehicleTypes();
        if (lastVehicleId != null) {
            vehicleTypeSpinner.post(() -> {
                for (int i = 0; i < vehicleTypeSpinner.getCount(); i++) {
                    String name = vehicleTypeSpinner.getItemAtPosition(i).toString();
                    if (vehicleTypeMap.get(name) != null && vehicleTypeMap.get(name).equals(lastVehicleId)) {
                        vehicleTypeSpinner.setSelection(i);
                        break;
                    }
                }
            });
        }
//        TextView textFromCity = dialogView.findViewById(R.id.textFromCity);
//        TextView textToCity = dialogView.findViewById(R.id.textToCity);
//        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
//        textFromCity.setOnClickListener(v2 -> showCityDialog(true, textFromCity, tripType));
//        textToCity.setOnClickListener(v2 -> showCityDialog(false, textToCity, tripType));
//        carIcons.setOnClickListener(v -> {
//            String fromCity = textFromCity.getText().toString().trim();
//            String toCity = textToCity.getText().toString().trim();
//
//            if (fromCity.equals("حدد المدينة") || toCity.equals("حدد المدينة")) {
//                return;
//            }
//
//            // عكس القيم بينهما
//            textFromCity.setText(toCity);
//            textToCity.setText(fromCity);
//        });

//        String defaultCity = prefs.getString("default_city", "حدد المدينة");
//        textFromCity.setText(defaultCity);
        checkboxFamiliesOnly.setChecked(lastFamiliesOnly);
        final int[] passengers = {lastPassengers > 0 ? lastPassengers : 1};
        passengersTextView.setText(String.valueOf(passengers[0]));

        minusBtn.setOnClickListener(v -> {
            if (passengers[0] > 1) {
                passengers[0]--;
                passengersTextView.setText(String.valueOf(passengers[0]));
            }
        });

        plusBtn.setOnClickListener(v -> {
            if (passengers[0] < 6) {
                passengers[0]++;
                passengersTextView.setText(String.valueOf(passengers[0]));
            }
        });

        Dialog dialog = new Dialog(requireContext(), R.style.TransparentBottomDialog);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            int marginInDp = 20;
            float scale = requireContext().getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);
            int screenWidth = requireContext().getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = screenWidth - (2 * marginInPx);
            window.setLayout(dialogWidth, WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogSlideUpAnimation;
        }

        // --- الأيام ---
//        LinearLayout weekContainer = dialogView.findViewById(R.id.week_container);
//        Calendar calendar2 = Calendar.getInstance(new Locale("ar"));
//        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ar"));
//        SimpleDateFormat dateFormat = new SimpleDateFormat("d", new Locale("ar"));
//        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
//        int today = calendar2.get(Calendar.DAY_OF_MONTH);
//        LinearLayout[] dayViews = new LinearLayout[14];

//        for (int i = 0; i < 14; i++) {
//            View dayView = getLayoutInflater().inflate(R.layout.week_days, weekContainer, false);
//            dayViews[i] = (LinearLayout) dayView;
//
//            TextView tvDayName = dayView.findViewById(R.id.day_name);
//            TextView tvDayNumber = dayView.findViewById(R.id.day_number);
//            View dayDot = dayView.findViewById(R.id.day_dot);
//
//            Calendar tempCal = (Calendar) calendar.clone();
//            String dayName = dayFormat.format(tempCal.getTime());
//            String dayNumber = dateFormat.format(tempCal.getTime());
//            String fullDate = fullDateFormat.format(tempCal.getTime());
//            dayView.setTag(fullDate);
//
//            tvDayName.setText(dayName);
//            tvDayNumber.setText(dayNumber);
//
//            if (Integer.parseInt(dayNumber) == today) {
//                dayView.setSelected(true);
//                tvDayName.setTextColor(getResources().getColor(R.color.white));
//                tvDayNumber.setTextColor(getResources().getColor(R.color.white));
//                dayDot.setBackgroundTintList(ColorStateList.valueOf(
//                        getResources().getColor(R.color.white)
//                ));
//            } else {
//                tvDayName.setTextColor(getResources().getColor(R.color.secondary));
//                tvDayNumber.setTextColor(getResources().getColor(R.color.secondary));
//                dayDot.setBackgroundTintList(ColorStateList.valueOf(
//                        getResources().getColor(R.color.secondary)
//                ));
//            }
//
//            // حدث الضغط على اليوم
//            dayView.setOnClickListener(v -> {
//                for (int j = 0; j < 14; j++) {
//                    dayViews[j].setSelected(false);
//                    ((TextView) dayViews[j].findViewById(R.id.day_name))
//                            .setTextColor(getResources().getColor(R.color.secondary));
//                    ((TextView) dayViews[j].findViewById(R.id.day_number))
//                            .setTextColor(getResources().getColor(R.color.secondary));
//                    dayViews[j].findViewById(R.id.day_dot)
//                            .setBackgroundTintList(ColorStateList.valueOf(
//                                    getResources().getColor(R.color.secondary)
//                            ));
//                }
//
//                dayView.setSelected(true);
//                tvDayName.setTextColor(getResources().getColor(R.color.white));
//                tvDayNumber.setTextColor(getResources().getColor(R.color.white));
//                dayDot.setBackgroundTintList(ColorStateList.valueOf(
//                        getResources().getColor(R.color.white)
//                ));
//
////                tvDayName.setTextColor(getResources().getColor(R.color.white));
////                tvDayNumber.setTextColor(getResources().getColor(R.color.white));
////                dayDot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
//                Object selectedItem = vehicleTypeSpinner.getSelectedItem();
//                String selectedName = (selectedItem != null && !selectedItem.toString().isEmpty()) ? selectedItem.toString() : null;
//                Integer selectedVehicleId = null;
//                if (selectedName != null && vehicleTypeMap.containsKey(selectedName)) {
//                    selectedVehicleId = vehicleTypeMap.get(selectedName);
//                }
//                String selectedDate = (String) v.getTag();
//                currentPage = 1;
//                isLastPage = false;
//                tripList.clear();
//                adapter.clear();
//                recyclerView.setAdapter(adapter);
//                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
//                fetchTrips(currentPage, lastFromCityId, lastToCityId, selectedDate, passengers[0], checkboxFamiliesOnly.isChecked() ? 1 : 0, selectedVehicleId, null);
//            });
//
//            calendar.add(Calendar.DAY_OF_MONTH, 1);
//            weekContainer.addView(dayView);
//        }
        final String[] finalSelectedDate = {(lastDate != null && !lastDate.isEmpty()) ? lastDate : new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date())};
        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
//        selectedDateText.setText(finalSelectedDate[0]);

//        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
//        final String[] finalSelectedDate = {new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date())}; // افتراضياً تاريخ اليوم

//        selectedDateText.setText(finalSelectedDate[0]);
// --- استعادة الأسماء المخزنة سابقاً ---
//        if (lastFromCityName != null && !lastFromCityName.isEmpty()) {
//            textFromCity.setText(lastFromCityName);
//        } else {
//            // إذا لم يوجد بحث سابق، نضع المدينة الافتراضية من الشيرد بريفرنس
//            textFromCity.setText(prefs.getString("default_city", "حدد المدينة"));
//        }
//
//        if (lastToCityName != null && !lastToCityName.isEmpty()) {
//            textToCity.setText(lastToCityName);
//        }
        dateSelectorLayout.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            // محاولة تحليل التاريخ النصي إلى كائن Calendar
            try {
                SimpleDateFormat sdfParser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Date d = sdfParser.parse(finalSelectedDate[0]);
                if (d != null) cal.setTime(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.getTime());
                finalSelectedDate[0] = formattedDate;
                selectedDateText.setText(formattedDate);

                lastDate = formattedDate;

            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

            datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
            datePicker.show();
        });
// داخل showCustomSearchDialog

// --- 1. التاريخ ---
// نتحقق إذا كان هناك تاريخ بحث سابق (lastDate)، وإلا نستخدم تاريخ اليوم


// --- 2. المدن ---
// نضع النصوص المخزنة سابقاً في حقول المدن (يجب أن تكون قد خزنت الأسماء في متغيرات مثل lastFromCityName)
//        if (lastFromCityName != null) textFromCity.setText(lastFromCityName);
//        if (lastToCityName != null) textToCity.setText(lastToCityName);

// --- 3. بقية الفلاتر ---
        checkboxFamiliesOnly.setChecked(lastFamiliesOnly);
// عدد الركاب (أنت قمت بها بالفعل في كودك)
        passengers[0] = lastPassengers > 0 ? lastPassengers : 1;
        passengersTextView.setText(String.valueOf(passengers[0]));
        ViewGroup decorView = (ViewGroup) requireActivity().getWindow().getDecorView();
        Blurry.with(requireContext()).radius(15).sampling(2).onto(decorView);
        btnResetFilter.setOnClickListener(v -> {
            lastDate = null;
            lastFromCityId = null;
            lastToCityId = null;

            lastFromCityName = null;
            lastToCityName = null;

            lastPassengers = 1;
            lastFamiliesOnly = false;

            currentPage = 1;
            isLastPage = false;
            tripList.clear();
            if (adapter != null) adapter.clear();

            fetchTrips(1, null, null, null, 1, 0, null, defaultCityId, null);
            dialog.dismiss();
        });
        dialog.setOnDismissListener(d -> Blurry.delete(decorView));
        // --- الإغلاق ---
        dialogCancelButton.setOnClickListener(v -> dialog.dismiss());
        dialogSearchButton.setOnClickListener(v -> {
            lastDate = finalSelectedDate[0];
            lastPassengers = passengers[0];
            lastFamiliesOnly = checkboxFamiliesOnly.isChecked();
            int passengersCount = passengers[0];

            // --- خيار العوائل ---
            int familiesOnly = checkboxFamiliesOnly.isChecked() ? 1 : 0;

            // --- نوع المركبة ---
            Object selectedItem = vehicleTypeSpinner.getSelectedItem();
            String selectedName = (selectedItem != null && !selectedItem.toString().isEmpty()) ? selectedItem.toString() : null;
            Integer selectedVehicleId = null;
            if (selectedName != null && vehicleTypeMap.containsKey(selectedName)) {
                lastVehicleId = vehicleTypeMap.get(selectedName); // حفظه للمرة القادمة
            } else {
                lastVehicleId = null;
            }
//            lastFromCityName = textFromCity.getText().toString();
//            lastToCityName = textToCity.getText().toString();
            String date = finalSelectedDate[0];
            currentPage = 1;
            isLastPage = false;
            tripList.clear();
            adapter.clear();
            recyclerView.setAdapter(adapter);
            String selectedCompanyName = companySpinner.getSelectedItem().toString();
            lastCompanyId = companyMap.get(selectedCompanyName);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
//            fetchTrips(currentPage, lastFromCityId, lastToCityId, date, passengersCount, familiesOnly, selectedVehicleId, null);
            fetchTrips(currentPage, lastFromCityId, lastToCityId, date, lastPassengers,
                    familiesOnly, selectedVehicleId, null, lastCompanyId);
            dialog.dismiss();
        });

//        dialogSearchButton.setOnClickListener(v -> {
//
//            dialog.dismiss();
//            fetchTrips(currentPage, lastFromCityId, lastToCityId, null, passengers[0], checkboxFamiliesOnly.isChecked() ? 1 : 0, null);
//        });
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);

        dialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomePage) {
            SharedPreferences prefs = SharedPrefsHelper.get(getContext());

            if (lastFromCityId == null || lastFromCityId == -1) {
                defaultCityId = prefs.getInt("default_city_id", -1);
                lastFromCityId = defaultCityId;
            }


            if (tripList != null && !tripList.isEmpty()) {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
//                updateUIFields();
            } else {
                fetchTrips(1, lastFromCityId, lastToCityId, lastDate,
                        lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId, null);
            }

            ActionBar actionBar = ((HomePage) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

//    private void updateUIFields() {
//        if (lastDate != null && !lastDate.isEmpty()) {
//            editTextDate.setText(lastDate);
//        }
//        // تأكد من وضع اسم المدينة المختارة في الـ fromCity EditText
//        // يمكنك جلب الاسم من DBHelper بناءً على lastFromCityId
//    }
}