package com.example.vopet.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.adapter.WordAdapter;
import com.example.vopet.model.Vocabulary;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PublicTopicDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private List<Vocabulary> wordList;
    FirebaseFirestore db;
    private TextToSpeech textToSpeech;
    String creatorId, topicName;
    private TextView tvTopicName, priority, memoryWord;
    Button btnStudy;
    private Button btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_public_topic_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        topicName = getIntent().getStringExtra("topicName");
        creatorId = getIntent().getStringExtra("creatorId");

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
        btnStudy = findViewById(R.id.btnStudy);
        btnStudy.setOnClickListener(v -> showStudyDialog());
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        wordList = new ArrayList<>();
        wordAdapter = new WordAdapter(this, wordList, textToSpeech);
        recyclerView.setAdapter(wordAdapter);

        // Tham chiếu đến TextViews trong layout
        tvTopicName = findViewById(R.id.topicName);
        priority = findViewById(R.id.priority);
        memoryWord = findViewById(R.id.memoryWord);

        // Hiển thị tên chủ đề
        tvTopicName.setText(topicName);

        // Tải danh sách từ của chủ đề
        loadTopic(topicName, creatorId);
        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(PublicTopicDetailActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });

        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(PublicTopicDetailActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(PublicTopicDetailActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(PublicTopicDetailActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(PublicTopicDetailActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
    }
    private void loadTopic(String folderName, String creatorId) {
        db.collection("topics") // Tên collection trong Firestore
                .whereEqualTo("creatorId", creatorId) // Lọc theo creatorId
                .whereEqualTo("name", folderName) // Lọc theo tên chủ đề
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Truy cập subcollection "vocabularies" của mỗi tài liệu trong "topics"
                            document.getReference().collection("vocabularies")
                                    .get()
                                    .addOnSuccessListener(vocabSnapshots -> {
                                        List<Vocabulary> filteredList = new ArrayList<>();
                                        for (QueryDocumentSnapshot vocabDoc : vocabSnapshots) {
                                            int stt = vocabDoc.getLong("stt").intValue();
                                            String english = vocabDoc.getString("english");
                                            String meaning = vocabDoc.getString("meaning");
                                            String pronounce = vocabDoc.getString("pronounce");
                                            String status = vocabDoc.getString("status");
                                            boolean priority = vocabDoc.getBoolean("priority");
                                            String photoUrl = vocabDoc.getString("photo");

                                            Vocabulary word = new Vocabulary(stt, english, meaning, pronounce, status, priority, photoUrl);
                                            word.setId(vocabDoc.getId());
                                            filteredList.add(word);
                                        }
                                        // Sắp xếp danh sách từ theo thứ tự tăng dần
                                        filteredList.sort((w1, w2) -> Integer.compare(w1.getStt(), w2.getStt()));

                                        // Cập nhật RecyclerView
                                        wordAdapter.updateData(filteredList);

                                        int wordCount = vocabSnapshots.size();

                                        // Đếm số từ ưu tiên
                                        document.getReference().collection("vocabularies")
                                                .whereEqualTo("priority", true)
                                                .get()
                                                .addOnSuccessListener(prioritySnapshots -> {
                                                    int priorityWords = prioritySnapshots.size();
                                                    priority.setText(priorityWords + "/" + wordCount);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to count priority words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });

                                        // Đếm số từ đã ghi nhớ
                                        document.getReference().collection("vocabularies")
                                                .whereEqualTo("status", "Đã thuộc")
                                                .get()
                                                .addOnSuccessListener(statusSnapshots -> {
                                                    int masteredWords = statusSnapshots.size();
                                                    memoryWord.setText(masteredWords + "/" + wordCount);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to count mastered words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to load vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "No topics found for folderName: " + folderName, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
    private void showStudyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_study_with_topic, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        Spinner spinnerStudyMode = dialogView.findViewById(R.id.spinnerStudyMode);
        Button btnStart = dialogView.findViewById(R.id.btnStart);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.study_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudyMode.setAdapter(adapter);

        btnStart.setOnClickListener(v -> {

            Intent intent;
            String selectStudyMode = spinnerStudyMode.getSelectedItem().toString();
            switch (selectStudyMode) {
                case "Flashcard":
                    intent = new Intent(PublicTopicDetailActivity.this, FlashcardWithClickCardActivity.class);
                    break;
                case "Multiple Choice":
                    intent = new Intent(PublicTopicDetailActivity.this, StudyTopicInMulChoiceActivity.class);
                    break;
                case "Type word":
                    intent = new Intent(PublicTopicDetailActivity.this, StudyTopicInFindAndFillActivity.class);
                    break;
                default:
                    Toast.makeText(PublicTopicDetailActivity.this, "Please select a valid option", Toast.LENGTH_SHORT).show();
                    return;
            }

            intent.putExtra("topicName", topicName);
            intent.putExtra("creatorId", creatorId);
            startActivity(intent);

            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}