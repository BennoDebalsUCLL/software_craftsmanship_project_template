package com.bookstore.services;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.StockDao;
import com.bookstore.models.Book;
import com.bookstore.models.StockItem;
import com.bookstore.util.Logger;
import com.bookstore.util.Utils;
import java.util.List;

public class InventoryService {

    private StockDao stockDao = new StockDao();
    private BookDao bookDao = new BookDao();
    private Logger logger = Logger.getInstance();

    public void createStockItem(String isbn, int amount) {
        stockDao.save(new StockItem(isbn, amount));
    }

    public void reserve(String isbn, int amount) {
        StockItem item = stockDao.findByIsbn(isbn);
        if (item == null) {
            logger.error("no stock record for " + isbn);
            return;
        }
        if (item.getAantal() - item.getReserved() < amount) {
            throw new RuntimeException("not enough stock");
        }
        item.setReserved(item.getReserved() + amount);
        stockDao.save(item);
    }

    public void release(String isbn, int amount) {
        StockItem item = stockDao.findByIsbn(isbn);
        item.setReserved(item.getReserved() - amount);
        stockDao.save(item);
    }

    public void shipOut(String isbn, int amount) {
        StockItem item = stockDao.findByIsbn(isbn);
        item.setAantal(item.getAantal() - amount);
        item.setReserved(item.getReserved() - amount);
        stockDao.save(item);
    }

    public void restock(String isbn, int amount) {
        StockItem item = stockDao.findByIsbn(isbn);
        if (item == null) {
            createStockItem(isbn, amount);
            return;
        }
        item.setAantal(item.getAantal() + amount);
        stockDao.save(item);
        Book book = bookDao.findByIsbn(isbn);
        book.setStock(book.getStock() + amount);
        bookDao.save(book);
    }

    public boolean isLowStock(String isbn) {
        StockItem item = stockDao.findByIsbn(isbn);
        if (item == null) {
            return true;
        }
        return item.getAantal() < 5;
    }

    public void printStockReport() {
        System.out.println("STOCK REPORT");
        System.out.println(Utils.pad("ISBN", 18) + Utils.pad("CATALOG", 10) + Utils.pad("WAREHOUSE", 12)
                + Utils.pad("RESERVED", 10) + Utils.pad("AVAILABLE", 10));
        List<StockItem> items = stockDao.findAll();
        for (int i = 0; i < items.size(); i++) {
            StockItem item = items.get(i);
            Book book = bookDao.findByIsbn(item.getIsbn());
            String catalogStock = "?";
            if (book != null) {
                catalogStock = String.valueOf(book.getStock());
            }
            System.out.println(Utils.pad(item.getIsbn(), 18) + Utils.pad(catalogStock, 10)
                    + Utils.pad(String.valueOf(item.getAantal()), 12)
                    + Utils.pad(String.valueOf(item.getReserved()), 10)
                    + Utils.pad(String.valueOf(item.getAantal() - item.getReserved()), 10));
        }
    }
}
