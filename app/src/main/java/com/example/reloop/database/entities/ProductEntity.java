package com.example.reloop.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.reloop.models.Product;
import com.example.reloop.utils.Constants;

import java.util.Objects;

// This class represents a product saved in the local wishlist (Room database)
@Entity(
        tableName = Constants.TABLE_WISHLIST,
        indices = {@Index(value = "pid", unique = true)} // make sure no duplicate products
)
public class ProductEntity {

    @PrimaryKey
    @NonNull
    private String pid; // product ID (same as Firebase)

    private String title;
    private String price;
    private String description;
    private String imageUrl;
    private String category;
    private boolean isFavorite; // used to mark if it's still in wishlist

    // Required empty constructor for Room
    public ProductEntity() {
        this.isFavorite = true;
    }

    // Constructor used when creating a new entity manually
    /**
     * Secondary constructor for manual object creation.
     * @Ignore tells Room to bypass this constructor to avoid ambiguity.
     */
    @Ignore
    public ProductEntity(String pid, String title, String price,
                         String description, String imageUrl, String category) {
        this.pid = pid;
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.isFavorite = true;
    }

    // Getters
    public String getPid() { return pid; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public boolean isFavorite() { return isFavorite; }

    // Setters
    public void setPid(String pid) { this.pid = pid; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(String price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    // Convert Firebase Product -> local Room entity
    public static ProductEntity fromCloudProduct(Product p) {
        if (p == null) return null;

        ProductEntity entity = new ProductEntity();
        entity.setPid(p.getPid());
        entity.setTitle(p.getTitle());
        entity.setPrice(p.getPrice());
        entity.setDescription(p.getDescription());
        entity.setImageUrl(p.getImageUrl());
        entity.setCategory(p.getCategory());
        entity.setFavorite(true);

        return entity;
    }

    // Convert back to Product model (not really needed now, but useful later)
    public Product toCloudProduct() {
        Product p = new Product();
        p.setPid(pid);
        p.setTitle(title);
        p.setPrice(price);
        p.setDescription(description);
        p.setImageUrl(imageUrl);
        p.setCategory(category);
        p.setSellerId("local_user"); // placeholder
        p.setSold(false);
        return p;
    }

    @Override
    public String toString() {
        return title + " - " + price;
    }

    // Compare based on product ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductEntity)) return false;
        ProductEntity that = (ProductEntity) o;
        return Objects.equals(pid, that.pid);
    }

    @Override
    public int hashCode() {
        return pid != null ? pid.hashCode() : 0;
    }
}