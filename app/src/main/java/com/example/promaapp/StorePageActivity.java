package com.example.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StorePageActivity extends AppCompatActivity {

    private TextView tvNoStore;
    private TextView tvTitle;
    private Button btnAddStore;
    private Button btnProductPage; // Button for switching to Product Page
    private Button btnOrderPage; // Button for switching to Order Page
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String storeId; // Store ID to be passed to Product Page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_page);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        tvNoStore = findViewById(R.id.tvNoStore);
        tvTitle = findViewById(R.id.tvTitle);
        btnAddStore = findViewById(R.id.btnAddStore);
        btnProductPage = findViewById(R.id.btnProductPage); // Initialize the Button
        btnOrderPage = findViewById(R.id.btnOrderPage); // Initialize the Button

        btnAddStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to AddStoreActivity
                Intent intent = new Intent(StorePageActivity.this, AddStoreActivity.class);
                startActivity(intent);
            }
        });

        // Retrieve current user's store information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String accountId = currentUser.getUid();
            retrieveStoreInfo(accountId);
        }

        btnProductPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to ProductPageActivity and pass the store ID
                Intent intent = new Intent(StorePageActivity.this, ProductPageActivity.class);
                intent.putExtra("storeId", storeId);
                startActivity(intent);
            }
        });

        btnOrderPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to OrderPageActivity
                // Add your code to switch to the Order Page here
                Intent intent = new Intent(StorePageActivity.this, OrderProductActivity.class);
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
                        // Account has a store
                        tvNoStore.setVisibility(View.GONE);
                        btnAddStore.setVisibility(View.GONE);

                        // Get the first store document
                        DocumentSnapshot storeDocument = value.getDocuments().get(0);
                        String storeName = storeDocument.getString("storeName");

                        // Set the store name as the title
                        tvTitle.setText("Cửa Hàng " + storeName);

                        // Set the store ID
                        storeId = storeDocument.getId();

                        // Display the title, buttons, and store information
                        tvTitle.setVisibility(View.VISIBLE);
                        btnProductPage.setVisibility(View.VISIBLE);
                        btnOrderPage.setVisibility(View.VISIBLE);
                    } else {
                        // No store found for the account
                        tvNoStore.setVisibility(View.VISIBLE);
                        btnAddStore.setVisibility(View.VISIBLE);
                        tvTitle.setVisibility(View.GONE);
                        btnProductPage.setVisibility(View.GONE);
                        btnOrderPage.setVisibility(View.GONE);
                    }
                });
    }
}
