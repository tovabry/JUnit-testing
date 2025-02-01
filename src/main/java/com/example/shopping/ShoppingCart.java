package com.example.shopping;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private final Map<String, Item> items = new HashMap<>();


    public void addItem(String productName, double price, int quantity) {
    items.put(productName, new Item(productName, price, quantity));
    }

    public int itemCount() {
        return items.size();
    }

}
