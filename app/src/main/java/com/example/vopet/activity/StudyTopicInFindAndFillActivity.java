package com.example.vopet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

import com.example.vopet.model.HistoryStudy;
import com.example.vopet.R;
import com.example.vopet.adapter.CategoryAdapterInFlashcard;
import com.example.vopet.adapter.MultiViewInFindAndFillAdapter;
import com.example.vopet.model.Category;
import com.example.vopet.pattern.SessionSingleton;
import com.example.vopet.pattern.command.CommandButton;
import com.example.vopet.pattern.command.IBundleProvider;
import com.example.vopet.pattern.command.OpenActivityCommand;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudyTopicInFindAndFillActivity extends AppCompatActivity implements IBundleProvider {
    Spinner spinner;
    CategoryAdapterInFlashcard categoryAdapterInFlashcard;
    private RecyclerView recyclerView;
    private MultiViewInFindAndFillAdapter adapter;
    private List<HistoryStudy> historyStudyList;
    private Button btnBack, btnShare;
    private CheckBox checkBoxShuffle, checkBoxOnlyPriorityWords;
    private TextView level, tvTopicName, priority, memoryWord, learnPercent, progressBar;
    private String topicName, userId;
    private FirebaseFirestore db;
    private boolean isShuffleChecked = false;
    private boolean isOnlyPriorityWordsChecked = false;
    private boolean isAutoSurfChecked = false;
    private ProgressBar circularProgressBar, progressBar1, progressBar2;

    private CommandButton btnStart;

    @Override
    public Bundle getBundle() {
        Bundle bundle = new Bundle();

        boolean isShuffle = checkBoxShuffle.isChecked();
        boolean isOnlyPriorityWords = checkBoxOnlyPriorityWords.isChecked();
        String selection = spinner.getSelectedItem().toString();

        // Gửi thông tin qua Intent
        bundle.putBoolean("isShuffle", isShuffle);
        bundle.putBoolean("isOnlyPriorityWords", isOnlyPriorityWords);
        bundle.putString("selection", selection);
        bundle.putString("topicName", topicName);
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_topic_in_find_and_fill);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        ImageButton btnAdd = findViewById(R.id.btnAdd);

        topicName = getIntent().getStringExtra("topicName");
        userId = SessionSingleton.getInstance().getUserId();
        db = FirebaseFirestore.getInstance();

        spinner = findViewById(R.id.spinner);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnStart = findViewById(R.id.btnStart);
        level = findViewById(R.id.level);
        priority = findViewById(R.id.priority);
        memoryWord = findViewById(R.id.memoryWord);
        progressBar1 = findViewById(R.id.progressBar1);
        progressBar2 = findViewById(R.id.progressBar2);
        learnPercent = findViewById(R.id.learnPercent);
        tvTopicName = findViewById(R.id.topicName);
        progressBar = findViewById(R.id.progressBar);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        checkBoxShuffle = findViewById(R.id.checkBox1);
        checkBoxOnlyPriorityWords = findViewById(R.id.checkBox2);

        categoryAdapterInFlashcard = new CategoryAdapterInFlashcard(this, R.layout.item_selected, getListCategory());
        spinner.setAdapter(categoryAdapterInFlashcard);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyStudyList = new ArrayList<>();

        Intent intent = getIntent();
        String wordSelection = intent.getStringExtra("wordSelection");
        if ("only".equals(wordSelection)) {
            checkBoxOnlyPriorityWords.setChecked(true);
        }

        loadInfoTopicForFindAndFill();

        adapter = new MultiViewInFindAndFillAdapter(historyStudyList);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            finish(); // Trở về màn hình trước đó
        });

        OpenActivityCommand openActivityCommand = new OpenActivityCommand(StudyTopicInFindAndFillActivity.this, StudyByFindAndFillActivity.class, this);

        btnStart.setCommand(openActivityCommand);

        btnShare.setOnClickListener(v -> showShareDialog());

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(StudyTopicInFindAndFillActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(StudyTopicInFindAndFillActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(StudyTopicInFindAndFillActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(StudyTopicInFindAndFillActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(StudyTopicInFindAndFillActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
    }
    private void showShareDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Public Topic");
        builder.setMessage("Do you want to make this topic public?");

        // Nút Yes
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Toast.makeText(this, "Topic has been made public!", Toast.LENGTH_SHORT).show();
        });

        // Nút No
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss(); // Đóng dialog
        });

        // Hiển thị Dialog
        builder.create().show();
    }
    private List<Category> getListCategory(){
        List<Category> list = new ArrayList<>();
        list.add(new Category("English"));
        list.add(new Category("Vietnamese"));
        return list;
    }

    private void loadInfoTopicForFindAndFill() {
        tvTopicName.setText(topicName);
        // Truy vấn thông tin topic
        db.collection("topics")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().collection("vocabularies")
                                    .get()
                                    .addOnSuccessListener(vocabSnapshots -> {
                                        int wordCount = vocabSnapshots.size();
                                        adapter.setTotalQuestion(wordCount);
                                        if (wordCount <= 10) {
                                            level.setText("normal");
                                        } else if (wordCount <= 20) {
                                            level.setText("medium");
                                        } else {
                                            level.setText("high");
                                        }

                                        document.getReference().collection("vocabularies")
                                                .whereEqualTo("priority", true)
                                                .get()
                                                .addOnSuccessListener(prioritySnapshots -> {
                                                    int priorityWords = prioritySnapshots.size();
                                                    priority.setText(priorityWords + "/" + wordCount);
                                                    int res = (int) Math.round(((double) priorityWords / wordCount) * 100);
                                                    progressBar1.setProgress(res);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to count priority words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });

                                        document.getReference().collection("vocabularies")
                                                .whereEqualTo("status", "Đã thuộc")
                                                .get()
                                                .addOnSuccessListener(statusSnapshots -> {
                                                    int masteredWords = statusSnapshots.size();
                                                    memoryWord.setText(masteredWords + "/" + wordCount);
                                                    int result = (int) Math.round(((double) masteredWords / wordCount) * 100);
                                                    learnPercent.setText(result + "%");
                                                    progressBar.setText(result + "%");
                                                    circularProgressBar.setProgress(result);
                                                    progressBar2.setProgress(result);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to count mastered words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to count vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "No topics found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        db.collection("users").document(userId).collection("history")
                .whereEqualTo("typeStudy", "Find and Fill") // Truy vấn đơn giản
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyStudyList.clear(); // Xóa danh sách cũ

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HistoryStudy history = document.toObject(HistoryStudy.class);

                        // Lọc topicName trên client
                        if (history.getTopicName() != null && history.getTopicName().equals(topicName)) {
                            historyStudyList.add(history);
                        }
                    }

                    if (historyStudyList.isEmpty()) {
                        Toast.makeText(this, "No history found for the selected topic.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Cập nhật adapter của RecyclerView
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

}