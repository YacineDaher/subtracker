package com.emsi.subtracker.models;

public class SubscriptionTemplate {
    private String name;
    private String category;
    private double defaultPrice;
    private String hexColor;

    public SubscriptionTemplate(String name, String category, double defaultPrice, String hexColor) {
        this.name = name;
        this.category = category;
        this.defaultPrice = defaultPrice;
        this.hexColor = hexColor;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getDefaultPrice() {
        return defaultPrice;
    }

    public String getHexColor() {
        return hexColor;
    }

    @Override
    public String toString() {
        return name;
    }
}
