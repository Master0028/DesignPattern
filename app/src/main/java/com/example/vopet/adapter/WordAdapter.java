package com.example.vopet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vopet.R;
import com.example.vopet.model.Vocabulary;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private Context context;
    private List<Vocabulary> wordList;
    private TextToSpeech textToSpeech;

    public WordAdapter(Context context, List<Vocabulary> wordList, TextToSpeech textToSpeech) {
        this.context = context;
        this.wordList = wordList;
        this.textToSpeech = textToSpeech;
    }

    public void setTextToSpeech(TextToSpeech textToSpeech) {
        this.textToSpeech = textToSpeech;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_word_item_each_topic, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Vocabulary word = wordList.get(position);
        if (holder.stt != null) {
            holder.stt.setText(String.valueOf(word.getStt()));
        }
        if (holder.word != null) {
            holder.word.setText(word.getEnglish());
        }
        if (holder.meaning != null) {
            holder.meaning.setText(word.getMeaning());
        }
        if (holder.pronounce != null) {
            holder.pronounce.setText(word.getPronounce());
        }

        String photoBase64 = word.getPhoto();
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            try {
                // Giải mã chuỗi Base64 thành Bitmap
                byte[] decodedString = Base64.decode(photoBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgOfWord.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                // Nếu chuỗi Base64 không hợp lệ, hiển thị ảnh mặc định
                holder.imgOfWord.setImageResource(R.drawable.bg_topic);
            }
        } else {
            // Nếu không có chuỗi ảnh, hiển thị ảnh mặc định
            holder.imgOfWord.setImageResource(R.drawable.bg_topic);
        }

        holder.btnSound.setOnClickListener(v -> {
            if (textToSpeech != null) {
                String pronounce = word.getEnglish();
                if (pronounce != null && !pronounce.isEmpty()) {
                    textToSpeech.speak(pronounce, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    Toast.makeText(context, "Pronounce not available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Text-to-Speech not initialized", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public void updateData(List<Vocabulary> newWordList) {
        this.wordList.clear();
        this.wordList.addAll(newWordList);
        notifyDataSetChanged();
    }

    public void release() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }


    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView stt, word, meaning, pronounce;
        ImageView imgOfWord;
        Button btnSound;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            stt = itemView.findViewById(R.id.stt);
            word = itemView.findViewById(R.id.word);
            imgOfWord = itemView.findViewById(R.id.imgOfWord);
            meaning = itemView.findViewById(R.id.mean);
            pronounce = itemView.findViewById(R.id.pronounce);
            btnSound = itemView.findViewById(R.id.btnSound);
        }
    }
}
