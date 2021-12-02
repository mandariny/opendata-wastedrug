package com.example.waste_drug.manage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.waste_drug.R;
import com.example.waste_drug.db.MyDrugInfo;

public class DetailActivity extends AppCompatActivity {
    private TextView drugName, drugExpiryDate, drugEffect, drugAddInfo;
    private ImageView drugImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initViews();
        setViews();
    }

    private void initViews() {
        drugName = findViewById(R.id.tv_name);
        drugExpiryDate = findViewById(R.id.tv_date);
        drugEffect = findViewById(R.id.tv_effect);
        drugImage = findViewById(R.id.iv_drug_photo);
        drugAddInfo = findViewById(R.id.tv_option);
    }

    private void setViews() {
        Intent intent = getIntent();
        MyDrugInfo myDrugInfo = (MyDrugInfo) intent.getSerializableExtra("drugInfo");

        drugName.setText(myDrugInfo.name);
        drugExpiryDate.setText(myDrugInfo.date);
        drugEffect.setText(myDrugInfo.effect);
        Glide.with(getApplicationContext())
                .load(myDrugInfo.picture)
                .into(drugImage);
        drugAddInfo.setText(myDrugInfo.addInfo);
    }
}
