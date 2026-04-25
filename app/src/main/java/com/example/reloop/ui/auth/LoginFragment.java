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
 * Fragment responsible for user login interface.
 * Strictly adheres to MVVM: delegates logic to AuthViewModel.
 */
public class LoginFragment extends Fragment {

    private AuthViewModel authViewModel;
    private EditText etEmail, etPassword;
    private LoadingButton btnLogin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel (Lifecycle aware)
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Bind UI components
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        TextView tvGoToRegister = view.findViewById(R.id.tvGoToRegister);

        // Set initial text for the custom LoadingButton
        btnLogin.setText(getString(R.string.action_sign_in));

        // Set up LiveData observers to listen for ViewModel updates
        setupObservers(view);

        // Handle login button click using custom listener
        btnLogin.setOnButtonClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate input
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            // Delegate authentication process to ViewModel
            authViewModel.login(email, password);
        });

        // Navigate to Registration screen
        tvGoToRegister.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment)
        );
    }

    /**
     * Observe state changes from ViewModel and update UI accordingly
     */
    private void setupObservers(View view) {
        // Handle loading state: automatically toggles progress bar
        authViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            btnLogin.setLoading(isLoading);
        });

        // Handle error messages
        authViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Handle successful authentication and navigation
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), isAuthenticated -> {
            if (isAuthenticated != null && isAuthenticated) {
                // Navigate to Home Fragment
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment);
            }
        });
    }
}