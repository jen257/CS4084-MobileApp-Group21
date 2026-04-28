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
import com.example.reloop.ui.home.adapters.CategoryAdapter;
import com.example.reloop.ui.home.adapters.ProductAdapter;
import com.example.reloop.ui.home.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment displays product list and category filter UI.
 */
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    private final List<Product> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Setup UI
        setupProductRecycler(root);
        setupCategoryRecycler(root);

        // Observe data
        observeViewModel();

        // Load data
        viewModel.loadProducts();

        return root;
    }

    /**
     * Setup product RecyclerView
     */
    private void setupProductRecycler(View root) {

        RecyclerView recyclerView = root.findViewById(R.id.recycler_home_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        productAdapter = new ProductAdapter(
                getContext(),
                productList,
                product -> Toast.makeText(getContext(),
                        "Added: " + product.getTitle(),
                        Toast.LENGTH_SHORT).show()
        );

        recyclerView.setAdapter(productAdapter);
    }

    /**
     * Setup category RecyclerView
     */
    private void setupCategoryRecycler(View root) {

        RecyclerView recyclerView = root.findViewById(R.id.recycler_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Electronics");
        categories.add("Clothing");
        categories.add("Books");
        categories.add("Others");

        categoryAdapter = new CategoryAdapter(categories, category -> {

            // Send category selection to ViewModel
            viewModel.filterByCategory(category);
        });

        recyclerView.setAdapter(categoryAdapter);
    }

    /**
     * Observe LiveData from ViewModel
     */
    private void observeViewModel() {

        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {

            if (products != null) {

                productList.clear();
                productList.addAll(products);

                productAdapter.notifyDataSetChanged();
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {

            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {

            if (getView() == null) return;

            View progressBar = getView().findViewById(R.id.progressBar);

            if (progressBar != null) {
                progressBar.setVisibility(
                        (isLoading != null && isLoading) ? View.VISIBLE : View.GONE
                );
            }
        });
    }
}