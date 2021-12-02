package com.example.waste_drug;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waste_drug.db.DrugBox;

import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Map1Activity extends AppCompatActivity implements MapView.POIItemEventListener {

    private LinearLayout info_view;
    private TextView name;
    private TextView address;
    private TextView phone;
    private ArrayList<String> arrs= new ArrayList<>();
    private ArrayList<String> titles= new ArrayList<>();
    private ArrayList<String> pnums= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map1);

        name = findViewById(R.id.info_name);
        address = findViewById(R.id.info_address);
        phone = findViewById(R.id.info_phone);

        Intent intent = getIntent();
        ArrayList<DrugBox> drugArrayList = (ArrayList<DrugBox>)intent.getSerializableExtra(("drugboxes"));
        int pos = intent.getIntExtra("position",0);

        for(int i=0; i<drugArrayList.size();i++){
            arrs.add(drugArrayList.get(i).getAddress());
            titles.add(drugArrayList.get(i).getName());
            pnums.add(drugArrayList.get(i).getTel());
        }

        ArrayList<Double> lat = new ArrayList<>();
        ArrayList<Double> lon = new ArrayList<>();

        Geocoder geocoder = new Geocoder(getApplicationContext());
        try{
            for(int i=0; i<drugArrayList.size();i++){
                List<Address> resultLocation = geocoder.getFromLocationName(arrs.get(i),1);
                lat.add(resultLocation.get(0).getLatitude());
                lon.add(resultLocation.get(0).getLongitude());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        MapView mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);

        MapPOIItem [] markers = new MapPOIItem[drugArrayList.size()];
        for(int i=0; i<drugArrayList.size();i++){
            markers[i] = new MapPOIItem();
            markers[i].setItemName(titles.get(i));
            markers[i].setTag(i);
            markers[i].setMapPoint(MapPoint.mapPointWithGeoCoord(lat.get(i),lon.get(i)));
            markers[i].setMarkerType(MapPOIItem.MarkerType.BluePin);
            markers[i].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        }

        name.setText(titles.get(pos));
        address.setText(arrs.get(pos));
        phone.setText(pnums.get(pos));

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