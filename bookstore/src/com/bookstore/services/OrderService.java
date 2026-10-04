package com.bookstore.services;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.CartDao;
import com.bookstore.dao.CustomerDao;
import com.bookstore.dao.OrderDao;
import com.bookstore.dao.OrderLineDao;
import com.bookstore.external.LegacyPaymentGatewayClient;
import com.bookstore.models.Book;
import com.bookstore.models.Cart;
import com.bookstore.models.CartItem;
import com.bookstore.models.Customer;
import com.bookstore.models.Invoice;
import com.bookstore.models.Order;
import com.bookstore.models.OrderLine;
import com.bookstore.util.Config;
import com.bookstore.util.EmailSender;
import com.bookstore.util.Logger;
import com.bookstore.util.Utils;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OrderService {

    private OrderDao orderDao = new OrderDao();
    private OrderLineDao orderLineDao = new OrderLineDao();
    private CustomerDao customerDao = new CustomerDao();
    private BookDao bookDao = new BookDao();
    private CartDao cartDao = new CartDao();
    private BillingService billingService = new BillingService();
    private ShippingService shippingService = new ShippingService();
    private InventoryService inventoryService = new InventoryService();
    private CustomerService customerService = new CustomerService();
    private Logger logger = Logger.getInstance();

    public Order placeOrder(String userId, String paymentMethod, String shippingMethod, String cardNumber,
            boolean giftWrap, boolean priority, String couponCode) {

        Customer client = customerDao.findById(userId);
        if (client == null) {
            throw new RuntimeException("unknown user " + userId);
        }
        if (!Utils.isValidEmail(client.getEmail())) {
            throw new RuntimeException("invalid email");
        }
        Cart cart = cartDao.findByUserId(userId);
        if (cart == null || cart.items.size() == 0) {
            throw new RuntimeException("cart is empty");
        }
        if (cart.items.size() > Config.MAX_LINES_PER_ORDER) {
            throw new RuntimeException("too many lines");
        }

        Order order = new Order();
        order.setId(orderDao.nextId());
        order.setCustomer(client);
        order.setStatus("NEW");
        order.setCreatedAt(new Date());
        order.setPaymentMethod(paymentMethod);
        order.setShippingMethod(shippingMethod);
        order.setGiftWrap(giftWrap);
        order.setPriority(priority);
        order.setCouponCode(couponCode);

        double subTotal = 0.0;
        int weight = 0;
        for (int i = 0; i < cart.items.size(); i++) {
            CartItem item = cart.items.get(i);
            Book book = bookDao.findByIsbn(item.isbn);
            if (book == null) {
                throw new RuntimeException("unknown article " + item.isbn);
            }
            if (book.getStock() < item.quantity) {
                throw new RuntimeException("not enough stock for " + book.getTitel());
            }
            OrderLine line = new OrderLine();
            line.setId(orderLineDao.nextId());
            line.setOrderId(order.getId());
            line.setBook(book);
            line.setQuantity(item.quantity);
            line.setUnitPrice(book.getPrice());
            line.setLineTotal(book.getPrice() * item.quantity);
            order.getLines().add(line);
            subTotal = subTotal + line.getLineTotal();
            weight = weight + (book.getWeightGrams() * item.quantity);
        }

        // calculate the discount
        // TODO the marketing team wants configurable discount rules (ticket BOOK-233)
        double discount = 0.0;
        if (client.getType().equals("GOLD")) {
            if (subTotal > 100) {
                if (couponCode != null && couponCode.equals("SUMMER")) {
                    discount = subTotal * 0.20;
                } else {
                    discount = subTotal * 0.15;
                }
            } else {
                if (couponCode != null && couponCode.equals("SUMMER")) {
                    discount = subTotal * 0.12;
                } else {
                    discount = subTotal * 0.10;
                }
            }
        } else if (client.getType().equals("STUDENT")) {
            if (subTotal > 100) {
                discount = subTotal * 0.10;
            } else {
                if (couponCode != null && couponCode.equals("SUMMER")) {
                    discount = subTotal * 0.08;
                } else {
                    discount = subTotal * 0.05;
                }
            }
        } else if (client.getType().equals("STAFF")) {
            discount = subTotal * 0.25;
        } else {
            if (couponCode != null && couponCode.equals("SUMMER")) {
                discount = subTotal * 0.05;
            } else if (couponCode != null && couponCode.equals("WELCOME10")) {
                discount = 10.0;
            } else {
                discount = 0.0;
            }
        }
        if (client.getLoyaltyPoints() > 500) {
            discount = discount + 5.0;
        }

        double vat;
        if (client.getCountry().equals("BE")) {
            vat = 0.21;
        } else if (client.getCountry().equals("NL")) {
            vat = 0.21;
        } else if (client.getCountry().equals("LU")) {
            vat = 0.17;
        } else if (client.getCountry().equals("FR")) {
            vat = 0.20;
        } else if (client.getCountry().equals("DE")) {
            vat = 0.19;
        } else if (client.getCountry().equals("US")) {
            vat = 0.07;
        } else {
            vat = 0.21;
        }
        double vatAmount = (subTotal - discount) * vat;

        double shippingCost = 4.99;
        if (shippingMethod.equals("PICKUP")) {
            shippingCost = 0.0;
        } else if (shippingMethod.equals("EXPRESS")) {
            shippingCost = 12.50;
        }
        if (weight > 2000) {
            shippingCost = shippingCost + 3.0;
        }
        if (priority) {
            shippingCost = shippingCost * 1.5;
        }
        if (!client.getCountry().equals("BE")) {
            shippingCost = shippingCost + 7.5;
        }
        if (subTotal > 50 && shippingMethod.equals("STANDARD")) {
            shippingCost = 0.0;
        }

        double total = subTotal - discount + vatAmount + shippingCost;
        if (giftWrap) {
            total = total + 3.5;
        }

        order.setSubTotal(Utils.round2(subTotal));
        order.setDiscountAmount(Utils.round2(discount));
        order.setVatAmount(Utils.round2(vatAmount));
        order.setShippingCost(Utils.round2(shippingCost));
        order.setTotal(Utils.round2(total));

        if (paymentMethod.equals("CARD") || paymentMethod.equals("PAYPAL")) {
            LegacyPaymentGatewayClient gateway = new LegacyPaymentGatewayClient();
            Map<String, String> response = gateway.doPayment(client.getEmail(), cardNumber,
                    (int) (order.getTotal() * 100), "EUR");
            if (response.get("txn_stat").equals("OK")) {
                order.setStatus("P");
                order.setPaidAt(new Date());
                order.setPaymentTransactionId(response.get("txn_id"));
                System.out.println("payment accepted, transaction " + response.get("txn_id") + " for "
                        + (Integer.parseInt(response.get("amt_cents")) / 100.0) + " EUR");
            } else {
                logger.error("payment refused: " + response.get("err_cd"));
                throw new RuntimeException("payment refused: " + response.get("err_cd"));
            }
        } else if (paymentMethod.equals("TRANSFER")) {
            order.setStatus("NEW");
        } else {
            throw new RuntimeException("unsupported payment method " + paymentMethod);
        }

        // save the order
        orderDao.save(order);
        client.getOrders().add(order);
        customerDao.save(client);

        for (int i = 0; i < order.getLines().size(); i++) {
            OrderLine line = order.getLines().get(i);
            Book book = line.getBook();
            book.setStock(book.getStock() - line.getQuantity());
            bookDao.save(book);
            inventoryService.reserve(book.getIsbn(), line.getQuantity());
        }

        Invoice invoice = billingService.createInvoice(order);

        if (!shippingMethod.equals("PICKUP")) {
            shippingService.createShipment(order);
            orderDao.save(order);
        }

        EmailSender mailer = new EmailSender();
        String body = "Dear " + client.getName() + ",\n\nThank you for your order " + order.getId() + ".\n"
                + "Total: " + Utils.formatMoney(order.getTotal()) + "\n"
                + "Invoice: " + invoice.getId() + "\n";
        if (order.getTrackingCode() != null) {
            body = body + "Tracking code: " + order.getTrackingCode() + "\n";
        }
        body = body + "\n" + Config.SHOP_NAME;
        mailer.sendEmail(client.getEmail(), "Order " + order.getId() + " confirmed", body);

        if (Config.FEATURE_LOYALTY_POINTS) {
            customerService.addLoyaltyPoints(client, order.getTotal());
        }

        cart.items.clear();
        cartDao.save(cart);

        logger.log("order " + order.getId() + " placed by " + client.getId() + " for "
                + Utils.formatMoney(order.getTotal()));
        return order;
    }

    /**
     * Moves the order to the next status. Statuses are NEW, P, S1, DONE and CANC.
     */
    public void advanceStatus(String orderId, String newStatus) {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("unknown order " + orderId);
        }
        if (order.getStatus().equals("NEW") && newStatus.equals("P")) {
            order.setStatus("P");
            order.setPaidAt(new Date());
        } else if (order.getStatus().equals("P") && newStatus.equals("S1")) {
            order.setStatus("S1");
            shippingService.markAsShipped(orderId);
            for (int i = 0; i < order.getLines().size(); i++) {
                inventoryService.shipOut(order.getLines().get(i).getBook().getIsbn(),
                        order.getLines().get(i).getQuantity());
            }
        } else if (order.getStatus().equals("S1") && newStatus.equals("DONE")) {
            order.setStatus("DONE");
            shippingService.markAsDelivered(orderId);
        } else if (order.getStatus().equals("CANC")) {
            System.out.println("order " + orderId + " is cancelled, cannot change status");
        } else {
            System.out.println("cannot go from " + order.getStatus() + " to " + newStatus);
            return;
        }
        orderDao.save(order);
        logger.log("order " + orderId + " is now " + order.getStatus());
    }

    public void cancelOrder(String orderId) {
        Order order = orderDao.findById(orderId);
        if (order.getStatus().equals("S1") || order.getStatus().equals("DONE")) {
            throw new RuntimeException("order already shipped");
        }
        order.setStatus("CANC");
        Invoice invoice = billingService.getInvoiceForOrder(orderId);
        if (invoice != null && invoice.getStatus().equals("PAID")) {
            billingService.refund(invoice.getId(), order.getPaymentTransactionId());
        }
        for (int i = 0; i < order.getLines().size(); i++) {
            OrderLine line = order.getLines().get(i);
            Book book = line.getBook();
            book.setStock(book.getStock() + line.getQuantity());
            bookDao.save(book);
            inventoryService.release(book.getIsbn(), line.getQuantity());
        }
        orderDao.save(order);
        EmailSender mailer = new EmailSender();
        mailer.sendEmail(order.getCustomer().getEmail(), "Order " + orderId + " cancelled",
                "Your order has been cancelled.");
        logger.log("order " + orderId + " cancelled");
    }

    public double getOrderTotal(String orderId) {
        Order order = orderDao.findById(orderId);
        double subTotal = 0.0;
        for (int i = 0; i < order.getLines().size(); i++) {
            subTotal = subTotal + order.getLines().get(i).getLineTotal();
        }
        double vat = Utils.vatFor(order.getCustomer().getCountry());
        double total = subTotal - order.getDiscountAmount()
                + ((subTotal - order.getDiscountAmount()) * vat) + order.getShippingCost();
        if (order.isGiftWrap()) {
            total = total + 3.5;
        }
        return Utils.round2(total);
    }

    public String printOrderAsHtml(String orderId) {
        Order order = orderDao.findById(orderId);
        String html = "<html><body>";
        html = html + "<h1>Order " + order.getId() + "</h1>";
        html = html + "<p>" + order.getCustomer().getName() + " ("
                + order.getCustomer().getCity() + ")</p>";
        html = html + "<table>";
        for (int i = 0; i < order.getLines().size(); i++) {
            OrderLine line = order.getLines().get(i);
            html = html + "<tr><td>" + line.getBook().getTitel() + "</td><td>" + line.getQuantity()
                    + "</td><td>" + line.getLineTotal() + "</td></tr>";
        }
        html = html + "</table>";
        html = html + "<p>Total: " + order.getTotal() + "</p>";
        html = html + "</body></html>";
        return html;
    }

    public String printOrderSummary(String orderId) {
        Order order = orderDao.findById(orderId);
        String text = "";
        text = text + "ORDER " + order.getId() + " [" + order.getStatus() + "]\n";
        text = text + "client       : " + order.getCustomer().getName() + " ("
                + order.getCustomer().getCity() + ", " + order.getCustomer().getCountry() + ")\n";
        for (int i = 0; i < order.getLines().size(); i++) {
            OrderLine line = order.getLines().get(i);
            text = text + "  " + Utils.pad(line.getQuantity() + "x " + line.getBook().getTitel(), 40)
                    + Utils.formatMoney(line.getLineTotal()) + "\n";
        }
        text = text + "subtotal     : " + Utils.formatMoney(order.getSubTotal()) + "\n";
        text = text + "discount     : " + Utils.formatMoney(order.getDiscountAmount()) + "\n";
        text = text + "vat          : " + Utils.formatMoney(order.getVatAmount()) + "\n";
        text = text + "shipping     : " + Utils.formatMoney(order.getShippingCost()) + "\n";
        text = text + "total        : " + Utils.formatMoney(order.getTotal());
        return text;
    }

    public List<Order> getOrdersOfUser(String userId) {
        return orderDao.findByCustomerId(userId);
    }

    public List<Order> getAllOrders() {
        return orderDao.findAll();
    }

    public Order getOrder(String orderId) {
        return orderDao.findById(orderId);
    }
}
