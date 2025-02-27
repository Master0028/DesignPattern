package com.example.vopet.adapter;

import static android.app.Activity.RESULT_OK;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.activity.FolderActivity;
import com.example.vopet.model.Folder;
import com.example.vopet.activity.FolderDetailActivity;
import com.example.vopet.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private final List<Folder> folderList;
    private String userId;
    private Context context;
    private final FirebaseFirestore db;
    public FolderAdapter(Context context, List<Folder> folderList, String userId) {
        this.context = context;
        this.userId = userId;
        this.folderList = folderList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_item, parent, false);

        return new FolderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);

        holder.topicNameTextView.setText(folder.getName());
        holder.numberOfTopic.setText(folder.getNumberOfTopic() + " topics");

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FolderDetailActivity.class);
            intent.putExtra("folderName", folder.getName());
            context.startActivity(intent);
        });

        holder.cardView.setOnLongClickListener(v -> {
            // Hiển thị hộp thoại xác nhận xóa
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Folder");
            builder.setMessage("Are you sure you want to delete this folder?");

            // Nút xác nhận xóa
            builder.setPositiveButton("Yes", (dialog, which) -> {
                deleteFolder(folder, position);
            });

            // Nút hủy
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            builder.show();
            return true; // Trả về true để sự kiện Long Click không kích hoạt thêm sự kiện Click khác
        });
    }

    private void deleteFolder(Folder folder, int position) {
        db.collection("folders")
                .whereEqualTo("creatorId", userId)
                .whereEqualTo("name", folder.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy ID của folder
                        String folderId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Xóa folder khỏi Firestore
                        db.collection("folders").document(folderId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Xóa thành công folder
                                    folderList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Folder deleted successfully", Toast.LENGTH_SHORT).show();

                                    // Xóa folderName khỏi các topics liên quan
                                    removeFolderNameFromTopics(folder.getName());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error deleting folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Folder not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error finding folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFolderNameFromTopics(String folderName) {
        // Lọc tất cả các topics có ownerId là userId và chứa folderName
        db.collection("topics")
                .whereEqualTo("ownerId", userId) // Lọc theo ownerId
                .whereArrayContains("folderName", folderName) // Lọc theo folderName
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot topicDoc : queryDocumentSnapshots) {
                        String topicId = topicDoc.getId();

                        // Cập nhật field folderName, loại bỏ folderName bị xóa
                        db.collection("topics").document(topicId)
                                .update("folderName", FieldValue.arrayRemove(folderName))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Removed folder name from topic: " + topicId, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error updating topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error finding topics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        public final TextView topicNameTextView;
        public final TextView numberOfTopic;
        public final CardView cardView;

        public FolderViewHolder(View itemView) {
            super(itemView);
            topicNameTextView = itemView.findViewById(R.id.topicName);
            numberOfTopic = itemView.findViewById(R.id.numberOfTopic);
            cardView = itemView.findViewById(R.id.cardViewButton);
        }
    }
}
