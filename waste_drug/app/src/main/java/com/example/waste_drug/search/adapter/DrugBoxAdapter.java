package com.example.waste_drug.search.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.waste_drug.R;
import com.example.waste_drug.db.DrugBox;

import java.util.ArrayList;

public class DrugBoxAdapter extends RecyclerView.Adapter<DrugBoxAdapter.DrugBoxViewHolder> {
    private ArrayList<DrugBox> drugBoxArrayList = new ArrayList<>();
    OnDrugBoxItemClickListener drugbox_listener = null;

    public DrugBoxAdapter(ArrayList<DrugBox> drugBoxArrayList) {
        this.drugBoxArrayList = drugBoxArrayList;
    }

    @NonNull
    @Override
    public DrugBoxAdapter.DrugBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater  = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_pharmacy_item, parent, false);
        DrugBoxAdapter.DrugBoxViewHolder viewHolder = new DrugBoxAdapter.DrugBoxViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DrugBoxAdapter.DrugBoxViewHolder holder, int position) {
        DrugBox drugBox = drugBoxArrayList.get(position);
        holder.address.setText(drugBox.address);
        holder.name.setText(drugBox.name);
        holder.tel.setText(drugBox.tel);
    }

    @Override
    public int getItemCount() {
        return drugBoxArrayList.size();
    }

    //리사이클러뷰 이벤트 리스너
    public interface OnDrugBoxItemClickListener{
        void onItemClick(View v, int pos);
    }

    public void setOnItemClicklistener(OnDrugBoxItemClickListener listener){
        this.drugbox_listener = listener;
    }


    public class DrugBoxViewHolder extends RecyclerView.ViewHolder {
        TextView address;
        TextView name;
        TextView tel;

        public DrugBoxViewHolder(@NonNull View itemView) {
            super(itemView);

            address = itemView.findViewById(R.id.tv_address);
            name = itemView.findViewById(R.id.tv_name);
            tel = itemView.findViewById(R.id.tv_phone);

            //클릭 이벤트 리스너
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        drugbox_listener.onItemClick(v, pos);

                    }
                }
            });
        }
    }

}
