package com.example.musafir;

import static com.example.musafir.LocationWorker.getCityNameFromIp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class Onboarding1 extends AppCompatActivity {
    DBHelper dbHelper = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding1);

        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);


        List<Fragment> pages = new ArrayList<>();
        pages.add(new Onboarding1Fragment());
        pages.add(new Onboarding2Fragment());
        pages.add(new Onboarding3Fragment());
        OnboardingAdapter adapter = new OnboardingAdapter(this, pages);
        viewPager.setAdapter(adapter);
        UserUtils.loadVehicleTypesToDB(this);

//        getCityNameFromIp(this, cityAr -> {
//            prefs.edit().putString("default_city", cityAr).apply();
//        });
        getCityNameFromIp(this, (cityAr, cityId) -> {
            SharedPreferences prefs = SharedPrefsHelper.get(this);
//            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("default_city", cityAr).apply();
            prefs.edit().putInt("default_city_id", cityId).apply();
        });
        SharedPreferences prefs = SharedPrefsHelper.get(this);

//        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isMessagesFetched = prefs.getBoolean("messages_fetched", false);
        prefs.edit().putBoolean("optional_update_shown", true).apply();
        UserUtils.fetchRoutes(this, new UserUtils.FetchCallback() {
            @Override
            public void onSuccess(String message) {
                prefs.edit().putBoolean("messages_fetched", true).apply();
            }

            @Override
            public void onError(String error) {
                UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                    @Override
                    public void onSuccess(String message) {
                        UserUtils.ToastMessages(Onboarding1.this, message);
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
            }
        });
        if (!isMessagesFetched) {

            UserUtils.fetchAndSaveCountry(this, new UserUtils.FetchCallback() {
                @Override
                public void onSuccess(String message) {
                    prefs.edit().putBoolean("messages_fetched", true).apply();
                }

                @Override
                public void onError(String error) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(Onboarding1.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
            });
            UserUtils.fetchAndSaveMessages(this, new UserUtils.FetchCallback() {
                @Override
                public void onSuccess(String message) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("messages_fetched", true);
                    editor.apply();
                }

                @Override
                public void onError(String error) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(Onboarding1.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            });
            UserUtils.fetchCompany(this, new UserUtils.OnCodesFetchedListener() {
                @Override
                public void onFetched(JSONArray response) {
                }

                @Override
                public void onError(String error) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(Onboarding1.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
            });
            UserUtils.fetchCodeDetails(this, 5, null, new UserUtils.OnCodesFetchedListener() {
                @Override
                public void onFetched(JSONArray response) {
                }

                @Override
                public void onError(String error) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(Onboarding1.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });

                }
            });

            UserUtils.fetchTypeTravelerRequests(this, dbHelper, new TravelerRequests.OnTypeRequestsFetchedListener() {
                @Override
                public void onFetched(List<DBHelper.TypeTravelerRequest> types) {
                }

                @Override
                public void onError(String error) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(Onboarding1.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            });
            UserUtils.fetchCashBankData(this, dbHelper, new UserUtils.OnCashBankFetchedListener() {
                @Override
                public void onFetched(List<DBHelper.CashBank> types) {
                }

                @Override
                public void onError(String error) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(Onboarding1.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            });

            UserUtils.fetchServiceHome(this, dbHelper, new PageHome.OnServiceHomeFetchedListener() {

                @Override
                public void onFetched(List<DBHelper.ServiceHome> types) {

                }

                @Override
                public void onError(String error) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(Onboarding1.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            });

            UserUtils.fetchAndSavecities(this, new UserUtils.citiesCallback() {
                @Override
                public void onSuccess(String message) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("messages_fetched", true);
                    editor.apply();
                }

                @Override
                public void onError(String error) {
                    UserUtils.getMessageFromLocal(4, dbHelper, new UserUtils.MessageCallback() {
                        @Override
                        public void onSuccess(String message) {
                            UserUtils.ToastMessages(Onboarding1.this, message);
                        }

                        @Override
                        public void onError(String error) {
                        }

                    });
                }
            });
        }
    }
}