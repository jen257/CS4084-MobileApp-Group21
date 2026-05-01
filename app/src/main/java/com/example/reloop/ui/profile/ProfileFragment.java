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

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    // UI Components
    private TextView tvUserName, tvUserEmail, tvEditProfile;
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

        // Bind all UI components
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvEditProfile = view.findViewById(R.id.tvEditProfile);
        tvSelling = view.findViewById(R.id.tvSelling);
        tvSold = view.findViewById(R.id.tvSold);
        tvMyWishlist = view.findViewById(R.id.tvMyWishlist);
        tvSettings = view.findViewById(R.id.tvSettings);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        loadUserData();
        setupListeners();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (tvUserName != null) {
                tvUserName.setText((displayName != null && !displayName.isEmpty())
                        ? displayName : "Add Username");
            }

            if (tvUserEmail != null) {
                tvUserEmail.setText(user.getEmail());
            }

            if (ivAvatar != null && user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.img_profile)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }
    }

    private void setupListeners() {
        tvEditProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_editProfileFragment)
        );

        tvSelling.setOnClickListener(v ->
                Toast.makeText(getContext(), "Items on sell coming soon", Toast.LENGTH_SHORT).show()
        );

        tvSold.setOnClickListener(v ->
                Toast.makeText(getContext(), "Sold items coming soon", Toast.LENGTH_SHORT).show()
        );

        tvMyWishlist.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_wishlistFragment)
        );

        tvSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_settingsFragment)
        );
    }
}
