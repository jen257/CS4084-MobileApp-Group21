package com.example.reloop.ui.profile.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.reloop.models.Product;
import com.example.reloop.repository.ProductRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class UserProductsViewModel extends ViewModel {

    private final ProductRepository repository;
    public MutableLiveData<List<Product>> userProducts = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserProductsViewModel() {
        repository = new ProductRepository();
    }

    public void fetchUserProducts(boolean requestSoldItems) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        isLoading.setValue(true);

        repository.getAllProducts(new ProductRepository.DataCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                List<Product> filteredList = new ArrayList<>();
                for (Product p : products) {
                    if (currentUserId.equals(p.getSellerId())) {
                        boolean isArchived = p.isSold() || p.isRemoved();
                        if (requestSoldItems && isArchived) {
                            filteredList.add(p);
                        }
                        else if (!requestSoldItems && !isArchived) {
                            filteredList.add(p);
                        }
                    }
                }
                userProducts.postValue(filteredList);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    public void updateProductStatus(Product product, boolean markAsSold, boolean markAsRemoved, boolean requestSoldItems) {
        isLoading.setValue(true);
        product.setSold(markAsSold);
        product.setRemoved(markAsRemoved);

        repository.updateProduct(product, new ProductRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                fetchUserProducts(requestSoldItems);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }
}