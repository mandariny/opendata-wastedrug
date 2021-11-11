package com.example.waste_drug;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waste_drug.data.Pharmacy;

import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Map2Activity extends AppCompatActivity implements MapView.POIItemEventListener {

    LinearLayout info_view;
    TextView name;
    TextView address;
    TextView phone;
    TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);

        name = findViewById(R.id.info_name2);
        address = findViewById(R.id.info_address2);
        phone = findViewById(R.id.info_phone2);
        time = findViewById(R.id.info_time2);

        MapView mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);

        Intent intent = getIntent();
        ArrayList<Pharmacy> pharmacyArrayList = (ArrayList<Pharmacy>)intent.getSerializableExtra(("pharmacies"));
        int pos = intent.getIntExtra("position",0);
        String title = pharmacyArrayList.get(pos).getDutyName();
        String arr = pharmacyArrayList.get(pos).getDutyAddr();
        String pnum = pharmacyArrayList.get(pos).getDutyTel1();
        String days = "";
        double lat = 0;
        double lon = 0;

        if(Integer.parseInt(pharmacyArrayList.get(pos).getDutyTime1c())>1800 ||
                Integer.parseInt(pharmacyArrayList.get(pos).getDutyTime2c())>1800 ||
                Integer.parseInt(pharmacyArrayList.get(pos).getDutyTime3c())>1800 ||
                Integer.parseInt(pharmacyArrayList.get(pos).getDutyTime4c())>1800 ||
                Integer.parseInt(pharmacyArrayList.get(pos).getDutyTime5c())>1800 )
            days += "평일 야간 ";
        if(pharmacyArrayList.get(pos).getDutyTime6c() != null ){
            if(!days.equals(""))
                days += "/ ";
            days += "토요일 ";
        }
        if(pharmacyArrayList.get(pos).getDutyTime7c() != null ){
            if(!days.equals(""))
                days += "/ ";
            days += "일요일 ";
        }
        if(pharmacyArrayList.get(pos).getDutyTime8c() != null ){
            if(!days.equals(""))
                days += "/ ";
            days += "공휴일 ";
        }
        if(days.equals(""))
            days += "운영 정보 없음";

        name.setText(title);
        address.setText(arr);
        phone.setText(pnum);
        time.setText(days);

        Geocoder geocoder = new Geocoder(getApplicationContext());
        try{
            List<Address> resultLocation = geocoder.getFromLocationName(arr,1);
            lat = resultLocation.get(0).getLatitude();
            lon = resultLocation.get(0).getLongitude();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //포인트 좌표의 위도, 경도 설정
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(lat, lon);
        mapView.setMapCenterPoint(MARKER_POINT, true);
        mapView.setPOIItemEventListener(this);

        mapViewContainer.addView(mapView);

        //마커 설정
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(title);
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);

        // 기본으로 제공하는 BluePin 마커 모양.
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        mapView.addPOIItem(marker);
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        //마커 클릭시 정보 노출
        info_view = findViewById(R.id.info2);
        info_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}