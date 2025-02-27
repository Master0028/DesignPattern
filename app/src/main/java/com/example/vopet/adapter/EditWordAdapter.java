package com.example.vopet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.model.SimpleTextWatcher;
import com.example.vopet.model.Vocabulary;
import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.FieldValue;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditWordAdapter extends RecyclerView.Adapter<EditWordAdapter.WordViewHolder> {
    private Context context;
    private List<Vocabulary> wordList;
    private String topicName;
    private OnImageClickListener imageClickListener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public EditWordAdapter(Context context, List<Vocabulary> wordList, String topicName, OnImageClickListener imageClickListener) {
        this.context = context;
        this.wordList = wordList;
        this.topicName = topicName;
        this.imageClickListener = imageClickListener;
    }

    public EditWordAdapter(Context context, List<Vocabulary> wordList, String topicName) {
        this.context = context;
        this.wordList = wordList;
        this.topicName = topicName;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_edit_word_item_each_topic, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Vocabulary word = wordList.get(position);
        holder.stt.setText(String.valueOf(word.getStt()));
        holder.wordEditText.setText(word.getEnglish());
        holder.meanEditText.setText(word.getMeaning());
        holder.pronounceEditText.setText(word.getPronounce());

        String photoBase64 = word.getPhoto();
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            try {
                // Giải mã Base64 thành Bitmap và hiển thị trong ImageButton
                byte[] decodedString = Base64.decode(photoBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgOfWord.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                // Nếu có lỗi khi giải mã Base64, hiển thị ảnh mặc định
                holder.imgOfWord.setImageResource(R.drawable.bg_topic);
            }
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định
            holder.imgOfWord.setImageResource(R.drawable.bg_topic);
        }
//        holder.imgOfWord.setImageResource(word.getImageResId());
        // Xử lý sự kiện xóa từ
        holder.deleteButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = SessionSingleton.getInstance().getUserId();

            // Truy cập tài liệu `topic` và subcollection `vocabularies`
            db.collection("topics")
                    .whereEqualTo("creatorId", userId) // Lọc theo userId
                    .whereEqualTo("name", topicName) // Lọc theo topicName
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String topicId = document.getId(); // Lấy ID của topic
                                document.getReference().collection("vocabularies")
                                        .whereEqualTo("english", word.getEnglish()) // Lọc theo từ cần xóa
                                        .get()
                                        .addOnSuccessListener(vocabSnapshots -> {
                                            for (QueryDocumentSnapshot vocabDoc : vocabSnapshots) {
                                                // Xóa từ vựng trong Firestore
                                                vocabDoc.getReference().delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Xóa từ vựng khỏi danh sách hiển thị
                                                            wordList.remove(position);
                                                            notifyItemRemoved(position);

                                                            // Cập nhật lại thứ tự trong Firestore
                                                            updateVocabularyOrder(document.getReference().collection("vocabularies"));

                                                            // Cập nhật lại numberOfWord của topic
                                                            updateNumberOfWord(topicId, 1);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Failed to find vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(context, "No topics found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to find topic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Lưu thông tin khi người dùng thay đổi nội dung
        holder.wordEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                word.setEnglish(s.toString());
            }
        });

        holder.meanEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                word.setMeaning(s.toString());
            }
        });

        holder.pronounceEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                word.setPronounce(s.toString());
            }
        });
        holder.imgOfWord.setOnClickListener(v -> {
            if (imageClickListener != null) {
                imageClickListener.onImageClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public void updateWordPhoto(int position, String encodedImage) {
        Vocabulary word = wordList.get(position);
        word.setPhoto(encodedImage);
        notifyItemChanged(position);
    }

    private void updateVocabularyOrder(CollectionReference vocabulariesRef) {
        vocabulariesRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                    int newOrder = 1; // Bắt đầu từ 1
                    for (DocumentSnapshot doc : documents) {
                        if (doc instanceof QueryDocumentSnapshot) { // Kiểm tra nếu doc là QueryDocumentSnapshot
                            batch.update(doc.getReference(), "stt", newOrder);
                            newOrder++;
                        }
                    }

                    // Commit batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Order updated successfully", Toast.LENGTH_SHORT).show();

                                // Làm mới giao diện RecyclerView
                                reloadVocabularyList(vocabulariesRef);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to update order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to retrieve vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void reloadVocabularyList(CollectionReference vocabulariesRef) {
        vocabulariesRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Vocabulary> updatedList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Vocabulary vocab = doc.toObject(Vocabulary.class);
                        vocab.setId(doc.getId());
                        updatedList.add(vocab);
                    }

                    // Cập nhật RecyclerView
                    wordList.clear();
                    wordList.addAll(updatedList);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to reload vocabularies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateNumberOfWord(String topicId, int decrementBy) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("topics").document(topicId)
                .update("numberOfWord", FieldValue.increment(-decrementBy))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Topic numberOfWord updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to update numberOfWord: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void updateData(List<Vocabulary> newWordList) {
        this.wordList.clear();
        this.wordList.addAll(newWordList);
        notifyDataSetChanged();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView stt;
        EditText wordEditText, meanEditText, pronounceEditText;
        ImageView imgOfWord;
        Button deleteButton;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            stt = itemView.findViewById(R.id.stt);
            imgOfWord = itemView.findViewById(R.id.imgOfWord);
            wordEditText = itemView.findViewById(R.id.word);
            meanEditText = itemView.findViewById(R.id.mean);
            pronounceEditText = itemView.findViewById(R.id.pronounce);
            deleteButton = itemView.findViewById(R.id.btnDel);
        }
    }
}
