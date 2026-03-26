package com.example.reloop.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// DAO for handling all wishlist database operations
@Dao
public interface ProductDao {

    // Add a product to wishlist (replace if already exists)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductEntity product);

    // Update product info
    @Update
    void update(ProductEntity product);

    // Remove a product from wishlist
    @Delete
    void delete(ProductEntity product);

    // Get all wishlist items
    @Query("SELECT * FROM wishlist_products ORDER BY pid DESC")
    List<ProductEntity> getAllWishlistProducts();

    // Get a single product by ID
    @Query("SELECT * FROM wishlist_products WHERE pid = :productId LIMIT 1")
    ProductEntity getProductById(String productId);

    // Check if a product is already in wishlist
    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_products WHERE pid = :productId)")
    boolean isProductInWishlist(String productId);

    // Delete using product ID (safer than passing whole object)
    @Query("DELETE FROM wishlist_products WHERE pid = :productId")
    void deleteById(String productId);

    // Get total number of saved items
    @Query("SELECT COUNT(*) FROM wishlist_products")
    int getWishlistCount();

    // Filter by category
    @Query("SELECT * FROM wishlist_products WHERE category = :category")
    List<ProductEntity> getProductsByCategory(String category);

    // Search by title or description
    @Query("SELECT * FROM wishlist_products WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<ProductEntity> searchProducts(String query);

    // Remove everything from wishlist
    @Query("DELETE FROM wishlist_products")
    void clearWishlist();

    // Update favorite flag (in case you want toggle logic later)
    @Query("UPDATE wishlist_products SET isFavorite = :isFavorite WHERE pid = :productId")
    void updateFavoriteStatus(String productId, boolean isFavorite);
}