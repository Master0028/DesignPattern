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
import com.example.vopet.activity.StudyTopicInMulChoiceActivity;
import com.example.vopet.activity.TopicDetailActivity;
import com.example.vopet.model.Topic;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TopicAdapterOrangeInMulChoice extends RecyclerView.Adapter<TopicAdapterOrangeInMulChoice.TopicViewHolder> {
    private List<Topic> topicList;
    private Context context;
    private String folderName;
    private FirebaseFirestore db;
    public TopicAdapterOrangeInMulChoice(Context context, List<Topic> topicList) {
        this.context = context;
        this.topicList = topicList;
    }

    public void setFolderName(String folderName) { this.folderName = folderName; }

    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_item4, parent, false);

        return new TopicViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TopicViewHolder holder, int position) {
        Topic topic = topicList.get(position);

        db = FirebaseFirestore.getInstance();

        holder.topicNameTextView.setText(topic.getName());
        holder.numberOfWordsTextView.setText(topic.getNumberOfWord() + " words");
        holder.progressBar.setProgress(topic.getProgress());
        holder.progressPercentage.setText(topic.getProgress() + "%");

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudyTopicInMulChoiceActivity.class);
            intent.putExtra("topicName", topic.getName());
            context.startActivity(intent);
        });

        holder.cardView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_topic, popupMenu.getMenu());

            // Hiển thị biểu tượng trong PopupMenu (nếu có)
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Topic");
                    builder.setMessage("Are you sure you want to delete this topic?");

                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("topics")
                                .whereEqualTo("name", topic.getName())
                                .whereEqualTo("creatorId", topic.getCreatorId())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        String topicId = queryDocumentSnapshots.getDocuments().get(0).getId();

                                        // Xóa folderName khỏi topic
                                        db.collection("topics").document(topicId)
                                                .update("folderName", FieldValue.arrayRemove(folderName))
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("DEBUG", "Folder name removed successfully from topic.");

                                                    // Cập nhật số lượng topics trong folder
                                                    db.collection("folders")
                                                            .whereEqualTo("name", folderName)
                                                            .get()
                                                            .addOnSuccessListener(folderQuerySnapshots -> {
                                                                if (!folderQuerySnapshots.isEmpty()) {
                                                                    String folderId = folderQuerySnapshots.getDocuments().get(0).getId();
                                                                    Long numberOfTopic = folderQuerySnapshots.getDocuments()
                                                                            .get(0)
                                                                            .getLong("numberOfTopic");

                                                                    if (numberOfTopic != null && numberOfTopic > 0) {
                                                                        db.collection("folders").document(folderId)
                                                                                .update("numberOfTopic", numberOfTopic - 1)
                                                                                .addOnSuccessListener(folderUpdate -> {
                                                                                    Log.d("DEBUG", "Folder numberOfTopic updated successfully.");
                                                                                })
                                                                                .addOnFailureListener(e -> {
                                                                                    Log.e("DEBUG", "Failed to update folder numberOfTopic: " + e.getMessage());
                                                                                });
                                                                    }
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("DEBUG", "Failed to fetch folder: " + e.getMessage());
                                                            });

                                                    // Cập nhật danh sách hiển thị
                                                    topicList.remove(position);
                                                    notifyItemRemoved(position);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("DEBUG", "Failed to remove folder name from topic: " + e.getMessage());
                                                });
                                    } else {
                                        Toast.makeText(context, "Topic not found!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to fetch topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });

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


    private void exportVocabularyToCsv(Topic topic) {
        db.collection("topics")
                .whereEqualTo("name", topic.getName())
                .whereEqualTo("creatorId", topic.getCreatorId())
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
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "text/csv");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {
        public TextView topicNameTextView;
        public TextView numberOfWordsTextView;
        public ProgressBar progressBar;
        public TextView progressPercentage;
        public CardView cardView;

        public TopicViewHolder(View itemView) {
            super(itemView);
            topicNameTextView = itemView.findViewById(R.id.topicName);
            numberOfWordsTextView = itemView.findViewById(R.id.numberOfWords);
            progressBar = itemView.findViewById(R.id.circularProgressBar);
            progressPercentage = itemView.findViewById(R.id.progressBar);
            cardView = itemView.findViewById(R.id.cardViewButton);
        }
    }
}
