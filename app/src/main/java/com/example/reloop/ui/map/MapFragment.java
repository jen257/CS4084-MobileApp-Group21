package com.example.reloop.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapFragment";
    private static final float DEFAULT_ZOOM_LEVEL = 12.0f;
    private static final float MAX_ZOOM_LEVEL = 18.0f;
    private static final float MIN_ZOOM_LEVEL = 5.0f;

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

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean allGranted = permissions.values().stream().allMatch(granted -> granted);
                        if (allGranted) {
                            enableLocationFeatures();
                        } else {
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
        mapService = MapService.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        customMapMarkerView = new CustomMapMarkerView(requireContext());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);

        viewModel.getProductsWithLocations().observe(getViewLifecycleOwner(), products -> {
            if (googleMap != null) {
                addProductMarkers(products);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners() {
        binding.fabMyLocation.setOnClickListener(v -> {
            if (currentLocation != null) {
                animateToLocation(currentLocation);
            } else {
                getCurrentLocation();
            }
        });

        binding.fabRefresh.setOnClickListener(v -> refreshProducts());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        configureMap();
        checkLocationPermissions();

        viewModel.loadProductsWithLocations();

        // Listen to map long clicks to pick location and get address via Geocoder
        googleMap.setOnMapLongClickListener(latLng -> {
            resolveAddressFromLatLng(latLng.latitude, latLng.longitude);
        });
    }

    private void configureMap() {
        if (googleMap == null) return;
        try {
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);

            googleMap.setMinZoomPreference(MIN_ZOOM_LEVEL);
            googleMap.setMaxZoomPreference(MAX_ZOOM_LEVEL);
            googleMap.setOnMarkerClickListener(this);
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
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            enableLocationFeatures();
        } else {
            locationPermissionLauncher.launch(requiredPermissions);
        }
    }

    private void enableLocationFeatures() {
        if (googleMap == null) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                googleMap.setMyLocationEnabled(true);
                getCurrentLocation();
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception enabling location", e);
            }
        }
    }

    private void getCurrentLocation() {
        if (!LocationUtils.hasLocationPermission(requireContext()) || !LocationUtils.isLocationEnabled(requireContext())) {
            showDefaultLocation();
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    updateCustomMarkerViewLocation(location);
                    animateToLocation(currentLocation);

                    // Call Geocoder to convert to real address when location is retrieved
                    resolveAddressFromLatLng(currentLocation.latitude, currentLocation.longitude);

                    viewModel.loadProductsNearby(currentLocation.latitude, currentLocation.longitude, 10.0);
                } else {
                    showDefaultLocation();
                }
            });
        } catch (SecurityException e) {
            showDefaultLocation();
        }
    }

    /**
     * Converts coordinates into a real-world address string using Geocoder
     */
    private void resolveAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String fullAddress = addresses.get(0).getAddressLine(0);
                // For demonstration, display the resolved address in a Toast.
                // In actual deployment, bind this to your UI EditText: addressEditText.setText(fullAddress);
                Toast.makeText(requireContext(), "Address: " + fullAddress, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Resolved Address: " + fullAddress);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder exception", e);
            e.printStackTrace();
        }
    }

    private void updateCustomMarkerViewLocation(Location location) {
        if (customMapMarkerView != null) {
            customMapMarkerView.setCurrentLocation(location);
        }
    }

    private void showDefaultLocation() {
        currentLocation = AMSTERDAM;
        animateToLocation(AMSTERDAM);
    }

    private void animateToLocation(LatLng location) {
        animateToLocation(location, DEFAULT_ZOOM_LEVEL);
    }

    private void animateToLocation(LatLng location, float zoomLevel) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
        }
    }

    private void addProductMarkers(@NonNull List<Product> products) {
        if (googleMap == null) return;
        clearProductMarkers();

        for (Product product : products) {
            if (product.getLocation() != null) {
                LatLng productLatLng = new LatLng(product.getLocation().getLatitude(), product.getLocation().getLongitude());
                Marker marker = createProductMarker(product, productLatLng);
                if (marker != null) {
                    productMarkers.add(marker);
                }
            }
        }
    }

    @Nullable
    private Marker createProductMarker(@NonNull Product product, @NonNull LatLng position) {
        try {
            float hue = product.isAvailable() ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED;
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(product.getTitle())
                    .snippet(String.format(Locale.getDefault(), "€%.2f", product.getPriceAsDouble()))
                    .icon(BitmapDescriptorFactory.defaultMarker(hue));

            Marker marker = googleMap.addMarker(markerOptions);
            if (marker != null) marker.setTag(product);
            return marker;
        } catch (Exception e) {
            return null;
        }
    }

    private void clearProductMarkers() {
        for (Marker marker : productMarkers) marker.remove();
        productMarkers.clear();
    }

    private void refreshProducts() {
        if (currentLocation != null) {
            viewModel.loadProductsNearby(currentLocation.latitude, currentLocation.longitude, 10.0);
        } else {
            viewModel.loadProductsWithLocations();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof Product) {
            Product product = (Product) tag;
            Toast.makeText(requireContext(), product.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}