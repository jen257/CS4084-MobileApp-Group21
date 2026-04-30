package com.example.reloop.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.reloop.database.daos.MessageDao;
import com.example.reloop.database.daos.ProductDao;
import com.example.reloop.database.daos.SettingsDao;
import com.example.reloop.database.daos.UserDao;

import com.example.reloop.database.entities.MessageEntity;
import com.example.reloop.database.entities.ProductEntity;
import com.example.reloop.database.entities.SettingsEntity;
import com.example.reloop.database.entities.UserEntity;

import com.example.reloop.database.converters.DateConverter;

/**
 * Singleton Room Database setup containing ALL app tables.
 */
@Database(entities = {ProductEntity.class, MessageEntity.class, UserEntity.class, SettingsEntity.class}, version = 4, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDataBase extends RoomDatabase {

    public abstract ProductDao productDao();
    public abstract MessageDao messageDao();
    public abstract UserDao userDao();
    public abstract SettingsDao settingsDao();

    private static volatile AppDataBase INSTANCE;

    public static AppDataBase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDataBase.class, "reloop_local_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}