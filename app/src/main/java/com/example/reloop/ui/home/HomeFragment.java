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

    private final List<Product> productList = new ArrayList<>();
    private ProductAdapter adapter;
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
        RecyclerView recyclerView = root.findViewById(R.id.recycler_home_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProductAdapter(getContext(), productList,
                product -> Toast.makeText(getContext(), "Added to wishlist: " + product.getTitle(), Toast.LENGTH_SHORT).show());

        recyclerView.setAdapter(adapter);
    }

    /**
     * Observe LiveData from ViewModel
     */
    private void observeProducts() {
        // Observe product list
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                updateProductList(products);
            }
        });

        // Observe error messages
        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe loading state
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator
            if (isLoading != null && isLoading && getView() != null) {
                View progressBar = getView().findViewById(R.id.progressBar);
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            } else if (getView() != null) {
                View progressBar = getView().findViewById(R.id.progressBar);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // Observe success messages
        viewModel.successMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Efficiently update product list with proper RecyclerView notifications
     */
    private void updateProductList(List<Product> newProducts) {
        int oldSize = productList.size();
        int newSize = newProducts.size();

        productList.clear();
        productList.addAll(newProducts);

        if (oldSize == 0) {
            // First load - insert all items
            adapter.notifyItemRangeInserted(0, newSize);
        } else if (oldSize == newSize) {
            // Same size - refresh all items
            adapter.notifyItemRangeChanged(0, newSize);
        } else if (oldSize > newSize) {
            // Items removed - remove excess and update remaining
            adapter.notifyItemRangeRemoved(newSize, oldSize - newSize);
            adapter.notifyItemRangeChanged(0, newSize);
        } else {
            // Items added - update existing and insert new
            adapter.notifyItemRangeChanged(0, oldSize);
            adapter.notifyItemRangeInserted(oldSize, newSize - oldSize);
        }
    }
}