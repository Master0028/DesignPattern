package com.example.vopet.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.adapter.CommunityAdapter;
import com.example.vopet.MainActivity;
import com.example.vopet.R;
import com.example.vopet.model.Community;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private List<Community> communityList;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_community);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btnHome1 = findViewById(R.id.btnHome);
        ImageButton btnTopics1 = findViewById(R.id.btnTopics);
        ImageButton btnProfile1 = findViewById(R.id.btnProfile);
        ImageButton btnCommunity1 = findViewById(R.id.btnCommunity);

        loadPublicTopicsWithAccess();

        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(CommunityActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(CommunityActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(CommunityActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(CommunityActivity.this, CommunityDetailActivity.class);
                    startActivity(i);
                }
            }
        });
        Button btnMenu = findViewById(R.id.btnMenu);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        communityList = new ArrayList<>();

        adapter = new CommunityAdapter(this, communityList);
        recyclerView.setAdapter(adapter);

        // Handle item click
        adapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < communityList.size()) {
                Community clickedItem = communityList.get(position);

                Intent intent = new Intent(CommunityActivity.this, CommunityDetailActivity.class);
                intent.putExtra("topicName", clickedItem.getTopicName());
                intent.putExtra("authName", clickedItem.getAuthName());
                intent.putExtra("numberOfWords", clickedItem.getNumberOfWord());
                intent.putExtra("ranking", clickedItem.getRanking());
                intent.putExtra("creatorId", clickedItem.getCreatorId());
                intent.putExtra("topicId", clickedItem.getTopicId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid item selected!", Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu.setOnClickListener(v -> {
            Dialog dialog = new Dialog(CommunityActivity.this);
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
                Intent intent = new Intent(CommunityActivity.this, HomeActivity.class);
                startActivity(intent);
            });
            Button btnFlashcard = dialog.findViewById(R.id.btnFlashcard);
            btnFlashcard.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, FlashcardActivity.class);
                startActivity(intent);
            });
            Button btnTopics = dialog.findViewById(R.id.btnTopics);
            btnTopics.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, TopicActivity.class);
                startActivity(intent);
            });
            Button btnFolders = dialog.findViewById(R.id.btnFolders);
            btnFolders.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, FolderActivity.class);
                startActivity(intent);
            });
            Button btnChoice = dialog.findViewById(R.id.btnChoice);
            btnChoice.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, MultipleChoiceActivity.class);
                startActivity(intent);
            });
            Button btnProfile = dialog.findViewById(R.id.btnProfile);
            btnProfile.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
            Button btnFind = dialog.findViewById(R.id.btnFind);
            btnFind.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, FindAndFillActivity.class);
                startActivity(intent);
            });
            Button btnCommunity = dialog.findViewById(R.id.btnCommunity);
            btnCommunity.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, CommunityActivity.class);
                startActivity(intent);
            });
            Button btnLogout = dialog.findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, MainActivity.class);
                startActivity(intent);
            });
            Button btnSetting = dialog.findViewById(R.id.btnSetting);
            btnSetting.setOnClickListener(view -> {
                Intent intent = new Intent(CommunityActivity.this, SettingActivity.class);
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

    private void loadPublicTopicsWithAccess() {
        db = FirebaseFirestore.getInstance();

        db.collection("topics")
                .whereEqualTo("permission", "Public")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String topicId = document.getId();
                            String topicName = document.getString("name");
                            String creatorId = document.getString("creatorId");
                            int numberOfWords = document.getLong("numberOfWord").intValue();
                            int access = document.getLong("access") != null ? document.getLong("access").intValue() : 0;

                            // Lấy tên người tạo từ `creatorId`
                            db.collection("users").document(creatorId)
                                    .get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        String creatorName = userSnapshot.getString("username");
                                        if (creatorName == null) creatorName = "Unknown";

                                        // Thêm vào danh sách
                                        communityList.add(new Community(topicName, creatorName, numberOfWords, access, creatorId, topicId));

                                        // Khi đã thêm hết các topics, sắp xếp danh sách
                                        if (communityList.size() == queryDocumentSnapshots.size()) {
                                            sortAndDisplayCommunities(communityList);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to fetch creator name for: " + creatorId, Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "No public topics found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sortAndDisplayCommunities(List<Community> loadedCommunityList) {
        // Sắp xếp theo số lượng người học (learnerCount) giảm dần
        loadedCommunityList.sort((c1, c2) -> Integer.compare(c2.getRanking(), c1.getRanking()));

        // Cập nhật RecyclerView
        adapter.updateData(loadedCommunityList); // Tạo một phương thức `updateData` trong Adapter
    }


}