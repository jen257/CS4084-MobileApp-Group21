package com.example.reloop.activities;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

/**
 * Data model representing a product in the marketplace.
 * Implements Serializable to allow passing product objects between Activities/Fragments.
 */
@IgnoreExtraProperties
public class product implements Serializable {
    public String pid;          // Unique Product ID (Firebase push key)
    public String title;        // Name of the product
    public String description;  // Detailed description
    public String price;        // Selling price
    public String category;     // Category (e.g., Electronics, Books, Furniture)
    public String imageUrl;     // Download URL from Firebase Storage
    public String sellerId;     // UID of the user who posted the item
    public long timestamp;      // Post creation time (Unix timestamp)
    public boolean isSold;      // Status of the item (true if sold)

    /**
     * Required empty constructor for Firebase Realtime Database image mapping.
     */
    public product() {
    }

    /**
     * Convenience constructor for creating a new product listing.
     */
    public product(String pid, String title, String description, String price,
                   String category, String imageUrl, String sellerId) {
        this.pid = pid;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.timestamp = System.currentTimeMillis();
        this.isSold = false;
    }
}