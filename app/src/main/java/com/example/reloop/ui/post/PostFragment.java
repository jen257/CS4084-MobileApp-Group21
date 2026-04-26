package com.example.reloop.ui.post;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.reloop.R;
import com.example.reloop.ui.post.viewmodel.PostViewModel;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment for posting a new product.
 */
public class PostFragment extends Fragment {

    private ImageView ivProductImage;
    private TextView txtPlaceholder;
    private EditText etTitle, etPrice, etDescription;
    private Spinner spCategory;
    private Button btnSelectImage, btnPost;

    private Uri selectedImageUri;

    private PostViewModel viewModel;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();

                    ivProductImage.setImageURI(selectedImageUri);
                    ivProductImage.setVisibility(View.VISIBLE);
                    txtPlaceholder.setVisibility(View.GONE);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post, container, false);

        initViews(view);
        setupViewModel();
        setupCategorySpinner();
        setupListeners();

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
        String[] categories = {"Electronics", "Clothes", "Books", "Furniture", "Other"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );

        spCategory.setAdapter(adapter);
    }

    private void setupListeners() {

        // Select Image
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Post Product
        btnPost.setOnClickListener(v -> {

            String title = etTitle.getText().toString().trim();
            String category = spCategory.getSelectedItem().toString();
            String description = etDescription.getText().toString().trim();

            double price = 0;
            try {
                price = Double.parseDouble(etPrice.getText().toString().trim());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "anonymous";

            viewModel.postProduct(
                    title,
                    category,
                    description,
                    price,
                    selectedImageUri,
                    userId
            );
        });
    }

    /**
     * Reset form after successful post
     */
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