package com.bookstore.services;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.CartDao;
import com.bookstore.dao.CustomerDao;
import com.bookstore.models.Book;
import com.bookstore.models.Cart;
import com.bookstore.models.CartItem;
import com.bookstore.models.Customer;
import com.bookstore.util.Logger;
import com.bookstore.util.Utils;

public class CartService {

    private CartDao cartDao = new CartDao();
    private BookDao bookDao = new BookDao();
    private CustomerDao customerDao = new CustomerDao();
    private Logger logger = Logger.getInstance();

    public Cart getOrCreateCart(String userId) {
        Cart cart = cartDao.findByUserId(userId);
        if (cart == null) {
            cart = new Cart(cartDao.nextId(), userId);
            cartDao.save(cart);
        }
        return cart;
    }

    public void addToCart(String userId, String isbn, int quantity) {
        Customer customer = customerDao.findById(userId);
        if (customer == null) {
            throw new RuntimeException("unknown user " + userId);
        }
        if (!Utils.isValidIsbn(isbn)) {
            throw new RuntimeException("invalid isbn: " + isbn);
        }
        if (quantity <= 0) {
            throw new RuntimeException("quantity must be positive");
        }
        Book book = bookDao.findByIsbn(isbn);
        if (book == null) {
            throw new RuntimeException("unknown article " + isbn);
        }
        if (book.getStock() < quantity) {
            throw new RuntimeException("not enough stock for " + book.getTitel());
        }
        Cart cart = getOrCreateCart(userId);
        for (int i = 0; i < cart.items.size(); i++) {
            if (cart.items.get(i).isbn.equals(isbn)) {
                cart.items.get(i).quantity = cart.items.get(i).quantity + quantity;
                cartDao.save(cart);
                logger.log("increased quantity of " + isbn + " in cart " + cart.id);
                return;
            }
        }
        cart.items.add(new CartItem(isbn, quantity, book.getPrice()));
        cartDao.save(cart);
        logger.log("added " + quantity + "x " + isbn + " to cart " + cart.id);
    }

    public void removeFromCart(String userId, String isbn) {
        Cart cart = getOrCreateCart(userId);
        for (int i = 0; i < cart.items.size(); i++) {
            if (cart.items.get(i).isbn.equals(isbn)) {
                cart.items.remove(i);
                cartDao.save(cart);
                return;
            }
        }
    }

    public double getCartTotal(String userId) {
        Cart cart = getOrCreateCart(userId);
        Customer customer = customerDao.findById(userId);
        double total = 0.0;
        for (int i = 0; i < cart.items.size(); i++) {
            CartItem item = cart.items.get(i);
            Book book = bookDao.findByIsbn(item.isbn);
            total = total + (book.getPrice() * item.quantity);
        }
        double vat;
        if (customer.getCountry().equals("BE")) {
            vat = 0.21;
        } else if (customer.getCountry().equals("NL")) {
            vat = 0.21;
        } else if (customer.getCountry().equals("LU")) {
            vat = 0.17;
        } else if (customer.getCountry().equals("FR")) {
            vat = 0.20;
        } else if (customer.getCountry().equals("DE")) {
            vat = 0.19;
        } else {
            vat = 0.21;
        }
        return Utils.round2(total + (total * vat));
    }

    public int getItemCount(String userId) {
        Cart cart = getOrCreateCart(userId);
        int count = 0;
        for (int i = 0; i < cart.items.size(); i++) {
            count = count + cart.items.get(i).quantity;
        }
        return count;
    }

    public void clearCart(String userId) {
        Cart cart = cartDao.findByUserId(userId);
        if (cart != null) {
            cart.items.clear();
            cartDao.save(cart);
        }
    }
}
