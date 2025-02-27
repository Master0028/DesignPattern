package com.example.vopet.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vopet.R;
import com.example.vopet.adapter.RankingAdapter;
import com.example.vopet.model.RankingItem; // Tạo một class model cho từng mục xếp hạng
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommunityDetailActivity extends AppCompatActivity {
    private String topicName, authName, creatorId, topicId;
    private int numberOfWords, ranking;
    private List<RankingItem> rankingList;
    private RecyclerView recyclerView;
    private RankingAdapter adapter; // Adapter riêng cho danh sách xếp hạng
    private FirebaseFirestore db;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_community_detail);

        findViewById(R.id.top1Layout).setVisibility(View.GONE);
        findViewById(R.id.top2Layout).setVisibility(View.GONE);
        findViewById(R.id.top3Layout).setVisibility(View.GONE);
        btnBack = findViewById(R.id.btn_back);
        // Nhận dữ liệu từ Intent
        topicName = getIntent().getStringExtra("topicName");
        authName = getIntent().getStringExtra("authName");
        numberOfWords = getIntent().getIntExtra("numberOfWords", 0);
        ranking = getIntent().getIntExtra("ranking", 0);
        creatorId = getIntent().getStringExtra("creatorId");
        topicId = getIntent().getStringExtra("topicId");

        // Ánh xạ RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rankingList = new ArrayList<>();
        adapter = new RankingAdapter(this, rankingList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        btnBack.setOnClickListener(v -> {
            finish(); // Go back to the previous activity
        });
        // Tải dữ liệu bảng xếp hạng
        loadRankingList();
    }

    private void loadRankingList() {
        db.collection("users")
                .get()
                .addOnSuccessListener(userSnapshots -> {
                    Map<String, Integer> userMaxScores = new HashMap<>();
                    int totalUsers = userSnapshots.size(); // Tổng số user
                    int[] processedUsers = {0}; // Đếm số user đã xử lý

                    for (QueryDocumentSnapshot userDoc : userSnapshots) {
                        String userId = userDoc.getId();

                        db.collection("users").document(userId).collection("history")
                                .whereEqualTo("creatorId", creatorId)
                                .whereEqualTo("topicName", topicName)
                                .get()
                                .addOnSuccessListener(historySnapshots -> {
                                    int maxScore = 0;

                                    for (QueryDocumentSnapshot historyDoc : historySnapshots) {
                                        int score = historyDoc.getLong("score").intValue();
                                        maxScore = Math.max(maxScore, score);
                                    }

                                    if (maxScore > 0) {
                                        userMaxScores.put(userId, maxScore);
                                    }

                                    // Kiểm tra nếu đã xử lý tất cả user
                                    processedUsers[0]++;
                                    if (processedUsers[0] == totalUsers) {
                                        // Chỉ gọi displayRanking() sau khi xử lý xong
                                        displayRanking(userMaxScores);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedUsers[0]++;
                                    if (processedUsers[0] == totalUsers) {
                                        displayRanking(userMaxScores);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                });
    }


    private void displayRanking(Map<String, Integer> userMaxScores) {
        Log.d("DisplayRanking", "Total unique users: " + userMaxScores.size());
        rankingList.clear();

        // Chuyển Map thành List và sắp xếp giảm dần theo điểm số
        List<Map.Entry<String, Integer>> sortedScores = new ArrayList<>(userMaxScores.entrySet());
        sortedScores.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue())); // Sắp xếp giảm dần

        // Giới hạn danh sách chỉ còn 50 người có điểm cao nhất
        if (sortedScores.size() > 50) {
            sortedScores = sortedScores.subList(0, 50);
        }

        // Danh sách tạm thời để lưu kết quả sau khi lấy dữ liệu chi tiết
        List<RankingItem> allUsers = new ArrayList<>();
        List<RankingItem> topUsers = new ArrayList<>();
        List<RankingItem> remainingUsers = new ArrayList<>();

        int totalUsers = sortedScores.size();
        int[] processedUsers = {0};

        for (int k = 0; k < sortedScores.size(); k++) {
            Map.Entry<String, Integer> entry = sortedScores.get(k);
            String userId = entry.getKey();
            int score = entry.getValue();

            String rank = "Top " + (k + 1);

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String username = userDoc.getString("username");
                        if (username == null) username = "Unknown";

                        // Thêm vào danh sách tạm
                        RankingItem rankingItem = new RankingItem(userId, username, score);
                        allUsers.add(rankingItem);

                        saveAchievement(userId, rank, topicName, authName, score);

                        processedUsers[0]++;
                        if (processedUsers[0] == totalUsers) {
                            // Sắp xếp lại danh sách allUsers nếu cần (ở đây có thể bỏ qua vì đã sắp xếp trước đó)
                            allUsers.sort((u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));

                            // Phân loại top 3 và còn lại
                            for (int i = 0; i < allUsers.size(); i++) {
                                if (i < 3) {
                                    topUsers.add(allUsers.get(i));
                                } else {
                                    remainingUsers.add(allUsers.get(i));
                                    rankingList.add(allUsers.get(i));
                                }
                            }

                            // Cập nhật giao diện
                            updateTopUsersUI(topUsers);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        processedUsers[0]++;
                        if (processedUsers[0] == totalUsers) {
                            // Xử lý thất bại cũng cần sắp xếp lại danh sách
                            allUsers.sort((u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));

                            for (int i = 0; i < allUsers.size(); i++) {
                                if (i < 3) {
                                    topUsers.add(allUsers.get(i));
                                } else {
                                    remainingUsers.add(allUsers.get(i));
                                    rankingList.add(allUsers.get(i));
                                }
                            }

                            updateTopUsersUI(topUsers);
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }



    private void updateTopUsersUI(List<RankingItem> topUsers) {
        for (int i = 0; i < topUsers.size(); i++) {
            RankingItem user = topUsers.get(i);

            switch (i) {
                case 0: // Người đầu tiên
                    findViewById(R.id.top1Layout).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.name1)).setText(user.getUsername());
                    ((TextView) findViewById(R.id.score1)).setText(String.valueOf(user.getScore()));

                    CircleImageView imgOfUser1 = findViewById(R.id.imgOfUser1);
                    loadImageForUser(user.getUserId(), imgOfUser1);
                    break;

                case 1: // Người thứ hai
                    findViewById(R.id.top2Layout).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.name2)).setText(user.getUsername());
                    ((TextView) findViewById(R.id.score2)).setText(String.valueOf(user.getScore()));

                    CircleImageView imgOfUser2 = findViewById(R.id.imgOfUser2);
                    loadImageForUser(user.getUserId(), imgOfUser2);
                    break;

                case 2: // Người thứ ba
                    findViewById(R.id.top3Layout).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.name3)).setText(user.getUsername());
                    ((TextView) findViewById(R.id.score3)).setText(String.valueOf(user.getScore()));

                    CircleImageView imgOfUser3 = findViewById(R.id.imgOfUser3);
                    loadImageForUser(user.getUserId(), imgOfUser3);
                    break;
            }
        }
    }

    private void loadImageForUser(String userId, CircleImageView imageView) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String imageUrl = documentSnapshot.getString("profileImageBase64");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        try {
                            byte[] decodedString = Base64.decode(imageUrl, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            imageView.setImageBitmap(decodedBitmap);
                        } catch (IllegalArgumentException e) {
                            imageView.setImageResource(R.drawable.bg_topic);
                        }
                    } else {
                        imageView.setImageResource(R.drawable.bg_topic);
                    }
                })
                .addOnFailureListener(e -> imageView.setImageResource(R.drawable.bg_topic));
    }

    private void saveAchievement(String userId, String rank, String topicName, String creatorName, int score) {
        db.collection("users").document(userId)
                .collection("achievements")
                .whereEqualTo("topicName", topicName)
                .whereEqualTo("rank", rank)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Nếu chưa có danh hiệu tương tự, thêm mới
                        Map<String, Object> achievement = new HashMap<>();
                        achievement.put("rank", rank);
                        achievement.put("topicName", topicName);
                        achievement.put("creatorName", creatorName);
                        achievement.put("score", score);
                        achievement.put("timestamp", System.currentTimeMillis());

                        db.collection("users").document(userId)
                                .collection("achievements")
                                .add(achievement)
                                .addOnSuccessListener(documentReference -> Log.d("Achievements", "Added achievement: " + rank + " for user " + userId))
                                .addOnFailureListener(e -> Log.e("Achievements", "Failed to add achievement: " + e.getMessage()));
                    } else {
                        Log.d("Achievements", "Achievement already exists for user: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Achievements", "Failed to check existing achievements: " + e.getMessage()));
    }
}
