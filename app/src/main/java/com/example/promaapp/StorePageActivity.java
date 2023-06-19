package com.example.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Store;
import com.example.promaapp.Model.StoreListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class StorePageActivity extends AppCompatActivity {

    private TextView tvNoStore;
    private ListView lvStoreList;
    private Button btnAddStore;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private List<Store> storeList;
    private StoreListAdapter storeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_page);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        tvNoStore = findViewById(R.id.tvNoStore);
        lvStoreList = findViewById(R.id.lvStoreList);
        btnAddStore = findViewById(R.id.btnAddStore);

        btnAddStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to AddStoreActivity
                Intent intent = new Intent(StorePageActivity.this, AddStoreActivity.class);
                startActivity(intent);
            }
        });

        storeList = new ArrayList<>();
        storeListAdapter = new StoreListAdapter(this, storeList);
        lvStoreList.setAdapter(storeListAdapter);

        // Retrieve current user's store information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String accountId = currentUser.getUid();
            retrieveStoreInfo(accountId);
        }

        lvStoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle store item click
                Store selectedStore = storeList.get(position);

                // Navigate to ProductPageActivity and pass the selected store's ID
                Intent intent = new Intent(StorePageActivity.this, ProductPageActivity.class);
                intent.putExtra("storeId", selectedStore.getStoreId());
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
                        // Account has one or more stores
                        tvNoStore.setVisibility(View.GONE);
                        lvStoreList.setVisibility(View.VISIBLE);
                        btnAddStore.setVisibility(View.VISIBLE);

                        storeList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            String storeId = document.getId();
                            String storeName = document.getString("storeName");
                            String storeAddress = document.getString("storeAddress");

                            Store store = new Store(storeId, accountId, storeName, storeAddress);
                            storeList.add(store);
                        }

                        storeListAdapter.notifyDataSetChanged();
                    } else {
                        // No stores found for the account
                        tvNoStore.setVisibility(View.VISIBLE);
                        lvStoreList.setVisibility(View.GONE);
                        btnAddStore.setVisibility(View.VISIBLE);
                    }
                });
    }
}
