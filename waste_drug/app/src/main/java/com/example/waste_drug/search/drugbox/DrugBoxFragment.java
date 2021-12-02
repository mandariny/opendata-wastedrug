package com.example.waste_drug.search.drugbox;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
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
import android.content.Context;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waste_drug.Map1Activity;
import com.example.waste_drug.R;
import com.example.waste_drug.db.AppDatabase;
import com.example.waste_drug.db.DrugBox;
import com.example.waste_drug.search.GpsTracker;
import com.example.waste_drug.search.adapter.DrugBoxAdapter;
import com.example.waste_drug.search.GpsTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrugBoxFragment extends Fragment implements View.OnClickListener{
    private RecyclerView recyclerView;
    private DrugBoxAdapter drugBoxAdapter;
    private SearchView searchView;
    private AppDatabase db = null;
    private ArrayList<DrugBox> drugBox = new ArrayList<>();
    private ArrayList<DrugBox> searchDrugBox = new ArrayList<>();
    private ArrayList<DrugBox> firstDrugBox = new ArrayList<>();
    private List<DrugBox> firstDrugBoxList;
    private GpsTracker gpsTracker;
    private Geocoder geocoder;
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
        View v = inflater.inflate(R.layout.fragment_drugbox, container, false);
        getInitView(v);

        mContext = container.getContext();

        boolean isConnected = isNetworkConnected();

        if(!isConnected){
            showDialogForNetwork();
        }

        getInitDB();
        makeDB();
        saveDB();



        firstDrugBoxList = drugBox.subList(0,20);
        firstDrugBox.addAll(firstDrugBoxList);

        getDB(firstDrugBox);
        searchViewClicked();
        searchViewClosed();

        show_loc = (ImageButton) v.findViewById(R.id.button3);

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
            case R.id.button3:
            {
                if (!checkLocationServicesStatus()) {
                    showDialogForLocationService();
                }else{
                    getSearchDrugBox();
                }
            }
        }
    }

    public void getSearchDrugBox(){
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

            searchDrugBox = new ArrayList<>();
            for(DrugBox drugBox : drugBox) {
                if(drugBox.address.contains(s) || drugBox.name.contains(s)) {
                    searchDrugBox.add(drugBox);
                }
            }

            getDB(searchDrugBox);
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

    public void getInitView(View v) {
        searchView = v.findViewById(R.id.search_drug_box_view);
        recyclerView = v.findViewById(R.id.rv_drug_box_view);
        recyclerView.setHasFixedSize(true);
    }

    public void getInitDB() {
        db = AppDatabase.getInstance(getContext());
    }

    public void saveDB() {
        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                try{
                    for(int i = 0; i < drugBox.size(); i++) {
                        db.drugBoxDao().insertDrugBox(drugBox.get(i));
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        InsertRunnable insertRunnable = new InsertRunnable();
        Thread thread = new Thread(insertRunnable);
        thread.start();
    }

    public void getDB(ArrayList<DrugBox> drugBox) {
        drugBoxAdapter = new DrugBoxAdapter(drugBox);
        recyclerView.setAdapter(drugBoxAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        //이벤트리스너 연결결
        drugBoxAdapter.setOnItemClicklistener(new DrugBoxAdapter.OnDrugBoxItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Intent intent = new Intent(getActivity(), Map1Activity.class);
                intent.putExtra("drugboxes",drugBox);
                intent.putExtra("position", pos);
                startActivity(intent);
            }
        });
    }

    public void searchViewClicked() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchDrugBox = new ArrayList<>();
                for(DrugBox drugBox : drugBox) {
                    if(drugBox.address.contains(s) || drugBox.name.contains(s)) {
                        searchDrugBox.add(drugBox);
                    }
                }

                getDB(searchDrugBox);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }

    public void searchViewClosed() {
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                getDB(firstDrugBox);
                return false;
            }
        });
    }
    public void makeDB() {
        drugBox.add(new DrugBox(0, "서울시 강동구 성내로 45 (성내동)", "강동구보건소 3층 보건의료과", "02-3425-6795"));
        drugBox.add(new DrugBox(1, "서울특별시 양천구 목동서로 339(신정동)", "양천구보건소", "02-2620-3924"));
        drugBox.add(new DrugBox(2, "서울특별시 양천구 남부순환로 407(신월동)", "신월보건지소 ", "02-2620-4751"));
        drugBox.add(new DrugBox(3, "서울특별시 양천구 목동중앙본로7가길 11(목동)", "목동보건지소", "02-2084-5251"));
        drugBox.add(new DrugBox(4, "서울특별시 양천구 남부순환로 331(신월동)", "메디힐병원", "02-2604-7551"));
        drugBox.add(new DrugBox(5, "서울특별시 양천구 신목로 120(목동)", "목동힘찬병원", "02-1899-2221"));
        drugBox.add(new DrugBox(6, "서울특별시 양천구 공항대로 610(목동)", "포미즈여성병원", "02-2651-7500"));
        drugBox.add(new DrugBox(7, "서울특별시 동대문구 서울시립대로 57, 천사병원 B1,1,2,3,6층 (전농동)", "다일천사병원", "02-2213-8004"));
        drugBox.add(new DrugBox(8, "서울특별시 동대문구 왕산로 18 (신설동)", "동서울병원", "02-926-9171"));
        drugBox.add(new DrugBox(9, "서울특별시 동대문구 답십리로 266, 파워스빌딩 (장안동)", "린여성병원", "02-2244-1212"));
        drugBox.add(new DrugBox(10, "서울특별시 동대문구 천호대로83길 44 (장안동)", "맑은수병원", "02-2681-0119"));
        drugBox.add(new DrugBox(11, "서울특별시 동대문구 한천로 49 (답십리동)", "멘토스병원", "02-2214-5100"));
        drugBox.add(new DrugBox(12, "서울특별시 동대문구 왕산로 137 (제기동)", "서울나은병원", "02-1544-6003"));
        drugBox.add(new DrugBox(13, "서울특별시 동대문구 고산자로 421 (용두동)", "서울명병원", "02-965-2800"));
        drugBox.add(new DrugBox(14, "서울특별시 동대문구 천호대로 421 (장안동)", "지혜병원", "02-499-9955"));
        drugBox.add(new DrugBox(15, "서울특별시 동대문구 답십리로 261 (장안동)", "참튼튼병원", "1588-7562"));
        drugBox.add(new DrugBox(16, "서울특별시 동대문구 장한로 143 (장안동)", "코리아병원", "02-2281-9999"));
        drugBox.add(new DrugBox(17, "서울특별시 동대문구 천호대로 145 (용두동)", "동대문구보건소", "02-2127-5428"));
        drugBox.add(new DrugBox(18, "서울특별시 용산구 청파로 383 (서계동)", "소화병원 2층 약제과 내부", "확인불가"));
        drugBox.add(new DrugBox(19, "서울특별시 용산구 이촌로 318 (이촌동)", "금산아산병원 지하1층 약제팀 내부", "확인불가"));
        drugBox.add(new DrugBox(20, "서울특별시 용산구 녹사평대로 150 (이태원동)", "용산구보건소 4층 보건의료과 내부", "확인불가"));
        drugBox.add(new DrugBox(21, "서울특별시 용산구 백범로 329 (원효로1가)", "용산구보건분소(코로나19로 임시폐쇄중) 1층 내부", "확인불가"));
        drugBox.add(new DrugBox(22, "서울특별시 용산구 한남동 대사관로 59 (한남동)", "순천향대학교병원 1층 내부", "확인불가"));
        drugBox.add(new DrugBox(23, "서울특별시 동작구 동작대로29길 36, 1층 (사당동)", "사계시장약국", "02-595-3137"));
        drugBox.add(new DrugBox(24, "서울특별시 동작구 서달로 157 (흑석동)", "소망메디컬약국", "02-826-2223"));
        drugBox.add(new DrugBox(25, "서울특별시 동작구 상도로 246, 1층 (상도동)", "송약국", "02-3280-8710"));
        drugBox.add(new DrugBox(26, "서울특별시 동작구 흑석로 108, 1~2층 (흑석동)", "씨에이(CA)정문약국", "02-825-1122"));
        drugBox.add(new DrugBox(27, "서울특별시 동작구 현충로 75, 원불교100년기념관 (흑석동)", "2층은약국", "02-6484-8248"));
        drugBox.add(new DrugBox(28, "서울특별시 동작구 보라매로 110, 영등포농협 1층 (대방동)", "민들레약국", "02-814-8275"));
        drugBox.add(new DrugBox(29, "서울특별시 동작구 흑석로 101, 1층 (흑석동)", "바름약국", "02-827-0308"));
        drugBox.add(new DrugBox(30, "서울특별시 동작구 상도로 345, 1층 (상도동)", "햇살온누리약국", "02-401-6599"));
        drugBox.add(new DrugBox(31, "서울특별시 동작구 동작대로 65, 1층 (사당동)", "수온누리약국", "02-581-6678"));
        drugBox.add(new DrugBox(32, "서울특별시 동작구 만양로 84, 2층 208호 (노량진동, 삼익주상복합아파트)", "아임약국", "02-3280-8312"));
        drugBox.add(new DrugBox(33, "서울특별시 동작구 사당로23바길 9, 동작삼성래미안아파트 상가 202호 (사당동, 동작삼성래미안아파트)", "신정약국", "02-591-3172"));
        drugBox.add(new DrugBox(34, "서울특별시 동작구 등용로 115, 101호 (대방동, 엘리시아빌딩)", "가족건강약국", "02-822-9095"));
        drugBox.add(new DrugBox(35, "서울특별시 동작구 장승배기로 107, 1층 2호 (노량진동)", "엄마랑약국", "02-517-0042"));
        drugBox.add(new DrugBox(36, "서울특별시 동작구 상도로 153, 1층 (상도동)", "바른약국", "02-816-2223"));
        drugBox.add(new DrugBox(37, "서울특별시 동작구 상도로 348, 평강빌딩 1층 (상도동)", "라파약국", "02-817-2846"));
        drugBox.add(new DrugBox(38, "서울특별시 동작구 동작대로29길 69, 금강산보석불가마사우나 303호 (사당동)", "오약국", "02-536-1775"));
        drugBox.add(new DrugBox(39, "서울특별시 동작구 보라매로5길 15, 전문건설회관빌딩 지하1층 C-5호 (신대방동)", "보라매서울약국", "02-833-1358"));
        drugBox.add(new DrugBox(40, "서울특별시 동작구 보라매로3길 29, 3층 306호 (신대방동, 해태보라매타워)", "해태사랑약국", "070-4189-7273"));
        drugBox.add(new DrugBox(41, "서울특별시 동작구 양녕로 186, 1층 (상도동)", "꿀약국", "02-823-4521"));
        drugBox.add(new DrugBox(42, "서울특별시 동작구 신대방1가길 38, 105동 3층 309호 (신대방동, 동작상떼빌아파트)", "참사랑약국", "02-831-4091"));
        drugBox.add(new DrugBox(43, "서울특별시 동작구 서달로 166, 2층 (흑석동)", "예쁜약국", "02-812-1029"));
        drugBox.add(new DrugBox(44, "서울특별시 동작구 흑석로 106-9, 2층 (흑석동)", "중문약국", "02-795-5656"));
        drugBox.add(new DrugBox(45, "서울특별시 동작구 노량진로6길 26, 1층 (노량진동)", "화생약국", "02-813-1551"));
        drugBox.add(new DrugBox(46, "서울특별시 동작구 상도로 99, 1층 (상도동)", "희경약국", "02-813-0716"));
        drugBox.add(new DrugBox(47, "서울특별시 동작구 노량진로 152, . 1층 (노량진동)", "메디팜엔비약국", "02-811-7723"));
        drugBox.add(new DrugBox(48, "서울특별시 동작구 상도로 297 (상도1동)", "상도온누리약국", "02-812-1322"));
        drugBox.add(new DrugBox(49, "서울특별시 동작구 서달로 144 (흑석동)", "우정약국", "02-816-7711"));
        drugBox.add(new DrugBox(50, "서울특별시 동작구 남부순환로 2057, 3층 (사당동)", "파란약국", "02-522-4230"));
        drugBox.add(new DrugBox(51, "서울특별시 동작구 동작대로29길 69, 201호 (사당동)", "엄마손약국", "02-592-3913"));
        drugBox.add(new DrugBox(52, "서울특별시 동작구 상도로 247, 1층 (상도동)", "성모약국", "02-815-8342"));
        drugBox.add(new DrugBox(53, "서울특별시 동작구 노들로 674, 노량진수산물도매시장 2층 (노량진동)", "수산약국", "02-2254-8275"));
        drugBox.add(new DrugBox(54, "서울특별시 동작구 동작대로 127, 1층 (사당동)", "광주약국", "02-591-0075"));
        drugBox.add(new DrugBox(55, "서울특별시 동작구 보라매로 61-1 (신대방동)", "단골약국", "02-821-3157"));
        drugBox.add(new DrugBox(56, "서울특별시 동작구 여의대방로22나길 1, 1층 (신대방동)", "유정온누리약국", "02-822-5692"));
        drugBox.add(new DrugBox(57, "서울특별시 동작구 사당로 224, 1층 (사당동)", "친절한약국", "02-584-0072"));
        drugBox.add(new DrugBox(58, "서울특별시 동작구 상도로 407-5, 삼호아파트 상가 109호 (상도동)", "교약국", "02-816-9633"));
        drugBox.add(new DrugBox(59, "서울특별시 동작구 신대방1가길 38, 105동 305, 306호 (신대방동, 동작상떼빌아파트)", "소망약국", "02-6337-3107"));
        drugBox.add(new DrugBox(60, "서울특별시 동작구 사당로 215, 107호 (사당동, 서림빌딩)", "유림온누리약국", "02-532-8225"));
        drugBox.add(new DrugBox(61, "서울특별시 동작구 동작대로 25, 4층 (사당동)", "새미약국", "02-586-7618"));
        drugBox.add(new DrugBox(62, "서울특별시 동작구 서달로8길 4 (흑석동)", "서보약국", "02-812-5399"));
        drugBox.add(new DrugBox(63, "서울특별시 동작구 사당로 308 (사당동, 화인빌딩)", "행복한오늘이수약국", "02-583-5235"));
        drugBox.add(new DrugBox(64, "서울특별시 동작구 동작대로29길 24, 101호 (사당동, 정현빌딩)", "이수약국", "02-534-8204"));
        drugBox.add(new DrugBox(65, "서울특별시 동작구 여의대방로 142 (대방동)", "라임약국", "02-813-7779"));
        drugBox.add(new DrugBox(66, "서울특별시 동작구 노량진로 155 (노량진동)", "우리약국", "02-826-5116"));
        drugBox.add(new DrugBox(67, "서울특별시 동작구 상도로 174 (상도동, 동서남북빌딩)", "메디홈약국", "02-817-1406"));
        drugBox.add(new DrugBox(68, "서울특별시 동작구 흑석로 106 (흑석동)", "대학약국", "02-815-6842"));
        drugBox.add(new DrugBox(69, "서울특별시 동작구 장승배기로 34, 1층 101호 (상도동)", "새싹약국", "070-4147-0521"));
        drugBox.add(new DrugBox(70, "서울특별시 동작구 장승배기로 168, B001호 (노량진동)", "꿈꾸는약국", "02-821-6500"));
        drugBox.add(new DrugBox(71, "서울특별시 동작구 사당로 300, 222호 (사당동, 이수자이)", "이수사랑약국", "02-3473-3171"));
        drugBox.add(new DrugBox(72, "경기도 고양시 덕양구 원당로33번길 28 (주교동)", "덕양구보건소", "031-8075-4014"));
        drugBox.add(new DrugBox(73, "경기도 고양시 일산동구 중앙로 1228 (마두동)", "일산동구보건소", "031-8075-4094"));
        drugBox.add(new DrugBox(74, "경기도 고양시 일산서구 일중로 54 (일산동)", "일산서구보건소", "031-8075-4160"));
        drugBox.add(new DrugBox(75, "경기 구리시 건원대로34번길 84 구리보건소", "구리시보건소", "031-550-8618"));
        drugBox.add(new DrugBox(76, "경기 구리시 체육관로 74 구리시행정복지센터", "수택보건지소", "031-550-2466"));
        drugBox.add(new DrugBox(77, "경기 구리시 산마루로 24 신성타워 5층", "갈매보건지소", "031-550-2562"));
        drugBox.add(new DrugBox(78, "서울특별시 서대문구 연희동 165-2", "서대문구보건소", "02-330-1801"));
        drugBox.add(new DrugBox(79, "서울특별시 서대문구 북가좌동 477", "서대문구보건소 가좌보건지소", "02-3140-8357"));
        drugBox.add(new DrugBox(80, "서울특별시 송파구 양재대로 1222, 올림픽프라자 23-1,23호 (방이동)", "3층메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(81, "서울특별시 송파구 올림픽로35길 112 (신천동, 장미B상가 315호)", "3층하나약국", "확인불가"));
        drugBox.add(new DrugBox(82, "서울특별시 송파구 강동대로 74, 잠실올림픽아이파크상가 1층 108호, 지층 B103호 (풍납동)", "가까운이화약국", "확인불가"));
        drugBox.add(new DrugBox(83, "서울특별시 송파구 충민로 66, 1층 F-1001호 (문정동, 가든파이브라이프 1층, F-1001호)", "가든파이브약국", "확인불가"));
        drugBox.add(new DrugBox(84, "서울특별시 송파구 동남로 207 (가락동)", "가락온누리약국", "확인불가"));
        drugBox.add(new DrugBox(85, "서울특별시 송파구 강동대로 67 (풍납동)", "가장큰현대아산약국", "확인불가"));
        drugBox.add(new DrugBox(86, "서울특별시 송파구 강동대로 74, 108동 101,102,지하102호 (풍납동)", "가장편한대영약국", "확인불가"));
        drugBox.add(new DrugBox(87, "서울특별시 송파구 백제고분로41길 11 (송파동)", "감초당약국", "확인불가"));
        drugBox.add(new DrugBox(88, "서울특별시 송파구 올림픽로 212 (잠실동, 갤러리아팰리스)", "갤러리약국", "확인불가"));
        drugBox.add(new DrugBox(89, "서울특별시 송파구 오금로 505 (거여동, 101호)", "거여서울약국", "확인불가"));
        drugBox.add(new DrugBox(90, "서울특별시 송파구 백제고분로 275, 1층 (석촌동)", "건강샘약국", "확인불가"));
        drugBox.add(new DrugBox(91, "서울시 광진구 자양로 117(자양동)", "광진구 보건소", "확인불가"));
        drugBox.add(new DrugBox(92, "서울특별시 광진구 강변역로4길 10, 강변역지너스타워 303호일부 (구의동)", "삼층약국", "확인불가"));
        drugBox.add(new DrugBox(93, "서울특별시 광진구 면목로 128, 1층 101호 (중곡동)", "늘편한약국", "확인불가"));
        drugBox.add(new DrugBox(94, "서울특별시 광진구 천호대로 666, 그랜드파크오피스텔 지1층 B101호 (구의동)", "파크온누리약국", "확인불가"));
        drugBox.add(new DrugBox(95, "서울특별시 광진구 아차산로 229, 한림타워 1층 103호 (화양동)", "원약국", "확인불가"));
        drugBox.add(new DrugBox(96, "서울특별시 광진구 면목로 127, 1층 (중곡동, 신성그랜드타워)", "신성온누리약국", "확인불가"));
        drugBox.add(new DrugBox(97, "서울특별시 광진구 자양로 109, 104호 (자양동)", "조은우리약국", "확인불가"));
        drugBox.add(new DrugBox(98, "서울특별시 광진구 뚝섬로 503, 1층 (자양동)", "새봄약국", "확인불가"));
        drugBox.add(new DrugBox(99, "서울특별시 광진구 광나루로 351, 1층 (군자동)", "화양백화점약국", "확인불가"));
        drugBox.add(new DrugBox(100, "서울특별시 광진구 천호대로 570, 1층 101호 (능동)", "더나은약국", "확인불가"));
        drugBox.add(new DrugBox(101, "서울특별시 광진구 능동로 110, 스타시티 영존빌딩 지2층 B201호 (화양동)", "나라약국", "확인불가"));
        drugBox.add(new DrugBox(102, "서울특별시 광진구 능동로 209, 세종대학교 대양 AI센터 1층 (군자동)", "세종약국", "확인불가"));
        drugBox.add(new DrugBox(103, "서울특별시 광진구 아차산로 237, 삼진빌딩 5층 (화양동)", "뉴우리약국", "확인불가"));
        drugBox.add(new DrugBox(104, "서울특별시 광진구 동일로 178, 광진캠퍼스시티 1층 104호 (화양동)", "스타온누리약국", "확인불가"));
        drugBox.add(new DrugBox(105, "서울특별시 광진구 긴고랑로 47, 1층 (중곡동)", "서울제일약국", "확인불가"));

        //
        drugBox.add(new DrugBox(262, "서울특별시 강서구 방화대로21길 76 (공항동)", "자성당약국", "2662-3231"));
        drugBox.add(new DrugBox(263, "서울특별시 강서구 강서로17길 141 (화곡동)", "월성약국", "2602-0774"));
        drugBox.add(new DrugBox(264, "서울특별시 강서구 양천로 601 (염창동)", "새은약국", "3665-1370"));
        drugBox.add(new DrugBox(265, "서울특별시 강서구 월정로30길 52 (화곡동)", "미동약국", "2602-4054"));
        drugBox.add(new DrugBox(266, "서울특별시 강서구 강서로7길 90 (화곡동)", "일심당약국", "2602-0856"));
        drugBox.add(new DrugBox(267, "서울특별시 강서구 양천로57길 16 (가양동)", "영보약국", "3664-0552"));
        drugBox.add(new DrugBox(268, "서울특별시 강서구 까치산로 43 (화곡동)", "혜성약국", "2602-6126"));
        drugBox.add(new DrugBox(269, "서울특별시 강서구 양천로 606 (등촌동)", "동산온누리약국", "3663-7953"));
        drugBox.add(new DrugBox(270, "서울특별시 강서구 양천길658 (염창동)", "영일약국", "3661-2954"));
        drugBox.add(new DrugBox(271, "서울특별시 강서구 방화동로 45(방화동)", "조광약국", "2666-1282"));
        drugBox.add(new DrugBox(272, "서울특별시 강서구 강서로5나길 109 (화곡동)", "성혜약국", "2603-1972"));
        drugBox.add(new DrugBox(273, "서울특별시 강서구 공항대로 23 (공항동)", "주약국", "2662-0431"));
        drugBox.add(new DrugBox(274, "서울특별시 강서구 강서로 301 (내발산동)", "내인당약국", "2662-0780"));
        drugBox.add(new DrugBox(275, "서울특별시 강서구 강서로45라길 35 (내발산동)", "신약국", "2663-3115"));
        drugBox.add(new DrugBox(276, "서울특별시 강서구 등촌로13길 31 (화곡동)", "태양약국", "2651-9504"));
        drugBox.add(new DrugBox(277, "서울특별시 강서구 화곡로 346 (화곡동)", "강서연세약국", "2606-1531"));
        drugBox.add(new DrugBox(278, "서울특별시 강서구 우현로 26 (화곡동, 우장산SKVIEW)", "한진약국", "2605-6916"));
        drugBox.add(new DrugBox(279, "서울특별시 강서구 곰달래로 225", "국민약국", "2602-4711"));
        drugBox.add(new DrugBox(280, "서울특별시 강서구 양천로 45-1 (방화동)", "강서힐약국", "2606-9137"));
        drugBox.add(new DrugBox(281, "서울특별시 강서구 공항대로 38,1층 (공항동)", "공항메디칼약국", "3664-5157"));
        drugBox.add(new DrugBox(282, "서울특별시 강서구 화곡로64길 78 (등촌동)", "녹십자약국", "3662-6143"));
        drugBox.add(new DrugBox(283, "서울특별시 강서구 강서로 38 (화곡동)", "감초약국", "2602-5884"));
        drugBox.add(new DrugBox(284, "서울특별시 강서구 하늘길 112, 3층(공항동, 김포공항 국내선청사)", "웰빙국내선약국", "2662-3623"));
        drugBox.add(new DrugBox(285, "서울특별시 강서구 수명로 78, 104호(내발산동)", "그린약국", "2667-6643"));
        drugBox.add(new DrugBox(286, "서울특별시 강서구 방화동로16길 32", "온누리성원약국", "2663-5547"));
        drugBox.add(new DrugBox(287, "서울특별시 강서구 등촌로 173 (등촌동)", "새한약국", "2651-1577"));
        drugBox.add(new DrugBox(288, "서울특별시 강서구 강서로 171 (화곡동)", "대생약국", "2696-8082"));
        drugBox.add(new DrugBox(289, "서울특별시 강서구 등촌로 183 ", "우림약국", "2651-0222"));
        drugBox.add(new DrugBox(290, "서울특별시 강서구 곰달래로 252 (화곡동)", "화평약국", "2647-9126"));
        drugBox.add(new DrugBox(291, "서울특별시 강서구 곰달래로 178 (화곡동)", "온누리용주약국", "2603-3375"));
        drugBox.add(new DrugBox(292, "서울특별시 강서구 등촌로5길 5 (화곡동)", "목화약국", "2646-1412"));
        drugBox.add(new DrugBox(293, "서울특별시 강서구 강서로18다길 34", "우리약국", "2604-1248"));
        drugBox.add(new DrugBox(294, "서울특별시 강서구 곰달래로53가길 3 (화곡동)", "우진약국", "2654-5225"));
        drugBox.add(new DrugBox(295, "서울특별시 강서구 등촌로 79 (등촌동)", "강서약국", "2644-2154"));
        drugBox.add(new DrugBox(296, "서울특별시 강서구 허준로 121, 109호 (가양동, 대림경동아파트)", "구암약국", "2659-8844"));
        drugBox.add(new DrugBox(297, "서울특별시 강서구 허준로 175 (가양동, 가양6단지아파트)", "명문약국", "2659-7374"));
        drugBox.add(new DrugBox(298, "서울특별시 강서구 금낭화로23길 8 (방화동, 방화7단지동성아파트)", "동성약국", "2666-5092"));
        drugBox.add(new DrugBox(299, "서울특별시 강서구 곰달래로 226 (화곡동)", "곰달래약국", "2642-4196"));
        drugBox.add(new DrugBox(300, "서울특별시 강서구 강서로 293-1 (내발산동)", "메디팜푸른약국", "2661-2001"));
        drugBox.add(new DrugBox(301, "서울특별시 강서구 금낭화로23길 25 (방화동, 방화6단지아파트)", "삼정약국", "2661-1618"));
        drugBox.add(new DrugBox(302, "서울특별시 강서구 화곡로 110", "바이엘약국", "2607-0217"));
        drugBox.add(new DrugBox(303, "서울특별시 강서구 양천길 244-2", "송약국", "2659-2526"));
        drugBox.add(new DrugBox(304, "서울특별시 강서구 양천로6길 28 (방화동, 방화12단지아파트)", "미주약국", "2665-7552"));
        drugBox.add(new DrugBox(305, "서울특별시 강서구 강서로56나길 110 (등촌동, 부영아파트)", "부영약국", "3663-1809"));
        drugBox.add(new DrugBox(306, "서울특별시 강서구 양천로57길 37 (가양동, 가양4단지아파트)", "한솔약국", "2659-2425"));
        drugBox.add(new DrugBox(307, "서울특별시 강서구 강서로 56 (화곡동)", "메디팜대야약국", "2608-9074"));
        drugBox.add(new DrugBox(308, "서울특별시 강서구 강서로 257 (내발산동)", "강서종로약국", "2606-8268"));
        drugBox.add(new DrugBox(309, "서울특별시 강서구 양천로57길 36 (가양동, 가양5단지아파트)", "종로당약국", "2668-0215"));
        drugBox.add(new DrugBox(310, "서울특별시 강서구 허준로 47 (가양동, 가양2단지아파트)", "용한약국", "2668-7644"));
        drugBox.add(new DrugBox(311, "서울특별시 강서구 양천로 460 (등촌동)", "가양종로약국", "2659-2356"));
        drugBox.add(new DrugBox(312, "서울특별시 강서구 가로공원로76길 94 (화곡동)", "제일약국", "2601-2431"));
        drugBox.add(new DrugBox(313, "서울특별시 강서구 금낭화로 135 (방화동)", "다마트약국", "2665-0181"));
        drugBox.add(new DrugBox(314, "서울특별시 강서구 양천로 72 (방화동)", "메디팜인정약국", "2664-1121"));
        drugBox.add(new DrugBox(315, "서울특별시 강서구 까치산로4길 3 (화곡동)", "강서메디칼약국", "2602-4567"));
        drugBox.add(new DrugBox(316, "서울특별시 강서구 양천로57길 13 (가양동)", "가양메디칼약국", "3661-3055"));
        drugBox.add(new DrugBox(317, "서울특별시 강서구 강서로 261 (내발산동)", "크리닉약국", "2695-8414"));
        drugBox.add(new DrugBox(318, "서울특별시 강서구 공항대로41길 75 (등촌동)", "메디칼약국", "2668-6068"));
        drugBox.add(new DrugBox(319, "서울특별시 강서구 화곡로 313 (화곡동)", "성모약국", "2601-5300"));
        drugBox.add(new DrugBox(320, "서울특별시 강서구 곰달래로 271 (화곡동)", "건강온누리약국", "2642-0012"));
        drugBox.add(new DrugBox(321, "서울특별시 강서구 강서로 64 (화곡동)", "온누리큰사랑약국", "2691-7005"));
        drugBox.add(new DrugBox(322, "서울특별시 강서구 가로공원로76길 63 (화곡동)", "천수약국", "2692-3552"));
        drugBox.add(new DrugBox(323, "서울특별시 강서구 공항대로41길 76 (등촌동)", "소망약국", "3665-8853"));
        drugBox.add(new DrugBox(324, "서울특별시 강서구 양천로16길 16 (방화동)", "방화프라자약국", "2666-3348"));
        drugBox.add(new DrugBox(325, "서울특별시 강서구 가로공원로76길 56 (화곡동)", "화곡메디칼약국", "2605-4155"));
        drugBox.add(new DrugBox(326, "서울특별시 강서구 공항대로41길 75 (등촌동)", "예약국", "2668-2925"));
        drugBox.add(new DrugBox(327, "서울특별시 강서구 금낭화로 135 (방화동)", "금강약국", "2665-6319"));
        drugBox.add(new DrugBox(328, "서울특별시 강서구 방화동로16길 62 (방화동)", "종로약국", "2665-3388"));
        drugBox.add(new DrugBox(329, "서울특별시 강서구 강서로45길 49-5 (화곡동)", "본초당약국", "2602-4593"));
        drugBox.add(new DrugBox(330, "서울특별시 강서구 양천로 366 (가양동)", "아름약국", "2658-4158"));
        drugBox.add(new DrugBox(331, "서울특별시 강서구 강서로18길 26 (화곡동)", "연수당약국", "2694-7582"));
        drugBox.add(new DrugBox(332, "서울특별시 강서구 강서로54길 79 (등촌동)", "동의송도약국", "2659-0832"));
        drugBox.add(new DrugBox(333, "서울특별시 강서구 양천로 731 (염창동)", "염창메디칼약국", "3663-1147"));
        drugBox.add(new DrugBox(334, "서울특별시 강서구 강서로 191 (화곡동)", "이화약국", "2602-6172"));
        drugBox.add(new DrugBox(335, "서울특별시 강서구 화곡로 206 (화곡동)", "시장약국", "2698-2010"));
        drugBox.add(new DrugBox(336, "서울특별시 강서구 화곡로 194-15 (화곡동)", "다나은약국", "2606-8948"));
        drugBox.add(new DrugBox(337, "서울특별시 강서구 강서로 380 (등촌동)", "발산그랜드약국", "3663-7510"));
        drugBox.add(new DrugBox(338, "서울특별시 강서구 가로공원로76길 100 (화곡동)", "우리온누리약국", "2692-7020"));
        drugBox.add(new DrugBox(339, "서울특별시 강서구 방화대로7나길 29 (공항동)", "다나아약국", "2665-5110"));
        drugBox.add(new DrugBox(340, "서울특별시 강서구 하늘길 74 (과해동)", "기린약국", "2662-0028"));
        drugBox.add(new DrugBox(341, "서울특별시 강서구 양천로 713 (염창동)", "열린온누리약국", "3662-0069"));
        drugBox.add(new DrugBox(342, "서울특별시 강서구 가로공원로76길 91 (화곡동)", "윤약국", "2602-2331"));
        drugBox.add(new DrugBox(343, "서울특별시 강서구 방화동로 10-2 (공항동)", "공항약국", "2661-4889"));
        drugBox.add(new DrugBox(344, "서울특별시 강서구 양천로 461 (가양동)", "하늘팜약국", "3663-7071"));
        drugBox.add(new DrugBox(345, "서울특별시 강서구 금낭화로 91-2 (방화동)", "하나메디칼약국", "2661-1210"));
        drugBox.add(new DrugBox(346, "서울특별시 강서구 강서로 52, 104-105호 (화곡동)", "까치프라자약국", "2604-3005"));
        drugBox.add(new DrugBox(347, "서울특별시 강서구 공항대로 525 (등촌동)", "비원약국", "2658-7575"));
        drugBox.add(new DrugBox(348, "서울특별시 강서구 초원로13길 56 (방화동)", "수약국", "2666-8182"));
        drugBox.add(new DrugBox(349, "서울특별시 강서구 화곡로 173 (화곡동)", "21C 세계로약국", "2607-0624"));
        drugBox.add(new DrugBox(350, "서울특별시 강서구 가로공원로 187 (화곡동)", "제일메디칼약국", "6402-3579"));
        drugBox.add(new DrugBox(351, "서울특별시 강서구 방화동로 66 (방화동)", "해맑은약국", "2661-1837"));
        drugBox.add(new DrugBox(352, "서울특별시 강서구 강서로47가길 6 (내발산동)", "키즈약국", "2661-8158"));
        drugBox.add(new DrugBox(353, "서울특별시 강서구 까치산로 36 (화곡동)", "노을약국", "2605-4280"));
        drugBox.add(new DrugBox(354, "서울특별시 강서구 하늘길 112, 3층(공항동, 김포공항 국내선청사)", "국제공항약국", "2664-1350"));
        drugBox.add(new DrugBox(355, "서울특별시 강서구 양천로57길 9-7 (가양동)", "새서울약국", "2659-6755"));
        drugBox.add(new DrugBox(356, "서울특별시 강서구 공항대로 627 (염창동)", "비전약국", "3665-8274"));
        drugBox.add(new DrugBox(357, "서울특별시 강서구 초록마을로 37 (화곡동)", "참사랑약국", "2696-4111"));
        drugBox.add(new DrugBox(358, "서울특별시 강서구 공항대로41길 65 (등촌동)", "푸른약국", "2063-3861"));
        drugBox.add(new DrugBox(359, "서울특별시 강서구 까치산로 120 (화곡동)", "정다운약국", "2065-2998"));
        drugBox.add(new DrugBox(360, "서울특별시 강서구 공항대로 426 (화곡동)", "가까운약국", "2697-7796"));
        drugBox.add(new DrugBox(361, "서울특별시 강서구 방화동로 56 (방화동)", "온누리다나약국", "2665-6200"));
        drugBox.add(new DrugBox(362, "서울특별시 강서구 월정로 160 (화곡동, 화곡대림아파트)", "선일약국", "2602-4095"));
        drugBox.add(new DrugBox(363, "서울특별시 강서구 화곡로68길 103, 106호(등촌동, 우성아파트상가)", "웰빙약국", "3665-6776"));
        drugBox.add(new DrugBox(364, "서울특별시 강서구 가로공원로76길 75 (화곡동)", "지혜약국", "2603-5553"));
        drugBox.add(new DrugBox(365, "서울특별시 강서구 양천길 500 (등촌동)", "고운마음약국", "3663-7779"));
        drugBox.add(new DrugBox(366, "서울특별시 강서구 양천로69길 , 1층 101호(염창동)", "금비약국", "2658-7767"));
        drugBox.add(new DrugBox(367, "서울특별시 강서구 강서로 43-17 (화곡동)", "희망찬약국", "2606-8287"));
        drugBox.add(new DrugBox(368, "서울특별시 강서구 강서로 205 (화곡동)", "화곡태평양약국", "2693-1044"));
        drugBox.add(new DrugBox(369, "서울특별시 강서구 화곡로 176-4 (화곡동)", "화곡명문약국", "2604-4533"));
        drugBox.add(new DrugBox(370, "서울특별시 강서구 수명로 68-11, 107호(내발산동, 발산타워)", "수명산약국", "2667-6363"));
        drugBox.add(new DrugBox(371, "서울특별시 강서구 양천로 104 (방화동)", "메디칼건강약국", "2661-3337"));
        drugBox.add(new DrugBox(372, "서울특별시 강서구 월정로30길 102 (화곡동)", "상록수약국", "325-2168"));
        drugBox.add(new DrugBox(373, "서울특별시 강서구 금낭화로 136 (방화동)", "하늘약국", "2661-1278"));
        drugBox.add(new DrugBox(374, "서울특별시 강서구 강서로18길 14 (화곡동)", "편안한약국", "2693-9105"));
        drugBox.add(new DrugBox(375, "서울특별시 강서구 수명로 82, 102호 (내발산동, 발산로얄타워)", "행복한온누리약국", "2667-8835"));
        drugBox.add(new DrugBox(376, "서울특별시 강서구 강서로 318 (우장산동)", "우리팜약국", "2662-1278"));
        drugBox.add(new DrugBox(377, "서울특별시 강서구 가로공원로82길 41-8 (화곡동)", "건강한온누리약국", "2606-0012"));
        drugBox.add(new DrugBox(378, "서울특별시 강서구 양천로 452 (등촌동)", "예다인온누리약국", "2658-7115"));
        drugBox.add(new DrugBox(379, "서울특별시 강서구 강서로 254, 302호 (화곡동, 이편한세상상가)", "화곡서울약국", "2692-3800"));
        drugBox.add(new DrugBox(380, "서울특별시 강서구 방화대로34길 92, 1층 (방화동)", "푸른온누리약국", "2664-6643"));
        drugBox.add(new DrugBox(381, "서울특별시 강서구 강서로 143 (화곡동)", "예성약국", "2699-9513"));
        drugBox.add(new DrugBox(382, "서울특별시 강서구 강서로47가길 16 (내발산동)", "큰덕약국", "2664-0717"));
        drugBox.add(new DrugBox(383, "서울특별시 강서구 강서로56길 30 (등촌동)", "아름드리약국", "3661-2060"));
        drugBox.add(new DrugBox(384, "서울특별시 강서구 등촌로5길 80 (화곡동)", "남부시장약국", "2645-0101"));
        drugBox.add(new DrugBox(385, "서울특별시 강서구 양천로 128 (방화동, 한림리첸빌)", "신방화약국", "2666-2040"));
        drugBox.add(new DrugBox(386, "서울특별시 강서구 화곡로 197, 1-3호 (화곡동)", "에이스약국", "2608-5294"));
        drugBox.add(new DrugBox(387, "서울특별시 강서구 화곡로61길 26 (등촌동)", "SooPharm수약국", "2659-0053"));
        drugBox.add(new DrugBox(388, "서울특별시 강서구 양천로 31 (방화동)", "개화약국", "2662-8028"));
        drugBox.add(new DrugBox(389, "서울특별시 강서구 하늘길 38, 지하2층 (방화동)", "스카이약국", "6116-1685"));
        drugBox.add(new DrugBox(390, "서울특별시 강서구 강서로 299 (내발산동)", "미즈정문약국", "2665-0302"));
        drugBox.add(new DrugBox(391, "서울특별시 강서구 방화동로 70 (방화동)", "김포약국", "2663-7575"));
        drugBox.add(new DrugBox(392, "서울특별시 강서구 곰달래로 260, 1층(화곡동)", "길약국", "2653-6008"));
        drugBox.add(new DrugBox(393, "서울특별시 강서구 화곡로 347 (화곡동, 그랜드아이파크)", "봄약국", "2601-6886"));
        drugBox.add(new DrugBox(394, "서울특별시 강서구 까치산로 151 (화곡동)", "한우리약국", "2699-0456"));
        drugBox.add(new DrugBox(395, "서울특별시 강서구 양천로 684 (염창동)", "애플약국", "3663-1205"));
        drugBox.add(new DrugBox(396, "서울특별시 강서구 공항대로 34 (공항동)", "송정온누리약국", "2661-7563"));
        drugBox.add(new DrugBox(397, "서울특별시 강서구 공항대로 437 (등촌동)", "흥부약국", "3663-1972"));
        drugBox.add(new DrugBox(398, "서울특별시 강서구 양천로57길 10-20 (가양동, 이스타빌Ⅰ)", "서연약국", "3663-6596"));
        drugBox.add(new DrugBox(399, "서울특별시 강서구 허준로 23, 107호 (가양동, 한강타운아파트상가)", "한강메디칼약국", "2659-7489"));
        drugBox.add(new DrugBox(400, "서울특별시 강서구 화곡로324 (화곡동)", "월드팜약국", "2692-4047"));
        drugBox.add(new DrugBox(401, "서울특별시 강서구 양천로 600, 1층 (등촌동)", "맑은약국", "2083-0456"));
        drugBox.add(new DrugBox(402, "서울특별시 강서구 양천로 401, A동 109-2호 (가양동, 강서한강자이타워)", "강서자이온누리약국", "2638-5233"));
        drugBox.add(new DrugBox(403, "서울특별시 강서구 공항대로41길 65, 132호 (등촌동, 그랜드상가)", "화창한약국", "3661-1076"));
        drugBox.add(new DrugBox(404, "서울특별시 강서구 남부순환로11가길 104 1층 (화곡동)", "원약국", "2064-7599"));
        drugBox.add(new DrugBox(405, "서울특별시 강서구 공항대로41길66, 세신종합상가 1층 133호", "참미래약국", "2063-3549"));
        drugBox.add(new DrugBox(406, "서울특별시 강서구 양천로59길 46, 1층 103호(가양동)", "새봄약국", "2063-3779"));
        drugBox.add(new DrugBox(407, "서울특별시 강서구 강서로16길 32 (화곡동)", "이수약국", "2696-5878"));
        drugBox.add(new DrugBox(408, "서울특별시 강서구 화곡로 153 (화곡동)", "굿모닝약국", "2695-0777"));
        drugBox.add(new DrugBox(409, "서울특별시 강서구 강서로 27, 103호 (화곡동)", "나라약국", "2694-9228"));
        drugBox.add(new DrugBox(410, "서울특별시 강서구 양천로 655, 2층 (염창동)", "차오름약국", "2658-7577"));
        drugBox.add(new DrugBox(411, "서울특별시 강서구 강서로 186 (화곡동)", "동방약국", "2605-4250"));
        drugBox.add(new DrugBox(412, "서울특별시 강서구 강서로 254, 203호(화곡동, 우장산e편한세상아이파크상가)", "새싹약국", "2695-8312"));
        drugBox.add(new DrugBox(413, "서울특별시 강서구 강서로 242 (화곡동,강서힐스테이트상가 321호)", "보람약국", "2699-7057"));
        drugBox.add(new DrugBox(414, "서울특별시 강서구 공항대로 383, 1층 4호 (등촌동)", "굿모닝이화약국", "3661-8896"));
        drugBox.add(new DrugBox(415, "서울특별시 강서구 양천로 73 (방화동)", "미소약국", "2664-2551"));
        drugBox.add(new DrugBox(416, "서울특별시 강서구 마곡중앙5로 81, 107호, 108호", "마곡메디칼약국", "2665-7585"));
        drugBox.add(new DrugBox(417, "서울특별시 강서구 까치산로 73, 1층 (화곡동)", "건강샘온누리약국", "2062-0155"));
        drugBox.add(new DrugBox(418, "서울특별시 강서구 마곡중앙5로 87(마곡동), 107호", "메디팜정다운약국", "2664-1088"));
        drugBox.add(new DrugBox(419, "서울특별시 강서구 등촌로 31, 2층 (화곡동)", "코끼리약국", "2652-0429"));
        drugBox.add(new DrugBox(420, "서울특별시 강서구 가로공원로 76길 47 (화곡동)", "우정약국", "2690-2025"));
        drugBox.add(new DrugBox(421, "서울특별시 강서구 등촌로 147, 유석빌딩 1층(등촌동)", "한마음약국", "2652-6692"));
        drugBox.add(new DrugBox(422, "서울특별시 강서구 양천로 431 지하1층(가양동)", "메디참사랑약국", "3663-7133"));
        drugBox.add(new DrugBox(423, "서울특별시 강서구 화곡로63길 133 (등촌동)", "산소망약국", "6335-1113"));
        drugBox.add(new DrugBox(424, "서울특별시 강서구 화곡로138, 1층 (화곡동)", "메디팜사랑약국", "2601-8498"));
        drugBox.add(new DrugBox(425, "서울특별시 강서구 화곡로 142, 114호 (화곡동, 메가박스빌딩)", "햇살온누리약국", "2699-6314"));
        drugBox.add(new DrugBox(426, "서울특별시 강서구 강서로62길 98, 1층 101호 (등촌동)", "한빛약국", "2658-7371"));
        drugBox.add(new DrugBox(427, "서울특별시 강서구 공항대로261, 306호 (마곡동, 발산파크프라자)", "발산 93약국", "2063-7582"));
        drugBox.add(new DrugBox(428, "서울특별시 강서구 등촌로59-1, 1층 (화곡동)", "햇빛온누리약국", "2062-7159"));
        drugBox.add(new DrugBox(429, "서울특별시 강서구 등촌로 71 ,1층 (등촌동)", "태평양약국", "2652-7214"));
        drugBox.add(new DrugBox(430, "서울특별시 강서구 강서로 43, B104호 (화곡동)", "365그린약국", "2602-8006"));
        drugBox.add(new DrugBox(431, "서울특별시 강서구 강서로 259, 103호 (내발산동)", "일신약국", "2601-4308"));
        drugBox.add(new DrugBox(432, "서울특별시 강서구 강서로 385, 106호 (마곡동, 우성에스비타워)", "마곡하나약국", "3664-0319"));
        drugBox.add(new DrugBox(433, "서울특별시 강서구 화곡로 331 (화곡동)", "유약국", "2695-7576"));
        drugBox.add(new DrugBox(434, "서울특별시 강서구 강서로 391, 108호 (마곡동, 문영비즈웍스)", "사과약국", "3663-0625"));
        drugBox.add(new DrugBox(435, "서울특별시 강서구 방화동로 14, 1층 (공항동)", "신라약국", "2665-3344"));
        drugBox.add(new DrugBox(436, "서울특별시 강서구 강서로 13길 3 (화곡동)", "라임약국", "070-8868-9161"));
        drugBox.add(new DrugBox(437, "서울특별시 강서구 화곡로 194-26, 1층 (화곡동)", "자연주의약국", "2608-5264"));
        drugBox.add(new DrugBox(438, "서울특별시 강서구 마곡중앙5로 6, 2층 233호 (마곡동, 마곡나루역보타닉푸르지오시티)", "마곡소망약국", "3662-6111"));
        drugBox.add(new DrugBox(439, "서울특별시 강서구 양천로 690, 1층 (염창동, 호서빌딩)", "밝은약국", "3664-0549"));
        drugBox.add(new DrugBox(440, "서울특별시 강서구 화곡로 196, 1층 (화곡동)", "새미래약국", "2602-5269"));
        drugBox.add(new DrugBox(441, "서울특별시 강서구 강서로45길 174, 3층 (내발산동)", "나무약국", "2665-7565"));
        drugBox.add(new DrugBox(442, "서울특별시 강서구 수명로 76, 102호(내발산동)", "마곡정문약국", "2667-1131"));
        drugBox.add(new DrugBox(443, "서울특별시 강서구 곰달래로 223, 104호(화곡동)", "화곡부성약국", "722-7585"));
        drugBox.add(new DrugBox(444, "서울특별시 강서구 강서로 463, 1층(마곡동,새싹타워)", "마곡새봄약국", "2659-6470"));
        drugBox.add(new DrugBox(445, "서울특별시 강서구 방화대로 294, 114호,115호(마곡동,마곡더블유타워)", "마곡열린약국", "2661-8171"));
        drugBox.add(new DrugBox(446, "서울특별시 강서구 화곡로 398 ,1층(등촌동)", "강서홈약국", "2659-1823"));
        drugBox.add(new DrugBox(447, "서울특별시 강서구 강서로 206, 1층(화곡동)", "더건강약국", "2699-2275"));
        drugBox.add(new DrugBox(448, "서울특별시 강서구 강서로 242, 상가동 4층 17호(화곡동, 강서힐스테이트 )", "이소약국", "2699-8681"));
        drugBox.add(new DrugBox(449, "서울특별시 강서구 강서로47가길 12(내발산동)", "미즈약국", "2661-2681"));
        drugBox.add(new DrugBox(450, "서울특별시 강서구 강서로 307, 서울스타병원빌딩 1층 (내발산동)", "대성약국", "2691-6675"));
        drugBox.add(new DrugBox(451, "서울특별시 강서구 강서로 360, 솔레드림 109호 (내발산동)", "발산에이스약국", "2658-5525"));
        drugBox.add(new DrugBox(452, "서울특별시 강서구 강서로 251, 103호(내발산동)", "세란온누리약국", "2065-1103"));
        drugBox.add(new DrugBox(453, "서울특별시 강서구 양천로 583, 우림블루나인비즈니스센터 A-118호(염창동)", "준온누리약국", "2093-7900"));
        drugBox.add(new DrugBox(454, "서울특별시 강서구 공항대로 272, 1층(내발산동)", "서울메디약국", "2666-2101"));
        drugBox.add(new DrugBox(455, "서울특별시 강서구 강서로 341, 1층 2호, 3호(내발산동)", "바로약국", "2662-8588"));
        drugBox.add(new DrugBox(456, "서울특별시 강서구 공항대로36길 9, 1층(내발산동)", "가까운천사약국", "2662-0266"));
        drugBox.add(new DrugBox(457, "서울특별시 강서구 공항대로67길 29-26, 1층(염창동) ", "강서보건약국", "2088-7501"));
        drugBox.add(new DrugBox(458, "서울특별시 강서구 강서로 341, 1층5호(내발산동)", "봄봄약국", "2662-6788"));
        drugBox.add(new DrugBox(459, "서울특별시 강서구 마곡서1로 115-1, 105~106호(마곡동, 마곡헤리움1차)", "마곡프라자약국", "070-8881-1122"));
        drugBox.add(new DrugBox(460, "서울특별시 강서구 마곡중앙5로 6, 227호 (마곡동, 마곡나루역보타닉푸르지오시티)", "마곡하늘약국", "3664-7374"));
        drugBox.add(new DrugBox(461, "서울특별시 강서구 공항대로 269-15, 309-1호(마곡동, 힐스테이트에코마곡)", "다온약국", "3665-9588"));
        drugBox.add(new DrugBox(462, "서울특별시 강서구 마곡중앙6로 66, 112호(마곡동, 퀸즈파크텐)", "달약국", "6411-9112"));
        drugBox.add(new DrugBox(463, "서울특별시 강서구 강서로 267, 1층(내발산동)", "프라자약국", "2664-8707"));
        drugBox.add(new DrugBox(464, "서울특별시 강서구 마곡중앙1로 72, 305호 (마곡동, 마곡엠밸리10단지 오피스)", "튼튼약국", "2664-0929"));
        drugBox.add(new DrugBox(465, "서울특별시 강서구 마곡중앙4로 74, 106~107호(마곡동, 이웰메디파크)", "가까운중앙약국", "2662-3675"));
        drugBox.add(new DrugBox(466, "서울특별시 강서구 마곡중앙4로 74, 101~103호(마곡동, 이웰메디파크)", "빠른대학온누리약국", "2661-2525"));
        drugBox.add(new DrugBox(467, "서울특별시 강서구 곰달래로49길 29 (화곡동)", "더조은약국", "2699-7007"));
        drugBox.add(new DrugBox(468, "서울특별시 강서구 공항대로41길 52, 202호(등촌동)", "서울사랑약국", "2658-7825"));
        drugBox.add(new DrugBox(469, "서울특별시 강서구 양천로 564, 131호 (등촌동, 두산위브센티움)", "솔약국", "3662-8899"));
        drugBox.add(new DrugBox(470, "서울특별시 강서구 공항대로 164, 104호(마곡동, 류마타워)", "초록문약국", "2666-7062"));
        drugBox.add(new DrugBox(471, "서울특별시 강서구 마곡중앙4로 74, 104~105호(마곡동, 이웰메디파크)", "이화정문약국", "2662-4863"));
        drugBox.add(new DrugBox(472, "서울특별시 강서구 공항대로 237, 107호(마곡동, 에이스타워마곡)", "밸런스약국", "3663-5677"));
        drugBox.add(new DrugBox(473, "서울특별시 강서구 화곡로 190, 1층(화곡동)", "늘푸른약국", "2604-9912"));
        drugBox.add(new DrugBox(474, "서울특별시 강서구 공항대로 168, 117~118호(마곡동, 747타워)", "맘온누리약국", "2661-7327"));
        drugBox.add(new DrugBox(475, "서울특별시 강서구 강서로 193, 101호(화곡동)", "행복나무약국", "2065-8864"));
        drugBox.add(new DrugBox(476, "서울특별시 강서구 화곡로 166, 1층 (화곡동)", "라온365온누리약국", "2699-6924"));
        drugBox.add(new DrugBox(477, "서울특별시 강서구 강서로 154, 101호(화곡동, 힐탑빌딩)", "샛별약국", "2607-6259"));
        drugBox.add(new DrugBox(478, "서울특별시 강서구 양천로 94, 1층(방화동)", "방화우리약국", "2663-8811"));
        drugBox.add(new DrugBox(479, "서울특별시 강서구 화곡로 161, 104~105호(화곡동, 대성빌딩)", "연세약국", "2065-8234"));
        drugBox.add(new DrugBox(480, "서울특별시 강서구 양천로 677, 1층(염창동)", "하나로약국", "3662-3500"));
        drugBox.add(new DrugBox(481, "서울특별시 강서구 양천로 476, 402호(등촌동, 금부빌딩)", "엔젤약국", "070-7543-5108"));
        drugBox.add(new DrugBox(482, "서울특별시 강서구 강서로 194-9, 1층(화곡동)", "선약국", "2605-0556"));
        drugBox.add(new DrugBox(483, "서울특별시 강서구 마곡중앙로 161-8, A210호(마곡동, 두산더랜드파크)", "유니약국", "6989-8070"));
        drugBox.add(new DrugBox(484, "서울특별시 강서구 공항대로 206, 111호(마곡동, 나인스퀘어)", "김비타약국", "2662-4860"));
        drugBox.add(new DrugBox(485, "서울특별시 강서구 공항대로 248, 111~113호, 206호(마곡동)", "가장큰이대약국", "2662-8279"));
        drugBox.add(new DrugBox(486, "서울특별시 강서구 양천로 556, 103호(등촌동)", "싱싱약국", "3664-3215"));
        drugBox.add(new DrugBox(487, "서울특별시 강서구 마곡중앙로 161-17, 110호(마곡동, 보타닉파크타워Ⅰ)", "명진팜약국", "6326-7353"));
        drugBox.add(new DrugBox(488, "서울특별시 강서구 방화동로 37, 102호(방화동, 메디스타워)", "공항시장샘온누리약국", "2064-2204"));
        drugBox.add(new DrugBox(489, "서울특별시 강서구 수명로 80, 102호 (내발산동, 베스트프라자)", "우성약국", "2667-3300"));
        drugBox.add(new DrugBox(490, "서울특별시 강서구 양천로 556, 강서메디칼센터 105호(등촌동)", "대현온누리약국", "2668-2525"));
        drugBox.add(new DrugBox(491, "서울특별시 강서구 양천로 660, 지층 L02호(염창동, 왈도강서캠퍼스)", "솔트약국", "2658-1770"));
        drugBox.add(new DrugBox(492, "서울특별시 강서구 화곡로 339, 1층(화곡동, 오선빌딩)", "파랑약국", "2604-5480"));
        drugBox.add(new DrugBox(493, "서울특별시 강서구 화곡로 152, 1층(화곡동)", "대학약국", "2603-0138"));
        drugBox.add(new DrugBox(494, "서울특별시 강서구 가로공원로76길 51, 1층(화곡동)", "휴베이스비타민약국", "2607-6007"));
        drugBox.add(new DrugBox(495, "서울특별시 강서구 등촌로5길 2, 104~105호(화곡동)", "기쁨약국", "6964-6988"));
        drugBox.add(new DrugBox(496, "서울특별시 강서구 양천로14길 23(방화동)", "미래약국", "2662-0377"));
        drugBox.add(new DrugBox(497, "서울특별시 강서구 공항대로 168, 106-1호(마곡동, 747타워)", "마곡스타약국", "6407-3535"));
        drugBox.add(new DrugBox(498, "서울특별시 강서구 방화동로 92, 103호(방화동)", "에스메디약국", "2666-9774"));
        drugBox.add(new DrugBox(499, "서울특별시 강서구 양천로559, 2층(가양이마트)", "2층즐거운약국", "3664-0420"));
        drugBox.add(new DrugBox(500, "서울특별시 강서구 마곡중앙로 59-21, 120호(마곡동)", "이수연약국", "2661-0118"));
        drugBox.add(new DrugBox(501, "서울특별시 강서구 화곡로 301, 103호(화곡동)", "정성가득한약국", "2604-0901"));
        drugBox.add(new DrugBox(502, "서울특별시 강서구 허준로 198, 104~105호(가양동)", "가양누리약국", "3663-8013"));
        drugBox.add(new DrugBox(503, "서울특별시 강서구 허준로 12, 103호(가양동)", "벼리약국", "3662-0072"));
        drugBox.add(new DrugBox(504, "서울특별시 강서구 공항대로 지하 267, 발산역 515-106호(마곡동)", "지하철정문약국", "0507-1369-6006"));
        drugBox.add(new DrugBox(505, "서울특별시 강서구 등촌로 177, 101동 102호(등촌동)", "밝은온누리약국", "2645-4288"));
        drugBox.add(new DrugBox(506, "서울특별시 강서구 공항대로 267 515-107호(마곡동)", "발산역지하철약국", "0507-1333-3954"));
        drugBox.add(new DrugBox(507, "서울특별시 강서구 마곡서로 157, 스프링파크타워 208-2호(마곡동)", "마곡나루약국", "070-4352-4547"));
        drugBox.add(new DrugBox(508, "서울특별시 강서구 강서로 242, 151호(화곡동, 강서힐스테이트)", "힐스테이트솔약국", "2695-6121"));
        drugBox.add(new DrugBox(509, "서울특별시 강서구 화곡로 162, 1층(화곡동)", "메디팜21세기약국", "2603-4569"));
        drugBox.add(new DrugBox(510, "서울특별시 강서구 공항대로 271, 이천이프라자 1층 103호(마곡동)", "더나은약국", "3663-1335"));
        drugBox.add(new DrugBox(106, "서울특별시 광진구 뚝섬로27길 65 (자양동)", "세영약국", "확인불가"));
        drugBox.add(new DrugBox(107, "서울특별시 광진구 동일로 74, 1층 102호 (자양동)", "치료의빛예은약국", "확인불가"));
        drugBox.add(new DrugBox(108, "서울특별시 광진구 능동로 110, 스타시티영존 지하2층 B203-1,204호 (화양동)", "건대온누리약국", "확인불가"));
        drugBox.add(new DrugBox(109, "서울특별시 광진구 광나루로56길 34, 구의동현대2단지 종합상가 1층 112호 (구의동)", "백향목약국", "확인불가"));
        drugBox.add(new DrugBox(110, "서울특별시 광진구 자양로 48, 1층 (자양동)", "태평양약국", "확인불가"));
        drugBox.add(new DrugBox(111, "서울특별시 광진구 천호대로 557, 풍국빌딩 1층 (중곡동)", "군자한마음약국", "확인불가"));
        drugBox.add(new DrugBox(112, "서울특별시 광진구 아차산로 373, 원이빌딩 1층 (구의동)", "미소온누리약국", "확인불가"));
        drugBox.add(new DrugBox(113, "서울특별시 광진구 자양로 87, 1층 (자양동)", "느티나무약국", "확인불가"));
        drugBox.add(new DrugBox(114, "서울특별시 광진구 광나루로56길 85, 테크노-마트21 지하1층 D-12호 (구의동)", "현대온누리약국", "확인불가"));
        drugBox.add(new DrugBox(115, "서울특별시 광진구 천호대로 556, 1층 (능동)", "군자역우리약국", "확인불가"));
        drugBox.add(new DrugBox(116, "서울특별시 광진구 아차산로 375, 크레신타워3차 1층 107호 (구의동)", "라임약국", "확인불가"));
        drugBox.add(new DrugBox(117, "서울특별시 광진구 자양로13길 4-1 (자양동)", "참약국", "확인불가"));
        drugBox.add(new DrugBox(118, "서울특별시 광진구 용마산로 5, 명클리닉 1층 101호 (중곡동)", "새수정온누리약국", "확인불가"));
        drugBox.add(new DrugBox(119, "서울특별시 광진구 아차산로 502, 진넥스 오딧세이 106,107호 (광장동)", "청구약국", "확인불가"));
        drugBox.add(new DrugBox(120, "서울특별시 광진구 광나루로56길 63, 현대프라임아파트 프라자 107,108,109호 (구의동)", "온누리강변프라자약국", "확인불가"));
        drugBox.add(new DrugBox(121, "서울특별시 광진구 자양로 88, 광영빌딩 1층 (자양동)", "자양약국", "확인불가"));
        drugBox.add(new DrugBox(122, "서울특별시 광진구 아차산로 450-2, 1층 (구의동)", "튼튼온누리약국", "확인불가"));
        drugBox.add(new DrugBox(123, "서울특별시 광진구 동일로 278, 나성빌딩 1층 (군자동)", "대화약국", "확인불가"));
        drugBox.add(new DrugBox(124, "서울특별시 광진구 동일로 92-1, 1층 (자양동)", "스마트온누리약국", "확인불가"));
        drugBox.add(new DrugBox(125, "서울특별시 광진구 천호대로122길 16, 1층 (능동)", "성원약국", "확인불가"));
        drugBox.add(new DrugBox(126, "서울특별시 광진구 뚝섬로 631-1, 1층 (자양동)", "힘찬약국", "확인불가"));
        drugBox.add(new DrugBox(127, "서울특별시 광진구 면목로 190, 1층 (중곡동)", "기쁨약국", "확인불가"));
        drugBox.add(new DrugBox(128, "서울특별시 광진구 뚝섬로 595, 1층 (자양동)", "뚝도약국", "확인불가"));
        drugBox.add(new DrugBox(129, "서울특별시 광진구 용마산로23길 28, 내담터 1층 (중곡동)", "서문사랑약국", "확인불가"));
        drugBox.add(new DrugBox(130, "서울특별시 광진구 광나루로 604, 진넥스 베르디엠 2층 202호 (구의동)", "하늘약국", "확인불가"));
        drugBox.add(new DrugBox(131, "서울특별시 광진구 천호대로 653, 1층 (중곡동)", "디딤온누리약국", "확인불가"));
        drugBox.add(new DrugBox(132, "서울특별시 광진구 면목로 117, 1층 102호 (중곡동, 바론채)", "소라약국", "확인불가"));
        drugBox.add(new DrugBox(133, "서울특별시 광진구 긴고랑로 41 (중곡동, 대화탕)", "다온약국", "확인불가"));
        drugBox.add(new DrugBox(134, "서울특별시 광진구 천호대로111길 19 (중곡동)", "햇님약국", "확인불가"));
        drugBox.add(new DrugBox(135, "서울특별시 광진구 아차산로 621, 1층 (광장동)", "한솔약국", "확인불가"));
        drugBox.add(new DrugBox(136, "서울특별시 광진구 능동로 378, 1층 (중곡동)", "새동산약국", "확인불가"));
        drugBox.add(new DrugBox(137, "서울특별시 광진구 능동로 315 (중곡동, 대남빌딩)", "프라자사랑약국", "확인불가"));
        drugBox.add(new DrugBox(138, "서울특별시 광진구 자양로 287 (구의동)", "아차산하이약국", "확인불가"));
        drugBox.add(new DrugBox(139, "서울특별시 광진구 자양로15길 18, 1층 (자양동, 혜성빌딩)", "누리온누리약국", "확인불가"));
        drugBox.add(new DrugBox(140, "서울특별시 광진구 용마산로 4, 1층 (중곡동)", "메디팜백악관약국", "확인불가"));
        drugBox.add(new DrugBox(141, "서울특별시 광진구 자양로13길 47, 1층 (자양동, 욱영빌딩)", "옵티마세란약국", "확인불가"));
        drugBox.add(new DrugBox(142, "서울특별시 광진구 능동로 420 (중곡동, 성문빌딩)", "센터온누리약국", "확인불가"));
        drugBox.add(new DrugBox(143, "서울특별시 광진구 아차산로 484, 1층 (구의동)", "치료의빛선약국", "확인불가"));
        drugBox.add(new DrugBox(144, "서울특별시 광진구 천호대로 671 (구의동, 파크타운)", "다솜약국", "확인불가"));
        drugBox.add(new DrugBox(145, "서울특별시 광진구 면목로 130 (중곡동)", "광제사약국", "확인불가"));
        drugBox.add(new DrugBox(146, "서울특별시 광진구 자양로 43 (자양동)", "착한약국", "확인불가"));
        drugBox.add(new DrugBox(147, "서울특별시 광진구 능동로 18 (자양동, 이튼타워리버3차)", "이층약국", "확인불가"));
        drugBox.add(new DrugBox(148, "서울특별시 광진구 능동로 409 (중곡동)", "천사약국", "확인불가"));
        drugBox.add(new DrugBox(149, "서울특별시 광진구 광나루로 508 (구의동)", "현약국", "확인불가"));
        drugBox.add(new DrugBox(150, "서울특별시 광진구 뚝섬로 596, 1층 (자양동)", "봄약국", "확인불가"));
        drugBox.add(new DrugBox(151, "서울특별시 광진구 자양로9길 77 (자양동)", "자양하나약국", "확인불가"));
        drugBox.add(new DrugBox(152, "서울특별시 광진구 용마산로 57 (중곡동)", "해동온누리약국", "확인불가"));
        drugBox.add(new DrugBox(153, "서울특별시 광진구 구의강변로 106, 105호 (구의동)", "쉐르빌대학약국", "확인불가"));
        drugBox.add(new DrugBox(154, "서울특별시 광진구 군자로 86-1 (군자동)", "신백구약국", "확인불가"));
        drugBox.add(new DrugBox(155, "서울특별시 광진구 면목로 173 (중곡동)", "혜원약국", "확인불가"));
        drugBox.add(new DrugBox(156, "서울특별시 광진구 아차산로 241 (화양동, 연한빌딩 4층 403호)", "연약국", "확인불가"));
        drugBox.add(new DrugBox(157, "서울특별시 광진구 능동로13길 39 (화양동)", "햇살온누리약국", "확인불가"));
        drugBox.add(new DrugBox(158, "서울특별시 광진구 용마산로 46 (중곡동)", "중곡종로약국", "확인불가"));
        drugBox.add(new DrugBox(159, "서울특별시 광진구 능동로 265 (군자동)", "포도원온누리약국", "확인불가"));
        drugBox.add(new DrugBox(160, "서울특별시 광진구 용마산로 53 (중곡동)", "중앙약국", "확인불가"));
        drugBox.add(new DrugBox(161, "서울특별시 광진구 능동로51길 44 (중곡동)", "중곡열린약국", "확인불가"));
        drugBox.add(new DrugBox(162, "서울특별시 광진구 자양로 115 (자양동)", "종로온누리약국", "확인불가"));
        drugBox.add(new DrugBox(163, "서울특별시 광진구 동일로 86 (자양동)", "삼육오약국", "확인불가"));
        drugBox.add(new DrugBox(164, "서울특별시 광진구 아차산로 478, 4층 (구의동, 그레이스빌딩)", "레몬약국", "확인불가"));
        drugBox.add(new DrugBox(165, "서울특별시 광진구 천호대로 548 (군자동)", "군자세계로약국", "확인불가"));
        drugBox.add(new DrugBox(166, "서울특별시 광진구 아차산로 220 (자양동)", "온누리메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(167, "서울특별시 광진구 자양로 285 (구의동)", "구의온누리약국", "확인불가"));
        drugBox.add(new DrugBox(168, "서울특별시 광진구 천호대로 575 (중곡동)", "세명약국", "확인불가"));
        drugBox.add(new DrugBox(169, "서울특별시 광진구 자양로15길 27, 1층 (자양동)", "행복한약국", "확인불가"));
        drugBox.add(new DrugBox(170, "서울특별시 광진구 자양로26길 8 (구의동)", "정약국", "확인불가"));
        drugBox.add(new DrugBox(171, "서울특별시 광진구 뚝섬로 558 (자양동, 대양빌딩)", "새한빛약국", "확인불가"));
        drugBox.add(new DrugBox(172, "서울특별시 광진구 능동로 110 (화양동)", "정문대학약국", "확인불가"));
        drugBox.add(new DrugBox(173, "서울특별시 광진구 뚝섬로 552 (자양동, 삼희빌딩)", "메디파워약국", "확인불가"));
        drugBox.add(new DrugBox(174, "서울특별시 광진구 능동로 90, 1층 104-2호 (자양동, 더클래식500)", "더클래식약국", "확인불가"));
        drugBox.add(new DrugBox(175, "서울특별시 광진구 뚝섬로24길 11, 1층 (자양동)", "신승보약국", "확인불가"));
        drugBox.add(new DrugBox(176, "서울특별시 광진구 능동로 지하 110 (화양동, 7호선 건대입구역)", "건대지하철약국", "확인불가"));
        drugBox.add(new DrugBox(177, "서울특별시 광진구 구의강변로 99 (구의동, 용천빌딩)", "서울온누리약국", "확인불가"));
        drugBox.add(new DrugBox(178, "서울특별시 광진구 뚝섬로 499 (자양동)", "웰빙서울온누리약국", "확인불가"));
        drugBox.add(new DrugBox(179, "서울특별시 광진구 아차산로 219 (화양동, 삼영빌딩)", "세계로약국", "확인불가"));
        drugBox.add(new DrugBox(180, "서울특별시 광진구 자양로 95 (자양동, 도광빌딩)", "수약국", "확인불가"));
        drugBox.add(new DrugBox(181, "서울특별시 광진구 면목로 113-1, 1층 (중곡동)", "광진프라자약국", "확인불가"));
        drugBox.add(new DrugBox(182, "서울특별시 광진구 용마산로 23 (중곡동)", "참사랑약국", "확인불가"));
        drugBox.add(new DrugBox(183, "서울특별시 광진구 면목로 126 (중곡동)", "미소약국", "확인불가"));
        drugBox.add(new DrugBox(184, "서울특별시 광진구 동일로18길 73 (자양동)", "비전약국", "확인불가"));
        drugBox.add(new DrugBox(185, "서울특별시 광진구 광나루로 355 (군자동)", "메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(186, "서울특별시 광진구 천호대로 574 (능동)", "군자백화점약국", "확인불가"));
        drugBox.add(new DrugBox(187, "서울특별시 광진구 능동로 18, 105호 (자양동, 이튼타워리버3차)", "우리약국", "확인불가"));
        drugBox.add(new DrugBox(188, "서울특별시 광진구 아차산로 546, 104호 (광장동, 삼성아파트상가)", "스마일약국", "확인불가"));
        drugBox.add(new DrugBox(189, "서울특별시 광진구 아차산로 272 (자양동, 스타시티쇼핑몰)", "더?스타시티약국", "확인불가"));
        drugBox.add(new DrugBox(190, "서울특별시 광진구 천호대로 561 (중곡동)", "군자종로약국", "확인불가"));
        drugBox.add(new DrugBox(191, "서울특별시 광진구 자양로 288 (구의동)", "새서울약국", "확인불가"));
        drugBox.add(new DrugBox(192, "서울특별시 광진구 능동로 110 (화양동)", "햇빛약국", "확인불가"));
        drugBox.add(new DrugBox(193, "서울특별시 광진구 긴고랑로 31 (중곡동)", "봉화약국", "확인불가"));
        drugBox.add(new DrugBox(194, "서울특별시 광진구 강변역로4길 10, 강변역지너스타워 307호 (구의동)", "초원온누리약국", "확인불가"));
        drugBox.add(new DrugBox(195, "서울특별시 광진구 광나루로56길 63, 123호,135호,136호 (구의동, 프라임프라자상가)", "강변그랜드약국", "확인불가"));
        drugBox.add(new DrugBox(196, "서울특별시 광진구 용마산로 8 (중곡동)", "선약국", "확인불가"));
        drugBox.add(new DrugBox(197, "서울특별시 광진구 자양로11길 6-8 (자양동)", "사랑약국", "확인불가"));
        drugBox.add(new DrugBox(198, "서울특별시 광진구 아차산로 244, 1층 나-1호 일부 (자양동)", "애플약국", "확인불가"));
        drugBox.add(new DrugBox(199, "서울특별시 광진구 뚝섬로 498-1 (자양동)", "무지개약국", "확인불가"));
        drugBox.add(new DrugBox(200, "서울특별시 광진구 광나루로 614 (구의동, 만택빌딩)", "우리온누리약국", "확인불가"));
        drugBox.add(new DrugBox(201, "서울특별시 광진구 자양로 113, 4층 401-1호 (자양동, 구의 현대 하이엘)", "굿모닝약국", "확인불가"));
        drugBox.add(new DrugBox(202, "서울특별시 광진구 천호대로 527 (중곡동, 거산빌딩)", "푸른온누리약국", "확인불가"));
        drugBox.add(new DrugBox(203, "서울특별시 광진구 능동로 318 (중곡동)", "소망온누리약국", "확인불가"));
        drugBox.add(new DrugBox(204, "서울특별시 광진구 용마산로 58 (중곡동)", "온누리선명약국", "확인불가"));
        drugBox.add(new DrugBox(205, "서울특별시 광진구 용마산로 49 (중곡동)", "21세기약국", "확인불가"));
        drugBox.add(new DrugBox(206, "서울특별시 광진구 군자로 85 (군자동)", "미래약국", "확인불가"));
        drugBox.add(new DrugBox(207, "서울특별시 광진구 자양로 43, 1층 3호 (자양동)", "광진백화점약국", "확인불가"));
        drugBox.add(new DrugBox(208, "서울특별시 광진구 광나루로 606 (구의동)", "일송약국", "확인불가"));
        drugBox.add(new DrugBox(209, "서울특별시 광진구 아차산로 532, 101호 (광장동, 진넥스빌딩)", "명약국", "확인불가"));
        drugBox.add(new DrugBox(210, "서울특별시 광진구 용마산로 45 (중곡동)", "중곡메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(211, "서울특별시 광진구 광나루로 542 (구의동)", "은혜약국", "확인불가"));
        drugBox.add(new DrugBox(212, "서울특별시 광진구 아차산로69길 8, 103, 104호 (광장동)", "광진온누리약국", "확인불가"));
        drugBox.add(new DrugBox(213, "서울특별시 광진구 구의로 23 (구의동)", "예일약국", "확인불가"));
        drugBox.add(new DrugBox(214, "서울특별시 광진구 뚝섬로 511 (자양동)", "신세계약국", "확인불가"));
        drugBox.add(new DrugBox(215, "서울특별시 광진구 천호대로 635 (중곡동)", "세란약국", "확인불가"));
        drugBox.add(new DrugBox(216, "서울특별시 광진구 아차산로 218 (자양동)", "건대역약국", "확인불가"));
        drugBox.add(new DrugBox(217, "서울특별시 광진구 뚝섬로 496 (자양동)", "뉴메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(218, "서울특별시 광진구 능동로3길 5, 111호 (자양동, 한강현대아파트종합상가)", "혜성약국", "확인불가"));
        drugBox.add(new DrugBox(219, "서울특별시 광진구 자양로 112, 111호,112호,113호 (구의동)", "금성약국", "확인불가"));
        drugBox.add(new DrugBox(220, "서울특별시 광진구 자양로 294-1 (구의동)", "한사랑약국", "확인불가"));
        drugBox.add(new DrugBox(221, "서울특별시 광진구 뚝섬로 585 (자양동)", "금와약국", "확인불가"));
        drugBox.add(new DrugBox(222, "서울특별시 광진구 뚝섬로24길 3 (자양동)", "신성모약국", "확인불가"));
        drugBox.add(new DrugBox(223, "서울특별시 광진구 아차산로29길 55 (화양동)", "그랜드약국", "확인불가"));
        drugBox.add(new DrugBox(224, "서울특별시 광진구 자양로15길 9 (자양동)", "진주약국", "확인불가"));
        drugBox.add(new DrugBox(225, "서울특별시 광진구 군자로 73 (군자동)", "화양종로약국", "확인불가"));
        drugBox.add(new DrugBox(226, "서울특별시 광진구 영화사로 30 (중곡동)", "우리들약국", "확인불가"));
        drugBox.add(new DrugBox(227, "서울특별시 광진구 자양로13길 73 (자양동)", "자양열린약국", "확인불가"));
        drugBox.add(new DrugBox(228, "서울특별시 광진구 영화사로 64-1 (구의동)", "보은약국", "확인불가"));
        drugBox.add(new DrugBox(229, "서울특별시 광진구 면목로 168 (중곡동)", "아나파약국", "확인불가"));
        drugBox.add(new DrugBox(230, "서울특별시 광진구 뚝섬로56길 46 (자양동, 근정빌딩)", "온누리건강약국", "확인불가"));
        drugBox.add(new DrugBox(231, "서울특별시 광진구 구의강변로 22 (구의동)", "신대영약국", "확인불가"));
        drugBox.add(new DrugBox(232, "서울특별시 광진구 아차산로57길 42 (구의동)", "진영약국", "확인불가"));
        drugBox.add(new DrugBox(233, "서울특별시 광진구 면목로 174 (중곡동)", "백온누리약국", "확인불가"));
        drugBox.add(new DrugBox(234, "서울특별시 광진구 광나루로 538 (구의동)", "명성약국", "확인불가"));
        drugBox.add(new DrugBox(235, "서울특별시 광진구 강변역로 50 (구의동, 동서울터미널빌딩)", "중부고속약국", "확인불가"));
        drugBox.add(new DrugBox(236, "서울특별시 광진구 긴고랑로 106, 1층 (중곡동)", "종합약국", "확인불가"));
        drugBox.add(new DrugBox(237, "서울특별시 광진구 뚝섬로 480, 1층 (자양동)", "종우약국", "확인불가"));
        drugBox.add(new DrugBox(238, "서울특별시 광진구 면목로 169 (중곡동)", "제중약국", "확인불가"));
        drugBox.add(new DrugBox(239, "서울특별시 광진구 아차산로 290 (자양동)", "스타시티약국", "확인불가"));
        drugBox.add(new DrugBox(240, "서울특별시 광진구 자양로 194 (구의동)", "완미약국", "확인불가"));
        drugBox.add(new DrugBox(241, "서울특별시 광진구 면목로 113 (중곡동)", "백조온누리약국", "확인불가"));
        drugBox.add(new DrugBox(242, "서울특별시 광진구 뚝섬로 655 (자양동)", "인정온누리약국", "확인불가"));
        drugBox.add(new DrugBox(243, "서울특별시 광진구 자양로 42 (자양동)", "신영신약국", "확인불가"));
        drugBox.add(new DrugBox(244, "서울특별시 광진구 긴고랑로 115 (중곡동)", "금성당약국", "확인불가"));
        drugBox.add(new DrugBox(245, "서울특별시 광진구 아차산로 552 (광장동)", "온누리극동약국", "확인불가"));
        drugBox.add(new DrugBox(246, "서울특별시 광진구 광나루로 529 (구의동)", "수도약국", "확인불가"));
        drugBox.add(new DrugBox(247, "서울특별시 광진구 군자로 93 (군자동)", "해정약국", "확인불가"));
        drugBox.add(new DrugBox(248, "서울특별시 광진구 동일로30길 28 (화양동)", "호림약국", "확인불가"));
        drugBox.add(new DrugBox(249, "서울특별시 광진구 광나루로 366 (화양동)", "참조은약국", "확인불가"));
        drugBox.add(new DrugBox(250, "서울특별시 광진구 강변역로 50, 동서울종합터미널 124호 (구의동)", "동서울약국", "확인불가"));
        drugBox.add(new DrugBox(251, "서울특별시 광진구 아차산로29길 12 (화양동)", "연진약국", "확인불가"));
        drugBox.add(new DrugBox(252, "서울특별시 광진구 자양번영로 11 (자양동)", "성동약국", "확인불가"));
        drugBox.add(new DrugBox(253, "서울특별시 광진구 능동로 298 (능동)", "광해약국", "확인불가"));
        drugBox.add(new DrugBox(254, "서울특별시 광진구 면목로 203 (중곡동)", "성진약국", "확인불가"));
        drugBox.add(new DrugBox(255, "서울특별시 광진구 아차산로 626 (광장동)", "용상약국", "확인불가"));
        drugBox.add(new DrugBox(256, "서울특별시 광진구 긴고랑로9길 29 (중곡동)", "조광옵티마약국", "확인불가"));
        drugBox.add(new DrugBox(257, "서울특별시 광진구 군자로 144 (군자동)", "진명약국", "확인불가"));
        drugBox.add(new DrugBox(258, "서울특별시 광진구 군자로 25 (화양동)", "기호약국", "확인불가"));
        drugBox.add(new DrugBox(259, "서울특별시 광진구 자양로 201 (구의동)", "용마약국", "확인불가"));
        drugBox.add(new DrugBox(260, "서울특별시 광진구 동일로60길 14 (군자동)", "진약국", "확인불가"));
        drugBox.add(new DrugBox(261, "서울특별시 광진구 자양로 311-1 (구의동)", "성안약국", "확인불가"));
        drugBox.add(new DrugBox(511, "서울특별시 송파구 가락로42길 9, 경인약국 (방이동)", "경인약국", "확인불가"));
        drugBox.add(new DrugBox(512, "서울특별시 송파구 마천로41길 21 (마천동)", "고바우약국", "확인불가"));
        drugBox.add(new DrugBox(513, "서울특별시 송파구 성내천로 261, 구본빌딩 (마천동)", "구인약국", "확인불가"));
        drugBox.add(new DrugBox(514, "서울특별시 송파구 송파대로 570, 2층 (신천동)", "굿데이약국", "확인불가"));
        drugBox.add(new DrugBox(515, "서울특별시 송파구 오금로 420, 한라산업개발빌딩 1층 (가락동)", "굿모닝약국", "확인불가"));
        drugBox.add(new DrugBox(516, "서울특별시 송파구 마천로 35, 태웅빌딩 1층 (방이동)", "그랜드약국", "확인불가"));
        drugBox.add(new DrugBox(517, "서울특별시 송파구 동남로18길 9 (가락동, 극동아파트상가 104호)", "극동약국", "확인불가"));
        drugBox.add(new DrugBox(518, "서울특별시 송파구 오금로64길 4 (거여동)", "남매약국", "확인불가"));
        drugBox.add(new DrugBox(519, "서울특별시 송파구 올림픽로 435, 309호 (신천동, 파크리오)", "남일온누리약국", "확인불가"));
        drugBox.add(new DrugBox(520, "서울특별시 송파구 백제고분로50길 26, 1층 (방이동)", "녹원약국", "확인불가"));
        drugBox.add(new DrugBox(521, "서울특별시 송파구 송파대로 383 (석촌동, 1층 3호)", "누가신약국", "확인불가"));
        drugBox.add(new DrugBox(522, "서울특별시 송파구 석촌호수로 160, 102호 (삼전동)", "뉴시티약국", "확인불가"));
        drugBox.add(new DrugBox(523, "서울특별시 송파구 오금로31길 3, 우성빌딩 (방이동)", "다나약국", "확인불가"));
        drugBox.add(new DrugBox(524, "서울특별시 송파구 토성로 20, 1층 (풍납동)", "다사랑약국", "확인불가"));
        drugBox.add(new DrugBox(525, "서울특별시 송파구 올림픽로 76, J타워 1층 104-1호 (잠실동)", "다인약국", "확인불가"));
        drugBox.add(new DrugBox(526, "서울특별시 송파구 백제고분로 349, 자재백화점 1층 (석촌동)", "다정약국", "확인불가"));
        drugBox.add(new DrugBox(527, "서울특별시 송파구 가락로 175, 1층 (송파동)", "대동약국", "확인불가"));
        drugBox.add(new DrugBox(528, "서울특별시 송파구 법원로11길 11, 문정현대지식산업센터1-1 A동 101호 (문정동)", "대한약국", "확인불가"));
        drugBox.add(new DrugBox(529, "서울특별시 송파구 오금로 507, 거여빌딩 1층 (거여동)", "도영약국", "확인불가"));
        drugBox.add(new DrugBox(530, "서울특별시 송파구 마천로5길 4 (오금동, 4층)", "동서약국", "확인불가"));
        drugBox.add(new DrugBox(531, "서울특별시 송파구 올림픽로 114, 2층 (잠실동, 서경빌딩)", "동아약국", "확인불가"));
        drugBox.add(new DrugBox(532, "서울특별시 송파구 거마로22길 49 (마천동)", "동의삼성약국", "확인불가"));
        drugBox.add(new DrugBox(533, "서울특별시 송파구 송이로 239, 도은빌딩 1층 4호 (문정동)", "두리약국", "확인불가"));
        drugBox.add(new DrugBox(534, "서울특별시 송파구 올림픽로35길 10, 파크리오상가B동 202-1호 (신천동, 파크리오)", "드림약국", "확인불가"));
        drugBox.add(new DrugBox(535, "서울특별시 송파구 마천로 25, 1층 (방이동)", "라임약국", "확인불가"));
        drugBox.add(new DrugBox(536, "서울특별시 송파구 송파대로 381, 형제빌딩 1층 (석촌동)", "락희온누리약국", "확인불가"));
        drugBox.add(new DrugBox(537, "서울특별시 송파구 석촌호수로 135, 레이크팰리스 A동 122호 (잠실동)", "레이크이화약국", "확인불가"));
        drugBox.add(new DrugBox(538, "서울특별시 송파구 송파대로 345, 1A동 2009호 (가락동, 헬리오시티)", "리오약국", "확인불가"));
        drugBox.add(new DrugBox(539, "서울특별시 송파구 거마로 58 (마천동)", "마천서울약국", "확인불가"));
        drugBox.add(new DrugBox(540, "서울특별시 송파구 거마로 60, 1층 (마천동)", "마천푸른솔약국", "확인불가"));
        drugBox.add(new DrugBox(541, "서울특별시 송파구 마천로51길 2, 수정빌딩 (마천동)", "마천행복한약국", "확인불가"));
        drugBox.add(new DrugBox(542, "서울특별시 송파구 동남로 236, 2층 (가락동)", "맑은샘약국", "확인불가"));
        drugBox.add(new DrugBox(543, "서울특별시 송파구 새말로 125, 어은회관 107호 (문정동)", "맑은온누리약국", "확인불가"));
        drugBox.add(new DrugBox(544, "서울특별시 송파구 송이로 87, 1층 (가락동)", "메디팜대신M약국", "확인불가"));
        drugBox.add(new DrugBox(545, "서울특별시 송파구 올림픽로35길 10, B동 212-1호 (신천동, 파크리오)", "메디팜우주약국", "확인불가"));
        drugBox.add(new DrugBox(546, "서울특별시 송파구 동남로 189, 가락쌍용아파트 101호 (가락동)", "메디팜푸른약국", "확인불가"));
        drugBox.add(new DrugBox(547, "서울특별시 송파구 송파대로 365 (석촌동)", "메디팜하나약국", "확인불가"));
        drugBox.add(new DrugBox(548, "서울특별시 송파구 마천로41길 20, 1층 (마천동)", "명문약국", "확인불가"));
        drugBox.add(new DrugBox(549, "서울특별시 송파구 백제고분로41길 31 (송파동)", "명생약국", "확인불가"));
        drugBox.add(new DrugBox(550, "서울특별시 송파구 백제고분로 258, 삼전원빌딩 (삼전동)", "모범약국", "확인불가"));
        drugBox.add(new DrugBox(551, "서울특별시 송파구 동남로4길 25, 금천빌딩 (문정동)", "문정약국", "확인불가"));
        drugBox.add(new DrugBox(552, "서울특별시 송파구 오금로 516, 삼정빌딩 1층 (거여동)", "미래약국", "확인불가"));
        drugBox.add(new DrugBox(553, "서울특별시 송파구 토성로 65, 2층 202-2호 (풍납동, 태양상가 202-2호)", "미르약국", "확인불가"));
        drugBox.add(new DrugBox(554, "서울특별시 송파구 송파대로 374 (송파동)", "미성송파약국", "확인불가"));
        drugBox.add(new DrugBox(555, "서울특별시 송파구 오금로31길 11 (방이동)", "미소약국", "확인불가"));
        drugBox.add(new DrugBox(556, "서울특별시 송파구 송파대로 345, 1A동 3041호 (가락동, 헬리오시티)", "민약국", "확인불가"));
        drugBox.add(new DrugBox(557, "서울특별시 송파구 중대로 24 (문정동, 훼밀리상가 136호)", "믿음약국", "확인불가"));
        drugBox.add(new DrugBox(558, "서울특별시 송파구 마천로7길 6, 오금대림아파트 117,118호 (오금동)", "박순영온누리약국", "확인불가"));
        drugBox.add(new DrugBox(559, "서울특별시 송파구 강동대로 73 (풍납동)", "밝은중앙약국", "확인불가"));
        drugBox.add(new DrugBox(560, "서울특별시 송파구 가락로 277 (방이동)", "방이메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(561, "서울특별시 송파구 마천로 29 (방이동)", "방이종로약국", "확인불가"));
        drugBox.add(new DrugBox(562, "서울특별시 송파구 마천로 53, 2층 (오금동)", "방이코끼리약국", "확인불가"));
        drugBox.add(new DrugBox(563, "서울특별시 송파구 가락로 244, 동원빌딩 (방이동)", "방이프라자약국", "확인불가"));
        drugBox.add(new DrugBox(564, "서울특별시 송파구 성내천로 212, 삼정빌딩 (마천동)", "보성약국", "확인불가"));
        drugBox.add(new DrugBox(565, "서울특별시 송파구 성내천로34길 24-1 (마천동)", "보영약국", "확인불가"));
        drugBox.add(new DrugBox(566, "서울특별시 송파구 동남로 317, 103호 (오금동)", "봄봄약국", "확인불가"));
        drugBox.add(new DrugBox(567, "서울특별시 송파구 가락로28길 3 (송파동)", "사랑의 약국", "확인불가"));
        drugBox.add(new DrugBox(568, "서울특별시 송파구 올림픽로37길 130, 파크리오 A동 317호 (신천동)", "삼원약국", "확인불가"));
        drugBox.add(new DrugBox(569, "서울특별시 송파구 새말로8길 6, 1층 (문정동, 광덕빌딩)", "새건영약국", "확인불가"));
        drugBox.add(new DrugBox(570, "서울특별시 송파구 송파대로 372 (송파동)", "새독일약국", "확인불가"));
        drugBox.add(new DrugBox(571, "서울특별시 송파구 풍성로25가길 11 (풍납동)", "새동산약국", "확인불가"));
        drugBox.add(new DrugBox(572, "서울특별시 송파구 석촌호수로 130, 신원빌딩 206호 (삼전동)", "새롬약국", "확인불가"));
        drugBox.add(new DrugBox(573, "서울특별시 송파구 풍성로 55 (풍납동)", "새봄약국", "확인불가"));
        drugBox.add(new DrugBox(574, "서울특별시 송파구 마천로 41, 수기빌딩 2층 (방이동)", "새솔약국", "확인불가"));
        drugBox.add(new DrugBox(575, "서울특별시 송파구 삼전로 75 (잠실동)", "새웅약국", "확인불가"));
        drugBox.add(new DrugBox(576, "서울특별시 송파구 오금로 306, 101호 (가락동, 가락스타클래스)", "새중일약국", "확인불가"));
        drugBox.add(new DrugBox(577, "서울특별시 송파구 송파대로 422 (송파동)", "새희망약국", "확인불가"));
        drugBox.add(new DrugBox(578, "서울특별시 송파구 마천로 137 (오금동)", "새힘온누리약국", "확인불가"));
        drugBox.add(new DrugBox(579, "서울특별시 송파구 송이로31길 4, 1층 (문정동)", "서울약국", "확인불가"));
        drugBox.add(new DrugBox(580, "서울특별시 송파구 송파대로 438, 103호 (송파동)", "석촌온누리약국", "확인불가"));
        drugBox.add(new DrugBox(581, "서울특별시 송파구 마천로 190 (오금동, 청전빌딩 104호)", "성심약국", "확인불가"));
        drugBox.add(new DrugBox(582, "서울특별시 송파구 동남로 136 (문정동)", "성조약국", "확인불가"));
        drugBox.add(new DrugBox(583, "서울특별시 송파구 거마로 66 (마천동)", "세계로약국", "확인불가"));
        drugBox.add(new DrugBox(584, "서울특별시 송파구 중대로 80 (문정동, 롯데마트 지1층)", "소나무약국", "확인불가"));
        drugBox.add(new DrugBox(585, "서울특별시 송파구 양산로 12, 세신거여훼미리타운 1층 107호 (거여동)", "소백약국", "확인불가"));
        drugBox.add(new DrugBox(586, "서울특별시 송파구 문정로4길 14, 동산빌딩 1층 (문정동)", "송파그랜드약국", "확인불가"));
        drugBox.add(new DrugBox(587, "서울특별시 송파구 올림픽로35길 10, B동 308-1호 (신천동, 파크리오)", "송파미소약국", "확인불가"));
        drugBox.add(new DrugBox(588, "서울특별시 송파구 송파대로28길 27 (가락동, 성원상떼빌 102동 202호)", "송파새롬약국", "확인불가"));
        drugBox.add(new DrugBox(589, "서울특별시 송파구 오금로32길 28 (송파동)", "송파온누리약국", "확인불가"));
        drugBox.add(new DrugBox(590, "서울특별시 송파구 토성로 65 (풍납동, 태양상가 108호)", "송파우리약국", "확인불가"));
        drugBox.add(new DrugBox(591, "서울특별시 송파구 동남로 217, 동희빌딩 1층 (가락동)", "송파제일약국", "확인불가"));
        drugBox.add(new DrugBox(592, "서울특별시 송파구 오금로 91, 1층 (방이동)", "송파중앙약국", "확인불가"));
        drugBox.add(new DrugBox(593, "서울특별시 송파구 가락로 112, 내경빌딩 (석촌동)", "송파프라자약국", "확인불가"));
        drugBox.add(new DrugBox(594, "서울특별시 송파구 마천로41길 1, 1층 (마천동)", "송파하나약국", "확인불가"));
        drugBox.add(new DrugBox(595, "서울특별시 송파구 가락로 113 (석촌동, 금도빌딩 104호)", "수약국", "확인불가"));
        drugBox.add(new DrugBox(596, "서울특별시 송파구 거마로 61, 1층 (마천동, 강동중앙의원)", "수프라자약국", "확인불가"));
        drugBox.add(new DrugBox(597, "서울특별시 송파구 석촌호수로 68 (잠실동, A동 1층, 지층)", "숲약국", "확인불가"));
        drugBox.add(new DrugBox(598, "서울특별시 송파구 오금로 508, 스타빌딩 1층 (거여동)", "스마트약국", "확인불가"));
        drugBox.add(new DrugBox(599, "서울특별시 송파구 송파대로 562 (신천동, 한빛프라자 3층)", "스타약국", "확인불가"));
        drugBox.add(new DrugBox(600, "서울특별시 송파구 올림픽로 116, 1층 (잠실동)", "시티약국", "확인불가"));
        drugBox.add(new DrugBox(601, "서울특별시 송파구 강동대로7길 4, 1층 (풍납동)", "신아산약국", "확인불가"));
        drugBox.add(new DrugBox(602, "서울특별시 송파구 송파대로 345, 1A동 2036호 (가락동, 헬리오시티)", "싱싱헬리오약국", "확인불가"));
        drugBox.add(new DrugBox(603, "서울특별시 송파구 올림픽로4길 48 (잠실동)", "쌍용약국", "확인불가"));
        drugBox.add(new DrugBox(604, "서울특별시 송파구 토성로 26, 강진빌딩 104,105호 (풍납동)", "아산약국", "확인불가"));
        drugBox.add(new DrugBox(605, "서울특별시 송파구 올림픽로 493, 풍납동 N빌딩 1층 (풍납동)", "아산큰길약국", "확인불가"));
        drugBox.add(new DrugBox(606, "서울특별시 송파구 위례광장로 230, B동 207호 (장지동, 위례2차아이파크)", "아이파크약국", "확인불가"));
        drugBox.add(new DrugBox(607, "서울특별시 송파구 올림픽로 119, 3B5호 (잠실동, 잠실파인애플상가)", "엘스3층 온누리약국", "확인불가"));
        drugBox.add(new DrugBox(608, "서울특별시 송파구 가락로 166, 1층 (송파동)", "열린약국", "확인불가"));
        drugBox.add(new DrugBox(609, "서울특별시 송파구 백제고분로 202 (삼전동)", "영보약국", "확인불가"));
        drugBox.add(new DrugBox(610, "서울특별시 송파구 한가람로 448 (풍납동, 한가람상가 102호)", "영진약국", "확인불가"));
        drugBox.add(new DrugBox(611, "서울특별시 송파구 백제고분로 446, 송암빌딩 2층 201호 (방이동)", "예사랑약국", "확인불가"));
        drugBox.add(new DrugBox(612, "서울특별시 송파구 위례성대로20길 33, 삼광빌딩 (오금동)", "오금중앙약국", "확인불가"));
        drugBox.add(new DrugBox(613, "서울특별시 송파구 송파대로 380, 행우빌딩 (송파동)", "오렌지약국", "확인불가"));
        drugBox.add(new DrugBox(614, "서울특별시 송파구 오금로11길 37, 101호 (방이동)", "오렌지온누리약국", "확인불가"));
        drugBox.add(new DrugBox(615, "서울특별시 송파구 백제고분로 386, 1층 (송파동)", "오약국", "확인불가"));
        drugBox.add(new DrugBox(616, "서울특별시 송파구 동남로 189, 503호 (가락동)", "오층조은약국", "확인불가"));
        drugBox.add(new DrugBox(617, "서울특별시 송파구 백제고분로12길 17 (잠실동)", "온누리건강약국", "확인불가"));
        drugBox.add(new DrugBox(618, "서울특별시 송파구 마천로 345, 1층 (마천동)", "온누리대웅약국", "확인불가"));
        drugBox.add(new DrugBox(619, "서울특별시 송파구 가락로 26 (석촌동)", "온누리배명약국", "확인불가"));
        drugBox.add(new DrugBox(620, "서울특별시 송파구 양산로10길 4 (거여동)", "온누리봄빛약국", "확인불가"));
        drugBox.add(new DrugBox(621, "서울특별시 송파구 송파대로 415, 1층 (석촌동)", "온누리엄마손약국", "확인불가"));
        drugBox.add(new DrugBox(622, "서울특별시 송파구 동남로 141, 옥산빌딩 (가락동)", "온누리옥산약국", "확인불가"));
        drugBox.add(new DrugBox(623, "서울특별시 송파구 강동대로7길 3, 1층 (풍납동)", "올리브약국", "확인불가"));
        drugBox.add(new DrugBox(624, "서울특별시 송파구 마천로41길 9 (마천동)", "옵티마100세약국", "확인불가"));
        drugBox.add(new DrugBox(625, "서울특별시 송파구 백제고분로 261 (삼전동)", "왕약국", "확인불가"));
        drugBox.add(new DrugBox(626, "서울특별시 송파구 백제고분로 466, 세나빌딩 1층 (방이동)", "우리들약국", "확인불가"));
        drugBox.add(new DrugBox(627, "서울특별시 송파구 오금로 404, 원일빌딩 (가락동)", "우리약국", "확인불가"));
        drugBox.add(new DrugBox(628, "서울특별시 송파구 오금로 477 (거여동)", "우림약국", "확인불가"));
        drugBox.add(new DrugBox(629, "서울특별시 송파구 올림픽로35길 112, 장미아파트 비상가 101,102호 (신천동)", "월드중앙약국", "확인불가"));
        drugBox.add(new DrugBox(630, "서울특별시 송파구 올림픽로 300, 지하1층 (신천동, 롯데마트월드타워점)", "월드타워약국", "확인불가"));
        drugBox.add(new DrugBox(631, "서울특별시 송파구 백제고분로 216, 청송빌딩 (삼전동)", "웰빙팜건강프라자약국", "확인불가"));
        drugBox.add(new DrugBox(632, "서울특별시 송파구 양재대로 1222, 3층 219-2호 (방이동, 올림픽프라자상가)", "웰약국", "확인불가"));
        drugBox.add(new DrugBox(633, "서울특별시 송파구 올림픽로37길 130, 파크리오 124,125,126호 (신천동)", "위드팜가까운지하철약국", "확인불가"));
        drugBox.add(new DrugBox(634, "서울특별시 송파구 송파대로 624 (신천동, 잠실나루역구내 제215-08호)", "위드팜잠실나루역약국", "확인불가"));
        drugBox.add(new DrugBox(635, "서울특별시 송파구 올림픽로 509 (풍납동, J빌딩 1층2호)", "은세약국", "확인불가"));
        drugBox.add(new DrugBox(636, "서울특별시 송파구 가락로 158, 1층 102호 (송파동)", "이화약국", "확인불가"));
        drugBox.add(new DrugBox(637, "서울특별시 송파구 가락로11길 8 (석촌동)", "일선약국", "확인불가"));
        drugBox.add(new DrugBox(638, "서울특별시 송파구 백제고분로 171 (잠실동)", "잠실프라자약국", "확인불가"));
        drugBox.add(new DrugBox(639, "서울특별시 송파구 올림픽로35길 112, 제2동 3층 20호 (신천동, 장미아파트 비상가)", "장미미소약국", "확인불가"));
        drugBox.add(new DrugBox(640, "서울특별시 송파구 마천로 237, 신동아아파트 상가동 104호 (마천동)", "장생당약국", "확인불가"));
        drugBox.add(new DrugBox(641, "서울특별시 송파구 오금로 487, 1층 (거여동)", "정다운약국", "확인불가"));
        drugBox.add(new DrugBox(642, "서울특별시 송파구 마천로 68, 1층 3호 (오금동)", "정민약국", "확인불가"));
        drugBox.add(new DrugBox(643, "서울특별시 송파구 백제고분로 127, 다산해명빌딩 (잠실동)", "정선약국", "확인불가"));
        drugBox.add(new DrugBox(644, "서울특별시 송파구 백제고분로 210 (삼전동)", "정온누리약국", "확인불가"));
        drugBox.add(new DrugBox(645, "서울특별시 송파구 양재대로 1222, 올림픽프라자 1층 67호 (방이동)", "종우약국", "확인불가"));
        drugBox.add(new DrugBox(646, "서울특별시 송파구 백제고분로 490, 거보산업(주) 1층 (방이동)", "주온누리약국", "확인불가"));
        drugBox.add(new DrugBox(647, "서울특별시 송파구 오금로38길 5-25 (가락동, 현대상가 101호)", "중앙당약국", "확인불가"));
        drugBox.add(new DrugBox(648, "서울특별시 송파구 마천로57길 4, 1층 (마천동)", "중앙프라자약국", "확인불가"));
        drugBox.add(new DrugBox(649, "서울특별시 송파구 새말로 134 (문정동)", "중의당약국", "확인불가"));
        drugBox.add(new DrugBox(650, "서울특별시 송파구 백제고분로48길 27, 성우빌딩 (방이동)", "지용약국", "확인불가"));
        drugBox.add(new DrugBox(651, "서울특별시 송파구 석촌호수로 58 (잠실동, 동신빌딩 1층)", "지혜온누리약국", "확인불가"));
        drugBox.add(new DrugBox(652, "서울특별시 송파구 마천로57길 7, 1층 (마천동)", "진메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(653, "서울특별시 송파구 동남로 138, 1층 (문정동)", "진약국", "확인불가"));
        drugBox.add(new DrugBox(654, "서울특별시 송파구 양재대로 1222, 올림픽프라자 3층 15호 (방이동)", "참소망약국", "확인불가"));
        drugBox.add(new DrugBox(655, "서울특별시 송파구 오금로 95, 1층 (방이동)", "천명약국", "확인불가"));
        drugBox.add(new DrugBox(656, "서울특별시 송파구 새말로8길 27, 봉의빌딩 1층 (문정동)", "천사약국", "확인불가"));
        drugBox.add(new DrugBox(657, "서울특별시 송파구 바람드리길 62 (풍납동)", "천호푸른약국", "확인불가"));
        drugBox.add(new DrugBox(658, "서울특별시 송파구 올림픽로37길 130, 파크리오 A상가동 307호 (신천동)", "초록약국", "확인불가"));
        drugBox.add(new DrugBox(659, "서울특별시 송파구 동남로 211 (가락동)", "큰사랑약국", "확인불가"));
        drugBox.add(new DrugBox(660, "서울특별시 송파구 올림픽로 240 (잠실동, 웰빌센타동 A-104호)", "키즈플러스약국", "확인불가"));
        drugBox.add(new DrugBox(661, "서울특별시 송파구 송파대로 345, 1A동 3011호 (가락동, 헬리오시티)", "트리풀약국", "확인불가"));
        drugBox.add(new DrugBox(662, "서울특별시 송파구 송파대로28길 43, 1층 101-2호 (가락동, 송파KCC웰츠타워 101-2호)", "팜플러스약국", "확인불가"));
        drugBox.add(new DrugBox(663, "서울특별시 송파구 백제고분로 279, 1층 (석촌동)", "푸른약국", "확인불가"));
        drugBox.add(new DrugBox(664, "서울특별시 송파구 바람드리길 33 (풍납동)", "풍납라파약국", "확인불가"));
        drugBox.add(new DrugBox(665, "서울특별시 송파구 양재대로 1222 (방이동)", "프라자약국", "확인불가"));
        drugBox.add(new DrugBox(666, "서울특별시 송파구 올림픽로4길 42, 109호 (잠실동, 프리마상가 109호)", "프리마약국", "확인불가"));
        drugBox.add(new DrugBox(667, "서울특별시 송파구 마천로 264, 흥일빌딩 1층 (거여동)", "하나로약국", "확인불가"));
        drugBox.add(new DrugBox(668, "서울특별시 송파구 오금로 504, 1층 (거여동)", "하나메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(669, "서울특별시 송파구 송파대로 111, 205동 305호 (문정동, 파크하비오)", "하나약국", "확인불가"));
        drugBox.add(new DrugBox(670, "서울특별시 송파구 백제고분로 276, 202호 (석촌동)", "하늘약국", "확인불가"));
        drugBox.add(new DrugBox(671, "서울특별시 송파구 백제고분로45길 5 (송파동)", "한솔약국", "확인불가"));
        drugBox.add(new DrugBox(672, "서울특별시 송파구 가락로 111 (석촌동)", "해성약국", "확인불가"));
        drugBox.add(new DrugBox(673, "서울특별시 송파구 올림픽로35길 124, 4층 7호 (신천동, 장미아파트 에이상가)", "해오름약국", "확인불가"));
        drugBox.add(new DrugBox(674, "서울특별시 송파구 오금로 512, 207호 (거여동, 거여역2차쌍용아파트 207호)", "해피홈약국", "확인불가"));
        drugBox.add(new DrugBox(675, "서울특별시 송파구 위례성대로20길 31, 1층 (오금동)", "햇살약국", "확인불가"));
        drugBox.add(new DrugBox(676, "서울특별시 송파구 양산로 42 (거여동)", "행복약국", "확인불가"));
        drugBox.add(new DrugBox(677, "서울특별시 송파구 풍성로25길 41, 백원빌딩 (풍납동)", "행복한우리약국", "확인불가"));
        drugBox.add(new DrugBox(678, "서울특별시 송파구 송파대로 345, 1A동 2023호 (가락동, 헬리오시티)", "헬리오메디칼약국", "확인불가"));
        drugBox.add(new DrugBox(679, "서울특별시 송파구 중대로 80 (문정동, 롯데마트 3층)", "화인약국", "확인불가"));
        drugBox.add(new DrugBox(680, "서울특별시 송파구 백제고분로 381, 우성빌딩 (송파동)", "황제약국", "확인불가"));
        drugBox.add(new DrugBox(681, "서울특별시 송파구 마천로45길 15 (마천동)", "후생약국", "확인불가"));
        drugBox.add(new DrugBox(682, "서울특별시 송파구 중대로 68 (문정동, 훼미리샤르망 106호)", "훼미리약국", "확인불가"));

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

    public boolean isNetworkConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        NetworkInfo lte = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
//
//        if(mobile != null || lte != null){
//            if(mobile.isConnected() || wifi.isConnected() || lte.isConnected())
//                return true;
//        }else{
//            if(wifi.isConnected())
//        }

        Network currentNetwork = connectivityManager.getActiveNetwork();

        if(currentNetwork != null)
            return true;
        else
            return false;
    }

    public void showDialogForNetwork(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("네트워크 필요");
        builder.setMessage("수거함 정보를 불러오기 위해서는 네트워크가 필요합니다 필요합니다.\n네트워크에 연결해주세요");
        builder.setCancelable(true);
        builder.setNegativeButton("메인으로 돌아가기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                getActivity().finish();
            }
        });
        builder.create().show();
    }
}
