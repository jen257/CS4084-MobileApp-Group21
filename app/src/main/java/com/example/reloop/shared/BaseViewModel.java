package com.example.reloop.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Base ViewModel class that provides common state management
 * Handles loading states, error messages, and success messages
 */
public abstract class BaseViewModel extends ViewModel {

    // Loading state management
    protected final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    // Error message management
    protected final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    // Success message management
    protected final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public LiveData<String> successMessage = _successMessage;

    /**
     * Set loading state
     */
    protected void setLoading(boolean loading) {
        _isLoading.postValue(loading);
    }

    /**
     * Set error message
     */
    protected void setError(String message) {
        _errorMessage.postValue(message);
    }

    /**
     * Set success message
     */
    protected void setSuccess(String message) {
        _successMessage.postValue(message);
    }

    /**
     * Clear all messages
     */
    protected void clearMessages() {
        _errorMessage.postValue(null);
        _successMessage.postValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        clearMessages();
    }
}