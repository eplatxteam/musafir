package com.example.musafir;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import androidx.core.widget.NestedScrollView;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BalanceFragment extends Fragment {

    private CardView cardYER, cardSAR, cardUSD, cardYERN;
    private TextView tvBalanceYER, tvBalanceSAR, tvBalanceUSD, tvBalanceYERN;
    private TextView tvNameYER, tvNameSAR, tvNameUSD, tvNameYERN;
    private LinearLayout detailsContainer;
    private LinearLayout tvDetailsTitle;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    Button btnAddBalance;
    ImageView ivArrowYER, ivArrowYERN, ivArrowSAR, ivArrowUSD;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String currentCurCode = ""; // لحفظ العملة المحددة حالياً

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

        ivArrowYER = view.findViewById(R.id.ivArrowYER);
        ivArrowYERN = view.findViewById(R.id.ivArrowYERN);
        ivArrowSAR = view.findViewById(R.id.ivArrowSAR);
        ivArrowUSD = view.findViewById(R.id.ivArrowUSD);

        requestQueue = Volley.newRequestQueue(requireContext());
        btnAddBalance = view.findViewById(R.id.btnAddBalance);
        DBHelper dbHelper = new DBHelper(getContext());

        btnAddBalance.setOnClickListener(new UserUtils.SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
//            UserUtils.showCashBankBottomSheet(getContext(), id -> {
////                    selectedWalletId = id;
//            });
                UserUtils.fetchAndSavePayTypes(getContext(), new UserUtils.GenericCallback() {

                    @Override
                    public void onSuccess(String message) {
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
                UserUtils.fetchCashBankData(getContext(), dbHelper, new UserUtils.OnCashBankFetchedListener() {
                    @Override
                    public void onFetched(List<DBHelper.CashBank> types) {
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
                UserUtils.showUnifiedPaymentBottomSheet(getContext(), 5, 0, null, null,
                        0, null, 0,
                        (payType, paymentStatus, success, request_id) -> {
                            if (success) {
                                fetchBalance();
//                                UserUtils.ToastMessages(getActivity(), UserUtils.getMessageFromLocalNew(325, dbHelper));
                            }
                        });
//            UserUtils.showGenericOptionsBottomSheet(getContext(), 5, 0, 0, 0, 0, null);
            }
        });

        cardYERN.setVisibility(View.GONE);
        cardYER.setVisibility(View.GONE);
        cardSAR.setVisibility(View.GONE);
        cardUSD.setVisibility(View.GONE);
        NestedScrollView nestedScrollView = view.findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight() - 200)) {
                if (!isLoading && !isLastPage) {
                    SharedPreferences prefs = SharedPrefsHelper.get(getContext());
                    int userId = prefs.getInt("user_id", 0);
                    fetchBalanceDetails(userId, currentCurCode, false);
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchBalance();
        UserUtils.app_Page(getContext(), 10);
    }

    public void fetchBalance() {
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
//                        e.printStackTrace();
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

    private void updateUI(String curCode, String curName, double balance, int user_id) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        formatter.applyPattern("#,###,##0");

        String formattedBalance = formatter.format(balance);
        CardView targetCard = null;
        ImageView targetArrow = null; // متغير لمسك السهم المطلوب

        switch (curCode) {
            case "YER":
                targetCard = cardYER;
                targetArrow = ivArrowYER;
                tvBalanceYER.setText(formattedBalance);
                if (!curName.isEmpty()) tvNameYER.setText(curName);
                break;
            case "YERN":
                targetCard = cardYERN;
                targetArrow = ivArrowYERN;
                tvBalanceYERN.setText(formattedBalance);
                if (!curName.isEmpty()) tvNameYERN.setText(curName);
                break;
            case "SAR":
                targetCard = cardSAR;
                targetArrow = ivArrowSAR;
                tvBalanceSAR.setText(formattedBalance);
                if (!curName.isEmpty()) tvNameSAR.setText(curName);
                break;
            case "USD":
                targetCard = cardUSD;
                targetArrow = ivArrowUSD;
                tvBalanceUSD.setText(formattedBalance);
                if (!curName.isEmpty()) tvNameUSD.setText(curName);
                break;
        }

        if (targetCard != null && targetArrow != null) {
            targetCard.setVisibility(View.VISIBLE);

            final ImageView finalArrow = targetArrow;

            targetCard.setOnClickListener(v -> {
                if (finalArrow.getRotation() == 0) {
                    resetAllArrows();
                    finalArrow.animate().rotation(180).setDuration(300).start();
                    fetchBalanceDetails(user_id, curCode, true);
                } else {
                    finalArrow.animate().rotation(0).setDuration(300).start();
                    detailsContainer.removeAllViews();
                    tvDetailsTitle.setVisibility(View.GONE);
                }
            });
        }
    }

    private void resetAllArrows() {
        ivArrowYER.setRotation(0);
        ivArrowYERN.setRotation(0);
        ivArrowSAR.setRotation(0);
        ivArrowUSD.setRotation(0);
    }
    private void fetchBalanceDetails(int user_id, String curCode, boolean clearContainer) {
        if (user_id == 0 || isLoading || (isLastPage && !clearContainer)) return;

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        if (clearContainer) {
            currentPage = 1;
            isLastPage = false;
            currentCurCode = curCode;
            detailsContainer.removeAllViews();
            tvDetailsTitle.setVisibility(View.GONE);
        }

        String url = UserUtils.BASE_URL + "user-balance-details/?ac_code_dtl=" + user_id + "&page=" + currentPage;
        if (curCode != null && !curCode.isEmpty()) {
            url += "&cur_code=" + curCode;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 0) {
                            tvDetailsTitle.setVisibility(View.VISIBLE);
                            for (int i = 0; i < results.length(); i++) {
                                addDetailItem(results.getJSONObject(i));
                            }
                            currentPage++; // زيادة رقم الصفحة للمرة القادمة
                        } else {
                            isLastPage = true; // لا توجد بيانات أخرى
                        }
                    } catch (JSONException e) {
                        isLastPage = true;
                    }
                },
                error -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SharedPrefsHelper.get(getContext()).getString("auth_token", ""));
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
        ImageView circleIcon = itemView.findViewById(R.id.circleIcon);
        ImageView ivIconbal = itemView.findViewById(R.id.ivIconbal);

        TextView tvCurCodeView = itemView.findViewById(R.id.tvCurCode);
        tvDocType.setText(detail.getString("doc_type_name"));
        double amt = detail.getDouble("amt");
        String curCode = detail.optString("cur_code", "");
//        DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
        // استخدام Locale.ENGLISH يضمن بقاء الأرقام 1234.56
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        formatter.applyPattern("#,###,##0.00");

        String formattedBalance = formatter.format(amt);
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
            ivIconbal.setImageResource(R.drawable.ei_arrow_up);
            ivIconbal.setBackgroundResource(R.drawable.badge_bal_up);
            ivIcon.setImageResource(R.drawable.ic_plus);
            ivIcon.setColorFilter(getResources().getColor(R.color.holo_green_dark));
            circleIcon.setColorFilter(getResources().getColor(R.color.holo_green_dark));
        } else {
            tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            ivIcon.setBackgroundResource(R.drawable.badge_background_bal_new);
            ivIconbal.setImageResource(R.drawable.ei_arrow_dow);
            ivIconbal.setBackgroundResource(R.drawable.badge_bal_dow);
            ivIcon.setImageResource(R.drawable.ic_minus);
            ivIcon.setColorFilter(getResources().getColor(R.color.primary2));
            circleIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        }

        tvDate.setText(detail.getString("doc_date"));
        tvDescription.setText(detail.getString("doc_dsc"));

        String srl = detail.optString("doc_srl", "");
        tvDocSrl.setText(srl + "#");

        mainLayout.setOnClickListener(v -> {
            if (expandableLayout.getVisibility() == View.VISIBLE) {
                expandableLayout.setVisibility(View.GONE);
                ivArrow.animate().rotation(0).setDuration(300).start();
            } else {
                expandableLayout.setVisibility(View.VISIBLE);
                ivArrow.animate().rotation(180).setDuration(300).start();
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
