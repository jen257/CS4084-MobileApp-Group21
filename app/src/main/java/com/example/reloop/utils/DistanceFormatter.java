package com.example.reloop.utils;

import androidx.annotation.NonNull;
import java.util.Locale;

public class DistanceFormatter {

    @NonNull
    public static String format(double distanceKm) {
        if (distanceKm < 1) {
            return String.format(Locale.US, "%.0fm", distanceKm * 1000);
        } else if (distanceKm < 10) {
            return String.format(Locale.US, "%.1fkm", distanceKm);
        } else {
            return String.format(Locale.US, "%.0fkm", distanceKm);
        }
    }
}