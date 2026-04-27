package com.example.reloop.ui.map.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.reloop.models.Product;
import com.example.reloop.repository.MapRepository;
import com.example.reloop.repository.ProductRepository;

import java.util.List;

public class MapViewModel extends ViewModel {

    private final MapRepository mapRepository;
    private final ProductRepository productRepository;

    private final MutableLiveData<List<Product>> productsWithLocations = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public MapViewModel(MapRepository mapRepo, ProductRepository productRepo) {
        this.mapRepository = mapRepo;
        this.productRepository = productRepo;
    }

    public void loadProductsWithLocations() {
        isLoading.setValue(true);

        productRepository.getAllProducts(new ProductRepository.DataCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                // Filter products that have location
                List<Product> productsWithLocation = filterProductsWithLocation(products);
                productsWithLocations.setValue(productsWithLocation);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue("Failed to load products: " + errorMessage);
                isLoading.setValue(false);
            }
        });
    }

    public void loadProductsNearby(double latitude, double longitude, double radiusKm) {
        isLoading.setValue(true);

        mapRepository.loadProductsInBounds(
                latitude - 0.1, latitude + 0.1,
                longitude - 0.1, longitude + 0.1,
                new MapRepository.MapDataCallback() {
                    @Override
                    public void onProductsLoaded(List<Product> products) {
                        productsWithLocations.setValue(products);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        error.setValue("Failed to load nearby products: " + errorMessage);
                        isLoading.setValue(false);
                    }
                }
        );
    }

    private List<Product> filterProductsWithLocation(List<Product> products) {
        return products.stream()
                .filter(product -> product.getLocation() != null)
                .collect(java.util.stream.Collectors.toList());
    }

    public LiveData<List<Product>> getProductsWithLocations() {
        return productsWithLocations;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}