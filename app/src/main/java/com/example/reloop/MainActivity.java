package com.example.reloop;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        View offlineOverlay = findViewById(R.id.offline_overlay);

        // Initialize Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Hide bottom navigation on Login/Register screens for a cleaner UI
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.loginFragment || id == R.id.registerFragment) {
                    bottomNav.setVisibility(View.GONE);
                } else {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            });
        }

        // --- Network Monitoring Logic ---
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            // Initial Check: If the app starts without an active network, show the overlay immediately
            if (cm.getActiveNetwork() == null) {
                offlineOverlay.setVisibility(View.VISIBLE);
            }

            // Register a callback to listen for real-time network changes
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    // Back online: Hide overlay on the main UI thread
                    runOnUiThread(() -> offlineOverlay.setVisibility(View.GONE));
                }

                @Override
                public void onLost(@NonNull Network network) {
                    // Connection lost: Show overlay on the main UI thread
                    runOnUiThread(() -> offlineOverlay.setVisibility(View.VISIBLE));
                }
            });
        }
    }
}