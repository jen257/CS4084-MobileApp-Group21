package com.example.reloop.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * [Member A - System Architect]
 * Room Entity representing the local 'users' table.
 * Caches the currently logged-in user's profile to reduce network calls
 * and support Member B's Profile and Settings features.
 */
@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    public String uid; // Firebase UID, acting as the primary key

    public String email;
    public String displayName;
    public String phoneNumber;
    public String profileImageUrl;

    // Preferences (Supporting Member B's Settings feature)
    public boolean isDarkModeEnabled;
    public boolean isNotificationsEnabled;

    // Required empty constructor for Room
    public UserEntity() {
        this.uid = "";
    }

    public UserEntity(@NonNull String uid, String email, String displayName,
                      String phoneNumber, String profileImageUrl) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        // Default preferences
        this.isDarkModeEnabled = false;
        this.isNotificationsEnabled = true;
    }

    // you can add them here if you prefer strict encapsulation.
}