package com.example.reloop.ui.post;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.reloop.R;
import com.example.reloop.ui.post.viewmodel.PostViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment for posting a new product with modern image picking and location support.
 */
public class PostFragment extends Fragment {

    private ImageView ivProductImage;
    private TextView txtPlaceholder;
    private EditText etTitle, etPrice, etDescription;
    private Spinner spCategory;
    private Button btnSelectImage, btnPost;

    private Uri selectedImageUri;
    private PostViewModel viewModel;

    // Location support to ensure product is "Searchable" by distance
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private FusedLocationProviderClient fusedLocationClient;

    // 1. Modern Photo Picker Launcher (No Permissions Required)
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProductImage.setImageURI(uri);
                    ivProductImage.setVisibility(View.VISIBLE);
                    txtPlaceholder.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    // 2. Location Permission Launcher
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    fetchLocation();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        initViews(view);
        setupViewModel();
        setupCategorySpinner();
        setupListeners();

        // Request location as soon as they open the post screen
        checkLocationPermission();

        return view;
    }

    private void initViews(View view) {
        ivProductImage = view.findViewById(R.id.ivProductImage);
        txtPlaceholder = view.findViewById(R.id.txtImagePlaceholder);
        etTitle = view.findViewById(R.id.etTitle);
        etPrice = view.findViewById(R.id.etPrice);
        etDescription = view.findViewById(R.id.etDescription);
        spCategory = view.findViewById(R.id.spCategory);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnPost = view.findViewById(R.id.btnPost);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(PostViewModel.class);
        viewModel.successMessage.observe(getViewLifecycleOwner(), msg -> {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            clearForm();
        });
        viewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCategorySpinner() {
        String[] categories = {"Electronics", "Clothing", "Books", "Furniture", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        // Modern Select Image
        btnSelectImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        // Post Product
        btnPost.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String category = spCategory.getSelectedItem().toString();
            String description = etDescription.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (selectedImageUri == null || title.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Please complete the form and select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "anonymous";

            // Post with location data
            viewModel.postProduct(title, category, description, price, selectedImageUri, userId, currentLat, currentLng);
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
            }
        });
    }

    private void clearForm() {
        etTitle.setText("");
        etPrice.setText("");
        etDescription.setText("");
        spCategory.setSelection(0);
        ivProductImage.setImageDrawable(null);
        ivProductImage.setVisibility(View.GONE);
        txtPlaceholder.setVisibility(View.VISIBLE);
        selectedImageUri = null;
    }
}