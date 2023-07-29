package com.example.promaapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Product;
import com.example.promaapp.Model.ProductListAdapter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {

    private int orderId;
    private String storeId;
    private List<Product> productList;
    private ProductListAdapter productListAdapter;
    private ListView lvOrderedProducts;
    private TextView tvOrderId;
    private TextView tvTotalPrice;
    private TextView tvCreateDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Get the order information from the intent
        productList = new ArrayList<>();
        orderId = getIntent().getIntExtra("orderId", -1);
        storeId = getIntent().getStringExtra("storeId");
        List<Map<String, Object>> productMaps =(List<Map<String, Object>>) getIntent().getSerializableExtra("productList");
        for ( Map<String, Object> productMap : productMaps) {
            Product product = Product.fromHashMap(productMap);
            productList.add(product);
        }
        // Retrieve the products using productIds
        productListAdapter = new ProductListAdapter(OrderDetailActivity.this, productList, true, true);
        // Initialize views
        tvOrderId = findViewById(R.id.tvOrderId);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvCreateDate = findViewById(R.id.tvCreateDate);
        lvOrderedProducts = findViewById(R.id.lvOrderedProducts);
        lvOrderedProducts.setAdapter(productListAdapter);

        // Use the retrieved orderId and storeId to fetch the order and products
        fetchOrderAndProducts();
    }

    private void fetchOrderAndProducts() {
        // Get a reference to the order document using orderId and storeId
        DocumentReference orderRef = FirebaseFirestore.getInstance()
                .collection("orders")
                .document(String.valueOf(orderId));
        Toast.makeText(OrderDetailActivity.this, "Order " + orderId, Toast.LENGTH_SHORT).show();

        // Perform the query to retrieve the order
        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Order found, parse the order data
                    float totalPrice = document.getLong("totalPrice");
                    Date createDate = document.getDate("createDate");

                    // Get the product IDs from the order document

                    // Display order details
                    tvOrderId.setText("Mã đơn: " + orderId);
                    tvTotalPrice.setText("Tổng tiền: " + totalPrice + " VNĐ");
                    tvCreateDate.setText("Created on: " + createDate.toString());
                } else {
                    // Order not found
                    Toast.makeText(OrderDetailActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error occurred while fetching the order
                Toast.makeText(OrderDetailActivity.this, "Failed to fetch order: " + task.getException(), Toast.LENGTH_SHORT).show();
                Log.e("OrderDetailActivity", "Failed to fetch order", task.getException());
            }
        });
    }

    private void fetchProductsByProductIds(List<String> productIds) {
        // Create a query to fetch products using the productIds list
        FirebaseFirestore.getInstance()
                .collection("products")
                .whereIn("id", productIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        productList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Parse the product data from the document
                            String productId = document.getString("id");
                            String name = document.getString("name");
                            double price = document.getDouble("price");
                            int quantity = Math.toIntExact(document.getLong("quantity"));
                            String image = document.getString("image");
                            String expiry = document.getString("expiry");

                            Product product = new Product(productId, storeId, name, price, quantity, image, expiry);
                            productList.add(product);
                        }
                        // Display the ordered products in the ListView
                        productListAdapter = new ProductListAdapter(OrderDetailActivity.this, productList, true, true);
                        lvOrderedProducts.setAdapter(productListAdapter);
                    } else {
                        // Error occurred while fetching products
                        Toast.makeText(OrderDetailActivity.this, "Failed to fetch products: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("OrderDetailActivity", "Failed to fetch products", task.getException());
                    }
                });
    }
}

