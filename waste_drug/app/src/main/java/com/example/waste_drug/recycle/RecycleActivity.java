package com.example.waste_drug.recycle;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.waste_drug.R;

public class RecycleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);

        setToolbar();
        clickEvent();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("폐의약품 분리배출 방법");
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

    private void clickEvent() {
        clickPillButton();
        clickPowderButton();
        clickLiquidButton();
        clickOintmentButton();
    }

    private void clickPillButton() {
        RelativeLayout pillLayout = findViewById(R.id.rl_pill);
        pillLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActivity(0);
            }
        });
    }

    private void clickPowderButton() {
        RelativeLayout powderLayout = findViewById(R.id.rl_powder);
        powderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActivity(1);
            }
        });
    }

    private void clickLiquidButton() {
        RelativeLayout liquidLayout = findViewById(R.id.rl_liquid);
        liquidLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActivity(2);
            }
        });
    }

    private void clickOintmentButton() {
        RelativeLayout ointmentLayout = findViewById(R.id.rl_ointment);
        ointmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActivity(3);
            }
        });
    }

    private void showActivity(int index) {
        Intent intent = new Intent(getApplicationContext(), RecycleDetailActivity.class);
        intent.putExtra("index", index);
        startActivity(intent);
    }

}
