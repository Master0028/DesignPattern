package com.example.vopet.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.model.Folder;
import com.example.vopet.MainActivity;
import com.example.vopet.R;
import com.example.vopet.adapter.FolderAdapter;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FolderAdapter folderAdapter;
    private List<Folder> folderList;
    private Button addFolder;
    private FirebaseFirestore db;
    private TextView tvCountFolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_folder);
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

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnAdd) {
                    Intent i = new Intent(FolderActivity.this, PublicTopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnHome) {
                    Intent i = new Intent(FolderActivity.this, HomeActivity.class);
                    startActivity(i);
                }
            }
        });
        btnTopics1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnTopics) {
                    Intent i = new Intent(FolderActivity.this, TopicActivity.class);
                    startActivity(i);
                }
            }
        });
        btnProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnProfile) {
                    Intent i = new Intent(FolderActivity.this, ProfileActivity.class);
                    startActivity(i);
                }
            }
        });
        btnCommunity1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCommunity) {
                    Intent i = new Intent(FolderActivity.this, CommunityActivity.class);
                    startActivity(i);
                }
            }
        });

        db = FirebaseFirestore.getInstance();

        Button btnMenu = findViewById(R.id.btnMenu);

        addFolder = findViewById(R.id.addFolder);
        addFolder.setOnClickListener(v -> showCreateFolderDialog());

        recyclerView = findViewById(R.id.recyclerView);
        tvCountFolder = findViewById(R.id.tvCountFolder);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        folderList = new ArrayList<>();
        folderAdapter = new FolderAdapter(this, folderList, SessionSingleton.getInstance().getUserId());
        recyclerView.setAdapter(folderAdapter);


        loadFoldersFromFirestore();

        btnMenu.setOnClickListener(v -> {
            Dialog dialog = new Dialog(FolderActivity.this);
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
                Intent intent = new Intent(FolderActivity.this, HomeActivity.class);
                startActivity(intent);
            });
            Button btnFlashcard = dialog.findViewById(R.id.btnFlashcard);
            btnFlashcard.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, FlashcardActivity.class);
                startActivity(intent);
            });
            Button btnTopics = dialog.findViewById(R.id.btnTopics);
            btnTopics.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, TopicActivity.class);
                startActivity(intent);
            });
            Button btnFolders = dialog.findViewById(R.id.btnFolders);
            btnFolders.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, FolderActivity.class);
                startActivity(intent);
            });
            Button btnChoice = dialog.findViewById(R.id.btnChoice);
            btnChoice.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, MultipleChoiceActivity.class);
                startActivity(intent);
            });
            Button btnProfile = dialog.findViewById(R.id.btnProfile);
            btnProfile.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
            Button btnFind = dialog.findViewById(R.id.btnFind);
            btnFind.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, FindAndFillActivity.class);
                startActivity(intent);
            });
            Button btnCommunity = dialog.findViewById(R.id.btnCommunity);
            btnCommunity.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, CommunityActivity.class);
                startActivity(intent);
            });
            Button btnLogout = dialog.findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, MainActivity.class);
                startActivity(intent);
            });
            Button btnSetting = dialog.findViewById(R.id.btnSetting);
            btnSetting.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, SettingActivity.class);
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

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_folder, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        EditText etFolderName = dialogView.findViewById(R.id.etFolderName);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnCreate.setOnClickListener(v -> {
            String folderName = etFolderName.getText().toString();
            createFolder(folderName);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void createFolder(String folderName) {
        if (!folderName.isEmpty()) {
            // Tạo đối tượng Folder mới
            String date = (new SimpleDateFormat("dd/mm/yyyy")).format(new Date());

            Map<String, Object> newFolder = new HashMap<>();
            newFolder.put("name", folderName);
            newFolder.put("creatorId", SessionSingleton.getInstance().getUserId());
            newFolder.put("numberOfTopic", 0);
            newFolder.put("progress", 0);
            newFolder.put("createdTime", date);
//            newTopic.put("lastVisitedTime", 0);

            // Lưu vào Firestore
            db.collection("folders")
                    .add(newFolder)
                    .addOnSuccessListener(documentReference -> {
                        // Thêm folder vào danh sách và cập nhật RecyclerView
                        Folder folder = new Folder(folderName, 0, SessionSingleton.getInstance().getUserId(), date); // Bạn có thể điều chỉnh số từ và tiến độ tùy ý
                        folderList.add(folder);
                        folderAdapter.notifyDataSetChanged();
                        loadFoldersFromFirestore();
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi
                        Toast.makeText(FolderActivity.this, "Error creating folder", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(FolderActivity.this, "Please enter folder name", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFoldersFromFirestore() {

        if (!SessionSingleton.getInstance().isLoggedIn()) {
            Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String creatorId = SessionSingleton.getInstance().getUserId();

        db.collection("folders")
                .whereEqualTo("creatorId", creatorId) // Lọc theo creatorId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        folderList.clear();
                        int folderCount = 0; // Biến đếm số lượng folder
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Folder folder = document.toObject(Folder.class);
                            folderList.add(folder);
                            folderCount++; // Tăng biến đếm
                        }
                        tvCountFolder.setText(String.valueOf(folderCount));
                        folderAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(FolderActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            boolean updated = data.getBooleanExtra("updated", false);
            if (updated) {
                // Cập nhật lại danh sách folder để hiển thị số lượng topic đã thay đổi
                loadFoldersFromFirestore();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoldersFromFirestore();
    }

}