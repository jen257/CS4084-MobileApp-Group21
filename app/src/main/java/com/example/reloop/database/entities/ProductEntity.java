package com.example.reloop.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.reloop.models.Product;
import com.example.reloop.utils.Constants;

import java.util.Objects;

@Entity(
        tableName = Constants.TABLE_WISHLIST,
        indices = {@Index(value = "pid", unique = true)}
)
public class ProductEntity {

    @PrimaryKey
    @NonNull
    private String pid;

    private String title;
    private String price;
    private String description;
    private String imageUrl;
    private String category;
    private boolean isFavorite;
    private double latitude;
    private double longitude;
    public ProductEntity() {
        this.isFavorite = true;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    // Constructor used when creating a new entity manually
    @Ignore
    public ProductEntity(String pid, String title, String price,
                         String description, String imageUrl, String category,
                         double latitude, double longitude) {
        this.pid = pid;
        this.title = title;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
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
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setPid(String pid) { this.pid = pid; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(String price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

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
        entity.setLatitude(p.getLatitude());     // Save coordinates locally
        entity.setLongitude(p.getLongitude());
        entity.setFavorite(true);

        return entity;
    }

    // Convert back to Product model
    public Product toCloudProduct() {
        Product p = new Product();
        p.setPid(pid);
        p.setTitle(title);
        p.setPrice(price);
        p.setDescription(description);
        p.setImageUrl(imageUrl);
        p.setCategory(category);
        p.setLatitude(latitude);                 // Restore coordinates
        p.setLongitude(longitude);
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