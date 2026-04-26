package com.example.reloop.ui.post.viewmodel;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.reloop.models.Product;
import com.example.reloop.repository.ProductRepository;
import com.example.reloop.utils.ImageLoader;

/**
 * ViewModel for handling product posting logic.
 */
public class PostViewModel extends ViewModel {

    private final ProductRepository repository;
    private final ImageLoader imageLoader;

    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<String> successMessage = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PostViewModel() {
        repository = new ProductRepository();
        imageLoader = new ImageLoader();
    }

    /**
     * Main function to handle product posting.
     */
    public void postProduct(String title,
                            String category,
                            String description,
                            double price,
                            Uri imageUri,
                            String sellerId) {

        // Basic validation
        if (title.isEmpty() || category.isEmpty() || description.isEmpty()) {
            errorMessage.setValue("Please fill all fields");
            return;
        }

        if (imageUri == null) {
            errorMessage.setValue("Please select an image");
            return;
        }

        isLoading.setValue(true);

        // Upload image to Firebase Storage
        imageLoader.uploadImage(imageUri, new ImageLoader.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {

                // Create product object
                Product product = new Product();
                product.setTitle(title);
                product.setCategory(category);
                product.setDescription(description);
                product.setPrice(price);
                product.setImageUrl(imageUrl);
                product.setSellerId(sellerId);
                product.setSold(false);

                // Save to Firebase Realtime DB
                repository.addProduct(product, new ProductRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        isLoading.postValue(false);
                        successMessage.postValue("Product posted successfully!");
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.postValue(false);
                        errorMessage.postValue(errorMsg);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Image upload failed: " + error);
            }
        });
    }
}