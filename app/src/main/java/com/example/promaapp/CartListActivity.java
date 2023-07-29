package com.example.promaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.promaapp.Model.CartList;
import com.example.promaapp.Model.Order;
import com.example.promaapp.Model.Product;
import com.example.promaapp.Model.ProductListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartListActivity extends AppCompatActivity {

    private ListView lvCartList;
    private List<Product> cartList;
    private ProductListAdapter cartListAdapter;
    private TextView tvTotalPrice;
    private Button btnCreateOrder;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);

        db = FirebaseFirestore.getInstance();

        lvCartList = findViewById(R.id.lvCartList);
        tvTotalPrice = findViewById(R.id.tvTotalPrice); // Add the TextView for total price
        btnCreateOrder = findViewById(R.id.btnCreateOrder); // Add the "Create Order" button

        cartList = new ArrayList<>();
        cartListAdapter = new ProductListAdapter(this, cartList, true,false);
        lvCartList.setAdapter(cartListAdapter);

        // Retrieve the cart items from the CartList class
        List<Product> items = CartList.getCartItems();

        // Add the items to the cart list
        cartList.addAll(items);
        cartListAdapter.notifyDataSetChanged();

        updateTotalPrice(); // Update the total price initially

        // Set item click listener for updating the total price when quantity changes
        cartListAdapter.setOnQuantityChangeListener(new ProductListAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged(int position, int quantity) {
                updateTotalPrice();
            }
        });

        // Set click listener for the "Create Order" button
        btnCreateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOrder();
            }
        });
    }

    private void updateTotalPrice() {
        double totalPrice = 0.0;
        for (Product product : cartList) {
            totalPrice += product.getPrice() * product.getQuantity();
        }
        tvTotalPrice.setText(String.format("Thành Tiền: $%.2f", totalPrice));
    }

    private void createOrder() {
        // Create an instance of the Order model and populate it with the relevant data
        int orderId = generateOrderId(); // Replace this with your own logic to generate unique order IDs
        List<Product> productList = new ArrayList<>(cartList);
        float totalPrice = calculateTotalPrice();
        String storeId = getIntent().getStringExtra("storeId");
        Date createDate = new Date(); // Create a new Date object with the current date and time

        Order order = new Order(orderId, productList, totalPrice, storeId, createDate);

        // Create a new document reference with a unique ID
        DocumentReference orderRef = db.collection("orders").document(String.valueOf(orderId));

        // Create a map to store the order data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", order.getOrderId());
        orderData.put("productList", order.getProductList());
        orderData.put("totalPrice", order.getTotalPrice());
        orderData.put("storeId", order.getStoreId());
        orderData.put("createDate", order.getCreateDate()); // Add the create date to the order data

        // Save the order data to Firestore
        orderRef.set(orderData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update the quantity of the products in the cart
                        for (Product product : productList) {
                            updateProductQuantity(product);
                        }

                        // Clear the cart and update the UI
                        CartList.clearCart();
                        cartList.clear();
                        cartListAdapter.notifyDataSetChanged();
                        updateTotalPrice();

                        // Show a success message or navigate to the order details screen
                        Toast.makeText(CartListActivity.this, "Order created successfully!", Toast.LENGTH_SHORT).show();
                        // Navigate back to OrderProductActivity
                        Intent intent = new Intent(CartListActivity.this, OrderProductActivity.class);
                        // Make sure to pass the storeId back to OrderProductActivity
                        intent.putExtra("storeId", getIntent().getStringExtra("storeId"));
                        startActivity(intent);
                        // Finish the current activity to remove it from the stack
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors that occurred while saving the order
                        Toast.makeText(CartListActivity.this, "Failed to create order.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProductQuantity(Product product) {
        // Retrieve the available quantity of the product from the database
        db.collection("products").document(product.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            int availableQuantity = product.getAvailableQuantity();

                            // Update the quantity of the product in the cart
                            int orderedQuantity = product.getQuantity();
                            if (orderedQuantity <= availableQuantity) {
                                int remainingQuantity = availableQuantity - orderedQuantity;

                                // Update the quantity in the database
                                db.collection("products").document(product.getId())
                                        .update("quantity", remainingQuantity)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Quantity updated successfully
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle the failure case
                                                Toast.makeText(CartListActivity.this, "Failed to update product quantity.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // The ordered quantity exceeds the available quantity
                                Toast.makeText(CartListActivity.this, "Insufficient quantity for " + product.getName(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private int generateOrderId() {
        // Implement your own logic to generate a unique order ID
        // This can be based on timestamps, random numbers, or any other method suitable for your application
        return (int) (System.currentTimeMillis() % 100000);
    }

    private float calculateTotalPrice() {
        float totalPrice = 0.0F;
        for (Product product : cartList) {
            totalPrice += product.getPrice() * product.getQuantity();
        }
        return totalPrice;
    }
}
