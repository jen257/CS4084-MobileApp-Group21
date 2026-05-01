package com.example.reloop.ui.wishlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
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

    // NEW: Keep track of the current list so we can instantly remove items
    private List<Product> currentWishlist = new ArrayList<>();

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

        adapter = new ProductAdapter(getContext(), currentWishlist,
                // 1. The Wishlist Button Click
                product -> {
                    // INSTANT UI UPDATE: Remove the product from the screen immediately
                    currentWishlist.remove(product);
                    adapter.updateData(currentWishlist);

                    // If the list is now empty, immediately show the empty state screen
                    if (currentWishlist.isEmpty()) {
                        rvWishlist.setVisibility(View.GONE);
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    }
                },

                // 2. The Whole Card Click (Navigates to details)
                product -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("product", product);

                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.productDetailFragment, bundle);
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        rvWishlist.setAdapter(adapter);
    }

    /**
     * Fetches saved products from Room Database and updates the UI state.
     */
    private void loadWishlistData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Fetch all product entities from local wishlist table
            List<ProductEntity> savedEntities = AppDataBase.getInstance(getContext())
                    .productDao().getAllWishlistProducts();

            // 2. Convert entities to the Product model for the adapter
            List<Product> fetchedProducts = new ArrayList<>();
            for (ProductEntity entity : savedEntities) {
                fetchedProducts.add(entity.toCloudProduct());
            }

            // 3. Update UI on the main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Update our local list with the fresh data from the database
                    currentWishlist.clear();
                    currentWishlist.addAll(fetchedProducts);

                    if (currentWishlist.isEmpty()) {
                        rvWishlist.setVisibility(View.GONE);
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    } else {
                        rvWishlist.setVisibility(View.VISIBLE);
                        emptyStateLayout.setVisibility(View.GONE);

                        // Update adapter data
                        adapter.updateData(currentWishlist);
                    }
                });
            }
        });
    }
}