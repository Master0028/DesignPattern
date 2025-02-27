package com.example.vopet.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vopet.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    Button btnEditProfile;
    CircleImageView profile_image;
    TextView txtName, txtEmail, txtPhoneNumber;
    private FirebaseFirestore db;
    private String userId;
    private String profileImageBase64;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("userId");
        loadUserProfile(userId);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);

        profile_image = findViewById(R.id.profile_image);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber);

        int profileImage = getIntent().getIntExtra("profile_image", R.drawable.user);
        profile_image.setImageResource(profileImage);

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnEditProfile){
                    Intent i = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
                    i.putExtra("userId", userId);
                    startActivityForResult(i, 1);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Cập nhật thông tin đã chỉnh sửa
            String updatedName = data.getStringExtra("updatedName");
            profileImageBase64 = data.getStringExtra("profileImageBase64");

            // Cập nhật giao diện người dùng
            txtName.setText(updatedName);
            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profile_image.setImageBitmap(decodedByte);
            }

            // Trả kết quả về ProfileActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedName", updatedName);
            resultIntent.putExtra("profileImageBase64", profileImageBase64);
            setResult(RESULT_OK, resultIntent);
        }
    }

    @Override
    public void onBackPressed() {
        // Đặt kết quả trả về ProfileActivity khi nhấn nút back
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedName", txtName.getText().toString());
        resultIntent.putExtra("profileImageBase64", profileImageBase64);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
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
                            txtName.setText(name);
                            txtEmail.setText(email);
                            txtPhoneNumber.setText(phone);

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

}