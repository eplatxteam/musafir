package com.example.musafir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private final List<JSONObject> vehicles;
    private final Context context;
    private final String baseUrl;
    String ImageUrl = UserUtils.ImageUrl;

    public VehicleAdapter(Context context, List<JSONObject> vehicles, String baseUrl) {
        this.context = context;
        this.vehicles = vehicles;
        this.baseUrl = baseUrl;
    }


    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        JSONObject vehicle = vehicles.get(position);
        try {
            String make = vehicle.getString("make");
//            String model = vehicle.getString("model");
            String color = vehicle.getString("color");
            String year = vehicle.getString("year");
            String licensePlate = vehicle.getString("license_plate");
            String totalSeats = vehicle.getString("total_seats");
            String vehicle_type = vehicle.getString("vehicle_type");

            String displayName = vehicle_type + " " + make;
            holder.driverName.setText(displayName);
            holder.yearText.setText(year);
//            holder.carPlate.setText(licensePlate);
//            holder.colorText.setText(color);
            holder.seatText.setText(totalSeats);

            int vehicleId = vehicle.has("vehicle_id") ? vehicle.getInt("vehicle_id") : vehicle.getInt("id");
            String lastLargeImage = vehicle.optString("last_big_image_url", "");
            String vehicle_image = vehicle.optString("vehicle_image", "");
            String imageUrl = null;

            if (vehicle_image != null && !vehicle_image.isEmpty() && !"null".equals(vehicle_image)) {
                imageUrl = vehicle_image;
            }

            if (imageUrl != null) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.empty2)
                        .into(holder.vehicleImage);
            } else {
                holder.vehicleImage.setImageResource(R.drawable.empty2);
            }
        } catch (Exception e) {
            UserUtils.sendLog(context, "VehicleAdapter", e.toString(), e.toString(), "Vehicle Adapter");
        }

        holder.editVehicle.setOnClickListener(v -> {
            try {
//                ((HomePage) context).updateToolbar("تعديل المركبة", false, R.drawable.local);

                Bundle bundle = new Bundle();
                bundle.putInt("vehicle_id", vehicle.getInt("vehicle_id"));
                bundle.putInt("total_seats", vehicle.getInt("total_seats"));
                bundle.putString("make", vehicle.getString("make"));
                bundle.putString("model", vehicle.getString("model"));
                bundle.putString("year", vehicle.getString("year"));
                bundle.putString("insurance_document", vehicle.getString("insurance_document"));
                bundle.putString("vehicle_type_ref", vehicle.getString("vehicle_type_ref"));
                bundle.putString("fuel_type", vehicle.getString("fuel_type"));
                bundle.putString("insurance_expiry_date", vehicle.getString("insurance_expiry_date"));
                bundle.putString("available_seats", vehicle.getString("available_seats"));
                bundle.putString("color", vehicle.getString("color"));
                bundle.putString("license_plate", vehicle.getString("license_plate"));
                bundle.putString("registration_document", vehicle.getString("registration_document"));
                bundle.putInt("vh_price", vehicle.isNull("vh_price") ? 0 : vehicle.getInt("vh_price"));
                bundle.putString("vehicle_image", vehicle.isNull("vehicle_image") ? null : vehicle.getString("vehicle_image"));
                bundle.putString("vehicle_name", vehicle.isNull("vehicle_name") ? null : vehicle.getString("vehicle_name"));
                bundle.putString("vehicle_image1", vehicle.isNull("vehicle_image1") ? null : vehicle.getString("vehicle_image1"));
                bundle.putString("vehicle_image2", vehicle.isNull("vehicle_image2") ? null : vehicle.getString("vehicle_image2"));
                bundle.putString("vehicle_image3", vehicle.isNull("vehicle_image3") ? null : vehicle.getString("vehicle_image3"));
                bundle.putString("vehicle_image4", vehicle.isNull("vehicle_image4") ? null : vehicle.getString("vehicle_image4"));
                AddVehicleFragment fragment = new AddVehicleFragment();
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.full_screen_container, fragment)
                        .addToBackStack(null)
                        .commit();

            } catch (Exception e) {
                UserUtils.sendLog(context, "VehicleAdapter", e.toString(), e.toString(), "Vehicle Adapter");
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView vehicleImage;
        TextView driverName, yearText,   seatText;
        MaterialButton editVehicle;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            seatText = itemView.findViewById(R.id.seatText);
//            colorText = itemView.findViewById(R.id.colorText);
//            carPlate = itemView.findViewById(R.id.carPlate);
            vehicleImage = itemView.findViewById(R.id.vehicleImage);
            driverName = itemView.findViewById(R.id.driver_name);
            yearText = itemView.findViewById(R.id.yearText);
            editVehicle = itemView.findViewById(R.id.edit_vehicle);
        }
    }
}
