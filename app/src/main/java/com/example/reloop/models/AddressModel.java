package com.example.reloop.models;

public class AddressModel {
    public String key; // The unique ID from Firebase
    public String country, county, city, street, postcode;

    // Required empty constructor for Firebase
    public AddressModel() {}

    public AddressModel(String key, String country, String county, String city, String street, String postcode) {
        this.key = key;
        this.country = country;
        this.county = county;
        this.city = city;
        this.street = street;
        this.postcode = postcode;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isEmpty()) sb.append(street).append(", ");
        if (city != null && !city.isEmpty()) sb.append(city).append(", ");
        if (county != null && !county.isEmpty()) sb.append(county).append(", ");
        if (country != null && !country.isEmpty()) sb.append(country);

        String result = sb.toString();
        if (result.endsWith(", ")) result = result.substring(0, result.length() - 2);
        return result;
    }
}