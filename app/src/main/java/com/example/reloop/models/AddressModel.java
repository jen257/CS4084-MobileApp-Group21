package com.example.reloop.models;

public class AddressModel {
    public String key;
    public String country, county, city, street, postcode;

    public AddressModel() {}

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