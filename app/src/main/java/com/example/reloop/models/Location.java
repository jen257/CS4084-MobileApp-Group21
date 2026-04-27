package com.example.reloop.models;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

/**
 * Data model representing a geographical location
 */
@IgnoreExtraProperties
public class Location implements Serializable {

    public double latitude;
    public double longitude;
    public String addressName;

    public Location() {
        // Default constructor required for Firebase
    }

    public Location(double latitude, double longitude, String addressName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressName = addressName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", addressName='" + addressName + '\'' +
                '}';
    }
}