package com.example.vopet.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TopicDetailActivity extends AppCompatActivity {
    Button btnStudy, btnEdit, btnBack, btnFilter;
    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private List<Vocabulary> wordList;
    String userId, topicName;
    FirebaseFirestore db;
    private TextView tvTopicName, priority, memoryWord;
    private TextToSpeech textToSpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_topic_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, 100);

        userId = SessionSingleton.getInstance().getUserId();
        topicName = getIntent().getStringExtra("topicName");
        boolean edit = getIntent().getBooleanExtra("edit", false);

        db = FirebaseFirestore.getInstance();
        btnStudy = findViewById(R.id.btnStudy);
        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilter);
        tvTopicName = findViewById(R.id.topicName);
        memoryWord = findViewById(R.id.memoryWord);
        priority = findViewById(R.id.priority);
        btnStudy.setOnClickListener(v -> showStudyDialog());

        if (!edit) {
            // Kích hoạt chế độ chỉnh sửa
            btnFilter.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
        }

        recyclerView = findViewById(R.id.recyclerView); // Tham chiếu đến RecyclerView từ layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        ImageButton btnBXH = findViewById(R.id.btnBXH);
        btnBXH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnBXH) {
                    Intent i = new Intent(TopicDetailActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(TopicDetailActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(TopicDetailActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(TopicDetailActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(TopicDetailActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
        // Tạo dữ liệu
        wordList = new ArrayList<>();

        // Khởi tạo adapter
        wordAdapter = new WordAdapter(this, wordList, textToSpeech);
        recyclerView.setAdapter(wordAdapter);

        loadTopic(topicName);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(TopicDetailActivity.this, EditTopicActivity.class);
            intent.putExtra("topicName", topicName);
            startActivityForResult(intent, 1);
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnFilter.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, btnFilter);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_filter_status, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.filter_all) {
                    filterWordsByStatus(null); // Hiển thị tất cả từ
                } else if (item.getItemId() == R.id.filter_not_learned) {
                    filterWordsByStatus("Chưa học");
                } else if (item.getItemId() == R.id.filter_learned) {
                    filterWordsByStatus("Đã học");
                } else if (item.getItemId() == R.id.filter_mastered) {
                    filterWordsByStatus("Đã thuộc");
                }
                return true;
            });
            popupMenu.show();
        });

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


            // Xác định message dựa trên RadioButton được chọn
            Intent intent;
            String selectStudyMode = spinnerStudyMode.getSelectedItem().toString();
            switch (selectStudyMode) {
                case "Flashcard":
                    intent = new Intent(TopicDetailActivity.this, FlashcardWithClickCardActivity.class);
                    break;
                case "Multiple Choice":
                    intent = new Intent(TopicDetailActivity.this, StudyTopicInMulChoiceActivity.class);
                    break;
                case "Type word":
                    intent = new Intent(TopicDetailActivity.this, StudyTopicInFindAndFillActivity.class);
                    break;
                default:
                    Toast.makeText(TopicDetailActivity.this, "Please select a valid option", Toast.LENGTH_SHORT).show();
                    return;
            }

            intent.putExtra("topicName", topicName);
            startActivity(intent);

            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void loadTopic(String folderName) {
        tvTopicName.setText(folderName);
        db.collection("topics") // Tên collection trong Firestore
                .whereEqualTo("ownerId", userId) // Lọc theo userId
                .whereEqualTo("name", folderName) // Lọc theo folderName
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
                                        filteredList.sort((w1, w2) -> Integer.compare(w1.getStt(), w2.getStt()));
                                        // Cập nhật RecyclerView
                                        wordAdapter.updateData(filteredList);

                                        int wordCount = vocabSnapshots.size();
                                        document.getReference().collection("vocabularies")
                                                .whereEqualTo("priority", true)
                                                .get()
                                                .addOnSuccessListener(prioritySnapshots -> {
                                                    int priorityWords = prioritySnapshots.size(); // Số từ có priority = true
                                                    priority.setText(priorityWords + "/" + wordCount);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // TTS đã được cài đặt, khởi tạo TextToSpeech
                textToSpeech = new TextToSpeech(this, status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        textToSpeech.setLanguage(Locale.UK);
                        // Cập nhật TextToSpeech cho adapter
                        wordAdapter.setTextToSpeech(textToSpeech);
                    }
                });
            } else {
                // Hướng dẫn cài đặt TTS
                Intent installIntent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }

        if(requestCode == 1) {
            loadTopic(topicName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (wordAdapter != null) {
            wordAdapter.release();
        }
    }

    private void filterWordsByStatus(@Nullable String status) {
        db.collection("topics")
                .whereEqualTo("creatorId", userId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Truy vấn vocabularies
                        if (status == null) {
                            // Lấy tất cả từ vựng nếu status là null
                            db.collection("topics").document(topicId).collection("vocabularies")
                                    .get()
                                    .addOnSuccessListener(vocabSnapshots -> {
                                        List<Vocabulary> allWords = new ArrayList<>();
                                        for (QueryDocumentSnapshot vocabDoc : vocabSnapshots) {
                                            String english = vocabDoc.getString("english");
                                            String meaning = vocabDoc.getString("meaning");
                                            String pronounce = vocabDoc.getString("pronounce");
                                            String wordStatus = vocabDoc.getString("status");
                                            boolean priority = vocabDoc.getBoolean("priority");
                                            String photoUrl = vocabDoc.getString("photo");

                                            Vocabulary word = new Vocabulary(0, english, meaning, pronounce, wordStatus, priority, photoUrl);
                                            word.setId(vocabDoc.getId());
                                            allWords.add(word);
                                        }
                                        updateWordListWithSequentialStt(allWords);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to fetch all words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Lọc theo status nếu không phải null
                            db.collection("topics").document(topicId).collection("vocabularies")
                                    .whereEqualTo("status", status)
                                    .get()
                                    .addOnSuccessListener(vocabSnapshots -> {
                                        List<Vocabulary> filteredList = new ArrayList<>();
                                        for (QueryDocumentSnapshot vocabDoc : vocabSnapshots) {
                                            String english = vocabDoc.getString("english");
                                            String meaning = vocabDoc.getString("meaning");
                                            String pronounce = vocabDoc.getString("pronounce");
                                            boolean priority = vocabDoc.getBoolean("priority");
                                            String photoUrl = vocabDoc.getString("photo");

                                            Vocabulary word = new Vocabulary(0, english, meaning, pronounce, status, priority, photoUrl);
                                            word.setId(vocabDoc.getId());
                                            filteredList.add(word);
                                        }
                                        updateWordListWithSequentialStt(filteredList);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to filter words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "No topics found for this user and topic!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Đặt lại STT cho danh sách từ vựng và cập nhật RecyclerView
    private void updateWordListWithSequentialStt(List<Vocabulary> wordList) {
        for (int i = 0; i < wordList.size(); i++) {
            wordList.get(i).setStt(i + 1); // Đặt STT tuần tự từ 1
        }
        wordAdapter.updateData(wordList); // Cập nhật RecyclerView với danh sách mới
    }
}