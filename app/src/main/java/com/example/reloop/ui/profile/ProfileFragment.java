package com.example.reloop.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.example.reloop.ui.profile.viewmodel.ProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Fragment responsible for displaying user profile information
 * and navigation to related settings or features.
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    // UI Components
    private TextView tvUserEmail, tvEditProfile;
    private TextView tvSelling, tvSold, tvMyWishlist, tvSettings;
    private ImageView ivAvatar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Bind UI components
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvEditProfile = view.findViewById(R.id.tvEditProfile);
        tvSelling = view.findViewById(R.id.tvSelling);
        tvSold = view.findViewById(R.id.tvSold);
        tvMyWishlist = view.findViewById(R.id.tvMyWishlist);
        tvSettings = view.findViewById(R.id.tvSettings);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        loadUserData();
        setupObservers();
        setupListeners();
    }

    /**
     * Load user data directly from FirebaseAuth and render UI using Glide
     */
    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Display name if available, otherwise fallback to email
            if (tvUserEmail != null) {
                String displayName = user.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    tvUserEmail.setText(displayName);
                } else {
                    tvUserEmail.setText(user.getEmail());
                }
            }

            // Load user profile picture using Glide
            if (ivAvatar != null && user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }
    }

    private void setupObservers() {
        // ViewModel observer reserved for future architectural expansion
        profileViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            // Data is handled directly via FirebaseAuth in loadUserData() for now
        });
    }

    /**
     * Set up click listeners for all interactive UI elements
     */
    private void setupListeners() {
        tvEditProfile.setOnClickListener(v ->
                Toast.makeText(getContext(), "Edit Profile coming soon", Toast.LENGTH_SHORT).show()
        );

        // Fixed: Added listeners to clear the "assigned but never accessed" warnings
        tvSelling.setOnClickListener(v ->
                Toast.makeText(getContext(), "Selling feature coming soon", Toast.LENGTH_SHORT).show()
        );

        tvSold.setOnClickListener(v ->
                Toast.makeText(getContext(), "Sold items feature coming soon", Toast.LENGTH_SHORT).show()
        );

        tvMyWishlist.setOnClickListener(v ->
                Toast.makeText(getContext(), "Wishlist feature coming soon", Toast.LENGTH_SHORT).show()
        );

        tvSettings.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_settingsFragment)
        );
    }
}