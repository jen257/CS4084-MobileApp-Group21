package com.example.reloop.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.reloop.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * MapService for handling Google Maps API requests
 * Provides methods for directions, geocoding, and reverse geocoding
 */
public class MapService {

    private static final String TAG = "MapService";
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";
    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final int TIMEOUT_SECONDS = 30;
    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int THREAD_POOL_SIZE = 3;

    private final OkHttpClient httpClient;
    private final ExecutorService executorService;
    private final String apiKey;

    // Fix: Make it non-static to allow usage
    private static MapService instance;

    /**
     * Initialize MapService with context
     */
    public MapService(@NonNull Context context) {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        String googleMapsKey = "";
        try {
            googleMapsKey = context.getString(R.string.google_maps_api_key);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load Google Maps API key from resources", e);
        }

        this.apiKey = googleMapsKey;

        if (this.apiKey == null || this.apiKey.isEmpty() || this.apiKey.contains("YOUR_API_KEY")) {
            Log.w(TAG, "Google Maps API key is not properly configured");
        } else {
            Log.d(TAG, "MapService initialized with API key");
        }
    }

    // Fix: Add static getInstance method
    public static synchronized MapService getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new MapService(context);
        }
        return instance;
    }

    /**
     * Get walking directions between two points
     */
    public void getDirections(@NonNull LatLng origin, @NonNull LatLng destination,
                              @NonNull DirectionsCallback callback) {
        if (!validateApiKey(callback)) return;

        // Fix: Use Locale.US for consistent formatting
        String url = String.format(Locale.US, "%s?origin=%f,%f&destination=%f,%f&mode=walking&key=%s",
                DIRECTIONS_API_URL,
                origin.latitude, origin.longitude,
                destination.latitude, destination.longitude,
                apiKey);

        Log.d(TAG, "Getting directions from: " + origin + " to: " + destination);

        executeApiCallWithRetry(url, response -> {
            try {
                List<Route> routes = parseDirectionsResponse(response);
                if (routes.isEmpty()) {
                    callback.onError("No routes found");
                } else {
                    callback.onDirectionsReceived(routes);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse directions response", e);
                callback.onError("Failed to parse directions: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error parsing directions", e);
                callback.onError("Unexpected error: " + e.getMessage());
            }
        }, error -> {
            Log.e(TAG, "Directions API error: " + error);
            callback.onError("Directions API error: " + error);
        });
    }

    /**
     * Convert coordinates to address (reverse geocoding)
     */
    public void reverseGeocode(@NonNull LatLng location, @NonNull GeocodingCallback callback) {
        if (!validateApiKey(callback)) return;

        // Fix: Use Locale.US for consistent formatting
        String url = String.format(Locale.US, "%s?plating=%f,%f&key=%s",
                GEOCODING_API_URL,
                location.latitude, location.longitude,
                apiKey);

        Log.d(TAG, "Reverse geocoding location: " + location);

        executeApiCallWithRetry(url, response -> {
            try {
                String address = parseGeocodingResponse(response);
                if (address != null && !address.isEmpty()) {
                    callback.onAddressReceived(address);
                } else {
                    callback.onError("No address found for the given coordinates");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse geocoding response", e);
                callback.onError("Failed to parse geocoding response: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error parsing geocoding", e);
                callback.onError("Unexpected error: " + e.getMessage());
            }
        }, error -> {
            Log.e(TAG, "Reverse geocoding API error: " + error);
            callback.onError("Reverse geocoding API error: " + error);
        });
    }

    /**
     * Convert address to coordinates (forward geocoding)
     */
    public void geocodeAddress(@NonNull String address, @NonNull GeocodingCallback callback) {
        if (!validateApiKey(callback)) return;

        if (address.trim().isEmpty()) {
            callback.onError("Address is empty");
            return;
        }

        String encodedAddress = address.trim().replace(" ", "+");
        String url = String.format("%s?address=%s&key=%s",
                GEOCODING_API_URL, encodedAddress, apiKey);

        Log.d(TAG, "Geocoding address: " + address);

        executeApiCallWithRetry(url, response -> {
            try {
                LatLng location = parseForwardGeocodingResponse(response);
                if (location != null) {
                    callback.onLocationReceived(location);
                } else {
                    callback.onError("No location found for the given address");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse geocoding response", e);
                callback.onError("Failed to parse geocoding response: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error parsing geocoding", e);
                callback.onError("Unexpected error: " + e.getMessage());
            }
        }, error -> {
            Log.e(TAG, "Geocoding API error: " + error);
            callback.onError("Geocoding API error: " + error);
        });
    }

    /**
     * Validate API key before making requests
     */
    private boolean validateApiKey(@NonNull ErrorCallback callback) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("YOUR_API_KEY")) {
            callback.onError("Google Maps API key is not configured properly");
            return false;
        }
        return true;
    }

    /**
     * Execute API call with retry mechanism
     */
    private void executeApiCallWithRetry(@NonNull String url,
                                         @NonNull ApiSuccessCallback successCallback,
                                         @NonNull ApiErrorCallback errorCallback) {
        executeApiCallWithRetry(url, successCallback, errorCallback, 0);
    }

    private void executeApiCallWithRetry(@NonNull String url,
                                         @NonNull ApiSuccessCallback successCallback,
                                         @NonNull ApiErrorCallback errorCallback,
                                         int retryCount) {
        executorService.execute(() -> {
            try {
                executeApiCall(url, successCallback, errorCallback);
            } catch (Exception e) {
                if (retryCount < MAX_RETRIES) {
                    Log.w(TAG, String.format(Locale.US, "Retry %d/%d for URL: %s",
                            retryCount + 1, MAX_RETRIES, url));

                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        errorCallback.onError("Request interrupted");
                        return;
                    }

                    executeApiCallWithRetry(url, successCallback, errorCallback, retryCount + 1);
                } else {
                    errorCallback.onError("Failed after " + MAX_RETRIES + " retries: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Execute single API call
     */
    private void executeApiCall(@NonNull String url,
                                @NonNull ApiSuccessCallback successCallback,
                                @NonNull ApiErrorCallback errorCallback) {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = "HTTP " + response.code() + " for URL: " + url;
                Log.e(TAG, errorMsg);
                errorCallback.onError(errorMsg);
                return;
            }

            if (response.body() == null) {
                errorCallback.onError("Empty response body");
                return;
            }

            String responseBody = response.body().string();
            if (responseBody == null || responseBody.isEmpty()) {
                errorCallback.onError("Empty response body");
                return;
            }

            successCallback.onSuccess(responseBody);

        } catch (IOException e) {
            Log.e(TAG, "Network error for URL: " + url, e);
            errorCallback.onError("Network error: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error for URL: " + url, e);
            errorCallback.onError("Unexpected error: " + e.getMessage());
        }
    }

    @NonNull
    private List<Route> parseDirectionsResponse(@NonNull String jsonResponse) throws JSONException {
        JSONObject json = new JSONObject(jsonResponse);

        String status = json.getString("status");
        if (!"OK".equals(status)) {
            String errorMessage = json.optString("error_message", "Unknown error");
            throw new JSONException("API returned status: " + status + " - " + errorMessage);
        }

        JSONArray routesArray = json.optJSONArray("routes");
        if (routesArray == null || routesArray.length() == 0) {
            return Collections.emptyList();
        }

        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < routesArray.length(); i++) {
            JSONObject routeJson = routesArray.getJSONObject(i);
            Route route = new Route();

            // Parse overview polyline
            JSONObject overviewPolyline = routeJson.optJSONObject("overview_polyline");
            if (overviewPolyline != null) {
                route.setPolyline(overviewPolyline.optString("points"));
            }

            // Parse legs
            JSONArray legsArray = routeJson.optJSONArray("legs");
            if (legsArray != null && legsArray.length() > 0) {
                JSONObject leg = legsArray.getJSONObject(0);

                JSONObject distance = leg.optJSONObject("distance");
                if (distance != null) {
                    route.setDistance(distance.optString("text"));
                }

                JSONObject duration = leg.optJSONObject("duration");
                if (duration != null) {
                    route.setDuration(duration.optString("text"));
                }
            }

            routes.add(route);
        }

        return routes;
    }

    @Nullable
    private String parseGeocodingResponse(@NonNull String jsonResponse) throws JSONException {
        JSONObject json = new JSONObject(jsonResponse);

        String status = json.getString("status");
        if (!"OK".equals(status)) {
            String errorMessage = json.optString("error_message", "Unknown error");
            throw new JSONException("API returned status: " + status + " - " + errorMessage);
        }

        JSONArray results = json.optJSONArray("results");
        if (results != null && results.length() > 0) {
            return results.getJSONObject(0).optString("formatted_address", "");
        }

        return null;
    }

    @Nullable
    private LatLng parseForwardGeocodingResponse(@NonNull String jsonResponse) throws JSONException {
        JSONObject json = new JSONObject(jsonResponse);

        String status = json.getString("status");
        if (!"OK".equals(status)) {
            String errorMessage = json.optString("error_message", "Unknown error");
            throw new JSONException("API returned status: " + status + " - " + errorMessage);
        }

        JSONArray results = json.optJSONArray("results");
        if (results != null && results.length() > 0) {
            JSONObject geometry = results.getJSONObject(0).optJSONObject("geometry");
            if (geometry != null) {
                JSONObject location = geometry.optJSONObject("location");
                if (location != null) {
                    double lat = location.optDouble("lat");
                    double lng = location.optDouble("lng");

                    // Validate coordinates
                    if (isValidLatitude(lat) && isValidLongitude(lng)) {
                        return new LatLng(lat, lng);
                    } else {
                        throw new JSONException("Invalid coordinates: lat=" + lat + ", lng=" + lng);
                    }
                }
            }
        }

        return null;
    }

    private boolean isValidLatitude(double latitude) {
        return latitude >= -90 && latitude <= 90;
    }

    private boolean isValidLongitude(double longitude) {
        return longitude >= -180 && longitude <= 180;
    }

    /**
     * Clean up resources
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // Fix: Make getters public to allow usage
    public String getPolyline(@NonNull Route route) {
        return route.getPolyline();
    }

    public String getDistance(@NonNull Route route) {
        return route.getDistance();
    }

    public String getDuration(@NonNull Route route) {
        return route.getDuration();
    }

    // 回调接口
    public interface DirectionsCallback extends ErrorCallback {
        void onDirectionsReceived(@NonNull List<Route> routes);
    }

    public interface GeocodingCallback extends ErrorCallback {
        void onAddressReceived(@NonNull String address);
        void onLocationReceived(@NonNull LatLng location);
    }

    interface ApiSuccessCallback {
        void onSuccess(@NonNull String response);
    }

    interface ApiErrorCallback {
        void onError(@NonNull String error);
    }

    interface ErrorCallback {
        void onError(@NonNull String error);
    }

    /**
     * Route data class
     */
    public static class Route {
        private String polyline = "";
        private String distance = "";
        private String duration = "";

        public String getPolyline() {
            return polyline;
        }

        public void setPolyline(@NonNull String polyline) {
            this.polyline = polyline;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(@NonNull String distance) {
            this.distance = distance;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(@NonNull String duration) {
            this.duration = duration;
        }

        @NonNull
        @Override
        public String toString() {
            return "Route{" +
                    "distance='" + distance + '\'' +
                    ", duration='" + duration + '\'' +
                    '}';
        }
    }
}