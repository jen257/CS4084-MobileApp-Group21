package com.example.reloop.shared.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.example.reloop.database.AppDataBase;
import com.example.reloop.database.daos.ProductDao;
import com.example.reloop.database.entities.ProductEntity;
import com.example.reloop.databinding.ViewProductCardBinding;
import com.example.reloop.models.Product;

import java.util.concurrent.Executors;

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
        setOnClickListener(v -> {
            if (listener != null && currentProduct != null) {
                listener.onProductClick(currentProduct);
            }
        });

        // Initialize the Save button click
        binding.wantButton.setOnClickListener(v -> {
            if (currentProduct != null) {
                toggleWishlist(currentProduct);
            }
        });
    }

    public void bindProduct(Product product) {
        this.currentProduct = product;
        binding.productName.setText(product.getTitle());
        binding.productPrice.setText(product.getPrice());
        binding.productDescription.setText(product.getDescription());

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(getContext())
                    .load(product.getImageUrl())
                    .into(binding.productImage);
        } else {
            binding.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if (product.getSellerId() != null) {
            String sellerText = getContext().getString(R.string.seller_id_format, product.getSellerId());
            binding.sellerName.setText(sellerText);
        }

        if (product.isSold()) {
            binding.availability.setText(R.string.product_sold);
            binding.availability.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
        } else {
            binding.availability.setText(R.string.product_available);
            binding.availability.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
        }

        if (product.getCategory() != null) {
            binding.category.setText(product.getCategory());
        }

        // Fetch Wishlist status for the custom View Component
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean exists = AppDataBase.getInstance(getContext()).productDao().isProductInWishlist(product.getPid());
            ((android.app.Activity) getContext()).runOnUiThread(() -> updateHeartIcon(binding.wantButton, exists));
        });
    }

    private void toggleWishlist(Product product) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ProductEntity entity = ProductEntity.fromCloudProduct(product);
                AppDataBase db = AppDataBase.getInstance(getContext());
                ProductDao dao = db.productDao();

                boolean exists = dao.isProductInWishlist(entity.getPid());

                if (!exists) {
                    dao.insert(entity);
                    ((android.app.Activity) getContext()).runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Added to wishlist!", Toast.LENGTH_SHORT).show();
                        updateHeartIcon(binding.wantButton, true);
                    });
                } else {
                    dao.delete(entity);
                    ((android.app.Activity) getContext()).runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                        updateHeartIcon(binding.wantButton, false);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateHeartIcon(Button button, boolean isInWishlist) {
        if (isInWishlist) {
            button.setText("♥ Saved");
            button.setTextColor(0xFFFF4444);
        } else {
            button.setText("♡ Save");
            button.setTextColor(0xFF666666);
        }
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}