package com.example.waste_drug.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DrugBoxDao {
    @Query("SELECT * FROM DrugBox")
    List<DrugBox> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDrugBox(DrugBox drugBox);
}
