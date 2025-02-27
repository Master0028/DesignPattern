package com.example.vopet.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.vopet.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserProfileActivity extends AppCompatActivity {
    CircleImageView profile_image;
    Button btnCapture, btnSaveUserProfile;
    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private FirebaseFirestore db;
    EditText ed_userName, ed_email, ed_phoneNumber;
    Uri selectedImageUri;
    TextView tv_username, tv_email, tv_phoneNumber;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user_profile);

        db = FirebaseFirestore.getInstance();
        profile_image = findViewById(R.id.profile_image);
        btnCapture = findViewById(R.id.btnCapture);
        ed_userName = findViewById(R.id.ed_userName);
        ed_email = findViewById(R.id.ed_email);
        ed_phoneNumber = findViewById(R.id.ed_phoneNumber);
        tv_username = findViewById(R.id.tv_username);
        tv_email = findViewById(R.id.tv_email);
        tv_phoneNumber = findViewById(R.id.tv_phoneNumber);
        btnSaveUserProfile = findViewById(R.id.btnSaveUserProfile);

        userId = getIntent().getStringExtra("userId");
        loadUserProfile(userId);

        ed_userName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tv_username.setTextColor(getResources().getColor(R.color.blue));
                } else {
                    tv_username.setTextColor(getResources().getColor(R.color.gray1));
                }
            }
        });
        ed_email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tv_email.setTextColor(getResources().getColor(R.color.blue));
                } else {
                    tv_email.setTextColor(getResources().getColor(R.color.gray1));
                }
            }
        });
        ed_phoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tv_phoneNumber.setTextColor(getResources().getColor(R.color.blue));
                } else {
                    tv_phoneNumber.setTextColor(getResources().getColor(R.color.gray1));
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);

        btnCapture.setOnClickListener(v -> showImagePickerOptions());

        btnSaveUserProfile.setOnClickListener(v -> {
            profile_image.setDrawingCacheEnabled(true); // Bật cache
            profile_image.buildDrawingCache();
            Bitmap profileBitmap = profile_image.getDrawingCache();
            UpdateUserData(userId, profileBitmap);
        });

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showImagePickerOptions() {
        String[] options = {"Take a photo", "Select from library"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Select image source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Chụp ảnh
                        openCamera();
                    } else if (which == 1) {
                        // Chọn từ thư viện
                        openGallery();
                    }

                })
                .show();
    }
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE);
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                selectedImageUri = data.getData();
                profile_image.setImageURI(selectedImageUri);
            } else if (requestCode == CAPTURE_IMAGE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                profile_image.setImageBitmap(imageBitmap);
            }
        }
    }

    private void loadUserProfile(String userId) {
        // Tham chiếu đến document của người dùng trong Firestore
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Lấy tên người dùng từ document
                            String name = document.getString("username");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String profileImageBase64 = document.getString("profileImageBase64");

                            // Hiển thị tên trên TextView
                            ed_userName.setText(name);
                            ed_email.setText(email);
                            ed_phoneNumber.setText(phone);

                            // Nếu có ảnh đại diện (Base64), chuyển đổi sang Bitmap và gán vào CircleImageView
                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                profile_image.setImageBitmap(decodedByte);
                            }
                        } else {
                            Toast.makeText(this, "User profile does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("loadUserProfile", "Error getting user profile", task.getException());
                        Toast.makeText(this, "Failed to load user profile.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UpdateUserData(String userId, Bitmap profileBitmap) {
        String username = ed_userName.getText().toString();
        String email = ed_email.getText().toString();
        String phone = ed_phoneNumber.getText().toString();

        // Input validations
        if (username.isEmpty()) {
            ed_userName.setError("Please enter username");
            ed_userName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            ed_email.setError("Please enter email");
            ed_email.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            ed_phoneNumber.setError("Please enter phone number");
            ed_phoneNumber.requestFocus();
            return;
        } else if (!phone.matches("\\d+")) {
            ed_phoneNumber.setError("Phone number must be only number");
            ed_phoneNumber.requestFocus();
            return;
        } else if (phone.length() != 10) {
            ed_phoneNumber.setError("Phone number must be 10 digits");
            ed_phoneNumber.requestFocus();
            return;
        }

        // Chuyển đổi ảnh đại diện thành chuỗi Base64
        String profileImageBase64 = encodeImageToBase64(profileBitmap);

        // Check if the email already exists in the Firestore database
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Email already exists, check if it's not the current user
                        boolean emailExistsForAnotherUser = false;
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (!documentSnapshot.getId().equals(userId)) {
                                emailExistsForAnotherUser = true;
                                break;
                            }
                        }

                        if (emailExistsForAnotherUser) {
                            ed_email.setError("Email already exists");
                            ed_email.requestFocus();
                            return;
                        }
                    }

                    // If email is valid, proceed with updating the user data
                    DocumentReference userRef = db.collection("users").document(userId);

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("phone", phone);
                    if (profileImageBase64 != null) {
                        userData.put("profileImageBase64", profileImageBase64);
                    }

                    // Update user data in Firestore
                    userRef.update(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User data updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("userId", userId);
                                resultIntent.putExtra("updatedName", username);
                                resultIntent.putExtra("profileImageBase64", profileImageBase64);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error updating user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking email existence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Bạn có thể giảm chất lượng nếu muốn
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}