package com.example.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Order;
import com.example.promaapp.Model.OrderListAdapter;
import com.example.promaapp.Model.Product;
import com.example.promaapp.OrderProductActivity;
import com.example.promaapp.ProductPageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

public class StorePageActivity extends AppCompatActivity {

    private TextView tvNoStore;
    private TextView tvTitle;
    private Button btnAddStore;
    private Button btnProductPage; // Button for switching to Product Page
    private Button btnOrderPage; // Button for switching to Order Page
    private Button btnOrderHistory;
    private TextView tvTotalRevenue;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String storeId; // Store ID to be passed to Product Page
    private List<Order> orderList;
    private OrderListAdapter orderListAdapter;
    private float totalRevenue;
    private BarChart chartOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_page);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        tvNoStore = findViewById(R.id.tvNoStore);
        tvTitle = findViewById(R.id.tvTitle);
        btnAddStore = findViewById(R.id.btnAddStore);
        btnProductPage = findViewById(R.id.btnProductPage);
        btnOrderPage = findViewById(R.id.btnOrderPage);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        chartOrder = findViewById(R.id.chartOrder);

        btnAddStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to AddStoreActivity
                Intent intent = new Intent(StorePageActivity.this, AddStoreActivity.class);
                startActivity(intent);
            }
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String accountId = currentUser.getUid();
            retrieveStoreInfo(accountId);
            retrieveOrderHistory();
        }

        btnProductPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StorePageActivity.this, ProductPageActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            }
        });

        btnOrderPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StorePageActivity.this, OrderProductActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            }
        });

        btnOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StorePageActivity.this, OrderHistoryActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            }
        });
    }

    private void retrieveStoreInfo(String accountId) {
        firestore.collection("Store")
                .whereEqualTo("accountId", accountId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(StorePageActivity.this, "Failed to retrieve stores", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        tvNoStore.setVisibility(View.GONE);
                        btnAddStore.setVisibility(View.GONE);

                        DocumentSnapshot storeDocument = value.getDocuments().get(0);
                        String storeName = storeDocument.getString("storeName");

                        tvTitle.setText("Cửa Hàng " + storeName);

                        storeId = storeDocument.getId();

                        tvTitle.setVisibility(View.VISIBLE);
                        btnProductPage.setVisibility(View.VISIBLE);
                        btnOrderPage.setVisibility(View.VISIBLE);
                        btnOrderHistory.setVisibility(View.VISIBLE);
                    } else {
                        tvNoStore.setVisibility(View.VISIBLE);
                        btnAddStore.setVisibility(View.VISIBLE);
                        tvTitle.setVisibility(View.GONE);
                        btnProductPage.setVisibility(View.GONE);
                        btnOrderPage.setVisibility(View.GONE);
                        btnOrderHistory.setVisibility(View.GONE);
                    }
                });
    }

    private void retrieveOrderHistory() {
        firestore.collection("orders")
                .whereEqualTo("storeId", storeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String orderId = document.getId();
                            float totalPrice = document.getLong("totalPrice");

                            Order order = new Order(orderId, totalPrice);
                            orderList.add(order);
                        }

                        populateOrderChart(orderList);
                    } else {
                        Toast.makeText(StorePageActivity.this, "Failed to retrieve order history: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("OrderHistoryActivity", "Failed to retrieve order history", task.getException());
                    }
                });
    }

    private void populateOrderChart(List<Order> orderList) {
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < orderList.size(); i++) {
            Order order = orderList.get(i);
            entries.add(new BarEntry(i, order.getTotalPrice()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Order Amounts");
        BarData barData = new BarData(dataSet);

        chartOrder.setData(barData);

        Description chartDescription = new Description();
        chartDescription.setText("Order Amounts");
        chartOrder.setDescription(chartDescription);

        chartOrder.invalidate();
    }
}
