package com.example.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Product;
import com.example.promaapp.Model.ProductListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductPageActivity extends AppCompatActivity {

    private ListView lvProductList;
    private List<Product> productList;
    private ProductListAdapter productListAdapter;
    private FirebaseFirestore firestore;
    private Button btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);

        firestore = FirebaseFirestore.getInstance();
        lvProductList = findViewById(R.id.lvProductList);
        btnAddProduct = findViewById(R.id.btnAddProduct);

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
                                Product product = new Product(productId, storeId, productName, productPrice, productQuantity,imageURL,expiry);
                                productList.add(product);
                            }

                            productListAdapter.notifyDataSetChanged();
                        } else {
                            // No products found for the store
                            Toast.makeText(ProductPageActivity.this, "No products found for the store", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
