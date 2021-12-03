package com.example.waste_drug.recycle;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.waste_drug.R;

public class RecycleDetailActivity extends AppCompatActivity {
    String[] message = {"<알약>\n포장된 비닐, 종이 등을 제거한 뒤\n내용물만 모아 배출", "<가루약>\n포장지를 뜯지 않고 그대로 배출", "<물약>\n한 병에 모을 수 있는 만큼 모아,\n새지 않도록 밀봉하여 배출", "<연고/안약>\n겉의 종이박스만 제거하고 용기째 배출"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recycle_detail);

        setToolbar();
        getIntents();
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

    private void getIntents() {
        ImageView poster = findViewById(R.id.iv_recycle_poster);
        TextView content = findViewById(R.id.tv_recycle_content);

        Intent intent = getIntent();
        int index = intent.getIntExtra("index", -1);

        if (index == 0) {
            poster.setImageResource(R.drawable.pill);
            content.setText(message[0]);
        } else if (index == 1) {
            poster.setImageResource(R.drawable.powder);
            content.setText(message[1]);
        } else if (index == 2) {
            poster.setImageResource(R.drawable.liquid);
            content.setText(message[2]);
        } else if (index == 3) {
            poster.setImageResource(R.drawable.ointment);
            content.setText(message[3]);
        }
    }
}
