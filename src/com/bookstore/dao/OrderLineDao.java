package com.bookstore.dao;

import com.bookstore.models.OrderLine;
import java.util.ArrayList;
import java.util.List;

public class OrderLineDao {

    private Database db = Database.getInstance();

    public void save(OrderLine line) {
        db.save("order_lines", line.getId(), line);
    }

    public OrderLine findById(String id) {
        return (OrderLine) db.find("order_lines", id);
    }

    public List<OrderLine> findByOrderId(String orderId) {
        List<OrderLine> result = new ArrayList<OrderLine>();
        List<Object> rows = db.findAll("order_lines");
        for (int i = 0; i < rows.size(); i++) {
            OrderLine line = (OrderLine) rows.get(i);
            if (line.getOrderId().equals(orderId)) {
                result.add(line);
            }
        }
        return result;
    }

    public List<OrderLine> findAll() {
        List<OrderLine> result = new ArrayList<OrderLine>();
        List<Object> rows = db.findAll("order_lines");
        for (int i = 0; i < rows.size(); i++) {
            result.add((OrderLine) rows.get(i));
        }
        return result;
    }

    public void updateQuantity(String lineId, int quantity) {
        OrderLine line = findById(lineId);
        line.setQuantity(quantity);
        line.setLineTotal(line.getUnitPrice() * quantity);
        db.save("order_lines", lineId, line);
    }

    private static int sequence = 0;

    public String nextId() {
        sequence++;
        return "LINE-" + sequence;
    }
}
