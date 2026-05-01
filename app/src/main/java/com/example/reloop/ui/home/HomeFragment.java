package com.example.reloop.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.example.reloop.ui.home.adapters.CategoryAdapter;
import com.example.reloop.ui.home.adapters.ProductAdapter;
import com.example.reloop.ui.home.dialogs.FilterDialog;
import com.example.reloop.ui.home.viewmodel.HomeViewModel;
import com.example.reloop.utils.LocationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> displayList = new ArrayList<>();

    private String currentKeyword = "";
    private String currentCategory = "All";
    private Double currentMinPrice = null;
    private Double currentMaxPrice = null;
    private int currentMaxDistance = 50;

    private double userLat = 0.0;
    private double userLng = 0.0;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if ((fineLocationGranted != null && fineLocationGranted) || (coarseLocationGranted != null && coarseLocationGranted)) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location denied. Products will be shown without distance filtering.", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupProductRecycler(root);
        setupCategoryRecycler(root);
        setupSearchView(root);
        setupFilterButton(root);

        // Start loading data immediately so the screen is not blank while waiting for permissions
        observeViewModel();
        viewModel.loadProducts();

        checkLocationPermission();

        return root;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();
                applyFilters();
            }
        });
    }

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

        productAdapter = new ProductAdapter(getContext(), displayList,
                product -> { /* Wishlist logic handled inside adapter */ },
                product -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("product", product);
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_productDetailFragment, bundle);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Navigation failed", Toast.LENGTH_SHORT).show();
                    }
                }
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

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            View pb = getView() != null ? getView().findViewById(R.id.progressBar) : null;
            if (pb != null) pb.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void applyFilters() {
        displayList.clear();

        for (Product product : allProducts) {
            if (!currentCategory.equals("All") && product.getCategory() != null) {
                if (!product.getCategory().equalsIgnoreCase(currentCategory)) continue;
            }

            if (!currentKeyword.isEmpty()) {
                if (product.getTitle() == null || !product.getTitle().toLowerCase().contains(currentKeyword.toLowerCase())) continue;
            }

            if (currentMinPrice != null || currentMaxPrice != null) {
                try {
                    double pPrice = product.getPriceAsDouble();
                    if (currentMinPrice != null && pPrice < currentMinPrice) continue;
                    if (currentMaxPrice != null && pPrice > currentMaxPrice) continue;
                } catch (Exception e) { continue; }
            }

            // Only filter by distance if location data is available
            if (userLat != 0.0 && userLng != 0.0 && product.getLatitude() != 0.0 && product.getLongitude() != 0.0) {
                double distance = LocationUtils.calculateDistance(userLat, userLng, product.getLatitude(), product.getLongitude());
                if (distance > currentMaxDistance) continue;
            }

            displayList.add(product);
        }

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
    }
}