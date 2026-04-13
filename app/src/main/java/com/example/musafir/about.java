package com.example.musafir;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

public class about extends Fragment {

   @Override
public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) { MenuItem placeholderItem = menu.findItem(R.id.action_placeholder); if (placeholderItem != null) { placeholderItem.setVisible(true); View actionView = placeholderItem.getActionView(); if (actionView != null) { actionView.setPressed(true); actionView.postDelayed(() -> actionView.setPressed(false), 100); } actionView.setOnClickListener(v -> { requireActivity().onBackPressed(); }); } }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        TextView appVersion = view.findViewById(R.id.appVersion);
        appVersion.append(UserUtils.app_version);
        setHasOptionsMenu(true);
        return  view;
    }
}