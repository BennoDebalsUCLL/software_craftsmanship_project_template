package com.bookstore.dao;

import com.bookstore.models.StockItem;
import java.util.ArrayList;
import java.util.List;

public class StockDao {

    private Database db = Database.getInstance();

    public void save(StockItem item) {
        db.save("stock", item.getIsbn(), item);
    }

    public StockItem findByIsbn(String isbn) {
        return (StockItem) db.find("stock", isbn);
    }

    public List<StockItem> findAll() {
        List<StockItem> result = new ArrayList<StockItem>();
        List<Object> rows = db.findAll("stock");
        for (int i = 0; i < rows.size(); i++) {
            result.add((StockItem) rows.get(i));
        }
        return result;
    }
}
