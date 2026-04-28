package com.example.reloop.ui.home.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;

import java.util.List;

/**
 * RecyclerView Adapter for displaying product categories in Home screen.
 * Handles category selection and notifies ViewModel through callback.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<String> categoryList;

    /**
     * Callback interface to notify category selection to Fragment/ViewModel
     */
    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    private final OnCategoryClickListener listener;

    /**
     * Tracks currently selected category position
     */
    private int selectedPosition = 0;

    /**
     * Constructor
     *
     * @param categoryList list of category names
     * @param listener click callback listener
     */
    public CategoryAdapter(List<String> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflate category item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {

        String category = categoryList.get(position);

        // Set category text
        holder.txtCategory.setText(category);

        // Handle selected and normal UI states
        if (position == selectedPosition) {

            // Selected state UI
            holder.txtCategory.setBackgroundResource(R.drawable.bg_category_selected);
            holder.txtCategory.setTextColor(Color.WHITE);

        } else {

            // Normal state UI
            holder.txtCategory.setBackgroundResource(R.drawable.bg_category_normal);
            holder.txtCategory.setTextColor(Color.BLACK);
        }

        // Click event handling
        holder.itemView.setOnClickListener(v -> {

            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Update only changed items (better performance)
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            // Notify listener (HomeFragment -> ViewModel)
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    /**
     * ViewHolder class for category item
     */
    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView txtCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(R.id.txt_category);
        }
    }
}