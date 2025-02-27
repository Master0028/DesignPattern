package com.example.vopet.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.MainActivity;
import com.example.vopet.R;
import com.example.vopet.adapter.CategoryAdapter;
import com.example.vopet.adapter.TopicAdapterBlue;
import com.example.vopet.adapter.TopicAdapterBlue1;
import com.example.vopet.model.Category;
import com.example.vopet.model.Topic;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PublicTopicActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TopicAdapterBlue1 topicAdapter;
    private List<Topic> topicList;
    private FirebaseFirestore db;
    String currentUser;
    private TextView tvCountTopic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_public_topic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        tvCountTopic = findViewById(R.id.tvCountTopic);

        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(PublicTopicActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(PublicTopicActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(PublicTopicActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(PublicTopicActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });

        db = FirebaseFirestore.getInstance();
        currentUser = SessionSingleton.getInstance().getUserId();

        Button btnMenu = findViewById(R.id.btnMenu);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        topicList = new ArrayList<>();

        topicAdapter = new TopicAdapterBlue1(this, topicList);
        recyclerView.setAdapter(topicAdapter);


        loadAllTopics();

        btnMenu.setOnClickListener(v -> {
            Dialog dialog = new Dialog(PublicTopicActivity.this);
            dialog.setContentView(R.layout.activity_menu);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                        (int) (getResources().getDisplayMetrics().heightPixels * 0.9)
                );
                window.setGravity(Gravity.START);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.getAttributes().windowAnimations = R.style.DialogAnimation;

                TranslateAnimation slideIn = new TranslateAnimation(-window.getAttributes().width, 0, 0, 0);
                slideIn.setDuration(300);
                window.getDecorView().startAnimation(slideIn);
            }

            Button btnHome = dialog.findViewById(R.id.btnHome);
            btnHome.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, HomeActivity.class);
                startActivity(intent);
            });
            Button btnFlashcard = dialog.findViewById(R.id.btnFlashcard);
            btnFlashcard.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, FlashcardActivity.class);
                startActivity(intent);
            });
            Button btnTopics = dialog.findViewById(R.id.btnTopics);
            btnTopics.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, TopicActivity.class);
                startActivity(intent);
            });
            Button btnFolders = dialog.findViewById(R.id.btnFolders);
            btnFolders.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, FolderActivity.class);
                startActivity(intent);
            });
            Button btnChoice = dialog.findViewById(R.id.btnChoice);
            btnChoice.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, MultipleChoiceActivity.class);
                startActivity(intent);
            });
            Button btnProfile = dialog.findViewById(R.id.btnProfile);
            btnProfile.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
            Button btnFind = dialog.findViewById(R.id.btnFind);
            btnFind.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, FindAndFillActivity.class);
                startActivity(intent);
            });
            Button btnCommunity = dialog.findViewById(R.id.btnCommunity);
            btnCommunity.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, CommunityActivity.class);
                startActivity(intent);
            });
            Button btnLogout = dialog.findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, MainActivity.class);
                startActivity(intent);
            });
            Button btnSetting = dialog.findViewById(R.id.btnSetting);
            btnSetting.setOnClickListener(view -> {
                Intent intent = new Intent(PublicTopicActivity.this, SettingActivity.class);
                startActivity(intent);
            });
            dialog.show();
            dialog.setOnDismissListener(dialogInterface -> {
                TranslateAnimation slideOut = new TranslateAnimation(0, window.getAttributes().width, 0, 0);
                slideOut.setDuration(300);
                window.getDecorView().startAnimation(slideOut);
            });
        });

    }

    private void loadAllTopics() {
        topicList.clear();

        // Truy vấn Firestore để lấy những topic có trường "permission" là "public" và sắp xếp theo thời gian
        db.collection("topics")
                .whereEqualTo("permission", "Public") // Chỉ lấy những topics có quyền truy cập công khai
                .orderBy("createdTime", Query.Direction.DESCENDING) // Sắp xếp giảm dần theo thời gian
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("FirestoreError", "Error loading topics: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        topicList.clear(); // Làm rỗng danh sách để tránh trùng lặp
                        int count = 0;

                        for (QueryDocumentSnapshot topicDoc : queryDocumentSnapshots) {
                            Topic topic = topicDoc.toObject(Topic.class);
                            topic.setId(topicDoc.getId());
                            topicList.add(topic);
                            count++;
                        }

                        tvCountTopic.setText(String.valueOf(count)); // Cập nhật số lượng topic
                        topicAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                    }
                });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadAllTopics();
        }
    }

}