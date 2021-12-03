package com.example.waste_drug.shipping;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waste_drug.R;
import com.example.waste_drug.db.MyDrugDatabase;
import com.example.waste_drug.db.MyDrugInfo;

import java.util.LinkedList;
import java.util.List;

public class ShippingActivity extends AppCompatActivity {

    private List<MyDrugInfo> myDrugInfoArrayList = new LinkedList<>();
    private List<MyDrugInfo> getDBArrayList = new LinkedList<>();
    private MyDrugDatabase db;
    private RecyclerView subscribeRecyclerView;
    private ShippingAdapter shippingAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);
        setToolbar();
        getSubscribeDrugs();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("약 구독 서비스");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void getSubscribeDrugs() {
        getDB();
    }

    private void getDB() {
        db = MyDrugDatabase.getInstance(getApplicationContext());

        class GetRunnable implements Runnable {
            @Override
            public void run() {
                try {
                    getDBArrayList = db.myDrugInfoDao().getAll();
                    setRecyclerView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        GetRunnable getRunnable = new GetRunnable();
        Thread thread = new Thread(getRunnable);
        thread.start();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setRecyclerView() {
        for (MyDrugInfo subscribe : getDBArrayList) {
            if (subscribe.subscribe == 1) {
                myDrugInfoArrayList.add(subscribe);
            }
        }

        getViews();
        subscribeRecyclerView = findViewById(R.id.rv_shipping);
        shippingAdapter = new ShippingAdapter(myDrugInfoArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);

        subscribeRecyclerView.setLayoutManager(layoutManager);
        subscribeRecyclerView.setAdapter(shippingAdapter);
    }

    private void getViews() {
        TextView count = findViewById(R.id.tv_manage);
        count.setText("구독 중인 의약품 : " + myDrugInfoArrayList.size() + "개");
    }
}
