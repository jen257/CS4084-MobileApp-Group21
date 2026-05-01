package com.example.reloop.utils;

import com.example.reloop.models.Product;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FireBaseHelper {

    private final DatabaseReference productRef;

    public FireBaseHelper() {
        // Initialize reference to the 'products' node in Firebase
        productRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_PRODUCTS);
    }

    // 1. Fetch All Products (Used by Member C for Home Feed)
    public ValueEventListener getAllProducts(FirebaseCallback callback) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Product> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        list.add(product);
                    }
                }
                callback.onSuccess(list);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };
        // Attach the listener to the database reference
        productRef.addValueEventListener(listener);
        return listener; // Return the listener so ViewModels can remove it on clear
    }

    public void removeListener(ValueEventListener listener) {
        if (listener != null) {
            productRef.removeEventListener(listener);
        }
    }

    // 2. Add New Product (Used for Creating Listings)
    public void addProduct(Product product) {
        String id = productRef.push().getKey();
        if (id != null) {
            product.setPid(id); // Set the auto-generated unique ID
            productRef.child(id).setValue(product);
        }
    }
    // 3. Delete Product
    public void deleteProduct(String pid) {
        productRef.child(pid).removeValue();
    }

    // 4. Fetch Products by Seller (Used for My Listings/Profile)
    public void getProductsByUser(String sellerId, FirebaseCallback callback) {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Product> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product p = data.getValue(Product.class);
                    // Check if the product belongs to the requested seller
                    if (p != null && sellerId.equals(p.getSellerId())) {
                        list.add(p);
                    }
                }
                callback.onSuccess(list);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // 5. Update Product Status (Mark as Sold)
    public void markAsSold(String pid) {
        productRef.child(pid).child("isSold").setValue(true);
    }

    // FirebaseCallback Interface
    public interface FirebaseCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }
}