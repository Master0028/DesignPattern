package com.example.vopet.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.MainActivity;
import com.example.vopet.R;
import com.example.vopet.model.Topic;
import com.example.vopet.adapter.CategoryAdapter;
import com.example.vopet.adapter.TopicAdapterBlue;
import com.example.vopet.model.Category;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopicActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TopicAdapterBlue topicAdapter;
    private List<Topic> topicList;
    Spinner spinner;
    CategoryAdapter categoryAdapter;
    Button addTopic;
    private FirebaseFirestore db;
    String currentUser;
    private TextView tvCountTopic, tvCountDownload;
    private CardView download, create;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_topic);
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
        tvCountTopic = findViewById(R.id.tvCountTopic);
        tvCountDownload = findViewById(R.id.tvCountDownload);
        download = findViewById(R.id.download);
        create = findViewById(R.id.create);

        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(TopicActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(TopicActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(TopicActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(TopicActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(TopicActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });

        db = FirebaseFirestore.getInstance();
        currentUser = SessionSingleton.getInstance().getUserId();

        Button btnMenu = findViewById(R.id.btnMenu);
        addTopic = findViewById(R.id.addTopic);
        addTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TopicActivity.this, CreateNewTopicActivity.class);
                startActivityForResult(i, 1);
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        topicList = new ArrayList<>();

        topicAdapter = new TopicAdapterBlue(this, topicList);
        recyclerView.setAdapter(topicAdapter);

        spinner = findViewById(R.id.spinner);
        categoryAdapter = new CategoryAdapter(this, R.layout.item_selected, getListCategory());
        spinner.setAdapter(categoryAdapter);

        loadAllTopics(currentUser);

        btnMenu.setOnClickListener(v -> {
            Dialog dialog = new Dialog(TopicActivity.this);
            dialog.setContentView(R.layout.activity_menu);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                        (int) (getResources().getDisplayMetrics().heightPixels * 0.9)
                );
                window.setGravity(Gravity.START);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.getAttributes().windowAnimations = R.style.DialogAnimation;

                TranslateAnimation slideIn = new TranslateAnimation(-window.getAttributes().width, 0, 0, 0);
                slideIn.setDuration(300);
                window.getDecorView().startAnimation(slideIn);
            }

            Button btnHome = dialog.findViewById(R.id.btnHome);
            btnHome.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, HomeActivity.class);
                startActivity(intent);
            });
            Button btnFlashcard = dialog.findViewById(R.id.btnFlashcard);
            btnFlashcard.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, FlashcardActivity.class);
                startActivity(intent);
            });
            Button btnTopics = dialog.findViewById(R.id.btnTopics);
            btnTopics.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, TopicActivity.class);
                startActivity(intent);
            });
            Button btnFolders = dialog.findViewById(R.id.btnFolders);
            btnFolders.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, FolderActivity.class);
                startActivity(intent);
            });
            Button btnChoice = dialog.findViewById(R.id.btnChoice);
            btnChoice.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, MultipleChoiceActivity.class);
                startActivity(intent);
            });
            Button btnProfile = dialog.findViewById(R.id.btnProfile);
            btnProfile.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
            Button btnFind = dialog.findViewById(R.id.btnFind);
            btnFind.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, FindAndFillActivity.class);
                startActivity(intent);
            });
            Button btnCommunity = dialog.findViewById(R.id.btnCommunity);
            btnCommunity.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, CommunityActivity.class);
                startActivity(intent);
            });
            Button btnLogout = dialog.findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, MainActivity.class);
                startActivity(intent);
            });
            Button btnSetting = dialog.findViewById(R.id.btnSetting);
            btnSetting.setOnClickListener(view -> {
                Intent intent = new Intent(TopicActivity.this, SettingActivity.class);
                startActivity(intent);
            });
            dialog.show();
            dialog.setOnDismissListener(dialogInterface -> {
                TranslateAnimation slideOut = new TranslateAnimation(0, window.getAttributes().width, 0, 0);
                slideOut.setDuration(300);
                window.getDecorView().startAnimation(slideOut);
            });
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                if (selectedCategory.getName().equals("Created")) {
                    sortTopicsByCreatedTime(); // Sắp xếp theo thời gian tạo
                } else if (selectedCategory.getName().equals("Visited")) {
                    sortTopicsByVisitedTime(); // Sắp xếp theo thời gian truy cập lần cuối
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì khi không có gì được chọn
            }
        });
    }

    private List<Category> getListCategory(){
        List<Category> list = new ArrayList<>();
        list.add(new Category("Created"));
        list.add(new Category("Visited"));
        return list;
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

        Button btnConfirmImport = dialogView.findViewById(R.id.btnCreate);
        Button btnCancelImport = dialogView.findViewById(R.id.btnCancel);

        btnConfirmImport.setOnClickListener(v -> {
            // Thêm logic khi nhấn nút Confirm Import
            importDialog.dismiss();
        });

        btnCancelImport.setOnClickListener(v -> importDialog.dismiss());

        importDialog.show();
    }

    private void loadAllTopics(String userId) {
        topicList.clear();

        db.collection("topics")
                .whereEqualTo("ownerId", userId) // Lọc tất cả topics theo ownerId
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("FirestoreError", "Error loading topics: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        topicList.clear(); // Làm rỗng danh sách để tránh trùng lặp
                        int count = 0;
                        int countDownload = 0;

                        for (QueryDocumentSnapshot topicDoc : queryDocumentSnapshots) {
                            Topic topic = topicDoc.toObject(Topic.class);
                            topicList.add(topic);
                            count++;

                            // Kiểm tra nếu progress của topic là 100
                            if (topic.getProgress() == 100) {
                                countDownload++;
                            }
                        }

                        tvCountTopic.setText(String.valueOf(count)); // Cập nhật số lượng topics
                        tvCountDownload.setText(String.valueOf(countDownload)); // Cập nhật số lượng topics hoàn thành
                        topicAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                    }
                });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadAllTopics(currentUser);
        }
    }


    private void sortTopicsByCreatedTime() {
        Collections.sort(topicList, (t1, t2) -> Long.compare(t2.getCreatedTime(), t1.getCreatedTime())); // Giảm dần
        topicAdapter.notifyDataSetChanged(); // Cập nhật giao diện
    }

    private void sortTopicsByVisitedTime() {
        Collections.sort(topicList, (t1, t2) -> Long.compare(t2.getLastVisitedTime(), t1.getLastVisitedTime())); // Giảm dần
        topicAdapter.notifyDataSetChanged(); // Cập nhật giao diện
    }


}