package com.example.vopet.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vopet.MainActivity;
import com.example.vopet.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnHome = findViewById(R.id.btnHome);
        Button btnTopics = findViewById(R.id.btnTopics);
        Button btnFolders = findViewById(R.id.btnFolders);
        Button btnFlashcard = findViewById(R.id.btnFlashcard);
        Button btnChoice = findViewById(R.id.btnChoice);
        Button btnFind = findViewById(R.id.btnFind);
        Button btnCommunity = findViewById(R.id.btnCommunity);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnSetting = findViewById(R.id.btnSetting);

        ObjectAnimator moveTextAnimator = ObjectAnimator.ofFloat(btnHome, "translationX", 40f);
        ObjectAnimator moveTextAnimator1 = ObjectAnimator.ofFloat(btnTopics, "translationX", 40f);
        ObjectAnimator moveTextAnimator2 = ObjectAnimator.ofFloat(btnFolders, "translationX", 40f);
        ObjectAnimator moveTextAnimator3 = ObjectAnimator.ofFloat(btnFlashcard, "translationX", 40f);
        ObjectAnimator moveTextAnimator4 = ObjectAnimator.ofFloat(btnChoice, "translationX", 40f);
        ObjectAnimator moveTextAnimator5 = ObjectAnimator.ofFloat(btnFind, "translationX", 40f);
        ObjectAnimator moveTextAnimator6 = ObjectAnimator.ofFloat(btnCommunity, "translationX", 40f);
        ObjectAnimator moveTextAnimator7 = ObjectAnimator.ofFloat(btnProfile, "translationX", 40f);
        ObjectAnimator moveTextAnimator8 = ObjectAnimator.ofFloat(btnLogout, "translationX", 40f);
        ObjectAnimator moveTextAnimator9 = ObjectAnimator.ofFloat(btnSetting, "translationX", 40f);

        moveTextAnimator.setDuration(100);
        moveTextAnimator1.setDuration(100);
        moveTextAnimator2.setDuration(100);
        moveTextAnimator3.setDuration(100);
        moveTextAnimator4.setDuration(100);
        moveTextAnimator5.setDuration(100);
        moveTextAnimator6.setDuration(100);
        moveTextAnimator7.setDuration(100);
        moveTextAnimator8.setDuration(100);
        moveTextAnimator9.setDuration(100);


        btnHome.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator.reverse();
                        break;
                }
                return false;
            }
        });
        btnTopics.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator1.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator1.reverse();
                        break;
                }
                return false;
            }
        });
        btnFolders.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator2.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator2.reverse();
                        break;
                }
                return false;
            }
        });
        btnFlashcard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator3.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator3.reverse();
                        break;
                }
                return false;
            }
        });
        btnChoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator4.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator4.reverse();
                        break;
                }
                return false;
            }
        });
        btnFind.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator5.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator5.reverse();
                        break;
                }
                return false;
            }
        });
        btnCommunity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator6.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator6.reverse();
                        break;
                }
                return false;
            }
        });
        btnProfile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator7.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator7.reverse();
                        break;
                }
                return false;
            }
        });
        btnLogout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator8.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator8.reverse();
                        break;
                }
                return false;
            }
        });
        btnSetting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moveTextAnimator9.start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        moveTextAnimator9.reverse();
                        break;
                }
                return false;
            }
        });


        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(MenuActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnFolders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnFolders) {
                    Intent i = new Intent(MenuActivity.this, FolderActivity.class);
                    startActivity(i);
                }
            }
        });
        btnChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnChoice) {
                    Intent i = new Intent(MenuActivity.this, MultipleChoiceActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(MenuActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Logout", "Logout button clicked"); // Kiểm tra xem có vào đây không
                Toast.makeText(MenuActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();

                // Logic chuyển Activity
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnSetting) {
                    Intent i = new Intent(MenuActivity.this, SettingActivity.class);
                    startActivity(i);
                }
            }
        });

    }
}