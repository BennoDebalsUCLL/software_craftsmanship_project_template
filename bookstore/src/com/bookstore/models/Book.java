package com.bookstore.models;

public class Book {

    private String isbn;
    private String titel;
    private String author;
    private String genre;
    private double price;
    private int stock;
    private int weightGrams;

    public Book() {
    }

    public Book(String isbn, String titel, String author, String genre, double price, int stock, int weightGrams) {
        this.isbn = isbn;
        this.titel = titel;
        this.author = author;
        this.genre = genre;
        this.price = price;
        this.stock = stock;
        this.weightGrams = weightGrams;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(int weightGrams) {
        this.weightGrams = weightGrams;
    }

    // price including belgian vat
    public double getPriceWithVat() {
        return getPrice() * 1.21;
    }

    public String toString() {
        return isbn + " " + titel;
    }
}
