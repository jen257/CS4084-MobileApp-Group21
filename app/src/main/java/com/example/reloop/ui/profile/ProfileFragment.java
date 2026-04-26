package com.example.reloop.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.reloop.R;
import com.example.reloop.shared.views.LoadingButton;
import com.example.reloop.ui.profile.viewmodel.ProfileViewModel;

/**
 * Fragment responsible for displaying and managing user profile.
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private TextView tvUserEmail;
    private LoadingButton btnEditProfile;
    private Button btnLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnEditProfile.setText("Edit Profile");

        setupObservers(view);

        btnEditProfile.setOnButtonClickListener(v ->
                Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> profileViewModel.logout());
    }

    private void setupObservers(View view) {
        // Optimized: Statement lambda replaced with expression lambda
        profileViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null && firebaseUser.getEmail() != null) {
                tvUserEmail.setText(firebaseUser.getEmail());
            }
        });

        // Optimized Lambda
        profileViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading ->
                btnEditProfile.setLoading(isLoading)
        );

        profileViewModel.successMessage.observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });

        // Fixed navigation: action ID now matches nav_graph.xml
        profileViewModel.getNavigateToLogin().observe(getViewLifecycleOwner(), navigate -> {
            if (navigate != null && navigate) {
                Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_loginFragment);
            }
        });
    }
}