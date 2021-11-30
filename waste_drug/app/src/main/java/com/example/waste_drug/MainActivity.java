package com.example.waste_drug;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waste_drug.manage.ManageActivity;
import com.example.waste_drug.recycle.RecycleActivity;
import com.example.waste_drug.search.SearchActivity;

public class MainActivity extends AppCompatActivity {

    /*private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }*/

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clickEvent();
    }

    private void clickEvent() {
        clickCollectionBoxLayout();
        clickPharmacyLayout();
        clickRecycleLayout();
        clickManageMyDrugLayout();
        clickShippingServiceLayout();
        clickMyBenefitLayout();
    }

    private void clickCollectionBoxLayout() {
        RelativeLayout searchCollectionBoxLayout = findViewById(R.id.rl_search_collection_box);
        searchCollectionBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                intent.putExtra("index", 0);
                startActivity(intent);
            }
        });
    }

    private void clickPharmacyLayout() {
        RelativeLayout searchPharmacyLayout = findViewById(R.id.rl_search_pharmacy);
        searchPharmacyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                intent.putExtra("index", 1);
                startActivity(intent);
            }
        });
    }

    private void clickRecycleLayout() {
        RelativeLayout recycleLayout = findViewById(R.id.rl_recycler);
        recycleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RecycleActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clickManageMyDrugLayout() {
        RelativeLayout manageDrugLayout = findViewById(R.id.rl_drug_manage);
        manageDrugLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ManageActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clickShippingServiceLayout() {
        RelativeLayout shippingServiceLayout = findViewById(R.id.rl_shipping_service);
        shippingServiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "shipping click!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clickMyBenefitLayout() {
        RelativeLayout benefitLayout = findViewById(R.id.rl_benefit);
        benefitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "benefit click!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}