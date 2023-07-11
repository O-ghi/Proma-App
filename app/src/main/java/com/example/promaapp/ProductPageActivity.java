package com.example.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Product;
import com.example.promaapp.Model.ProductListAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductPageActivity extends AppCompatActivity {

    private ListView lvProductList;
    private List<Product> productList;
    private ProductListAdapter productListAdapter;
    private FirebaseFirestore firestore;
    private Button btnAddProduct;
    private TableLayout tableStatistics;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);

        firestore = FirebaseFirestore.getInstance();
        lvProductList = findViewById(R.id.lvProductList);
        tableStatistics = findViewById(R.id.tableStatistics);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        barChart = findViewById(R.id.barChart);

        // Retrieve the store ID from the intent
        String storeId = getIntent().getStringExtra("storeId");

        productList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(this, productList);
        lvProductList.setAdapter(productListAdapter);

        // Retrieve products for the specified store ID
        retrieveProductsForStore(storeId);

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ScanBarcodeActivity and pass the storeId
                Intent intent = new Intent(ProductPageActivity.this, ScanBarcodeActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            }
        });

        // Handle item click on the product list
        lvProductList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product
                Product product = productList.get(position);

                // Start ProductDetailActivity and pass the product details
                Intent intent = new Intent(ProductPageActivity.this, ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                intent.putExtra("storeId", product.getStoreId());
                intent.putExtra("productName", product.getName());
                intent.putExtra("productPrice", product.getPrice());
                intent.putExtra("productQuantity", product.getQuantity());
                intent.putExtra("imageUrl", product.getImage());
                intent.putExtra("expiry", product.getExpiry());
                startActivity(intent);
            }
        });
    }

    private void retrieveProductsForStore(String storeId) {
        firestore.collection("products")
                .whereEqualTo("storeId", storeId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Error occurred
                            Toast.makeText(ProductPageActivity.this, "Failed to retrieve products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            productList.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String productId = document.getId();
                                String productName = document.getString("name");
                                double productPrice = document.getDouble("price");
                                int productQuantity = Math.toIntExact(document.getLong("quantity"));
                                String imageURL = document.getString("image");
                                String expiry = document.getString("expiry");
                                Product product = new Product(productId, storeId, productName, productPrice, productQuantity, imageURL, expiry);
                                productList.add(product);
                            }

                            // Update the table statistics
                            updateTableStatistics();

                            // Sort the product list by expiry
                            sortProductListByExpiry();

                            productListAdapter.notifyDataSetChanged();

                            // Update the bar chart
                            updateBarChart();
                        } else {
                            // No products found for the store
                            Toast.makeText(ProductPageActivity.this, "No products found for the store", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateTableStatistics() {
        // Clear existing table rows
        tableStatistics.removeAllViews();

        // Create a new row for the headers
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(getResources().getColor(R.color.lightGrey));

        TextView headerProductName = new TextView(this);
        headerProductName.setText("Product Name");
        headerProductName.setPadding(10, 10, 10, 10);
        headerRow.addView(headerProductName);

        TextView headerTotalQuantity = new TextView(this);
        headerTotalQuantity.setText("Total Quantity");
        headerTotalQuantity.setPadding(10, 10, 10, 10);
        headerRow.addView(headerTotalQuantity);

        tableStatistics.addView(headerRow);

        // Calculate the total quantity for each product
        for (Product product : productList) {
            TableRow row = new TableRow(this);

            TextView productName = new TextView(this);
            productName.setText(product.getName());
            productName.setPadding(10, 10, 10, 10);
            row.addView(productName);

            TextView totalQuantity = new TextView(this);
            totalQuantity.setText(String.valueOf(product.getQuantity()));
            totalQuantity.setPadding(10, 10, 10, 10);
            row.addView(totalQuantity);

            tableStatistics.addView(row);
        }
    }

    private void sortProductListByExpiry() {
        Collections.sort(productList, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                String expiry1 = p1.getExpiry();
                String expiry2 = p2.getExpiry();

                if (expiry1 == null && expiry2 == null) {
                    return 0;
                } else if (expiry1 == null) {
                    return 1;
                } else if (expiry2 == null) {
                    return -1;
                } else {
                    return expiry1.compareTo(expiry2);
                }
            }
        });
    }


    private void updateBarChart() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();

        for (int i = 0; i < productList.size(); i++) {
            Product product = productList.get(i);
            int quantity = product.getQuantity();
            barEntries.add(new BarEntry(i, quantity));
            xAxisLabels.add(product.getName());
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Product Quantity");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(getResources().getColor(android.R.color.black));

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setGranularity(1f);
        rightAxis.setGranularityEnabled(true);

        barChart.animateY(1000);
        barChart.invalidate();
    }
}
