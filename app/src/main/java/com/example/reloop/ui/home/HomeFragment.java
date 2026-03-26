package com.example.reloop.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.example.reloop.ui.home.adapters.ProductAdapter;
import com.example.reloop.utils.FireBaseHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;

    private FireBaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        //  Bind RecyclerView
        recyclerView = root.findViewById(R.id.recycler_home_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize data list
        productList = new ArrayList<>();

        //  Initialize Adapter
        adapter = new ProductAdapter(getContext(), productList);
        recyclerView.setAdapter(adapter);

        //  Initialize FirebaseHelper
        firebaseHelper = new FireBaseHelper();

        //  Load data from cloud
        loadProducts();

        return root;
    }

    private void loadProducts() {

        firebaseHelper.getAllProducts(new FireBaseHelper.ProductCallback() {

            @Override
            public void onSuccess(List<Product> products) {
                Log.d("HomeFragment", "Products loaded: " + products.size());

                productList.clear();
                productList.addAll(products);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Log.e("HomeFragment", "Error loading products: " + error);
            }
        });
    }
}