package com.example.waste_drug.recycle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waste_drug.R;

public class RecycleActivity extends AppCompatActivity {
    String[] title = {"알약 분리배출 방법", "가루약 분리배출 방법", "물약 분리배출 방법", "연고,안약 분리배출 방법"};
    String[] message = {"포장된 비닐, 종이 등을 제거한 뒤 내용물만 모아 배출", "포장지를 뜯지 않고 그대로 배출", "한 병에 모을 수 있는 만큼 모아, 새지 않도록 밀봉하여 배출", "겉의 종이박스만 제거하고 용기째 배출"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);

        clickEvent();
    }

    private void clickEvent() {
        clickPillButton();
        clickPowderButton();
        clickLiquidButton();
        clickOintmentButton();
    }

    private void clickPillButton() {
        RelativeLayout pillLayout = findViewById(R.id.rl_pill);
        pillLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog(0);
            }
        });
    }

    private void clickPowderButton() {
        RelativeLayout powderLayout = findViewById(R.id.rl_powder);
        powderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog(1);
            }
        });
    }

    private void clickLiquidButton() {
        RelativeLayout liquidLayout = findViewById(R.id.rl_liquid);
        liquidLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog(2);
            }
        });
    }

    private void clickOintmentButton() {
        RelativeLayout ointmentLayout = findViewById(R.id.rl_ointment);
        ointmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog(3);
            }
        });
    }

    private void showAlertDialog(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecycleActivity.this);
        builder.setTitle(title[index]).setMessage(message[index])
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
