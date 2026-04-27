package com.example.reloop.repository;

import androidx.annotation.NonNull;

import com.example.reloop.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final DatabaseReference productsRef;

    public ProductRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");
    }

    public void getAllProducts(DataCallback callback) {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Product> products = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        product.setPid(snapshot.getKey());
                        products.add(product);
                    }
                }

                callback.onSuccess(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void getProductsByCategory(String category, DataCallback callback) {
        productsRef.orderByChild("category").equalTo(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Product> products = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Product product = snapshot.getValue(Product.class);
                            if (product != null) {
                                product.setPid(snapshot.getKey());
                                products.add(product);
                            }
                        }

                        callback.onSuccess(products);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    public void addProduct(Product product, OperationCallback callback) {
        String key = productsRef.push().getKey();
        if (key != null) {
            productsRef.child(key).setValue(product)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onError("Failed to generate product key");
        }
    }

    public interface DataCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }
}