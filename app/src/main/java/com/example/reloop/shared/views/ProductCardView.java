package com.example.reloop.shared.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.example.reloop.databinding.ViewProductCardBinding;
import com.example.reloop.models.Product;

/**
 * Custom view for displaying product information in a card layout
 * Handles product image loading and click events
 */
@SuppressWarnings("unused")
public class ProductCardView extends FrameLayout {

    private ViewProductCardBinding binding;
    private OnProductClickListener listener;
    private Product currentProduct;

    public ProductCardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ProductCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProductCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = ViewProductCardBinding.inflate(LayoutInflater.from(context), this, true);

        // Set click listener for the entire card
        setOnClickListener(v -> {
            if (listener != null && currentProduct != null) {
                listener.onProductClick(currentProduct);
            }
        });
    }

    /**
     * Bind product data to the view
     */
    public void bindProduct(Product product) {
        this.currentProduct = product;

        // Set basic product information using your actual Product model methods
        binding.productName.setText(product.getTitle());
        binding.productPrice.setText(product.getPrice());
        binding.productDescription.setText(product.getDescription());

        // Load product image using Glide
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(getContext())
                    .load(product.getImageUrl())
                    .into(binding.productImage);
        } else {
            binding.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Display seller information
        if (product.getSellerId() != null) {
            String sellerText = getContext().getString(R.string.seller_id_format, product.getSellerId());
            binding.sellerName.setText(sellerText);
        }

        // Display availability status
        if (product.isSold()) {
            binding.availability.setText(R.string.product_sold);
            binding.availability.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
        } else {
            binding.availability.setText(R.string.product_available);
            binding.availability.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
        }

        // Display category information
        if (product.getCategory() != null) {
            binding.category.setText(product.getCategory());
        }
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    /**
     * Interface for handling product click events
     */
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}