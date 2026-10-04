package com.bookstore.dao;

import com.bookstore.models.Customer;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    private Database db = Database.getInstance();

    public void save(Customer customer) {
        db.save("customers", customer.getId(), customer);
    }

    public Customer findById(String id) {
        return (Customer) db.find("customers", id);
    }

    public Customer findByEmail(String email) {
        List<Customer> all = findAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getEmail().equals(email)) {
                return all.get(i);
            }
        }
        return null;
    }

    public List<Customer> findAll() {
        List<Customer> result = new ArrayList<Customer>();
        List<Object> rows = db.findAll("customers");
        for (int i = 0; i < rows.size(); i++) {
            result.add((Customer) rows.get(i));
        }
        return result;
    }

    private static int sequence = 0;

    public String nextId() {
        sequence++;
        return "CUST-" + sequence;
    }
}
