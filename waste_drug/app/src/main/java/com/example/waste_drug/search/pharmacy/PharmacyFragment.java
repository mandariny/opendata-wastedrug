package com.example.waste_drug.search.pharmacy;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waste_drug.R;
import com.example.waste_drug.data.Pharmacy;
import com.example.waste_drug.search.adapter.PharmacyAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PharmacyFragment extends Fragment {
    private ArrayList<Pharmacy> pharmacyArrayList = null;
    private String requestUrl = "";
    private String searchText = "";
    private RecyclerView recyclerView;
    private SearchView searchView;
    private boolean isSearch = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pharmacy, container, false);
        getInitView(v);
        searchButtonClicked();
        executeAsyncTask();
        return v;
    }

    private void getInitView(View v) {
        searchView = v.findViewById(R.id.search_pharmacy_view);
        recyclerView = v.findViewById(R.id.rv_pharmacy_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void searchButtonClicked() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                isSearch = true;
                searchText = s;
                executeAsyncTask();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void executeAsyncTask() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
    }

    public class MyAsyncTask extends AsyncTask<String, Void, String> {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... strings) {
            if (!isSearch) {
                requestUrl = "http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire?serviceKey=&pageNo=1&numOfRows=50";
            } else {
                requestUrl = "http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire?serviceKey=&QN=" + searchText + "&ORD=NAME&pageNo=1&numOfRows=50";
                isSearch = false;
            }
            try {
                URL url = new URL(requestUrl);
                InputStream stream = url.openStream();
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(stream, StandardCharsets.UTF_8));

                boolean isDutyAddress = false;
                boolean isDutyName = false;
                boolean isDutyTel = false;
                boolean isDutyTimeMonday = false;
                boolean isDutyTimeTuesday = false;
                boolean isDutyTimeWednesday = false;
                boolean isDutyTimeThursday = false;
                boolean isDutyTimeFriday = false;
                boolean isDutyTimeSaturday = false;
                boolean isDutyTimeSunday = false;
                boolean isDutyTimeHoliday = false;

                Pharmacy pharmacy = null;
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            pharmacyArrayList = new ArrayList<>();
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.END_TAG:
                            if (parser.getName().equals("item") && pharmacy != null) {
                                Log.d("MAIN", pharmacy.getDutyTime1c());
                                if (pharmacy.getDutyTime6c() != null) {
                                    pharmacy.setOpenInSaturday(true);
                                } if (pharmacy.getDutyTime7c() != null) {
                                    pharmacy.setOpenInSunday(true);
                                } if (pharmacy.getDutyTime8c() != null) {
                                    pharmacy.setOpenInHoliday(true);
                                } if (Integer.parseInt(pharmacy.getDutyTime1c()) > 1800 ||
                                        Integer.parseInt(pharmacy.getDutyTime2c()) > 1800 ||
                                        Integer.parseInt(pharmacy.getDutyTime3c()) > 1800 ||
                                        Integer.parseInt(pharmacy.getDutyTime4c()) > 1800 ||
                                        Integer.parseInt(pharmacy.getDutyTime5c()) > 1800
                                ) {
                                    pharmacy.setOpenInNight(true);
                                }

                                if(pharmacy.isOpenInNight() || pharmacy.isOpenInSaturday() || pharmacy.isOpenInSunday() || pharmacy.isOpenInHoliday()) {
                                    pharmacyArrayList.add(pharmacy);
                                }
                            }
                            break;
                        case XmlPullParser.START_TAG:
                            switch (parser.getName()) {
                                case "item":
                                    pharmacy = new Pharmacy();
                                    break;
                                case "dutyAddr":
                                    isDutyAddress = true;
                                    break;
                                case "dutyName":
                                    isDutyName = true;
                                    break;
                                case "dutyTel1":
                                    isDutyTel = true;
                                    break;
                                case "dutyTime1c":
                                    isDutyTimeMonday = true;
                                    break;
                                case "dutyTime2c":
                                    isDutyTimeTuesday = true;
                                    break;
                                case "dutyTime3c":
                                    isDutyTimeWednesday = true;
                                    break;
                                case "dutyTime4c":
                                    isDutyTimeThursday = true;
                                    break;
                                case "dutyTime5c":
                                    isDutyTimeFriday = true;
                                    break;
                                case "dutyTime6c":
                                    isDutyTimeSaturday = true;
                                    break;
                                case "dutyTime7c":
                                    isDutyTimeSunday = true;
                                    break;
                                case "dutyTime8c":
                                    isDutyTimeHoliday = true;
                                    break;
                            }
                            break;
                        case XmlPullParser.TEXT:
                            if (isDutyAddress) {
                                pharmacy.setDutyAddr(parser.getText());
                                isDutyAddress = false;
                            } else if (isDutyName) {
                                pharmacy.setDutyName(parser.getText());
                                isDutyName = false;
                            } else if (isDutyTel) {
                                pharmacy.setDutyTel1(parser.getText());
                                isDutyTel = false;
                            } else if (isDutyTimeMonday) {
                                pharmacy.setDutyTime1c(parser.getText());
                                isDutyTimeMonday = false;
                            } else if (isDutyTimeTuesday) {
                                pharmacy.setDutyTime2c(parser.getText());
                                isDutyTimeTuesday = false;
                            } else if (isDutyTimeWednesday) {
                                pharmacy.setDutyTime3c(parser.getText());
                                isDutyTimeWednesday = false;
                            } else if (isDutyTimeThursday) {
                                pharmacy.setDutyTime4c(parser.getText());
                                isDutyTimeThursday = false;
                            } else if (isDutyTimeFriday) {
                                pharmacy.setDutyTime5c(parser.getText());
                                isDutyTimeFriday = false;
                            } else if (isDutyTimeSaturday) {
                                pharmacy.setDutyTime6c(parser.getText());
                                isDutyTimeSaturday = false;
                            } else if (isDutyTimeSunday) {
                                pharmacy.setDutyTime7c(parser.getText());
                                isDutyTimeSunday = false;
                            } else if (isDutyTimeHoliday) {
                                pharmacy.setDutyTime8c(parser.getText());
                                isDutyTimeHoliday = false;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(getContext(), pharmacyArrayList);
            recyclerView.setAdapter(pharmacyAdapter);
        }
    }
}
