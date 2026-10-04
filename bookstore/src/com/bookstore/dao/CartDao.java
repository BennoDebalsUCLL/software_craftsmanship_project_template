package com.bookstore.dao;

import com.bookstore.models.Cart;
import java.util.ArrayList;
import java.util.List;

public class CartDao {

    private Database db = Database.getInstance();

    public void save(Cart cart) {
        db.save("carts", cart.id, cart);
    }

    public Cart findByUserId(String userId) {
        List<Object> rows = db.findAll("carts");
        for (int i = 0; i < rows.size(); i++) {
            Cart cart = (Cart) rows.get(i);
            if (cart.userId.equals(userId)) {
                return cart;
            }
        }
        return null;
    }

    public List<Cart> findAll() {
        List<Cart> result = new ArrayList<Cart>();
        List<Object> rows = db.findAll("carts");
        for (int i = 0; i < rows.size(); i++) {
            result.add((Cart) rows.get(i));
        }
        return result;
    }

    public void delete(String cartId) {
        db.delete("carts", cartId);
    }

    private static int sequence = 0;

    public String nextId() {
        sequence++;
        return "CART-" + sequence;
    }
}
