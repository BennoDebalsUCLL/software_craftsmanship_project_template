package com.bookstore.services;

import com.bookstore.dao.ShipmentDao;
import com.bookstore.models.Order;
import com.bookstore.models.Shipment;
import com.bookstore.util.Logger;
import com.bookstore.util.Utils;

public class ShippingService {

    private ShipmentDao shipmentDao = new ShipmentDao();
    private Logger logger = Logger.getInstance();

    public Shipment createShipment(Order order) {
        Shipment shipment = new Shipment();
        shipment.setId(shipmentDao.nextId());
        shipment.setOrderId(order.getId());
        shipment.setStreet(order.getCustomer().getStreet());
        shipment.setHouseNumber(order.getCustomer().getHouseNumber());
        shipment.setZipCode(order.getCustomer().getZipCode());
        shipment.setCity(order.getCustomer().getCity());
        shipment.setCountry(order.getCustomer().getCountry());

        int weight = 0;
        for (int i = 0; i < order.getLines().size(); i++) {
            weight = weight + (order.getLines().get(i).getBook().getWeightGrams()
                    * order.getLines().get(i).getQuantity());
        }
        shipment.setTotalWeightGrams(weight);

        String carrier;
        if (order.getCustomer().getCountry().equals("BE")) {
            if (order.isPriority()) {
                carrier = "BPOST_EXPRESS";
            } else {
                carrier = "BPOST";
            }
        } else if (order.getCustomer().getCountry().equals("NL")) {
            carrier = "POSTNL";
        } else if (order.getCustomer().getCountry().equals("FR")
                || order.getCustomer().getCountry().equals("DE")
                || order.getCustomer().getCountry().equals("LU")) {
            carrier = "DHL";
        } else {
            carrier = "UPS";
        }
        shipment.setCarrier(carrier);
        shipment.setTrackingCode(carrier.substring(0, 2) + shipment.getId().replace("SHIP-", "") + order.getId());
        shipment.setStatus("CREATED");
        shipment.setCost(calculateShippingCost(order));
        shipmentDao.save(shipment);

        order.setTrackingCode(shipment.getTrackingCode());
        logger.log("shipment " + shipment.getId() + " created with " + carrier + " for order " + order.getId());
        return shipment;
    }

    /**
     * Calculates the shipping cost. Shipping is free for standard delivery from
     * 50 euro onwards.
     */
    public double calculateShippingCost(Order order) {
        double cost = 4.99;
        if (order.getShippingMethod().equals("PICKUP")) {
            cost = 0.0;
        } else if (order.getShippingMethod().equals("EXPRESS")) {
            cost = 12.50;
        } else if (order.getShippingMethod().equals("STANDARD")) {
            cost = 4.99;
        }
        int weight = 0;
        for (int i = 0; i < order.getLines().size(); i++) {
            weight = weight + (order.getLines().get(i).getBook().getWeightGrams()
                    * order.getLines().get(i).getQuantity());
        }
        if (weight > 2000) {
            cost = cost + 3.0;
        }
        if (order.isPriority()) {
            cost = cost * 1.5;
        }
        if (!order.getCustomer().getCountry().equals("BE")) {
            cost = cost + 7.5;
        }
        if (order.getSubTotal() > 50 && order.getShippingMethod().equals("STANDARD")) {
            cost = 0.0;
        }
        return Utils.round2(cost);
    }

    public void markAsShipped(String orderId) {
        Shipment shipment = shipmentDao.findByOrderId(orderId);
        if (shipment == null) {
            logger.error("no shipment for order " + orderId);
            return;
        }
        shipment.setStatus("SHIPPED");
        shipmentDao.save(shipment);
        logger.log("shipment " + shipment.getId() + " handed over to " + shipment.getCarrier());
    }

    public void markAsDelivered(String orderId) {
        Shipment shipment = shipmentDao.findByOrderId(orderId);
        shipment.setStatus("DELIVERED");
        shipmentDao.save(shipment);
    }

    public String printLabel(String orderId) {
        Shipment shipment = shipmentDao.findByOrderId(orderId);
        String label = "";
        label = label + "+----------------------------------+\n";
        label = label + "| " + Utils.pad(shipment.getCarrier(), 33) + "|\n";
        label = label + "| " + Utils.pad(shipment.getStreet() + " " + shipment.getHouseNumber(), 33) + "|\n";
        label = label + "| " + Utils.pad(shipment.getZipCode() + " " + shipment.getCity(), 33) + "|\n";
        label = label + "| " + Utils.pad(shipment.getCountry(), 33) + "|\n";
        label = label + "| " + Utils.pad("track: " + shipment.getTrackingCode(), 33) + "|\n";
        label = label + "+----------------------------------+";
        return label;
    }

    public Shipment getShipmentForOrder(String orderId) {
        return shipmentDao.findByOrderId(orderId);
    }
}
