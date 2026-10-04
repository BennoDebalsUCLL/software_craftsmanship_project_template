package com.bookstore.dao;

import com.bookstore.models.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    private Database db = Database.getInstance();
    private OrderLineDao lineDao = new OrderLineDao();

    public void save(Order order) {
        db.save("orders", order.getId(), order);
        for (int i = 0; i < order.getLines().size(); i++) {
            lineDao.save(order.getLines().get(i));
        }
    }

    public Order findById(String id) {
        return (Order) db.find("orders", id);
    }

    public List<Order> findAll() {
        List<Order> result = new ArrayList<Order>();
        List<Object> rows = db.findAll("orders");
        for (int i = 0; i < rows.size(); i++) {
            result.add((Order) rows.get(i));
        }
        return result;
    }

    public List<Order> findByCustomerId(String customerId) {
        List<Order> result = new ArrayList<Order>();
        List<Order> all = findAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getCustomer().getId().equals(customerId)) {
                result.add(all.get(i));
            }
        }
        return result;
    }

    public List<Order> findByStatus(String status) {
        List<Order> result = new ArrayList<Order>();
        List<Order> all = findAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getStatus().equals(status)) {
                result.add(all.get(i));
            }
        }
        return result;
    }

    private static int sequence = 0;

    public String nextId() {
        sequence++;
        return "ORD-" + sequence;
    }
}
