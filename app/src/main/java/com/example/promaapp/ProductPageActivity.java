package com.example.promaapp;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Product;
import com.example.promaapp.Model.ProductListAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductPageActivity extends AppCompatActivity {

    private ListView lvProductList;
    private List<Product> productList;
    private ProductListAdapter productListAdapter;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);

        firestore = FirebaseFirestore.getInstance();
        lvProductList = findViewById(R.id.lvProductList);

        // Retrieve the store ID from the intent
        String storeId = getIntent().getStringExtra("storeId");

        productList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(this, productList);
        lvProductList.setAdapter(productListAdapter);

        // Retrieve products for the specified store ID
        retrieveProductsForStore(storeId);
    }

    private void retrieveProductsForStore(String storeId) {
        firestore.collection("Products")
                .whereEqualTo("storeId", storeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            productList.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String productId = document.getId();
                                String productName = document.getString("name");
                                double productPrice = document.getDouble("price");
                                int productQuantity = Math.toIntExact(document.getLong("total"));

                                Product product = new Product(productId, productName, productPrice, productQuantity);
                                productList.add(product);
                            }

                            productListAdapter.notifyDataSetChanged();
                        } else {
                            // No products found for the store
                            Toast.makeText(ProductPageActivity.this, "No products found for the store", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProductPageActivity.this, "Failed to retrieve products", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
