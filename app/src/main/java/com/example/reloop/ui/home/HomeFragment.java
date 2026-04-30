package com.example.reloop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.example.reloop.ui.home.adapters.CategoryAdapter;
import com.example.reloop.ui.home.adapters.ProductAdapter;
import com.example.reloop.ui.home.dialogs.FilterDialog;
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

    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> displayList = new ArrayList<>();

    // Record current filter states
    private String currentKeyword = "";
    private String currentCategory = "All";
    private Double currentMinPrice = null;
    private Double currentMaxPrice = null;
    private int currentMaxDistance = 50; // Default max distance 50km

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
        setupSearchView(root);
        setupFilterButton(root);

        // Observe data & Load
        observeViewModel();
        viewModel.loadProducts();

        return root;
    }

    /**
     * Bind and setup the top search bar
     */
    private void setupSearchView(View root) {
        SearchView searchView = root.findViewById(R.id.homeSearchView);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentKeyword = query;
                    applyFilters();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentKeyword = newText;
                    applyFilters();
                    return true;
                }
            });
        }
    }

    /**
     * Bind advanced filter button (shows FilterDialog)
     */
    private void setupFilterButton(View root) {
        View btnFilter = root.findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                FilterDialog filterDialog = new FilterDialog();
                filterDialog.setFilterListener((minPrice, maxPrice, maxDistanceKm) -> {
                    currentMinPrice = minPrice;
                    currentMaxPrice = maxPrice;
                    currentMaxDistance = maxDistanceKm;
                    applyFilters();
                });
                filterDialog.show(getChildFragmentManager(), "FilterDialog");
            });
        }
    }

    private void setupProductRecycler(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.recycler_home_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new ProductAdapter(
                getContext(),
                displayList,
                product -> Toast.makeText(getContext(),
                        "Added: " + product.getTitle(),
                        Toast.LENGTH_SHORT).show()
        );

        recyclerView.setAdapter(productAdapter);
    }

    private void setupCategoryRecycler(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.recycler_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Electronics");
        categories.add("Clothing");
        categories.add("Books");
        categories.add("Others");

        categoryAdapter = new CategoryAdapter(categories, category -> {
            currentCategory = category;
            applyFilters();
        });

        recyclerView.setAdapter(categoryAdapter);
    }

    private void observeViewModel() {
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                allProducts.clear();
                allProducts.addAll(products);
                applyFilters();
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
                progressBar.setVisibility((isLoading != null && isLoading) ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Advanced Filtering Algorithm: Comprehensive check for Keyword + Category + Price + Distance
     */
    private void applyFilters() {
        displayList.clear();
        double userLat = 52.6738;
        double userLng = -8.5721;

        for (Product product : allProducts) {

            // 1. Category Filter
            if (!currentCategory.equals("All") && product.getCategory() != null) {
                if (!product.getCategory().equalsIgnoreCase(currentCategory)) {
                    continue; // Category mismatch, skip and hide
                }
            }

            // 2. Keyword Filter
            if (currentKeyword != null && !currentKeyword.trim().isEmpty()) {
                if (product.getTitle() == null || !product.getTitle().toLowerCase().contains(currentKeyword.toLowerCase())) {
                    continue; // Title does not contain keyword, skip
                }
            }

            // 3. Price Filter
            if (currentMinPrice != null || currentMaxPrice != null) {
                try {
                    // Assuming getPrice() returns a String like "19.99"
                    String priceString = product.getPrice();
                    if (priceString != null && !priceString.isEmpty()) {
                        // Remove any currency symbols if they exist (e.g., "€19.99" -> "19.99")
                        priceString = priceString.replaceAll("[^\\d.]", "");
                        double productPrice = Double.parseDouble(priceString);

                        if (currentMinPrice != null && productPrice < currentMinPrice) {
                            continue;
                        }
                        if (currentMaxPrice != null && productPrice > currentMaxPrice) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    continue; // If price can't be parsed, hide it
                }
            }

            // 4. Distance Filter
            if (product.getLatitude() != 0.0 && product.getLongitude() != 0.0) {
                double distance = com.example.reloop.utils.LocationUtils.calculateDistance(
                        userLat, userLng, product.getLatitude(), product.getLongitude()
                );
                if (distance > currentMaxDistance) {
                    continue; // Exceeds the selected kilometers on the SeekBar, skip
                }
            }

            // Product passed all conditions, add to display list!
            displayList.add(product);
        }

        // Notify UI to refresh
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
    }
}