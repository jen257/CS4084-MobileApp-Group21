package com.example.reloop.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.reloop.repository.AuthRepository;
import com.example.reloop.shared.BaseViewModel;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel for handling authentication logic (Login & Register).
 * Inherits loading and error state management from BaseViewModel.
 */
public class AuthViewModel extends BaseViewModel {

    private final AuthRepository authRepository;

    // LiveData to notify the Fragment about successful authentication
    private final MutableLiveData<Boolean> authState = new MutableLiveData<>();

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
        checkCurrentUser();
    }

    public LiveData<Boolean> getAuthState() {
        return authState;
    }

    /**
     * Check if a user is already logged in upon initialization
     */
    private void checkCurrentUser() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            authState.postValue(true);
        }
    }

    /**
     * Handle user login
     */
    public void login(String email, String password) {
        setLoading(true); // Trigger LoadingButton state
        clearMessages();

        authRepository.loginUser(email, password)
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    setSuccess("Login successful!");
                    authState.postValue(true); // Trigger navigation
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    setError(e.getMessage()); // Trigger Toast message
                    authState.postValue(false);
                });
    }

    /**
     * Handle user registration
     */
    public void register(String email, String password) {
        setLoading(true);
        clearMessages();

        authRepository.registerUser(email, password)
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    setSuccess("Registration successful!");
                    authState.postValue(true);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    setError(e.getMessage());
                    authState.postValue(false);
                });
    }

    /**
     * Handle user logout
     */
    public void logout() {
        authRepository.logoutUser();
        authState.postValue(false);
    }
}