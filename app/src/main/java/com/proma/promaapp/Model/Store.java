package com.proma.promaapp.Model;

public class Store {
    private String storeId;
    private String accountId;
    private String storeName;
    private String storeAddress;

    public Store() {
        // Default constructor required for Firestore
    }

    public Store(String storeId, String accountId, String storeName, String storeAddress) {
        this.storeId = storeId;
        this.accountId = accountId;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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
