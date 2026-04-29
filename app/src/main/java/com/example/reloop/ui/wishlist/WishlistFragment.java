package com.example.reloop.ui.wishlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;
import com.example.reloop.database.AppDataBase;
import com.example.reloop.database.entities.ProductEntity;
import com.example.reloop.ui.wishlist.adapters.WishlistAdapter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WishlistFragment extends Fragment {

    private RecyclerView rvWishlist;
    private TextView tvEmptyWishlist;
    private WishlistAdapter adapter;
    private AppDataBase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);
        initViews(view);
        initDatabase();
        setupRecyclerView();
        loadWishlist();
        return view;
    }

    private void initViews(View view) {
        rvWishlist = view.findViewById(R.id.rv_wishlist);
        tvEmptyWishlist = view.findViewById(R.id.tv_empty_wishlist);
        rvWishlist.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    private void initDatabase() {
        database = AppDataBase.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        // Initialize adapter with empty list
        adapter = new WishlistAdapter(requireContext(), Collections.emptyList());
        rvWishlist.setAdapter(adapter);
    }

    private void loadWishlist() {
        executor.execute(() -> {
            List<ProductEntity> wishlist = database.productDao().getAllWishlistProducts();

            if (getActivity() == null) return;

            getActivity().runOnUiThread(() -> updateUI(wishlist));
        });
    }

    private void updateUI(List<ProductEntity> wishlist) {
        if (wishlist == null || wishlist.isEmpty()) {
            showEmptyState();
        } else {
            showWishlist(wishlist);
        }
    }

    private void showEmptyState() {
        rvWishlist.setVisibility(View.GONE);
        tvEmptyWishlist.setVisibility(View.VISIBLE);
    }

    private void showWishlist(List<ProductEntity> wishlist) {
        rvWishlist.setVisibility(View.VISIBLE);
        tvEmptyWishlist.setVisibility(View.GONE);
        adapter.updateData(wishlist);
    }

    @SuppressWarnings("unused")
    public void refreshWishlist() {
        loadWishlist();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWishlist(); // Refresh when fragment becomes visible
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up executor
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}