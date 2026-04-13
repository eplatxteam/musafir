package com.example.musafir;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class OnboardingAdapter extends FragmentStateAdapter {

    private List<Fragment> fragmentList;

    public OnboardingAdapter(@NonNull FragmentActivity fa, List<Fragment> list) {
        super(fa);
        fragmentList = list;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // يرجع الـ Fragment كما هو
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
