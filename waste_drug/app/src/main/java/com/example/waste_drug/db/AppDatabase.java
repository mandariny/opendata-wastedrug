package com.example.waste_drug.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DrugBox.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DrugBoxDao drugBoxDao();

    private static AppDatabase INSTANCE = null;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "drugbox-db").build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}


