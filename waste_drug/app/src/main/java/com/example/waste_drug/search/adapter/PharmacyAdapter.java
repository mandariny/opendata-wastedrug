package com.example.waste_drug.search.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waste_drug.R;
import com.example.waste_drug.data.Pharmacy;

import java.util.ArrayList;

public class PharmacyAdapter extends RecyclerView.Adapter<PharmacyAdapter.PharmacyViewHolder> {

    private ArrayList<Pharmacy> pharmacyList;
    private Context context;
    private LayoutInflater mInflate;
    OnPharmacyItemClickListener pharmacy_listener = null;

    public PharmacyAdapter(Context context, ArrayList<Pharmacy> pharmacyList) {
        this.pharmacyList = pharmacyList;
        this.context = context;
        this.mInflate = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public PharmacyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflate.inflate(R.layout.layout_pharmacy_item, parent, false);
        return new PharmacyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PharmacyViewHolder holder, int position) {
        holder.tvName.setText(pharmacyList.get(position).getDutyName());
        holder.tvAddress.setText(pharmacyList.get(position).getDutyAddr());
        holder.tvPhone.setText(pharmacyList.get(position).getDutyTel1());

        String time = "";
        if (pharmacyList.get(position).isOpenInHoliday()) {
            time += "공휴일 / ";
        }
        if (pharmacyList.get(position).isOpenInNight()) {
            time += "평일 야간 / ";
        }
        if (pharmacyList.get(position).isOpenInSaturday()) {
            time += "토요일 / ";
        }
        if (pharmacyList.get(position).isOpenInSunday()) {
            time += "일요일 / ";
        }

        time = time.substring(0, time.length() - 2);
        holder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return pharmacyList.size();
    }

    //리사이클러뷰 이벤트 리스너
    public interface OnPharmacyItemClickListener{
        void onItemClick(View v, int pos);
    }

    public void setOnItemClicklistener(OnPharmacyItemClickListener listener){
        this.pharmacy_listener = listener;
    }

    public class PharmacyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvAddress;
        public TextView tvPhone;
        public TextView tvTime;

        public PharmacyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvTime = itemView.findViewById(R.id.tv_time);

            //클릭 이벤트 리스너
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        pharmacy_listener.onItemClick(v, pos);

                    }
                }
            });
        }
    }


}
