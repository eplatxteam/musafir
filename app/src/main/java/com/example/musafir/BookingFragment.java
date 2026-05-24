package com.example.musafir;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class BookingFragment extends Fragment {
    ViewPager2 viewPager;
    int tabToOpen = 0;

    @Override
    public void onResume() {
        super.onResume();

        MyBookingsFragment.currentPage = 1;
        MyBookingsFragment.isLastPage = false;
        MyBookingsFragment.isLoading = false;

        if (MyBookingsFragment.bookingsList != null) {
            MyBookingsFragment.bookingsList.clear();
            if (MyBookingsFragment.adapter != null) {
                MyBookingsFragment.adapter.notifyDataSetChanged();
            }
        }

        MyBookingsFragment.showLoading();

        MyBookingsFragment.fetchBooking(1, getContext());

        MyTripsFragment.currentPage = 1;
        MyTripsFragment.isLastPage = false;
        MyTripsFragment.isLoading = false;

        if (MyTripsFragment.tripList != null) {
            MyTripsFragment.tripList.clear();
            if (MyTripsFragment.adapter != null) {
                MyTripsFragment.adapter.notifyDataSetChanged();
            }
        }

        MyTripsFragment.showLoading();

        MyTripsFragment.loadTrips(1, getContext());
        if (getArguments() != null && getArguments().containsKey("tab_to_open")) {
            tabToOpen = getArguments().getInt("tab_to_open", 0);
            getArguments().remove("tab_to_open"); // مسحه لكي لا يفتح مرة أخرى عند التدوير

            if (viewPager != null) {
                viewPager.post(() -> {
                    if (isAdded()) {
                        viewPager.setCurrentItem(tabToOpen, false);
                    }
                });
            }
        }
        refreshCurrentTab();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        viewPager.setSaveEnabled(false);
        if (getArguments() != null) {
            tabToOpen = getArguments().getInt("tab_to_open", 0);
        }

        BookingPagerAdapter adapter = new BookingPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);

        // ربط الـ TabLayout بالـ ViewPager (مرة واحدة فقط وبشكل يضمن ملء المساحة)
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            View tabView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
            TextView title = tabView.findViewById(R.id.tabTitle);
            ImageView icon = tabView.findViewById(R.id.tabIcon);

            // إجبار الكاستم تاب على أخذ كامل العرض والارتفاع المتاحين
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            tabView.setLayoutParams(params);

            if (position == 0) {
                title.setText("الحجوزات");
                icon.setImageResource(R.drawable.booking);
            } else {
                title.setText("طلبات الرحلات");
                icon.setImageResource(R.drawable.booking);
            }
            tab.setCustomView(tabView);
        }).attach();

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final int finalTabToOpen = tabToOpen;
        tabLayout.post(() -> {
            if (isAdded() && viewPager != null && tabLayout != null) {
                viewPager.setCurrentItem(finalTabToOpen, false);
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null) {
                        updateTabUI(tab, i == finalTabToOpen);
                        if (i == finalTabToOpen) {
                            tab.select();
                        }
                    }
                }
            }
        });

        // مستمع التنقل بين التبويبات لتغيير الألوان والخلفيات
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabUI(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabUI(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // معالجة زر الرجوع
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        ((HomePage) requireActivity()).selectTab(R.id.nav_home);
                    }
                }
        );

        // مراقب تغيير الـ Toolbar
        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                Fragment currentFragment = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.full_screen_container);
                if (currentFragment instanceof BookingFragment) {
                    updateToolbar("رحلاتي", false, R.drawable.airplane_t, 1);
                } else if (currentFragment instanceof AddTripFragment) {
                    updateToolbar("إضافة رحلة", false, R.drawable.locations, 0);
                } else if (currentFragment instanceof AddTripRequests) {
                    updateToolbar("طلب رحلة خاصة", false, R.drawable.locations, 0);
                } else if (currentFragment instanceof TravelerRequests) {
                    updateToolbar("خدمات المسافرين", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof BookingDetailsFragment) {
                    updateToolbar("تفاصيل الحجز", false, R.drawable.booking, 0);
                } else if (currentFragment instanceof CustomBooking) {
                    updateToolbar("حجز رحلة", false, R.drawable.booking, 0);
                } else if (currentFragment instanceof TripDetailsFragment) {
                    updateToolbar("تفاصيل الطلب", false, R.drawable.booking, 0);
                }
            }
        });

        // تحديث بيانات الملف الشخصي والأمان
        UserUtils.updateProfile(getActivity(), (isVerified, isActive) -> {
            if (!isActive) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                DBHelper dbHelper = new DBHelper(getContext());
                UserUtils.getMessageFromLocal(22, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(getActivity(), message);
                    }
                    @Override
                    public void onError(String error) {}
                });
            }
        });

        return view;
    }

    public void refreshCurrentTab() {
        viewPager.post(() -> {
            if (viewPager.getAdapter() != null) {
                int currentTab = viewPager.getCurrentItem();
                viewPager.getAdapter().notifyItemChanged(currentTab);
            }
        });
    }

    private void updateTabUI(TabLayout.Tab tab, boolean isSelected) {

        if (tab != null && tab.getCustomView() != null) {
            View v = tab.getCustomView();
            TextView title = v.findViewById(R.id.tabTitle);
            ImageView icon = v.findViewById(R.id.tabIcon);

            if (isSelected) {
                v.setBackgroundResource(R.drawable.tab_selected);
                title.setTextColor(getResources().getColor(R.color.white));
                icon.setColorFilter(getResources().getColor(R.color.primary2));
                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
                title.setTypeface(typeface, Typeface.BOLD);
            } else {
                v.setBackgroundResource(R.drawable.tab_unselected);
                title.setTextColor(getResources().getColor(R.color.black));
                icon.setColorFilter(getResources().getColor(R.color.black));
                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.rptregular);
                title.setTypeface(typeface, Typeface.NORMAL);
            }
        }
    }

    private void updateToolbar(String title, boolean showBackArrow, int iconRes, int fragmentId) {
        HomePage activity = (HomePage) getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
        }
        activity.updateToolbar(title, showBackArrow, iconRes, fragmentId);
    }
}
