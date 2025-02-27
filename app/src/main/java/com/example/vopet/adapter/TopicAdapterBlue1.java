package com.example.vopet.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.activity.ResultOfStudyByMulChoiceActivity;
import com.example.vopet.activity.StudyTopicInMulChoiceActivity;
import com.example.vopet.activity.TopicActivity;
import com.example.vopet.activity.TopicDetailActivity;
import com.example.vopet.model.Topic;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;

public class TopicAdapterBlue1 extends RecyclerView.Adapter<TopicAdapterBlue1.TopicViewHolder> {
    private List<Topic> topicList;
    private Context context;
    private FirebaseFirestore db;
    public TopicAdapterBlue1(Context context, List<Topic> topicList) {
        this.context = context;
        this.topicList = topicList;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.public_topic_item, parent, false);

        return new TopicViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TopicViewHolder holder, int position) {
        Topic topic = topicList.get(position);

        holder.topicNameTextView.setText(topic.getName());
        holder.numberOfWordsTextView.setText(topic.getNumberOfWord() + " words");

        db.collection("users").document(topic.getCreatorId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String creatorName = documentSnapshot.getString("username");
                        holder.creatorNameTextView.setText("Creator: " + creatorName);
                    } else {
                        holder.creatorNameTextView.setText("Creator: Unknown");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to load creator name", Toast.LENGTH_SHORT).show();
                    holder.creatorNameTextView.setText("Creator: Unknown");
                });

        holder.cardView.setOnClickListener(v -> {
            String currentUserId = SessionSingleton.getInstance().getUserId(); // Lấy ID người dùng hiện tại

            if (topic.getCreatorId().equals(currentUserId)) {
                // Nếu topic thuộc về người dùng hiện tại
                Toast.makeText(context, "This topic is already yours and cannot be downloaded!", Toast.LENGTH_SHORT).show();
            } else {
                // Hiển thị dialog tải xuống nếu topic không phải của người dùng
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Download Topic");
                builder.setMessage("Do you want to save this topic?");
                builder.setPositiveButton("Yes", (dialog, which) -> saveTopicWithVocabularies(topic));
                builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                builder.show();
            }
        });


    }
    @Override
    public int getItemCount() {
        return topicList.size();
    }

    private void saveTopicWithVocabularies(Topic topic) {
        String currentUserId = SessionSingleton.getInstance().getUserId();

        // Tạo một bản sao mới của Topic
        Topic newTopic = new Topic(
                topic.getFolderName(),
                topic.getCreatorId(),  // Gán ownerId là người dùng hiện tại
                topic.getName(),
                topic.getNumberOfWord(),
                0,  // Progress ban đầu
                System.currentTimeMillis(),  // createdTime
                System.currentTimeMillis()   // lastVisitedTime
        );
        newTopic.setPermission("Private"); // Gán permission là Private
        newTopic.setOwnerId(currentUserId);

        // Lưu topic mới vào Firestore
        db.collection("topics")
                .add(newTopic)
                .addOnSuccessListener(topicRef -> {
                    // Truy xuất và lưu subcollection `vocabularies`
                    db.collection("topics").document(topic.getId()).collection("vocabularies")
                            .get()
                            .addOnSuccessListener(vocabSnapshots -> {
                                for (QueryDocumentSnapshot vocabDoc : vocabSnapshots) {
                                    Map<String, Object> vocabData = vocabDoc.getData();
                                    vocabData.put("status", "Chưa học"); // Gán status = "Chưa học"
                                    vocabData.put("numberOfStudy", 0);  // Gán numberOfStudy = 0

                                    // Lưu từ vựng vào topic mới
                                    topicRef.collection("vocabularies").add(vocabData)
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Failed to save vocabulary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                                Toast.makeText(context, "Topic and vocabularies saved successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, TopicActivity.class);
                                context.startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to load vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to save topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
