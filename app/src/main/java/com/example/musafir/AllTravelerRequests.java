package com.example.musafir;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllTravelerRequests extends Fragment {


    public AllTravelerRequests() {
        // Required empty public constructor
    }

    String BASE_URL = UserUtils.BASE_URL;

    RecyclerView RecyclerViewTrvAll;
    LottieAnimationView lottieTrvRq;
    LinearLayout noDataTextTrip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_traveler_request, container, false);

        RecyclerViewTrvAll = view.findViewById(R.id.recyclerViewTrvAll);
        lottieTrvRq = view.findViewById(R.id.lottieTrvRq);
        noDataTextTrip = view.findViewById(R.id.noDataTextTrip);

        get_trv_req();
        return view;

    }

    private void get_trv_req() {

        // اظهار التحميل قبل البدء
        lottieTrvRq.setVisibility(View.VISIBLE);
        RecyclerViewTrvAll.setVisibility(View.GONE);
        noDataTextTrip.setVisibility(View.GONE);

        RequestQueue queue = Volley.newRequestQueue(getContext());
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());

//        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        int passenger_id = prefs.getInt("user_id", -1);
        String token = prefs.getString("auth_token", null);
        String url = BASE_URL + "TravelerRequests/?passenger_id=" + passenger_id;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {

                    List<JSONObject> list = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            list.add(response.getJSONObject(i));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    lottieTrvRq.setVisibility(View.GONE);

                    if (list.isEmpty()) {
                        // لا توجد بيانات
                        RecyclerViewTrvAll.setVisibility(View.GONE);
                        noDataTextTrip.setVisibility(View.VISIBLE);
                    } else {
                        // توجد بيانات
                        noDataTextTrip.setVisibility(View.GONE);
                        RecyclerViewTrvAll.setVisibility(View.VISIBLE);
                        RecyclerViewTrvAll.setLayoutManager(
                                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
                        );
                        TravelerRequestsAdapter adapter = new TravelerRequestsAdapter(getContext(), list);
                        RecyclerViewTrvAll.setAdapter(adapter);
                    }

                }, error -> {
            lottieTrvRq.setVisibility(View.GONE);
            RecyclerViewTrvAll.setVisibility(View.GONE);
            noDataTextTrip.setVisibility(View.VISIBLE); // عرض رسالة عند الخطأ
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        queue.add(request);
    }

}