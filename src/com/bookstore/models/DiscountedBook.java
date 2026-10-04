package com.bookstore.models;

public class DiscountedBook extends Book {

    private double discountPercentage;

    public DiscountedBook(String isbn, String titel, String author, String genre, double price, int stock,
            int weightGrams, double discountPercentage) {
        super(isbn, titel, author, genre, price, stock, weightGrams);
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getPrice() {
        return super.getPrice() - (super.getPrice() * discountPercentage / 100.0);
    }
}
