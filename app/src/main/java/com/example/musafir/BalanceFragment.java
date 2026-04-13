package com.example.musafir;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BalanceFragment extends Fragment {

    private CardView cardYER, cardSAR, cardUSD, cardYERN;
    private TextView tvBalanceYER, tvBalanceSAR, tvBalanceUSD, tvBalanceYERN;
    private TextView tvNameYER, tvNameSAR, tvNameUSD, tvNameYERN;
    private LinearLayout detailsContainer;
    private TextView tvDetailsTitle;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private Button btnAddBalance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_balance, container, false);
        setHasOptionsMenu(true);

        cardYER = view.findViewById(R.id.cardYER);
        cardYERN = view.findViewById(R.id.cardYERN);
        cardSAR = view.findViewById(R.id.cardSAR);
        cardUSD = view.findViewById(R.id.cardUSD);

        tvBalanceYER = view.findViewById(R.id.tvBalanceYER);
        tvBalanceYERN = view.findViewById(R.id.tvBalanceYERN);
        tvBalanceSAR = view.findViewById(R.id.tvBalanceSAR);
        tvBalanceUSD = view.findViewById(R.id.tvBalanceUSD);

        tvNameYER = view.findViewById(R.id.tvNameYER);
        tvNameYERN = view.findViewById(R.id.tvNameYERN);
        tvNameSAR = view.findViewById(R.id.tvNameSAR);
        tvNameUSD = view.findViewById(R.id.tvNameUSD);

        detailsContainer = view.findViewById(R.id.detailsContainer);
        tvDetailsTitle = view.findViewById(R.id.tvDetailsTitle);
        progressBar = view.findViewById(R.id.progressBar);

        requestQueue = Volley.newRequestQueue(requireContext());
        btnAddBalance = view.findViewById(R.id.btnAddBalance);

        btnAddBalance.setOnClickListener(v -> {
//            UserUtils.showCashBankBottomSheet(getContext(), id -> {
//                Log.d("Selection", "تم اختيار المحفظة رقم: " + id);
////                    selectedWalletId = id;
//            });
            UserUtils.showGenericOptionsBottomSheet(getContext(), 5, 0, 0, 0, 0, null);
        });

        cardYERN.setVisibility(View.GONE);
        cardYER.setVisibility(View.GONE);
        cardSAR.setVisibility(View.GONE);
        cardUSD.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchBalance();
        UserUtils.app_Page(getContext(), 10);
    }

    private void fetchBalance() {
        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        int userId = prefs.getInt("user_id", 0);
        String token = prefs.getString("auth_token", "");

        if (userId == 0) return;

        progressBar.setVisibility(View.VISIBLE);
        detailsContainer.removeAllViews();
        tvDetailsTitle.setVisibility(View.GONE);

        String url = UserUtils.BASE_URL + "user-balance/?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.getJSONObject(i);
                            String curCode = item.getString("cur_code");
                            String curName = item.optString("cur_name", "");
                            double balance = item.getDouble("balance");
                            int user_id = item.optInt("user_id", 0);

                            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
                            formatter.applyPattern("#,###,##0");

                            String formattedBalance = formatter.format(balance);
                            if (curCode.equalsIgnoreCase("YER")) {
                                prefs.edit().putString("user_balance", formattedBalance).apply();
                            }
                            updateUI(curCode, curName, balance, user_id);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        MenuItem addVehicleItem = menu.findItem(R.id.action_placeholder2);
//        if (addVehicleItem != null) {
//            addVehicleItem.setIcon(R.drawable.baseline_add_24);
//            addVehicleItem.setVisible(true);

    /// /            addVehicleItem.setTitle("إضافة مركبة");
//        }
//
//        MenuItem placeholderItem = menu.findItem(R.id.action_placeholder);
//        if (placeholderItem != null) {
//            placeholderItem.setVisible(true);
//            View actionView = placeholderItem.getActionView();
//            if (actionView != null) {
//                actionView.setPressed(true);
//                actionView.postDelayed(() -> actionView.setPressed(false), 100);
//            }
//            actionView.setOnClickListener(v -> {
//                requireActivity().onBackPressed();
//            });
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_placeholder2) {
//
//            View rootView = getView();
//
//            if (rootView != null) {
//                UserUtils.showGenericOptionsBottomSheet(getContext(), 5);
//
//            }
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }


//    private void updateUI(String curCode, String curName, double balance, int user_id) {
//        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
//        formatter.applyPattern("#,###,##0.00");
//
//        String formattedBalance = formatter.format(balance);
//        CardView targetCard = null;
//        switch (curCode) {
//            case "YER":
//                targetCard = cardYER;
//                tvBalanceYER.setText(formattedBalance);
//                if (!curName.isEmpty()) tvNameYER.setText(curName);
//                break;
//            case "YERN":
//                targetCard = cardYERN;
//                tvBalanceYERN.setText(formattedBalance);
//                if (!curName.isEmpty()) tvNameYERN.setText(curName);
//                break;
//            case "SAR":
//                targetCard = cardSAR;
//                tvBalanceSAR.setText(formattedBalance);
//                if (!curName.isEmpty()) tvNameSAR.setText(curName);
//                break;
//            case "USD":
//                targetCard = cardUSD;
//                tvBalanceUSD.setText(formattedBalance);
//                if (!curName.isEmpty()) tvNameUSD.setText(curName);
//                break;
//        }
//
//        if (targetCard != null) {
//            targetCard.setOnClickListener(v -> fetchBalanceDetails(user_id, curCode, true));
//        }
//    }
    private void updateUI(String curCode, String curName, double balance, int user_id) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        formatter.applyPattern("#,###,##0");

        String formattedBalance = formatter.format(balance);
        CardView targetCard = null;

        switch (curCode) {
            case "YER":
                targetCard = cardYER;
                tvBalanceYER.setText(formattedBalance);
                if (!curName.isEmpty()) tvNameYER.setText(curName);
                break;
            case "YERN":
                targetCard = cardYERN;
                tvBalanceYERN.setText(formattedBalance);
                if (!curName.isEmpty()) tvNameYERN.setText(curName);
                break;
            case "SAR":
                targetCard = cardSAR;
                tvBalanceSAR.setText(formattedBalance);
                if (!curName.isEmpty()) tvNameSAR.setText(curName);
                break;
            case "USD":
                targetCard = cardUSD;
                tvBalanceUSD.setText(formattedBalance);
                if (!curName.isEmpty()) tvNameUSD.setText(curName);
                break;
        }

        if (targetCard != null) {
            targetCard.setVisibility(View.VISIBLE);
            targetCard.setOnClickListener(v -> fetchBalanceDetails(user_id, curCode, true));
        }
    }

    private void fetchBalanceDetails(int user_id, String curCode, boolean clearContainer) {
        if (user_id == 0) return;

        requestQueue.cancelAll("balance_details");

        SharedPreferences prefs = SharedPrefsHelper.get(getContext());
        String token = prefs.getString("auth_token", "");

        progressBar.setVisibility(View.VISIBLE);
        if (clearContainer) {
            detailsContainer.removeAllViews();
            tvDetailsTitle.setVisibility(View.GONE);
        }

        String url = UserUtils.BASE_URL + "user-balance-details/?ac_code_dtl=" + user_id;
        if (curCode != null && !curCode.isEmpty()) {
            url += "&cur_code=" + curCode;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 0) {
                            tvDetailsTitle.setVisibility(View.VISIBLE);
                            for (int i = 0; i < results.length(); i++) {
                                addDetailItem(results.getJSONObject(i));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> progressBar.setVisibility(View.GONE)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        request.setTag("balance_details");
        requestQueue.add(request);
    }

    private void addDetailItem(JSONObject detail) throws JSONException {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_balance_detail, detailsContainer, false);

        LinearLayout mainLayout = itemView.findViewById(R.id.mainLayout);
        LinearLayout expandableLayout = itemView.findViewById(R.id.expandableLayout);
        ImageView ivArrow = itemView.findViewById(R.id.ivArrow);

        TextView tvDocType = itemView.findViewById(R.id.tvDocType);
        TextView tvAmount = itemView.findViewById(R.id.tvAmount);
        TextView tvDate = itemView.findViewById(R.id.tvDate);
        TextView tvDescription = itemView.findViewById(R.id.tvDescription);
        TextView tvDocSrl = itemView.findViewById(R.id.tvDocSrl);
        ImageView ivIcon = itemView.findViewById(R.id.ivIcon);
        TextView tvCurCodeView = itemView.findViewById(R.id.tvCurCode);
        tvDocType.setText(detail.getString("doc_type_name"));
        double amt = detail.getDouble("amt");
        String curCode = detail.optString("cur_code", "");
//        DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
        // استخدام Locale.ENGLISH يضمن بقاء الأرقام 1234.56
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        formatter.applyPattern("#,###,##0.00");

        String formattedBalance = formatter.format(amt);
//        String amountStr = String.format(Locale.ENGLISH, "%.2f", amt);
//        tvAmount.setText((amt >= 0 ? "+" : "") + formattedBalance + " \n" + curCode);
        tvAmount.setText((amt >= 0 ? "+" : "") + formattedBalance);
        tvAmount.setText((amt >= 0 ? "+" : "") + formattedBalance);
        if (tvCurCodeView != null) {
            String curNameAr = curCode;
            if (curCode.equalsIgnoreCase("YER")) curNameAr = "ريال يمني";
            else if (curCode.equalsIgnoreCase("YERN")) curNameAr = "ريال يمني (جديد)";
            else if (curCode.equalsIgnoreCase("SAR")) curNameAr = "ريال سعودي";
            else if (curCode.equalsIgnoreCase("USD")) curNameAr = "دولار أمريكي";

            tvCurCodeView.setText(curNameAr);
            tvCurCodeView.setTextColor(tvDate.getTextColors());
        }

        if (amt >= 0) {
            tvAmount.setTextColor(getResources().getColor(R.color.holo_green_dark));
            ivIcon.setBackgroundResource(R.drawable.badge_background_bal);
            ivIcon.setImageResource(R.drawable.ic_plus);
            ivIcon.setColorFilter(getResources().getColor(android.R.color.white));
        } else {
            tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            ivIcon.setBackgroundResource(R.drawable.badge_background_details);
            ivIcon.setImageResource(R.drawable.ic_minus);
            ivIcon.setColorFilter(getResources().getColor(android.R.color.black));
        }

        tvDate.setText(detail.getString("doc_date"));
        tvDescription.setText(detail.getString("doc_dsc"));

        String srl = detail.optString("doc_srl", "");
        tvDocSrl.setText("#" + srl);

        mainLayout.setOnClickListener(v -> {
            if (expandableLayout.getVisibility() == View.VISIBLE) {
                expandableLayout.setVisibility(View.GONE);
                ivArrow.setRotation(0);
            } else {
                expandableLayout.setVisibility(View.VISIBLE);
                ivArrow.setRotation(180);
            }
        });

        detailsContainer.addView(itemView);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll("balance_details");
        }
    }
}
