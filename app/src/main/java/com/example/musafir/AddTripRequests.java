package com.example.musafir;

import static com.example.musafir.LocationWorker.getCityNameFromIp;
import static com.example.musafir.R.drawable.radio_button_text_color;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.Nullable;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jp.wasabeef.blurry.Blurry;

public class AddTripRequests extends Fragment {
    EditText editTextNotes;
    Button addhBtn;
    ArrayList<String> cityNames = new ArrayList<>();
    ArrayList<Integer> cityIds = new ArrayList<>();
    String BASE_URL = UserUtils.BASE_URL;
    TextView travelersCount;
    private Integer lastFromCityId = null;
    private Integer lastToCityId = null;
    final Calendar calendar = Calendar.getInstance();
    RadioGroup radioTripType;
    RadioButton radioPrivate, radioShared;
    ArrayList<String> dayNames = new ArrayList<>();
    ArrayList<Integer> dayIds = new ArrayList<>();
    RadioGroup dayTimeRadioGroup;
    HorizontalScrollView dayTimeScrollView;
    View firstErrorView = null;
    ScrollView scrollViewcon;
    DBHelper dbHelper;

    Spinner vehicleTypeSpinner;
    List<String> vehicleTypeNames = new ArrayList<>();
    Map<String, Integer> vehicleTypeMap = new HashMap<>();
    private Integer vehicleTypeFromArgs = null;
    TextView textFromCity, textToCity;
    private LinearLayout fromLayout, toLayout, containerCar, container_address;
    CheckBox reception_car;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem placeholderItem = menu.findItem(R.id.action_placeholder);
//        placeholderItem.setVisibility(View.GONE);
//        if (placeholderItem != null) {
//            placeholderItem.setVisible(true);
//            View actionView = placeholderItem.getActionView();
//            if (actionView != null) {
//                actionView.setPressed(true);
//                actionView.postDelayed(() -> actionView.setPressed(false), 100);
//            }
//            actionView.setOnClickListener(v -> {
//                requireActivity().onBackPressed();
//            });
//        }
    }


    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false); // ❌ يخفي سهم الرجوع
        }
    }
    int selectedDayId = -1;

    String selectedDate;
    Spinner routeSpinner;
    int selectedRouteId = -1;
    TextView carCode;
    EditText address;
    int v_reception_car = 0;
    TextView dtNoError;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_trip_requests, container, false);
        carCode = view.findViewById(R.id.carCode);
        dbHelper = new DBHelper(requireContext());
        setHasOptionsMenu(true);
        editTextNotes = view.findViewById(R.id.notes);
        containerCar = view.findViewById(R.id.containerCar);
        reception_car = view.findViewById(R.id.reception_car);
        container_address = view.findViewById(R.id.container_address);
        address = view.findViewById(R.id.address);
        addhBtn = view.findViewById(R.id.addbtn);
        radioTripType = view.findViewById(R.id.radioTripType);
        radioPrivate = view.findViewById(R.id.radioPrivate);
        radioShared = view.findViewById(R.id.radioShared);
        dayTimeRadioGroup = view.findViewById(R.id.dayTimeRadioGroup);
        dayTimeScrollView = view.findViewById(R.id.dayTimeScrollView);
        vehicleTypeSpinner = view.findViewById(R.id.vehicleTypeSpinner);
        scrollViewcon = view.findViewById(R.id.scrollContent);
        dayTimeRadioGroup.removeAllViews();
        LinearLayout weekContainer = view.findViewById(R.id.week_containerReq);
        Calendar calendar = Calendar.getInstance(new Locale("ar"));
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ar"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", new Locale("ar"));
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        routeSpinner = view.findViewById(R.id.routeSpinner);

        reception_car.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                v_reception_car = 1;
                container_address.setVisibility(View.VISIBLE);
            } else {
                v_reception_car = 0;
                container_address.setVisibility(View.GONE);
            }
        });
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        LinearLayout[] dayViews = new LinearLayout[14];

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
                tvDayName.setTextColor(getResources().getColor(R.color.secondary));
                tvDayNumber.setTextColor(getResources().getColor(R.color.secondary));
                dayDot.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.secondary)
                ));
                selectedDate = fullDate; // fullDate هو التاريخ الكامل لليوم الحالي
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

            // الانتقال لليوم التالي
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            weekContainer.addView(dayView);
        }
        View customView = getLayoutInflater().inflate(R.layout.toolbar_custom, null);
//        TextView textGreeting = customView.findViewById(R.id.textGreeting);
//        ImageView iconTool = customView.findViewById(R.id.iconTool);
//        ImageView iconHi = customView.findViewById(R.id.iconHi);
        ImageView action_placeholder = customView.findViewById(R.id.action_placeholder);
        action_placeholder.setVisibility(View.GONE);
//        editTextDate.setOnClickListener(v -> showDatePickerDialog(editTextDate));
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        ((HomePage) requireActivity()).selectTab(R.id.nav_home);
                    }
                }
        );
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
        travelersCount = view.findViewById(R.id.numbertravelers);
        ImageView btnIncrease = view.findViewById(R.id.btnIncrease);
        ImageView btnDecrease = view.findViewById(R.id.btnDecrease);
        final int MIN_COUNT = 1;
        final int MAX_COUNT = 6;
        ImageView carIcons = view.findViewById(R.id.carIcon);

        carIcons.setOnClickListener(v -> {
            String fromCity = textFromCity.getText().toString().trim();
            String toCity = textToCity.getText().toString().trim();

            if (fromCity.equals("حدد المدينة") || toCity.equals("حدد المدينة")) {
                return;
            }

            // عكس القيم بينهما
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
        UserUtils.setEditTextState(editTextNotes, false);
        fetchRoutes();

        btnIncrease.setOnClickListener(v -> {
            int count = Integer.parseInt(travelersCount.getText().toString());
            if (count < MAX_COUNT) {
                count++;
                travelersCount.setText(String.valueOf(count));
            }
        });

        btnDecrease.setOnClickListener(v -> {
            int count = Integer.parseInt(travelersCount.getText().toString());
            if (count > MIN_COUNT) {
                count--;
                travelersCount.setText(String.valueOf(count));
            }
        });
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);

        TextView fromCityError = view.findViewById(R.id.fromCityError);
        TextView toCityError = view.findViewById(R.id.toCityError);
        dtNoError = view.findViewById(R.id.dtNoError);

        addhBtn.setOnClickListener(v -> {

            firstErrorView = null;
            boolean isValid = true;

            if (textFromCity.getText().toString().isEmpty() || textFromCity.getText().toString().equals("حدد المدينة")) {
                fromCityError.setText("يرجى اختيار من المدينة");
                fromCityError.setVisibility(View.VISIBLE);
                isValid = false;
                lastFromCityId = null;
                if (firstErrorView == null) firstErrorView = fromCityError;
            } else {
                fromCityError.setVisibility(View.GONE);

                DBHelper.City city = dbHelper.getCityByName(textFromCity.getText().toString());
                if (city != null) {
                    lastFromCityId = city.getId();
                } else {
                    fromCityError.setText("المدينة غير صالحة");
                    if (firstErrorView == null) firstErrorView = fromCityError;
                    fromCityError.setVisibility(View.VISIBLE);
                    isValid = false;
                    lastFromCityId = null;
                }
            }

            // تحقق من مدينة الوصول
            if (textToCity.getText().toString().isEmpty() || textToCity.getText().toString().equals("حدد المدينة")) {
                toCityError.setText("يرجى اختيار إلى المدينة");
                toCityError.setVisibility(View.VISIBLE);
                isValid = false;
                lastToCityId = null;
                if (firstErrorView == null) firstErrorView = toCityError;
            } else {
                toCityError.setVisibility(View.GONE);

                DBHelper.City city = dbHelper.getCityByName(textToCity.getText().toString());
                if (city != null) {
                    lastToCityId = city.getId();
                } else {
                    toCityError.setText("المدينة غير صالحة");
                    toCityError.setVisibility(View.VISIBLE);
                    isValid = false;
                    lastToCityId = null;
                }
            }

            // تحقق من اختيار فترة الرحلة
            if (dayTimeRadioGroup.getCheckedRadioButtonId() == -1) {
                dtNoError.setText("يرجى اختيار فترة للرحلة");
                dtNoError.setVisibility(View.VISIBLE);
                isValid = false;
                if (firstErrorView == null) firstErrorView = dtNoError;
            } else dtNoError.setVisibility(View.GONE);

            if (firstErrorView != null) {
                scrollViewcon.post(() -> scrollViewcon.smoothScrollTo(0, firstErrorView.getTop()));
            }
            if (isValid) sendTripRequest();
        });

        fetchCitiesWithHttpURLConnection();


        fetchDayTime();
        loadVehicleTypes();
        String defaultCity = prefs.getString("default_city", "حدد المدينة");
        textFromCity.setText(defaultCity);
        DBHelper.City defaultCityObj = dbHelper.getCityByName(defaultCity);
        if (defaultCityObj != null) {
            textFromCity.setTag(defaultCityObj.getId());
            lastFromCityId = defaultCityObj.getId();
        }

        // getCityNameFromIp(getContext(), cityAr -> {
        //     prefs.edit().putString("default_city", cityAr).apply();

        //     ((Activity) getContext()).runOnUiThread(() -> {
        //         textFromCity.setText(cityAr);
        //         DBHelper db = new DBHelper(getContext());
//                 DBHelper.City defaultCity = db.getCityByName(cityAr);
//                 if (defaultCity != null) {
//                     textFromCity.setTag(defaultCity.getId());
//                 }
        //     });

        // });
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
                // لا توجد مسارات
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

    private List<String> Car_codes = new ArrayList<>();

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
                        if (isAdded() && getActivity() != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    getActivity(),
                                    android.R.layout.simple_spinner_item,
                                    routeNames
                            );

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            routeSpinner.setAdapter(adapter);
                        }
                        routeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position == 0) {
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

                        routeSpinner.setSelection(0);
                        selectedRouteId = -1;
                    });
                }

            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "fetchRoutes", e.toString(), e.toString(), "add trip");
            }
        }).start();
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
            UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
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

            targetTextView.setText(city.getNameAr());
            targetTextView.setTag(city.getId());

            int country_id = city.getCountryId();


            if (isFromCity) {
                lastFromCityId = city.getId();
                if (country_id > 1) {
                    containerCar.setVisibility(View.VISIBLE);
                } else {
                    containerCar.setVisibility(View.GONE);
                }

            } else
                lastToCityId = city.getId();
            updateRoutesByCities();
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

    @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists"})
    private int dt_no = 0; // تعريف المتغيّر في الكلاس

    private void populateDayTimeRadioButtons(List<String> displayDays, Set<Integer> disabledPositions) {
        try {
            dayTimeRadioGroup.removeAllViews();

            int[][] states = new int[][]{new int[]{android.R.attr.state_enabled, android.R.attr.state_checked},
                    new int[]{android.R.attr.state_enabled},
                    new int[]{-android.R.attr.state_enabled} // disabled
            };
            int[] colors = new int[]{ContextCompat.getColor(getContext(), R.color.secondary),
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

                // تعيين الحالة (تمكين/تعطيل)
                radioButton.setEnabled(!disabledPositions.contains(i));

                // تعيين لون النص
                radioButton.setTextColor(colorStateList);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 10, 0);
                radioButton.setLayoutParams(params);
                radioButton.setBackgroundResource(R.drawable.radio_button_home);

                dayTimeRadioGroup.addView(radioButton);
            }

            // ✅ استماع لاختيار أي زر لتحديث dt_no
            dayTimeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selected = getActivity().findViewById(checkedId);
                if (selected != null) {
                    int index = (int) selected.getTag();
                    dt_no = index + 1; // ✅ يبدأ من 1

                }
            });

            dayTimeScrollView.post(() -> dayTimeScrollView.fullScroll(View.FOCUS_RIGHT));

        } catch (Exception e) {
            UserUtils.sendLog(getContext(), "populateDayTimeRadioButtons", e.toString(), e.toString(), "add trip request");
        }
    }


    private void fetchCitiesWithHttpURLConnection() {
        DBHelper dbHelper = new DBHelper(getContext());

        new Thread(() -> {
            try {
                List<DBHelper.City> cities = dbHelper.getAllCities();

                cityNames.clear();
                cityIds.clear();

                for (DBHelper.City city : cities) {
                    cityNames.add(city.name);
                    cityIds.add(city.id);
                }

                getActivity().runOnUiThread(() -> {
//                    populateDialogFromCityRadioButtons();
//                    populateCityRadioButtons();
                });


            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "fetchCitiesWithHttpURLConnection", e.toString(), e.toString(), "add trip");

                getActivity().runOnUiThread(() -> {
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
        }).start();


    }

    public interface TripCheckCallback {
        void onResult(boolean tripExists, @Nullable Integer tripId);
    }


    private void fetchTrips(int page, @Nullable Integer fromCityId, @Nullable Integer toCityId, @Nullable String date, @Nullable Integer passengers, @Nullable Integer familiesOnly, boolean checkOnly, TripCheckCallback callback) {

        new Thread(() -> {
            try {
                StringBuilder urlBuilder = new StringBuilder(BASE_URL + "trips/?page=" + page + "&limit=6");

                if (fromCityId != null) urlBuilder.append("&start_city=").append(fromCityId);
                if (toCityId != null) urlBuilder.append("&end_city=").append(toCityId);
                if (date != null) urlBuilder.append("&start_date=").append(date);
                if (passengers != null && passengers != 1)
                    urlBuilder.append("&seats=").append(passengers);
                if (familiesOnly != null && familiesOnly != 0)
                    urlBuilder.append("&family=").append(familiesOnly);


                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                String token2 = "1";

                if (token2 != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token2);
                }
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

                    boolean tripExists = resultsArray.length() > 0;
                    Integer tripId = null;
                    if (tripExists) {
                        JSONObject firstTrip = resultsArray.getJSONObject(0);
                        tripId = firstTrip.getInt("trip_id"); // تأكد أن حقل id موجود في JSON
                    }

                    Integer finalTripId = tripId;
                    getActivity().runOnUiThread(() -> {
                        if (callback != null) callback.onResult(tripExists, finalTripId);
                    });

                } else {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.getMessageFromLocal(38, dbHelper, new UserUtils.MessageCallback() {
                            @Override
                            public void onSuccess(String message) {
                                UserUtils.ToastMessages(getActivity(), message);
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });
                        if (callback != null) callback.onResult(false, null);
                    });
                }

                conn.disconnect();
            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "fetchTrips", e.toString(), e.toString(), "add trip request");
                getActivity().runOnUiThread(() -> {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(getActivity(), message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                    if (callback != null) callback.onResult(false, null);
                });
            }
        }).start();
    }
    private void sendTripRequest() {
        String notes = editTextNotes.getText().toString().trim();
        String selectedSeatsText = travelersCount.getText().toString().trim();
        String v_address = address.getText().toString().trim();

        firstErrorView = null;


        int checkedId = dayTimeRadioGroup.getCheckedRadioButtonId(); //
        if (checkedId == -1) {
            dtNoError.setText("يرجى اختيار فترة للرحلة");
            dtNoError.setVisibility(View.VISIBLE);

            scrollViewcon.post(() -> scrollViewcon.smoothScrollTo(0, dtNoError.getTop()));

            return;
        } else {
            dtNoError.setVisibility(View.GONE);
        }


        int numberOfSeats = Integer.parseInt(selectedSeatsText);
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        int userId = prefs.getInt("user_id", -1);

        int selectedTypeId = radioTripType.getCheckedRadioButtonId();
        int isPrivate = (selectedTypeId == R.id.radioPrivate) ? 1 : 0;

        RadioButton selectedRadio = dayTimeRadioGroup.findViewById(checkedId);
        if (selectedRadio != null && selectedRadio.getTag() != null) {
            selectedDayId = (Integer) selectedRadio.getTag();
        } else {
            selectedDayId = -1;
        }

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("جاري التحقق من الرحلات ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        fetchTrips(1, lastFromCityId, lastToCityId, selectedDate, numberOfSeats, isPrivate, true, new TripCheckCallback() {
            @Override
            public void onResult(boolean tripExists, @Nullable Integer tripId) {
                progressDialog.dismiss();

                if (tripExists && tripId != null) {
                    int finalSelectedDayId = selectedDayId;

                    new AlertDialog.Builder(getContext()).setTitle("تنبيه").setMessage("يوجد بالفعل رحلة تطابق بياناتك؟").setPositiveButton("عرض الرحلة", (dialog, which) -> {
                        Fragment fragment = new MoreDetails();
                        Bundle args = new Bundle();
                        args.putInt("trip_id", tripId);
                        fragment.setArguments(args);

                        if (requireActivity() instanceof HomePage) {
                            ((HomePage) requireActivity()).openFullScreenFragment(
                                    fragment,
                                    "تفاصيل الرحلة",
                                    R.drawable.booking,
                                    2
                            );
                        }

//                        Intent intent = new Intent(getContext(), MoreDetails.class);
//                        intent.putExtra("trip_id", tripId);
//                        startActivity(intent);
                    }).setNegativeButton("إستمرار", (dialog, which) -> {
                        sendTripRequestToServer(lastFromCityId, lastToCityId, selectedDate, numberOfSeats, isPrivate, notes, userId, finalSelectedDayId, v_reception_car, v_address);
                    }).show();
                } else {
                    sendTripRequestToServer(lastFromCityId, lastToCityId, selectedDate, numberOfSeats, isPrivate, notes, userId, selectedDayId, v_reception_car, v_address);
                }
            }
        });
    }

//    private void sendTripRequest() {
//
////        String travelDate = editTextDate.getText().toString().trim();
//        String notes = editTextNotes.getText().toString().trim();
//        String selectedSeatsText = travelersCount.getText().toString().trim();
//        String v_address = address.getText().toString().trim();
////        String v_reception_car = reception_car.getText().toString().trim();
//
//        int numberOfSeats = Integer.parseInt(selectedSeatsText);
//        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
//
////        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
//        int userId = prefs.getInt("user_id", -1);
//
//        int selectedTypeId = radioTripType.getCheckedRadioButtonId();
//        int isPrivate = (selectedTypeId == R.id.radioPrivate) ? 1 : 0;
//
//        ProgressDialog progressDialog = new ProgressDialog(getContext());
//        progressDialog.setMessage("جاري التحقق من الرحلات ...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//        fetchTrips(1, lastFromCityId, lastToCityId, selectedDate, numberOfSeats, isPrivate, true, new TripCheckCallback() {
//            @Override
//            public void onResult(boolean tripExists, @Nullable Integer tripId) {
//                progressDialog.dismiss();
//                int checkedId = dayTimeRadioGroup.getCheckedRadioButtonId();
//                if (checkedId != -1) {
//                    RadioButton selectedRadio = dayTimeRadioGroup.findViewById(checkedId);
//                    if (selectedRadio != null && selectedRadio.getTag() != null) {
//                        selectedDayId = (Integer) selectedRadio.getTag();
//                    } else {
//                        selectedDayId = -1;
//                    }
////                    selectedDayId = (Integer) selectedRadio.getTag();
//                }
//                if (tripExists && tripId != null) {
//                    int finalSelectedDayId = selectedDayId;
//
//                    new AlertDialog.Builder(getContext()).setTitle("تنبيه").setMessage("يوجد بالفعل رحلة تطابق بياناتك؟").setPositiveButton("عرض الرحلة", (dialog, which) -> {
//                        Fragment fragment = new MoreDetails();
//                        Bundle args = new Bundle();
//                        args.putInt("trip_id", tripId);
//                        fragment.setArguments(args);
//
//                        if (requireActivity() instanceof HomePage) {
//                            ((HomePage) requireActivity()).openFullScreenFragment(
//                                    fragment,
//                                    "تفاصيل الرحلة",
//                                    R.drawable.booking,
//                                    2
//                            );
//                        }
//
////                        Intent intent = new Intent(getContext(), MoreDetails.class);
////                        intent.putExtra("trip_id", tripId);
////                        startActivity(intent);
//                    }).setNegativeButton("إستمرار", (dialog, which) -> {
//                        sendTripRequestToServer(lastFromCityId, lastToCityId, selectedDate, numberOfSeats, isPrivate, notes, userId, finalSelectedDayId, v_reception_car, v_address);
//                    }).show();
//                } else {
//                    sendTripRequestToServer(lastFromCityId, lastToCityId, selectedDate, numberOfSeats, isPrivate, notes, userId, selectedDayId, v_reception_car, v_address);
//                }
//            }
//        });
//    }


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
                String period = displayDays.get(i); // مثال: "04 - 06 (ص)"
                try {
                    String[] parts = period.split(" - ");
                    if (parts.length >= 2) {
                        int startHour = Integer.parseInt(parts[0].trim());
                        String endPart = parts[1].trim(); // "06 (ص)"
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
                }
            }
        } else {
        }

        populateDayTimeRadioButtons(displayDays, disabledPositions);

    }

    private void fetchDayTime() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "day-time");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                int responseCode = conn.getResponseCode();

                BufferedReader reader = new BufferedReader(new InputStreamReader((responseCode == 200) ? conn.getInputStream() : conn.getErrorStream()));

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

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateDayAdapter(); // سيتم استدعاء populateDayTimeRadioButtons داخله
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        UserUtils.getMessageFromLocal(38, dbHelper, new UserUtils.MessageCallback() {
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
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        UserUtils.sendLog(getContext(), "fetchDayTime", e.toString(), e.toString(), "add trip request");
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

    private void loadVehicleTypes() {
        DBHelper dbHelper = new DBHelper(getContext());

        // مسح البيانات القديمة
        vehicleTypeNames.clear();
        vehicleTypeMap.clear();

        // جلب البيانات من جدول SQLite
        List<DBHelper.VehicleType> vehicleTypes = dbHelper.getVehicleTypes(1);

        // ملء المصفوفات
        for (DBHelper.VehicleType v : vehicleTypes) {
            vehicleTypeNames.add(v.getName());
            vehicleTypeMap.put(v.getName(), v.getId());
        }

        // تهيئة Spinner إذا كانت Fragment مضافة
        if (isAdded() && getActivity() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, vehicleTypeNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vehicleTypeSpinner.setAdapter(adapter);

            // تحديد العنصر من المتغير إذا كان موجود
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

        // إذا لم توجد بيانات في الجدول
        if (vehicleTypes.isEmpty()) {
            UserUtils.getMessageFromLocal(161, dbHelper, new UserUtils.MessageCallback() {
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

    private void sendTripRequestToServer(int startCityId, int endCityId, String date, int seats, int isPrivate,
                                         @Nullable String notes, int userId, int dtNo,
                                         @Nullable int reception_car, @Nullable String address) {

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("جاري إرسال الطلب...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                String deviceId = UserUtils.getDeviceID(getContext());
                String deviceInfo = UserUtils.getDeviceInfo();
                URL url = new URL(BASE_URL + "trip-requests/?device_id=" + deviceId + "&device_info=" + deviceInfo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//                SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }
                String selectedName = vehicleTypeSpinner.getSelectedItem().toString();
                int vehicleTypeId = vehicleTypeMap.get(selectedName);
                JSONObject requestBody = new JSONObject();
                requestBody.put("start_city_id", startCityId);
                requestBody.put("end_city_id", endCityId);
                int routeIdToSend = (selectedRouteId <= 0) ? 1 : selectedRouteId;

                requestBody.put("route_id", routeIdToSend);
                requestBody.put("number_of_seats", seats);
                requestBody.put("preferred_departure_date", date);
                requestBody.put("additional_notes", notes);
                requestBody.put("passenger", userId);
                requestBody.put("dt_no", dtNo);
                requestBody.put("private", isPrivate);
                requestBody.put("reception_car", reception_car);
                requestBody.put("address", address);
                requestBody.put("vehicle_type_ref", vehicleTypeId);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.close();
                String s =
                        "{ start_city_id: " + startCityId +
                                " , end_city_id: " + endCityId +
                                " , number_of_seats: " + seats +
                                " , preferred_departure_date: " + date +
                                " , additional_notes: " + notes +
                                " , reception_car: " + reception_car +
                                " , address: " + address +
                                " , passenger: " + userId +
                                " , dt_no: " + dtNo +
                                " , private: " + isPrivate +
                                " , vehicle_type_ref: " + vehicleTypeId + " }";
                int responseCode = conn.getResponseCode();
                InputStream inputStream;

                if (responseCode >= 200 && responseCode < 400) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                String responseBody = responseBuilder.toString();

                String finalResponseBody = responseBody;
                getActivity().runOnUiThread(() -> {
                    UserUtils.hideSuccessGif(getActivity());

                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        if (getActivity() != null) {
                                if (getActivity() != null) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                    ((HomePage) requireActivity()).selectTab(R.id.nav_reservation);
                                    openBookingFragment(1, "رحلاتي");
                                    UserUtils.getMessageFromLocal(81, dbHelper, new UserUtils.MessageCallback() {
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
                    } else {
                        try {
                            JSONObject errorObj = new JSONObject(finalResponseBody);
                            if (errorObj.has("error")) {
                                JSONArray errorArray = errorObj.getJSONArray("error");
                                StringBuilder errorMessages = new StringBuilder();

                                for (int i = 0; i < errorArray.length(); i++) {
                                    JSONObject errorItem = errorArray.getJSONObject(i);
                                    if (errorItem.has("message")) {
                                        errorMessages.append("- ").append(errorItem.getString("message")).append("\n");
                                    }
                                }

                                String errorsStr = errorMessages.toString().trim();
                                UserUtils.getMessageFromLocal(5, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(getActivity(), message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                                UserUtils.sendLog(getContext(), "sendTripRequestToServer", errorsStr, s, "add trip request");
                                UserUtils.hideSuccessGif(getActivity());

                            } else if (errorObj.has("detail")) {
                                String errorMessage = errorObj.getString("detail");
                                UserUtils.sendLog(getContext(), "sendTripRequestToServer", errorMessage, s, "add trip request");

                                UserUtils.getMessageFromLocal(82, dbHelper, new UserUtils.MessageCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        UserUtils.ToastMessages(getActivity(), message);
                                    }

                                    @Override
                                    public void onError(String error) {
                                    }
                                });
                                UserUtils.hideSuccessGif(getActivity());

                            } else {
                                UserUtils.sendLog(getContext(), "sendTripData", String.valueOf(errorObj), s, "add trip request");
                                UserUtils.getMessageFromLocal(63, dbHelper, new UserUtils.MessageCallback() {
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
                        } catch (Exception ex) {
                            UserUtils.getMessageFromLocal(63, dbHelper, new UserUtils.MessageCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    UserUtils.ToastMessages(getActivity(), message);
                                }

                                @Override
                                public void onError(String error) {
                                }
                            });
                            UserUtils.hideSuccessGif(getActivity());

                            UserUtils.sendLog(getContext(), "sendTripRequestToServer", ex.toString(), s, "add trip request");
                        }
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                UserUtils.sendLog(getContext(), "sendTripRequestToServer", e.toString(), e.toString(), "add trip request");
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
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
                UserUtils.hideSuccessGif(getActivity());

            }
        }).start();
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
}