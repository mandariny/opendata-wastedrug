package com.example.waste_drug.slide;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.waste_drug.R;

public class SlideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_detail);

        setToolbar();
        getIntents();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("약:속");
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
        ImageView poster = findViewById(R.id.iv_poster);

        Intent intent = getIntent();
        int position = intent.getIntExtra("slide", -1);

        if (position == 0) {
            poster.setBackgroundResource(R.drawable.feed_big1);
        } else if (position == 1) {
            poster.setBackgroundResource(R.drawable.feed_big2);
        } else if (position == 2) {
            poster.setBackgroundResource(R.drawable.feed_big3);
        }
    }
}
