package com.example.promaapp.Model;

public class Product {
    private String id;
    private String storeId;
    private String name;
    private double price;
    private int quantity;
    private String image;
    private String expiry;

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
}
