package com.bookstore.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps a history of price changes. Not used anywhere yet, but we will need it
 * once the pricing dashboard is built.
 */
public class PriceHistoryTracker {

    private Map<String, List<Double>> history = new HashMap<String, List<Double>>();

    public void record(String isbn, double price) {
        if (!history.containsKey(isbn)) {
            history.put(isbn, new ArrayList<Double>());
        }
        history.get(isbn).add(price);
    }

    public List<Double> getHistory(String isbn) {
        return history.get(isbn);
    }

    public double getAveragePrice(String isbn) {
        List<Double> prices = history.get(isbn);
        if (prices == null || prices.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < prices.size(); i++) {
            sum = sum + prices.get(i);
        }
        return sum / prices.size();
    }
}
