package com.example.reloop.shared.views;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.reloop.R;
import com.example.reloop.models.Product;
import com.example.reloop.utils.DistanceFormatter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Locale;

/**
 * Custom info window for product markers on the map
 */
public class CustomMapMarkerView implements GoogleMap.InfoWindowAdapter {

    private final View windowView;
    private final Context context;
    private Location currentLocation;

    public CustomMapMarkerView(@NonNull Context context) {
        this.context = context;
        this.windowView = LayoutInflater.from(context).inflate(
                R.layout.layout_custom_marker,
                null,
                false
        );
    }

    public void setCurrentLocation(@Nullable Location location) {
        this.currentLocation = location;
    }

    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        renderView(marker, windowView);
        return windowView;
    }

    @Override
    public View getInfoContents(@NonNull Marker marker) {
        return null;
    }

    private void renderView(@NonNull Marker marker, @NonNull View view) {
        Object tag = marker.getTag();
        if (!(tag instanceof Product)) {
            return;
        }

        Product product = (Product) tag;

        ImageView imageView = view.findViewById(R.id.ivProductImage);
        TextView tvTitle = view.findViewById(R.id.tvProductTitle);
        TextView tvPrice = view.findViewById(R.id.tvProductPrice);
        TextView tvCategory = view.findViewById(R.id.tvProductCategory);
        TextView tvDistance = view.findViewById(R.id.tvDistance);
        View statusIndicator = view.findViewById(R.id.viewStatus);

        // Set product data
        tvTitle.setText(product.getTitle());
        tvPrice.setText(String.format(Locale.getDefault(), "€%.2f", product.getPriceAsDouble()));
        tvCategory.setText(product.getCategory());

        // Set availability status
        if (product.isAvailable()) {
            statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
            tvDistance.setText(context.getString(R.string.available));
        } else {
            statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
            tvDistance.setText(context.getString(R.string.sold));
        }

        // Calculate and display distance if current location is available
        if (currentLocation != null && product.getLocation() != null) {
            double distance = calculateDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    product.getLocation().getLatitude(), product.getLocation().getLongitude()
            );
            String distanceText = DistanceFormatter.format(distance);
            tvDistance.setText(String.format(Locale.getDefault(), "%s away", distanceText));
        }

        // Load product image
        loadProductImage(product.getImageUrl(), imageView);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0] / 1000.0;
    }

    private void loadProductImage(@Nullable String imageUrl, @NonNull ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_product_placeholder);
        }
    }
}