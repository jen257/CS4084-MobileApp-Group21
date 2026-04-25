package com.example.reloop.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.reloop.R;
import com.example.reloop.shared.views.LoadingButton;
import com.example.reloop.ui.auth.viewmodel.AuthViewModel;

/**
 * Fragment responsible for user registration interface.
 */
public class RegisterFragment extends Fragment {

    private AuthViewModel authViewModel;
    private EditText etEmail, etPassword, etConfirmPassword;
    private LoadingButton btnRegister;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Bind UI components
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        TextView tvGoToLogin = view.findViewById(R.id.tvGoToLogin);

        btnRegister.setText("Register");

        setupObservers(view);

        btnRegister.setOnButtonClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Delegate to ViewModel for registration
            authViewModel.register(email, password);
        });

        // Navigate back to LoginFragment
        tvGoToLogin.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );
    }

    private void setupObservers(View view) {
        // Fixed Statement lambda can be replaced with expression lambda
        authViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading ->
                btnRegister.setLoading(isLoading)
        );

        authViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getAuthState().observe(getViewLifecycleOwner(), isAuthenticated -> {
            if (isAuthenticated != null && isAuthenticated) {
                Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_homeFragment);
            }
        });
    }
}