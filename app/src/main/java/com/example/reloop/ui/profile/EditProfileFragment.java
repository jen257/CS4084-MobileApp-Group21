package com.example.reloop.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class EditProfileFragment extends Fragment {

    // Strictly matching the IDs in your XML
    private EditText etNewDisplayName, etNewBio;
    private ImageView ivEditAvatar;
    private Button btnSaveProfile;

    private Uri selectedImageUri;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    // Image Picker Launcher
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivEditAvatar.setImageURI(uri);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        }

        // 1. Binding IDs exactly as they appear in your XML
        ivEditAvatar = view.findViewById(R.id.ivEditAvatar);
        etNewDisplayName = view.findViewById(R.id.etNewDisplayName);
        etNewBio = view.findViewById(R.id.etNewBio);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);

        loadExistingData();

        // Listeners
        ivEditAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadExistingData() {
        if (currentUser == null) return;

        // Load current Auth Avatar
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(ivEditAvatar);
        }

        // Fetch Username and Bio from Database to sync the UI
        if (userRef != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String dbUsername = snapshot.child("username").getValue(String.class);
                        String dbBio = snapshot.child("bio").getValue(String.class);

                        // Populate the EditTexts
                        etNewDisplayName.setText(dbUsername != null ? dbUsername : currentUser.getDisplayName());
                        etNewBio.setText(dbBio != null ? dbBio : "");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveProfileChanges() {
        String newName = etNewDisplayName.getText().toString().trim();
        String newBio = etNewBio.getText().toString().trim();

        if (newName.isEmpty()) {
            etNewDisplayName.setError("Username is required");
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(newName, newBio);
        } else {
            updateFirebaseProfile(newName, newBio, currentUser.getPhotoUrl());
        }
    }

    private void uploadImageAndSaveProfile(String name, String bio) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_pics/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                        updateFirebaseProfile(name, bio, uri)
                )
        ).addOnFailureListener(e -> {
            btnSaveProfile.setEnabled(true);
            btnSaveProfile.setText("Save Changes");
            Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFirebaseProfile(String name, String bio, Uri photoUri) {
        // Update Firebase Auth Profile
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photoUri)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update Realtime Database
                Map<String, Object> updates = new HashMap<>();
                updates.put("username", name);
                updates.put("bio", bio);

                if (userRef != null) {
                    userRef.updateChildren(updates).addOnCompleteListener(dbTask -> {
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText("Save Changes");
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                            // Go back to the main Profile screen
                            Navigation.findNavController(requireView()).popBackStack();
                        }
                    });
                }
            }
        });
    }
}