package com.example.vopet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vopet.activity.ForgotPasswordActivity;
import com.example.vopet.activity.HomeActivity;
import com.example.vopet.activity.SignUpActivity;
import com.example.vopet.sync.SyncManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.vopet.pattern.SessionSingleton;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView link_signup;
    private EditText txtUserName, txtPassword;
    private Button btnLogin;
    private TextView fg_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        link_signup = findViewById(R.id.link_signup);
        txtUserName = findViewById(R.id.txtUserName);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        fg_pw = findViewById(R.id.fg_pw);

        link_signup.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        fg_pw.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String username = txtUserName.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            //Lưu thông tin người dùng vào Singleton
                            SessionSingleton.getInstance().setUser(firebaseUser);

                            SyncManager syncManager = new SyncManager(this);
                            syncManager.syncFromFirebase(SessionSingleton.getInstance().getUserId());
                            syncManager.setupRealtimeSync(SessionSingleton.getInstance().getUserId());

                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // Kiểm tra nếu đã đăng nhập, tự động vào HomeActivity
//        if (SessionSingleton.getInstance().isLoggedIn()) {
//            startActivity(new Intent(MainActivity.this, HomeActivity.class));
//            finish();
//        }
//    }
}