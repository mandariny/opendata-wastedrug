package com.example.waste_drug.search.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import com.example.waste_drug.search.drugbox.DrugBoxFragment;
import com.example.waste_drug.search.pharmacy.PharmacyFragment;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> items;
    private ArrayList<String> title = new ArrayList<>();
    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        items = new ArrayList<>();
        items.add(new DrugBoxFragment());
        items.add(new PharmacyFragment());

        title.add("수거함 정보");
        title.add("약국 정보");
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return items.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }
}
