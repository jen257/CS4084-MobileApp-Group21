package com.example.reloop.ui.settings;

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
import com.example.reloop.ui.settings.viewmodel.SettingsViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private SwitchMaterial switchNotifications;
    private TextView tvPersonalProfile, tvAddressManagement, tvAccountSecurity;
    private Button btnLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        tvPersonalProfile = view.findViewById(R.id.tvPersonalProfile);
        tvAddressManagement = view.findViewById(R.id.tvAddressManagement);
        tvAccountSecurity = view.findViewById(R.id.tvAccountSecurity);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        btnLogout = view.findViewById(R.id.btnLogout);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        settingsViewModel.getNotificationStatus().observe(getViewLifecycleOwner(), isEnabled -> {
            if (switchNotifications.isChecked() != isEnabled) {
                switchNotifications.setChecked(isEnabled);
            }
        });
    }

    private void setupListeners() {
        // Navigate to Personal Profile using the correct action ID from nav_graph
        tvPersonalProfile.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_personalProfileFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(getContext(), "Navigation error: check nav_graph actions", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigates to Address Management
        tvAddressManagement.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_addressFragment)
        );

        // Navigates to Security Fragment
        tvAccountSecurity.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_securityFragment)
        );

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                settingsViewModel.setNotifications(isChecked)
        );

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Navigate back to Login and clear the backstack
            Navigation.findNavController(requireView()).navigate(R.id.action_settingsFragment_to_loginFragment);
        });
    }
}