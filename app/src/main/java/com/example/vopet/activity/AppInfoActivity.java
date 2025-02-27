package com.example.vopet.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vopet.R;

public class AppInfoActivity extends AppCompatActivity {

    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_app);

        // Initialize views
        btnBack = findViewById(R.id.btn_back);

        // Set back button listener
        btnBack.setOnClickListener(v -> finish());
    }
}
