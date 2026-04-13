package com.example.musafir;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> implements Filterable {

    private List<DBHelper.City> cityList;
    private List<DBHelper.City> filteredList;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(DBHelper.City city);
    }

    public CityAdapter(List<DBHelper.City> cityList, OnCityClickListener listener) {
        this.cityList = cityList;
        this.filteredList = new ArrayList<>(cityList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        DBHelper.City city = filteredList.get(position);
        holder.text.setText(city.getNameAr());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onCityClick(city);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    class CityViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        CityViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(android.R.id.text1);
        }
    }
    public void updateList(List<DBHelper.City> newList) {
        filteredList.clear();
        filteredList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return cityFilter;
    }

    private final Filter cityFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<DBHelper.City> resultList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                resultList.addAll(cityList);
            } else {
                String query = constraint.toString().toLowerCase();

                for (DBHelper.City city : cityList) {
                    if (city.getNameAr().toLowerCase().contains(query)) {
                        resultList.add(city);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = resultList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            filteredList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}

