package com.example.waste_drug.search.pharmacy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.location.Geocoder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waste_drug.BuildConfig;
import com.example.waste_drug.Map2Activity;
import com.example.waste_drug.R;
import com.example.waste_drug.data.Pharmacy;
import com.example.waste_drug.db.DrugBox;
import com.example.waste_drug.search.adapter.PharmacyAdapter;
import com.example.waste_drug.search.GpsTracker;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PharmacyFragment extends Fragment implements View.OnClickListener{
    private ArrayList<Pharmacy> pharmacyArrayList = null;
    private String requestUrl = "";
    private String searchText = "";
    private RecyclerView recyclerView;
    private SearchView searchView;
    private boolean isSearch = false;
    private final String[] APIKEY = new String[]{BuildConfig.PHARMACY_API_KEY};
    private GpsTracker gpsTracker;
    private Geocoder geocoder;
    private boolean isGps = false;

    private Context mContext;
    private ImageButton show_loc;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pharmacy, container, false);
        getInitView(v);

        mContext = container.getContext();

        searchButtonClicked();
        searchButtonClosed();
        executeAsyncTask();

        show_loc = (ImageButton) v.findViewById(R.id.button4);

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            show_loc.setOnClickListener(this);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

        return v;
    }

    @Override
    public void onClick(View v){
        switch(v.getId())
        {
            case R.id.button4:
            {
                if (!checkLocationServicesStatus()) {
                    showDialogForLocationService();
                }else{
                    getPharmacyInfo();
                }
            }
        }
    }

    public void getPharmacyInfo(){
        gpsTracker = new GpsTracker(mContext);
        geocoder = new Geocoder(mContext, Locale.getDefault());

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        List<Address> address;
        Address add;

        try {
            address = geocoder.getFromLocation(latitude, longitude, 1);
            add = address.get(0);

            String s = add.getThoroughfare().toString();

            isSearch = true;
            isGps = true;
            searchText = s;
            Log.v("tag","searchText: "+searchText);
            executeAsyncTask();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void showDialogForLocationService(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("위치 서비스 필요");
        builder.setMessage("GPS 기능을 이용하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 수정해주세요");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                getActivity().finish();
            }
        });
        builder.create().show();
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
                isGps = false;
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

    public void searchButtonClosed() {
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                isSearch = false;
                executeAsyncTask();
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
                requestUrl = "http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire?serviceKey=" + APIKEY[0] + "&numOfRows=20";
            } else if(isGps){
                requestUrl = "http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire?serviceKey=" + APIKEY[0] + "&Q0=" + searchText + "&ORD=NAME&pageNo=1&numOfRows=50";
                isSearch = false;
            }
            else{
                requestUrl = "http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire?serviceKey=" + APIKEY[0] + "&QN=" + searchText + "&ORD=NAME&pageNo=1&numOfRows=50";
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
                                }
                                if (pharmacy.getDutyTime7c() != null) {
                                    pharmacy.setOpenInSunday(true);
                                }
                                if (pharmacy.getDutyTime8c() != null) {
                                    pharmacy.setOpenInHoliday(true);
                                }
                                if (Integer.parseInt(pharmacy.getDutyTime1c()) > 1800 ||
                                        Integer.parseInt(pharmacy.getDutyTime2c()) > 1800 ||
                                        Integer.parseInt(pharmacy.getDutyTime3c()) > 1800 ||
                                        Integer.parseInt(pharmacy.getDutyTime4c()) > 1800 ||
                                        Integer.parseInt(pharmacy.getDutyTime5c()) > 1800
                                ) {
                                    pharmacy.setOpenInNight(true);
                                }

                                if (pharmacy.isOpenInNight() || pharmacy.isOpenInSaturday() || pharmacy.isOpenInSunday() || pharmacy.isOpenInHoliday()) {
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
                            if(pharmacy != null) {
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

            //Log.v("tag", "PharmacyClickLiestener"+pharmacyArrayList.size()); //보여지는 애들


            PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(getContext(), pharmacyArrayList);
            recyclerView.setAdapter(pharmacyAdapter);
            pharmacyAdapter.setOnItemClicklistener(new PharmacyAdapter.OnPharmacyItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    Intent intent = new Intent(getActivity(), Map2Activity.class);
                    intent.putExtra("pharmacies", pharmacyArrayList);
                    intent.putExtra("position", pos);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], int[] grantResult){
        if(requestCode == 1000){
            boolean check_result = true;

            for(int result: grantResult){
                if(result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if(check_result == true){

            }else{
                getActivity().finish();
            }
        }
    }

}
