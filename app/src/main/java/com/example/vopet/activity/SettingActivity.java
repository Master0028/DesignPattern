package com.example.vopet.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.vopet.MainActivity;
import com.example.vopet.R;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SettingActivity extends AppCompatActivity {

    private Switch darkModeSwitch;
    private RadioGroup textSizeRadioGroup;
    private Button btnDeleteAccount;
    private FirebaseFirestore db;
    private Button btnAppInfo;
    private Button btnClearHistory;
    private Button btnBack;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences("theme_pref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Initialize views
        db = FirebaseFirestore.getInstance();
        darkModeSwitch = findViewById(R.id.switch_dark_mode);
        textSizeRadioGroup = findViewById(R.id.radio_group_text_size);
        btnDeleteAccount = findViewById(R.id.btnDelAccount);
        btnAppInfo = findViewById(R.id.btn_app_info);
        btnClearHistory = findViewById(R.id.btn_clear_history);
        btnBack = findViewById(R.id.btn_back);

        // Set listeners
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("isDarkMode", true);
                Toast.makeText(this, "Dark mode on", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("isDarkMode", false);
                Toast.makeText(this, "Light mode on", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
        });
        float savedTextSize = sharedPreferences.getFloat("textSize", 18f);
        applyTextSizeToAllViews(findViewById(android.R.id.content), savedTextSize);

        textSizeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            float textSize = 18f;
            if (checkedId == R.id.radio_text_size_small) {
                textSize = 16f;
            } else if (checkedId == R.id.radio_text_size_normal) {
                textSize = 20f;
            } else if (checkedId == R.id.radio_text_size_large) {
                textSize = 24f;
            }
            applyTextSizeToAllViews(findViewById(android.R.id.content), textSize);
            editor.putFloat("textSize", textSize);
            editor.apply();
            Toast.makeText(this, "Fontsize: " + textSize + "sp", Toast.LENGTH_SHORT).show();
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                showDelAccDialog();
            }
        });

        btnAppInfo.setOnClickListener(v -> {
            Intent i = new Intent(SettingActivity.this, AppInfoActivity.class);
            startActivity(i);
        });

        btnClearHistory.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = "user-id"; // Thay bằng ID người dùng hiện tại

            db.collection("users").document(userId).collection("history")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                db.collection("users").document(userId).collection("history")
                                        .document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Deleted history successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to delete history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(this, "No history found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


        btnBack.setOnClickListener(v -> {
            finish(); // Go back to the previous activity
        });
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
                                            //Xóa session sau khi xóa tài khoản
                                            SessionSingleton.getInstance().clearSession();
                                            Toast.makeText(this, "Deleted successfully.", Toast.LENGTH_SHORT).show();
                                            // Chuyển về màn hình đăng nhập sau khi xóa
                                            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
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
    private void applyTextSizeToAllViews(View view, float textSize) {
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        } else if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                applyTextSizeToAllViews(((ViewGroup) view).getChildAt(i), textSize);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Apply saved text size globally
        float savedTextSize = sharedPreferences.getFloat("textSize", 18f);
        applyTextSizeToAllViews(findViewById(android.R.id.content), savedTextSize);
    }
}
