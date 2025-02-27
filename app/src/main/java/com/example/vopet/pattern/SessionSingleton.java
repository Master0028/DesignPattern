package com.example.vopet.pattern;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class SessionSingleton {
    private static SessionSingleton instance;
    private String userId;
    private String userName;
    private String email;

    private SessionSingleton() {}

    public static synchronized SessionSingleton getInstance() {
        if (instance == null) {
            instance = new SessionSingleton();
        }
        return instance;
    }

    // Thiết lập thông tin từ FirebaseUser
    public void setUser(FirebaseUser user) {
        if (user != null) {
            this.userId = user.getUid();
            this.userName = (user.getDisplayName() != null) ? user.getDisplayName() : "Unknown";
            this.email = (user.getEmail() != null) ? user.getEmail() : "No email";
        }
    }

    // Thiết lập thông tin từ dữ liệu tùy chỉnh (không cần Firebase)
    public void setUser(String userId, String userName, String email) {
        this.userId = userId;
        this.userName = userName != null ? userName : "Unknown";
        this.email = email != null ? email : "No email";
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getEmail() { return email; }

    // Kiểm tra xem người dùng có đang đăng nhập không
    public boolean isLoggedIn() {
        return userId != null;
    }

    // Xóa phiên đăng nhập
    public void clearSession() {
        userId = null;
        userName = null;
        email = null;
    }
}
