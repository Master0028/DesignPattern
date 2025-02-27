package com.example.vopet.activity;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import com.example.vopet.R;
import com.example.vopet.model.Topic;
import com.example.vopet.model.Vocabulary;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateNewTopicActivity extends AppCompatActivity {
    private LinearLayout containerLayout;
    private Button btnAddWord, btnSetting, btnBack, btnCreate, btnImport;
    private FirebaseFirestore db;
    private TextToSpeech textToSpeech;
    private EditText txtTopicName;
    private String tvPermission = "private";
    private String topicName;
    private static final int REQUEST_CODE_PICK_IMAGE = 102;
    private static final int REQUEST_PERMISSION_READ_STORAGE = 1;
    private ImageButton selectedImageButton;
    private TextView tvReview;
    private List<Vocabulary> vocabularyList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_topic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        Button btnImport = findViewById(R.id.btnImport);
        containerLayout = findViewById(R.id.linearlayout); // Layout mà bạn sẽ thêm CardView vào
        btnAddWord = findViewById(R.id.btnAddWord); // Nút Add Word
        btnSetting = findViewById(R.id.btnSetting);
        btnBack = findViewById(R.id.btnBack);
        btnCreate = findViewById(R.id.btnCreate);
        txtTopicName = findViewById(R.id.topicName);
        btnAddWord.setOnClickListener(v -> addNewCard());

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US); // Đặt ngôn ngữ
            } else {
                Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SDK_INT >= Build.VERSION_CODES.R)
                {
                    if(Environment.isExternalStorageManager()){
                        //choosing csv file
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE,true);
                        startActivityForResult(Intent.createChooser(intent,"Select File "),101);
                    }
                    else{
                        //getting permission from user
                        Intent intent=new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri=Uri.fromParts("package",getPackageName(),null);
                        startActivity(intent);
                    }
                }
                showImportDialog();
            }
        });


        btnSetting.setOnClickListener(v -> showSettingPrivacyDialog());

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topicName = txtTopicName.getText().toString();
                if(!topicName.isEmpty()) {
                    saveVocabulariesToFirestore(topicName);
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent); // Trả kết quả về Home activity
                    finish(); // Quay lại màn hình Home
                } else {
                    txtTopicName.setError("Please enter topic's name");
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void addNewCard() {
        // Sử dụng LayoutInflater để thêm CardView từ XML
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.add_word_item_each_topic, containerLayout, false);

        ImageButton btnImportImg = cardView.findViewById(R.id.btnImportImg);
        Button btnDel = cardView.findViewById(R.id.btnDel);
        Button btnImportVoice = cardView.findViewById(R.id.btnImportVoice);
        btnImportImg.setOnClickListener(v -> {

            selectedImageButton = btnImportImg; // Ghi nhớ nút đã chọn
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
            openImagePicker();
        });

        btnDel.setOnClickListener(v -> {
            containerLayout.removeView(cardView); // Xóa CardView khỏi LinearLayout
        });

        btnImportVoice.setOnClickListener(v -> {
            EditText etWord = cardView.findViewById(R.id.word);
            String wordToSpeak = etWord.getText().toString().trim();
            if (!wordToSpeak.isEmpty()) {
                textToSpeech.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, null); // Đọc từ
            } else {
                Toast.makeText(this, "Please enter a word to speak", Toast.LENGTH_SHORT).show();
            }
        });

        int index = containerLayout.indexOfChild(findViewById(R.id.btnAddWord));
        containerLayout.addView(cardView, index);
    }

    private void showImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_import_topic, null);
        builder.setView(dialogView);
        AlertDialog importDialog = builder.create();

        if (importDialog.getWindow() != null) {
            importDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        EditText etTopicName = dialogView.findViewById(R.id.etTopicName);
        Button btnConfirmImport = dialogView.findViewById(R.id.btnCreate);
        Button btnCancelImport = dialogView.findViewById(R.id.btnCancel);
        tvReview = dialogView.findViewById(R.id.tvReview);
        Spinner spinnerAccessing = dialogView.findViewById(R.id.spinnerAccessing);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.accessing_permission, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessing.setAdapter(adapter);

        btnConfirmImport.setOnClickListener(v -> {
            String topicName = etTopicName.getText().toString().trim();
            if (topicName.isEmpty()) {
                Toast.makeText(this, "Topic name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (vocabularyList.isEmpty()) {
                Toast.makeText(this, "No vocabularies to import!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu topic vào Firestore
            long timestamp = System.currentTimeMillis();
            int countVob = vocabularyList.size();

            String selectedAccessingPermission = spinnerAccessing.getSelectedItem().toString();

            Topic newTopic = new Topic(null, SessionSingleton.getInstance().getUserId(), topicName, countVob, 0, timestamp, 0);
            newTopic.setPermission(selectedAccessingPermission);
            db.collection("topics")
                    .add(newTopic)
                    .addOnSuccessListener(documentReference -> {
                        String topicId = documentReference.getId();

                        // Thêm vocabularies vào subcollection
                        for (Vocabulary vocab : vocabularyList) {
                            db.collection("topics")
                                    .document(topicId)
                                    .collection("vocabularies")
                                    .add(vocab)
                                    .addOnSuccessListener(aVoid -> {
                                        // Cập nhật danh sách RecyclerView nếu cần
                                    })
                                    .addOnFailureListener(e -> {
                                        e.printStackTrace();
                                        Toast.makeText(this, "Failed to import vocabulary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }

                        // Truy vấn và cập nhật thuộc tính numberOfWord của folder

                        Toast.makeText(this, "Topic and vocabularies imported successfully!", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent); // Trả kết quả về Home activity
                        finish(); // Quay lại màn hình Home
                        importDialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to create topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnCancelImport.setOnClickListener(v -> importDialog.dismiss());
        importDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null && selectedImageButton != null) {
                selectedImageButton.setImageURI(selectedImageUri);
                selectedImageButton.setTag(selectedImageUri);
                Toast.makeText(this, "Image selected: " + selectedImageUri.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    String fileType = getContentResolver().getType(fileUri);
                    Toast.makeText(this, "tệp: " + fileType, Toast.LENGTH_SHORT).show();
                    // Kiểm tra MIME type của tệp
                    if ("text/comma-separated-values".equals(fileType) || "text/csv".equals(fileType)) {
                        importCsvFile(fileUri);
                    } else {
                        Toast.makeText(this, "Vui lòng chọn tệp CSV", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void importCsvFile(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            StringBuilder fileContent = new StringBuilder(); // Dùng để hiển thị file
            vocabularyList.clear(); // Xóa danh sách cũ

            while ((line = reader.readLine()) != null) {
                // Bỏ qua dòng trống hoặc không hợp lệ
                if (line.trim().isEmpty()) continue;

                // Tách các cột trong dòng CSV
                String[] values = line.split(","); // Giả sử dùng dấu phẩy làm dấu phân cách
                if (values.length >= 5) {
                    try {
                        int stt = Integer.parseInt(values[0].trim());
                        String english = values[1].trim();
                        String meaning = values[2].trim();
                        String pronounce = values[3].trim();
                        boolean priority = Boolean.parseBoolean(values[4].trim());

                        // Tạo Vocabulary và thêm vào danh sách
                        Vocabulary vocab = new Vocabulary(stt, english, meaning, pronounce, "Chưa học", priority);
                        vocabularyList.add(vocab);

                        // Thêm dòng vào nội dung hiển thị
                        fileContent.append(line).append("\n");
                    } catch (NumberFormatException e) {
                        // Bỏ qua các dòng có lỗi định dạng số
                        e.printStackTrace();
                    }
                }
            }

            // Cập nhật nội dung lên TextView
            if (tvReview != null) {
                tvReview.setText(fileContent.toString());
            } else {
                Toast.makeText(this, "Cannot update tvReview: Reference is null", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, "Imported " + vocabularyList.size() + " vocabularies from CSV", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveVocabulariesToFirestore(String topicName) {
        // Lấy danh sách các CardView từ containerLayout
        int childCount = containerLayout.getChildCount();
        List<Vocabulary> vocabularyList = new ArrayList<>();

        int stt = 1;
        for (int i = 0; i < childCount; i++) {
            View cardView = containerLayout.getChildAt(i);

            if (cardView != null && cardView.findViewById(R.id.word) != null) {
                // Lấy thông tin từ các EditText và CheckBox trong CardView
                EditText etWord = cardView.findViewById(R.id.word);
                EditText etMean = cardView.findViewById(R.id.mean);
                EditText etPronounce = cardView.findViewById(R.id.pronounce);
                CheckBox cbPriority = cardView.findViewById(R.id.checkBoxPriority);
                ImageButton btnImportImg = cardView.findViewById(R.id.btnImportImg);

                String word = etWord.getText().toString().trim();
                String meaning = etMean.getText().toString().trim();
                String pronounce = etPronounce.getText().toString().trim();
                boolean priority = cbPriority.isChecked();

                String photoBase64 = null;
                if (btnImportImg.getTag() != null) {
                    Uri imageUri = (Uri) btnImportImg.getTag();
                    photoBase64 = convertImageToBase64(imageUri);
                }

                if (!word.isEmpty() && !meaning.isEmpty()) {
                    // Tạo đối tượng Vocabulary
                    Vocabulary vocab = new Vocabulary(stt++, word, meaning, pronounce, "Chưa học", priority, photoBase64);
                    vocabularyList.add(vocab);
                }
            }
        }

        if (vocabularyList.isEmpty()) {
            Toast.makeText(this, "No vocabularies to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        int countVob = vocabularyList.size();
        Topic newTopic = new Topic(null, SessionSingleton.getInstance().getUserId(), topicName, countVob, 0, timestamp, 0);
        if (!tvPermission.isEmpty()) {
            newTopic.setPermission(tvPermission);
        }
        // Thêm vào Firestore
        db.collection("topics")
                .add(newTopic)
                .addOnSuccessListener(documentReference -> {
                    String topicId = documentReference.getId();

                    // Thêm vocabularies vào subcollection
                    for (Vocabulary vocab : vocabularyList) {
                        db.collection("topics")
                                .document(topicId)
                                .collection("vocabularies")
                                .add(vocab)
                                .addOnSuccessListener(aVoid -> {
                                    // Cập nhật danh sách RecyclerView nếu cần
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Failed to import vocabulary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                    Toast.makeText(this, "Topic and vocabularies imported successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to create topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSettingPrivacyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_setting_privacy_topic, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

//        Spinner spinnerEditing = dialogView.findViewById(R.id.spinnerEditing);
        Spinner spinnerAccessing = dialogView.findViewById(R.id.spinnerAccessing);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.accessing_permission, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessing.setAdapter(adapter);

//        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
//                this, R.array.accessing_permission, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerAccessing.setAdapter(adapter1);

        btnSave.setOnClickListener(v -> {
            // Lấy giá trị đã chọn từ Spinner
            String selectedAccessingPermission = spinnerAccessing.getSelectedItem().toString();

            // Kiểm tra giá trị
            if (selectedAccessingPermission.isEmpty()) {
                Toast.makeText(this, "Please select an accessing permission", Toast.LENGTH_SHORT).show();
                return;
            }
            tvPermission = selectedAccessingPermission;
            // Đóng dialog sau khi lưu
            dialog.dismiss();
        });


        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

}