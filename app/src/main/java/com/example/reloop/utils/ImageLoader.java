package com.example.reloop.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.reloop.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;

/**
 * [Member A - System Architect]
 * Handles image loading from URLs and image uploads to Firebase Storage.
 * Uses Glide for efficient memory management and caching (Optimization target for Day 6).
 */
public class ImageLoader {

    private final FirebaseStorage storage;

    public ImageLoader() {
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Loads an image from a URL into an ImageView using Glide.
     * Implements DiskCacheStrategy for performance optimization.
     */
    public static void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original and resized images
                .placeholder(android.R.drawable.ic_menu_report_image) // Default placeholder
                .error(android.R.drawable.ic_menu_close_clear_cancel) // Error image
                .into(imageView);
    }

    /**
     * Uploads a local image file to Firebase Storage.
     * @param imageUri The local URI of the image to upload.
     * @param callback Interface to return the download URL or error.
     */
    public void uploadImage(Uri imageUri, ImageUploadCallback callback) {
        // Create a unique filename using UUID
        String fileName = "products/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the public download URL after successful upload
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        callback.onSuccess(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Callback interface for image upload results.
     */
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }
}