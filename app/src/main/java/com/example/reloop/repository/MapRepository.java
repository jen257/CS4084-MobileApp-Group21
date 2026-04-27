package com.example.reloop.repository;

import androidx.annotation.NonNull;

import com.example.reloop.models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for map-specific data operations
 */
public class MapRepository {

    private final DatabaseReference productsRef;
    private final DatabaseReference mapDataRef;

    public MapRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");
        mapDataRef = database.getReference("map_data");
    }

    /**
     * Load products with location data for map display
     */
    public void loadProductsWithLocations(final MapDataCallback callback) {
        productsRef.orderByChild("location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Product> products = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null && product.getLocation() != null) {
                        product.setPid(snapshot.getKey()); // Set Firebase key
                        products.add(product);
                    }
                }

                callback.onProductsLoaded(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Load products within geographic bounds
     */
    public void loadProductsInBounds(double minLat, double maxLat, double minLng, double maxLng,
                                     final MapDataCallback callback) {
        // Note: Firebase doesn't support geographic queries natively
        // This would require a geohash implementation for production
        loadProductsWithLocations(new MapDataCallback() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                // Filter products by bounds (client-side filtering)
                List<Product> filteredProducts = new ArrayList<>();
                for (Product product : products) {
                    if (product.getLocation() != null) {
                        double lat = product.getLocation().getLatitude();
                        double lng = product.getLocation().getLongitude();

                        if (lat >= minLat && lat <= maxLat && lng >= minLng && lng <= maxLng) {
                            filteredProducts.add(product);
                        }
                    }
                }
                callback.onProductsLoaded(filteredProducts);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Save user's map preferences (center, zoom level, etc.)
     */
    public void saveMapPreferences(String userId, MapPreferences preferences,
                                   final OperationCallback callback) {
        mapDataRef.child("preferences").child(userId).setValue(preferences)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Load user's saved map preferences
     */
    public void loadMapPreferences(String userId, final MapPreferencesCallback callback) {
        mapDataRef.child("preferences").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        MapPreferences preferences = snapshot.getValue(MapPreferences.class);
                        callback.onPreferencesLoaded(preferences);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                }
        );
    }

    /**
     * Track product views on map for analytics
     */
    public void trackProductView(String productId, String userId) {
        mapDataRef.child("analytics").child(productId).child("views").child(userId).setValue(System.currentTimeMillis());
    }

    /**
     * Get popular products based on map interactions
     */
    public void getPopularProducts(final MapDataCallback callback) {
        // Implementation for getting popular products based on views
        loadProductsWithLocations(callback); // Fallback for now
    }

    public interface MapDataCallback {
        void onProductsLoaded(List<Product> products);
        void onError(String error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface MapPreferencesCallback {
        void onPreferencesLoaded(MapPreferences preferences);
        void onError(String error);
    }

    /**
     * Data class for map preferences
     */
    public static class MapPreferences {
        public double centerLat;
        public double centerLng;
        public float zoomLevel;
        public String mapType;
        public long lastUpdated;

        public MapPreferences() {
            // Default constructor for Firebase
        }

        public MapPreferences(double centerLat, double centerLng, float zoomLevel, String mapType) {
            this.centerLat = centerLat;
            this.centerLng = centerLng;
            this.zoomLevel = zoomLevel;
            this.mapType = mapType;
            this.lastUpdated = System.currentTimeMillis();
        }
    }
}