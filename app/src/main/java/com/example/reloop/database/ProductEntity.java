package com.example.reloop.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.reloop.models.Product;

import java.util.Objects;

/**
 * Wishlist product entity class
 * Corresponds to the wishlist_products table in local database
 */
@Entity(tableName = "wishlist_products",
        indices = {@Index(value = "pid", unique = true)})
public class ProductEntity {
    @PrimaryKey
    private String pid;          // Product ID (Primary Key)
    private String title;        // Product title
    private String price;        // Price (String format to match cloud)
    private String description;  // Product description
    private String imageUrl;     // Image URL
    private String category;     // Product category
    private boolean isFavorite;  // Favorite status

    // Default constructor (required by Room)
    public ProductEntity() {
        this.isFavorite = true;
    }

    // Full parameter constructor
    public ProductEntity(String pid, String title, String price, String description,
                         String imageUrl, String category) {
        this.pid = pid;
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.isFavorite = true;
    }

    // Getter methods
    public String getPid() { return pid; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public boolean isFavorite() { return isFavorite; }

    // Setter methods
    public void setPid(String pid) { this.pid = pid; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(String price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    // Utility method: Convert to simplified string representation
    public String toShortString() {
        return title + " - " + price;
    }

    // Debug toString method
    @Override
    public String toString() {
        return "ProductEntity{" +
                "pid='" + pid + '\'' +
                ", title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", category='" + category + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }

    // Equality based on product ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductEntity that = (ProductEntity) o;
        return Objects.equals(pid, that.pid);
    }

    @Override
    public int hashCode() {
        return pid != null ? pid.hashCode() : 0;
    }

    // Static method: Convert from cloud Product to local Entity
    public static ProductEntity fromCloudProduct(Product cloudProduct) {
        if (cloudProduct == null) return null;

        // Use public fields directly for Firebase compatibility
        return new ProductEntity(
                cloudProduct.pid,        // Direct field access
                cloudProduct.title,      // Direct field access
                cloudProduct.price,      // Direct field access
                cloudProduct.description, // Direct field access
                cloudProduct.imageUrl,   // Direct field access
                cloudProduct.category    // Direct field access
        );
    }

    // Convert back to cloud Product model
    public Product toCloudProduct() {
        Product product = new Product();
        product.pid = this.pid;
        product.title = this.title;
        product.price = this.price;
        product.description = this.description;
        product.imageUrl = this.imageUrl;
        product.category = this.category;
        product.isSold = false; // Default for wishlist items
        product.sellerId = "wishlist_user"; // Placeholder for wishlist items
        return product;
    }
}