package com.example.waste_drug;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.waste_drug.data.SlideItem;
import com.example.waste_drug.manage.ManageActivity;
import com.example.waste_drug.recycle.RecycleActivity;
import com.example.waste_drug.search.SearchActivity;
import com.example.waste_drug.shipping.ShippingActivity;
import com.example.waste_drug.slide.SliderAdapter;

import java.util.ArrayList;
import java.util.List;

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

    private List<SlideItem> slideItemList = new ArrayList<>();
    private ViewPager2 viewPager;
    private Handler sliderHandler = new Handler();
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();
        setSliders();
        clickEvent();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("약:속");
        toolbar.setTitleTextColor(Color.WHITE);
    }

    private void setSliders() {
        viewPager = findViewById(R.id.vp_main);

        slideItemList.add(new SlideItem(R.drawable.feed1));
        slideItemList.add(new SlideItem(R.drawable.feed2));
        slideItemList.add(new SlideItem(R.drawable.feed3));

        setTransform();
        setAutoMove();
    }

    private void setTransform() {
        viewPager.setAdapter(new SliderAdapter(slideItemList, viewPager));
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(3);
        viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        viewPager.setPageTransformer(compositePageTransformer);
    }

    private void setAutoMove() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    };

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
                if (!isNetworkConnected()) {
                    showDialogForNetwork();
                } else {
                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                    intent.putExtra("index", 0);
                    startActivity(intent);
                }
            }
        });
    }

    private void clickPharmacyLayout() {
        RelativeLayout searchPharmacyLayout = findViewById(R.id.rl_search_pharmacy);
        searchPharmacyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkConnected()) {
                    showDialogForNetwork();
                } else {
                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                    intent.putExtra("index", 1);
                    startActivity(intent);
                }
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
                Intent intent = new Intent(getApplicationContext(), ShippingActivity.class);
                startActivity(intent);
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

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network currentNetwork = connectivityManager.getActiveNetwork();

        if (currentNetwork != null)
            return true;
        else
            return false;
    }

    public void showDialogForNetwork() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("네트워크 필요");
        builder.setMessage("\n수거함 정보를 불러오기 위해서는 네트워크가 필요합니다 필요합니다.\n네트워크에 연결해주세요");
        builder.setCancelable(true);
        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}