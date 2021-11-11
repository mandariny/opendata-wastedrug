package com.example.waste_drug;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waste_drug.data.Pharmacy;

import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import java.util.ArrayList;

public class KakaoActivity extends AppCompatActivity implements MapView.POIItemEventListener {

    LinearLayout info_view;
    TextView name;
    TextView add;
    TextView phone;
    TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao);

        Intent intent = getIntent();
        ArrayList<Pharmacy> pharmacyArrayList = (ArrayList<Pharmacy>)intent.getSerializableExtra(("pharmacies"));
        int pos = intent.getIntExtra("position",0);
        Log.v("tag", "Kakaomap~"+pharmacyArrayList.size()+"/"+pos);

        MapView mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);

        //포인트 좌표의 위도, 경도 설정
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.5514579595, 126.951949155);
        mapView.setMapCenterPoint(MARKER_POINT, true);
        mapView.setPOIItemEventListener(this);

        mapViewContainer.addView(mapView);

        //마커 설정
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Default Marker");
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

        info_view = findViewById(R.id.info);
        info_view.setVisibility(View.VISIBLE);

        name = findViewById(R.id.info_name);
        add = findViewById(R.id.info_address);
        phone = findViewById(R.id.info_phone);
        time = findViewById(R.id.info_time);

        name.setText("NAME");
        add.setText("ADDRESS");
        phone.setText("PHONE");
        time.setText("TIME");
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