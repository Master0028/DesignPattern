package com.example.vopet.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import com.example.vopet.adapter.MultiViewInMulChoiceAdapter;
import com.example.vopet.model.Category;
import com.example.vopet.pattern.SessionSingleton;
import com.example.vopet.pattern.command.CommandButton;
import com.example.vopet.pattern.command.IBundleProvider;
import com.example.vopet.pattern.command.OpenMulChoiceActivityCommand;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StudyTopicInMulChoiceActivity extends AppCompatActivity implements IBundleProvider {
    Spinner spinner;
    CategoryAdapterInFlashcard categoryAdapterInFlashcard;
    private RecyclerView recyclerView;
    private MultiViewInMulChoiceAdapter adapter;
    private List<HistoryStudy> historyStudyList;
    private Button btnBack, btnShare;
    private CheckBox checkBoxShuffle, checkBoxOnlyPriorityWords, checkBoxAutoSpeaking;
    private TextView level, tvTopicName, priority, memoryWord, learnPercent, progressBar;
    private String topicName, userId;
    private FirebaseFirestore db;
    ProgressBar progressBar1, progressBar2, circularProgressBar;
    private boolean isShuffleChecked = false;
    private boolean isOnlyPriorityWordsChecked = false;
    private TextToSpeech textToSpeech;
    private boolean isAutoSpeakingChecked = false;

    CommandButton btnStart;

    @Override
    public Bundle getBundle() {
        isOnlyPriorityWordsChecked = checkBoxOnlyPriorityWords.isChecked();
        isShuffleChecked = checkBoxShuffle.isChecked();
        isAutoSpeakingChecked = checkBoxAutoSpeaking.isChecked();

        String selectedLanguage = spinner.getSelectedItem().toString();

        Bundle bundle = new Bundle();

        bundle.putString("topicName", topicName);
        bundle.putString("selection", selectedLanguage);
        bundle.putBoolean("isOnlyPriorityWords", isOnlyPriorityWordsChecked);
        bundle.putBoolean("isShuffle", isShuffleChecked);
        bundle.putBoolean("isAutoSpeakingChecked", isAutoSpeakingChecked);

        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_topic_in_mul_choice);
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
//        String creatorId = getIntent().getStringExtra("creatorId");
        userId = SessionSingleton.getInstance().getUserId();
        db = FirebaseFirestore.getInstance();

        spinner = findViewById(R.id.spinner);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnStart = findViewById(R.id.btnStart);
        level = findViewById(R.id.level);
        progressBar1 = findViewById(R.id.progressBar1);
        progressBar2 = findViewById(R.id.progressBar2);
        progressBar = findViewById(R.id.progressBar);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        priority = findViewById(R.id.priority);
        memoryWord = findViewById(R.id.memoryWord);
        learnPercent = findViewById(R.id.learnPercent);
        tvTopicName = findViewById(R.id.topicName);
        checkBoxShuffle = findViewById(R.id.checkBox1);
        checkBoxOnlyPriorityWords = findViewById(R.id.checkBox2);
        checkBoxAutoSpeaking = findViewById(R.id.checkBox3);
        categoryAdapterInFlashcard = new CategoryAdapterInFlashcard(this, R.layout.item_selected, getListCategory());
        spinner.setAdapter(categoryAdapterInFlashcard);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            } else {
                Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
        updateMemorizedWordsCount(0);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyStudyList = new ArrayList<>();

        adapter = new MultiViewInMulChoiceAdapter(historyStudyList);
        recyclerView.setAdapter(adapter);
        Intent intent = getIntent();
        String wordSelection = intent.getStringExtra("wordSelection");
        if ("only".equals(wordSelection)) {
            checkBoxOnlyPriorityWords.setChecked(true);
        }
        btnBack.setOnClickListener(v -> {
            finish(); // Trở về màn hình trước đó
        });

        loadInfoTopicForMultipleChoice();

        btnShare.setOnClickListener(v -> showShareDialog());

        OpenMulChoiceActivityCommand openMulChoiceActivityCommand = new OpenMulChoiceActivityCommand(StudyTopicInMulChoiceActivity.this, StudyByMulChoiceActivity.class, this);

        btnStart.setCommand(openMulChoiceActivityCommand);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(StudyTopicInMulChoiceActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(StudyTopicInMulChoiceActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(StudyTopicInMulChoiceActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(StudyTopicInMulChoiceActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(StudyTopicInMulChoiceActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void updateMemorizedWordsCount(int correctAnswerCount) {
        db.collection("topics")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        document.getReference().update("memorizedWordsCount", correctAnswerCount)
                                .addOnSuccessListener(aVoid -> {
                                    // Update successful
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update memorized words count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No topics found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

//    private void btnStartOnClick() {
//        isOnlyPriorityWordsChecked = checkBoxOnlyPriorityWords.isChecked();
//        isShuffleChecked = checkBoxShuffle.isChecked();
//        isAutoSpeakingChecked = checkBoxAutoSpeaking.isChecked();  // Lưu trạng thái checkbox AutoSpeaking
//
////        String creatorId = getIntent().getStringExtra("creatorId");
//        db.collection("topics")
//                .whereEqualTo("ownerId", userId)
//                .whereEqualTo("name", topicName)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                            document.getReference().collection("vocabularies")
//                                    .get()
//                                    .addOnSuccessListener(vocabSnapshots -> {
//                                        topicName = getIntent().getStringExtra("topicName");
//                                        ArrayList<String> wordsList = new ArrayList<>();
//                                        ArrayList<String> meaningsList = new ArrayList<>();
//                                        ArrayList<String> pronunciationList = new ArrayList<>();
//
//                                        for (QueryDocumentSnapshot vocab : vocabSnapshots) {
//                                            // Kiểm tra nếu "Only Priority Words" được chọn thì chỉ lấy từ có priority = true
//                                            if (isOnlyPriorityWordsChecked && !vocab.getBoolean("priority")) {
//                                                continue; // Bỏ qua từ không có priority = true
//                                            }
//
//                                            if ("English".equals(selectedLanguage)) {
//                                                // Display words in English, meanings in Vietnamese
//                                                wordsList.add(vocab.getString("english"));
//                                                meaningsList.add(vocab.getString("meaning"));
//                                            } else {
//                                                // Display meanings in Vietnamese, words in English
//                                                wordsList.add(vocab.getString("meaning"));
//                                                meaningsList.add(vocab.getString("english"));
//                                            }
//                                            pronunciationList.add(vocab.getString("pronounce"));
//                                        }
//
//                                        // Nếu chọn Shuffle thì trộn ngẫu nhiên danh sách từ vựng
//                                        if (isShuffleChecked) {
//                                            long seed = System.nanoTime();
//                                            Collections.shuffle(wordsList, new java.util.Random(seed));
//                                            Collections.shuffle(meaningsList, new java.util.Random(seed));
//                                            Collections.shuffle(pronunciationList, new java.util.Random(seed));
//                                        }
//
//                                        // Truyền danh sách từ vựng qua intent
//                                        Intent intent1 = new Intent(StudyTopicInMulChoiceActivity.this, StudyByMulChoiceActivity.class);
//                                        intent1.putStringArrayListExtra("wordsList", wordsList);
//                                        intent1.putStringArrayListExtra("meaningsList", meaningsList);
//                                        intent1.putStringArrayListExtra("pronunciationList", pronunciationList);
//                                        startActivity(intent1);
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Toast.makeText(this, "Failed to load vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    });
//                        }
//                    } else {
//                        Toast.makeText(this, "No topics found", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Failed to load topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

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

    private void loadInfoTopicForMultipleChoice() {
        tvTopicName.setText(topicName);
        db.collection("topics") // Tên collection trong Firestore
                .whereEqualTo("ownerId", userId) // Lọc theo userId
                .whereEqualTo("name", topicName) // Lọc theo folderName
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Truy cập subcollection "vocabularies"
                            document.getReference().collection("vocabularies")
                                    .get()
                                    .addOnSuccessListener(vocabSnapshots -> {
                                        int wordCount = vocabSnapshots.size(); // Đếm số lượng tài liệu
                                        adapter.setTotalQuestion(wordCount);
                                        // Hiển thị số lượng từ
                                        if(wordCount <= 10) {
                                            level.setText("normal");
                                        } else if(wordCount <= 20) {
                                            level.setText("medium");
                                        } else {
                                            level.setText("high");
                                        }

                                        document.getReference().collection("vocabularies")
                                                .whereEqualTo("priority", true)
                                                .get()
                                                .addOnSuccessListener(prioritySnapshots -> {
                                                    int priorityWords = prioritySnapshots.size(); // Số từ có priority = true
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
                                                    int result = ((int) Math.round((masteredWords/wordCount)*100));
                                                    memoryWord.setText(masteredWords + "/" + wordCount);
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
                        Toast.makeText(this, "No topics found" , Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        db.collection("users").document(userId).collection("history")
                .whereEqualTo("typeStudy", "Multiplechoice") // Truy vấn đơn giản
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