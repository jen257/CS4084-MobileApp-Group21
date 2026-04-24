package com.example.reloop.network;

import com.example.reloop.models.Product;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * [Member A - System Architect]
 * Handles all direct network requests related to the Firebase Realtime Database.
 * This class abstracts the database operations away from the rest of the app.
 */
public class FirebaseService {

    private static final String PRODUCTS_NODE = "products";
    private final DatabaseReference databaseReference;

    public FirebaseService() {
        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
    }

    /**
     * Gets the database reference pointing to the products node.
     * @return DatabaseReference for "products"
     */
    public DatabaseReference getProductsRef() {
        return databaseReference.child(PRODUCTS_NODE);
    }

    /**
     * Adds a new product to the database (Create).
     * Automatically generates a unique key (pid) and sets it in the product object.
     * @param product The product object to be added.
     * @return Task to allow the Repository layer to listen for success/failure callbacks.
     */
    public Task<Void> addProduct(Product product) {
        DatabaseReference newProductRef = getProductsRef().push();
        product.setPid(newProductRef.getKey()); // Set the generated unique pid
        return newProductRef.setValue(product);
    }

    /**
     * Updates an existing product in the database (Update).
     * @param product The product object with updated values.
     * @return Task for success/failure callbacks.
     */
    public Task<Void> updateProduct(Product product) {
        return getProductsRef().child(product.getPid()).setValue(product);
    }

    /**
     * Deletes a product from the database (Delete).
     * @param productId The unique ID of the product to delete.
     * @return Task for success/failure callbacks.
     */
    public Task<Void> deleteProduct(String productId) {
        return getProductsRef().child(productId).removeValue();
    }

    /**
     * Listens for real-time changes to the products data (Read).
     * Essential for the Home page (Member C) to display live updates.
     * @param listener The ValueEventListener to handle data changes.
     */
    public void listenToProducts(ValueEventListener listener) {
        getProductsRef().addValueEventListener(listener);
    }

    /**
     * Removes the data listener to prevent memory leaks when the component is destroyed.
     * @param listener The ValueEventListener to be removed.
     */
    public void removeProductsListener(ValueEventListener listener) {
        getProductsRef().removeEventListener(listener);
    }
}