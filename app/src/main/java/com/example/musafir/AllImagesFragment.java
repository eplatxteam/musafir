package com.example.musafir;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

public class AllImagesFragment extends Fragment {

    private LottieAnimationView lottieWave;
    private LinearLayout noInternet;
    private RecyclerView recyclerView;

    public AllImagesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // مهم للخيارات
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_all_images, container, false);

        lottieWave = view.findViewById(R.id.lottieWaveAll);
        noInternet = view.findViewById(R.id.noInternet);
        recyclerView = view.findViewById(R.id.recyclerViewimg);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadImages();
    }

    private void loadImages() {
        if (!isAdded()) return;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        int inHomePage = 0;
        if (getArguments() != null) {
            inHomePage = getArguments().getInt("inHomePage", 0);
        }

        String url = UserUtils.BASE_URL + "ImagesHome/?in_home_page=" + inHomePage;

        if (!UserUtils.isNetworkAvailable(requireContext())) {
            if (!isAdded()) return;
            noInternet.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lottieWave.setVisibility(View.GONE);
            return;
        }

        lottieWave.playAnimation();
        lottieWave.setVisibility(View.VISIBLE);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!isAdded()) return;

                    lottieWave.setVisibility(View.GONE);
                    lottieWave.cancelAnimation();

                    recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                    recyclerView.setAdapter(new AllImageAdapter(requireContext(), response));
                },
                error -> {
                    if (!isAdded()) return;

                    lottieWave.setVisibility(View.GONE);
                    lottieWave.cancelAnimation();
                }
        );

        queue.add(request);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem placeholderItem = menu.findItem(R.id.action_placeholder);
        if (placeholderItem != null) {
            placeholderItem.setVisible(true);
            View actionView = placeholderItem.getActionView();
            if (actionView != null) {
                actionView.setPressed(true);
                actionView.postDelayed(() -> actionView.setPressed(false), 100);
            }
            actionView.setOnClickListener(v -> {
                requireActivity().onBackPressed();
            });
        }
    }

}
