package com.example.reloop.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Handles all direct network requests related to Firebase Authentication.
 * Abstracts the authentication operations away from the ViewModel.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public AuthRepository() {
        // Initialize Firebase Auth instance
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Authenticate user with email and password
     */
    public Task<AuthResult> loginUser(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Register a new user with email and password
     */
    public Task<AuthResult> registerUser(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Sign out the current user
     */
    public void logoutUser() {
        firebaseAuth.signOut();
    }

    /**
     * Get the currently signed-in user
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}