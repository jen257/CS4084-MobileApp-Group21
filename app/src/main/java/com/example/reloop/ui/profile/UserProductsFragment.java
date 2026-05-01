package com.example.reloop.ui.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.example.reloop.ui.home.adapters.ProductAdapter;
import com.example.reloop.ui.profile.viewmodel.UserProductsViewModel;

import java.util.ArrayList;

public class UserProductsFragment extends Fragment {

    private UserProductsViewModel viewModel;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextView tvPageTitle;

    private boolean showSoldItems = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showSoldItems = getArguments().getBoolean("showSoldItems", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_products, container, false);

        tvPageTitle = view.findViewById(R.id.tvPageTitle);
        recyclerView = view.findViewById(R.id.rvUserProducts);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        tvPageTitle.setText(showSoldItems ? "Sold Items" : "Items on Sell");

        setupViewModel();
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(requireContext(), new ArrayList<>(), p -> {});

        // Enable Manage Mode and Listen for Menu Clicks
        adapter.setManageMode(true, new ProductAdapter.OnProductManageListener() {
            @Override
            public void onMarkAsSold(Product p) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Mark as Sold")
                        .setMessage("Are you sure you want to mark this item as sold?")
                        .setPositiveButton("Yes", (d, w) -> {
                            viewModel.updateProductStatus(p, true, false, showSoldItems);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            @Override
            public void onRemoveItem(Product p) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Remove Item")
                        .setMessage("Are you sure you want to remove this item?")
                        .setPositiveButton("Yes", (d, w) -> {
                            viewModel.updateProductStatus(p, false, true, showSoldItems);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(UserProductsViewModel.class);

        viewModel.userProducts.observe(getViewLifecycleOwner(), products -> {
            adapter.updateData(products);
            tvEmptyState.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        // Trigger the initial data fetch
        viewModel.fetchUserProducts(showSoldItems);
    }
}