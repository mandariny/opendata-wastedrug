package com.example.waste_drug.search;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.waste_drug.R;
import com.example.waste_drug.search.adapter.PharmacyAdapter;
import com.example.waste_drug.search.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViewPager();
        setToolbar();
    }

    public void initViewPager() {
        ViewPager viewPager = findViewById(R.id.vp_view);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        initTabs(viewPager);
    }

    public void initTabs(ViewPager viewPager) {
        TabLayout tabs = findViewById(R.id.tab);
        tabs.setupWithViewPager(viewPager);

        Intent intent = getIntent();
        int index = intent.getIntExtra("index", -1);
        tabs.selectTab(tabs.getTabAt(index));
        tabs.getTabAt(0).setIcon(R.drawable.box_icon);
        tabs.getTabAt(1).setIcon(R.drawable.pharmacy_icon);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("수거함/약국 검색");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
