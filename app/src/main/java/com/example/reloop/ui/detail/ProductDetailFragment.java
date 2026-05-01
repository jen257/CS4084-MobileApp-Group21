package com.example.reloop.ui.detail;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProductDetailFragment extends Fragment {

    private ImageView ivProductImage;
    private TextView tvTitle, tvCategory, tvPrice, tvDescription;
    private Button btnMessageSeller;
    private Product product;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        getProductFromBundle();
        bindData();
    }

    private void initViews(View view) {
        ivProductImage = view.findViewById(R.id.ivProductImage);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvDescription = view.findViewById(R.id.tvDescription);
        btnMessageSeller = view.findViewById(R.id.btnMessageSeller);
    }

    private void getProductFromBundle() {
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable("product");
        }
    }

    private void bindData() {
        if (product == null) return;

        tvTitle.setText(product.getTitle());
        tvCategory.setText(product.getCategory());
        tvPrice.setText(product.getFormattedPrice());
        tvDescription.setText(product.getDescription());

        if (!TextUtils.isEmpty(product.getImageUrl())) {
            Glide.with(requireContext()).load(product.getImageUrl()).centerCrop().into(ivProductImage);
        }

        if (btnMessageSeller != null) {
            btnMessageSeller.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentUserId = currentUser.getUid();
                String sellerId = product.getSellerId();
                String productId = product.getPid();

                if (currentUserId.equals(sellerId)) {
                    Toast.makeText(getContext(), "You cannot message yourself", Toast.LENGTH_SHORT).show();
                    return;
                }

                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(sellerId)
                        .child("username")
                        .get()
                        .addOnSuccessListener(dataSnapshot -> {
                            String actualSellerName = dataSnapshot.getValue(String.class);
                            if (actualSellerName == null) actualSellerName = "User";

                            //  Create a consistent ID based ONLY on the two user IDs.
                            String conversationId = (currentUserId.compareTo(sellerId) < 0)
                                    ? currentUserId + "_" + sellerId
                                    : sellerId + "_" + currentUserId;

                            Bundle args = new Bundle();
                            args.putString("conversationId", conversationId);
                            args.putString("currentUserId", currentUserId);
                            args.putString("otherUserId", sellerId);
                            args.putString("productId", productId);
                            args.putString("otherUserName", actualSellerName);

                            Navigation.findNavController(v).navigate(R.id.action_productDetailFragment_to_chatFragment, args);
                        });
            });
        }
    }
}