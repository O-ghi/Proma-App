package com.example.promaapp.Model;// Account.java

public class Account {
    private String userId;
    private String email;
    private String fullName;
    private String address;



    private String storeName;
    private String storeAddress;

    public Account(String userId, String email, String fullName, String address, String storeName, String storeAddress) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.address = address;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }
}
