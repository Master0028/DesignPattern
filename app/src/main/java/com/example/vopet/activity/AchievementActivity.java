package com.example.vopet.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.adapter.AchievementAdapter;
import com.example.vopet.model.Achievement;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AchievementActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AchievementAdapter adapter;
    private List<Achievement> achievementList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_achievement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AchievementAdapter(this, achievementList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadAchievements();
    }

    private void loadAchievements() {
        String userId = SessionSingleton.getInstance().getUserId();

        db.collection("users").document(userId).collection("achievements")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    achievementList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Achievement achievement = doc.toObject(Achievement.class);
                        achievementList.add(achievement);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load achievements: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}