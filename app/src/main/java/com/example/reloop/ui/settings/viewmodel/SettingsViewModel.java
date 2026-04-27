package com.example.reloop.ui.settings.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.reloop.repository.AuthRepository;
import com.example.reloop.shared.BaseViewModel;

/**
 * ViewModel for SettingsFragment.
 * Manages user preferences and handles the logout process.
 */
public class SettingsViewModel extends AndroidViewModel {

    private final SharedPreferences preferences;
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> areNotificationsEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        // Initialize SharedPreferences for persistence
        preferences = application.getSharedPreferences("reloop_settings", Context.MODE_PRIVATE);
        // Initialize AuthRepository for logout functionality
        authRepository = new AuthRepository();
        loadPreferences();
    }

    public LiveData<Boolean> getNotificationStatus() {
        return areNotificationsEnabled;
    }

    public LiveData<Boolean> getNavigateToLogin() {
        return navigateToLogin;
    }

    private void loadPreferences() {
        areNotificationsEnabled.setValue(preferences.getBoolean("notifications", true));
    }

    public void setNotifications(boolean isEnabled) {
        preferences.edit().putBoolean("notifications", isEnabled).apply();
        areNotificationsEnabled.setValue(isEnabled);
    }

    /**
     * Perform user logout through AuthRepository
     */
    public void logout() {
        authRepository.logoutUser();
        navigateToLogin.setValue(true);
    }
}