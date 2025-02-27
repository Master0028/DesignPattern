package com.example.vopet.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StudyByMulChoiceActivity extends AppCompatActivity {
    private Button btnChoose1, btnChoose2, btnChoose3, btnChoose4, btnDontKnow, btnStop;
    private TextView stt, textViewResult, tv_choose, word, pronunciate, tv_topicName;
    private String correctAnswer = "";
    private ImageButton ibSound, ibStar;
    private Switch swtch;
    private TextToSpeech textToSpeech;
    private ArrayList<String> wordsList;
    private ArrayList<String> meaningsList;
    private ArrayList<String> pronunciationList;
    private MediaPlayer correctSound, incorrectSound;
    private int currentWordIndex = 0; // Biến lưu vị trí từ vựng hiện tại
    private boolean isAutoSpeakingChecked = false;
    private int correctCount = 0;
    private String topicName, selection, userId;
    private boolean isTextToSpeechReady = false;
    private List<WordLearned> learnedWordsList = new ArrayList<>();
    private FirebaseFirestore db;
    private ImageView imgWord;
    private long startTime, questionStartTime; // Thời gian bắt đầu và thời gian mỗi câu
    private int totalScore = 0; // Tổng điểm
    private int totalTimeSpent = 0; // Tổng thời gian học (giây)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_by_mul_choice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startTime = System.currentTimeMillis();

        userId = SessionSingleton.getInstance().getUserId();
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        btnChoose1 = findViewById(R.id.btnChoose1);
        btnChoose2 = findViewById(R.id.btnChoose2);
        btnChoose3 = findViewById(R.id.btnChoose3);
        btnChoose4 = findViewById(R.id.btnChoose4);
        btnDontKnow = findViewById(R.id.btnDontKnow);
        textViewResult = findViewById(R.id.tvResult);
        tv_choose = findViewById(R.id.tv_choose);
        tv_topicName = findViewById(R.id.tv_topicName);
        ibSound = findViewById(R.id.ibSound);
        ibStar = findViewById(R.id.ibStar);
        tv_topicName = findViewById(R.id.tv_topicName);
        stt = findViewById(R.id.stt);
        // Gán giá trị cho word
        word = findViewById(R.id.word);
        pronunciate = findViewById(R.id.pronunciate);
        imgWord = findViewById(R.id.imgWord);

        topicName = intent.getStringExtra("topicName");
        tv_topicName.setText(topicName);
        wordsList = intent.getStringArrayListExtra("wordsList");
        meaningsList = intent.getStringArrayListExtra("meaningsList");
        pronunciationList = intent.getStringArrayListExtra("pronunciationList");
        isAutoSpeakingChecked = intent.getBooleanExtra("isAutoSpeakingChecked", false);
        selection = intent.getStringExtra("selectedLanguage");

        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if (selection.equals("English")) {
                    textToSpeech.setLanguage(Locale.US);
                } else {
                    textToSpeech.setLanguage(new Locale("vi", "VN"));
                }
                isTextToSpeechReady = true; // Đánh dấu TextToSpeech đã sẵn sàng
                // Nếu có từ đầu tiên, gọi speak() ngay sau khi TextToSpeech sẵn sàng
                if (currentWordIndex == 0 && wordsList != null && !wordsList.isEmpty()) {
                    speak(wordsList.get(0));
                }
            } else {
                Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Hiển thị từ vựng đầu tiên
        if (wordsList != null && !wordsList.isEmpty()) {
            displayCurrentWord();
        } else {
            Toast.makeText(this, "No words available!", Toast.LENGTH_SHORT).show();
            finish();
        }
        ibSound.setOnClickListener(v -> {
            String text = word.getText().toString();
            if (isEnglishWord(text)) {
                textToSpeech.setLanguage(Locale.US);
            } else {
                textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
            }
            speak(text);
        });

        btnChoose1.setOnClickListener(v -> handleAnswer(btnChoose1));
        btnChoose2.setOnClickListener(v -> handleAnswer(btnChoose2));
        btnChoose3.setOnClickListener(v -> handleAnswer(btnChoose3));
        btnChoose4.setOnClickListener(v -> handleAnswer(btnChoose4));
        btnDontKnow.setOnClickListener(v -> handleDontKnow());

        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        ImageButton btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(StudyByMulChoiceActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(StudyByMulChoiceActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(StudyByMulChoiceActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(StudyByMulChoiceActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(StudyByMulChoiceActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });

        ibStar.setOnClickListener(v -> {
            if (currentWordIndex < wordsList.size()) {
                String englishWord = wordsList.get(currentWordIndex);

                db.collection("topics")
                        .whereEqualTo("name", topicName)
                        .whereEqualTo("ownerId", userId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String topicId = querySnapshot.getDocuments().get(0).getId();

                                db.collection("topics").document(topicId).collection("vocabularies")
                                        .whereEqualTo("english", englishWord)
                                        .get()
                                        .addOnSuccessListener(vocabSnapshots -> {
                                            if (!vocabSnapshots.isEmpty()) {
                                                String vocabId = vocabSnapshots.getDocuments().get(0).getId();
                                                boolean currentPriority = vocabSnapshots.getDocuments().get(0).getBoolean("priority");

                                                // Đảo ngược trạng thái priority
                                                boolean newPriority = !currentPriority;

                                                // Cập nhật Firestore
                                                db.collection("topics").document(topicId).collection("vocabularies").document(vocabId)
                                                        .update("priority", newPriority)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Cập nhật icon
                                                            if (newPriority) {
                                                                ibStar.setImageResource(R.drawable.baseline_star_24);
                                                                Toast.makeText(this, "Marked as priority", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                ibStar.setImageResource(R.drawable.baseline_star_border_24);
                                                                Toast.makeText(this, "Removed from priority", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(this, "Failed to update priority: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        });
                            }
                        });
            }
        });

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

    private boolean isEnglishWord(String text) {
        // Logic kiểm tra xem từ có phải tiếng Anh hay không.
        // Ở đây chỉ là kiểm tra đơn giản: nếu tất cả các ký tự là chữ cái tiếng Anh thì coi là từ tiếng Anh.
        return text.matches("^[a-zA-Z\\s]+$");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }
        if (incorrectSound != null) {
            incorrectSound.release();
            incorrectSound = null;
        }
    }

    private void displayCurrentWord() {
        questionStartTime = System.currentTimeMillis(); // Lưu thời gian bắt đầu câu hỏi
        if (currentWordIndex < wordsList.size() && currentWordIndex < meaningsList.size()) {

            String englishWord = wordsList.get(currentWordIndex);
            String meaning = meaningsList.get(currentWordIndex);
            String pronunciation = pronunciationList.get(currentWordIndex);
            int totalWords = wordsList.size();
            stt.setText((currentWordIndex + 1) + "/" + totalWords);
            word.setText(englishWord);
            pronunciate.setText(pronunciation);
            correctAnswer = meaning;

            // Tự động phát âm từ hiện tại
            speak(englishWord);

            db.collection("topics")
                    .whereEqualTo("name", topicName)
                    .whereEqualTo("ownerId", userId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String topicId = querySnapshot.getDocuments().get(0).getId();

                            db.collection("topics").document(topicId).collection("vocabularies")
                                    .whereEqualTo("english", englishWord)
                                    .get()
                                    .addOnSuccessListener(vocabSnapshots -> {
                                        if (!vocabSnapshots.isEmpty()) {
                                            // Lấy thông tin từ vựng
                                            Vocabulary vocabulary = vocabSnapshots.getDocuments().get(0).toObject(Vocabulary.class);

                                            if (vocabulary != null) {
                                                // Kiểm tra nếu có ảnh
                                                if (vocabulary.getPhoto() != null && !vocabulary.getPhoto().isEmpty()) {
                                                    try {
                                                        // Loại bỏ phần "data:image/jpeg;base64," trước khi decode
                                                        String base64Image = vocabulary.getPhoto().split(",")[1];
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

                                                // Cập nhật trạng thái "priority" cho nút `ibStar`
                                                boolean priority = vocabulary.getPriority();
                                                if (priority) {
                                                    ibStar.setImageResource(R.drawable.baseline_star_24);
                                                } else {
                                                    ibStar.setImageResource(R.drawable.baseline_star_border_24);
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to fetch vocabulary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
            // Tạo danh sách đáp án và đảm bảo có 4 đáp án cho câu hỏi
            List<String> answerOptions = new ArrayList<>(meaningsList);
            answerOptions.remove(correctAnswer);

            // Thêm đáp án đúng vào danh sách
            List<String> options = new ArrayList<>();
            options.add(correctAnswer);

            // Thêm các đáp án sai
            Collections.shuffle(answerOptions);
            for (int i = 0; i < Math.min(3, answerOptions.size()); i++) {
                options.add(answerOptions.get(i));
            }

            // Thêm lựa chọn "None" nếu thiếu đáp án
            while (options.size() < 4) {
                options.add("None");
            }

            Collections.shuffle(options); // Trộn đáp án

            // Gán giá trị cho các nút đáp án
            btnChoose1.setText(options.get(0));
            btnChoose2.setText(options.get(1));
            btnChoose3.setText(options.get(2));
            btnChoose4.setText(options.get(3));

            // Cập nhật icon cho ibStar dựa trên trạng thái priority
            db.collection("topics")
                    .whereEqualTo("name", topicName)
                    .whereEqualTo("ownerId", userId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String topicId = querySnapshot.getDocuments().get(0).getId();

                            db.collection("topics").document(topicId).collection("vocabularies")
                                    .whereEqualTo("english", englishWord)
                                    .get()
                                    .addOnSuccessListener(vocabSnapshots -> {
                                        if (!vocabSnapshots.isEmpty()) {
                                            boolean priority = vocabSnapshots.getDocuments().get(0).getBoolean("priority");
                                            if (priority) {
                                                ibStar.setImageResource(R.drawable.baseline_star_24);
                                            } else {
                                                ibStar.setImageResource(R.drawable.baseline_star_border_24);
                                            }
                                        }
                                    });
                        }
                    });

            resetAnswerButtons();
        } else {
            navigateToResultActivity();
        }
    }

    private void resetAnswerButtons() {
        // Đặt lại giao diện các nút đáp án về trạng thái ban đầu
        Button[] allButtons = {btnChoose1, btnChoose2, btnChoose3, btnChoose4};

        for (Button button : allButtons) {
            button.setEnabled(true);
            button.setBackgroundResource(R.drawable.button_border); // Thiết lập lại viền mặc định
            button.setTextColor(Color.parseColor("#000000")); // Màu chữ mặc định (đen)
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // Xóa icon
        }

        btnDontKnow.setEnabled(true);
        btnDontKnow.setTextColor(Color.parseColor("#F37122")); // Màu mặc định của nút "You don't know?"
        textViewResult.setVisibility(View.GONE); // Ẩn thông báo kết quả
    }

    private void resetButtons() {
        // Đặt lại giao diện các nút
        textViewResult.setVisibility(View.GONE);
        btnChoose1.setEnabled(true);
        btnChoose2.setEnabled(true);
        btnChoose3.setEnabled(true);
        btnChoose4.setEnabled(true);
        btnDontKnow.setEnabled(true);
    }

    private void handleAnswer(Button selectedButton) {
        tv_choose.setVisibility(View.GONE);

        long questionEndTime = System.currentTimeMillis(); // Thời gian người dùng nhấn trả lời
        int questionTime = (int) ((questionEndTime - questionStartTime) / 1000); // Tính thời gian trả lời (giây)
        totalTimeSpent += questionTime;

        String userAnswer = selectedButton.getText().toString();
        boolean isCorrect = userAnswer.equalsIgnoreCase(correctAnswer);

        if (isCorrect) {
            textViewResult.setVisibility(View.VISIBLE);
            textViewResult.setText("Correct answer!");
            correctCount++;
            textViewResult.setTextColor(Color.parseColor("#00D30E"));
            selectedButton.setBackgroundResource(R.drawable.button_correct_border);
            selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.correct_icon, 0);

            int questionScore = Math.max(0, (100 - questionTime)*100);
            totalScore += questionScore; // Cộng dồn điểm

            if (correctSound.isPlaying()) {
                correctSound.stop();
                correctSound.prepareAsync();
            }
            correctSound.start();
        } else {
            textViewResult.setVisibility(View.VISIBLE);
            textViewResult.setText("Wrong answer!");
            textViewResult.setTextColor(Color.RED);
            selectedButton.setBackgroundResource(R.drawable.button_incorrect_border);
            selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.incorrect_icon, 0);
            highlightCorrectAnswer();

            if (incorrectSound.isPlaying()) {
                incorrectSound.stop();
                incorrectSound.prepareAsync();
            }
            incorrectSound.start();
        }

        // Lưu thông tin từ đã học
        WordLearned wordLearned = new WordLearned(
                currentWordIndex + 1,
                wordsList.get(currentWordIndex),
                userAnswer,
                correctAnswer
        );
        learnedWordsList.add(wordLearned);

        // Cập nhật từ vựng trong Firestore
        updateVocabularyInFirestore(wordsList.get(currentWordIndex), isCorrect);

        disableOtherButtons(selectedButton);

        // Tự động chuyển sang từ tiếp theo sau 3 giây
        selectedButton.postDelayed(() -> {
            currentWordIndex++;
            if (currentWordIndex < wordsList.size()) {
                displayCurrentWord();
            } else {
                navigateToResultActivity();
            }
        }, 5000);
    }


    private void handleDontKnow() {
        disableOtherButtons(null);

        textViewResult.setVisibility(View.VISIBLE);
        textViewResult.setText("Correct answer: " + correctAnswer);
        textViewResult.setTextColor(Color.BLUE);

        // Tự động chuyển sang từ tiếp theo sau 3 giây
        btnDontKnow.postDelayed(() -> {
            currentWordIndex++;
            if (currentWordIndex < wordsList.size()) {
                // Nếu còn từ để học, chuyển sang từ tiếp theo và hiển thị lại
                displayCurrentWord();
            } else {
                // Nếu đã hoàn thành tất cả các từ
                navigateToResultActivity();
            }
        }, 3000);
    }

    private void highlightCorrectAnswer() {
        Button[] allButtons = {btnChoose1, btnChoose2, btnChoose3, btnChoose4};

        for (Button button : allButtons) {
            if (button.getText().toString().equalsIgnoreCase(correctAnswer)) {
                button.setBackgroundResource(R.drawable.button_correct_border); // Đổi viền thành màu xanh lá
                button.setTextColor(Color.parseColor("#00D30E")); // Đổi màu chữ thành xanh lá
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.correct_icon, 0); // Thêm icon dấu tích
            }
        }
    }

    private void disableOtherButtons(Button selectedButton) {
        Button[] allButtons = {btnChoose1, btnChoose2, btnChoose3, btnChoose4};

        for (Button button : allButtons) {
            if (button != selectedButton && !button.getText().toString().equalsIgnoreCase(correctAnswer)) {
                button.setBackgroundResource(R.drawable.button_disabled_border);
                button.setEnabled(false);
                button.setTextColor(Color.parseColor("#9E9E9E"));
            }
        }
        btnDontKnow.setTextColor(Color.parseColor("#A0A0A0"));
        btnDontKnow.setEnabled(false);
    }

    private void navigateToResultActivity() {
        int totalQuestions = wordsList.size();

        long endTime = System.currentTimeMillis(); // Thời gian kết thúc
        int totalTimeInSeconds = (int) ((endTime - startTime) / 1000); // Tính tổng thời gian (giây)

        Intent intent = new Intent(this, ResultOfStudyByMulChoiceActivity.class);
        intent.putExtra("countCorrect", correctCount);
        intent.putExtra("totalQuestions", totalQuestions);
        intent.putExtra("topicName", tv_topicName.getText().toString());
        intent.putExtra("userId", userId);
        intent.putExtra("totalScore", totalScore); // Truyền tổng điểm
        intent.putParcelableArrayListExtra("wordLearnedList", new ArrayList<>(learnedWordsList));
        startActivity(intent);
        finish();
    }

    private void updateVocabularyInFirestore(String word, boolean isCorrect) {
        db.collection("topics")
                .whereEqualTo("name", topicName)
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Tìm tài liệu của từ vựng cần cập nhật
                        db.collection("topics").document(topicId).collection("vocabularies")
                                .whereEqualTo("english", word)
                                .get()
                                .addOnSuccessListener(vocabSnapshots -> {
                                    if (!vocabSnapshots.isEmpty()) {
                                        String vocabId = vocabSnapshots.getDocuments().get(0).getId();

                                        Vocabulary vocabulary = vocabSnapshots.getDocuments().get(0).toObject(Vocabulary.class);
                                        if (vocabulary != null) {
                                            int newNumberOfStudy = vocabulary.getNumberOfStudy();
                                            String newStatus = vocabulary.getStatus();

                                            if (isCorrect) {
                                                newNumberOfStudy += 1;
                                                newStatus = newNumberOfStudy >= 5 ? "Đã thuộc" : "Đã học";
                                            } else {
                                                newStatus = "Đã học";
                                            }

                                            // Cập nhật trong Firestore
                                            db.collection("topics").document(topicId).collection("vocabularies")
                                                    .document(vocabId)
                                                    .update("numberOfStudy", newNumberOfStudy, "status", newStatus)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
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


}