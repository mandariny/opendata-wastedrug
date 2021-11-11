package com.example.waste_drug.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DrugBox {
    @PrimaryKey(autoGenerate = true)
    public int uid = 0;

    @ColumnInfo(name="address")
    public String address;

    @ColumnInfo(name="name")
    public String name;

    @ColumnInfo(name="tel")
    public String tel;

    public DrugBox(int uid, String address, String name, String tel) {
        this.uid = uid;
        this.address = address;
        this.name = name;
        this.tel = tel;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
