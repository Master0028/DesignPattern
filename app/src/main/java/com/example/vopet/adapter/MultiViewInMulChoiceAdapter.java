package com.example.vopet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.model.HistoryStudy;
import com.example.vopet.R;

import java.util.List;

public class MultiViewInMulChoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FIRST_TIME = 0;
    private static final int VIEW_TYPE_SECOND_TIME = 1;
    private List<HistoryStudy> itemList;
    private int totalQuestion;

    public MultiViewInMulChoiceAdapter(List<HistoryStudy> itemList) {
        this.itemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return VIEW_TYPE_FIRST_TIME;
        } else {
            return VIEW_TYPE_SECOND_TIME;
        }
    }

    public void setTotalQuestion(int totalQuestion) {
        this.totalQuestion = totalQuestion;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FIRST_TIME) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_study_item_in_mulchoice1, parent, false);
            return new FirstTimeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_study_item_in_mulchoice2, parent, false);
            return new SecondTimeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryStudy item = itemList.get(position);
        if (holder instanceof FirstTimeViewHolder) {
            ((FirstTimeViewHolder) holder).bind(item);
        } else if (holder instanceof SecondTimeViewHolder) {
            ((SecondTimeViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class FirstTimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvCompletedWords, tvMemoriedWords;
        ProgressBar progressCompletedWords, progressMemoriedWords;

        FirstTimeViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCompletedWords = itemView.findViewById(R.id.tvCompletedWords);
            progressCompletedWords = itemView.findViewById(R.id.progressCompletedWords);
            tvMemoriedWords = itemView.findViewById(R.id.tvMemoriedWords);
            progressMemoriedWords = itemView.findViewById(R.id.progressMemoriedWords);


        }

        void bind(HistoryStudy item) {
            tvTime.setText(item.getTimes());
            tvCompletedWords.setText(item.getCompletedWords() + "/" + totalQuestion);
            progressCompletedWords.setProgress(item.getCompletedProgress());
            tvMemoriedWords.setText(item.getMemoriedWords() + "/" + totalQuestion);
            progressMemoriedWords.setProgress(item.getMemoriedProgress());
        }
    }

    class SecondTimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvCompletedWords, tvMemoriedWords;
        ProgressBar progressCompletedWords, progressMemoriedWords;

        SecondTimeViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCompletedWords = itemView.findViewById(R.id.tvCompletedWords);
            progressCompletedWords = itemView.findViewById(R.id.progressCompletedWords);
            tvMemoriedWords = itemView.findViewById(R.id.tvMemoriedWords);
            progressMemoriedWords = itemView.findViewById(R.id.progressMemoriedWords);
        }

        void bind(HistoryStudy item) {
            tvTime.setText(item.getTimes());
            tvCompletedWords.setText(item.getCompletedWords() + "/" + totalQuestion);
            progressCompletedWords.setProgress(item.getCompletedProgress());
            tvMemoriedWords.setText(item.getMemoriedWords() + "/" + totalQuestion);
            progressMemoriedWords.setProgress(item.getMemoriedProgress());
        }
    }
}
