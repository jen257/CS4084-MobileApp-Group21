package com.example.reloop.activities;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

// Member A: Basic User model for Auth and Profile
@IgnoreExtraProperties
public class user implements Serializable {

    public String userId;       // UID from Firebase
    public String email;
    public String username;
    public String profileImageUrl;
    public long accountCreated; // Timestamp for "member since" feature

    // Firebase needs this empty constructor to work, don't delete!
    public user() {
    }

    // Use this constructor in RegisterFragment after successful signup
    public user(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profileImageUrl = "";
        this.accountCreated = System.currentTimeMillis();
    }
}