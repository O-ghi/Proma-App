package com.example.promaapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Order;
import com.example.promaapp.Model.OrderListAdapter;
import com.example.promaapp.Model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private ListView lvOrderList;
    private List<Order> orderList;
    private OrderListAdapter orderListAdapter;
    private FirebaseFirestore firestore;
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        firestore = FirebaseFirestore.getInstance();
        lvOrderList = findViewById(R.id.lvOrderList);

        // Retrieve the storeId passed from the previous activity
        storeId = getIntent().getStringExtra("storeId");

        orderList = new ArrayList<>();
        orderListAdapter = new OrderListAdapter(this, orderList);
        lvOrderList.setAdapter(orderListAdapter);

        // Retrieve the order history from Firestore
        retrieveOrderHistory();
    }

    private void retrieveOrderHistory() {
        firestore.collection("orders")
                .whereEqualTo("storeId", storeId)
                .orderBy("createDate") // Sort the orders by create date
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int orderId = document.getLong("orderId").intValue();
                            float totalPrice = document.getLong("totalPrice");
                            List<Product> productList = (List<Product>) document.get("productList");
                            String storeId = document.getString("storeId");

                            // Retrieve the create date from the Firestore document
                            Date createDate = document.getDate("createDate");

                            Order order = new Order(orderId, productList, totalPrice, storeId, createDate);
                            orderList.add(order);
                        }
                        orderListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(OrderHistoryActivity.this, "Failed to retrieve order history: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("OrderHistoryActivity", "Failed to retrieve order history", task.getException());
                    }
                });
    }

}
