package com.example.vopet.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
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

import com.example.vopet.MainActivity;
import com.example.vopet.R;
import com.example.vopet.adapter.TopicAdapterGreen;
import com.example.vopet.adapter.TopicAdapterYellow;
import com.example.vopet.model.Topic;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView, recyclerView1;
    private TopicAdapterYellow topicAdapter;
    private TopicAdapterGreen topicAdapter1;
    private List<Topic> topicList, topicList1;
    private FirebaseFirestore db;
    String userId;
    TextView userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        Button btnMenu = findViewById(R.id.btnMenu);
        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        userName = findViewById(R.id.userName);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView1 = findViewById(R.id.recyclerView1);
        loadUserProfile();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView1.setLayoutManager(layoutManager1);

        topicList = new ArrayList<>();

        topicList1 = new ArrayList<>();

        topicAdapter = new TopicAdapterYellow(topicList);
        recyclerView.setAdapter(topicAdapter);
        topicAdapter1 = new TopicAdapterGreen(topicList1);
        recyclerView1.setAdapter(topicAdapter1);
//        loadRecentTopics();
        loadAllUserTopics();
        loadPublicTopics();

        androidx.cardview.widget.CardView cardFolders = findViewById(R.id.cardFolders);
        androidx.cardview.widget.CardView cardTopics = findViewById(R.id.cardTopics);
        androidx.cardview.widget.CardView cardCommunity = findViewById(R.id.cardCommunity);
        androidx.cardview.widget.CardView cardMulChoice = findViewById(R.id.cardMulChoice);
        androidx.cardview.widget.CardView cardFlashcard = findViewById(R.id.cardFlashcard);
        androidx.cardview.widget.CardView cardFindAndFill = findViewById(R.id.cardFindAndFill);

        cardFolders.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FolderActivity.class);
            startActivity(intent);
        });

        cardTopics.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TopicActivity.class);
            startActivity(intent);
        });

        cardCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        cardMulChoice.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MultipleChoiceActivity.class);
            startActivity(intent);
        });

        cardFlashcard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FlashcardActivity.class);
            startActivity(intent);
        });

        cardFindAndFill.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FindAndFillActivity.class);
            startActivity(intent);
        });

        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(HomeActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(HomeActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(HomeActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(HomeActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });
        btnMenu.setOnClickListener(v -> {
            Dialog dialog = new Dialog(HomeActivity.this);
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
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
            });
            Button btnFlashcard = dialog.findViewById(R.id.btnFlashcard);
            btnFlashcard.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, FlashcardActivity.class);
                startActivity(intent);
            });
            Button btnTopics = dialog.findViewById(R.id.btnTopics);
            btnTopics.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, TopicActivity.class);
                startActivity(intent);
            });
            Button btnFolders = dialog.findViewById(R.id.btnFolders);
            btnFolders.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, FolderActivity.class);
                startActivity(intent);
            });
            Button btnChoice = dialog.findViewById(R.id.btnChoice);
            btnChoice.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, MultipleChoiceActivity.class);
                startActivity(intent);
            });
            Button btnProfile = dialog.findViewById(R.id.btnProfile);
            btnProfile.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
            Button btnFind = dialog.findViewById(R.id.btnFind);
            btnFind.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, FindAndFillActivity.class);
                startActivity(intent);
            });
            Button btnCommunity = dialog.findViewById(R.id.btnCommunity);
            btnCommunity.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, CommunityActivity.class);
                startActivity(intent);
            });
            Button btnLogout = dialog.findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            });
            Button btnSetting = dialog.findViewById(R.id.btnSetting);
            btnSetting.setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
                startActivity(intent);
            });
            dialog.show();
            dialog.setOnDismissListener(dialogInterface -> {
                TranslateAnimation slideOut = new TranslateAnimation(0, window.getAttributes().width, 0, 0);
                slideOut.setDuration(300);
                window.getDecorView().startAnimation(slideOut);
            });
        });
    }
    private void loadRecentTopics() {
        userId = SessionSingleton.getInstance().getUserId();

        db.collection("topics")
                .whereEqualTo("ownerId", userId) // Lọc các chủ đề do người dùng hiện tại tạo
                .orderBy("lastVisitedTime", Query.Direction.DESCENDING) // Sắp xếp theo thời gian truy cập gần đây
                .limit(10) // Giới hạn số lượng chủ đề
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        topicList.clear();
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Topic topic = document.toObject(Topic.class);
                                if (topic != null) {
                                    topicList.add(topic);
                                }
                            }
                        }

                        // Nếu không có chủ đề truy cập gần đây, tải tất cả chủ đề của người dùng
                        if (topicList.isEmpty()) {
                            loadAllUserTopics();
                        } else {
                            topicAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("loadRecentTopics", "Error getting recent topics", task.getException());
                    }
                });
    }
    private void loadAllUserTopics() {
        db.collection("topics")
                .whereEqualTo("ownerId", userId) // Lọc các chủ đề do người dùng hiện tại tạo
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        topicList.clear();
                        if (task.getResult() != null) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Topic topic = document.toObject(Topic.class);
                                if (topic != null) {
                                    topicList.add(topic);
                                }
                            }
                        }
                        topicAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("loadAllUserTopics", "Error getting all user topics", task.getException());
                    }
                });
    }
    // Phương thức để tải tất cả các chủ đề công khai
    private void loadPublicTopics() {
        db.collection("topics")
                .whereEqualTo("permission", "Public") // Chỉ lấy những topics công khai
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        topicList1.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Topic topic = document.toObject(Topic.class);
                            topicList1.add(topic);
                        }
                        topicAdapter1.notifyDataSetChanged();
                    } else {
                        Log.e("loadPublicTopics", "Error getting public topics", task.getException());
                    }
                });
    }
    private void loadUserProfile() {
        // Lấy thông tin người dùng hiện tại từ FirebaseAuth
        userId = SessionSingleton.getInstance().getUserId();

        // Tham chiếu đến document của người dùng trong Firestore
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Lấy tên người dùng từ document
                            String name = document.getString("username");

                            // Hiển thị tên trên TextView
                            userName.setText("Hi, " + name);

                        } else {
                            Toast.makeText(this, "User profile does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("loadUserProfile", "Error getting user profile", task.getException());
                        Toast.makeText(this, "Failed to load user profile.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}