package com.example.promaapp.Model;

import java.util.ArrayList;
import java.util.List;

public class CartList {
    private static List<Product> cartItems = new ArrayList<>();

    public static void addToCart(Product product) {
        // Check if the product already exists in the cart list
        int existingIndex = getCartItemIndex(product);

        if (existingIndex != -1) {
            // Product already exists in the cart list, update the quantity
            Product existingProduct = cartItems.get(existingIndex);
            existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
        } else {
            // Product doesn't exist in the cart list, add it
            cartItems.add(product);
        }
    }

    public static void removeFromCart(Product product) {
        cartItems.remove(product);
    }

    public static List<Product> getCartItems() {
        return cartItems;
    }

    public static void clearCart() {
        cartItems.clear();
    }

    public static int getCartItemIndex(Product product) {
        // Check if the product exists in the cart list
        for (int i = 0; i < cartItems.size(); i++) {
            Product cartProduct = cartItems.get(i);
            if (cartProduct.getId().equals(product.getId())) {
                return i; // Return the index of the matching product
            }
        }
        return -1; // Return -1 if the product is not found
    }

    public static Product getCartItem(int index) {
        return cartItems.get(index);
    }
}
