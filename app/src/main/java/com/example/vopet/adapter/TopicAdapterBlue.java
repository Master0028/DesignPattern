package com.example.vopet.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.activity.StudyTopicInFindAndFillActivity;
import com.example.vopet.model.Topic;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.vopet.activity.TopicDetailActivity;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TopicAdapterBlue extends RecyclerView.Adapter<TopicAdapterBlue.TopicViewHolder> {
    private List<Topic> topicList;
    private Context context;
    private FirebaseFirestore db;
    public TopicAdapterBlue(Context context, List<Topic> topicList) {
        this.context = context;
        this.topicList = topicList;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_item3, parent, false);

        return new TopicViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TopicViewHolder holder, int position) {
        Topic topic = topicList.get(position);

        holder.topicNameTextView.setText(topic.getName());
        holder.numberOfWordsTextView.setText(topic.getNumberOfWord() + " words");

        String currentUserId = SessionSingleton.getInstance().getUserId();
        if (!topic.getCreatorId().equals(currentUserId)) {
            // Nếu creatorId không phải của người dùng hiện tại, hiển thị tên creator
            db.collection("users").document(topic.getCreatorId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String creatorName = documentSnapshot.getString("username");
                            holder.creatorNameTextView.setText("Creator: " + creatorName);
                            holder.creatorNameTextView.setVisibility(View.VISIBLE); // Hiển thị
                        } else {
                            holder.creatorNameTextView.setText("Creator: Unknown");
                            holder.creatorNameTextView.setVisibility(View.VISIBLE); // Hiển thị
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to load creator name", Toast.LENGTH_SHORT).show();
                        holder.creatorNameTextView.setText("Creator: Unknown");
                        holder.creatorNameTextView.setVisibility(View.VISIBLE); // Hiển thị
                    });
        } else {
            // Nếu là creator của topic, ẩn tên creator
            holder.creatorNameTextView.setVisibility(View.GONE);
        }
        // Add click listener for card view
        holder.cardView.setOnClickListener(v -> {
            db.collection("topics")
                    .whereEqualTo("name", topic.getName()) // Điều kiện theo topicName
                    .whereEqualTo("creatorId", topic.getCreatorId()) // Điều kiện theo creatorId
                    .whereEqualTo("ownerId", topic.getOwnerId()) // Điều kiện theo ownerId
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            String ownerId = queryDocumentSnapshots.getDocuments().get(0).getString("ownerId");
                            boolean edit = ownerId != null && ownerId.equals(topic.getCreatorId());

                            // Gọi hàm để cập nhật thời gian truy cập cuối
                            updateLastVisitedTime(topicId, position);

                            // Truyền dữ liệu qua Intent
                            Intent intent = new Intent(context, TopicDetailActivity.class);
                            intent.putExtra("topicName", topic.getName());
                            intent.putExtra("edit", edit);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Topic not found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to query topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        holder.cardView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_topic, popupMenu.getMenu());
            try {
                Field popup = PopupMenu.class.getDeclaredField("mPopup");
                popup.setAccessible(true);
                Object menuPopupHelper = popup.get(popupMenu);
                Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuPopupHelper, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_delete) {
                    // Hiển thị hộp thoại xác nhận xóa
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Topic");
                    builder.setMessage("Are you sure you want to delete this topic?");

                    // Nút xác nhận xóa
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        deleteTopic(topic, position);
                    });

                    // Nút hủy
                    builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                    builder.show();
                } else if (itemId == R.id.action_export) {
                    exportVocabularyToCsv(topic);
                }
                return true;
            });

            popupMenu.show();
            return true; // Trả về true để sự kiện Long Click không kích hoạt thêm sự kiện Click khác
        });
    }

    private void updateLastVisitedTime(String topicId, int position) {
        long currentTime = System.currentTimeMillis();
        db.collection("topics").document(topicId)
                .update("lastVisitedTime", currentTime)
                .addOnSuccessListener(aVoid -> {
                    topicList.get(position).setLastVisitedTime(currentTime); // Cập nhật dữ liệu trong danh sách
                    notifyItemChanged(position); // Cập nhật giao diện nếu cần
                    Log.d("UpdateLastVisited", "lastVisitedTime updated for topic: " + topicId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update lastVisitedTime: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void exportVocabularyToCsv(Topic topic) {
        db.collection("topics")
                .whereEqualTo("name", topic.getName())
                .whereEqualTo("ownerId", topic.getOwnerId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Lấy danh sách từ vựng trong subcollection
                        db.collection("topics").document(topicId).collection("vocabularies")
                                .get()
                                .addOnSuccessListener(vocabularySnapshots -> {
                                    if (!vocabularySnapshots.isEmpty()) {
                                        StringBuilder csvContent = new StringBuilder();
                                        csvContent.append("Word,Meaning,Pronunciation,Status,Priority\n");

                                        for (QueryDocumentSnapshot doc : vocabularySnapshots) {
                                            String word = doc.getString("english");
                                            String meaning = doc.getString("meaning");
                                            String pronunciation = doc.getString("pronounce");
                                            String status = doc.getString("status");
                                            boolean priority = doc.getBoolean("priority") != null && doc.getBoolean("priority");

                                            csvContent.append(String.format("%s,%s,%s,%s,%s\n",
                                                    word, meaning, pronunciation, status, priority ? "Yes" : "No"));
                                        }

                                        saveCsvToFile(topic.getName(), csvContent.toString());
                                    } else {
                                        Toast.makeText(context, "No vocabularies found in this topic!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to fetch vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to fetch topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveCsvToFile(String topicName, String csvContent) {
        try {
            String fileName = topicName + ".csv";
            File file = new File(context.getExternalFilesDir(null), fileName);

            FileWriter writer = new FileWriter(file);
            writer.append(csvContent);
            writer.flush();
            writer.close();

            Toast.makeText(context, "CSV file saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Mở file để xem nếu cần
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.fromFile(file), "text/csv");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteTopic(Topic topic, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Truy vấn tài liệu với điều kiện topicName và ownerId
        db.collection("topics")
                .whereEqualTo("name", topic.getName()) // Tìm theo tên topic
                .whereEqualTo("ownerId", topic.getOwnerId()) // Tìm theo ownerId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy topic ID của tài liệu đầu tiên tìm thấy
                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Xóa tất cả vocabularies trong subcollection trước
                        db.collection("topics").document(topicId).collection("vocabularies")
                                .get()
                                .addOnSuccessListener(vocabularySnapshots -> {
                                    for (QueryDocumentSnapshot vocabDoc : vocabularySnapshots) {
                                        // Xóa từng tài liệu trong subcollection
                                        db.collection("topics").document(topicId).collection("vocabularies")
                                                .document(vocabDoc.getId())
                                                .delete();
                                    }

                                    // Sau khi xóa subcollection, xóa tài liệu chính
                                    db.collection("topics").document(topicId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Xóa thành công, cập nhật lại danh sách và RecyclerView
                                                topicList.remove(position);
                                                notifyItemRemoved(position);
                                                Toast.makeText(context, "Topic deleted successfully", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Error deleting topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error deleting vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Topic not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error finding topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {
        public TextView topicNameTextView;
        public TextView numberOfWordsTextView;
        public TextView creatorNameTextView;
        public CardView cardView;

        public TopicViewHolder(View itemView) {
            super(itemView);
            topicNameTextView = itemView.findViewById(R.id.topicName);
            numberOfWordsTextView = itemView.findViewById(R.id.numberOfWords);
            creatorNameTextView = itemView.findViewById(R.id.authName);
            cardView = itemView.findViewById(R.id.cardViewButton);
        }
    }
}
