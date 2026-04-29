package com.example.reloop.utils;

import com.example.reloop.models.Product;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

/**
 * [Member A - System Architect]
 * Global Firebase Helper class to manage Realtime Database operations.
 * Acts as the unified data access layer for the entire team to prevent synchronization conflicts.
 */
public class FireBaseHelper {

    private final DatabaseReference productRef;

    public FireBaseHelper() {
        // Initialize reference to the 'products' node in Firebase
        productRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_PRODUCTS);
    }

    // ============================================================
    // 1. Fetch All Products (Used by Member C for Home Feed)
    // ============================================================
    /**
     * [Day 5 Fix] Listens for real-time updates in the products node.
     * @param callback Interface to handle the asynchronous list of products.
     * @return ValueEventListener instance, allowing the UI to detach it later to prevent memory leaks.
     */
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
        return listener; // CRITICAL: Return the listener so ViewModels can remove it on clear
    }

    /**
     * [Day 5 Fix] Detaches a previously attached database listener.
     * @param listener The ValueEventListener to be removed.
     */
    public void removeListener(ValueEventListener listener) {
        if (listener != null) {
            productRef.removeEventListener(listener);
        }
    }

    // ============================================================
    // 2. Add New Product (Used for Creating Listings)
    // ============================================================
    /**
     * Generates a unique push ID for a new product and saves it to the database.
     * @param product The Product object to be pushed to the cloud.
     */
    public void addProduct(Product product) {
        String id = productRef.push().getKey();
        if (id != null) {
            product.setPid(id); // Set the auto-generated unique ID
            productRef.child(id).setValue(product);
        }
    }

    // ============================================================
    // 3. Delete Product
    // ============================================================
    /**
     * Removes a product from the database using its unique PID.
     * @param pid The unique Product ID.
     */
    public void deleteProduct(String pid) {
        productRef.child(pid).removeValue();
    }

    // ============================================================
    // 4. Fetch Products by Seller (Used for My Listings/Profile)
    // ============================================================
    /**
     * Filters products based on the seller's unique Firebase UID.
     * @param sellerId The UID of the seller to filter by.
     * @param callback Interface to handle the fetched list.
     */
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

    // ============================================================
    // 5. Update Product Status (Mark as Sold)
    // ============================================================
    /**
     * Updates the 'isSold' boolean field in Firebase.
     * @param pid The unique Product ID to mark as sold.
     */
    public void markAsSold(String pid) {
        productRef.child(pid).child("isSold").setValue(true);
    }

    // ============================================================
    // FirebaseCallback Interface
    // ============================================================
    /**
     * Custom interface to handle asynchronous results from Firebase operations.
     */
    public interface FirebaseCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }
}