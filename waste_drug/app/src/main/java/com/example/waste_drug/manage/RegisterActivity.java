package com.example.waste_drug.manage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.example.waste_drug.R;
import com.example.waste_drug.db.AppDatabase;
import com.example.waste_drug.db.MyDrugDatabase;
import com.example.waste_drug.db.MyDrugInfo;

import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    private EditText drugName, drugEffect, drugAddInfo;
    private ImageView drugPicture;
    private TextView drugText;
    private Button registerButton, drugExpiryDateButton;
    private String name = "", date = "", effect = "", pic = "", addInfo = "";
    private final int GET_GALLERY_IMAGE_CODE = 200, PERMISSION_GALLERY_CODE = 100;
    private final String[] REQUIRED_PERMISSION = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputDrugInformation();
    }

    private void inputDrugInformation() {
        drugName = findViewById(R.id.et_name);
        drugExpiryDateButton = findViewById(R.id.btn_date);
        drugEffect = findViewById(R.id.et_effect);
        drugPicture = findViewById(R.id.iv_drug_photo);
        drugAddInfo = findViewById(R.id.et_option);
        drugText = findViewById(R.id.tv_image);
        registerButton = findViewById(R.id.btn_register);

        inputDrugName();
        inputDrugExpiryDate();
        inputDrugEffect();
        inputDrugAddInfo();
        inputDrugPicture();
        clickRegisterButton();
    }

    private void inputDrugName() {
        drugName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                name = editable.toString();
                drugException();
            }
        });
    }

    private void inputDrugExpiryDate() {
        Calendar cal = Calendar.getInstance();

        drugExpiryDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date = String.format("%d-%d-%d", year, month + 1, dayOfMonth);
                        drugExpiryDateButton.setText(date);
                        drugException();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                dialog.getDatePicker().setMinDate(new Date().getTime());
                dialog.show();
            }
        });
    }

    private void inputDrugEffect() {
        drugEffect.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                effect = editable.toString();
                drugException();
            }
        });
    }

    private void inputDrugAddInfo() {
        drugAddInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                addInfo = editable.toString();
                drugException();
            }
        });
    }

    private void inputDrugPicture() {
        drugPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });
    }

    private void checkPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_GALLERY_CODE);
        } else {
            pickFromGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_GALLERY_CODE) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery();
            } else {
                Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GET_GALLERY_IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_GALLERY_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    if (data != null) {
                        Uri uri = data.getData();
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .into(drugPicture);
                        pic = uri.toString();
                        drugText.setVisibility(View.INVISIBLE);
                        drugException();
                    }
                } catch (Exception ignored) {

                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "이미지 가져오기 취소", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clickRegisterButton() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void drugException() {
        Log.d("MAIN", name + " " + date + " " + effect + " " + pic + " " + addInfo);

        if(!name.equals("") && !date.equals("") && !effect.equals("") && !pic.equals("")) {
            registerButton.getBackground().setColorFilter(Color.parseColor("#e7d6af"), PorterDuff.Mode.MULTIPLY);
            registerButton.setEnabled(true);
        } else {
            registerButton.getBackground().setColorFilter(Color.parseColor("#dcdcdc"), PorterDuff.Mode.MULTIPLY);
            registerButton.setEnabled(false);
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("약 등록").setMessage("입력하신 약을 등록하시겠습니까?");
        builder.setNegativeButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputMyDrugIntoDB();
                Toast.makeText(getApplicationContext(), "등록 완료!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void inputMyDrugIntoDB() {
        MyDrugInfo myDrugInfo = new MyDrugInfo(0, name, date, effect, pic, addInfo, 0);
        MyDrugDatabase db = MyDrugDatabase.getInstance(getApplicationContext());

        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                try{
                   db.myDrugInfoDao().insertMyDrug(myDrugInfo);

                   Intent intent = new Intent(getApplicationContext(), ManageActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(intent);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        InsertRunnable insertRunnable = new InsertRunnable();
        Thread thread = new Thread(insertRunnable);
        thread.start();
    }
}
