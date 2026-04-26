package com.example.reloop.ui.profile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.reloop.repository.AuthRepository;
import com.example.reloop.shared.BaseViewModel;
import com.google.firebase.auth.FirebaseUser;

public class ProfileViewModel extends BaseViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>();

    public ProfileViewModel() {
        this.authRepository = new AuthRepository();
        loadUserProfile();
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getNavigateToLogin() {
        return navigateToLogin;
    }

    public void loadUserProfile() {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            currentUser.setValue(user);
        } else {
            navigateToLogin.setValue(true);
        }
    }

    public void logout() {
        setLoading(true);
        authRepository.logoutUser();
        setLoading(false);
        setSuccess("Logged out successfully");
        navigateToLogin.setValue(true);
    }
}