package com.example.reloop.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reloop.database.entities.SettingsEntity;

@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSettings(SettingsEntity settings);

    @Update
    void updateSettings(SettingsEntity settings);

    @Query("SELECT * FROM settings_table WHERE userId = :userId LIMIT 1")
    LiveData<SettingsEntity> getSettingsByUserId(String userId);

    @Query("DELETE FROM settings_table WHERE userId = :userId")
    void deleteSettingsByUserId(String userId);
}