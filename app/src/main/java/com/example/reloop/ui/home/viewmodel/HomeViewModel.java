package com.example.reloop.ui.home.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.reloop.models.Product;
import com.example.reloop.utils.FireBaseHelper;

import java.util.List;

/**
 * HomeViewModel handles data logic for HomeFragment.
 * It communicates with Firebase and exposes data via LiveData.
 */
public class HomeViewModel extends ViewModel {

    // LiveData for product list
    private final MutableLiveData<List<Product>> productList = new MutableLiveData<>();

    // LiveData for error message
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Firebase helper
    private final FireBaseHelper firebaseHelper;

    public HomeViewModel() {
        firebaseHelper = new FireBaseHelper();
    }

    /**
     * Load products from Firebase
     */
    public void loadProducts() {
        firebaseHelper.getAllProducts(new FireBaseHelper.FirebaseCallback() {

            @Override
            public void onSuccess(List<Product> products) {
                if (products != null) {
                    Log.d("HomeViewModel", "Loaded " + products.size() + " products");
                    productList.setValue(products);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("HomeViewModel", "Error: " + error);
                errorMessage.setValue(error);
            }
        });
    }

    /**
     * Get product list LiveData
     */
    public LiveData<List<Product>> getProducts() {
        return productList;
    }

    /**
     * Get error message LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}