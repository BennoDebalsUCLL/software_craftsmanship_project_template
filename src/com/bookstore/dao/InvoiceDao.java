package com.bookstore.dao;

import com.bookstore.models.Invoice;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDao {

    private Database db = Database.getInstance();

    public void save(Invoice invoice) {
        db.save("invoices", invoice.getId(), invoice);
    }

    public Invoice findById(String id) {
        return (Invoice) db.find("invoices", id);
    }

    public Invoice findByOrderId(String orderId) {
        List<Object> rows = db.findAll("invoices");
        for (int i = 0; i < rows.size(); i++) {
            Invoice invoice = (Invoice) rows.get(i);
            if (invoice.getOrderId().equals(orderId)) {
                return invoice;
            }
        }
        return null;
    }

    public List<Invoice> findAll() {
        List<Invoice> result = new ArrayList<Invoice>();
        List<Object> rows = db.findAll("invoices");
        for (int i = 0; i < rows.size(); i++) {
            result.add((Invoice) rows.get(i));
        }
        return result;
    }

    private static int sequence = 0;

    public String nextId() {
        sequence++;
        return "INV-" + sequence;
    }
}
