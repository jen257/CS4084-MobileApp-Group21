package com.example.reloop.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * [Member A - System Architect]
 * Utility class to handle Android Runtime Permissions.
 * Essential for accessing camera and gallery (Member C's Post feature).
 */
public class PermissionHelper {

    public static final int GALLERY_PERMISSION_CODE = 1001;
    public static final int CAMERA_PERMISSION_CODE = 1002;
    public static final int LOCATION_PERMISSION_CODE = 1003;

    /**
     * Checks if the app has permission to access the gallery.
     * Note: Handles different Android versions (Tiramisu and above use different permissions).
     */
    public static boolean hasGalleryPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Requests gallery permissions from the specified activity.
     */
    public static void requestGalleryPermission(Activity activity) {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission}, GALLERY_PERMISSION_CODE);
    }

    /**
     * Checks and requests camera permission.
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }
}