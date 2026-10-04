package com.bookstore.models;

public class CartItem {

    public String isbn;
    public int quantity;
    public double unitPrice;

    public CartItem(String isbn, int quantity, double unitPrice) {
        this.isbn = isbn;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
