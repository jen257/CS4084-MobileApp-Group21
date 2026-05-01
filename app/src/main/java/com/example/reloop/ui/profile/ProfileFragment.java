package com.example.reloop.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    // UI Components
    private TextView tvUserName, tvUserEmail, tvEditProfile; // Added tvUserName here
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
        tvUserName = view.findViewById(R.id.tvUserName); // Bind the username text view
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

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            // 1. Set the Email correctly
            if (tvUserEmail != null) {
                tvUserEmail.setText(user.getEmail());
            }

            // 2. Set the Username correctly
            if (tvUserName != null) {
                String displayName = user.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    tvUserName.setText(displayName);
                } else {
                    tvUserName.setText("Add Username"); // Fallback if they haven't set one yet
                }
            }

            // 3. Set the Avatar
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
        profileViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
        });
    }

    private void setupListeners() {
        tvEditProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment)
        );

        tvSelling.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("showSoldItems", false);
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_userProductsFragment, bundle);
        });

        tvSold.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("showSoldItems", true);
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_userProductsFragment, bundle);
        });

        tvMyWishlist.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.wishlistFragment)
        );

        tvSettings.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_settingsFragment)
        );
    }
}