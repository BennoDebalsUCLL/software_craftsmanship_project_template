package com.bookstore.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    public String id;
    public String userId;
    public List<CartItem> items = new ArrayList<CartItem>();

    public Cart(String id, String userId) {
        this.id = id;
        this.userId = userId;
    }
}
