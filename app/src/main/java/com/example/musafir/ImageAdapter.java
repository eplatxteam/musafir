package com.example.musafir;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    private final List<String> imageUrls;
    private final Context context;
    private int inHomePage;

    public ImageAdapter(Context context, List<String> imageUrls, int InHomePage) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.inHomePage = InHomePage;
    }

    @Override
    public int getItemViewType(int position) {
//        if (position == Math.min(imageUrls.size(), 5)) return TYPE_ADD_BUTTON;
        return TYPE_IMAGE;
    }

    @Override
//    public int getItemCount() {
//        return Math.min(imageUrls.size(), 5) + 1;
//    }
    public int getItemCount() {
        return imageUrls.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        } else {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_add_button, parent, false);
//            return new AddButtonViewHolder(view);
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            String data = imageUrls.get(position);
            String imgId = "";
            String url = data;

            if (data.contains("|")) {
                String[] parts = data.split("\\|");
                imgId = parts[0];
                url = parts[1];
            }

            Glide.with(context)
                    .load(url)
                    .into(((ImageViewHolder) holder).imageView);

            String finalImgId = imgId;

        } else if (holder instanceof AddButtonViewHolder) {
            ((AddButtonViewHolder) holder).addButton.setOnClickListener(v -> {
                AllImagesFragment fragment = new AllImagesFragment();
                Bundle args = new Bundle();
                args.putInt("inHomePage", inHomePage);
                fragment.setArguments(args);
                ((HomePage) context).openFullScreenFragment(fragment, "الإعلانات", R.drawable.ads, 0);
//                ((HomePage) context).updateToolbar("الإعلانات", false, R.drawable.ads, 0);
            });
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    static class AddButtonViewHolder extends RecyclerView.ViewHolder {
        FrameLayout addButton;

        public AddButtonViewHolder(@NonNull View itemView) {
            super(itemView);
//            addButton = itemView.findViewById(R.id.addButton);
        }
    }
}
