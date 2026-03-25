package com.example.reloop;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Bottom Navigation and NavHost
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            // Link BottomNavigationView with NavController
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Control BottomNav visibility based on current destination
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Hide BottomNav on Auth screens (Login/Register)
                if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.registerFragment) {
                    bottomNav.setVisibility(View.GONE);
                } else {
                    // Show BottomNav on main screens (Home/Wishlist)
                    bottomNav.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    // Create options menu for Logout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add logout item to the menu
        menu.add(0, 1, 0, R.string.action_logout).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check if Logout was clicked
        if (item.getItemId() == 1) {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Redirect to Login screen and clear navigation history
            if (navController != null) {
                navController.navigate(R.id.loginFragment);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}