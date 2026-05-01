package com.example.reloop.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class PersonalProfileFragment extends Fragment {

    private TextInputEditText etUsername, etEmail, etPassword, etBio, etAddress;
    private ImageView ivProfileAvatar;
    private MaterialButton btnSaveProfile;

    private Uri selectedImageUri;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    // Image Picker for Avatar
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileAvatar.setImageURI(uri);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        }

        // Bind Views from fragment_personal_profile.xml
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar);
        etUsername = view.findViewById(R.id.et_profile_username);
        etEmail = view.findViewById(R.id.et_profile_email);
        etPassword = view.findViewById(R.id.et_profile_password);
        etBio = view.findViewById(R.id.et_profile_bio);
        etAddress = view.findViewById(R.id.et_profile_address);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);

        // Click listeners for navigation on read-only fields
        etEmail.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_personalProfileFragment_to_securityFragment));
        etPassword.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_personalProfileFragment_to_securityFragment));
        etAddress.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_personalProfileFragment_to_addressFragment));

        // Load data initially
        loadProfileData();

        // Avatar change click
        view.findViewById(R.id.layout_change_avatar).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Save button click
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadProfileData() {
        if (currentUser == null) return;

        // Load Auth info
        etEmail.setText(currentUser.getEmail());
        etPassword.setText("••••••••");

        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(ivProfileAvatar);
        }

        // Fetch real-time data (Username, Bio, Address) from Database
        if (userRef != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String dbUsername = snapshot.child("username").getValue(String.class);
                        String dbBio = snapshot.child("bio").getValue(String.class);
                        String dbAddress = snapshot.child("address").getValue(String.class);

                        etUsername.setText(dbUsername != null ? dbUsername : currentUser.getDisplayName());
                        etBio.setText(dbBio != null ? dbBio : "");
                        etAddress.setText(dbAddress != null ? dbAddress : "No address set");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveProfileChanges() {
        String newName = etUsername.getText().toString().trim();
        String newBio = etBio.getText().toString().trim();

        if (newName.isEmpty()) {
            etUsername.setError("Username is required");
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        if (selectedImageUri != null) {
            uploadAvatarAndSave(newName, newBio);
        } else {
            updateFirebaseProfile(newName, newBio, currentUser.getPhotoUrl());
        }
    }

    private void uploadAvatarAndSave(String name, String bio) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_pics/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                        updateFirebaseProfile(name, bio, uri)
                )
        ).addOnFailureListener(e -> {
            btnSaveProfile.setEnabled(true);
            btnSaveProfile.setText("Save Changes");
            Toast.makeText(getContext(), "Avatar upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFirebaseProfile(String name, String bio, Uri photoUri) {
        // 1. Update Auth Profile
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photoUri)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 2. Update Realtime Database
                Map<String, Object> updates = new HashMap<>();
                updates.put("username", name);
                updates.put("bio", bio);

                userRef.updateChildren(updates).addOnCompleteListener(dbTask -> {
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("Save Changes");
                    if (dbTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Save Changes");
            }
        });
    }
}