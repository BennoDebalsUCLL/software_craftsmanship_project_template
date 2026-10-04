package com.bookstore.models;

public class SeasonalDiscountedBook extends DiscountedBook {

    private String season;

    public SeasonalDiscountedBook(String isbn, String titel, String author, String genre, double price, int stock,
            int weightGrams, double discountPercentage, String season) {
        super(isbn, titel, author, genre, price, stock, weightGrams, discountPercentage);
        this.season = season;
    }

    public String getSeason() {
        return season;
    }

    public double getPrice() {
        if (season.equals("SUMMER")) {
            return super.getPrice() * 0.95;
        }
        return super.getPrice();
    }
}
