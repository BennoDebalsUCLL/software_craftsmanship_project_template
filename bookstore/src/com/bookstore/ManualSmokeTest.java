package com.bookstore;

import com.bookstore.models.Customer;
import com.bookstore.models.Order;
import com.bookstore.services.CartService;
import com.bookstore.services.CatalogService;
import com.bookstore.services.CustomerService;
import com.bookstore.services.OrderService;
import com.bookstore.models.Book;

/**
 * Run this before a release to check that the order flow still works.
 */
public class ManualSmokeTest {

    // TODO write real tests for this (2019)
    public static void main(String[] args) {
        CatalogService catalogService = new CatalogService();
        CustomerService customerService = new CustomerService();
        CartService cartService = new CartService();
        OrderService orderService = new OrderService();

        catalogService.addBook(new Book("978-0132350884", "Clean Code", "Robert C. Martin", "TECH", 42.50, 5, 700));
        Customer customer = customerService.register("Test User", "test@example.com", "Teststraat", "1", "1000",
                "Brussel", "BE", "NORMAL");
        cartService.addToCart(customer.getId(), "978-0132350884", 1);
        Order order = orderService.placeOrder(customer.getId(), "CARD", "STANDARD", "4111111111111111", false, false,
                null);

        System.out.println("order id     : " + order.getId());
        System.out.println("order total  : " + order.getTotal());
        System.out.println("order status : " + order.getStatus());
        System.out.println("smoke test finished, check the numbers above by hand");
    }
}
