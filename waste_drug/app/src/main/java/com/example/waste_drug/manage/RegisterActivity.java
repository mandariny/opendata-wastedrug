package com.example.waste_drug.manage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText drugName, drugExpiryDate, drugEffect, drugAddInfo;
    private ImageView drugPicture;
    private TextView drugText;
    private Button registerButton;
    private String name, date, effect, pic, addInfo;
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
        drugExpiryDate = findViewById(R.id.et_date);
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
            }
        });
    }

    private void inputDrugExpiryDate() {
        drugExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                date = editable.toString();
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
                inputMyDrugIntoDB();
            }
        });
    }

    private void inputMyDrugIntoDB() {
        MyDrugInfo myDrugInfo = new MyDrugInfo(0, name, date, effect, pic, addInfo);
        MyDrugDatabase db = MyDrugDatabase.getInstance(getApplicationContext());

        class InsertRunnable implements Runnable {
            @Override
            public void run() {
                try{
                   db.myDrugInfoDao().insertMyDrug(myDrugInfo);
                   // Toast.makeText(getApplicationContext(), "등록 완료!", Toast.LENGTH_SHORT).show();

                   Intent intent = new Intent(getApplicationContext(), ManageActivity.class);
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
