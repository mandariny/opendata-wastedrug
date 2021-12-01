package com.example.waste_drug.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface MyDrugInfoDao {
    @Query("SELECT * FROM MyDrugInfo ORDER BY subscribe DESC, uid DESC")
    List<MyDrugInfo> getAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMyDrug(MyDrugInfo myDrugInfo);

    @Update
    void updateMyDrug(MyDrugInfo myDrugInfo);

    @Delete
    void deleteMyDrug(MyDrugInfo myDrugInfo);
}
