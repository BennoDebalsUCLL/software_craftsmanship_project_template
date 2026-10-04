package com.bookstore.services;

import com.bookstore.dao.BookDao;
import com.bookstore.models.Book;
import com.bookstore.util.Logger;
import com.bookstore.util.Utils;
import java.util.ArrayList;
import java.util.List;

public class CatalogService {

    private BookDao bookDao = new BookDao();
    private InventoryService inventoryService = new InventoryService();
    private Logger logger = Logger.getInstance();

    public void addBook(Book book) {
        if (!Utils.isValidIsbn(book.getIsbn())) {
            throw new RuntimeException("invalid isbn: " + book.getIsbn());
        }
        if (book.getPrice() < 0) {
            throw new RuntimeException("price cannot be negative");
        }
        bookDao.save(book);
        inventoryService.createStockItem(book.getIsbn(), book.getStock());
        logger.log("added article " + book.getIsbn() + " to the catalog");
    }

    public Book getBook(String isbn) {
        return bookDao.findByIsbn(isbn);
    }

    public List<Book> getAllBooks() {
        return bookDao.findAll();
    }

    public List<Book> search(String titlePart, String genre, double maxPrice) {
        List<Book> result = new ArrayList<Book>();
        List<Book> all = bookDao.findAll();
        for (int i = 0; i < all.size(); i++) {
            Book book = all.get(i);
            boolean matches = true;
            if (titlePart != null && titlePart.length() > 0) {
                if (!book.getTitel().toLowerCase().contains(titlePart.toLowerCase())) {
                    matches = false;
                }
            }
            if (genre != null && genre.length() > 0) {
                if (!book.getGenre().equals(genre)) {
                    matches = false;
                }
            }
            if (maxPrice > 0) {
                if (book.getPrice() > maxPrice) {
                    matches = false;
                }
            }
            if (matches) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> searchByAuthor(String authorPart) {
        List<Book> result = new ArrayList<Book>();
        List<Book> all = bookDao.findAll();
        for (int i = 0; i < all.size(); i++) {
            Book book = all.get(i);
            if (book.getAuthor().toLowerCase().contains(authorPart.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    public double getPriceWithVat(String isbn, String country) {
        Book book = bookDao.findByIsbn(isbn);
        double vat;
        if (country.equals("BE")) {
            vat = 0.21;
        } else if (country.equals("NL")) {
            vat = 0.21;
        } else if (country.equals("LU")) {
            vat = 0.17;
        } else if (country.equals("FR")) {
            vat = 0.20;
        } else if (country.equals("DE")) {
            vat = 0.19;
        } else if (country.equals("US")) {
            vat = 0.07;
        } else {
            vat = 0.21;
        }
        return Utils.round2(book.getPrice() * (1 + vat));
    }

    public void decreaseStock(String isbn, int amount) {
        Book book = bookDao.findByIsbn(isbn);
        book.setStock(book.getStock() - amount);
        bookDao.save(book);
    }
}
