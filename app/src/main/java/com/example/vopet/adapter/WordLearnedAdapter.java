package com.example.vopet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.model.WordLearned;

import java.util.List;

public class WordLearnedAdapter extends RecyclerView.Adapter<WordLearnedAdapter.WordViewHolder> {
    private List<WordLearned> wordLearnedList;

    public WordLearnedAdapter(List<WordLearned> wordLearnedList) {
        this.wordLearnedList = wordLearnedList;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.word_item_in_find_and_fill, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        WordLearned wordLearned = wordLearnedList.get(position);
        holder.sttTextView.setText(String.valueOf(wordLearned.getOrder()));
        holder.wordTextView.setText(wordLearned.getWord());
        holder.userAnswerTextView.setText(wordLearned.getUserAnswer());
        holder.correctAnswerTextView.setText(wordLearned.getCorrectAnswer());
        if (wordLearned.getUserAnswer().equalsIgnoreCase(wordLearned.getCorrectAnswer())) {
            // Set correct border and add check icon if the answer is correct
//            holder.itemView.setBackgroundResource(R.drawable.correct_border);
            holder.correctAnswerTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_check_25, 0);
        } else {
            // Set default border if the answer is incorrect
//            holder.itemView.setBackgroundResource(R.drawable.default_border);
//            holder.correctAnswerTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return wordLearnedList.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView sttTextView, wordTextView, userAnswerTextView, correctAnswerTextView;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            sttTextView = itemView.findViewById(R.id.stt);
            wordTextView = itemView.findViewById(R.id.wordLearned);
            userAnswerTextView = itemView.findViewById(R.id.answerOfUser);
            correctAnswerTextView = itemView.findViewById(R.id.correctAnswer);
        }
    }
}
