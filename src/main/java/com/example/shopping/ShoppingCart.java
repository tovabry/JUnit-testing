package com.example.shopping;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private final Map<String, Item> items = new HashMap<>();


    public void addItem(String productName, double price, int quantity) {
        if (items.containsKey(productName)) {
            Item existingItem = items.get(productName);
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            items.put(productName, new Item(productName, price, quantity));
        }
    }

    public int itemCount() {
        return items.size();
    }

    public void removeItem(String itemName) {
        items.remove(itemName);
    }

    public int getQuantity(String productName) {
        return items.containsKey(productName) ? items.get(productName).getQuantity() : 0;    }
}
