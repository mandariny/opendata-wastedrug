package com.example.waste_drug.manage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.waste_drug.R;
import com.example.waste_drug.db.MyDrugDatabase;
import com.example.waste_drug.db.MyDrugInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyDrugAdapter extends RecyclerView.Adapter<MyDrugAdapter.MyDrugViewHolder> {

    private List<MyDrugInfo> myDrugList;
    private Context mContext;

    public MyDrugAdapter(List<MyDrugInfo> myDrugList, Context context) {
        this.myDrugList = myDrugList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyDrugAdapter.MyDrugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.my_drug_item, parent, false);
        return new MyDrugViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyDrugViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final MyDrugInfo myDrugInfo = myDrugList.get(position);

        long diffDays = 0;
        try {
            diffDays = getDiffTime(myDrugInfo.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (diffDays < 7) {
            holder.expiryDate.setTextColor(Color.parseColor("#eb0000"));
        } else {
            holder.expiryDate.setTextColor(Color.parseColor("#0065AA"));
        }

        makeDateFormat(myDrugInfo);
        holder.name.setText(myDrugInfo.name);
        holder.expiryDate.setText("유통기한 : " + myDrugInfo.date);
        Glide.with(holder.itemView.getContext())
                .load(myDrugInfo.picture)
                .centerCrop()
                .into(holder.picture);

        if (myDrugInfo.subscribe == 1) {
            holder.subscribe.setVisibility(View.VISIBLE);
        } else {
            holder.subscribe.setVisibility(View.INVISIBLE);
        }

        holder.option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(holder, v, position);
            }
        });
    }

    private long getDiffTime(String date) throws ParseException {
        Calendar todayDate = Calendar.getInstance();
        todayDate.setTime(new Date());

        @SuppressLint("SimpleDateFormat")
        Date drugDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        Calendar cmpDate = Calendar.getInstance();
        if (drugDate != null) {
            cmpDate.setTime(drugDate);
        }

        long diffSec = (cmpDate.getTimeInMillis() - todayDate.getTimeInMillis()) / 1000;

        return diffSec / (24 * 60 * 60);
    }

    @SuppressLint("SimpleDateFormat")
    private void makeDateFormat(MyDrugInfo info) {
        try {
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date formatDate = targetFormat.parse(info.date);
            info.date = newFormat.format(formatDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showPopup(@NonNull final MyDrugViewHolder holder, View v, int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, holder.option);
        popupMenu.inflate(R.menu.popup_menu);
        Menu menu = popupMenu.getMenu();

        if (myDrugList.get(position).subscribe == 1) {
            menu.getItem(1).setTitle("구독 취소");
        } else {
            menu.getItem(1).setTitle("구독 하기");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_popup_delete:
                        deleteDrugInfo(v, position);
                        Toast.makeText(mContext, "삭제 완료!", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_popup_star:
                        if (myDrugList.get(position).subscribe == 0) {
                            subscribeDrugInfo(v, position, true);
                            Toast.makeText(mContext, "구독 완료!", Toast.LENGTH_SHORT).show();
                        } else {
                            subscribeDrugInfo(v, position, false);
                            Toast.makeText(mContext, "구독 취소!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteDrugInfo(View v, int position) {
        MyDrugDatabase db = MyDrugDatabase.getInstance(v.getContext());
        MyDrugInfo drugInfo = myDrugList.remove(position);
        notifyDataSetChanged();

        class removeRunnable implements Runnable {
            @Override
            public void run() {
                try {
                    db.myDrugInfoDao().deleteMyDrug(drugInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        removeRunnable removeRunnable = new removeRunnable();
        Thread thread = new Thread(removeRunnable);
        thread.start();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void subscribeDrugInfo(View v, int position, boolean idx) {
        MyDrugDatabase db = MyDrugDatabase.getInstance(v.getContext());

        if (idx) {
            myDrugList.get(position).subscribe = 1;
        } else {
            myDrugList.get(position).subscribe = 0;
        }
        notifyDataSetChanged();

        class subscribeRunnable implements Runnable {
            @Override
            public void run() {
                try {
                    db.myDrugInfoDao().updateMyDrug(myDrugList.get(position));
                    myDrugList = db.myDrugInfoDao().getAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        subscribeRunnable subscribeRunnable = new subscribeRunnable();
        Thread thread = new Thread(subscribeRunnable);
        thread.start();
    }

    @Override
    public int getItemCount() {
        return myDrugList.size();
    }

    public class MyDrugViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView expiryDate;
        ImageView picture;
        ImageButton option;
        TextView subscribe;

        public MyDrugViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_drug_name);
            expiryDate = itemView.findViewById(R.id.tv_expiry_date);
            picture = itemView.findViewById(R.id.iv_drug_photo);
            option = itemView.findViewById(R.id.btn_option);
            subscribe = itemView.findViewById(R.id.tv_subscribe);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DetailActivity.class);
                    intent.putExtra("drugInfo", myDrugList.get(getAdapterPosition()));
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
