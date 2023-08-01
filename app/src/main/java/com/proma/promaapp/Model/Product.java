package com.proma.promaapp.Model;

import java.util.Map;

public class Product {
    private String id;
    private String storeId;
    private String name;
    private double price;
    private int quantity;
    private String image;
    private String expiry;
    private int availableQuantity;

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public Product() {
        // Empty constructor needed for Firebase Realtime Database
    }


    public Product(String id, String storeId, String name, double price, int quantity, String image) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
    }
    public Product(String id, String storeId, String name, double price, int quantity, String image, String expiry) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
        this.expiry = expiry;
    }
    public Product(String id, String storeId, String name, double price, int quantity) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    public static Product fromHashMap(Map<String, Object> productMap) {
        String id = (String) productMap.get("id");
        String storeId = (String) productMap.get("storeId");
        String name = (String) productMap.get("name");
        double price = (Double) productMap.get("price");
        int quantity = ((Long) productMap.get("quantity")).intValue();
        String image = (String) productMap.get("image");
        String expiry = (String) productMap.get("expiry");
        Product product = new Product(id, storeId, name, price, quantity, image, expiry);
        return product;
    }
}
