package com.bookstore.models;

public class StockItem {

    private String isbn;
    private int aantal;
    private int reserved;

    public StockItem(String isbn, int aantal) {
        this.isbn = isbn;
        this.aantal = aantal;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getAantal() {
        return aantal;
    }

    public void setAantal(int aantal) {
        this.aantal = aantal;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }
}
