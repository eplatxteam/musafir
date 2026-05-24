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
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
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

import javax.security.auth.callback.Callback;

import jp.wasabeef.blurry.Blurry;

public class HomeFragment extends Fragment {
    private String nextTripsUrl = null;
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
    int visa_required;
    int company_no;
    String driver_id, carCode, carCodes, carCodesId, LocationTrip, DateTrip;

//    RadioGroup toCityRadioGroup;

    private LinearLayout fromLayout, toLayout;
    LottieAnimationView lottieWave;
    //    RadioGroup countriesRadioGroup;
    LinearLayout filterIcon;
    TextView textFromCity, textToCity;
    private String tripType;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView carIcon;
    private String latitude;
    private String longitude;
    int defaultCityId;
    private int selectedCountryId = -1;
    int v_reception_car = 0;
    Spinner vehicleTypeSpinner, companySpinner;
    List<String> vehicleTypeNames = new ArrayList<>();
    Map<String, Integer> vehicleTypeMap = new HashMap<>();
    private Integer vehicleTypeFromArgs = null;
    List<String> companyNames = new ArrayList<>();
    Map<String, Integer> companyMap = new HashMap<>();
    private Integer lastCompanyId = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.recyclerViewTrips);
        carIcon = view.findViewById(R.id.carIcon);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

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
        DBHelper dbHelper = new DBHelper(getContext());

        TextView textnodata = view.findViewById(R.id.textnodata);
        textnodata.setText(UserUtils.getMessageFromLocalNew(404, dbHelper));
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
                    getCityNameFromIp(getContext(), (cityAr, cityId, country_code) -> {
                        prefs.edit().putString("default_city", cityAr).apply();
                        prefs.edit().putString("country_code", country_code).apply();
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
                                       int passport_requireds, int visa_requireds, int p_company_no,String dateTimeString) {
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
                    visa_required = visa_requireds;
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
                            Fragment custombookings = new CustomBooking();
                            Bundle args = new Bundle();
                            args.putString("car_code", carCode);
                            args.putInt("trip_id", tripId2);
                            args.putInt("pricePerSeat", pricePerSeat);
                            args.putInt("pickup_orders", pickupOrders);
                            args.putInt("dropoff_orders", dropoffOrders);
                            args.putInt("passenger_id", passengerId);
                            args.putString("DateTrip", dateTimeString );
                            args.putString("carCodes", carCodes);
                            args.putString("driver_id", driver_id);
                            args.putString("car_codes_id", carCodesId);
                            args.putInt("discountPricePerSeat", discountPricePerSeat);
                            args.putInt("availableSeats", seats);
                            args.putInt("passport_required", passport_required);
                            args.putInt("visa_required", visa_required);
                            args.putInt("company_no", company_no);
                            args.putInt("v_reception_car", v_reception_car);
                            args.putString("location_trip", LocationTrip);

                            custombookings.setArguments(args);
                            ((HomePage) getContext()).openFullScreenFragment(custombookings, "حجز رحلة", R.drawable.booking, 2);
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
                                    textView.setTextSize(16);
                                    textView.setPadding(32, 32, 32, 32);

                                    Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
                                    textView.setTypeface(typeface);

                                    textView.setGravity(Gravity.CENTER);
                                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                                    TextView titleView = new TextView(context);
                                    titleView.setText("المسافرون في الرحلة");
                                    titleView.setTextSize(20);
                                    titleView.setTypeface(typeface);
                                    titleView.setTextColor(ContextCompat.getColor(context, R.color.primary2));
                                    textView.setTextColor(ContextCompat.getColor(context, R.color.text));
                                    titleView.setGravity(Gravity.CENTER);
                                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    titleView.setPadding(16, 16, 16, 16);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setCustomTitle(titleView)
                                            .setView(textView)
                                            .setPositiveButton("حسناً", null);

                                    AlertDialog dialog = builder.create();

                                    if (dialog.getWindow() != null) {
                                        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
                                    }
                                    dialog.show();

                                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
//                                    params.width = MATCH_PARENT;
//                                    positiveButton.setLayoutParams(params);
//                                    positiveButton.setGravity(Gravity.CENTER);
//                                    positiveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.primary2));
//                                    positiveButton.setTextColor(ContextCompat.getColor(context, R.color.white));
//                                    positiveButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//                                    positiveButton.setTextSize(20);
//                                    positiveButton.setPadding(16, 16, 16, 16);

                                    positiveButton.setAllCaps(false);
                                    positiveButton.setTypeface(typeface);
                                    positiveButton.setTextSize(18);
                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                                    params.width = MATCH_PARENT;

                                    positiveButton.setBackgroundColor(ContextCompat.getColor(context, R.color.primary2));
                                    positiveButton.setTextColor(ContextCompat.getColor(context, R.color.white));
                                    params.setMargins(40, 10, 40, 10);
                                    positiveButton.setLayoutParams(params);
                                    positiveButton.setMinimumHeight(120);
                                    positiveButton.setGravity(Gravity.CENTER);
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

//            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                    super.onScrolled(recyclerView, dx, dy);
//                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                    if (layoutManager == null) return;
//                    int totalItemCount = layoutManager.getItemCount();
//                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
//                    if (firstLoadDone && !isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 2) {
//                        recyclerView.post(() -> fetchTrips(currentPage + 1, lastFromCityId, lastToCityId, lastDate,
//                                lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId, null));
//                    } else {
//                    }
//                }
//            });
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    // التحقق من أن المستخدم يسحب للأسفل فقط
                    if (dy <= 0) return;

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int totalItemCount = layoutManager.getItemCount();
                        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                        // إذا شارف المستخدم على الوصول لنهاية القائمة ولم يكن هناك تحميل مسبق وليست الصفحة الأخيرة
                        if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 2) {

                            // تفعيل حالة التحميل لعدم تكرار الطلب
                            isLoading = true;

                            // إظهار اللودر أسفل الريسايكلر
                            recyclerView.post(() -> adapter.addLoadingFooter());

                            // جلب الصفحة التالية
                            int nextPage = currentPage + 1;
                            recyclerView.post(() -> fetchTrips(nextPage, lastFromCityId, lastToCityId, lastDate,
                                    lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId, null));

                        }
                    }
                }
            });

//            fetchTrips(1, null, lastToCityId, lastDate, lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId, null);

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

        DBHelper db = new DBHelper(getContext());
        List<DBHelper.City> allCitiesFromDB = db.getAllCities2();

        List<DBHelper.City> allCities = new ArrayList<>();

        int type = (currentTripType != null) ? Integer.parseInt(currentTripType) : 0;
        Integer otherCityId = isFromCity ? lastToCityId : lastFromCityId;
        for (DBHelper.City city : allCitiesFromDB) {
            if (otherCityId != null && city.getId() == (int)otherCityId) {
                continue;
            }
            if (type == 3) {
                if (city.getCountryId() == 1) {
                    allCities.add(city);
                }
            } else if (type == 2) {
//                if (!isFromCity) {
//                    if (city.getCountryId() == 1) {
                        allCities.add(city);
//                    }
//                } else {
                    // "من": المدن المحلية فقط
//                    if (city.getCountryId() == 1) {
//                    allCities.add(city);
//                    }
//                }
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
        Context context = getContext();
        if (context == null || !isAdded()) return;
        lastToCityId = null;
        lastFromCityId = defaultCityId;
        lastVehicleId = null;
//        familiesOnly = 0;
        lastPassengers = 1;
        if (defaultCityId != 0) {
            String defaultCityName = dbHelper.getCityNameById(defaultCityId);
            if (defaultCityName != null) {
                textFromCity.setText(defaultCityName);
            }
        } else {
            textFromCity.setText("حدد المدينة");
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        lastDate = sdf.format(calendar.getTime());
        textToCity.setText("حدد المدينة");
        if (!isLoading) {
            lottieWave.setVisibility(View.VISIBLE);
            lottieWave.playAnimation();
        }

        UserUtils.checkAppUpdate(context);
        UserUtils.app_Pages(context);
        UserUtils.fetchBalance(getContext());

        UserUtils.fetchServiceHome(context, dbHelper, new PageHome.OnServiceHomeFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.ServiceHome> types) {
            }

            @Override
            public void onError(String error) {
                if (isAdded() && getActivity() != null) {
                    UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
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
        UserUtils.fetchAndSaveContactInfo(context, dbHelper);

        UserUtils.loadVehicleTypesToDB(context);

        UserUtils.fetchCashBankData(getContext(), dbHelper, new UserUtils.OnCashBankFetchedListener() {
            @Override
            public void onFetched(List<DBHelper.CashBank> types) {
            }

            @Override
            public void onError(String error) {

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

            }
        });
        UserUtils.syncDayTimesFromServer(context, new UserUtils.DayTimeCallback() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String error) {

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

            }
        });

        UserUtils.fetchCodeDetails(context, 5, null, new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {

            }
        });
        UserUtils.fetchAndSavePayTypes(context, new UserUtils.GenericCallback() {

            @Override
            public void onSuccess(String message) {
            }

            @Override
            public void onError(String error) {

            }
        });
        UserUtils.fetchCompany(context, new UserUtils.OnCodesFetchedListener() {
            @Override
            public void onFetched(JSONArray response) {
            }

            @Override
            public void onError(String error) {

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
            fetchTrips(currentPage, null, lastToCityId, null,
                    lastPassengers, lastFamiliesOnly ? 1 : 0, null, null, null);

        });


        LinearLayout weekContainer = rootView.findViewById(R.id.week_container);

        Calendar calendar = Calendar.getInstance(new Locale("en"));
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ar"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", new Locale("en"));
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        HorizontalScrollView daysScroll = rootView.findViewById(R.id.days_scroll_view);

        daysScroll.post(() -> {
            daysScroll.fullScroll(View.FOCUS_RIGHT);
        });
        daysScroll.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    swipeRefreshLayout.setEnabled(false);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    swipeRefreshLayout.setEnabled(true);
                    break;
            }
            return false;
        });
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        LinearLayout[] dayViews = new LinearLayout[14];
//        LayoutInflater inflater;
        weekContainer.removeAllViews();
        for (int i = 0; i < 14; i++) {
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
//            dayView.setOnClickListener(v -> {
//                boolean alreadySelected = v.isSelected();
//
//                String dateToSend = alreadySelected ? null : (String) v.getTag();
//
//                for (int j = 0; j < 14; j++) {
//                    dayViews[j].setSelected(false);
//                    ((TextView) dayViews[j].findViewById(R.id.day_name)).setTextColor(getResources().getColor(R.color.secondary));
//                    ((TextView) dayViews[j].findViewById(R.id.day_number)).setTextColor(getResources().getColor(R.color.secondary));
//                    dayViews[j].findViewById(R.id.day_dot).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.secondary)));
//                }
//
//                if (!alreadySelected) {
//                    v.setSelected(true);
//                    tvDayName.setTextColor(getResources().getColor(R.color.secondary));
//                    tvDayNumber.setTextColor(getResources().getColor(R.color.secondary));
//                    dayDot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.secondary)));
//                }
////                if (!alreadySelected) {
////                    v.setSelected(true);
////                    tvDayName.setTextColor(getResources().getColor(R.color.primary)); // لون مختلف للمختار
////                    tvDayNumber.setTextColor(getResources().getColor(R.color.primary));
////                    dayDot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));
////                    dayDot.setVisibility(View.VISIBLE); // إظهار النقطة للمختار فقط مثلاً
////                }
//                currentPage = 1;
//                isLastPage = false;
//                tripList.clear();
//                adapter.notifyDataSetChanged();
//
//                fetchTrips(currentPage, lastFromCityId, lastToCityId, dateToSend,
//                        lastPassengers, lastFamiliesOnly ? 1 : 0, null, null, null);
//            });
            dayView.setOnClickListener(v -> {
                boolean alreadySelected = v.isSelected();
                String dateToSend = alreadySelected ? null : (String) v.getTag();
                lastDate = dateToSend;

                // 1. إعادة تعيين ألوان وحالة جميع الأيام إلى الوضع غير المختار
                for (int j = 0; j < weekContainer.getChildCount(); j++) {
                    View child = weekContainer.getChildAt(j);
                    child.setSelected(false);

                    TextView name = child.findViewById(R.id.day_name);
                    TextView number = child.findViewById(R.id.day_number);
                    View dot = child.findViewById(R.id.day_dot);

                    name.setTextColor(getResources().getColor(R.color.secondary));
                    number.setTextColor(getResources().getColor(R.color.secondary));
                    dot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.secondary)));
                }

                // 2. إذا لم يكن العنصر مختاراً مسبقاً، قم بتحديده الآن
                if (!alreadySelected) {
                    v.setSelected(true);
                    tvDayName.setTextColor(getResources().getColor(R.color.black)); // لون المختار
                    tvDayNumber.setTextColor(getResources().getColor(R.color.black));
                    dayDot.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                }

                // 3. تحديث البيانات والطلب من السيرفر
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


        UserUtils.fetchAndSavecities(getContext(), new UserUtils.citiesCallback() {
            @Override
            public void onSuccess(String message) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("messages_fetched", true);
                editor.apply();
            }

            @Override
            public void onError(String error) {

            }
        });

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

        UserUtils.getPublicIp((ipJson, city_id, country_code) ->

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

        filterIcon.setOnClickListener(new UserUtils.SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showCustomSearchDialog();
            }
        });
    }

    private void fetchTrips(int page,
                            @Nullable Integer fromCityId,
                            @Nullable Integer toCityId,
                            @Nullable String date,
                            @Nullable Integer passengers,
                            @Nullable Integer familiesOnly,
                            @Nullable Integer vehicle_type,
                            @Nullable Integer defaultCityId,
                            @Nullable Integer lastCompanyId) {

        if (!isAdded() || getActivity() == null) return;

        if (page == 1) {
            if (isLoading) return;
            isLoading = true;
            currentPage = 1;
            isLastPage = false;
            tripList.clear();

            requireActivity().runOnUiThread(() -> {
                adapter.clear();
                noDataText.setVisibility(View.GONE);
                noInternet.setVisibility(View.GONE);
                lottieWave.setVisibility(View.VISIBLE);
                lottieWave.playAnimation();
                recyclerView.setVisibility(View.VISIBLE);
            });
        }

        if (!UserUtils.isNetworkAvailable(requireContext())) {
            requireActivity().runOnUiThread(() -> {
                if (page == 1) {
                    if (lottieWave.isAnimating()) lottieWave.cancelAnimation();
                    lottieWave.setVisibility(View.GONE);
                    noInternet.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    adapter.removeLoadingFooter();
                    UserUtils.ToastMessages(getActivity(), "لا يوجد اتصال بالإنترنت للمزيد من الرحلات");
                }
                swipeRefreshLayout.setRefreshing(false);
            });
            isLoading = false;
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                SharedPreferences sharedPreferences = SharedPrefsHelper.get(getContext());
                String userType = sharedPreferences.getString("user_type", "");
                int driverId = sharedPreferences.getInt("user_id", -1);

                StringBuilder urlBuilder;

                if (page > 1 && nextTripsUrl != null && !nextTripsUrl.equals("null")) {
                    urlBuilder = new StringBuilder(nextTripsUrl);
                } else {
                    if ("driver".equals(userType)) {
                        urlBuilder = new StringBuilder(BASE_URL + "trip-list/?driver_id=" + driverId);
                    } else {
                        urlBuilder = new StringBuilder(BASE_URL + "trip-list/?page=" + page + "&limit=5");
                    }

                    urlBuilder.append("&trip_type=").append(tripType);

                    if (defaultCityId != null)
                        urlBuilder.append("&start_city_order=").append(defaultCityId);
                    if (latitude != null && longitude != null)
                        urlBuilder.append("&latitude=").append(latitude).append("&longitude=").append(longitude);
                    if (fromCityId != null && fromCityId > 0)
                        urlBuilder.append("&start_city=").append(fromCityId);
                    if (toCityId != null && toCityId > 0)
                        urlBuilder.append("&end_city=").append(toCityId);
                    if (date != null && !date.isEmpty())
                        urlBuilder.append("&start_date=").append(date);
                    if (passengers != null && passengers > 1)
                        urlBuilder.append("&seats=").append(passengers);
                    if (familiesOnly != null && familiesOnly != 0)
                        urlBuilder.append("&family=").append(familiesOnly);
                    if (vehicle_type != null)
                        urlBuilder.append("&vehicle_type_id=").append(vehicle_type);
                    if (lastCompanyId != null)
                        urlBuilder.append("&company_no=").append(lastCompanyId);
                }
                URL url = new URL(urlBuilder.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("Accept", "application/json");
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
                    JSONArray resultsArray = jsonObject.optJSONArray("results");
                    List<JSONObject> newTrips = new ArrayList<>();

                    if (resultsArray != null) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            JSONObject trip = resultsArray.getJSONObject(i);

                            // فلترة inactive للركاب
                            if (!"driver".equals(userType)) {
                                int inactive = trip.optInt("inactive", 1);
                                if (inactive == 0) continue;
                            }
                            newTrips.add(trip);
                        }
                    }
                    nextTripsUrl = jsonObject.optString("next", null);
                    if (jsonObject.isNull("next") || nextTripsUrl == null || nextTripsUrl.equals("null")) {
                        isLastPage = true;
                    } else {
                        isLastPage = false;
                    }
                    boolean reachedLastPage = jsonObject.isNull("next");

                    requireActivity().runOnUiThread(() -> {
                        if (page > 1) {
                            adapter.removeLoadingFooter();
                        } else {
                            lottieWave.cancelAnimation();
                            lottieWave.setVisibility(View.GONE);
                        }

                        swipeRefreshLayout.setRefreshing(false);
                        if (newTrips.isEmpty() && page == 1) {
                            recyclerView.setVisibility(View.GONE);
                            noDataText.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            noDataText.setVisibility(View.GONE);
                            adapter.addTrips(newTrips);
                            currentPage = page;
                            isLastPage = reachedLastPage;
                        }
                        isLoading = false;
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        if (page > 1) adapter.removeLoadingFooter();
                        else {
                            recyclerView.setVisibility(View.GONE);
                            noDataText.setVisibility(View.VISIBLE);
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Activity activity = getActivity();

                if (activity == null || !isAdded()) {
                    return;
                }

                activity.runOnUiThread(() -> {
                    if (!isAdded() || getView() == null) return;

                    if (page > 1) {
                        adapter.removeLoadingFooter();
                    } else {
                        lottieWave.cancelAnimation();
                        lottieWave.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        noInternet.setVisibility(View.VISIBLE);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                    isLoading = false;
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }



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
                    currentPage = 1;
                    isLastPage = false;
                    tripList.clear();
                    adapter.clear();

                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

//                    fetchTrips(currentPage, null, lastToCityId, lastDate, lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId);
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void loadCompanies(String triptype) {
        DBHelper dbHelper = new DBHelper(getContext());

        companyNames.clear();
        companyMap.clear();

        companyNames.add("اختر الشركة");
        companyMap.put("", -1);

        List<Map<String, Object>> companies = dbHelper.getAllCompanies(triptype);

        for (Map<String, Object> c : companies) {
            String name = (String) c.get("company_name");
            int id = (int) c.get("company_no");
            companyNames.add(name);
            companyMap.put(name, id);
        }

        if (isAdded()) {
            // تخصيص الـ Adapter لتغيير لون النص
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                    android.R.layout.simple_spinner_item, companyNames) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    if (position == 0) {
                        ((TextView) v).setTextColor(Color.GRAY);
                    } else {
                        ((TextView) v).setTextColor(Color.BLACK); // أو اللون الافتراضي لديك
                    }
                    return v;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View v = super.getDropDownView(position, convertView, parent);
                    if (position == 0) {
                        ((TextView) v).setTextColor(Color.GRAY);
                    } else {
                        ((TextView) v).setTextColor(Color.BLACK);
                    }
                    return v;
                }
            };

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

        vehicleTypeNames.add("اختر المركبة");
        vehicleTypeMap.put("", -1);

        List<DBHelper.VehicleType> vehicleTypes = dbHelper.getVehicleTypes(1, tripType);

        for (DBHelper.VehicleType v : vehicleTypes) {
            vehicleTypeNames.add(v.getName());
            vehicleTypeMap.put(v.getName(), v.getId());
        }

        if (isAdded()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                    android.R.layout.simple_spinner_item, vehicleTypeNames) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    if (position == 0) {
                        ((TextView) v).setTextColor(Color.GRAY);
                    } else {
                        ((TextView) v).setTextColor(Color.BLACK);
                    }
                    return v;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View v = super.getDropDownView(position, convertView, parent);
                    if (position == 0) {
                        ((TextView) v).setTextColor(Color.GRAY);
                    } else {
                        ((TextView) v).setTextColor(Color.BLACK);
                    }
                    return v;
                }
            };

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
        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
        if("1".equals(tripType)){
            checkboxFamiliesOnly.setVisibility(View.VISIBLE);
        }else {
            checkboxFamiliesOnly.setVisibility(View.GONE);
        }

        Dialog dialog = new Dialog(requireContext(), R.style.TransparentBottomDialog);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(MATCH_PARENT, WRAP_CONTENT); // لتسهيل الحجم مؤقتاً وضمان عدم الخطأ بحسبة البكسل
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        loadCompanies(tripType);
        loadVehicleTypes();

        if (lastVehicleId != null && vehicleTypeMap != null) {
            vehicleTypeSpinner.post(() -> {
                for (int i = 0; i < vehicleTypeSpinner.getCount(); i++) {
                    Object item = vehicleTypeSpinner.getItemAtPosition(i);
                    if (item != null) {
                        String name = item.toString();
                        Integer currentId = vehicleTypeMap.get(name);
                        if (currentId != null && currentId.equals(lastVehicleId)) {
                            vehicleTypeSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            });
        }
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

//        Dialog dialog = new Dialog(requireContext(), R.style.TransparentBottomDialog);
//        dialog.setContentView(dialogView);

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


        final String[] finalSelectedDate = {(lastDate != null && !lastDate.isEmpty()) ? lastDate : new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date())};
//        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
        dateSelectorLayout.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
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
        checkboxFamiliesOnly.setChecked(lastFamiliesOnly);
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
        dialogCancelButton.setOnClickListener(v -> dialog.dismiss());
        dialogSearchButton.setOnClickListener(v -> {
            lastDate = finalSelectedDate[0];
            lastPassengers = passengers[0];
            lastFamiliesOnly = checkboxFamiliesOnly.isChecked();
            int passengersCount = passengers[0];

            int familiesOnly = checkboxFamiliesOnly.isChecked() ? 1 : 0;

            Object selectedItem = vehicleTypeSpinner.getSelectedItem();
            String selectedName = (selectedItem != null && !selectedItem.toString().isEmpty()) ? selectedItem.toString() : null;
            Integer selectedVehicleId = null;
            if (selectedName != null && vehicleTypeMap.containsKey(selectedName)) {
                lastVehicleId = vehicleTypeMap.get(selectedName); // حفظه للمرة القادمة
            } else {
                lastVehicleId = null;
            }
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

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);

//        dialog.show();
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
                fetchTrips(1, null, lastToCityId, lastDate,
                        lastPassengers, lastFamiliesOnly ? 1 : 0, null, defaultCityId, null);
            }

            ActionBar actionBar = ((HomePage) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

}