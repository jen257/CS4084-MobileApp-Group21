package com.example.reloop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.example.reloop.ui.home.adapters.ProductAdapter;
import com.example.reloop.ui.home.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment displays the main product feed using MVVM architecture.
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;

    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate layout
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup RecyclerView
        setupRecyclerView(root);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observe LiveData
        observeProducts();

        // Load data from Firebase
        viewModel.loadProducts();

        return root;
    }

    /**
     * Setup RecyclerView and adapter
     */
    private void setupRecyclerView(View root) {
        recyclerView = root.findViewById(R.id.recycler_home_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        productList = new ArrayList<>();

        adapter = new ProductAdapter(getContext(), productList, product -> {
            Toast.makeText(getContext(),
                    "Added to wishlist: " + product.getTitle(),
                    Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(adapter);
    }

    /**
     * Observe LiveData from ViewModel
     */
    private void observeProducts() {

        // Observe product list
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productList.clear();
                productList.addAll(products);
                adapter.notifyDataSetChanged();
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(),
                        "Failed to load products: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}