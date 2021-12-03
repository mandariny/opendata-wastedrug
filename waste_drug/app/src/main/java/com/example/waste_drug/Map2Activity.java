package com.example.waste_drug;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private LinearLayout info_view;
    private TextView name;
    private TextView address;
    private TextView phone;
    private TextView time;
    private ArrayList<String> arrs= new ArrayList<>();
    private ArrayList<String> titles= new ArrayList<>();
    private ArrayList<String> pnums= new ArrayList<>();
    private ArrayList<String> times = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);

        name = findViewById(R.id.info_name2);
        address = findViewById(R.id.info_address2);
        phone = findViewById(R.id.info_phone2);
        time = findViewById(R.id.info_time2);

        Intent intent = getIntent();
        ArrayList<Pharmacy> pharmacyArrayList = (ArrayList<Pharmacy>)intent.getSerializableExtra(("pharmacies"));
        int pos = intent.getIntExtra("position",0);

        for(int i=0; i<pharmacyArrayList.size();i++){
            String days = "";
            arrs.add(pharmacyArrayList.get(i).getDutyAddr());
            titles.add(pharmacyArrayList.get(i).getDutyName());
            pnums.add(pharmacyArrayList.get(i).getDutyTel1());

            if(Integer.parseInt(pharmacyArrayList.get(i).getDutyTime1c())>1800 ||
                    Integer.parseInt(pharmacyArrayList.get(i).getDutyTime2c())>1800 ||
                    Integer.parseInt(pharmacyArrayList.get(i).getDutyTime3c())>1800 ||
                    Integer.parseInt(pharmacyArrayList.get(i).getDutyTime4c())>1800 ||
                    Integer.parseInt(pharmacyArrayList.get(i).getDutyTime5c())>1800 )
                days += "평일 야간 ";
            if(pharmacyArrayList.get(i).getDutyTime6c() != null ){
                if(!days.equals(""))
                    days += "/ ";
                days += "토요일 ";
            }
            if(pharmacyArrayList.get(i).getDutyTime7c() != null ){
                if(!days.equals(""))
                    days += "/ ";
                days += "일요일 ";
            }
            if(pharmacyArrayList.get(i).getDutyTime8c() != null ){
                if(!days.equals(""))
                    days += "/ ";
                days += "공휴일 ";
            }
            if(days.equals(""))
                days += "운영 정보 없음";

            times.add(days);
        }

        ArrayList<Double> lat = new ArrayList<>();
        ArrayList<Double> lon = new ArrayList<>();


        for(int i=0; i<pharmacyArrayList.size();i++){
            lat.add(Double.parseDouble(pharmacyArrayList.get(i).getWgs84Lat()));
            lon.add(Double.parseDouble(pharmacyArrayList.get(i).getWgs84Lon()));
        }

        MapView mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);

        MapPOIItem [] markers = new MapPOIItem[pharmacyArrayList.size()];
        for(int i=0; i<pharmacyArrayList.size();i++){
            markers[i] = new MapPOIItem();
            markers[i].setItemName(titles.get(i));
            markers[i].setTag(i);
            markers[i].setMapPoint(MapPoint.mapPointWithGeoCoord(lat.get(i),lon.get(i)));
            markers[i].setMarkerType(MapPOIItem.MarkerType.BluePin);
            markers[i].setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            markers[i].setCustomSelectedImageResourceId(R.drawable.marker);
            markers[i].setCustomImageAutoscale(false);
            //markers[i].setCustomImageAnchor(0.5f, 0.0f);
            markers[i].setCustomImageAnchorPointOffset(new MapPOIItem.ImageOffset(120,0));
        }

        name.setText(titles.get(pos));
        address.setText(arrs.get(pos));
        phone.setText(pnums.get(pos));
        time.setText(times.get(pos));

        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat.get(pos),lon.get(pos)), true);
        mapView.setPOIItemEventListener(this);
        mapView.setZoomLevel(2,false);
        mapView.addPOIItems(markers);
        mapView.selectPOIItem(markers[pos],false);
        mapViewContainer.addView(mapView);
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        name.setText(titles.get(mapPOIItem.getTag()));
        address.setText(arrs.get(mapPOIItem.getTag()));
        phone.setText(pnums.get(mapPOIItem.getTag()));
        time.setText(times.get(mapPOIItem.getTag()));
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