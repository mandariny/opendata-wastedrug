package com.example.waste_drug.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;


@Entity
public class MyDrugInfo implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int uid = 0;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "expiryDate")
    public String date;

    @ColumnInfo(name = "effect")
    public String effect;

    @ColumnInfo(name = "picture")
    public String picture;

    @ColumnInfo(name = "addInfo")
    public String addInfo;

    @ColumnInfo(name = "subscribe")
    public int subscribe = 0;

    public MyDrugInfo(int uid, String name, String date, String effect, String picture, String addInfo, int subscribe) {
        this.uid = uid;
        this.name = name;
        this.date = date;
        this.effect = effect;
        this.picture = picture;
        this.addInfo = addInfo;
        this.subscribe = subscribe;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getAddInfo() {
        return addInfo;
    }

    public void setAddInfo(String addInfo) {
        this.addInfo = addInfo;
    }

    public int getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(int subscribe) {
        this.subscribe = subscribe;
    }
}
