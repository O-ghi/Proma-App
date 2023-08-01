package com.proma.promaapp.Model;

import java.util.Date;
import java.util.List;

public class Order {
    private int orderId;
    private List<Product> productList;
    private float totalPrice;
    private String storeId;
    private Date createDate;

    public Order() {

    }

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

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreateDate() {
        return createDate;
    }
}
