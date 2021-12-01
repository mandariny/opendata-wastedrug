package com.example.waste_drug.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MyDrugInfo.class}, version = 1)
public abstract class MyDrugDatabase extends RoomDatabase {
    public abstract MyDrugInfoDao myDrugInfoDao();

    private static MyDrugDatabase INSTANCE = null;

    public static MyDrugDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), MyDrugDatabase.class, "mydrug-db").build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}