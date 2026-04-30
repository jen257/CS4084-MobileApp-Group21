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

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.loginFragment || id == R.id.registerFragment) {
                    bottomNav.setVisibility(View.GONE);
                } else {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            });
        }

        // Register global network callback to toggle the offline overlay
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    runOnUiThread(() -> offlineOverlay.setVisibility(View.GONE));
                }

                @Override
                public void onLost(@NonNull Network network) {
                    runOnUiThread(() -> offlineOverlay.setVisibility(View.VISIBLE));
                }
            });
        }
    }
}