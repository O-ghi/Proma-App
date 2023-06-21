package com.example.promaapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.promaapp.Model.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CAMERA = 1;
    private static final int REQUEST_PERMISSION_STORAGE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_IMAGE_SELECTION = 4;

    private EditText etId, etName, etPrice, etQuantity, etExpiry;
    private Button btnSelectImage, btnSave;
    private ImageView ivProductImage;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri imageUri;

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            etExpiry.setText(selectedDate);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String productIdString = getIntent().getStringExtra("productId");
        String storeId = getIntent().getStringExtra("storeId");

        etId = findViewById(R.id.et_id);
        etName = findViewById(R.id.et_name);
        etPrice = findViewById(R.id.et_price);
        etQuantity = findViewById(R.id.et_quantity);
        etExpiry = findViewById(R.id.et_expiry);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSave = findViewById(R.id.btn_save);
        ivProductImage = findViewById(R.id.iv_product_image);

        etId.setText(productIdString);

        etExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelectionDialog();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productId = etId.getText().toString().trim();
                String productName = etName.getText().toString().trim();
                double price = Double.parseDouble(etPrice.getText().toString().trim());
                int quantity = Integer.parseInt(etQuantity.getText().toString().trim());
                String expiryDate =etExpiry.getText().toString().trim();

                if (productId.isEmpty() || productName.isEmpty() || etPrice.getText().toString().isEmpty() || etQuantity.getText().toString().isEmpty()) {
                    Toast.makeText(AddProductActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (imageUri == null) {
                    Toast.makeText(AddProductActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                } else {
                    saveProduct(productId, productName, price, quantity, storeId, expiryDate);
                }
            }
        });
    }
    private void showDatePickerDialog() {
        // Get the current date for initializing the DatePickerDialog
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and show it
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddProductActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etExpiry.setText(selectedDate);
                        Log.d("selectedDate", "check " + etExpiry.getText());

                    }
                },
                year,
                month,
                dayOfMonth
        );

        // Set the minimum date to the current date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }


    private void showImageSelectionDialog() {
        String[] options = {"Take Photo", "Choose from Library"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermission();
            } else if (which == 1) {
                checkStoragePermission();
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
        } else {
            openGallery();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_IMAGE_SELECTION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.containsKey("data")) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivProductImage.setImageBitmap(imageBitmap);
                    imageUri = getImageUri(imageBitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_SELECTION && data != null) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    ivProductImage.setImageBitmap(imageBitmap);
                    imageUri = selectedImageUri;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Image", null);
        return Uri.parse(path);
    }

    private void saveProduct(String productId, String productName, double price, int quantity, String storeId, String expiryDate) {
        CollectionReference productsRef = db.collection("products");

        final StorageReference imageRef = storageReference.child("images/" + UUID.randomUUID().toString());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                String imageUrl = downloadUri.toString();
                                Product product = new Product(productId, storeId, productName, price, quantity, imageUrl, expiryDate);
                                productsRef.document(productId).set(product)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(AddProductActivity.this, "Product saved successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddProductActivity.this, "Failed to save product", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddProductActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
