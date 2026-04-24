package com.example.reloop.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.reloop.models.Product;
import com.example.reloop.network.FirebaseService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * [Member A - System Architect]
 * Repository Pattern: Acts as the Single Source of Truth for product data.
 * It retrieves data from Firebase via FirebaseService and exposes it as
 * observable LiveData for the UI (ViewModel) to consume.
 */
public class ProductRepository {

    private static final String TAG = "ProductRepository";

    private final FirebaseService firebaseService;
    private final MutableLiveData<List<Product>> productsLiveData;
    private final MutableLiveData<String> errorLiveData;

    public ProductRepository() {
        firebaseService = new FirebaseService();
        productsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();

        // Start fetching data immediately upon initialization
        fetchProducts();
    }

    /**
     * Provides LiveData containing the list of products for observation.
     * Typically observed by HomeViewModel (Member C).
     * @return LiveData object holding a list of Product objects.
     */
    public LiveData<List<Product>> getProductsLiveData() {
        return productsLiveData;
    }

    /**
     * Provides LiveData containing error messages for observation.
     * @return LiveData object holding error strings.
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /**
     * Fetches all product listings from Firebase and updates the LiveData.
     * Uses a ValueEventListener to listen for real-time updates.
     */
    private void fetchProducts() {
        firebaseService.listenToProducts(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> productList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                // Use postValue to ensure UI updates happen on the main thread
                productsLiveData.postValue(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                errorLiveData.postValue(error.getMessage());
            }
        });
    }

    /**
     * Method provided for PostViewModel (Member C) to publish a new product.
     * @param product The product object to be published.
     * @param callback Custom callback interface to notify the UI of success or failure.
     */
    public void addProduct(Product product, final OperationCallback callback) {
        firebaseService.addProduct(product)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    /**
     * Basic callback interface for database operations.
     */
    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMsg);
    }
}