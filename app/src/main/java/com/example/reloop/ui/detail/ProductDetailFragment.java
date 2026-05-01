package com.example.reloop.ui.detail;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.example.reloop.models.Product;

import android.widget.Button;
import android.widget.Toast;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ProductDetailFragment
 * Displays full product details in read-only mode.
 * UI is aligned with fragment_post.xml (same structure & style).
 */
public class ProductDetailFragment extends Fragment {

    private ImageView ivProductImage;
    private TextView tvTitle, tvCategory, tvPrice, tvDescription;
    private Button btnMessageSeller;
    private Product product;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        initViews(view);
        getProductFromBundle();
        bindData();

        return view;
    }

    /**
     * Initialize UI components
     */
    private void initViews(View view) {
        ivProductImage = view.findViewById(R.id.ivProductImage);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvDescription = view.findViewById(R.id.tvDescription);
        btnMessageSeller = view.findViewById(R.id.btnMessageSeller);
    }

    /**
     * Receive Product object from previous fragment (Home)
     */
    private void getProductFromBundle() {
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable("product");
        }
    }

    /**
     * Bind product data to UI
     */
    private void bindData() {
        if (product == null) return;

        // Set text fields
        tvTitle.setText(product.getTitle());
        tvCategory.setText(product.getCategory());
        tvPrice.setText(product.getFormattedPrice());
        tvDescription.setText(product.getDescription());

        // Load image
        if (!TextUtils.isEmpty(product.getImageUrl())) {
            Glide.with(requireContext())
                    .load(product.getImageUrl())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(ivProductImage);
        } else {
            ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        if (btnMessageSeller != null) {
            btnMessageSeller.setOnClickListener(v -> {

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(getContext(), "Please log in to message the seller", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentUserId = currentUser.getUid();

                String sellerId = product.getSellerId();


                if (currentUserId.equals(sellerId)) {
                    Toast.makeText(getContext(), "You cannot message yourself", Toast.LENGTH_SHORT).show();
                    return;
                }


                Bundle args = new Bundle();
                args.putString("receiverId", sellerId);

                try {
                    Navigation.findNavController(v).navigate(
                            R.id.action_productDetailFragment_to_chatFragment,
                            args
                    );
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), "Navigation action not found! Check nav_graph.xml", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}