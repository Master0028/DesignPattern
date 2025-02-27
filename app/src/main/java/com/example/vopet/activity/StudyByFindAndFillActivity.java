package com.example.vopet.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vopet.R;
import com.example.vopet.model.Vocabulary;
import com.example.vopet.model.WordLearned;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StudyByFindAndFillActivity extends AppCompatActivity {
    private TextToSpeech textToSpeech;
//    private EditText meanEnglish;
    private Button btnSubmit;
    private TextView tvResult, enterAnswer, correctAnswerTv, incorrectAnswerTv, tvTopicName, tvStt, tvQuestion, tvPronounce;
    private ImageView imgWord;
    private List<Vocabulary> vocabularyList;
    private int currentIndex = 0; // Theo dõi câu hỏi hiện tại
    private FirebaseFirestore db;
    private ImageButton ibSound, btnHome, btnTopics, btnProfile, btnCommunity, btnAdd, ibStar;
    private String topicName, selection, userId;
    MediaPlayer correctSound, incorrectSound;
    private boolean isShuffle, isOnlyPriorityWords;
    private int countCorrect = 0;
    private List<WordLearned> wordLearnedList = new ArrayList<>();
    private long startTime, questionStartTime; // Thời gian bắt đầu và thời gian mỗi câu
    private int totalScore = 0; // Tổng điểm
    private int totalTimeSpent = 0; // Tổng thời gian tính bằng giây
    private AutoCompleteTextView meanEnglish;
    private List<String> validWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_by_find_and_fill);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);
        // Ánh xạ các thành phần giao diện
//        meanEnglish = findViewById(R.id.meanEnglish);
        meanEnglish = findViewById(R.id.meanEnglish);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnHome = findViewById(R.id.btnHome);
        btnTopics = findViewById(R.id.btnTopics);
        btnProfile = findViewById(R.id.btnProfile);
        btnCommunity = findViewById(R.id.btnCommunity);
        btnAdd = findViewById(R.id.btnAdd);
        tvResult = findViewById(R.id.tvResult);
        tvPronounce = findViewById(R.id.tvPronounce);
        correctAnswerTv = findViewById(R.id.correctAnswer);
        incorrectAnswerTv = findViewById(R.id.incorrectAnswer);
        enterAnswer = findViewById(R.id.enterAnswer);
        tvTopicName = findViewById(R.id.tvTopicName);
        tvStt = findViewById(R.id.tvStt);
        tvQuestion = findViewById(R.id.tvQuestion);
        imgWord = findViewById(R.id.imgWord);
        ibSound = findViewById(R.id.ibSound);
        ibStar = findViewById(R.id.ibStar);

        startTime = System.currentTimeMillis();

        // Lấy topicName và trạng thái shuffle, auto-surf từ Intent
        topicName = getIntent().getStringExtra("topicName");
        isShuffle = getIntent().getBooleanExtra("isShuffle", false);
        isOnlyPriorityWords = getIntent().getBooleanExtra("isOnlyPriorityWords", false);
        selection = getIntent().getStringExtra("selection");
        userId = SessionSingleton.getInstance().getUserId();

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if (selection.equals("English")) {
                    textToSpeech.setLanguage(Locale.US);
                } else {
                    textToSpeech.setLanguage(new Locale("vi", "VN"));
                }
            } else {
                Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });

        db = FirebaseFirestore.getInstance();
        vocabularyList = new ArrayList<>();

        // Lấy topicName từ Intent
        String topicName = getIntent().getStringExtra("topicName");
        tvTopicName.setText(topicName);

        // Tải danh sách từ vựng
        loadVocabularyFromFirestore(topicName, this::setupAutoComplete);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(StudyByFindAndFillActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(StudyByFindAndFillActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });

        btnTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(StudyByFindAndFillActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(StudyByFindAndFillActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(StudyByFindAndFillActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });

        // Nút Submit/Next
        btnSubmit.setOnClickListener(v -> {
            if (btnSubmit.getText().toString().equals("Submit")) {
                checkAnswer();

                // Tự động chuyển câu sau 2 giây
                new android.os.Handler().postDelayed(() -> {
                    showNextQuestion(); // Chuyển câu tiếp theo
                }, 3000); // Thời gian trì hoãn: 2000ms (2 giây)
            } else {
                showNextQuestion(); // Chuyển câu tiếp theo nếu nút đang là "Next"
            }
        });


        setupNavigationButtons();

        // Nút âm thanh
        ibSound.setOnClickListener(v -> {
            if (vocabularyList != null && !vocabularyList.isEmpty()) {
                Vocabulary currentCard = vocabularyList.get(currentIndex);
                if (selection.equals("English")) {
                    speak(currentCard.getEnglish());
                } else {
                    speak(currentCard.getMeaning());
                }
            }
        });

        ibStar.setOnClickListener(v -> {
            if (!vocabularyList.isEmpty()) {
                Vocabulary currentCard = vocabularyList.get(currentIndex);

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
                        .whereEqualTo("ownerId", SessionSingleton.getInstance().getUserId())
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

    private void setupAutoComplete() {
        List<String> suggestions = new ArrayList<>();
        for (Vocabulary vocab : vocabularyList) {
            suggestions.add(vocab.getEnglish());
        }

        // Adapter để hiển thị gợi ý
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                suggestions
        );

        meanEnglish.setAdapter(adapter);
    }

    private void loadVocabularyFromFirestore(String topicName, Runnable onVocabularyLoaded) {
        db.collection("topics")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String topicId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("topics").document(topicId).collection("vocabularies")
                                .get()
                                .addOnSuccessListener(vocabSnapshot -> {
                                    vocabularyList.clear(); // Xóa danh sách cũ

                                    for (QueryDocumentSnapshot doc : vocabSnapshot) {
                                        Vocabulary vocab = doc.toObject(Vocabulary.class);

                                        // Nếu isOnlyPriorityWords = true, chỉ thêm từ có priority = true
                                        if (!isOnlyPriorityWords || vocab.getPriority()) {
                                            vocabularyList.add(vocab);
                                            validWords.add(vocab.getEnglish());
                                        }
                                    }

                                    // Nếu isShuffle = true, trộn danh sách từ
                                    if (isShuffle) {
                                        Collections.shuffle(vocabularyList);
                                    }

                                    if (!vocabularyList.isEmpty()) {
                                        showQuestion(0); // Hiển thị câu hỏi đầu tiên
                                    } else {
                                        Toast.makeText(this, "No vocabularies available!", Toast.LENGTH_SHORT).show();
                                    }

                                    // Thực thi callback
                                    if (onVocabularyLoaded != null) {
                                        onVocabularyLoaded.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to load vocabularies!", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Topic not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load topic!", Toast.LENGTH_SHORT).show();
                });
    }

    private String checkSpelling(String input) {
        String correctedWord = input;

        // Tìm từ gần nhất dựa trên Levenshtein distance
        int minDistance = Integer.MAX_VALUE;
        for (String validWord : validWords) {
            int distance = calculateLevenshteinDistance(input.toLowerCase(), validWord.toLowerCase());
            if (distance < minDistance) {
                minDistance = distance;
                correctedWord = validWord;
            }
        }

        // Nếu khoảng cách > ngưỡng (ví dụ: 2 ký tự), giữ nguyên từ gốc
        if (minDistance > 2) {
            return input;
        }

        return correctedWord;
    }

    private int calculateLevenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    private void showQuestion(int index) {
        Vocabulary vocab = vocabularyList.get(index);
        questionStartTime = System.currentTimeMillis();

        // Hiển thị câu hỏi
        if (selection.equals("English")) {
            tvQuestion.setText(vocab.getMeaning()); // Mặt trước: nghĩa
        } else {
            tvQuestion.setText(vocab.getEnglish()); // Mặt trước: từ tiếng Anh
        }
        tvPronounce.setText(vocab.getPronounce());
        // Hiển thị trạng thái câu hỏi
        tvStt.setText((index + 1) + "/" + vocabularyList.size());
        meanEnglish.setText(""); // Xóa nội dung nhập trước đó
        tvResult.setVisibility(View.GONE);
        correctAnswerTv.setVisibility(View.GONE);
        incorrectAnswerTv.setVisibility(View.GONE);
        enterAnswer.setVisibility(View.VISIBLE);
        meanEnglish.setVisibility(View.VISIBLE);
        btnSubmit.setText("Submit");

        // Tự động phát âm từ tiếng Anh
        autoSpeak(vocab.getEnglish());

        // Cập nhật biểu tượng ưu tiên
        if (vocab.getPriority()) {
            ibStar.setImageResource(R.drawable.baseline_star_24);
        } else {
            ibStar.setImageResource(R.drawable.baseline_star_border_24);
        }
        if (vocab.getPhoto() != null && !vocab.getPhoto().isEmpty()) {
            try {
                // Loại bỏ phần "data:image/jpeg;base64," trước khi decode
                String base64Image = vocab.getPhoto().split(",")[1];
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
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



    private void checkAnswer() {
        long questionEndTime = System.currentTimeMillis(); // Thời gian người dùng nhấn Submit
        int questionTime = (int) ((questionEndTime - questionStartTime) / 1000); // Thời gian trả lời mỗi câu (giây)
        totalTimeSpent += questionTime;

        String userAnswer = meanEnglish.getText().toString().trim();
        String correctedAnswer = checkSpelling(userAnswer);
        Vocabulary currentVocab = vocabularyList.get(currentIndex);

        String correctAnswer = selection.equals("English") ? currentVocab.getEnglish() : currentVocab.getMeaning();

        // Ẩn nút Submit
        btnSubmit.setVisibility(View.GONE);

        // Tạo đối tượng WordLearned
        WordLearned wordLearned = new WordLearned(
                currentIndex + 1, // Thứ tự
                selection.equals("English") ? currentVocab.getEnglish() : currentVocab.getMeaning(), // Từ cần học
                userAnswer, // Câu trả lời của người dùng
                correctAnswer // Câu trả lời đúng
        );

        if (correctedAnswer.equalsIgnoreCase(correctAnswer)) {
            // Hiển thị thông báo trả lời đúng
            tvResult.setText("The correct answer!");
            tvResult.setTextColor(Color.parseColor("#00D30E"));
            correctAnswerTv.setText(correctAnswer);
            correctAnswerTv.setVisibility(View.VISIBLE);
            countCorrect++;
            int questionScore = Math.max(0, (100 - questionTime)*100);
            totalScore += questionScore; // Cộng dồn điểm
            // Tăng `numberOfStudy` nếu trả lời đúng
            updateVocabularyStatus(currentVocab, true);
            if (correctSound.isPlaying()) {
                correctSound.stop();
                correctSound.prepareAsync();
            }
            correctSound.start();
        } else {
            // Hiển thị thông báo trả lời sai
            tvResult.setText("The incorrect answer!");
            tvResult.setTextColor(Color.RED);
            incorrectAnswerTv.setText(userAnswer);
            incorrectAnswerTv.setVisibility(View.VISIBLE);
            correctAnswerTv.setText(correctAnswer);
            correctAnswerTv.setVisibility(View.VISIBLE);

            // Không tăng `numberOfStudy`, nhưng vẫn cập nhật trạng thái "Đã học"
            updateVocabularyStatus(currentVocab, false);
            if (incorrectSound.isPlaying()) {
                incorrectSound.stop();
                incorrectSound.prepareAsync();
            }
            incorrectSound.start();
        }

        // Thêm kết quả vào danh sách wordLearnedList
        if (wordLearnedList == null) {
            wordLearnedList = new ArrayList<>();
        }
        wordLearnedList.add(wordLearned);

        // Hiển thị kết quả
        tvResult.setVisibility(View.VISIBLE);
        enterAnswer.setVisibility(View.GONE);
        meanEnglish.setVisibility(View.GONE);

        // Hiện lại nút Submit sau khi chuyển sang câu hỏi mới
        new android.os.Handler().postDelayed(() -> btnSubmit.setVisibility(View.VISIBLE), 3000); // Thời gian trì hoãn: 3 giây
    }



    private void showNextQuestion() {
        if (currentIndex < vocabularyList.size() - 1) {
            currentIndex++;
            showQuestion(currentIndex); // Hiển thị câu hỏi tiếp theo
        } else {

            Intent intent = new Intent(StudyByFindAndFillActivity.this, ResultOfStudyByFindAndFillActivity.class);

            // Truyền dữ liệu qua Intent
            intent.putExtra("countCorrect", countCorrect);
            intent.putExtra("totalQuestions", vocabularyList.size());
            intent.putExtra("topicName", topicName);
            intent.putExtra("userId", userId);
            intent.putExtra("totalScore", totalScore);
            intent.putParcelableArrayListExtra("wordLearnedList", new ArrayList<>(wordLearnedList));

            startActivity(intent);
            finish(); // Đóng màn hình hiện tại
        }
    }

    private void setupNavigationButtons() {
        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);

        btnHome1.setOnClickListener(view -> startActivity(new Intent(this, HomeActivity.class)));
        btnTopics1.setOnClickListener(view -> startActivity(new Intent(this, TopicActivity.class)));
        btnProfile1.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));
        btnCommunity1.setOnClickListener(view -> startActivity(new Intent(this, CommunityActivity.class)));
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void autoSpeak(String text) {
        if (textToSpeech != null && text != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void updateVocabularyStatus(Vocabulary vocab, boolean isCorrect) {
        String topicName = getIntent().getStringExtra("topicName");
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("topics")
                .whereEqualTo("name", topicName)
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String topicId = querySnapshot.getDocuments().get(0).getId();

                        db.collection("topics").document(topicId).collection("vocabularies")
                                .whereEqualTo("english", vocab.getEnglish())
                                .get()
                                .addOnSuccessListener(vocabSnapshot -> {
                                    if (!vocabSnapshot.isEmpty()) {
                                        String vocabId = vocabSnapshot.getDocuments().get(0).getId();

                                        int newNumberOfStudy = isCorrect ? vocab.getNumberOfStudy() + 1 : vocab.getNumberOfStudy();
                                        String newStatus = newNumberOfStudy >= 5 ? "Đã thuộc" : "Đã học";

                                        db.collection("topics").document(topicId).collection("vocabularies").document(vocabId)
                                                .update("numberOfStudy", newNumberOfStudy, "status", newStatus)
                                                .addOnSuccessListener(aVoid -> {
                                                    vocab.setNumberOfStudy(newNumberOfStudy);
                                                    vocab.setStatus(newStatus);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to update vocabulary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(this, "Vocabulary not found!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to query vocabulary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Topic not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to query topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
