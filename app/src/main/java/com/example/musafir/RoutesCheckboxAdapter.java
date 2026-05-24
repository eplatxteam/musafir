package com.example.musafir;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.recyclerview.widget.RecyclerView;

import com.example.musafir.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoutesCheckboxAdapter extends RecyclerView.Adapter<RoutesCheckboxAdapter.ViewHolder> {

    private final List<JSONObject> routes;
    private SparseBooleanArray checkedStates = new SparseBooleanArray();

    public RoutesCheckboxAdapter(List<JSONObject> routes) {
        this.routes = routes;
        checkedStates = new SparseBooleanArray(routes.size());
        for (int i = 0; i < routes.size(); i++) {
            checkedStates.put(i, routes.get(i).optBoolean("is_favorite", false));
        }
    }
    public List<JSONObject> getAllRoutes() {
        return routes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_checkbox, parent, false);
        return new ViewHolder(view);
    }
    public List<JSONObject> getSelectedRoutes() {
        List<JSONObject> selected = new ArrayList<>();
        for (int i = 0; i < routes.size(); i++) {
            if (checkedStates.get(i, false)) {
                selected.add(routes.get(i));
            }
        }
        return selected;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject route = routes.get(position);
            holder.checkBox.setText(route.getString("route_name"));
            holder.checkBox.setChecked(checkedStates.get(position, false));

            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkedStates.put(position, isChecked));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxRoute);
        }
    }
}
