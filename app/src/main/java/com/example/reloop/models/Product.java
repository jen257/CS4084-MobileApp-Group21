package com.example.reloop.models;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.Objects;

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

    public Location location;   // Geographical location of the product (Legacy/Custom)
    public double latitude;
    public double longitude;

    public Product() {
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Product(String pid, String title, String description, String price,
                   String category, String imageUrl, String sellerId, Location location,
                   double latitude, double longitude) {
        this.pid = pid;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.location = location;
        this.isSold = false; // Default status is always Available
        this.latitude = latitude;
        this.longitude = longitude;
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
    public Location getLocation() { return location; }

    // New Coordinate Getters
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setPid(String pid) { this.pid = pid; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPrice(String price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setSold(boolean sold) { isSold = sold; }
    public void setLocation(Location location) { this.location = location; }

    // Coordinate Setters
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // ===== Utility Methods =====

    public double getPriceAsDouble() {
        if (price == null || price.isEmpty()) return 0.0;
        try {
            String clean = price.replaceAll("[^\\d.]", "");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getFormattedPrice() {
        if (price == null || price.isEmpty()) return "€0";
        return price.startsWith("€") ? price : "€" + price;
    }

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

    public static Product createNew(String title, String description, String price,
                                    String category, String imageUrl, String sellerId,
                                    Location location, double latitude, double longitude) {
        return new Product(null, title, description, price, category, imageUrl, sellerId, location, latitude, longitude);
    }
}