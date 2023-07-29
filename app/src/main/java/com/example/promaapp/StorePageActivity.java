package com.example.promaapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Order;
import com.example.promaapp.Model.OrderListAdapter;
import com.example.promaapp.Model.Product;
import com.example.promaapp.OrderProductActivity;
import com.example.promaapp.ProductPageActivity;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


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
    private LinearLayout barChartLayout;
    private TextView tvTotalRevenue;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String storeId; // Store ID to be passed to Product Page
    private OrderListAdapter orderListAdapter;
    private List<Order> orderList;

    private BarChart chartOrder;
    private EditText fromDateEditText;
    private EditText toDateEditText;
    private String fromDate; // Store selected FromDate as a string
    private String toDate;   // Store selected ToDate as a string
    BarChart barChart;
    ArrayList<String> dates;
    Random random;
    ArrayList<BarEntry> barEntries;
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
        chartOrder = findViewById(R.id.chartOrder);
        fromDateEditText = findViewById(R.id.FromDate);
        toDateEditText = findViewById(R.id.ToDate);
        barChartLayout = findViewById(R.id.barChartLayout);
        orderList = new ArrayList<>();

        orderListAdapter = new OrderListAdapter(this, orderList);

        // Calculate the default dates (30 days ago for FromDate and today for ToDate)
        Calendar calendar = Calendar.getInstance();
        String currentDate = formatDate(calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1, // Months are 0-indexed, so add 1
                calendar.get(Calendar.YEAR));
        toDateEditText.setText(currentDate);

        calendar.add(Calendar.DAY_OF_MONTH, -30); // Go back 30 days from today
        String thirtyDaysAgo = formatDate(calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1, // Months are 0-indexed, so add 1
                calendar.get(Calendar.YEAR));
        fromDateEditText.setText(thirtyDaysAgo);
        retrieveOrderHistory(thirtyDaysAgo, currentDate);
        fromDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDateEditText.clearFocus(); // Clear focus to prevent the keyboard from appearing
                showDatePickerDialog(fromDateEditText);
            }
        });

        toDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDateEditText.clearFocus(); // Clear focus to prevent the keyboard from appearing
                showDatePickerDialog(toDateEditText);
            }
        });
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
            retrieveOrderHistory(fromDateEditText.getText().toString(), toDateEditText.getText().toString());
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
    private void showDatePickerDialog(final EditText editText) {
        editText.clearFocus();

        // Get the current date from the EditText, if it already has a date set
        int year, month, day;
        String dateText = editText.getText().toString();
        if (!dateText.isEmpty()) {
            String[] dateParts = dateText.split("/");
            year = Integer.parseInt(dateParts[2]);
            month = Integer.parseInt(dateParts[1]) - 1; // Months are 0-indexed in DatePickerDialog
            day = Integer.parseInt(dateParts[0]);
        } else {
            // If the EditText is empty, use the current date
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        // Create a new DatePickerDialog and show it
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                StorePageActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Update the EditText with the selected date
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    editText.setText(selectedDate);
                    // Refresh the chart based on the new date selection
                    retrieveOrderHistory(fromDateEditText.getText().toString(), toDateEditText.getText().toString());
                },
                year, month, day
        );
        datePickerDialog.show();
    }
    private String formatDate(int day, int month, int year) {
        // Format the date as "dd/MM/yyyy"
        return String.format("%02d/%02d/%04d", day, month, year);
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
                        barChartLayout.setVisibility(View.VISIBLE);
                    } else {
                        tvNoStore.setVisibility(View.VISIBLE);
                        btnAddStore.setVisibility(View.VISIBLE);
                        tvTitle.setVisibility(View.GONE);
                        btnProductPage.setVisibility(View.GONE);
                        btnOrderPage.setVisibility(View.GONE);
                        btnOrderHistory.setVisibility(View.GONE);
                        barChartLayout.setVisibility(View.GONE);

                    }
                });
    }

    private void retrieveOrderHistory(String fromDate, String toDate) {
        Date fromDateObj = getTimestampFromString(fromDate);
        Date toDateObj = getTimestampFromString(toDate);

        if (fromDateObj == null || toDateObj == null) {
            // Invalid date format or parsing error
            Toast.makeText(StorePageActivity.this, "Invalid date format.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("orders")
                .whereEqualTo("storeId", storeId)
                .whereGreaterThanOrEqualTo("createDate", fromDateObj)
                .whereLessThanOrEqualTo("createDate", toDateObj)
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
                        populateOrderChart(orderList);
                    } else {
                        Toast.makeText(StorePageActivity.this, "Failed to retrieve order history: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("OrderHistoryActivity", "Failed to retrieve order history", task.getException());
                    }
                });
    }

    private Date getTimestampFromString(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void populateOrderChart(List<Order> orderList) {
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < orderList.size(); i++) {
            Order order = orderList.get(i);
            float totalPrice = order.getTotalPrice();
            if (!Float.isNaN(totalPrice) && !Float.isInfinite(totalPrice)) {
                entries.add(new BarEntry(i, totalPrice));
            }
        }
        BarChart chartOrder = findViewById(R.id.chartOrder); // Find the BarChart view by its ID

        if (entries.isEmpty()) {
            chartOrder.setNoDataText("Không có đơn hàng nào trong khoảng thời gian này");
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Tổng Tiền");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS); // Customize the bar color

        BarData barData = new BarData(dataSet);

        chartOrder.setData(barData);
        // Customize the appearance of the chart
        chartOrder.getXAxis().setLabelCount(entries.size() );
        chartOrder.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value ) {
                // You can customize X-axis labels here, e.g., use order dates as labels
                int index = (int) value;
                if (index >= 0 && index < orderList.size()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
                    String createDate = dateFormat.format(orderList.get(index).getCreateDate());
                    return createDate;
                }
                return "";
            }
        });
        chartOrder.getXAxis().setAxisMaximum(barData.getXMax() + 0.5f);
        chartOrder.getXAxis().setAxisMinimum(barData.getXMin() - 0.5f);
        chartOrder.getAxisRight().setEnabled(false);
        chartOrder.getDescription().setEnabled(false);
        chartOrder.invalidate();
    }

}
