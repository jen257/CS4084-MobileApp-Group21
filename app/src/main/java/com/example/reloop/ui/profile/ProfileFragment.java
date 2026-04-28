package com.example.reloop.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.reloop.R;
import com.example.reloop.ui.profile.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private TextView tvUserEmail, tvEditProfile;
    private TextView tvSelling, tvSold, tvMyWishlist, tvSettings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvEditProfile = view.findViewById(R.id.tvEditProfile);
        tvSelling = view.findViewById(R.id.tvSelling);
        tvSold = view.findViewById(R.id.tvSold);
        tvMyWishlist = view.findViewById(R.id.tvMyWishlist);
        tvSettings = view.findViewById(R.id.tvSettings);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        profileViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null && firebaseUser.getEmail() != null) {
                tvUserEmail.setText(firebaseUser.getEmail());
            }
        });
        // Removed logout observer as it is now handled in SettingsFragment
    }

    private void setupListeners() {
        tvEditProfile.setOnClickListener(v ->
                Toast.makeText(getContext(), "Edit Profile coming soon", Toast.LENGTH_SHORT).show()
        );

        tvSettings.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_settingsFragment)
        );

        // Add other listeners (Selling, Sold, Wishlist) here as needed
    }
}