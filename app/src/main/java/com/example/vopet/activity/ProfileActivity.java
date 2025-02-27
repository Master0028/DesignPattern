package com.example.vopet.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vopet.ChangePasswordActivity;
import com.example.vopet.MainActivity;
import com.example.vopet.R;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    Button btnShowUserProfile, btnBackHome, btnChangePassword, btnDelAccount, btnLogout, btnAchievement;
    TextView userName;
    CircleImageView profile_image;
    private FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        btnShowUserProfile = findViewById(R.id.btnShowUserProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDelAccount = findViewById(R.id.btnDelAccount);
        btnAchievement = findViewById(R.id.btnAchievement);
        btnLogout = findViewById(R.id.btnLogout);
        userName = findViewById(R.id.userName);
        profile_image = findViewById(R.id.profile_image);
        btnBackHome = findViewById(R.id.btnBackHome);

        loadUserProfile();

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnChangePassword) {
                    Intent i = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                    i.putExtra("userId", userId);
                    startActivity(i);
                }
            }
        });

        btnAchievement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAchievement) {
                    Intent i = new Intent(ProfileActivity.this, AchievementActivity.class);
                    i.putExtra("userId", userId);
                    startActivity(i);
                }
            }
        });

        btnShowUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnShowUserProfile) {
                    Intent i = new Intent(ProfileActivity.this, UserProfileActivity.class);
                    i.putExtra("userId", userId);
                    startActivityForResult(i, 1);
                }
            }
        });

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnBackHome) {
                    Intent i = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Logout", "Logout button clicked"); // Kiểm tra xem có vào đây không
                Toast.makeText(ProfileActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();

                // Logic chuyển Activity
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
//        btnChangePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(ProfileActivity.this, ResetPasswordActivity.class);
//                startActivity(i);
//            }
//        });

        btnDelAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDelAccDialog();
            }
        });
    }

    private void showDelAccDialog() {
        // Tạo một AlertDialog để xác nhận hành động xóa
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure to delete your account?")
                .setCancelable(false)  // Không cho phép đóng dialog khi nhấn ngoài
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAccount();
                    }
                })
                .setNegativeButton("Cancel", null)  // Nếu người dùng hủy, không làm gì
                .create();

        // Lấy ra các button của dialog
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Lấy các button từ dialog
                Button btnDelete = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);

                btnDelete.setTextColor(getResources().getColor(R.color.blue)); // Màu xanh (thêm màu xanh vào resource)
                btnCancel.setTextColor(getResources().getColor(R.color.dark_red)); // Màu đỏ (thêm màu đỏ vào resource)
            }
        });

        // Hiển thị dialog
        dialog.show();
    }

    private void deleteAccount() {
        // Lấy người dùng hiện tại từ FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = SessionSingleton.getInstance().getUserId();

        if (auth.getCurrentUser() != null) {
            // Xóa dữ liệu người dùng trong Firestore trước
            db.collection("users").document(userId)
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sau khi xóa dữ liệu Firestore, xóa tài khoản từ Firebase Authentication
                            auth.getCurrentUser().delete()
                                    .addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            Toast.makeText(this, "Deleted successfully.", Toast.LENGTH_SHORT).show();
                                            // Chuyển về màn hình đăng nhập sau khi xóa
                                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(this, "Can't be delete this account: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Can't be deleted data of this account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "The account not found.", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadUserProfile() {
        // Lấy thông tin người dùng hiện tại từ FirebaseAuth
        userId = SessionSingleton.getInstance().getUserId();

        // Tham chiếu đến document của người dùng trong Firestore
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Lấy tên người dùng từ document
                            String name = document.getString("username");
                            String profileImageBase64 = document.getString("profileImageBase64");

                            // Hiển thị tên trên TextView
                            userName.setText(name);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Lấy tên người dùng và ảnh đại diện đã chỉnh sửa
            String updatedName = data.getStringExtra("updatedName");
            String updatedProfileImageBase64 = data.getStringExtra("profileImageBase64");
            // Cập nhật giao diện người dùng
            userName.setText(updatedName);

            if (updatedProfileImageBase64 != null && !updatedProfileImageBase64.isEmpty()) {
                byte[] decodedString = Base64.decode(updatedProfileImageBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profile_image.setImageBitmap(decodedByte);
            }
        }
    }

}