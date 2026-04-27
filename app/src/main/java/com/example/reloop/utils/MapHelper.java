package com.example.reloop.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

/**
 * Utility class for common map operations and calculations
 */
public class MapHelper {
    private static final String TAG = "MapHelper";
    private static final float DEFAULT_ZOOM_LEVEL = 12.0f;
    private static final float CLUSTER_ZOOM_LEVEL = 10.0f;
    private static final int MARKER_ICON_SIZE = 120; // dp

    /**
     * Calculate optimal zoom level to show all markers
     */
    public static float calculateOptimalZoom(@Nullable List<LatLng> locations, @Nullable GoogleMap googleMap) {
        if (locations == null || locations.isEmpty() || googleMap == null) {
            Log.d(TAG, "Using default zoom level");
            return DEFAULT_ZOOM_LEVEL;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng location : locations) {
            builder.include(location);
        }

        try {
            LatLngBounds bounds = builder.build();
            int padding = 100; // padding in pixels

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            float zoom = googleMap.getCameraPosition().zoom;
            Log.d(TAG, "Calculated optimal zoom: " + zoom);
            return zoom;
        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate optimal zoom, using default", e);
            return DEFAULT_ZOOM_LEVEL;
        }
    }

    /**
     * Create custom marker icon based on product type and status
     */
    @NonNull
    public static BitmapDescriptor createProductMarkerIcon(@NonNull Context context, @NonNull Product product) {
        int drawableId;
        String category = product.getCategory() != null ? product.getCategory().toLowerCase(Locale.ROOT) : "";

        // Choose icon based on product category
        switch (category) {
            case "electronics":
                drawableId = product.isAvailable() ?
                        R.drawable.ic_marker_default : R.drawable.ic_marker_default_sold;
                break;
            case "clothes":
                drawableId = product.isAvailable() ?
                        R.drawable.ic_marker_default : R.drawable.ic_marker_default_sold;
                break;
            case "books":
                drawableId = product.isAvailable() ?
                        R.drawable.ic_marker_default : R.drawable.ic_marker_default_sold;
                break;
            case "furniture":
                drawableId = product.isAvailable() ?
                        R.drawable.ic_marker_default : R.drawable.ic_marker_default_sold;
                break;
            default:
                drawableId = product.isAvailable() ?
                        R.drawable.ic_marker_default : R.drawable.ic_marker_default_sold;
                break;
        }

        Log.d(TAG, "Creating marker icon for product: " + product.getTitle() +
                ", available: " + product.isAvailable() + ", drawable: " + drawableId);
        return getBitmapDescriptor(context, drawableId);
    }

    /**
     * Convert drawable to BitmapDescriptor for map marker
     */
    @NonNull
    private static BitmapDescriptor getBitmapDescriptor(@NonNull Context context, int drawableId) {
        try {
            Drawable vectorDrawable = ContextCompat.getDrawable(context, drawableId);
            if (vectorDrawable == null) {
                Log.w(TAG, "Drawable not found: " + drawableId);
                return BitmapDescriptorFactory.defaultMarker();
            }

            vectorDrawable.setBounds(0, 0, MARKER_ICON_SIZE, MARKER_ICON_SIZE);
            Bitmap bitmap = Bitmap.createBitmap(MARKER_ICON_SIZE, MARKER_ICON_SIZE, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create bitmap descriptor", e);
            return BitmapDescriptorFactory.defaultMarker();
        }
    }

    /**
     * Calculate distance between two locations in kilometers
     */
    public static double calculateDistance(@NonNull LatLng point1, @NonNull LatLng point2) {
        float[] results = new float[1];
        Location.distanceBetween(
                point1.latitude, point1.longitude,
                point2.latitude, point2.longitude,
                results
        );
        double distanceKm = results[0] / 1000.0; // Convert to kilometers
        Log.d(TAG, String.format(Locale.US, "Distance between %s and %s: %.2f km",
                point1, point2, distanceKm));
        return distanceKm;
    }

    /**
     * Format distance for display
     */
    @NonNull
    public static String formatDistance(double distanceKm) {
        String formatted;
        if (distanceKm < 1) {
            formatted = String.format(Locale.US, "%.0fm", distanceKm * 1000);
        } else {
            formatted = String.format(Locale.US, "%.1fkm", distanceKm);
        }
        Log.d(TAG, "Formatted distance: " + distanceKm + " km -> " + formatted);
        return formatted;
    }

    /**
     * Check if location is within radius
     */
    public static boolean isWithinRadius(@NonNull LatLng center, @NonNull LatLng point, double radiusKm) {
        double distance = calculateDistance(center, point);
        boolean within = distance <= radiusKm;
        Log.d(TAG, String.format(Locale.US, "Point %s is within %.2f km of center %s: %s (distance: %.2f km)",
                point, radiusKm, center, within, distance));
        return within;
    }

    /**
     * Create cluster marker for multiple products in same area
     */
    @NonNull
    public static MarkerOptions createClusterMarker(@NonNull Context context, @NonNull List<Product> products,
                                                    @NonNull LatLng position) {
        int count = products.size();
        Log.d(TAG, "Creating cluster marker for " + count + " products at " + position);

        MarkerOptions marker = new MarkerOptions()
                .position(position)
                .title(count + " products")
                .snippet("Tap to view")
                .icon(getClusterIcon(context, count));

        return marker;
    }

    @NonNull
    private static BitmapDescriptor getClusterIcon(@NonNull Context context, int count) {
        float hue;
        if (count > 5) {
            hue = BitmapDescriptorFactory.HUE_RED;
        } else if (count > 2) {
            hue = BitmapDescriptorFactory.HUE_ORANGE;
        } else {
            hue = BitmapDescriptorFactory.HUE_GREEN;
        }

        Log.d(TAG, "Cluster icon for " + count + " items, hue: " + hue);
        return BitmapDescriptorFactory.defaultMarker(hue);
    }

    /**
     * Generate walking directions URL
     */
    @NonNull
    public static String getWalkingDirectionsUrl(@NonNull LatLng origin, @NonNull LatLng destination) {
        String url = String.format(Locale.US,
                "https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f&travelmode=walking",
                origin.latitude, origin.longitude,
                destination.latitude, destination.longitude);
        Log.d(TAG, "Generated walking directions URL: " + url);
        return url;
    }

    /**
     * Validate if coordinates are reasonable
     */
    public static boolean isValidCoordinate(double lat, double lng) {
        boolean valid = lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
        if (!valid) {
            Log.w(TAG, "Invalid coordinates: lat=" + lat + ", lng=" + lng);
        }
        return valid;
    }
}