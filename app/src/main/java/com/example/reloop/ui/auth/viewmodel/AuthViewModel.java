package com.example.reloop.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.reloop.repository.AuthRepository;
import com.example.reloop.shared.BaseViewModel;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.example.reloop.utils.Constants;
import android.util.Log;

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
                    try {
                        if (authResult != null && authResult.getUser() != null) {
                            saveFCMTokenToDatabase(authResult.getUser().getUid());
                        }
                    } catch (Exception e) {
                        Log.e("AuthViewModel", "Error getting user ID for token", e);
                    }

                    authState.postValue(true);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    setError(e.getMessage());
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

                    try {
                        if (authResult != null && authResult.getUser() != null) {
                            saveFCMTokenToDatabase(authResult.getUser().getUid());
                        }
                    } catch (Exception e) {
                        Log.e("AuthViewModel", "Error getting user ID for token", e);
                    }

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

    private void saveFCMTokenToDatabase(String userId) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("AuthViewModel", "Fetching FCM registration token failed", task.getException());
                return;
            }


            String token = task.getResult();


            FirebaseDatabase.getInstance()
                    .getReference(Constants.NODE_USERS)
                    .child(userId)
                    .child("fcmToken")
                    .setValue(token)
                    .addOnSuccessListener(aVoid -> Log.d("AuthViewModel", "FCM Token saved successfully!"))
                    .addOnFailureListener(e -> Log.e("AuthViewModel", "Failed to save FCM token", e));
        });
    }
}