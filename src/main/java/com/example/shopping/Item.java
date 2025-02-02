package com.example.shopping;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Item{
    private final String name;
    private final double price;
    private int quantity;
    private final double discountPercentage;

    public Item(String name, double price, int quantity, double discountPercentage) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.discountPercentage = discountPercentage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public double getDiscountedPrice() {
        BigDecimal discountedPrice = BigDecimal.valueOf(price * (1 - discountPercentage / 100));
        return discountedPrice.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

}