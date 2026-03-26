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
import com.example.reloop.utils.FireBaseHelper; // Ensure the case matches your file (FirebaseHelper)
import com.example.reloop.ui.home.adapters.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * [Member C - UI/Fragment Implementation]
 * HomeFragment displays the main product feed for the Reloop Marketplace.
 * Integrated with Member A's FirebaseHelper to fetch real-time data.
 */
public class HomeFragment extends Fragment {

    // --- UI Components ---
    private RecyclerView recyclerView;
    private ProductAdapter adapter;

    // --- Data Storage ---
    private List<Product> productList;

    // --- Utilities ---
    private FireBaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1. Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. Initialize UI Components
        setupRecyclerView(root);

        // 3. Initialize Firebase Utility (Member A's Architecture)
        firebaseHelper = new FireBaseHelper();

        // 4. Fetch data from the cloud
        loadProductsFromFirebase();

        return root;
    }

    /**
     * Standard setup for the RecyclerView and its Adapter.
     */
    private void setupRecyclerView(View root) {
        recyclerView = root.findViewById(R.id.recycler_home_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize list and bind it to the adapter
        productList = new ArrayList<>();
        adapter = new ProductAdapter(getContext(), productList);
        recyclerView.setAdapter(adapter);
    }

    /**
     * [Data Integration]
     * Calls the global FirebaseHelper to retrieve the latest product listings.
     */
    private void loadProductsFromFirebase() {

        // Using the standardized callback interface defined in FirebaseHelper
        firebaseHelper.getAllProducts(new FireBaseHelper.FirebaseCallback() {

            @Override
            public void onSuccess(List<Product> products) {
                if (products != null) {
                    Log.d("HomeFragment", "Successfully loaded " + products.size() + " items.");

                    // Clear existing data to avoid duplicates
                    productList.clear();

                    // Add new data and refresh the UI thread
                    productList.addAll(products);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                // Log error for debugging (Member A)
                Log.e("HomeFragment", "Database Error: " + error);

                // Show user-friendly toast message
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to sync with cloud: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}