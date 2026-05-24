package com.example.musafir;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AllImageAdapter extends RecyclerView.Adapter<AllImageAdapter.ImageViewHolder> {

    private final JSONArray imageArray;
    private final Context context;
    String ImageUrl = UserUtils.ImageUrl;

    public AllImageAdapter(Context context, JSONArray imageArray) {
        this.context = context;
        this.imageArray = imageArray;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_all_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        try {
            JSONObject obj = imageArray.getJSONObject(position);

            final int imgId = obj.getInt("img_id"); // نحتفظ بالـ img_id النهائي
            String imgUrl = ImageUrl + "/media/" + obj.getString("img_name");
            String title = obj.getString("title");
            String details = obj.getString("details");

            // قص النص لأوّل 10 كلمة
            String[] words = details.split("\\s+");
            if (words.length > 10) {
                StringBuilder shortText = new StringBuilder();
                for (int i = 0; i < 10; i++) {
                    shortText.append(words[i]).append(" ");
                }
                shortText.append("...");
                details = shortText.toString().trim();
            }

            holder.title.setText(title);
//            holder.details.setText(details);

            Glide.with(context)
                    .load(imgUrl)
                    .into(holder.imageView);
            String finalImgId = String.valueOf(imgId);

            holder.imageView.setOnClickListener(v -> {
                Advertisements fragment = new Advertisements();

                Bundle bundle = new Bundle();
                bundle.putString("img_id", finalImgId);
                fragment.setArguments(bundle);

                ((AppCompatActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.full_screen_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });

            holder.cardDetails.setOnClickListener(v -> {
                Advertisements fragment = new Advertisements();

                Bundle bundle = new Bundle();
                bundle.putString("img_id", finalImgId);
                fragment.setArguments(bundle);

                ((AppCompatActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.full_screen_container, fragment)
                        .addToBackStack(null)
                        .commit();

            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int getItemCount() {
        return imageArray.length();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;
        CardView cardDetails;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImage);
            title = itemView.findViewById(R.id.itemTitle);
//            details = itemView.findViewById(R.id.itemDetails);
            cardDetails = itemView.findViewById(R.id.cardDetails);
        }
    }
}
