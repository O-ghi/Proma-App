package com.proma.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.proma.promaapp.Model.Order;
import com.proma.promaapp.Model.OrderListAdapter;
import com.proma.promaapp.Model.Product;
import com.proma.promaapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

        lvOrderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected order
                Order selectedOrder = orderList.get(position);

                // Pass the order information to OrderDetailActivity
                Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                intent.putExtra("orderId", selectedOrder.getOrderId());
                intent.putExtra("totalPrice", selectedOrder.getTotalPrice());
                intent.putExtra("productList", (ArrayList<Product>) selectedOrder.getProductList());
                intent.putExtra("storeId", selectedOrder.getStoreId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Retrieve the order history from Firestore whenever the activity starts or resumes
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
                        Log.e("OrderHistoryActivity", "Failed to retrieve order history", task.getException());
                    }
                });
    }

}
