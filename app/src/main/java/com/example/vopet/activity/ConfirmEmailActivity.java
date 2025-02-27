package com.example.vopet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vopet.R;
import com.google.firebase.auth.FirebaseAuth;

public class ConfirmEmailActivity extends AppCompatActivity {
    private EditText etOTP;
    private Button btnSend;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_email);

        // Cài đặt Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        etOTP = findViewById(R.id.etOTP);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> verifyOTP());
    }

    private void verifyOTP() {
        String otp = etOTP.getText().toString().trim();

        // Kiểm tra xem người dùng đã nhập mã chưa
        if (TextUtils.isEmpty(otp)) {
            Toast.makeText(ConfirmEmailActivity.this, "Please enter OTP code.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mã OTP
        mAuth.verifyPasswordResetCode(otp)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Nếu mã hợp lệ, tiếp tục sang màn hình ResetPasswordActivity
                        String email = task.getResult(); // Lấy email của người dùng
                        Toast.makeText(ConfirmEmailActivity.this, "Valid code. Email: " + email, Toast.LENGTH_SHORT).show();

                        // Chuyển sang màn hình nhập mật khẩu mới
                        Intent intent = new Intent(ConfirmEmailActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("email", email);  // Truyền email qua màn hình ResetPasswordActivity
                        intent.putExtra("otp", otp);
                        startActivity(intent);
                        finish(); // Đóng màn hình hiện tại
                    } else {
                        // Nếu mã không hợp lệ
                        Toast.makeText(ConfirmEmailActivity.this, "Invalid recovery code.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
