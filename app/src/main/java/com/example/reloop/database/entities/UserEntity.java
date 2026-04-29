package com.example.reloop.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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
    @Ignore
    public UserEntity(@NonNull String uid, String email, String displayName,
                      String phoneNumber, String profileImageUrl) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.isDarkModeEnabled = false;
        this.isNotificationsEnabled = true;
    }
}