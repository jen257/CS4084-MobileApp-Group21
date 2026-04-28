package com.example.reloop.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.reloop.database.entities.UserEntity;

/**
 * [Member A - System Architect]
 * Data Access Object (DAO) for the UserEntity.
 * Defines the SQL queries and operations for local user data management.
 */
@Dao
public interface UserDao {

    /**
     * Insert a new user or replace if the UID already exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    /**
     * Update existing user information (e.g., when saving settings).
     */
    @Update
    void updateUser(UserEntity user);

    /**
     * Delete user data (e.g., upon logout).
     */
    @Delete
    void deleteUser(UserEntity user);

    /**
     * Delete all users (useful for a clean logout).
     */
    @Query("DELETE FROM users")
    void deleteAllUsers();

    /**
     * Retrieve the currently cached user as an observable LiveData.
     * Always limits to 1 since we only cache the active user.
     */
    @Query("SELECT * FROM users LIMIT 1")
    LiveData<UserEntity> getCurrentUser();

    /**
     * Synchronous fetch for background workers (without LiveData).
     */
    @Query("SELECT * FROM users WHERE uid = :userId LIMIT 1")
    UserEntity getUserByIdSync(String userId);
}