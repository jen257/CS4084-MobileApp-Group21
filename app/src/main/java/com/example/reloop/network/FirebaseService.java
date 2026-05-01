package com.example.reloop.network;

import com.example.reloop.models.Product;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Handles all direct network requests related to the Firebase Realtime Database.
 * This class abstracts the database operations away from the rest of the app.
 */
public class FirebaseService {

    private static final String PRODUCTS_NODE = "products";
    private final DatabaseReference databaseReference;

    public FirebaseService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
    }

    public DatabaseReference getProductsRef() {
        return databaseReference.child(PRODUCTS_NODE);
    }

    public Task<Void> addProduct(Product product) {
        DatabaseReference newProductRef = getProductsRef().push();
        product.setPid(newProductRef.getKey());
        return newProductRef.setValue(product);
    }

    public Task<Void> updateProduct(Product product) {
        return getProductsRef().child(product.getPid()).setValue(product);
    }

    public Task<Void> deleteProduct(String productId) {
        return getProductsRef().child(productId).removeValue();
    }

    public void listenToProducts(ValueEventListener listener) {
        getProductsRef().addValueEventListener(listener);
    }

    public void removeProductsListener(ValueEventListener listener) {
        getProductsRef().removeEventListener(listener);
    }
}