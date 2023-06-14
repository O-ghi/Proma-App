package com.example.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.promaapp.Model.Store;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStoreActivity extends AppCompatActivity {

    private EditText editTextStoreName;
    private EditText editTextStoreAddress;
    private Button buttonAddStore;
    private Button buttonBack;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_store);

        editTextStoreName = findViewById(R.id.editTextStoreName);
        editTextStoreAddress = findViewById(R.id.editTextStoreAddress);
        buttonAddStore = findViewById(R.id.buttonAddStore);
        buttonBack = findViewById(R.id.buttonBack);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        buttonAddStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStore();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addStore() {
        String storeName = editTextStoreName.getText().toString().trim();
        String storeAddress = editTextStoreAddress.getText().toString().trim();

        // Perform validation if needed

        // Create a new store document in Firestore
        String userId = firebaseAuth.getCurrentUser().getUid();
        String storeId = firestore.collection("Store").document().getId();
        Store store = new Store(storeId, userId, storeName, storeAddress);

        firestore.collection("Store").document(storeId)
                .set(store)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Store added successfully
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // Error adding store
                        // Handle the error case
                    }
                });
    }
}
