package com.example.reloop.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Product Data Access Object (DAO)
 * Defines all database operations related to the wishlist
 */
@Dao
public interface ProductDao {

    // Insert product into wishlist (replace if exists)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductEntity product);

    // Update product information in wishlist
    @Update
    void update(ProductEntity product);

    // Delete product from wishlist
    @Delete
    void deleteProduct(ProductEntity product);

    // Query all wishlist products
    @Query("SELECT * FROM wishlist_products")
    List<ProductEntity> getAllWishlistProducts();

    // Query specific product by product ID
    @Query("SELECT * FROM wishlist_products WHERE pid = :productId")
    ProductEntity getProductById(String productId);

    // Check if product is already in wishlist
    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_products WHERE pid = :productId)")
    boolean isProductInWishlist(String productId);

    // Get the count of products in wishlist
    @Query("SELECT COUNT(*) FROM wishlist_products")
    int getWishlistCount();

    // Filter wishlist products by category
    @Query("SELECT * FROM wishlist_products WHERE category = :category")
    List<ProductEntity> getProductsByCategory(String category);

    // Search products in wishlist (title or description contains keyword)
    @Query("SELECT * FROM wishlist_products WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<ProductEntity> searchProducts(String query);

    // Clear entire wishlist
    @Query("DELETE FROM wishlist_products")
    void clearWishlist();

    // Batch delete multiple products
    @Query("DELETE FROM wishlist_products WHERE pid IN (:productIds)")
    void deleteProductsByIds(List<String> productIds);

    // Get all product IDs in wishlist
    @Query("SELECT pid FROM wishlist_products")
    List<String> getAllWishlistProductIds();

    // Update favorite status of a product
    @Query("UPDATE wishlist_products SET isFavorite = :isFavorite WHERE pid = :productId")
    void updateFavoriteStatus(String productId, boolean isFavorite);

    // Toggle favorite status (convenience method)
    @Query("UPDATE wishlist_products SET isFavorite = NOT isFavorite WHERE pid = :productId")
    void toggleFavoriteStatus(String productId);

    // Check if a product exists in wishlist by ID
    @Query("SELECT COUNT(*) FROM wishlist_products WHERE pid = :productId")
    int productExists(String productId);
}