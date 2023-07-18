package com.example.promaapp.Model;

import java.util.Date;
import java.util.List;

public class Order {
    private int orderId;
    private List<Product> productList;
    private float totalPrice;
    private String storeId;
    private Date createDate;

    public Order(int orderId, List<Product> productList, float totalPrice, String storeId, Date createDate) {
        this.orderId = orderId;
        this.productList = productList;
        this.totalPrice = totalPrice;
        this.storeId = storeId;
        this.createDate = createDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public String getStoreId() {
        return storeId;
    }

    public Date getCreateDate() {
        return createDate;
    }
}
