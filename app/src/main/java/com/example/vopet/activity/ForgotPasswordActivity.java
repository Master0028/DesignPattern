package com.example.vopet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vopet.MainActivity;
import com.example.vopet.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etPassword;
    private Button btnSend;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        // Cài đặt Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Gán các thành phần giao diện
        etPassword = findViewById(R.id.etPassword);
        btnSend = findViewById(R.id.btnSend);

        // Xử lý khi bấm nút "Send"
        btnSend.setOnClickListener(v -> {
            String email = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi yêu cầu đặt lại mật khẩu qua email
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Email sent successfully, please access your email!",
                                    Toast.LENGTH_LONG).show();
                            startActivity(intent);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Email sending failed. Please check your email again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
