package com.example.vopet;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private ImageView currentPassEye, newPassEye, recurrentPassEye;
    private EditText currentPass, newPass, reTypeNewPass;
    private Button changePass;
    private FirebaseUser user;
    private boolean isCurrentPassVisible = false;
    private boolean isNewPassVisible = false;
    private boolean isRecurrentPassVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        // Xác định các thành phần giao diện
        currentPass = findViewById(R.id.currentPass);
        newPass = findViewById(R.id.newPass);
        reTypeNewPass = findViewById(R.id.reTypeNewPass);
        changePass = findViewById(R.id.changePass);
        currentPassEye = findViewById(R.id.currentPassEye);
        newPassEye = findViewById(R.id.newPassEye);
        recurrentPassEye = findViewById(R.id.recurrentPassEye);

        user = FirebaseAuth.getInstance().getCurrentUser();
        setupPasswordVisibilityToggles();
        // Thiết lập sự kiện khi nhấn vào nút "Change Password"
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = currentPass.getText().toString().trim();
                String newPassword = newPass.getText().toString().trim();
                String reTypePassword = reTypeNewPass.getText().toString().trim();

                // Kiểm tra các trường dữ liệu đã được điền đầy đủ
                if (TextUtils.isEmpty(currentPassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "Please enter your current password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "Please enter your new password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(reTypePassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "Please re-enter your new password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(reTypePassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.equals(currentPassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "New password must be different from the current password", Toast.LENGTH_SHORT).show();
                    return;
                }

                reauthenticateAndChangePassword(currentPassword, newPassword);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void setupPasswordVisibilityToggles() {
        currentPassEye.setOnClickListener(view -> togglePasswordVisibility(currentPass, currentPassEye, isCurrentPassVisible = !isCurrentPassVisible));
        newPassEye.setOnClickListener(view -> togglePasswordVisibility(newPass, newPassEye, isNewPassVisible = !isNewPassVisible));
        recurrentPassEye.setOnClickListener(view -> togglePasswordVisibility(reTypeNewPass, recurrentPassEye, isRecurrentPassVisible = !isRecurrentPassVisible));
    }
    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.baseline_visibility_off_24);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.baseline_remove_red_eye_24);
        }
        // Đặt con trỏ ở cuối đoạn văn bản sau khi thay đổi loại input
        passwordField.setSelection(passwordField.length());
    }

    // Xác thực lại người dùng và thay đổi mật khẩu
    private void reauthenticateAndChangePassword(String currentPassword, String newPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Người dùng đã được xác thực lại thành công, tiến hành thay đổi mật khẩu
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                                        finish(); // Kết thúc hoạt động sau khi cập nhật mật khẩu thành công
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Re-authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
