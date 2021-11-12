package com.example.waste_drug;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.waste_drug.data.GridItem;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    ArrayList<GridItem> items = new ArrayList<>();
    Context context;

    public void insertItem(GridItem item) {
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        GridItem value = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gridview_item, parent, false);
        }
        LinearLayout linearLayout = convertView.findViewById(R.id.li_gridview_item);
        TextView name = convertView.findViewById(R.id.tv_value);
        TextView content = convertView.findViewById(R.id.tv_content);
        name.setText(value.getName());
        content.setText(value.getContent());
        // linearLayout.setBackgroundResource(R.color.grid_background_color);
        return convertView;
    }
}
