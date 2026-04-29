package com.example.reloop.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings_table")
public class SettingsEntity {

    @PrimaryKey
    @NonNull
    private String userId = "";

    private boolean isDarkModeEnabled;
    private boolean areNotificationsEnabled;

    public SettingsEntity() {
        // Default constructor required by Room
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public boolean isDarkModeEnabled() {
        return isDarkModeEnabled;
    }

    public void setDarkModeEnabled(boolean darkModeEnabled) {
        isDarkModeEnabled = darkModeEnabled;
    }

    public boolean areNotificationsEnabled() {
        return areNotificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        areNotificationsEnabled = notificationsEnabled;
    }
}