package com.example.waste_drug.search.drugbox;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.location.Geocoder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private GpsTracker gpsTracker;
    private Geocoder geocoder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drugbox, container, false);
        getInitView(v);
        getInitDB();
        makeDB();
        saveDB();
        getDB(drugBox);
        searchViewClicked();
        searchViewClosed();

        gpsTracker = new GpsTracker(container.getContext());
        geocoder = new Geocoder(container.getContext(), Locale.getDefault());
        Button show_loc = (Button) v.findViewById(R.id.button3);
        show_loc.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v){
        switch(v.getId())
        {
            case R.id.button3:
            {
                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                //Log.v("tag","lat: "+latitude+" & lon: "+longitude);

                List<Address> address;
                Address add;

                try {
                    address = geocoder.getFromLocation(latitude, longitude, 1);
                    add = address.get(0);
                    //Log.v("tag", "add: "+add.getSubLocality().toString());
                    //Log.v("tag", "add: "+add.getThoroughfare().toString());

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
        }
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
                getDB(drugBox);
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
        //drugBox.add(new DrugBox(105, "서울특별시 광진구 긴고랑로 47, 1층 (면목동)", "신성모약국", "확인불가"));
    }
}
