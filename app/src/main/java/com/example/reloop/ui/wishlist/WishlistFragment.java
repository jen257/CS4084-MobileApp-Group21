package com.example.reloop.ui.wishlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;
import com.example.reloop.database.AppDataBase;
import com.example.reloop.database.entities.ProductEntity;
import com.example.reloop.models.Product;
import com.example.reloop.ui.home.adapters.ProductAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * WishlistFragment handles the display of saved products.
 * It observes the local Room database and updates the UI accordingly.
 */
public class WishlistFragment extends Fragment {

    private RecyclerView rvWishlist;
    private LinearLayout emptyStateLayout;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        rvWishlist = view.findViewById(R.id.rvWishlist);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data every time the user returns to this fragment
        loadWishlistData();
    }

    private void setupRecyclerView() {
        rvWishlist.setLayoutManager(new LinearLayoutManager(getContext()));

        // Using the existing ProductAdapter to maintain UI consistency
        adapter = new ProductAdapter(getContext(), new ArrayList<>(), product -> {
            // Optional: Handle item clicks here
        });

        rvWishlist.setAdapter(adapter);
    }

    /**
     * Fetches saved products from Room Database and updates the UI state.
     */
    private void loadWishlistData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Fetch all product entities from local wishlist table [cite: 145]
            List<ProductEntity> savedEntities = AppDataBase.getInstance(getContext())
                    .productDao().getAllWishlistProducts();

            // 2. Convert entities to the Product model for the adapter [cite: 165]
            List<Product> savedProducts = new ArrayList<>();
            for (ProductEntity entity : savedEntities) {
                savedProducts.add(entity.toCloudProduct());
            }

            // 3. Update UI on the main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (savedProducts.isEmpty()) {
                        rvWishlist.setVisibility(View.GONE);
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    } else {
                        rvWishlist.setVisibility(View.VISIBLE);
                        emptyStateLayout.setVisibility(View.GONE);

                        // Update adapter data [cite: 46]
                        adapter.updateData(savedProducts);
                    }
                });
            }
        });
    }
}