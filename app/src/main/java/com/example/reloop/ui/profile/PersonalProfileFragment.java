package com.example.reloop.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.example.reloop.models.AddressModel;
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

    private TextInputEditText etUsername, etEmail, etBio, etAddress;
    private MaterialButton btnSave;
    private ImageView ivAvatar;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private String uid;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivAvatar.setImageURI(uri);
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

        etUsername = view.findViewById(R.id.et_profile_username);
        etEmail = view.findViewById(R.id.et_profile_email);
        etBio = view.findViewById(R.id.et_profile_bio);
        etAddress = view.findViewById(R.id.et_profile_address);
        btnSave = view.findViewById(R.id.btn_save_profile);
        ivAvatar = view.findViewById(R.id.iv_profile_avatar);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            loadUserProfile();
        }

        View layoutChangeAvatar = view.findViewById(R.id.layout_change_avatar);
        layoutChangeAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        etAddress.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_personalProfileFragment_to_addressFragment)
        );

        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            etEmail.setText(currentUser.getEmail());
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(ivAvatar);
            }
        }

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                etUsername.setText(snapshot.child("username").getValue(String.class));
                etBio.setText(snapshot.child("bio").getValue(String.class));

                String defaultAddrKey = snapshot.child("defaultAddressKey").getValue(String.class);
                if (defaultAddrKey != null) {
                    loadAddressDetails(defaultAddrKey);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadAddressDetails(String key) {
        userRef.child("addresses").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AddressModel address = snapshot.getValue(AddressModel.class);
                if (address != null && isAdded()) {
                    etAddress.setText(address.getFullAddress());
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveProfileChanges() {
        String name = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (name.isEmpty()) {
            etUsername.setError("Username required");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Updating...");

        if (selectedImageUri != null) {
            uploadImageAndSave(name, bio);
        } else {
            updateFirebaseData(name, bio, currentUser.getPhotoUrl());
        }
    }

    private void uploadImageAndSave(String name, String bio) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_pics/" + uid + ".jpg");

        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                        updateFirebaseData(name, bio, uri)
                )
        ).addOnFailureListener(e -> {
            btnSave.setEnabled(true);
            btnSave.setText("Save Changes");
            Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFirebaseData(String name, String bio, Uri photoUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photoUri)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("username", name);
            updates.put("bio", bio);

            userRef.updateChildren(updates).addOnCompleteListener(dbTask -> {
                btnSave.setEnabled(true);
                btnSave.setText("Save Changes");
                if (dbTask.isSuccessful()) {
                    Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}