package com.example.waste_drug.shipping;

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

import java.util.LinkedList;
import java.util.List;

public class ShippingAdapter extends RecyclerView.Adapter<ShippingAdapter.ShippingViewHolder> {

    private List<MyDrugInfo> subscribeList = new LinkedList<>();

    public ShippingAdapter(List<MyDrugInfo> subscribeList) {
        this.subscribeList = subscribeList;
    }

    @NonNull
    @Override
    public ShippingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.shipping_item, parent, false);
        ShippingAdapter.ShippingViewHolder viewHolder = new ShippingAdapter.ShippingViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShippingViewHolder holder, int position) {
        MyDrugInfo drugInfo = subscribeList.get(position);

        if (drugInfo.subscribe == 1) {
            holder.name.setText(drugInfo.name);
            holder.effect.setText(drugInfo.effect);
            Glide.with(holder.itemView.getContext())
                    .load(drugInfo.picture)
                    .centerCrop()
                    .into(holder.picture);
        }
    }

    @Override
    public int getItemCount() {
        return subscribeList.size();
    }

    public class ShippingViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView effect;
        ImageView picture;

        public ShippingViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_drug_name);
            effect = itemView.findViewById(R.id.tv_effect);
            picture = itemView.findViewById(R.id.iv_drug_photo);
        }
    }
}
