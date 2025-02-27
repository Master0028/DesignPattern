package com.example.vopet.activity;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.CustomTypefaceSpan;
import com.example.vopet.adapter.Topic1Adapter;
import com.example.vopet.model.Folder;
import com.example.vopet.R;
import com.example.vopet.model.Topic;
import com.example.vopet.adapter.CategoryAdapterInDetailFolder;
import com.example.vopet.adapter.TopicAdapterOrange;
import com.example.vopet.model.Category;
import com.example.vopet.model.Topic1;
import com.example.vopet.model.Vocabulary;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    Spinner spinner;
    private List<Topic> topicList;
    private TopicAdapterOrange topicAdapterOrange;
    CategoryAdapterInDetailFolder categoryAdapter;
    private TextView userName, dateCreated, tvReview;
    Button btnAddTopic, btnBack, btnAddTopicAvailable;;
    private FirebaseFirestore db;
    String folderId, currentUserId;
    private String folderName, tvPermission;
    private List<Vocabulary> vocabularyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_folder_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        currentUserId = SessionSingleton.getInstance().getUserId();
        btnAddTopicAvailable = findViewById(R.id.btnAddTopicAvailable);
        btnAddTopicAvailable.setOnClickListener(v -> showAvailableTopicsDialog());

        btnBack = findViewById(R.id.btnBack);
        btnAddTopic = findViewById(R.id.btnAddTopic);
        userName = findViewById(R.id.userName);
        dateCreated = findViewById(R.id.dateCreated);
        folderName = getIntent().getStringExtra("folderName");

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        loadTopicsFromFirestore(folderName);

        topicList = new ArrayList<>();

        topicAdapterOrange = new TopicAdapterOrange(this, topicList);
        topicAdapterOrange.setFolderName(folderName);
        recyclerView.setAdapter(topicAdapterOrange);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnAddTopic.setOnClickListener(v -> showCreateFolderDialog());

        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        ImageButton btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(FolderDetailActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(FolderDetailActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(FolderDetailActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(FolderDetailActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(FolderDetailActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void showAvailableTopicsDialog() {
        // Create an AlertDialog.Builder and set the view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_available_topics, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Set dialog window background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Initialize RecyclerView for showing available topics
        RecyclerView availableTopicsRecyclerView = dialogView.findViewById(R.id.availableTopicsRecyclerView);
        availableTopicsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // List to hold available topics and adapter
        List<Topic1> availableTopicList = new ArrayList<>();
        Topic1Adapter availableTopicAdapter = new Topic1Adapter(this, availableTopicList);
        availableTopicsRecyclerView.setAdapter(availableTopicAdapter);

        // Load available topics from Firestore
        db.collection("topics")
                .whereEqualTo("ownerId", currentUserId) // Chỉ lấy topic của người dùng hiện tại
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        availableTopicList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Topic1 topic = document.toObject(Topic1.class);
                            topic.setId(document.getId());

                            // Kiểm tra nếu folderName không chứa folder hiện tại
                            List<String> folderNames = topic.getFolderName();
                            if (folderNames == null || !folderNames.contains(folderName)) {
                                availableTopicList.add(topic);
                            }
                        }
                        if (availableTopicList.isEmpty()) {
                            Toast.makeText(this, "No available topics to add!", Toast.LENGTH_SHORT).show();
                        }
                        availableTopicAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No topics found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching available topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Set onClick listener for adding a selected topic to the folder
        availableTopicAdapter.setOnItemClickListener(topic -> {
            addTopicToFolder(folderName, topic);
            dialog.dismiss(); // Đóng dialog sau khi thêm
        });
        Button btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    private void addTopicToFolder(String folderName, Topic1 topic) {
        db.collection("topics")
                .document(topic.getId()) // Tìm topic theo ID
                .update("folderName", FieldValue.arrayUnion(folderName)) // Thêm tên folder vào mảng
                .addOnSuccessListener(aVoid -> {
                    // Sau khi thêm topic vào folder thành công, cập nhật numberOfTopic
                    db.collection("folders")
                            .whereEqualTo("name", folderName) // Tìm folder theo tên
                            .whereEqualTo("creatorId", currentUserId) // Chỉ lấy folder của người dùng hiện tại
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (QueryDocumentSnapshot folderDoc : queryDocumentSnapshots) {
                                        String folderId = folderDoc.getId(); // Lấy ID của folder
                                        long currentNumberOfTopics = folderDoc.getLong("numberOfTopic"); // Lấy số lượng topic hiện tại
                                        long updatedNumberOfTopics = currentNumberOfTopics + 1; // Tăng thêm 1

                                        // Cập nhật lại số lượng topic
                                        db.collection("folders")
                                                .document(folderId)
                                                .update("numberOfTopic", updatedNumberOfTopics)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(this, "Topic added to folder and count updated!", Toast.LENGTH_SHORT).show();
                                                    loadTopicsFromFirestore(folderName); // Cập nhật danh sách topics
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to update folder count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to fetch folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add topic to folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<Category> getListCategory(){
        List<Category> list = new ArrayList<>();
        list.add(new Category("Study"));
        list.add(new Category("FlashCard"));
        list.add(new Category("Multiple Choice"));
        list.add(new Category("Word type"));
        return list;
    }
    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_topic, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        EditText etTopicName = dialogView.findViewById(R.id.etTopicName);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnImport = dialogView.findViewById(R.id.btnImport);
        Spinner spinnerAccessing = dialogView.findViewById(R.id.spinnerAccessing);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.accessing_permission, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessing.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> {
            String topicName = etTopicName.getText().toString().trim();

            if (topicName.isEmpty()) {
                etTopicName.setError("Topic name cannot be empty");
                return;
            }
            String selectedAccessingPermission = spinnerAccessing.getSelectedItem().toString();

            // Kiểm tra giá trị
            if (selectedAccessingPermission.isEmpty()) {
                Toast.makeText(this, "Please select an accessing permission", Toast.LENGTH_SHORT).show();
                return;
            }
            tvPermission = selectedAccessingPermission;

            // Gọi hàm createTopic để tạo topic
            createTopic(folderName, topicName);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
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
                dialog.dismiss();
            }
        });
        dialog.show();
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

            List<String> folderNames = new ArrayList<>();
            folderNames.add(folderName);

            Topic newTopic = new Topic(folderNames, currentUserId, topicName, countVob, 0, timestamp, 0);
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
                        db.collection("folders")
                                .whereEqualTo("name", folderName) // Lọc theo folderName
                                .whereEqualTo("creatorId", currentUserId) // Lọc theo creatorId
                                .get()
                                .addOnSuccessListener(folderQuery -> {
                                    if (!folderQuery.isEmpty()) {
                                        for (QueryDocumentSnapshot folderDoc : folderQuery) {
                                            int currentTopicCount = folderDoc.getLong("numberOfTopic").intValue();
                                            int updatedTopicCount = currentTopicCount + 1;

                                            // Cập nhật số lượng từ
                                            db.collection("folders")
                                                    .document(folderDoc.getId())
                                                    .update("numberOfTopic", updatedTopicCount)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this, "Folder updated successfully!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Failed to update folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Failed to update folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        Toast.makeText(this, "Topic and vocabularies imported successfully!", Toast.LENGTH_SHORT).show();
                        loadTopicsFromFirestore(folderName);
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

    private void loadTopicsFromFirestore(String folderName) {
        String creatorId = currentUserId; // Lấy ID người dùng hiện tại
        userName.setText(folderName);

        db.collection("topics") // Truy cập collection "topics"
                .whereEqualTo("ownerId", creatorId) // Lọc theo creatorId
                .whereArrayContains("folderName", folderName) // Kiểm tra folderName trong mảng
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        topicList.clear(); // Xóa danh sách cũ
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Topic topic = document.toObject(Topic.class);
                            topicList.add(topic); // Thêm topic vào danh sách
                        }
                        // Cập nhật RecyclerView sau khi lấy xong danh sách topics
                        topicAdapterOrange.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No topics found for this folder!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createTopic(String folderName, String topicName) {
        if (topicName.isEmpty()) {
            Toast.makeText(this, "Please enter topic name", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        int countVob = vocabularyList.size();

        List<String> folderNames = new ArrayList<>();
        folderNames.add(folderName);

        Topic newTopic = new Topic(folderNames, currentUserId, topicName, countVob, 0, timestamp, 0);
        if (!tvPermission.isEmpty()) {
            newTopic.setPermission(tvPermission);
        }

        // Lưu topic vào collection "topics" của Firestore
        db.collection("topics")
                .add(newTopic) // Tạo document mới với ID tự động
                .addOnSuccessListener(documentReference -> {
                    // Thêm topic mới vào danh sách và cập nhật RecyclerView
                    topicList.add(newTopic);
                    topicAdapterOrange.notifyItemInserted(topicList.size() - 1);

                    // Cập nhật số lượng topic trong folder
                    db.collection("folders")
                            .whereEqualTo("name", folderName)
                            .whereEqualTo("creatorId", currentUserId)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    // Lấy document ID của folder
                                    String folderId = querySnapshot.getDocuments().get(0).getId();

                                    // Cập nhật số lượng topic
                                    db.collection("folders")
                                            .document(folderId)
                                            .update("numberOfTopic", FieldValue.increment(1)) // Tăng numberOfTopic lên 1
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Folder updated successfully!", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Failed to update folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to find folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    Toast.makeText(this, "Topic created successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to create topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
            vocabularyList.clear(); // Xóa danh sách cũ
            StringBuilder fileContent = new StringBuilder();
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(","); // CSV định dạng: stt,english,meaning
                if (values.length >= 4) {
                    int stt = Integer.parseInt(values[0].trim());
                    String english = values[1].trim();
                    String meaning = values[2].trim();
                    String pronounce = values[3].trim();
                    String priority = values[4].trim();

                    boolean prio = false;
                    if(priority != null) {
                        prio = Boolean.parseBoolean(priority);
                    }

                    Vocabulary vocab = new Vocabulary(stt, english, meaning, pronounce, "Chưa học", prio);
                    vocabularyList.add(vocab);

                    fileContent.append(line).append("\n"); // Hiển thị nội dung file
                }
            }

            if (tvReview != null) {
                tvReview.setText(fileContent.toString()); // Hiển thị nội dung file CSV
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
//    @Override
//    public void onBackPressed() {
//        Intent resultIntent = new Intent();
//        resultIntent.putExtra("updated", isTopicAdded);
//        setResult(RESULT_OK, resultIntent);
//        finish();
////        super.onBackPressed();
//    }

}