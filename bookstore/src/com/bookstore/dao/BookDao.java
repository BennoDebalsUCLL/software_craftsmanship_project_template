package com.bookstore.dao;

import com.bookstore.models.Book;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    private Database db = Database.getInstance();

    public void save(Book book) {
        db.save("books", book.getIsbn(), book);
    }

    public Book findByIsbn(String isbn) {
        return (Book) db.find("books", isbn);
    }

    public List<Book> findAll() {
        List<Book> result = new ArrayList<Book>();
        List<Object> rows = db.findAll("books");
        for (int i = 0; i < rows.size(); i++) {
            result.add((Book) rows.get(i));
        }
        return result;
    }

    public List<Book> findByGenre(String genre) {
        List<Book> result = new ArrayList<Book>();
        List<Book> all = findAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getGenre().equals(genre)) {
                result.add(all.get(i));
            }
        }
        return result;
    }

    public void delete(String isbn) {
        db.delete("books", isbn);
    }
}
