package com.example.reloop.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.reloop.R;
import com.example.reloop.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends Fragment {

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Elements
    private EditText etUsername, etEmail, etPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Bind UI components
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        TextView tvGoToLogin = view.findViewById(R.id.tvGoToLogin);

        // Set listeners
        btnRegister.setOnClickListener(v -> registerUser(view));

        tvGoToLogin.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_loginFragment)
        );
    }

    private void registerUser(View view) {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Validate input fields
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Create user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fUser = mAuth.getCurrentUser();
                        if (fUser != null) {
                            // 3. Save additional user info to Realtime Database
                            User newUser = new User(fUser.getUid(), username, email);
                            mDatabase.child(fUser.getUid()).setValue(newUser)
                                    .addOnCompleteListener(dbTask -> {
                                        if(dbTask.isSuccessful()) {
                                            // 4. Navigate to Home on success
                                            Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_homeFragment);
                                        }
                                    });
                        }
                    } else {
                        // Show error message if Auth fails
                        Toast.makeText(getContext(), String.format(getString(R.string.error_auth_failed), task.getException().getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
    }
}