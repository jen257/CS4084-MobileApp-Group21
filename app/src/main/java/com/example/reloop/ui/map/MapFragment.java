package com.example.reloop.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.reloop.R;
import com.example.reloop.databinding.FragmentMapBinding;
import com.example.reloop.models.Product;
import com.example.reloop.network.MapService;
import com.example.reloop.shared.views.CustomMapMarkerView;
import com.example.reloop.ui.map.viewmodel.MapViewModel;
import com.example.reloop.utils.LocationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MapFragment for displaying products on a map with location-based features
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapFragment";
    private static final float DEFAULT_ZOOM_LEVEL = 12.0f;
    private static final float MAX_ZOOM_LEVEL = 18.0f;
    private static final float MIN_ZOOM_LEVEL = 5.0f;

    // Default location for Amsterdam
    private static final LatLng AMSTERDAM = new LatLng(52.3676, 4.9041);

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private MapService mapService;
    private CustomMapMarkerView customMapMarkerView;
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar progressBar;

    private final List<Marker> productMarkers = new ArrayList<>();
    private LatLng currentLocation;

    // Permission request launcher
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean allGranted = permissions.values().stream().allMatch(granted -> granted);
                        if (allGranted) {
                            Log.d(TAG, "Location permissions granted");
                            enableLocationFeatures();
                        } else {
                            Log.w(TAG, "Location permissions denied");
                            Toast.makeText(requireContext(),
                                    "Location permission is required for map features",
                                    Toast.LENGTH_LONG).show();
                            showDefaultLocation();
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        setupViewModel();
        setupMapFragment();
        setupClickListeners();
    }

    private void initializeComponents() {
        progressBar = binding.progressBar;

        // Initialize services
        mapService = MapService.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        customMapMarkerView = new CustomMapMarkerView(requireContext());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);

        // Observe products with locations
        viewModel.getProductsWithLocations().observe(getViewLifecycleOwner(), products -> {
            Log.d(TAG, "Received " + products.size() + " products with locations");
            if (googleMap != null) {
                addProductMarkers(products);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                Log.d(TAG, "Loading state: " + isLoading);
            }
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "ViewModel error: " + error);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d(TAG, "Map fragment setup successfully");
        } else {
            Log.e(TAG, "Failed to find map fragment");
            Toast.makeText(requireContext(), "Map initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        // My Location FAB
        binding.fabMyLocation.setOnClickListener(v -> {
            if (currentLocation != null) {
                animateToLocation(currentLocation);
            } else {
                getCurrentLocation();
            }
        });

        // Refresh products button
        binding.fabRefresh.setOnClickListener(v -> refreshProducts());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "GoogleMap is ready");
        this.googleMap = googleMap;

        configureMap();
        checkLocationPermissions();

        // Load products with locations
        viewModel.loadProductsWithLocations();
    }

    private void configureMap() {
        if (googleMap == null) {
            Log.e(TAG, "GoogleMap is null during configuration");
            return;
        }

        try {
            // Basic map configuration
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(true);

            // Set zoom limits
            googleMap.setMinZoomPreference(MIN_ZOOM_LEVEL);
            googleMap.setMaxZoomPreference(MAX_ZOOM_LEVEL);

            // Set listeners
            googleMap.setOnMarkerClickListener(this);
            googleMap.setInfoWindowAdapter(customMapMarkerView);

            // Set map type
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            Log.d(TAG, "Map configuration completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error configuring map", e);
        }
    }

    private void checkLocationPermissions() {
        String[] requiredPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean allGranted = true;
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            Log.d(TAG, "Location permissions already granted");
            enableLocationFeatures();
        } else {
            Log.d(TAG, "Requesting location permissions");
            requestLocationPermissions();
        }
    }

    private void requestLocationPermissions() {
        String[] requiredPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        // Check if we should show rationale
        boolean shouldShowRationale = false;
        for (String permission : requiredPermissions) {
            if (shouldShowRequestPermissionRationale(permission)) {
                shouldShowRationale = true;
                break;
            }
        }

        if (shouldShowRationale) {
            Toast.makeText(requireContext(),
                    "Location permission is required to show your current location on the map",
                    Toast.LENGTH_LONG).show();
        }

        locationPermissionLauncher.launch(requiredPermissions);
    }

    private void enableLocationFeatures() {
        if (googleMap == null) {
            Log.w(TAG, "GoogleMap is null, cannot enable location features");
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            try {
                googleMap.setMyLocationEnabled(true);
                Log.d(TAG, "Location features enabled");

                // Get current location
                getCurrentLocation();

            } catch (SecurityException e) {
                Log.e(TAG, "Security exception enabling location", e);
                Toast.makeText(requireContext(), "Location access error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "Fine location permission not granted");
            showDefaultLocation();
        }
    }

    private void getCurrentLocation() {
        if (!LocationUtils.hasLocationPermission(requireContext())) {
            Log.w(TAG, "No location permission, using default location");
            showDefaultLocation();
            return;
        }

        if (!LocationUtils.isLocationEnabled(requireContext())) {
            Log.w(TAG, "Location services disabled, using default location");
            Toast.makeText(requireContext(), "Please enable location services", Toast.LENGTH_SHORT).show();
            showDefaultLocation();
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            Log.d(TAG, "Current location: " + currentLocation);

                            // Update custom marker view with current location
                            updateCustomMarkerViewLocation(location);

                            // Animate to current location
                            animateToLocation(currentLocation);

                            // Load nearby products
                            viewModel.loadProductsNearby(
                                    currentLocation.latitude,
                                    currentLocation.longitude,
                                    10.0 // 10km radius
                            );

                        } else {
                            Log.w(TAG, "Failed to get current location", task.getException());
                            showDefaultLocation();
                        }
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting location", e);
            showDefaultLocation();
        }
    }

    private void updateCustomMarkerViewLocation(Location location) {
        if (customMapMarkerView != null) {
            customMapMarkerView.setCurrentLocation(location);
        }
    }

    private void showDefaultLocation() {
        Log.d(TAG, "Showing default location: Amsterdam");
        currentLocation = AMSTERDAM;
        animateToLocation(AMSTERDAM);
    }

    private void animateToLocation(LatLng location) {
        animateToLocation(location, DEFAULT_ZOOM_LEVEL);
    }

    private void animateToLocation(LatLng location, float zoomLevel) {
        if (googleMap != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, zoomLevel);
            googleMap.animateCamera(cameraUpdate);
            Log.d(TAG, "Animating to location: " + location + " with zoom: " + zoomLevel);
        }
    }

    private void addProductMarkers(@NonNull List<Product> products) {
        if (googleMap == null) {
            Log.w(TAG, "GoogleMap is null, cannot add markers");
            return;
        }

        Log.d(TAG, "Adding " + products.size() + " product markers to map");

        // Clear existing markers
        clearProductMarkers();

        if (products.isEmpty()) {
            Log.d(TAG, "No products to display");
            Toast.makeText(requireContext(), "No products found in this area", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        int markersAdded = 0;

        for (Product product : products) {
            if (product.getLocation() != null) {
                LatLng productLatLng = new LatLng(
                        product.getLocation().getLatitude(),
                        product.getLocation().getLongitude()
                );

                try {
                    Marker marker = createProductMarker(product, productLatLng);
                    if (marker != null) {
                        productMarkers.add(marker);
                        boundsBuilder.include(productLatLng);
                        markersAdded++;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error creating marker for product: " + product.getTitle(), e);
                }
            }
        }

        Log.d(TAG, "Successfully added " + markersAdded + " markers");

        // Adjust camera to show all markers if we have any
        if (markersAdded > 0) {
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 100; // padding in pixels
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } catch (Exception e) {
                Log.e(TAG, "Error adjusting camera bounds", e);
                // Fallback: zoom to first marker
                if (!productMarkers.isEmpty()) {
                    Marker firstMarker = productMarkers.get(0);
                    animateToLocation(firstMarker.getPosition());
                }
            }
        }
    }

    @Nullable
    private Marker createProductMarker(@NonNull Product product, @NonNull LatLng position) {
        try {
            BitmapDescriptor icon = createProductMarkerIcon(product);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(product.getTitle())
                    .snippet(String.format(Locale.getDefault(), "€%.2f", product.getPriceAsDouble()))
                    .icon(icon);

            Marker marker = googleMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(product);
                Log.d(TAG, "Created marker for product: " + product.getTitle());
            }
            return marker;

        } catch (Exception e) {
            Log.e(TAG, "Error creating marker for product: " + product.getTitle(), e);
            return null;
        }
    }

    @NonNull
    private BitmapDescriptor createProductMarkerIcon(@NonNull Product product) {
        // Use different colors based on product availability
        float hue = product.isAvailable() ?
                BitmapDescriptorFactory.HUE_GREEN :
                BitmapDescriptorFactory.HUE_RED;

        return BitmapDescriptorFactory.defaultMarker(hue);
    }

    private void clearProductMarkers() {
        Log.d(TAG, "Clearing " + productMarkers.size() + " existing markers");
        for (Marker marker : productMarkers) {
            marker.remove();
        }
        productMarkers.clear();

        if (googleMap != null) {
            googleMap.clear();
        }
    }

    private void refreshProducts() {
        Log.d(TAG, "Refreshing products");

        if (currentLocation != null) {
            viewModel.loadProductsNearby(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    10.0 // 10km radius
            );
        } else {
            viewModel.loadProductsWithLocations();
        }

        Toast.makeText(requireContext(), "Refreshing products...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof Product) {
            Product product = (Product) tag;

            // Show product info in a toast
            String message = String.format(Locale.getDefault(),
                    "%s - €%.2f", product.getTitle(), product.getPriceAsDouble());
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            // Show product details
            showProductDetails(product);

            return true;
        }
        return false;
    }

    private void showProductDetails(@NonNull Product product) {
        Log.d(TAG, "Showing details for product: " + product.getTitle());

        String details = String.format(Locale.getDefault(),
                "%s\nPrice: €%.2f\nCategory: %s\nStatus: %s",
                product.getTitle(),
                product.getPriceAsDouble(),
                product.getCategory(),
                product.isAvailable() ? "Available" : "Sold"
        );

        Toast.makeText(requireContext(), details, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "MapFragment resumed");

        // Refresh products when fragment becomes visible
        if (googleMap != null && viewModel != null) {
            refreshProducts();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "MapFragment paused");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "MapFragment destroying view");

        // Clean up resources
        if (mapService != null) {
            mapService.shutdown();
        }

        clearProductMarkers();
        binding = null;
    }
}