package com.example.musafir;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class BookingFragment extends Fragment {
    ViewPager2 viewPager;

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
        refreshCurrentTab();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        BookingPagerAdapter adapter = new BookingPagerAdapter(requireActivity());

        int tabToOpen = (getArguments() != null) ? getArguments().getInt("tab_to_open", 0) : 0;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(tabToOpen, false);
        viewPager.setOffscreenPageLimit(1);


        viewPager.post(() -> {
            if (isAdded() && viewPager.getAdapter() != null) {
                viewPager.getAdapter().notifyItemChanged(tabToOpen);
            }
        });
        // رجوع
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        ((HomePage) requireActivity()).selectTab(R.id.nav_home);
                    }
                }
        );
        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                Fragment currentFragment = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.full_screen_container);
                if (currentFragment instanceof BookingFragment) {
//                    BookingFragment bf = (BookingFragment) currentFragment;
//                    Bundle args = bf.getArguments();
//                    int tab = args != null ? args.getInt("tab_to_open", 0) : 0;
                    updateToolbar("رحلاتي", false, R.drawable.airplane_new, 1);
                } else if (currentFragment instanceof AddTripFragment) {
                    updateToolbar("إضافة رحلة", false, R.drawable.locations, 1);
                } else if (currentFragment instanceof AddTripRequests) {
                    updateToolbar("طلب رحلة", false, R.drawable.locations, 1);
                } else if (currentFragment instanceof TravelerRequests) {
                    updateToolbar("خدمات المسافرين", false, R.drawable.solo_traveller, 0);
                } else if (currentFragment instanceof BookingDetailsFragment) {
                    updateToolbar("تفاصيل الحجز", false, R.drawable.booking, 0);
                } else if (currentFragment instanceof TripDetailsFragment) {
                    updateToolbar("تفاصيل الطلب", false, R.drawable.booking, 0);
                }
            }
        });
        // ----------- تطبيق التبويبات المخصصة -----------
        tabLayout.post(() -> {
            if (isAdded() && viewPager != null && tabLayout != null) {
                // نستخدم tabToOpen الذي أصبح 1 افتراضياً
                viewPager.setCurrentItem(tabToOpen, false);

                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null) {
                        // تحديث الشكل (الخلفية البيضاء للمحدد، والرمادي لغيره)
                        updateTabUI(tab, i == tabToOpen);

                        if (i == tabToOpen) {
                            tab.select();
                        }
                    }
                }
            }
        });
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            View tabView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
            TextView title = tabView.findViewById(R.id.tabTitle);
            ImageView icon = tabView.findViewById(R.id.tabIcon);

            if (position == 0) {
                title.setText("الحجوزات");
                icon.setImageResource(R.drawable.booking);
            } else {
                title.setText("طلبات الرحلات");
                icon.setImageResource(R.drawable.booking);
            }
            tab.setCustomView(tabView);
        }).attach();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();

                v.setBackgroundResource(R.drawable.tab_selected);

                TextView title = v.findViewById(R.id.tabTitle);
                ImageView icon = v.findViewById(R.id.tabIcon);

                title.setTextColor(getResources().getColor(R.color.primary2));
                icon.setColorFilter(getResources().getColor(R.color.primary2));
                title.setTypeface(null, Typeface.BOLD);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();

                v.setBackgroundResource(R.drawable.tab_unselected);
                TextView title = v.findViewById(R.id.tabTitle);
                ImageView icon = v.findViewById(R.id.tabIcon);

                title.setTextColor(getResources().getColor(R.color.black));
                icon.setColorFilter(getResources().getColor(R.color.black));
                title.setTypeface(null, Typeface.NORMAL);

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

//        viewPager.setCurrentItem(openTab, false);

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
                    public void onError(String error) {
                    }
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
                title.setTextColor(getResources().getColor(R.color.primary2));
                icon.setColorFilter(getResources().getColor(R.color.primary2));
                title.setTypeface(null, Typeface.BOLD);
            } else {
                v.setBackgroundResource(R.drawable.tab_unselected);
                title.setTextColor(getResources().getColor(R.color.black));
                icon.setColorFilter(getResources().getColor(R.color.black));
                title.setTypeface(null, Typeface.NORMAL);
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
