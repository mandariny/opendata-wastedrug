package com.example.waste_drug.manage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waste_drug.R;
import com.example.waste_drug.db.MyDrugDatabase;
import com.example.waste_drug.db.MyDrugInfo;

import java.util.ArrayList;
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

        getMyDrugs();
        clickEvent();
        setRecyclerView();
    }

    private void getMyDrugs() {
        getDB();
    }

    private void getDB() {
        db = MyDrugDatabase.getInstance(getApplicationContext());

        class GetRunnable implements Runnable {
            @Override
            public void run() {
                try{
                    myDrugInfoArrayList = db.myDrugInfoDao().getAll();
                    setRecyclerView();
                } catch(Exception e) {
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
        for(int i = 0; i < myDrugInfoArrayList.size(); i++) {
            Log.d("MAIN", myDrugInfoArrayList.get(i).picture);
        }
        myDrugRecyclerView = findViewById(R.id.rv_manage);
        myDrugAdapter = new MyDrugAdapter(myDrugInfoArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);

        myDrugRecyclerView.setLayoutManager(layoutManager);
        myDrugRecyclerView.setAdapter(myDrugAdapter);
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
