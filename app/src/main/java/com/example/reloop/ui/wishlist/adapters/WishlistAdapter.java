package com.example.reloop.ui.wishlist.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.example.reloop.database.AppDataBase;
import com.example.reloop.database.ProductEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private final Context context;
    private List<ProductEntity> wishlist;
    private final AppDataBase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public WishlistAdapter(Context context, List<ProductEntity> wishlist) {
        this.context = context;
        this.wishlist = wishlist;
        this.db = AppDataBase.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductEntity product = wishlist.get(position);

        // Set product details
        holder.title.setText(product.getTitle());
        holder.price.setText("€" + product.getPrice());
        holder.favIcon.setSelected(true);

        // Load product image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .centerCrop()
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Remove from wishlist
        holder.favIcon.setOnClickListener(v -> removeFromWishlist(product, position));
    }

    private void removeFromWishlist(ProductEntity product, int position) {
        executor.execute(() -> {
            db.productDao().delete(product);

            // Update on UI thread
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    wishlist.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, wishlist.size());
                    Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlist != null ? wishlist.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ProductEntity> newWishlist) {
        this.wishlist = newWishlist;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;
        ImageView favIcon, productImage;

        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.product_title);
            price = itemView.findViewById(R.id.product_price);
            favIcon = itemView.findViewById(R.id.want_button);
            productImage = itemView.findViewById(R.id.product_image);
        }
    }
}