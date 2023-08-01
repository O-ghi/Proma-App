package com.proma.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.proma.promaapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView fullNameTextView;
    private TextView emailTextView;
    private ImageView editIcon;


    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fullNameTextView = findViewById(R.id.fullNameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        editIcon = findViewById(R.id.editIcon);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUserPage();
            }
        });

        Button buttonAddStore = findViewById(R.id.buttonAddStore);
        buttonAddStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to the Add Store page
                Intent intent = new Intent(MainActivity.this, StorePageActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Retrieve the user document from Firestore
        String userId = firebaseAuth.getCurrentUser().getUid();
        firestore.collection("Account").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve user data and update the TextViews
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");

                        fullNameTextView.setText(fullName);
                        emailTextView.setText(email);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void navigateToUserPage() {
        Intent userPageIntent = new Intent(this, UserActivity.class);
        startActivity(userPageIntent);
    }
}
