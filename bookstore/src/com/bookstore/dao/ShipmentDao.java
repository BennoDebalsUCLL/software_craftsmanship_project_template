package com.bookstore.dao;

import com.bookstore.models.Shipment;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDao {

    private Database db = Database.getInstance();

    public void save(Shipment shipment) {
        db.save("shipments", shipment.getId(), shipment);
    }

    public Shipment findById(String id) {
        return (Shipment) db.find("shipments", id);
    }

    public Shipment findByOrderId(String orderId) {
        List<Object> rows = db.findAll("shipments");
        for (int i = 0; i < rows.size(); i++) {
            Shipment shipment = (Shipment) rows.get(i);
            if (shipment.getOrderId().equals(orderId)) {
                return shipment;
            }
        }
        return null;
    }

    public List<Shipment> findAll() {
        List<Shipment> result = new ArrayList<Shipment>();
        List<Object> rows = db.findAll("shipments");
        for (int i = 0; i < rows.size(); i++) {
            result.add((Shipment) rows.get(i));
        }
        return result;
    }

    private static int sequence = 0;

    public String nextId() {
        sequence++;
        return "SHIP-" + sequence;
    }
}
