package com.bookstore;

import com.bookstore.dao.Database;
import com.bookstore.models.Book;
import com.bookstore.models.Customer;
import com.bookstore.models.DiscountedBook;
import com.bookstore.models.Invoice;
import com.bookstore.models.Order;
import com.bookstore.models.SeasonalDiscountedBook;
import com.bookstore.models.Shipment;
import com.bookstore.services.BillingService;
import com.bookstore.services.CartService;
import com.bookstore.services.CatalogService;
import com.bookstore.services.CustomerService;
import com.bookstore.services.InventoryService;
import com.bookstore.services.OrderService;
import com.bookstore.services.ReportService;
import com.bookstore.services.ShippingService;
import com.bookstore.util.Logger;
import com.bookstore.util.Utils;

public class Main {

    public static void main(String[] args) {

        Logger.DEBUG = false;

        CatalogService catalogService = new CatalogService();
        CustomerService customerService = new CustomerService();
        InventoryService inventoryService = new InventoryService();
        CartService cartService = new CartService();
        OrderService orderService = new OrderService();
        BillingService billingService = new BillingService();
        ShippingService shippingService = new ShippingService();
        ReportService reportService = new ReportService();

        System.out.println("=== THE BOOKSTORE ===");
        System.out.println(Database.getInstance().getConnection());
        System.out.println();

        // seed the catalog
        catalogService.addBook(new Book("978-0132350884", "Clean Code", "Robert C. Martin", "TECH", 42.50, 20, 700));
        catalogService.addBook(new Book("978-0321125215", "Domain-Driven Design", "Eric Evans", "TECH", 54.99, 20, 950));
        catalogService.addBook(new DiscountedBook("978-0201633610", "Design Patterns", "Erich Gamma", "TECH", 49.99,
                20, 800, 15.0));
        catalogService.addBook(new SeasonalDiscountedBook("978-0134757599", "Refactoring", "Martin Fowler", "TECH",
                47.95, 20, 880, 10.0, "SUMMER"));
        catalogService.addBook(new Book("978-0596007126", "Head First Design Patterns", "Eric Freeman", "TECH", 39.99,
                20, 1400));
        catalogService.addBook(new Book("978-0135957059", "The Pragmatic Programmer", "Andrew Hunt", "TECH", 24.95,
                20, 450));

        // seed the clients
        Customer ann = customerService.register("Ann Peeters", "ann@example.com", "Kerkstraat", "12", "2000",
                "Antwerpen", "BE", "GOLD");
        Customer luc = customerService.register("Luc Martin", "luc@example.fr", "Rue du Pont", "8", "75001", "Paris",
                "FR", "NORMAL");
        Customer sam = customerService.register("Sam De Wit", "sam@student.example.com", "Langestraat", "44", "9000",
                "Gent", "BE", "STUDENT");

        System.out.println();
        System.out.println("--- SCENARIO 1: gold client, standard shipping, card payment ---");
        cartService.addToCart(ann.getId(), "978-0132350884", 1);
        cartService.addToCart(ann.getId(), "978-0135957059", 1);
        System.out.println("cart contains " + cartService.getItemCount(ann.getId())
                + " items, cart total before discount and shipping is "
                + Utils.formatMoney(cartService.getCartTotal(ann.getId())));
        Order order1 = orderService.placeOrder(ann.getId(), "CARD", "STANDARD", "4111111111111111", false, false,
                null);
        System.out.println();
        System.out.println(orderService.printOrderSummary(order1.getId()));
        System.out.println();
        System.out.println(billingService.printInvoice(order1.getId()));
        System.out.println();
        Shipment shipment1 = shippingService.getShipmentForOrder(order1.getId());
        System.out.println(shippingService.printLabel(order1.getId()));
        System.out.println("shipping cost on the order    : " + Utils.formatMoney(order1.getShippingCost()));
        System.out.println("shipping cost on the shipment : " + Utils.formatMoney(shipment1.getCost()));

        System.out.println();
        System.out.println("--- SCENARIO 2: order lifecycle ---");
        orderService.advanceStatus(order1.getId(), "S1");
        orderService.advanceStatus(order1.getId(), "DONE");
        orderService.advanceStatus(order1.getId(), "P");

        System.out.println();
        System.out.println("--- SCENARIO 3: french client, express, gift wrap, priority, coupon ---");
        cartService.addToCart(luc.getId(), "978-0201633610", 1);
        cartService.addToCart(luc.getId(), "978-0596007126", 2);
        cartService.addToCart(luc.getId(), "978-0321125215", 1);
        Order order2 = orderService.placeOrder(luc.getId(), "PAYPAL", "EXPRESS", "4111111111111111", true, true,
                "WELCOME10");
        System.out.println();
        System.out.println(orderService.printOrderSummary(order2.getId()));

        System.out.println();
        System.out.println("--- SCENARIO 4: student, bank transfer, invoice paid later ---");
        cartService.addToCart(sam.getId(), "978-0134757599", 1);
        Order order3 = orderService.placeOrder(sam.getId(), "TRANSFER", "PICKUP", null, false, false, null);
        System.out.println();
        System.out.println(orderService.printOrderSummary(order3.getId()));
        Invoice invoice3 = billingService.getInvoiceForOrder(order3.getId());
        System.out.println("invoice status before payment : " + invoice3.getStatus());
        billingService.payInvoice(invoice3.getId(), "4111111111111111");
        System.out.println("invoice status after payment  : " + invoice3.getStatus());
        System.out.println("order status after payment    : " + orderService.getOrder(order3.getId()).getStatus());

        System.out.println();
        System.out.println("--- SCENARIO 5: cancelling an order ---");
        cartService.addToCart(sam.getId(), "978-0132350884", 3);
        Order order4 = orderService.placeOrder(sam.getId(), "CARD", "STANDARD", "4111111111111111", false, false,
                null);
        orderService.cancelOrder(order4.getId());
        System.out.println("order 4 status: " + orderService.getOrder(order4.getId()).getStatus());

        System.out.println();
        System.out.println("--- SCENARIO 6: things that go wrong ---");
        try {
            orderService.placeOrder(ann.getId(), "CARD", "STANDARD", "4111111111111111", false, false, null);
        } catch (RuntimeException e) {
            System.out.println("caught: " + e.getMessage());
        }
        try {
            cartService.addToCart(ann.getId(), "978-0132350884", 1);
            orderService.placeOrder(ann.getId(), "BITCOIN", "STANDARD", "4111111111111111", false, false, null);
        } catch (RuntimeException e) {
            System.out.println("caught: " + e.getMessage());
        }
        try {
            cartService.addToCart(ann.getId(), "978-9999999999", 1);
        } catch (RuntimeException e) {
            System.out.println("caught: " + e.getMessage());
        }
        cartService.clearCart(ann.getId());

        System.out.println();
        System.out.println("--- SCENARIO 7: catalog prices ---");
        System.out.println("Clean Code excl vat        : "
                + Utils.formatMoney(catalogService.getBook("978-0132350884").getPrice()));
        System.out.println("Clean Code incl vat for BE : "
                + Utils.formatMoney(catalogService.getPriceWithVat("978-0132350884", "BE")));
        System.out.println("Design Patterns excl vat   : "
                + Utils.formatMoney(catalogService.getBook("978-0201633610").getPrice())
                + " (list price 49.99 EUR with a 15% discount)");

        System.out.println();
        System.out.println("--- CONSISTENCY CHECK ---");
        String[] orderIds = new String[] { order1.getId(), order2.getId(), order3.getId(), order4.getId() };
        for (int i = 0; i < orderIds.length; i++) {
            Order order = orderService.getOrder(orderIds[i]);
            Invoice invoice = billingService.getInvoiceForOrder(orderIds[i]);
            Shipment shipment = shippingService.getShipmentForOrder(orderIds[i]);
            String shippingLine = "no shipment";
            if (shipment != null) {
                shippingLine = Utils.formatMoney(order.getShippingCost()) + " on the order, "
                        + Utils.formatMoney(shipment.getCost()) + " on the shipment";
            }
            System.out.println(Utils.pad(order.getId(), 8)
                    + Utils.pad("order " + Utils.formatMoney(order.getTotal()), 22)
                    + Utils.pad("invoice " + Utils.formatMoney(invoice.getAmountInclVat()), 24)
                    + shippingLine);
        }

        System.out.println();
        System.out.println("--- REPORTS ---");
        System.out.println(reportService.generateTextOrderReport());
        System.out.println(reportService.generateCsvOrderReport());
        System.out.println(reportService.generateBestsellerReport());
        inventoryService.printStockReport();

        System.out.println();
        System.out.println("lifetime value of " + ann.getName() + ": "
                + Utils.formatMoney(customerService.getLifetimeValue(ann.getId())));
        System.out.println("queries executed: " + Database.queryCount);
        System.out.println("log messages: " + Logger.getInstance().getMessageCount());
    }
}
