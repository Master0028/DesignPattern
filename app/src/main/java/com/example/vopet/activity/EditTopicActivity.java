package com.example.vopet.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.example.vopet.adapter.EditWordAdapter;
import com.example.vopet.model.Vocabulary;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.WriteBatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditTopicActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditWordAdapter adapter;
    private List<Vocabulary> wordList;
    private String topicName, userId;
    private Button btnBack, btnSave, btnAdd;
    private TextView tvTopicName;
    FirebaseFirestore db;
    private static final int REQUEST_CODE_PICK_IMAGE = 102;
    private int selectedWordPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_topic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        userId = SessionSingleton.getInstance().getUserId();

        topicName = getIntent().getStringExtra("topicName");
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        tvTopicName = findViewById(R.id.topicName);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        ImageButton btnAdd1 = findViewById(R.id.btnAdd1);

        // Tạo danh sách các từ
        wordList = new ArrayList<>();

        // Thiết lập adapter cho RecyclerView
//        adapter = new EditWordAdapter(this, wordList, topicName);
        adapter = new EditWordAdapter(this, wordList, topicName, position -> {
            // Khi người dùng nhấn vào hình ảnh, mở trình chọn ảnh và lưu lại vị trí của từ vựng
            selectedWordPosition = position;
            openImagePicker();
        });
        recyclerView.setAdapter(adapter);

        loadTopic(topicName);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSave.setOnClickListener(v -> saveAllWordsToFirestore());
        btnAdd.setOnClickListener(v -> addNewCard());

        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(EditTopicActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnAdd1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd1) {
                    Intent i = new Intent(EditTopicActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(EditTopicActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(EditTopicActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(EditTopicActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                Bitmap resizedBitmap = resizeBitmap(bitmap, 800, 800); // Đặt kích thước tối đa phù hợp (800x800)
                String encodedImage = compressAndEncodeBitmap(resizedBitmap);

                if (selectedWordPosition != -1) {
                    adapter.updateWordPhoto(selectedWordPosition, encodedImage);
                }
            } catch (IOException e) {
                Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addNewCard() {
        // Tạo một từ mới với các giá trị trống
        int newIndex = wordList.size() + 1; // STT mới sẽ là kích thước hiện tại của danh sách + 1
        Vocabulary newWord = new Vocabulary(newIndex, "", "", "", "Not learned", false, "");

        // Thêm từ mới vào danh sách
        wordList.add(newWord);

        // Cập nhật adapter để hiển thị card mới
        adapter.notifyItemInserted(wordList.size() - 1);

        // Cuộn đến vị trí của card vừa thêm để người dùng dễ dàng nhìn thấy
        recyclerView.smoothScrollToPosition(wordList.size() - 1);
    }

    private void loadTopic(String folderName) {
        tvTopicName.setText(folderName);
        db.collection("topics") // Tên collection trong Firestore
                .whereEqualTo("creatorId", userId) // Lọc theo userId
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
                                            String id = vocabDoc.getId();
                                            String photo = vocabDoc.getString("photo");

                                            Vocabulary word = new Vocabulary(stt, english, meaning, pronounce, status, priority, photo);
                                            word.setId(id);
                                            filteredList.add(word);
                                        }
                                        filteredList.sort((w1, w2) -> Integer.compare(w1.getStt(), w2.getStt()));

                                        // Cập nhật RecyclerView
                                        adapter.updateData(filteredList);
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

    private void saveAllWordsToFirestore() {
        db.collection("topics") // Tìm topic dựa trên userId và topicName
                .whereEqualTo("creatorId", userId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Thực hiện lưu dữ liệu vào subcollection "vocabularies"
                            saveWords(document.getReference().collection("vocabularies"));
                        }
                    } else {
                        Toast.makeText(this, "No topic found to save data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveWords(CollectionReference vocabulariesRef) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Tạo một batch mới
        WriteBatch batch = db.batch();
        List<Vocabulary> newWords = new ArrayList<>();
        // Duyệt qua danh sách từ và thêm vào batch
        for (Vocabulary word : wordList) {
            String documentId = word.getId(); // Đảm bảo documentId không null
            if (documentId == null || documentId.isEmpty()) {
//                Toast.makeText(this, "Invalid document ID for word: " + word.getEnglish(), Toast.LENGTH_SHORT).show();
//                continue;
                newWords.add(word);
            }else {
                batch.update(vocabulariesRef.document(documentId), word.toMap());
            }
        }

        // Commit batch và xử lý thành công hoặc thất bại
        batch.commit()
                .addOnSuccessListener(aVoid -> {
//                    Intent intent = new Intent();
//                    intent.putExtra("topicName", topicName);
//                    setResult(RESULT_OK, intent);
//                    finish();
                    addNewWordsToFirestore(vocabulariesRef, newWords);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreBatchError", "Error updating words", e);
                });
    }
    private void addNewWordsToFirestore(CollectionReference vocabulariesRef, List<Vocabulary> newWords) {
        if (newWords.isEmpty()) {
            // Không có từ mới để thêm, kết thúc Activity
            Intent intent = new Intent();
            intent.putExtra("topicName", topicName);
            setResult(RESULT_OK, intent);
            finish();
            return;
        }

        // Thêm từ mới vào Firestore
        for (Vocabulary word : newWords) {
            vocabulariesRef.add(word.toMap())
                    .addOnSuccessListener(documentReference -> {
                        // Cập nhật ID cho từ mới vừa được thêm
                        word.setId(documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add new word: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        // Cập nhật `numberOfWord` sau khi thêm từ mới
        db.collection("topics")
                .whereEqualTo("creatorId", userId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        int addedWords = newWords.size();

                        db.collection("topics").document(topicId)
                                .update("numberOfWord", FieldValue.increment(addedWords))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Updated numberOfWord successfully!", Toast.LENGTH_SHORT).show();
                                    // Kết thúc Activity sau khi cập nhật thành công
                                    Intent intent = new Intent();
                                    intent.putExtra("topicName", topicName);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update numberOfWord: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Topic not found for updating numberOfWord!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch topic for numberOfWord update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxWidth;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxHeight;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private String compressAndEncodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Nén với chất lượng 50% để giảm kích thước
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}