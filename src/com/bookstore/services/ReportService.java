package com.bookstore.services;

import com.bookstore.dao.OrderDao;
import com.bookstore.models.Order;
import com.bookstore.models.OrderLine;
import com.bookstore.util.Utils;
import java.util.List;

public class ReportService {

    private OrderDao orderDao = new OrderDao();

    public String generateCsvOrderReport() {
        List<Order> orders = orderDao.findAll();
        String result = "";
        result = result + "order;client;country;lines;subtotal;discount;vat;shipping;total;status\n";
        double grandTotal = 0.0;
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            int lineCount = 0;
            for (int j = 0; j < order.getLines().size(); j++) {
                lineCount = lineCount + order.getLines().get(j).getQuantity();
            }
            result = result + order.getId() + ";" + order.getCustomer().getName() + ";"
                    + order.getCustomer().getCountry() + ";" + lineCount + ";" + order.getSubTotal() + ";"
                    + order.getDiscountAmount() + ";" + order.getVatAmount() + ";" + order.getShippingCost()
                    + ";" + order.getTotal() + ";" + order.getStatus() + "\n";
            grandTotal = grandTotal + order.getTotal();
        }
        result = result + ";;;;;;;;" + Utils.round2(grandTotal) + ";\n";
        return result;
    }

    public String generateTextOrderReport() {
        List<Order> orders = orderDao.findAll();
        String result = "";
        result = result + "ORDER REPORT " + Utils.today() + "\n";
        result = result + "==========================================================\n";
        double grandTotal = 0.0;
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            int lineCount = 0;
            for (int j = 0; j < order.getLines().size(); j++) {
                lineCount = lineCount + order.getLines().get(j).getQuantity();
            }
            result = result + Utils.pad(order.getId(), 10) + Utils.pad(order.getCustomer().getName(), 20)
                    + Utils.pad(order.getCustomer().getCountry(), 5) + Utils.pad(lineCount + " items", 12)
                    + Utils.pad(Utils.formatMoney(order.getTotal()), 14) + order.getStatus() + "\n";
            grandTotal = grandTotal + order.getTotal();
        }
        result = result + "==========================================================\n";
        result = result + "TOTAL: " + Utils.formatMoney(Utils.round2(grandTotal)) + "\n";
        return result;
    }

    // public String generateHtmlOrderReport() {
    //     List<Order> orders = orderDao.findAll();
    //     String result = "<table>";
    //     for (Order order : orders) {
    //         result = result + "<tr><td>" + order.getId() + "</td></tr>";
    //     }
    //     return result + "</table>";
    // }

    public String generateBestsellerReport() {
        List<Order> orders = orderDao.findAll();
        String result = "BESTSELLERS\n";
        java.util.Map<String, Integer> counts = new java.util.HashMap<String, Integer>();
        for (int i = 0; i < orders.size(); i++) {
            List<OrderLine> lines = orders.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                String title = lines.get(j).getBook().getTitel();
                if (counts.containsKey(title)) {
                    counts.put(title, counts.get(title) + lines.get(j).getQuantity());
                } else {
                    counts.put(title, lines.get(j).getQuantity());
                }
            }
        }
        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            result = result + Utils.pad(entry.getKey(), 35) + entry.getValue() + "\n";
        }
        return result;
    }
}
