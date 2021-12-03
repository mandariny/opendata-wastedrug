package com.example.waste_drug.manage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.waste_drug.R;
import com.example.waste_drug.db.MyDrugDatabase;
import com.example.waste_drug.db.MyDrugInfo;

import java.util.LinkedList;
import java.util.List;

public class ManageActivity extends AppCompatActivity {
    private List<MyDrugInfo> myDrugInfoArrayList = new LinkedList<>();
    private RecyclerView myDrugRecyclerView;
    private MyDrugDatabase db;
    private MyDrugAdapter myDrugAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        setToolbar();
        getMyDrugs();
        clickEvent();
        setRecyclerView();
        refresh();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("개인 의약품 관리");
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

    private void getMyDrugs() {
        getDB();
    }

    private void getDB() {
        db = MyDrugDatabase.getInstance(getApplicationContext());

        class GetRunnable implements Runnable {
            @Override
            public void run() {
                try {
                    myDrugInfoArrayList = db.myDrugInfoDao().getAll();
                    setView();
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

    private void setView() {
        TextView count = findViewById(R.id.tv_manage);
        count.setText("복용 중인 의약품 : " + myDrugInfoArrayList.size() + "개");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setRecyclerView() {
        myDrugRecyclerView = findViewById(R.id.rv_manage);
        myDrugAdapter = new MyDrugAdapter(myDrugInfoArrayList, getApplicationContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);

        myDrugRecyclerView.setLayoutManager(layoutManager);
        myDrugRecyclerView.setAdapter(myDrugAdapter);
    }

    private void refresh() {
        SwipeRefreshLayout swipeRefresh = findViewById(R.id.refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDB();

                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void clickEvent() {
        ImageView iv_plus_btn = findViewById(R.id.iv_plus);

        iv_plus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
