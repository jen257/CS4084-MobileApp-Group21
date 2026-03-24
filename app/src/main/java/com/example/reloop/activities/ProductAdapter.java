package com.example.reloop.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reloop.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<product> productList;

    // Click listener for "Want" button
    public interface OnWantClickListener {
        void onWantClick(product p);
    }

    private OnWantClickListener listener;

    public ProductAdapter(Context context, List<product> productList, OnWantClickListener listener) {
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

        product p = productList.get(position);

        // Title
        holder.txtTitle.setText(p.title);

        // Tag (category)
        holder.txtTagValue.setText(p.category);

        // Price
        holder.txtPriceValue.setText("€ " + p.price);

        // Description
        holder.txtDescription.setText(p.description);

        // Image (Glide)
        if (p.imageUrl != null && !p.imageUrl.isEmpty()) {
            holder.imgProduct.setVisibility(View.VISIBLE);
            holder.txtPlaceholder.setVisibility(View.GONE);

            Glide.with(context)
                    .load(p.imageUrl)
                    .centerCrop()
                    .into(holder.imgProduct);

        } else {
            holder.imgProduct.setVisibility(View.GONE);
            holder.txtPlaceholder.setVisibility(View.VISIBLE);
        }

        // Button click
        holder.btnWant.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWantClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder
    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtPlaceholder, txtTitle, txtTagValue, txtPriceValue, txtDescription;
        Button btnWant;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtPlaceholder = itemView.findViewById(R.id.txtImagePlaceholder);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtTagValue = itemView.findViewById(R.id.txtTagValue);
            txtPriceValue = itemView.findViewById(R.id.txtPriceValue);
            txtDescription = itemView.findViewById(R.id.txtDescription);

            btnWant = itemView.findViewById(R.id.btnWant);
        }
    }
}