package com.example.reloop.ui.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

    // For Profile Manage Mode
    private boolean isManageMode = false;
    private OnProductManageListener manageListener;

    public interface OnWantClickListener {
        void onWantClick(Product p);
    }

    // Interface for managing items
    public interface OnProductManageListener {
        void onMarkAsSold(Product p);
        void onRemoveItem(Product p);
    }

    public ProductAdapter(Context context, List<Product> productList, OnWantClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    // Method to enable manage mode (used in UserProductsFragment)
    public void setManageMode(boolean manageMode, OnProductManageListener manageListener) {
        this.isManageMode = manageMode;
        this.manageListener = manageListener;
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
            Glide.with(context).load(p.getImageUrl()).centerCrop().into(holder.imgProduct);
        } else {
            holder.imgProduct.setVisibility(View.VISIBLE);
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // --- Overlay & Button Logic ---
        boolean isArchived = p.isSold() || p.isRemoved();

        if (p.isSold()) {
            holder.overlaySold.setText("SOLD OUT");
            holder.overlaySold.setTextColor(0xFFD32F2F); // Red text
            holder.overlaySold.setVisibility(View.VISIBLE);
        } else if (p.isRemoved()) {
            holder.overlaySold.setText("REMOVED ITEM");
            holder.overlaySold.setTextColor(0xFF555555); // Gray text
            holder.overlaySold.setVisibility(View.VISIBLE);
        } else {
            holder.overlaySold.setVisibility(View.GONE);
        }

        if (isManageMode) {
            holder.btnWant.setVisibility(View.GONE); // Hide 'Save' in manage mode
            if (!isArchived) {
                holder.btnOptions.setVisibility(View.VISIBLE);
                holder.btnOptions.setOnClickListener(v -> showPopupMenu(holder.btnOptions, p));
            } else {
                holder.btnOptions.setVisibility(View.GONE);
            }
        } else {
            holder.btnOptions.setVisibility(View.GONE); // Hide options in Home
            holder.btnWant.setVisibility(isArchived ? View.GONE : View.VISIBLE);
        }

        if (!isArchived && !isManageMode) {
            Executors.newSingleThreadExecutor().execute(() -> {
                boolean exists = AppDataBase.getInstance(context).productDao().isProductInWishlist(p.getPid());
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> updateHeartIcon(holder.btnWant, exists));
                }
            });
        }

        holder.btnWant.setOnClickListener(v -> {
            if (listener != null) listener.onWantClick(p);
            toggleWishlist(p, holder);
        });
    }

    private void showPopupMenu(View view, Product product) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenu().add(0, 1, 0, "Mark as Sold");
        popupMenu.getMenu().add(0, 2, 0, "Remove item");
        popupMenu.setOnMenuItemClickListener(item -> {
            if (manageListener != null) {
                if (item.getItemId() == 1) manageListener.onMarkAsSold(product);
                if (item.getItemId() == 2) manageListener.onRemoveItem(product);
            }
            return true;
        });
        popupMenu.show();
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
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            Toast.makeText(context, "Added to wishlist!", Toast.LENGTH_SHORT).show();
                            updateHeartIcon(holder.btnWant, true);
                        });
                    }
                } else {
                    dao.deleteById(product.getPid());
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                            updateHeartIcon(holder.btnWant, false);
                        });
                    }
                }
            } catch (Exception e) {}
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
        TextView overlaySold;
        TextView btnOptions;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.product_image);
            txtTitle = itemView.findViewById(R.id.product_title);
            txtCategory = itemView.findViewById(R.id.product_category);
            txtPrice = itemView.findViewById(R.id.product_price);
            txtDescription = itemView.findViewById(R.id.product_description);
            btnWant = itemView.findViewById(R.id.want_button);
            overlaySold = itemView.findViewById(R.id.overlay_sold);
            btnOptions = itemView.findViewById(R.id.btnOptions);
        }
    }

    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }
}