package com.proma.promaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private EditText etEmail, etPassword;
    private TextView tvRegister;

    private Button btnLogin;
    private ImageView btnGoogle;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login button click
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                loginUser(email, password);
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Google login/register button click
                signInWithGoogle();
            }
        });

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            // Email or password is empty, show an error message
            Toast.makeText(this, "Hãy nhập đầy đủ Email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            // Start the StoreActivity
                            Intent intent = new Intent(LoginActivity.this, StorePageActivity.class);
                            startActivity(intent);
                            finish(); // Optional: Finish the LoginActivity to prevent going back
                        } else {
                            // Login failed
                            Toast.makeText(LoginActivity.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                            Log.d("LoginActivity", "Error: " + task.getException().getMessage());
                        }
                    }
                });
    }


    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                // Google sign-in successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult();
                firebaseAuthWithGoogle(account);
            } else {
                // Google sign-in failed
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Google sign-in/authenticate successful
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            saveUserInfoToFirestore(acct);
                            Intent intent = new Intent(LoginActivity.this, StorePageActivity.class);
                            startActivity(intent);
                            finish(); // Optional: Finish the LoginActivity to prevent going back
                        } else {
                            // Google sign-in/authenticate failed
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                            Log.d("LoginActivity", "Error: " + task.getException().getMessage());
                        }
                    }
                });
    }
    private void saveUserInfoToFirestore(GoogleSignInAccount account) {
        String fullName = account.getDisplayName();
        String email = account.getEmail();
        String userId = mAuth.getUid();

        // Check if the account already exists in Firestore
        DocumentReference userRef = firestore.collection("Account").document(userId);

        userRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Account already exists, no need to save again
                            // You can perform any additional actions or show a message here
                            // For example:
                        } else {
                            // Account does not exist, save the user information
                            // Create a new Firestore document with the user information
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("fullName", fullName);
                            userData.put("email", email);
                            userData.put("paid", false); // Set default value for isPaid

                            userRef.set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Data saved successfully
                                        // Perform any additional actions here
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to save data
                                        // Handle the failure or show an error message
                                    });
                        }
                    } else {
                        // Failed to read account information
                        // Handle the failure or show an error message
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Google API connection failed
        Toast.makeText(LoginActivity.this, "Không thể kết nối với Google", Toast.LENGTH_SHORT).show();
    }
}
