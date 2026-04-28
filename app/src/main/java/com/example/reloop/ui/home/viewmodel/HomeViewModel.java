package com.example.reloop.ui.home.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.reloop.models.Product;
import com.example.reloop.utils.FireBaseHelper;
import com.example.reloop.shared.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing product data and filtering logic
 */
public class HomeViewModel extends BaseViewModel {

    private final MutableLiveData<List<Product>> productList = new MutableLiveData<>();
    private final FireBaseHelper firebaseHelper;

    // Store original unfiltered data
    private List<Product> originalList;

    // Current selected category
    private String currentCategory = "All";

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
                    originalList = products; // Save original list
                    productList.setValue(products);
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                setError("Failed to load products: " + error);
            }
        });
    }

    /**
     * Return LiveData of product list
     */
    public LiveData<List<Product>> getProducts() {
        return productList;
    }

    /**
     * Filter products by selected category
     */
    public void filterByCategory(String category) {
        currentCategory = category;
        applyFilter();
    }

    /**
     * Apply category filtering logic
     */
    private void applyFilter() {
        if (originalList == null) return;

        List<Product> filtered = new ArrayList<>();

        if (currentCategory.equals("All")) {
            filtered.addAll(originalList);
        } else {
            for (Product p : originalList) {
                if (p.getCategory() != null &&
                        p.getCategory().equalsIgnoreCase(currentCategory)) {
                    filtered.add(p);
                }
            }
        }

        productList.setValue(filtered);
    }
}