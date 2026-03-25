package com.example.reloop.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * [Member A]
 * Singleton Room Database setup.
 * This is the local SQLite database for storing offline data like the Wishlist.
 */
// Define the tables (entities) that belong to this database
@Database(entities = {ProductEntity.class}, version = 1, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {

    // Member D will call this to insert/delete wishlist items
    public abstract ProductDao productDao();

    // Singleton instance to prevent memory leaks
    private static volatile AppDataBase INSTANCE;

    public static AppDataBase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDataBase.class) {
                if (INSTANCE == null) {
                    // "reloop_local_db" is the actual file name saved on the phone
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDataBase.class, "reloop_local_db")
                            .fallbackToDestructiveMigration() // Wipes data if schema changes
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}