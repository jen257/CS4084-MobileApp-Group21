package com.example.reloop.utils;

import com.example.reloop.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class containing search algorithms and filtering logic for products.
 */
public class SearchHelper {

    /**
     * Filters a list of products based on query, category, and price range.
     *
     * @param originalList The base list of products to filter from.
     * @param query        The search text (matches title or description).
     * @param category     The specific category to filter by (null for all).
     * @param minPrice     The minimum price bound (null if none).
     * @param maxPrice     The maximum price bound (null if none).
     * @return A new list containing products that match all provided criteria.
     */
    public static List<Product> filterProducts(List<Product> originalList, String query, String category, Double minPrice, Double maxPrice) {
        List<Product> filteredList = new ArrayList<>();

        if (originalList == null || originalList.isEmpty()) {
            return filteredList;
        }

        String lowerCaseQuery = (query != null) ? query.toLowerCase() : "";

        for (Product product : originalList) {
            boolean matchesQuery = true;
            boolean matchesCategory = true;
            boolean matchesPrice = true;

            // 1. Evaluate search query
            if (!lowerCaseQuery.isEmpty()) {
                String title = product.getTitle() != null ? product.getTitle().toLowerCase() : "";
                String desc = product.getDescription() != null ? product.getDescription().toLowerCase() : "";

                if (!title.contains(lowerCaseQuery) && !desc.contains(lowerCaseQuery)) {
                    matchesQuery = false;
                }
            }

            // 2. Evaluate category filter
            if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
                String prodCategory = product.getCategory();
                if (prodCategory == null || !prodCategory.equalsIgnoreCase(category)) {
                    matchesCategory = false;
                }
            }

            // 3. Evaluate price range bounds
            try {
                if (product.getPrice() != null && !product.getPrice().isEmpty()) {
                    double price = Double.parseDouble(product.getPrice());

                    if (minPrice != null && price < minPrice) {
                        matchesPrice = false;
                    }
                    if (maxPrice != null && price > maxPrice) {
                        matchesPrice = false;
                    }
                }
            } catch (NumberFormatException e) {
                // If price parsing fails, we skip price filtering for this specific item
            }

            // If all conditions are met, add to result list
            if (matchesQuery && matchesCategory && matchesPrice) {
                filteredList.add(product);
            }
        }

        return filteredList;
    }
}