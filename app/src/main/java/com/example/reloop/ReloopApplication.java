package com.example.reloop;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
public class ReloopApplication extends Application {

    private static final String TAG = "ReloopApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase offline persistence enabled successfully.");
        } catch (Exception e) {
            // Catching exceptions to prevent crashes during hot-reloads or duplicate initializations.
            Log.e(TAG, "Firebase persistence already enabled or failed: " + e.getMessage());
        }
    }
}