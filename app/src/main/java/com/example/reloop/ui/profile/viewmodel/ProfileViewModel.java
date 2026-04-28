package com.example.reloop.ui.profile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.reloop.repository.AuthRepository;
import com.example.reloop.shared.BaseViewModel;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel for ProfileFragment to load user data.
 */
public class ProfileViewModel extends BaseViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();

    public ProfileViewModel() {
        this.authRepository = new AuthRepository();
        loadUserProfile();
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public void loadUserProfile() {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            currentUser.setValue(user);
        }
    }
}