package com.example.musafir;

import static com.example.musafir.LocationWorker.getCityNameFromIp;
import static com.example.musafir.R.drawable.radio_button_text_color;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.TimePickerDialog;
import android.widget.TimePicker;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import jp.wasabeef.blurry.Blurry;

public class AddTripFragment extends Fragment {

    Spinner routeSpinner;
    EditText departureTime, fromAddress, toAddress,
            price, availableSeats, notes;
    Button submitBtn;
    Spinner vehicleSpinner;

    String BASE_URL = UserUtils.BASE_URL;
    int selectedRouteId = -1;

    ArrayList<String> vehicleNames = new ArrayList<>();
    ArrayList<Integer> vehicleIds = new ArrayList<>();

    ArrayList<String> cityNames = new ArrayList<>();
    ArrayList<Integer> cityIds = new ArrayList<>();

    ArrayList<String> dayNames = new ArrayList<>();
    ArrayList<Integer> dayIds = new ArrayList<>();

    RadioGroup dayTimeRadioGroup;
    HorizontalScrollView dayTimeScrollView;
    ScrollView scrollViewcon;
    private Integer lastFromCityId = null;
    private Integer lastToCityId = null;
    private List<Integer> vehicleSeats = new ArrayList<>();
    private List<Integer> vh_price = new ArrayList<>();

    private List<String> Car_codes = new ArrayList<>();
    ProgressBar routeLoading;
    private boolean isDataChanged = false;

    public AddTripFragment() {
    }

    View firstErrorView = null;

    TextView carCode, routeError, vehicleError, fromCityError, toCityError, fromAddressError, toAddressError, departureTimeError, dayTimeError, priceError, availableSeatsError;
    TextView textFromCity, textToCity;
    private LinearLayout fromLayout, toLayout;
    String selectedDate;
    int selectedDayId = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_trip, container, false);
        setHasOptionsMenu(true);
        dayTimeRadioGroup = view.findViewById(R.id.dayTimeRadioGroup);
        carCode = view.findViewById(R.id.carCode);


        routeSpinner = view.findViewById(R.id.routeSpinner);
        departureTime = view.findViewById(R.id.departureTime);
        fromAddress = view.findViewById(R.id.fromAddress);
        toAddress = view.findViewById(R.id.toAddress);
        price = view.findViewById(R.id.price);
        availableSeats = view.findViewById(R.id.availableSeats);
        notes = view.findViewById(R.id.notes);

        submitBtn = view.findViewById(R.id.submitBtn);
        dayTimeScrollView = view.findViewById(R.id.dayTimeScrollView);
        scrollViewcon = view.findViewById(R.id.scrollViewcon);


        routeError = view.findViewById(R.id.routeError);
        vehicleError = view.findViewById(R.id.vehicleError);
        fromCityError = view.findViewById(R.id.fromCityError);
        toCityError = view.findViewById(R.id.toCityError);
        fromAddressError = view.findViewById(R.id.fromAddressError);
        toAddressError = view.findViewById(R.id.toAddressError);
        departureTimeError = view.findViewById(R.id.departureTimeError);
        dayTimeError = view.findViewById(R.id.dayTimeError);
        priceError = view.findViewById(R.id.priceError);
        availableSeatsError = view.findViewById(R.id.availableSeatsError);

        textFromCity = view.findViewById(R.id.textFromCity);
        textToCity = view.findViewById(R.id.textToCity);
        fromLayout = view.findViewById(R.id.fromLayout);
        toLayout = view.findViewById(R.id.toLayout);
        fromLayout.setOnClickListener(v ->
                showCityDialog(true, textFromCity)
        );

        toLayout.setOnClickListener(v ->
                showCityDialog(false, textToCity)
        );
        ImageView carIcons = view.findViewById(R.id.carIcon);

        carIcons.setOnClickListener(v -> {
            String fromCity = textFromCity.getText().toString().trim();
            String toCity = textToCity.getText().toString().trim();

            if (fromCity.equals("حدد المدينة") || toCity.equals("حدد المدينة")) {
                return;
            }
            textFromCity.setText(toCity);
            textToCity.setText(fromCity);
        });

        carIcons.setOnTouchListener((v, event) -> {
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
        LinearLayout weekContainer = view.findViewById(R.id.week_containerAddtrip);
        Calendar calendar = Calendar.getInstance(new Locale("ar"));
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ar"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", new Locale("ar"));
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        int today = calendar.get(Calendar.DAY_OF_MONTH);
        LinearLayout[] dayViews = new LinearLayout[14];

        for (int i = 0; i < 14; i++) {
            View dayView = inflater.inflate(R.layout.week_days, weekContainer, false);
            dayViews[i] = (LinearLayout) dayView;

            TextView tvDayName = dayView.findViewById(R.id.day_name);
            TextView tvDayNumber = dayView.findViewById(R.id.day_number);
            View dayDot = dayView.findViewById(R.id.day_dot);

            // نأخذ نسخة مستقلة من التقويم لهذا اليوم
            Calendar tempCal = (Calendar) calendar.clone();

            // نولّد اليوم والتاريخ الكامل
            String dayName = dayFormat.format(tempCal.getTime());
            String dayNumber = dateFormat.format(tempCal.getTime());
            String fullDate = fullDateFormat.format(tempCal.getTime()); // مثل 2025-11-12

            // حفظ التاريخ في الـ Tag
            dayView.setTag(fullDate);

            tvDayName.setText(dayName);
            tvDayNumber.setText(dayNumber);

            if (Integer.parseInt(dayNumber) == today) {
                dayView.setSelected(true);
                tvDayName.setTextColor(getResources().getColor(R.color.secondary));
                tvDayNumber.setTextColor(getResources().getColor(R.color.secondary));
                dayDot.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.secondary)
                ));
                selectedDate = fullDate;
                updateDayAdapter();
            } else {
                tvDayName.setTextColor(getResources().getColor(R.color.secondary));
                tvDayNumber.setTextColor(getResources().getColor(R.color.secondary));
                dayDot.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.secondary)
                ));

            }

            dayView.setOnClickListener(v -> {
                for (int j = 0; j < 14; j++) {
                    dayViews[j].setSelected(false);
                    ((TextView) dayViews[j].findViewById(R.id.day_name))
                            .setTextColor(getResources().getColor(R.color.secondary));
                    ((TextView) dayViews[j].findViewById(R.id.day_number))
                            .setTextColor(getResources().getColor(R.color.secondary));
                    dayViews[j].findViewById(R.id.day_dot)
                            .setBackgroundTintList(ColorStateList.valueOf(
                                    getResources().getColor(R.color.secondary)
                            ));
                }

                dayView.setSelected(true);
                tvDayName.setTextColor(getResources().getColor(R.color.secondary));
                tvDayNumber.setTextColor(getResources().getColor(R.color.secondary));
                dayDot.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.secondary)
                ));

                selectedDate = (String) v.getTag();

                updateDayAdapter();
            });

            calendar.add(Calendar.DAY_OF_MONTH, 1);

            weekContainer.addView(dayView);
        }
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isDataChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
//        requireActivity().getOnBackPressedDispatcher().addCallback(
//                getViewLifecycleOwner(),
//                new OnBackPressedCallback(true) {
//                    @Override
//                    public void handleOnBackPressed() {
//                        ((HomePage) requireActivity()).selectTab(R.id.nav_home);
//                    }
//                }
//        );
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // استدعاء الديالوج المخصص عند محاولة الرجوع
                ExitConfirmationDialog dialog = new ExitConfirmationDialog();
                dialog.show(getParentFragmentManager(), "ExitDialog");
            }
        });
        fromAddress.addTextChangedListener(watcher);
        toAddress.addTextChangedListener(watcher);
        price.addTextChangedListener(watcher);
        departureTime.addTextChangedListener(watcher);

        submitBtn.setOnClickListener(v -> {
            validateAndSendData();
        });
        departureTime.setOnClickListener(v -> showTimePicker());
        vehicleSpinner = view.findViewById(R.id.vehicleSpinner);

        dayTimeRadioGroup.removeAllViews();
        fetchRoutes();

        routeLoading = view.findViewById(R.id.routeLoading);
        UserUtils.setEditTextState(price, false);
        UserUtils.setEditTextState(departureTime, false);
        UserUtils.setEditTextState(availableSeats, false);
        UserUtils.setEditTextState(notes, false);
        UserUtils.setEditTextState(fromAddress, false);
        UserUtils.setEditTextState(toAddress, false);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId != -1) {
            fetchVehicles(userId);
        }


        fetchDayTime();
        vehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (vehicleSeats != null && vehicleSeats.size() > position) {
                    int seats = vehicleSeats.get(position);
                    availableSeats.setText(String.valueOf(seats));
                } else {
                    availableSeats.setText("0");
                }

                // ✅ عرض السعر عند اختيار المركبة
                if (vh_price != null && vh_price.size() > position) {
                    int priceValue = vh_price.get(position);
                    price.setText(String.valueOf(priceValue));
                } else {
                    price.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                availableSeats.setText("");
                price.setText("");
            }
        });


        // getCityNameFromIp(getContext(), cityAr -> {
        //     prefs.edit().putString("default_city", cityAr).apply();

        //     ((Activity) getContext()).runOnUiThread(() -> {
        //         textFromCity.setText(cityAr);
        DBHelper dbHelper = new DBHelper(getContext());
        //         DBHelper.City defaultCity = db.getCityByName(cityAr);
        //         if (defaultCity != null) {
        //             textFromCity.setTag(defaultCity.getId());
        //         }
        //     });
        // });
        String defaultCity = prefs.getString("default_city", "حدد المدينة");
        textFromCity.setText(defaultCity);
        DBHelper.City defaultCityObj = dbHelper.getCityByName(defaultCity);
        if (defaultCityObj != null) {
            textFromCity.setTag(defaultCityObj.getId());
            lastFromCityId = defaultCityObj.getId();
        }

        return view;
    }

    private void updateRoutesByCities() {

        int fromId = -1, toId = -1;

        if (textFromCity.getTag() != null)
            fromId = (int) textFromCity.getTag();

        if (textToCity.getTag() != null)
            toId = (int) textToCity.getTag();

        DBHelper dbHelper = new DBHelper(getContext());

        if (fromId != -1 && toId != -1 && fromId != toId) {

            List<DBHelper.RouteModel> list = dbHelper.searchRoutesByCities(fromId, toId);

            if (list.size() == 0) {
                ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_spinner_item,
                        new String[]{"لا توجد مسارات"}
                );
                routeSpinner.setAdapter(emptyAdapter);
                selectedRouteId = -1;
                carCode.setText("");
                return;
            }

            // تجهيز أسماء المسارات
            List<String> routeNamesList = new ArrayList<>();
            for (DBHelper.RouteModel route : list) {
                routeNamesList.add(route.name); // route_name

            }

            // تعبئة الـ Spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    routeNamesList
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            routeSpinner.setAdapter(adapter);

            // عند اختيار مسار
            routeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    DBHelper.RouteModel selected = list.get(position);

                    selectedRouteId = selected.id;       // حفظ route_id
                    carCode.setText(selected.carCode);   // إظهار car_code
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        } else {
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    new String[]{"اختر المسار"}
            );
            routeSpinner.setAdapter(emptyAdapter);
            selectedRouteId = -1;
            carCode.setText("");
        }
    }

    private void showCityDialog(boolean isFromCity, TextView targetTextView) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_city, null);

        // عناصر الديالوج
        EditText searchCity = dialogView.findViewById(R.id.searchBox);
        RecyclerView recyclerCities = dialogView.findViewById(R.id.recyclerCities);
        LinearLayout closeBtn = dialogView.findViewById(R.id.btnCloseDialog);

        // إنشاء الديالوج
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

            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        DBHelper db = new DBHelper(getContext());
        List<DBHelper.City> allCities = db.getAllCities2();

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

        recyclerCities.setLayoutManager(new LinearLayoutManager(getContext()));

        List<DBHelper.City> filtered = new ArrayList<>(allCities);

        CityAdapter adapter2 = new CityAdapter(filtered, city -> {

            // عند اختيار مدينة
            targetTextView.setText(city.getNameAr());
            targetTextView.setTag(city.getId());


            if (isFromCity)
                lastFromCityId = city.getId();
            else
                lastToCityId = city.getId();
            updateRoutesByCities();
            dialog.dismiss();
        });

        recyclerCities.setAdapter(adapter2);

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

    private void validateAndSendData() {
        firstErrorView = null;
        boolean hasError = false;
        // تحقق من المسار
        if (textFromCity.getText().toString().isEmpty() || textFromCity.getText().toString().equals("حدد المدينة")) {
            fromCityError.setText("يرجى اختيار من المدينة");
            fromCityError.setVisibility(View.VISIBLE);
            hasError = true;
            if (firstErrorView == null) firstErrorView = fromCityError;

        } else {
            fromCityError.setVisibility(View.GONE);
        }

        if (textToCity.getText().toString().isEmpty() || textToCity.getText().toString().equals("حدد المدينة")) {
            toCityError.setText("يرجى اختيار إلى المدينة");
            toCityError.setVisibility(View.VISIBLE);
            hasError = true;
            if (firstErrorView == null) firstErrorView = toCityError;
        } else {
            toCityError.setVisibility(View.GONE);
        }
        String selectedRouteName = "";
        if (routeSpinner.getSelectedItem() != null) {
            selectedRouteName = routeSpinner.getSelectedItem().toString();
        }

        if (selectedRouteId == -1 || selectedRouteName.equals("اختر المسار") || selectedRouteName.equals("لا توجد مسارات")) {
            routeError.setText("يرجى اختيار المسار");
            routeError.setVisibility(View.VISIBLE);
            hasError = true;
            if (firstErrorView == null) firstErrorView = routeError;
        } else {
            routeError.setVisibility(View.GONE);
        }
//        if (routeSpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION || routeSpinner.getSelectedItemPosition() == 0) {
//            routeError.setText("يرجى اختيار المسار");
//            routeError.setVisibility(View.VISIBLE);
//            hasError = true;
//            if (firstErrorView == null) firstErrorView = routeError;
//        } else {
//            routeError.setVisibility(View.GONE);
//        }

        // تحقق من المركبة
        if (vehicleSpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            vehicleError.setText("يرجى اختيار المركبة");

            vehicleError.setVisibility(View.VISIBLE);
            hasError = true;
            if (firstErrorView == null) firstErrorView = vehicleError;
        } else {
            vehicleError.setVisibility(View.GONE);
        }

        // تحقق من عنوان الانطلاق
        if (fromAddress.getText().toString().trim().isEmpty()) {
            fromAddressError.setText("يرجى إدخال عنوان الانطلاق");
            fromAddressError.setVisibility(View.VISIBLE);
            fromAddress.setError("يرجى إدخال عنوان الانطلاق");
            UserUtils.setEditTextState(fromAddress, true);
            hasError = true;
            if (firstErrorView == null) firstErrorView = fromAddressError;
        } else {
            fromAddressError.setVisibility(View.GONE);
            UserUtils.setEditTextState(fromAddress, false);

        }

        // تحقق من عنوان الوصول
        if (toAddress.getText().toString().trim().isEmpty()) {
            toAddressError.setText("يرجى إدخال عنوان الوصول");
            toAddressError.setVisibility(View.VISIBLE);
            toAddress.setError("يرجى إدخال عنوان الوصول");
            UserUtils.setEditTextState(toAddress, true);
            hasError = true;
            if (firstErrorView == null) firstErrorView = toAddressError;
        } else {
            toAddressError.setVisibility(View.GONE);
            UserUtils.setEditTextState(toAddress, false);
        }

        // تحقق من تاريخ الانطلاق
//        if (departureDate.getText().toString().trim().isEmpty()) {
//            departureDateError.setText("يرجى اختيار تاريخ الانطلاق");
//            departureDate.setError("يرجى اختيار تاريخ الانطلاق");
//            UserUtils.setEditTextState(departureDate, true);
//            departureDateError.setVisibility(View.VISIBLE);
//            hasError = true;
//            if (firstErrorView == null) firstErrorView = departureDateError;
//        } else {
//            departureDateError.setVisibility(View.GONE);
//            UserUtils.setEditTextState(departureDate, false);
//
//        }

        // تحقق من وقت الانطلاق
        if (departureTime.getText().toString().trim().isEmpty()) {
            departureTimeError.setText("يرجى اختيار وقت الانطلاق");
            departureTime.setError("يرجى اختيار وقت الانطلاق");
            UserUtils.setEditTextState(departureTime, true);
            departureTimeError.setVisibility(View.VISIBLE);
            hasError = true;
            if (firstErrorView == null) firstErrorView = departureTimeError;
        } else {
            departureTimeError.setVisibility(View.GONE);
            UserUtils.setEditTextState(departureTime, false);
        }

        // تحقق من الفترة
        if (dayTimeRadioGroup.getCheckedRadioButtonId() == -1) {
            dayTimeError.setText("يرجى اختيار الفترة");
            dayTimeError.setVisibility(View.VISIBLE);

            hasError = true;
            if (firstErrorView == null) firstErrorView = dayTimeError;
        } else {
            dayTimeError.setVisibility(View.GONE);
        }

        // تحقق من السعر
        if (price.getText().toString().trim().isEmpty()) {
            priceError.setText("يرجى إدخال السعر");
            price.setError("يرجى إدخال السعر");
            UserUtils.setEditTextState(price, true);
            priceError.setVisibility(View.VISIBLE);
            hasError = true;
            if (firstErrorView == null) firstErrorView = priceError;
        } else {
            priceError.setVisibility(View.GONE);
            UserUtils.setEditTextState(price, false);
        }

        // تحقق من عدد المقاعد
        if (availableSeats.getText().toString().trim().isEmpty()) {
            availableSeatsError.setText("يرجى إدخال عدد المقاعد");
            availableSeats.setError("يرجى إدخال عدد المقاعد");
            UserUtils.setEditTextState(availableSeats, true);
            availableSeatsError.setVisibility(View.VISIBLE);
            hasError = true;
            if (firstErrorView == null) firstErrorView = availableSeatsError;
        } else {
            availableSeatsError.setVisibility(View.GONE);
            UserUtils.setEditTextState(availableSeats, false);
        }

        if (dayTimeRadioGroup.getCheckedRadioButtonId() == -1) {
            dayTimeError.setVisibility(View.VISIBLE);
        } else {
            dayTimeError.setVisibility(View.GONE);
        }

        if (firstErrorView != null) {
            scrollViewcon.post(() -> scrollViewcon.smoothScrollTo(0, firstErrorView.getTop()));
        }

        if (!hasError) {
            sendTripData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false); // ❌ يخفي سهم الرجوع
        }
    }

    private AlertDialog exitDialog;

    @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists"})
    private void populateDayTimeRadioButtons(List<String> displayDays, Set<Integer> disabledPositions) {
        try {
            dayTimeRadioGroup.removeAllViews();

            // إنشاء ColorStateList لتغيير اللون حسب الحالة
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_enabled, android.R.attr.state_checked}, // checked
                    new int[]{android.R.attr.state_enabled}, // enabled but not checked
                    new int[]{-android.R.attr.state_enabled} // disabled
            };
            int[] colors = new int[]{
                    ContextCompat.getColor(getContext(), R.color.secondary),
                    ContextCompat.getColor(getContext(), R.color.text),
                    Color.GRAY
            };
            ColorStateList colorStateList = new ColorStateList(states, colors);

            for (int i = 0; i < displayDays.size(); i++) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(displayDays.get(i));
                radioButton.setTag(i + 1);
                radioButton.setId(View.generateViewId());
                radioButton.setButtonDrawable(null);
                radioButton.setPadding(45, 16, 45, 16);
                radioButton.setGravity(Gravity.CENTER);
                radioButton.setTextSize(16);

                if (disabledPositions.contains(i + 1)) {
                    radioButton.setEnabled(false);
                } else {
                    radioButton.setEnabled(true);
                }

                radioButton.setTextColor(colorStateList);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 10, 0);
                radioButton.setLayoutParams(params);
                radioButton.setBackgroundResource(R.drawable.radio_button_home);

                dayTimeRadioGroup.addView(radioButton);
            }

            dayTimeScrollView.post(() -> dayTimeScrollView.fullScroll(View.FOCUS_RIGHT));
        } catch (Exception e) {
            UserUtils.sendLog(getContext(), "populateDayTimeRadioButtons", e.toString(), e.toString(), "add trip");
//            throw new RuntimeException(e);
        }
    }

    private void updateDayAdapter() {
        if (selectedDate == null) return;
        selectedDayId = -1;
        dayTimeRadioGroup.removeAllViews();
        dayTimeRadioGroup.clearCheck();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

        List<String> displayDays = new ArrayList<>(dayNames);
        Set<Integer> disabledPositions = new HashSet<>();

        if (selectedDate.equals(todayDate)) {
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);

            for (int i = 0; i < displayDays.size(); i++) {
                String period = displayDays.get(i);
                try {
                    String[] parts = period.split(" - ");
                    if (parts.length >= 2) {
                        String endPart = parts[1].trim();
                        int endHour = Integer.parseInt(endPart.split(" ")[0]);
                        String ampm = endPart.contains("ص") ? "AM" : "PM";

                        if (ampm.equals("PM") && endHour < 12) endHour += 12;
                        if (ampm.equals("AM") && endHour == 12) endHour = 0;

                        if (endHour <= currentHour) {
                            displayDays.set(i, period);
                            disabledPositions.add(i);
                        }
                    }
                } catch (Exception e) {
                    UserUtils.sendLog(getContext(), "updateDayAdapter", e.toString(), e.toString(), "add trip");
//            throw new RuntimeException(e);
                }
            }
        }
        dayTimeRadioGroup.removeAllViews();
        dayTimeRadioGroup.clearCheck();

        // إنشاء RadioButtons بنفس طريقة ArrayAdapter
        populateDayTimeRadioButtons(displayDays, disabledPositions);

    }

    private void fetchDayTime() {
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "day-time");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());
//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                int responseCode = conn.getResponseCode();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream()));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                conn.disconnect();

                if (responseCode == 200) {
                    JSONArray resultsArray = new JSONArray(result.toString());

                    dayNames.clear();
                    dayIds.clear();

                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject obj = resultsArray.getJSONObject(i);
                        dayNames.add(obj.getString("dt_dsply"));
                        dayIds.add(obj.getInt("dt_no"));
                    }

                    getActivity().runOnUiThread(() -> {
                        updateDayAdapter(); // سيتم استدعاء populateDayTimeRadioButtons داخله
                    });

                } else {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.getMessageFromLocal(2, dbHelper, new UserUtils.MessageCallback() {
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
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                                UserUtils.getMessageFromLocal(2, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(getActivity(), message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                            }
                    );
                }

                UserUtils.sendLog(getContext(), "fetchDayTime", e.toString(), e.toString(), "add trip");
            }

        }).start();
    }

    private void fetchVehicles(int ownerId) {
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "vehicles/?owner=" + ownerId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());
//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
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
                    JSONObject jsonObject = new JSONObject(result.toString());
                    JSONArray resultsArray = jsonObject.getJSONArray("results");

                    vehicleNames.clear();
                    vehicleIds.clear();
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject vehicleObj = resultsArray.getJSONObject(i);
                        String name = vehicleObj.optString("make", "") + " - " + vehicleObj.optString("vehicle_type", "");
                        vehicleNames.add(name);
                        vehicleIds.add(vehicleObj.getInt("vehicle_id"));
                        vehicleSeats.add(vehicleObj.optInt("available_seats", 0)); // ← نخزن المقاعد
                        vh_price.add(vehicleObj.optInt("vh_price", 0)); // ← نخزن المقاعد
                    }
//                    for (int i = 0; i < resultsArray.length(); i++) {
//                        JSONObject vehicleObj = resultsArray.getJSONObject(i);
//                        String name = vehicleObj.optString("make", "") + " - " + vehicleObj.optString("model", "");
//                        vehicleNames.add(name);
//                        vehicleIds.add(vehicleObj.getInt("vehicle_id"));  // أو اسم الحقل المعرف حسب API
//                    }
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (vehicleNames.isEmpty()) {
                                vehicleNames.add("لا توجد مركبات");
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    getActivity(),
                                    android.R.layout.simple_spinner_item,
                                    vehicleNames
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            vehicleSpinner.setAdapter(adapter);

                        });
                    }

                } else {
                    if (isAdded() && getActivity() != null) {

                        getActivity().runOnUiThread(() ->
                                {
                                    UserUtils.getMessageFromLocal(3, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            UserUtils.ToastMessages(getActivity(), message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }
                                    });
                                }
                        );
                    }
                }

                conn.disconnect();

            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "fetchVehicles", e.toString(), e.toString(), "add trip");

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            {
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
                    );
                }
            }
        }).start();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("اختر الوقت")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int selectedHour = picker.getHour();
            int selectedMinute = picker.getMinute();

            // تحويل للعرض 12 ساعة
            String amPm = (selectedHour >= 12) ? "م" : "ص";
            int hourIn12 = selectedHour % 12;
            if (hourIn12 == 0) hourIn12 = 12;

            String displayTime = String.format(Locale.ENGLISH, "%02d:%02d %s", hourIn12, selectedMinute, amPm);
            departureTime.setText(displayTime);

            // حفظ للسيرفر بصيغة hh:mm:ss
            String saveTime = String.format(Locale.ENGLISH, "%02d:%02d:00", selectedHour, selectedMinute);
            departureTime.setTag(saveTime);
        });

        // عرض الـ Picker
        picker.show(getParentFragmentManager(), "time_picker");
    }

    private void fetchRoutes() {
        new Thread(() -> {
            try {
                DBHelper db = new DBHelper(getContext());
                List<DBHelper.RouteModel> routesList = db.getAllRoutes(); // جلب البيانات من الجدول
                String[] routeNames = new String[routesList.size() + 1];
                int[] routeIds = new int[routesList.size() + 1];
                Car_codes.clear();

                routeNames[0] = "اختر المسار";
                routeIds[0] = -1;
                Car_codes.add("");

                for (int i = 0; i < routesList.size(); i++) {
                    DBHelper.RouteModel r = routesList.get(i);
                    routeIds[i + 1] = r.id;
                    routeNames[i + 1] = r.name;
                    Car_codes.add(r.carCode);
                }

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(),
                                android.R.layout.simple_spinner_item,
                                routeNames
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        routeSpinner.setAdapter(adapter);

                        routeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position == 0) {
                                    carCode.setText("");
                                    selectedRouteId = -1;
                                    return;
                                }
                                carCode.setText(Car_codes.get(position));
                                selectedRouteId = routeIds[position];
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                carCode.setText("");
                                selectedRouteId = -1;
                            }
                        });

                        // اجعل أول خيار هو الافتراضي
                        routeSpinner.setSelection(0);
                        carCode.setText("");
                        selectedRouteId = -1;
                    });
                }

            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "fetchRoutes", e.toString(), e.toString(), "add trip");
            }
        }).start();
    }


    private void sendTripData() {
        DBHelper dbHelper = new DBHelper(getContext());

        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                UserUtils.showSuccessGif(2, requireActivity(), null);

//                progressDialog = new ProgressDialog(getActivity());
//                progressDialog.setMessage("جاري إرسال بيانات الرحلة...");
//                progressDialog.setCancelable(false);
//                progressDialog.show();
            });
        }
        new Thread(() -> {
            String s = null;
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "trips/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());
//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                int userId = prefs.getInt("user_id", -1);

                JSONObject data = new JSONObject();
                data.put("route_id", selectedRouteId);

                int vehiclePosition = vehicleSpinner.getSelectedItemPosition();
                if (vehiclePosition < 0 || vehiclePosition >= vehicleIds.size()) {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            UserUtils.hideSuccessGif(getActivity());

                        });
                    }
                    return;
                }
//                int checkedRadioButtonId = fromCityRadioGroup.getCheckedRadioButtonId();
                Integer fromCityId = null;
                int checkedId = dayTimeRadioGroup.getCheckedRadioButtonId();
                if (checkedId != -1) {
                    RadioButton selectedRadio = dayTimeRadioGroup.findViewById(checkedId);
                    selectedDayId = (Integer) selectedRadio.getTag();

                }
//                if (checkedRadioButtonId != -1) {
//                    RadioButton selectedRadio = fromCityRadioGroup.findViewById(checkedRadioButtonId);
//                    fromCityId = (Integer) selectedRadio.getTag();
//                }
//                lastFromCityId = fromCityId;
                boolean isValid = true;

                if (textFromCity.getText().toString().isEmpty() || textFromCity.getText().toString().equals("حدد المدينة")) {
                    fromCityError.setText("يرجى اختيار من المدينة");
                    fromCityError.setVisibility(View.VISIBLE);
                    isValid = false;
                    lastFromCityId = null; // تأكد أنه لا يحمل قيمة خاطئة
                } else {
                    fromCityError.setVisibility(View.GONE);

                    DBHelper.City city = dbHelper.getCityByName(textFromCity.getText().toString());
                    if (city != null) {
                        lastFromCityId = city.getId();
                    } else {
                        fromCityError.setText("المدينة غير صالحة");

                        fromCityError.setVisibility(View.VISIBLE);
                        isValid = false;
                        lastFromCityId = null;
                    }
                }
//                int checkedRadioButtonId2 = toCityRadioGroup.getCheckedRadioButtonId();
//                Integer fromCityId2 = null;
//                if (checkedRadioButtonId2 != -1) {
//                    RadioButton selectedRadio = toCityRadioGroup.findViewById(checkedRadioButtonId2);
//                    fromCityId2 = (Integer) selectedRadio.getTag();
//                }
//                lastToCityId = fromCityId2;
                int selectedVehicleId = vehicleIds.get(vehiclePosition);
                data.put("vehicle", selectedVehicleId);
                data.put("departure_date", selectedDate);
                String timeToSave = (String) departureTime.getTag();
                data.put("departure_time", timeToSave);
                data.put("departure_address", fromAddress.getText().toString().trim());
                data.put("destination_address", toAddress.getText().toString().trim());
                data.put("price_per_seat", Integer.parseInt(price.getText().toString().replace(",", "").trim()));
                data.put("available_seats", Integer.parseInt(availableSeats.getText().toString().trim()));
                data.put("additional_notes", notes.getText().toString().trim());
                data.put("end_point_order", lastToCityId);
                data.put("start_point_order", lastFromCityId);
                data.put("dt_no", selectedDayId);
                data.put("driver", userId);
                data.put("trip_type", 1);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes("UTF-8"));
                os.close();

                s = "{ vehicle: " + selectedVehicleId +
                        " , departure_date: " + selectedDate +
                        " , departure_time: " + timeToSave +
                        " , departure_address: " + fromAddress.getText().toString().trim() +
                        " , destination_address: " + toAddress.getText().toString().trim() +
                        " , price_per_seat: " + price.getText().toString().trim() +
                        " , available_seats: " + availableSeats.getText().toString().trim() +
                        " , additional_notes: " + notes.getText().toString().trim() +
                        " , end_point_order: " + lastToCityId +
                        " , start_point_order: " + lastFromCityId +
                        " , trip_type: " + 1 +
                        " , dt_no: " + selectedDayId +
                        " , driver: " + userId +
                        " }";
                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode == 200 || responseCode == 201) ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                String response = builder.toString();
                if (isAdded() && getActivity() != null) {
                    String finalS = s;
                    getActivity().runOnUiThread(() -> {
                        UserUtils.hideSuccessGif(getActivity());


                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.has("trip_id")) {
                                UserUtils.hideSuccessGif(getActivity());

                                if (getActivity() != null) {
                                        if (getActivity() != null) {
                                            ((HomePage) requireActivity()).selectTab(R.id.nav_reservation);
                                            openHomeFragment("1");

                                            UserUtils.getMessageFromLocal(6, dbHelper, new UserUtils.MessageCallback() {
                                                @Override
                                                public void onSuccess(String message) {
                                                    UserUtils.ToastMessages(getActivity(), message);
                                                }

                                                @Override
                                                public void onError(String error) {
                                                }
                                            });
                                            isDataChanged = false;

                                        }
                                }
//                                openHomeFragment("1");
//
//                                UserUtils.getMessageFromLocal(6, dbHelper, new UserUtils.MessageCallback() {
//                                    @Override
//                                    public void onSuccess(String message) {
//                                        UserUtils.ToastMessages(getActivity(), message);
//                                    }
//
//                                    @Override
//                                    public void onError(String error) {
//                                    }
//                                });
                            } else if (jsonResponse.has("non_field_errors")) {
                                JSONArray errors = jsonResponse.getJSONArray("non_field_errors");
                                if (errors.length() > 0) {
                                    JSONObject firstError = errors.getJSONObject(0);
                                    String errorMessage = firstError.optString("message", "فشل في إضافة الرحلة");
                                    UserUtils.getMessageFromLocal(121, dbHelper, new UserUtils.MessageCallback() {
                                        @Override
                                        public void onSuccess(String message) {
                                            UserUtils.ToastMessages(getActivity(), message);
                                        }

                                        @Override
                                        public void onError(String error) {
                                        }
                                    });
                                }
                                UserUtils.hideSuccessGif(getActivity());

                            } else {
                                UserUtils.sendLog(getContext(), "sendTripData else", String.valueOf(jsonResponse), finalS, "add trip");
                                UserUtils.getMessageFromLocal(7, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(getActivity(), message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                                UserUtils.hideSuccessGif(getActivity());

                            }

                        } catch (Exception e) {
                            UserUtils.sendLog(getContext(), "sendTripData catch", e.toString(), finalS, "add trip");
                            UserUtils.getMessageFromLocal(7, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            UserUtils.hideSuccessGif(getActivity());

                        }
                    });
                }
            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "sendTripData", e.toString(), s, "add trip");

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.hideSuccessGif(getActivity());

                        UserUtils.getMessageFromLocal(8, dbHelper, new UserUtils.MessageCallback() {
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

    private void openHomeFragment(String tripType) {
        Fragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("trip_type", tripType);
        fragment.setArguments(bundle);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.full_screen_container, fragment)
                .addToBackStack(null)
                .commit();

        switch (tripType) {
            case "1":
                UserUtils.app_Page(getContext(), 43);
                ((HomePage) requireActivity()).updateToolbar("رحلات تشاركية", false, R.drawable.big_car, 0);
                break;
            case "2":
                UserUtils.app_Page(getContext(), 44);
                ((HomePage) requireActivity()).updateToolbar("نقل دولي", false, R.drawable.world_new, 0);
                break;
            case "3":
                UserUtils.app_Page(getContext(), 45);
                ((HomePage) requireActivity()).updateToolbar("رحلات محلية", false, R.drawable.bus_2, 0);
                break;
        }
    }
}