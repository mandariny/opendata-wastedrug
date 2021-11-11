package com.example.waste_drug.search;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
    }
}
