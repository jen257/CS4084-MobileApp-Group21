package com.example.reloop.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * [Member A]
 * Centralized helper class for Firebase Realtime Database.
 * Members B and C should use this to read/write data.
 */
public class FireBaseHelper {
    private final DatabaseReference mDatabase;

    public FireBaseHelper() {
        // Initialize the root reference of our Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * For Member B: Auth & Profile operations
     */
    public DatabaseReference getUsersRef() {
        return mDatabase.child(Constants.NODE_USERS);
    }

    /**
     * For Member C: Home Feed operations
     */
    public DatabaseReference getProductsRef() {
        return mDatabase.child(Constants.NODE_PRODUCTS);
    }
}