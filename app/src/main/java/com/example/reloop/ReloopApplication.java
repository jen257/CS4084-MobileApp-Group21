package com.example.reloop;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

/**
 * [Member A - System Architect]
 * Global Application class for initializing core system configurations upon app launch.
 */
public class ReloopApplication extends Application {

    private static final String TAG = "ReloopApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // [Day 5 Bug Fix] Enable Firebase Disk Persistence globally.
        // This ensures data remains accessible during unstable network conditions (Offline Support).
        // CRITICAL: This must be called before any other Firebase Database references are created.
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase offline persistence enabled successfully.");
        } catch (Exception e) {
            // Catching exceptions to prevent crashes during hot-reloads or duplicate initializations.
            Log.e(TAG, "Firebase persistence already enabled or failed: " + e.getMessage());
        }
    }
}