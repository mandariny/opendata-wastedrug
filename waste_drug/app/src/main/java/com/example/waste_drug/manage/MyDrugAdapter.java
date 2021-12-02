package com.example.waste_drug.manage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.waste_drug.R;
import com.example.waste_drug.db.MyDrugInfo;

import java.util.ArrayList;
import java.util.List;

public class MyDrugAdapter extends RecyclerView.Adapter<MyDrugAdapter.MyDrugViewHolder> {

    private List<MyDrugInfo> myDrugList;

    public MyDrugAdapter(List<MyDrugInfo> myDrugList) {
        this.myDrugList = myDrugList;
    }

    @NonNull
    @Override
    public MyDrugAdapter.MyDrugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.my_drug_item, parent, false);
        MyDrugAdapter.MyDrugViewHolder viewHolder = new MyDrugAdapter.MyDrugViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyDrugAdapter.MyDrugViewHolder holder, int position) {
        MyDrugInfo myDrugInfo = myDrugList.get(position);

        holder.name.setText(myDrugInfo.name);
        holder.expiryDate.setText(myDrugInfo.date);
        Glide.with(holder.itemView.getContext())
                .load(myDrugInfo.picture)
                .centerCrop()
                .into(holder.picture);
    }

    @Override
    public int getItemCount() {
        return myDrugList.size();
    }

    public class MyDrugViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView expiryDate;
        ImageView picture;

        public MyDrugViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_drug_name);
            expiryDate = itemView.findViewById(R.id.tv_expiry_date);
            picture = itemView.findViewById(R.id.iv_drug_photo);
        }
    }
}
