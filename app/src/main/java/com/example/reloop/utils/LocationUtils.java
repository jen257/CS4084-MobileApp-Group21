package com.example.reloop.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

/**
 * [Member A - System Architect]
 * Utility class for handling location-related operations in Reloop application.
 * Provides methods for permission checking, location retrieval, and distance calculations.
 * Designed with precision and reliability for the Map feature.
 */
public class LocationUtils {

    private static final String TAG = "LocationUtils";

    // Location permission request code
    public static final int LOCATION_PERMISSION_CODE = 1003;

    // Default location (Amsterdam as fallback)
    public static final double DEFAULT_LATITUDE = 52.3676;
    public static final double DEFAULT_LONGITUDE = 4.9041;
    public static final String DEFAULT_ADDRESS = "Amsterdam, Netherlands";

    // Earth radius for distance calculations (kilometers)
    public static final double EARTH_RADIUS_KM = 6371.0;

    private static FusedLocationProviderClient fusedLocationClient;

    public static void getCurrentLocation(@NonNull Context context, @NonNull OnSuccessListener<Location> onSuccess) {
        getLastKnownLocation(context, onSuccess, e -> Log.e(TAG, "Location failed", e));
    }

    // ======================== PERMISSION METHODS ========================

    /**
     * Checks if the app has location permissions (fine or coarse).
     */
    public static boolean hasLocationPermission(@NonNull Context context) {
        boolean hasFineLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean hasCoarseLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return hasFineLocation || hasCoarseLocation;
    }

    /**
     * Requests location permissions from the specified activity.
     */
    public static void requestLocationPermission(@NonNull Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // TODO: Show explanation dialog to the user
            Log.d(TAG, "Should show location permission rationale");
        }

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(activity, permissions, LOCATION_PERMISSION_CODE);
    }

    /**
     * Checks if all required location permissions are granted.
     */
    public static boolean hasAllLocationPermissions(@NonNull Context context) {
        boolean hasFineLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean hasCoarseLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return hasFineLocation && hasCoarseLocation;
    }

    // ======================== LOCATION SERVICES ========================

    /**
     * Checks if location services are enabled on the device.
     */
    public static boolean isLocationEnabled(@NonNull Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "Error checking location services", e);
            return false;
        }
    }

    /**
     * Initializes the FusedLocationProviderClient if not already initialized.
     */
    public static void initialize(@NonNull Context context) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }
    }

    /**
     * Retrieves the last known location of the device.
     * Uses FusedLocationProviderClient for optimal battery usage and accuracy.
     */
    public static void getLastKnownLocation(@NonNull Context context,
                                            @NonNull OnSuccessListener<Location> onSuccess,
                                            @NonNull OnFailureListener onFailure) {
        initialize(context);

        if (!hasLocationPermission(context)) {
            onFailure.onFailure(new SecurityException("Location permission not granted"));
            return;
        }

        try {
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(onSuccess)
                    .addOnFailureListener(onFailure);
        } catch (SecurityException e) {
            onFailure.onFailure(e);
        }
    }

    // ======================== LOCATION OBJECT MANAGEMENT ========================

    /**
     * Creates a custom Location object from coordinates and address.
     */
    @NonNull
    public static com.example.reloop.models.Location createReloopLocation(double latitude, double longitude, String addressName) {
        return new com.example.reloop.models.Location(latitude, longitude, addressName != null ? addressName : "Unknown Location");
    }

    /**
     * Creates a default Location object (Amsterdam) as fallback.
     */
    @NonNull
    public static com.example.reloop.models.Location getDefaultLocation() {
        return new com.example.reloop.models.Location(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_ADDRESS);
    }

    // ======================== DISTANCE CALCULATIONS ========================

    /**
     * Calculates distance between two geographical points using Haversine formula.
     *
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance in kilometers between the two points
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        if (!isValidCoordinates(lat1, lng1) || !isValidCoordinates(lat2, lng2)) {
            return -1.0;
        }

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates distance between two Reloop Location objects.
     *
     * @param loc1 First Location object
     * @param loc2 Second Location object
     * @return Distance in kilometers between the two locations, or -1 if invalid
     */
    public static double calculateDistance(com.example.reloop.models.Location loc1, com.example.reloop.models.Location loc2) {
        if (loc1 == null || loc2 == null) {
            return -1.0;
        }

        return calculateDistance(loc1.getLatitude(), loc1.getLongitude(),
                loc2.getLatitude(), loc2.getLongitude());
    }

    /**
     * Formats distance for display in the UI.
     * Shows meters for distances less than 1km, kilometers for longer distances.
     *
     * @param distanceKm Distance in kilometers
     * @return Formatted distance string (e.g., "150m", "2.5km", "Unknown")
     */
    @NonNull
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 0) {
            return "Unknown";
        }

        if (distanceKm < 0.001) { // Less than 1 meter
            return "<1m";
        } else if (distanceKm < 1.0) {
            int meters = (int) (distanceKm * 1000);
            return meters + "m";
        } else {
            return String.format(Locale.getDefault(), "%.1fkm", distanceKm);
        }
    }

    /**
     * Formats distance with custom precision.
     */
    @NonNull
    public static String formatDistance(double distanceKm, int decimalPlaces) {
        if (distanceKm < 0) {
            return "Unknown";
        }

        if (distanceKm < 0.001) {
            return "<1m";
        } else if (distanceKm < 1.0) {
            int meters = (int) (distanceKm * 1000);
            return meters + "m";
        } else {
            return String.format(Locale.getDefault(), "%." + decimalPlaces + "fkm", distanceKm);
        }
    }

    // ======================== VALIDATION METHODS ========================

    /**
     * Validates if coordinates are within reasonable geographical bounds.
     *
     * @param latitude Latitude to validate (-90 to 90)
     * @param longitude Longitude to validate (-180 to 180)
     * @return true if coordinates are valid, false otherwise
     */
    public static boolean isValidCoordinates(double latitude, double longitude) {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude) &&
                latitude >= -90.0 && latitude <= 90.0 &&
                longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Validates if a Reloop Location object has valid coordinates.
     *
     * @param location Location object to validate
     * @return true if location has valid coordinates, false otherwise
     */
    public static boolean isValidLocation(com.example.reloop.models.Location location) {
        if (location == null) {
            return false;
        }
        return isValidCoordinates(location.getLatitude(), location.getLongitude());
    }

    // ======================== UTILITY METHODS ========================

    /**
     * Converts Android Location to Reloop Location model.
     */
    @NonNull
    public static com.example.reloop.models.Location convertToReloopLocation(Location androidLocation, String addressName) {
        if (androidLocation == null) {
            return getDefaultLocation();
        }
        return new com.example.reloop.models.Location(androidLocation.getLatitude(), androidLocation.getLongitude(),
                addressName != null ? addressName : "Converted Location");
    }

    /**
     * Checks if two locations are within specified distance threshold.
     *
     * @param loc1 First location
     * @param loc2 Second location
     * @param thresholdKm Distance threshold in kilometers
     * @return true if distance is within threshold, false otherwise
     */
    public static boolean isWithinDistance(com.example.reloop.models.Location loc1, com.example.reloop.models.Location loc2, double thresholdKm) {
        if (!isValidLocation(loc1) || !isValidLocation(loc2)) {
            return false;
        }

        double distance = calculateDistance(loc1, loc2);
        return distance >= 0 && distance <= thresholdKm;
    }

    /**
     * Cleans up resources to prevent memory leaks.
     */
    public static void cleanup() {
        fusedLocationClient = null;
    }

    /**
     * Example usage method to demonstrate the class functionality.
     */
    public static void demonstrateUsage(@NonNull Context context) {
        // Check location permission
        if (hasLocationPermission(context)) {
            Log.d(TAG, "Location permission granted");

            // Check if location services are enabled
            if (isLocationEnabled(context)) {
                Log.d(TAG, "Location services are enabled");

                // Get last known location safely
                getLastKnownLocation(context,
                        new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    Log.d(TAG, "Location retrieved: " + location.getLatitude() + ", " + location.getLongitude());

                                    // Calculate distance to default location
                                    com.example.reloop.models.Location defaultLoc = getDefaultLocation();
                                    com.example.reloop.models.Location currentLoc = convertToReloopLocation(location, "Current Location");
                                    double distance = calculateDistance(currentLoc, defaultLoc);
                                    String distanceStr = formatDistance(distance);

                                    Log.d(TAG, "Distance to Amsterdam: " + distanceStr);
                                } else {
                                    Log.w(TAG, "No location available");
                                }
                            }
                        },
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to get location", e);
                            }
                        }
                );
            } else {
                Log.w(TAG, "Location services are disabled");
            }
        } else {
            Log.w(TAG, "Location permission not granted");
        }
    }
}