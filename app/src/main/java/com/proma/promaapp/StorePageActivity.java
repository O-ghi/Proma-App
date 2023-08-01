package com.proma.promaapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.proma.promaapp.Model.Order;
import com.proma.promaapp.Model.OrderListAdapter;
import com.proma.promaapp.Model.Product;
import com.proma.promaapp.R;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class StorePageActivity extends AppCompatActivity {

    private TextView tvNoStore;
    private TextView tvTitle;
    private Button btnAddStore;
    private Button btnProductPage;
    private Button btnOrderPage;
    private Button btnOrderHistory;
    private LinearLayout barChartLayout;
    private TextView tvTotalRevenue;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String storeId;
    private OrderListAdapter orderListAdapter;
    private List<Order> orderList;

    private BarChart chartOrder;
    private EditText fromDateEditText;
    private EditText toDateEditText;
    private String fromDate;
    private String toDate;
    private BarChart barChart;
    private ArrayList<String> dates;
    private ArrayList<BarEntry> barEntries;
    private String thirtyDaysAgo;
    private String currentDate;

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

        Calendar calendar = Calendar.getInstance();
        currentDate = formatDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));
        toDateEditText.setText(currentDate);

        calendar.add(Calendar.DAY_OF_MONTH, -30);
        thirtyDaysAgo = formatDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));
        fromDateEditText.setText(thirtyDaysAgo);

        fromDateEditText.setOnClickListener(v -> {
            fromDateEditText.clearFocus();
            showDatePickerDialog(fromDateEditText);
        });

        toDateEditText.setOnClickListener(v -> {
            toDateEditText.clearFocus();
            showDatePickerDialog(toDateEditText);
        });

        btnAddStore.setOnClickListener(v -> {
            Intent intent = new Intent(StorePageActivity.this, AddStoreActivity.class);
            startActivity(intent);
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String accountId = currentUser.getUid();
            retrieveAccountInfo(accountId);
        }

        btnProductPage.setOnClickListener(v -> {
            Intent intent = new Intent(StorePageActivity.this, ProductPageActivity.class);
            intent.putExtra("storeId", storeId);
            startActivity(intent);
        });

        btnOrderPage.setOnClickListener(v -> {
            Intent intent = new Intent(StorePageActivity.this, OrderProductActivity.class);
            intent.putExtra("storeId", storeId);
            startActivity(intent);
        });

        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(StorePageActivity.this, OrderHistoryActivity.class);
            intent.putExtra("storeId", storeId);
            startActivity(intent);
        });
    }

    private void showDatePickerDialog(final EditText editText) {
        editText.clearFocus();

        int year, month, day;
        String dateText = editText.getText().toString();
        if (!dateText.isEmpty()) {
            String[] dateParts = dateText.split("/");
            year = Integer.parseInt(dateParts[2]);
            month = Integer.parseInt(dateParts[1]) - 1;
            day = Integer.parseInt(dateParts[0]);
        } else {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                StorePageActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    editText.setText(selectedDate);
                    retrieveOrderHistory(fromDateEditText.getText().toString(), toDateEditText.getText().toString());
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private String formatDate(int day, int month, int year) {
        return String.format("%02d/%02d/%04d", day, month, year);
    }

    private void retrieveAccountInfo(String accountId) {
        firestore.collection("Account")
                .document(accountId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            boolean isPaid = document.getBoolean("isPaid");
                            if (isPaid) {
                                retrieveStoreInfo(accountId);
                            } else {
                                tvNoStore.setVisibility(View.VISIBLE);
                                btnAddStore.setVisibility(View.GONE);
                                btnProductPage.setVisibility(View.GONE);
                                btnOrderPage.setVisibility(View.GONE);
                                btnOrderHistory.setVisibility(View.GONE);
                                barChartLayout.setVisibility(View.GONE);

                                tvTitle.setVisibility(View.GONE);
                                tvNoStore.setText("Bạn Chưa Được Cấp Quyền Sử Dụng Ứng Dụng.");
                            }
                        } else {
                            Toast.makeText(StorePageActivity.this, "Account information not found.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(StorePageActivity.this, "Failed to retrieve account information.", Toast.LENGTH_SHORT).show();
                        finish();
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
                        retrieveOrderHistory(thirtyDaysAgo, currentDate);
                        retrieveProducts();

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
        toDateObj.setHours(24);
        if (fromDateObj == null || toDateObj == null) {
            Toast.makeText(StorePageActivity.this, "Invalid date format.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("orders")
                .whereEqualTo("storeId", storeId)
                .whereGreaterThanOrEqualTo("createDate", fromDateObj)
                .whereLessThanOrEqualTo("createDate", toDateObj)
                .orderBy("createDate")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int orderId = document.getLong("orderId").intValue();
                            float totalPrice = document.getLong("totalPrice");
                            List<Product> productList = (List<Product>) document.get("productList");
                            String storeId = document.getString("storeId");

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

    private void retrieveProducts() {
        firestore.collection("products")
                .whereEqualTo("storeId", storeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int nearlyExpiredCount = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Date expiryDate = getTimestampFromString(document.getString("expiry"));
                            if (expiryDate != null) {
                                long timeDifference = expiryDate.getTime() - System.currentTimeMillis();
                                if (timeDifference > 0 && timeDifference <= 30L * 24L * 60L * 60L * 1000L) {
                                    nearlyExpiredCount++;
                                }
                            }
                        }
                        updateNearlyExpiredCountTextView(nearlyExpiredCount);
                    } else {
                        Toast.makeText(StorePageActivity.this, "Failed to retrieve products: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e("StorePageActivity", "Failed to retrieve products", task.getException());
                    }
                });
    }

    private void updateNearlyExpiredCountTextView(int count) {
        TextView tvNearlyExpiredCount = findViewById(R.id.tvNearlyExpiredCount);
        if (count > 0) {
            String countText = "Có " + count + " sản phẩm gần hết hạn";
            tvNearlyExpiredCount.setText(countText);
            tvNearlyExpiredCount.setVisibility(View.VISIBLE);

            // Apply additional styling to make the text more prominent
            tvNearlyExpiredCount.setTextColor(Color.WHITE); // Set text color to white
            tvNearlyExpiredCount.setBackgroundColor(Color.RED); // Set background color to red
            tvNearlyExpiredCount.setPadding(16, 8, 16, 8); // Add padding to the text view
        } else {
            tvNearlyExpiredCount.setVisibility(View.GONE);
        }
    }

    private void populateOrderChart(List<Order> orderList) {
        List<BarEntry> entries = new ArrayList<>();
        List<Order> tmpOrders = new ArrayList<>();
        for (int i = 0; i < orderList.size(); i++) {
            Order order = orderList.get(i);
            float totalPrice = order.getTotalPrice();

            if (!Float.isNaN(totalPrice) && !Float.isInfinite(totalPrice)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String createDate = dateFormat.format(order.getCreateDate());
                boolean foundDate = false;

                for (Order ordertmp: tmpOrders) {
                    String createDatetmp = dateFormat.format(ordertmp.getCreateDate());
                    if (createDatetmp.equals(createDate)) {
                        float total = ordertmp.getTotalPrice() + order.getTotalPrice();
                        ordertmp.setTotalPrice(total);
                        foundDate = true;
                        break;
                    }
                }
                if (!foundDate) {
                    Order o = new Order();
                    o.setCreateDate(order.getCreateDate());
                    o.setTotalPrice(order.getTotalPrice());
                    tmpOrders.add(o);
                }
            }
        }

        for(int i = 0; i < tmpOrders.size(); i++) {
            Order order = tmpOrders.get(i);
            float totalPrice = order.getTotalPrice();
            if(!Float.isNaN(totalPrice) && !Float.isInfinite(totalPrice)) {
                entries.add(new BarEntry(i, totalPrice));
            }
        }
        BarChart chartOrder = findViewById(R.id.chartOrder); // Find the BarChart view by its ID

        if (entries.isEmpty()) {
            chartOrder.setNoDataText("Không có đơn hàng nào trong khoảng thời gian này");
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Tổng Tiền");
        dataSet.setColors(Color.rgb(30,150,243)); // Customize the bar color

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
