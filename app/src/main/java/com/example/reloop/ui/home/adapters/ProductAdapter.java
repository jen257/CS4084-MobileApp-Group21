package com.example.reloop.ui.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.example.reloop.database.AppDataBase;
import com.example.reloop.database.daos.ProductDao;
import com.example.reloop.database.entities.ProductEntity;
import com.example.reloop.models.Product;

import java.util.List;
import java.util.concurrent.Executors;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnWantClickListener listener;

    public interface OnWantClickListener {
        void onWantClick(Product p);
    }

    public ProductAdapter(Context context, List<Product> productList, OnWantClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);

        holder.txtTitle.setText(p.getTitle());
        holder.txtCategory.setText(p.getCategory());
        holder.txtPrice.setText("€ " + p.getPrice());
        holder.txtDescription.setText(p.getDescription());

        // Image loading logic
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            holder.imgProduct.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(p.getImageUrl())
                    .centerCrop()
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setVisibility(View.VISIBLE);
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // --- NEW: Check initial wishlist state to display correct heart icon ---
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean exists = AppDataBase.getInstance(context).productDao().isProductInWishlist(p.getPid());
            ((android.app.Activity) context).runOnUiThread(() -> updateHeartIcon(holder.btnWant, exists));
        });

        // "Want" button click logic
        holder.btnWant.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWantClick(p);
            }
            toggleWishlist(p, holder);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void toggleWishlist(Product product, ProductViewHolder holder) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ProductEntity entity = ProductEntity.fromCloudProduct(product);
                AppDataBase db = AppDataBase.getInstance(context);
                ProductDao dao = db.productDao();

                boolean exists = dao.isProductInWishlist(entity.getPid());

                if (!exists) {
                    dao.insert(entity);
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Added to wishlist!", Toast.LENGTH_SHORT).show();
                        // FIXED: Use holder.btnWant instead of buttonToUpdate
                        updateHeartIcon(holder.btnWant, true);
                    });
                } else {
                    // UNSAVE logic: Delete using the faster deleteById method
                    dao.deleteById(product.getPid());

                    ((android.app.Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                        // FIXED: Use holder.btnWant instead of buttonToUpdate
                        updateHeartIcon(holder.btnWant, false);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Failed to update wishlist", Toast.LENGTH_SHORT).show());
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

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtTitle, txtCategory, txtPrice, txtDescription;
        Button btnWant;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.product_image);
            txtTitle = itemView.findViewById(R.id.product_title);
            txtCategory = itemView.findViewById(R.id.product_category);
            txtPrice = itemView.findViewById(R.id.product_price);
            txtDescription = itemView.findViewById(R.id.product_description);
            btnWant = itemView.findViewById(R.id.want_button);
        }
    }

    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }
}