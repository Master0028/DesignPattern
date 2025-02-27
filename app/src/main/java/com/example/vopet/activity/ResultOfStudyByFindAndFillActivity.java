package com.example.vopet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.model.HistoryStudy;
import com.example.vopet.model.WordLearned;
import com.example.vopet.adapter.WordLearnedAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ResultOfStudyByFindAndFillActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WordLearnedAdapter wordLearnedAdapter;
    private List<WordLearned> wordLearnedList;
    private int countCorrect, totalQuestions;
    private TextView tvCorrect, tvTotal, tvTopicName;
    private ProgressBar progressBar;
    private String topicName, userId;
    private FirebaseFirestore db;
    private Button btnRestart;
    private int totalScore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result_of_study_by_find_and_fill);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // Nhận dữ liệu từ Intent
        totalScore = getIntent().getIntExtra("totalScore", 0);
        countCorrect = getIntent().getIntExtra("countCorrect", 0);
        totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
        topicName = getIntent().getStringExtra("topicName");
        userId = getIntent().getStringExtra("userId");
        wordLearnedList = getIntent().getParcelableArrayListExtra("wordLearnedList");

        tvTopicName = findViewById(R.id.tvTopicName);
        tvCorrect = findViewById(R.id.tvCorrect);
        tvTotal = findViewById(R.id.tvTotal);
        progressBar = findViewById(R.id.progressCompletedWords);
        btnRestart = findViewById(R.id.btnRestart);


        tvCorrect.setText(String.valueOf(countCorrect));
        tvTotal.setText(String.valueOf(totalQuestions));
        tvTopicName.setText(topicName);

        int progress = (int) Math.round(((double) countCorrect / totalQuestions) * 100);
        progressBar.setProgress(progress);

        // Hiển thị danh sách từ đã học
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        wordLearnedAdapter = new WordLearnedAdapter(wordLearnedList);
        recyclerView.setAdapter(wordLearnedAdapter);
        btnRestart.setOnClickListener(v -> {
            saveHistoryToFirebase();
        });

        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(ResultOfStudyByFindAndFillActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(ResultOfStudyByFindAndFillActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(ResultOfStudyByFindAndFillActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(ResultOfStudyByFindAndFillActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(ResultOfStudyByFindAndFillActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void saveHistoryToFirebase() {
        db.collection("topics")
                .whereEqualTo("name", topicName)
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        String creatorId = queryDocumentSnapshots.getDocuments().get(0).getString("creatorId");

                        // Truy vấn để đếm số từ vựng có status là "Đã thuộc"
                        db.collection("topics").document(topicId).collection("vocabularies")
                                .whereEqualTo("status", "Đã thuộc")
                                .get()
                                .addOnSuccessListener(vocabSnapshots -> {
                                    int memoriedWords = vocabSnapshots.size(); // Số từ "Đã thuộc"

                                    // Lấy tất cả lịch sử học (không dùng `orderBy`)
                                    db.collection("users").document(userId).collection("history")
                                            .whereEqualTo("typeStudy", "Find and Fill")
                                            .get()
                                            .addOnSuccessListener(historySnapshots -> {
                                                int maxTimes = 0; // Giá trị `times` lớn nhất
                                                for (QueryDocumentSnapshot document : historySnapshots) {
                                                    String lastTimes = document.getString("times");
                                                    if (lastTimes != null && lastTimes.matches("\\d+")) {
                                                        maxTimes = Math.max(maxTimes, Integer.parseInt(lastTimes));
                                                    }
                                                }

                                                // Tạo đối tượng HistoryStudy mới
                                                int newTimes = maxTimes + 1;
                                                addHistoryToFirestore(newTimes, memoriedWords, totalQuestions, creatorId);
                                            })
                                            .addOnFailureListener(e -> {
                                                // Nếu truy vấn thất bại, mặc định với times = 1
                                                Log.e("HistoryError", "Failed to query history: " + e.getMessage());
                                                addHistoryToFirestore(1, memoriedWords, totalQuestions, creatorId);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to count memoried words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Topic not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to query topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addHistoryToFirestore(int times, int memoriedWords, int totalWords, String creatorId) {
        // Tạo đối tượng HistoryStudy mới
        HistoryStudy history = new HistoryStudy(
                String.valueOf(times),
                creatorId,
                topicName,
                "Find and Fill",
                countCorrect,
                memoriedWords,
                (int) Math.round(((double) countCorrect / totalWords) * 100),
                (int) Math.round(((double) memoriedWords / totalWords) * 100),
                totalScore
        );

        // Lưu lịch sử học vào Firestore
        db.collection("users").document(userId).collection("history")
                .add(history)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "History saved successfully!", Toast.LENGTH_SHORT).show();

                    // Cập nhật thuộc tính `access` của topic
                    updateTopicAccess(creatorId, topicName);
                    updateTopicProgress(userId, topicName, (int) Math.round(((double) countCorrect / totalWords) * 100));

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTopicAccess(String creatorId, String topicName) {
        Toast.makeText(this, topicName + ": " + creatorId, Toast.LENGTH_SHORT).show();
        db.collection("topics")
                .whereEqualTo("ownerId", creatorId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy tài liệu đầu tiên (giả định mỗi `creatorId` và `topicName` chỉ có một tài liệu)
                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        int currentAccess = queryDocumentSnapshots.getDocuments().get(0).getLong("access").intValue();

                        // Tăng giá trị `access` lên 1
                        db.collection("topics").document(topicId)
                                .update("access", currentAccess + 1)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("UpdateAccess", "Access count updated successfully!");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UpdateAccess", "Failed to update access count: " + e.getMessage());
                                });
                    } else {
                        Log.e("UpdateAccess", "No topic found with given creatorId and topicName!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateAccess", "Failed to query topic: " + e.getMessage());
                });
    }

    private void updateTopicProgress(String creatorId, String topicName, int progress) {
        Toast.makeText(this, topicName + ": " + creatorId, Toast.LENGTH_SHORT).show();
        db.collection("topics")
                .whereEqualTo("ownerId", creatorId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy tài liệu đầu tiên (giả định mỗi `creatorId` và `topicName` chỉ có một tài liệu)
                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Tăng giá trị `access` lên 1
                        db.collection("topics").document(topicId)
                                .update("progress", progress)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("progress", "progress count updated successfully!");
                                    restartStudy();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("progress", "Failed to update progress count: " + e.getMessage());
                                });
                    } else {
                        Log.e("progress", "No topic found with given creatorId and topicName!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateAccess", "Failed to query topic: " + e.getMessage());
                });
    }


    private void restartStudy() {
        Intent intent = new Intent(ResultOfStudyByFindAndFillActivity.this, StudyTopicInFindAndFillActivity.class);
        intent.putExtra("topicName", topicName);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish(); // Đóng màn hiện tại
    }

}