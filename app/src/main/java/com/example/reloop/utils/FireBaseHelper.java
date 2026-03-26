package com.example.reloop.utils;

import com.example.reloop.models.Product;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

/**
 * [Member A - System Architect]
 * Global Firebase Helper class to manage Realtime Database operations.
 * This class provides standardized methods for the whole team (Members B, C, and D)
 * to interact with the "products" node in the cloud.
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
     * Listens for real-time updates in the products node.
     * Triggers the callback whenever data changes on the server.
     */
    public void getAllProducts(FirebaseCallback callback) {
        productRef.addValueEventListener(new ValueEventListener() {
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
        });
    }

    // ============================================================
    // 2. Add New Product (Used for Creating Listings)
    // ============================================================
    /**
     * Generates a unique push ID for a new product and saves it to the database.
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
     */
    public void deleteProduct(String pid) {
        productRef.child(pid).removeValue();
    }

    // ============================================================
    // 4. Fetch Products by Seller (Used for My Listings/Profile)
    // ============================================================
    /**
     * Filters products based on the seller's unique Firebase UID.
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
     * Note: Field name matches the 'isSold' variable in Product.java.
     */
    public void markAsSold(String pid) {
        productRef.child(pid).child("isSold").setValue(true);
    }

    // ============================================================
    // FirebaseCallback Interface
    // ============================================================
    /**
     * Interface to handle asynchronous results from Firebase operations.
     */
    public interface FirebaseCallback {
        /**
         * Called when data is successfully retrieved.
         * @param products List of Product objects from the database.
         */
        void onSuccess(List<Product> products);

        /**
         * Called when a database error occurs.
         * @param error Error message string.
         */
        void onError(String error);
    }
}

