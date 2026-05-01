package com.example.reloop.ui.home.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.reloop.models.Product;
import com.example.reloop.repository.ProductRepository;
import com.example.reloop.shared.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends BaseViewModel {

    private final MutableLiveData<List<Product>> productList = new MutableLiveData<>();
    private final ProductRepository productRepository;

    private List<Product> originalList;
    private String currentCategory = "All";

    public HomeViewModel() {
        productRepository = new ProductRepository();
    }

    public void loadProducts() {
        setLoading(true);
        clearMessages();

        // Use the proper repository method that sets the PID
        productRepository.getAllProducts(new ProductRepository.DataCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                setLoading(false);
                if (products != null) {
                    List<Product> activeProducts = new ArrayList<>();

                    // Filter out Sold or Removed products from the source
                    for (Product p : products) {
                        if (!p.isSold() && !p.isRemoved()) {
                            activeProducts.add(p);
                        }
                    }

                    Log.d("HomeViewModel", "Loaded " + activeProducts.size() + " active products");
                    originalList = activeProducts;
                    productList.setValue(activeProducts);
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                setError("Failed to load products: " + error);
            }
        });
    }

    public LiveData<List<Product>> getProducts() {
        return productList;
    }

    public void filterByCategory(String category) {
        currentCategory = category;
        applyFilter();
    }

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