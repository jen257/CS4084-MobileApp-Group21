package com.example.reloop.ui.settings;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.reloop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SecurityFragment extends Fragment {

    private EditText etNewEmail, etNewPassword, etConfirmPassword;
    private Button btnUpdateEmail, btnUpdatePassword;
    private FirebaseUser user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_security, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();

        etNewEmail = view.findViewById(R.id.etNewEmailAddress);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnUpdateEmail = view.findViewById(R.id.btnUpdateEmail);
        btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword);

        btnUpdateEmail.setOnClickListener(v -> updateEmail());
        btnUpdatePassword.setOnClickListener(v -> updatePassword());
    }

    private void updateEmail() {
        String email = etNewEmail.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etNewEmail.setError("Valid email required");
            return;
        }
        if (user != null) {
            user.updateEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Email updated!", Toast.LENGTH_SHORT).show();
                    etNewEmail.setText("");
                } else {
                    Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void updatePassword() {
        String password = etNewPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (password.length() < 6) {
            etNewPassword.setError("Min 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords mismatch");
            return;
        }
        if (user != null) {
            user.updatePassword(password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_SHORT).show();
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");
                } else {
                    Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}