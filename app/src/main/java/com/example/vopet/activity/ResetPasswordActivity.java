package com.example.vopet.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vopet.R;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText etPassword;
    private Button btnSend;
    private FirebaseAuth mAuth;
    private String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        // Cài đặt Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String otp = getIntent().getStringExtra("otp");

        etPassword = findViewById(R.id.etPassword);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = etPassword.getText().toString().trim();
                auth.confirmPasswordReset(otp, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPasswordActivity.this, "Password reset successful.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, "Error resetting password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

}
