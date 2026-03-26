package com.example.reloop.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.example.reloop.ui.home.adapters.ProductAdapter;
import com.example.reloop.utils.FireBaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment displays the main product feed for Reloop Marketplace.
 * It integrates Firebase for real-time data and ProductAdapter for RecyclerView.
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FireBaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1. Inflate layout
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. Initialize RecyclerView
        setupRecyclerView(root);

        // 3. Initialize Firebase Helper
        firebaseHelper = new FireBaseHelper();

        // 4. Load products from Firebase
        loadProductsFromFirebase();

        return root;
    }

    /**
     * Setup RecyclerView and ProductAdapter with click listener
     */
    private void setupRecyclerView(View root) {
        recyclerView = root.findViewById(R.id.recycler_home_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize empty list
        productList = new ArrayList<>();

        // Initialize ProductAdapter with click callback
        adapter = new ProductAdapter(getContext(), productList, product -> {
            // Callback when "Want" button is clicked
            Toast.makeText(getContext(), "Added to wishlist: " + product.getTitle(), Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(adapter);
    }

    /**
     * Load products from Firebase using FireBaseHelper
     */
    private void loadProductsFromFirebase() {
        firebaseHelper.getAllProducts(new FireBaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products != null) {
                    Log.d("HomeFragment", "Loaded " + products.size() + " products from Firebase.");

                    // Clear previous data
                    productList.clear();

                    // Add new products
                    productList.addAll(products);

                    // Notify adapter to refresh UI
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e("HomeFragment", "Firebase Error: " + error);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load products: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}