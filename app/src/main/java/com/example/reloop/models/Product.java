package com.example.reloop.models;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.Objects;

/**
 * [Member A - System Architect]
 * Core Data Model representing a Product in the Reloop Marketplace.
 * This class is designed to be compatible with Firebase Realtime Database
 * and supports conversion to Room Entities for local storage.
 * * Implements Serializable to allow passing Product objects between
 * Fragments via Bundles (Member C/D Navigation).
 */
@IgnoreExtraProperties
public class Product implements Serializable {

    // Firebase database keys
    public String pid;          // Unique Product ID from Firebase push().getKey()
    public String sellerId;     // Firebase UID of the user who posted the item
    public String title;        // Name of the item
    public String description;  // Detailed item description
    public String imageUrl;     // URL for the product image hosted on Firebase/Web
    public String price;        // Price stored as String to handle currency formatting
    public String category;     // Item category (e.g., Electronics, Clothing)
    public boolean isSold;      // Availability status (True if item is no longer for sale)

    /**
     * Required default constructor for Firebase DataSnapshot.getValue(Product.class)
     */
    public Product() {
    }

    /**
     * Full constructor for manual object creation.
     */
    public Product(String pid, String title, String description, String price,
                   String category, String imageUrl, String sellerId) {
        this.pid = pid;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.isSold = false; // Default status is always Available
    }

    // ===== Getters and Setters =====

    public String getPid() { return pid; }
    public String getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getPrice() { return price; }
    public String getCategory() { return category; }
    public boolean isSold() { return isSold; }

    public void setPid(String pid) { this.pid = pid; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPrice(String price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setSold(boolean sold) { isSold = sold; }

    // ===== Utility Methods (Extra Credit Features) =====

    /**
     * Converts the string-based price to a double for numerical operations.
     * Removes non-numeric characters like "€" or commas.
     * @return double representation of price, or 0.0 if invalid.
     */
    public double getPriceAsDouble() {
        if (price == null || price.isEmpty()) return 0.0;
        try {
            String clean = price.replaceAll("[^\\d.]", "");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Formats the price for UI display.
     * Ensures the Euro symbol is present.
     */
    public String getFormattedPrice() {
        if (price == null || price.isEmpty()) return "€0";
        return price.startsWith("€") ? price : "€" + price;
    }

    /**
     * Helper to check availability status.
     */
    public boolean isAvailable() {
        return !isSold;
    }


    // ===== Standard Overrides =====

    @Override
    public String toString() {
        return title + " - " + getFormattedPrice();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product p = (Product) o;
        return Objects.equals(pid, p.pid);
    }

    @Override
    public int hashCode() {
        return pid != null ? pid.hashCode() : 0;
    }

    /**
     * Static factory method for creating new product instances before pushing to Firebase.
     */
    public static Product createNew(String title, String description, String price,
                                    String category, String imageUrl, String sellerId) {
        return new Product(null, title, description, price, category, imageUrl, sellerId);
    }
}