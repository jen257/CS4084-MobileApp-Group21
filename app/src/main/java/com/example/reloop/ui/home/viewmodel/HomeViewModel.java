package com.example.reloop.ui.home.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.reloop.models.Product;
import com.example.reloop.utils.FireBaseHelper;
import com.example.reloop.shared.BaseViewModel;

import java.util.List;

/**
 * HomeViewModel handles data logic for HomeFragment.
 * It communicates with Firebase and exposes data via LiveData.
 */
public class HomeViewModel extends BaseViewModel {

    // LiveData for product list
    private final MutableLiveData<List<Product>> productList = new MutableLiveData<>();

    // Firebase helper
    private final FireBaseHelper firebaseHelper;

    public HomeViewModel() {
        firebaseHelper = new FireBaseHelper();
    }

    /**
     * Load products from Firebase
     */
    public void loadProducts() {
        setLoading(true);
        clearMessages();

        firebaseHelper.getAllProducts(new FireBaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                setLoading(false);
                if (products != null) {
                    Log.d("HomeViewModel", "Loaded " + products.size() + " products");
                    productList.setValue(products);
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Log.e("HomeViewModel", "Error: " + error);
                setError("Failed to load products: " + error);
            }
        });
    }

    /**
     * Get product list LiveData
     */
    public LiveData<List<Product>> getProducts() {
        return productList;
    }
}