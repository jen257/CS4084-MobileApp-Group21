package com.example.reloop.utils;

/**
 * [Member A - System Architect]
 * Global constants for the Reloop application.
 * Using constants prevents typos and ensures consistency across the team's work.
 */
public class Constants {

    // --- Firebase Realtime Database Nodes ---
    // Use these instead of hardcoded strings like "products"
    public static final String NODE_PRODUCTS = "products";
    public static final String NODE_USERS = "users";

    // --- Intent / Bundle Extra Keys ---
    // Used when passing data between Fragments (e.g., from Home to Detail)
    public static final String KEY_PRODUCT_ID = "product_id";
    public static final String KEY_PRODUCT_OBJECT = "product_object";
    public static final String KEY_SELLER_ID = "seller_id";

    // --- Local Database (Room) Constants ---
    // Used by Member D for the Wishlist feature
    public static final String DATABASE_NAME = "reloop_db";
    public static final String TABLE_WISHLIST = "wishlist_products";

    // --- Image Upload Folders (If using Firebase Storage) ---
    public static final String STORAGE_PATH_UPLOADS = "product_images/";

    // --- Categories (To keep them consistent across the app) ---
    public static final String CAT_ELECTRONICS = "Electronics";
    public static final String CAT_CLOTHING = "Clothing";
    public static final String CAT_BOOKS = "Books";
    public static final String CAT_HOUSEHOLD = "Household";
}