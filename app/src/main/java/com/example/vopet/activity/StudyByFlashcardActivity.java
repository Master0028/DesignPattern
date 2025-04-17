package com.example.vopet.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vopet.R;
import com.example.vopet.model.HistoryStudy;
import com.example.vopet.model.Vocabulary;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StudyByFlashcardActivity extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private TextView tvQuestion, tvTopicName, tvStt;
    private Button btnPress, btnBack, btnNext, btnPause;
    private ImageButton ibSound, btnHome, btnTopics, btnProfile, btnCommunity, btnAdd, ibStar;
    private Switch swtFollow;
    private LinearLayout layout;
    private FirebaseFirestore db;
    private List<Vocabulary> flashcardList;
    private int currentIndex = 0; // Theo dõi thẻ hiện tại
    private boolean isFrontVisible = true;
    private boolean isAutoSurf, isShuffle, isOnlyPriorityWords;
    private boolean isPaused = false; // Trạng thái tạm dừng
    private String topicName, frontLanguage, backLanguage, userId;
    private boolean isTrackingProgress = false;
    private int correctCount = 0;
    private ImageView imgWord;
    private Handler autoSurfHandler = new Handler();
    private Runnable autoSurfRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_studybyflashcard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        userId = SessionSingleton.getInstance().getUserId();
        flashcardList = new ArrayList<>();

        // Ánh xạ giao diện
        imgWord = findViewById(R.id.imgWord);
        tvQuestion = findViewById(R.id.tvQuestion);
        btnPress = findViewById(R.id.btnPress);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnPause = findViewById(R.id.btnPause);
        btnHome = findViewById(R.id.btnHome);
        btnAdd = findViewById(R.id.btnAdd);
        btnTopics = findViewById(R.id.btnTopics);
        btnProfile = findViewById(R.id.btnProfile);
        btnCommunity = findViewById(R.id.btnCommunity);
        swtFollow = findViewById(R.id.swtFollow);
        ibSound = findViewById(R.id.ibSound);
        ibStar = findViewById(R.id.ibStar);
        tvTopicName = findViewById(R.id.tvTopicName);
        tvStt = findViewById(R.id.tvStt);
        layout = findViewById(R.id.layout);

        Bundle bundle = getIntent().getExtras();

        // Lấy topicName và trạng thái shuffle, auto-surf từ Intent
        topicName = bundle.getString("topicName");
        isAutoSurf = bundle.getBoolean("isAutoSurf", false);
        isShuffle = bundle.getBoolean("isShuffle", false);
        isOnlyPriorityWords = bundle.getBoolean("isOnlyPriorityWords", false);
        String selection = bundle.getString("selection");

        // Kiểm tra lựa chọn và hiển thị mặt trước, mặt sau phù hợp
        frontLanguage = "Vietnamese".equals(selection) ? "Vietnamese" : "English";
        backLanguage = "Vietnamese".equals(selection) ? "English" : "Vietnamese";

        // Khởi tạo Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if (frontLanguage.equals("English")) {
                    textToSpeech.setLanguage(Locale.US);
                } else {
                    textToSpeech.setLanguage(new Locale("vi", "VN"));
                }
            } else {
                Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });


        tvTopicName.setText(topicName);

        loadFlashcards(frontLanguage, backLanguage);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(StudyByFlashcardActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(StudyByFlashcardActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });

        btnTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(StudyByFlashcardActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(StudyByFlashcardActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(StudyByFlashcardActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });

        btnPause.setOnClickListener(v -> togglePause());

        // Hiệu ứng lật thẻ
        btnPress.setOnClickListener(v -> flipCard());

        // Nút Next
        btnNext.setOnClickListener(v -> {
            if (isTrackingProgress) {
                markWordAsLearned(true); // Trả lời đúng
                correctCount++;
            } else {
                showNextCard(); // Hiển thị từ vựng tiếp theo
            }
        });

        // Nút Back
        btnBack.setOnClickListener(v -> {
            if (isTrackingProgress) {
                markWordAsLearned(false); // Trả lời sai
            } else {
                showPreviousCard(); // Hiển thị từ vựng trước đó
            }
        });

        // Nút âm thanh
        ibSound.setOnClickListener(v -> {
            if (flashcardList != null && !flashcardList.isEmpty()) {
                Vocabulary currentCard = flashcardList.get(currentIndex);
                if (frontLanguage.equals("English")) {
                    speak(currentCard.getEnglish());
                } else {
                    speak(currentCard.getMeaning());
                }
            }
        });

        // Cài đặt auto-surf
        setupAutoSurf();

        swtFollow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTrackingProgress = isChecked;
            updateButtonLabels();
        });

        ibStar.setOnClickListener(v -> {
            if (!flashcardList.isEmpty()) {
                Vocabulary currentCard = flashcardList.get(currentIndex);

                // Đảo trạng thái priority
                boolean newPriority = !currentCard.getPriority();
                currentCard.setPriority(newPriority);

                // Cập nhật icon
                if (newPriority) {
                    ibStar.setImageResource(R.drawable.baseline_star_24);
                } else {
                    ibStar.setImageResource(R.drawable.baseline_star_border_24);
                }

                // Lưu cập nhật vào Firestore
                db.collection("topics")
                        .whereEqualTo("ownerId", userId)
                        .whereEqualTo("name", topicName)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String topicId = querySnapshot.getDocuments().get(0).getId();

                                db.collection("topics").document(topicId).collection("vocabularies")
                                        .whereEqualTo("english", currentCard.getEnglish())
                                        .get()
                                        .addOnSuccessListener(vocabSnapshot -> {
                                            if (!vocabSnapshot.isEmpty()) {
                                                String vocabId = vocabSnapshot.getDocuments().get(0).getId();

                                                db.collection("topics").document(topicId).collection("vocabularies").document(vocabId)
                                                        .update("priority", newPriority)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(this, "Priority updated successfully!", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(this, "Failed to update priority: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to fetch vocabulary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to fetch topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }

    private void loadFlashcards(String frontLanguage, String backLanguage) {
        if (isAutoSurf) {
            btnPause.setVisibility(View.VISIBLE);
        } else {
            btnPause.setVisibility(View.INVISIBLE);
        }
        db.collection("topics")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String topicId = querySnapshot.getDocuments().get(0).getId(); // Lấy ID của topic

                        db.collection("topics").document(topicId).collection("vocabularies")
                                .get()
                                .addOnSuccessListener(vocabSnapshot -> {
                                    flashcardList.clear();
                                    for (QueryDocumentSnapshot doc : vocabSnapshot) {
                                        Vocabulary vocab = doc.toObject(Vocabulary.class);

                                        // Chỉ thêm từ vựng nếu isOnlyPriorityWords là false hoặc vocab.priority là true
                                        if (!isOnlyPriorityWords || vocab.getPriority()) {
                                            flashcardList.add(vocab);
                                        }
                                    }

                                    // Nếu isShuffle là true, trộn danh sách
                                    if (isShuffle) {
                                        Collections.shuffle(flashcardList);
                                    }

                                    // Hiển thị flashcard đầu tiên
                                    if (!flashcardList.isEmpty()) {
                                        showFlashcard(0, frontLanguage, backLanguage);
                                    } else {
                                        tvQuestion.setText("No vocabularies available!");
                                        tvStt.setText("");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    tvQuestion.setText("Failed to load vocabularies: " + e.getMessage());
                                });
                    } else {
                        tvQuestion.setText("No topic found!");
                        tvStt.setText("");
                    }
                })
                .addOnFailureListener(e -> tvQuestion.setText("Failed to load topic: " + e.getMessage()));
    }

    private void showFlashcard(int index, String frontLanguage, String backLanguage) {
        if (!flashcardList.isEmpty()) {
            Vocabulary currentCard = flashcardList.get(index);

            // Hiển thị mặt trước theo ngôn ngữ được chọn
            if ("Vietnamese".equals(frontLanguage)) {
                tvQuestion.setText(currentCard.getMeaning());
            } else {
                tvQuestion.setText(currentCard.getEnglish());
            }

            tvStt.setText((index + 1) + "/" + flashcardList.size());
            isFrontVisible = true; // Đảm bảo mặt trước hiển thị
            autoSpeak(currentCard.getEnglish());

            // Cập nhật icon cho ibStar
            if (currentCard.getPriority()) {
                ibStar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_star_24));
            } else {
                ibStar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_star_border_24));
            }
            if (currentCard.getPhoto() != null && !currentCard.getPhoto().isEmpty()) {
                try {
                    // Loại bỏ phần "data:image/jpeg;base64," trước khi decode
                    String base64Image = currentCard.getPhoto().split(",")[1];
                    byte[] imageBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imgWord.setImageBitmap(bitmap);
                } catch (Exception e) {
                    // Nếu quá trình decode gặp lỗi, hiển thị ảnh mặc định
                    imgWord.setImageResource(R.drawable.bg_folder_detail);
                    Toast.makeText(this, "Error decoding image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                imgWord.setImageResource(R.drawable.bg_folder_detail); // Hình ảnh mặc định nếu không có ảnh
            }
        }
    }

    public void flipCard() {
        if (!flashcardList.isEmpty()) {
            Vocabulary currentCard = flashcardList.get(currentIndex);
            ObjectAnimator flip = ObjectAnimator.ofFloat(layout, "rotationY", 0f, 180f);
            flip.setDuration(500);

            flip.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (frontLanguage.equals("English")) {
                        if (isFrontVisible) {
                            tvQuestion.setText(currentCard.getMeaning()); // Hiển thị mặt sau
                        } else {
                            tvQuestion.setText(currentCard.getEnglish()); // Hiển thị mặt trước
                        }
                    } else {
                        if (isFrontVisible) {
                            tvQuestion.setText(currentCard.getEnglish()); // Hiển thị mặt sau
                        } else {
                            tvQuestion.setText(currentCard.getMeaning()); // Hiển thị mặt trước
                        }
                    }
                    isFrontVisible = !isFrontVisible;
                    ObjectAnimator reverseFlip = ObjectAnimator.ofFloat(layout, "rotationY", 180f, 360f);
                    reverseFlip.setDuration(500);
                    reverseFlip.start();
                }
            });
            flip.start();
        }
    }

    private void showNextCard() {
        if (currentIndex < flashcardList.size() - 1) {
            currentIndex++;
            showFlashcard(currentIndex, frontLanguage, backLanguage);
        } else {
            Toast.makeText(this, "You've reached the end!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(StudyByFlashcardActivity.this, FlashcardWithClickCardActivity.class);
            intent.putExtra("topicName", topicName);
            startActivity(intent);
            finish();
        }
    }

    private void showPreviousCard() {
        if (currentIndex > 0) {
            currentIndex--;
            showFlashcard(currentIndex, frontLanguage, backLanguage);
        } else {
            Toast.makeText(this, "You're at the start!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAutoSurf() {
        if (isAutoSurf) {
            autoSurfRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isPaused) {
                        flipCard(); // Lật thẻ hiện tại

                        autoSurfHandler.postDelayed(() -> { // Đợi 3 giây sau khi lật thẻ mới chuyển thẻ
                            if (!isPaused) {
                                if (currentIndex < flashcardList.size() - 1) {
                                    currentIndex++;
                                } else {
                                    currentIndex = 0; // Quay lại thẻ đầu tiên
                                }
                                showFlashcard(currentIndex, frontLanguage, backLanguage); // Hiển thị thẻ tiếp theo
                            }
                            autoSurfHandler.postDelayed(this, 5000); // Thiết lập lại 5 giây
                        }, 3000); // Thời gian đợi sau khi lật thẻ
                    }
                }
            };
            autoSurfHandler.postDelayed(autoSurfRunnable, 5000); // Bắt đầu sau 5 giây
        }
    }


    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (autoSurfHandler != null && autoSurfRunnable != null) {
            autoSurfHandler.removeCallbacks(autoSurfRunnable);
        }
        super.onDestroy();
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void togglePause() {
        isPaused = !isPaused; // Đảo ngược trạng thái tạm dừng

        if (isPaused) {
            // Tạm dừng auto-surf
            autoSurfHandler.removeCallbacks(autoSurfRunnable);
            btnPause.setBackgroundResource(R.drawable.baseline_play_arrow_24);
        } else {
            // Tiếp tục auto-surf
            setupAutoSurf();
            btnPause.setBackgroundResource(R.drawable.baseline_pause_24);
        }
    }

    private void autoSpeak(String text) {
        if (textToSpeech != null && text != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void updateButtonLabels() {
        if (isTrackingProgress) {
            btnNext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_24, 0, 0, 0); // Drawable trái
            btnBack.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_clear_24, 0, 0, 0); // Drawable trái
        } else {
            btnBack.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_arrow_back_24_w, 0, 0, 0); // Drawable trái
            btnNext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_arrow_forward_24, 0, 0, 0); // Drawable trái
        }
    }

    private void markWordAsLearned(boolean isCorrect) {
        if (!flashcardList.isEmpty()) {
            Vocabulary currentCard = flashcardList.get(currentIndex);

            // Tăng số lần học nếu trả lời đúng
            if (isCorrect) {
                currentCard.setNumberOfStudy(currentCard.getNumberOfStudy() + 1);
            }

            // Cập nhật trạng thái "Đã học"
            String newStatus = currentCard.getNumberOfStudy() >= 5 ? "Đã thuộc" : "Đã học";
            currentCard.setStatus(newStatus);

            // Lưu cập nhật vào Firestore
            db.collection("topics")
                    .whereEqualTo("ownerId", userId)
                    .whereEqualTo("name", topicName)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String topicId = querySnapshot.getDocuments().get(0).getId();

                            db.collection("topics").document(topicId).collection("vocabularies")
                                    .whereEqualTo("english", currentCard.getEnglish())
                                    .get()
                                    .addOnSuccessListener(vocabSnapshot -> {
                                        if (!vocabSnapshot.isEmpty()) {
                                            String vocabId = vocabSnapshot.getDocuments().get(0).getId();

                                            db.collection("topics").document(topicId).collection("vocabularies").document(vocabId)
                                                    .update("numberOfStudy", currentCard.getNumberOfStudy(), "status", newStatus)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to fetch vocabulary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            // Chuyển sang từ tiếp theo hoặc kết thúc
            if (currentIndex < flashcardList.size() - 1) {
                currentIndex++;
                showFlashcard(currentIndex, frontLanguage, backLanguage);
            } else {
                // Hoàn thành tất cả flashcard
                if (isTrackingProgress) {
                    saveHistoryToFirebase(); // Lưu lịch sử học
                } else {
                    // Quay lại trang chủ
                    Toast.makeText(this, "You've completed all flashcards!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(StudyByFlashcardActivity.this, FlashcardWithClickCardActivity.class);
                    intent.putExtra("topicName", topicName);
                    startActivity(intent);
                    finish();
                }
            }
        }
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
                                    int totalWords = flashcardList.size();

                                    // Lấy tất cả lịch sử học (không dùng `orderBy`)
                                    db.collection("users").document(userId).collection("history")
                                            .whereEqualTo("typeStudy", "Flashcard")
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
                                                addHistoryToFirestore(newTimes, memoriedWords, totalWords, creatorId);
                                            })
                                            .addOnFailureListener(e -> {
                                                // Nếu truy vấn thất bại, mặc định với times = 1
                                                Log.e("HistoryError", "Failed to query history: " + e.getMessage());
                                                addHistoryToFirestore(1, memoriedWords, totalWords, creatorId);
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
                "Flashcard",
                correctCount,
                memoriedWords,
                (int) Math.round(((double) correctCount / totalWords) * 100),
                (int) Math.round(((double) memoriedWords / totalWords) * 100),
                0
        );

        // Lưu lịch sử học vào Firestore
        db.collection("users").document(userId).collection("history")
                .add(history)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "History saved successfully!", Toast.LENGTH_SHORT).show();
                    updateTopicAccess(creatorId, topicName);
                    updateTopicProgress(userId, topicName, (int) Math.round(((double) correctCount / totalWords) * 100));
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
        Log.d("Restart", "res called");
        Intent intent = new Intent(StudyByFlashcardActivity.this, FlashcardWithClickCardActivity.class);
        intent.putExtra("topicName", topicName);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish(); // Đóng màn hiện tại
    }
}
