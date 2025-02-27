package com.example.vopet.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.activity.StudyTopicInFindAndFillActivity;
import com.example.vopet.model.Topic;

import java.util.List;

public class TopicAdapterBlueInFindAndFill extends RecyclerView.Adapter<TopicAdapterBlueInFindAndFill.TopicViewHolder> {
    private List<Topic> topicList;

    public TopicAdapterBlueInFindAndFill(List<Topic> topicList) {
        this.topicList = topicList;
    }

    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_item6, parent, false);

        return new TopicViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TopicViewHolder holder, int position) {
        Topic topic = topicList.get(position);

        holder.topicNameTextView.setText(topic.getName());
        holder.numberOfWordsTextView.setText(topic.getNumberOfWord() + " words");
        holder.progressBar.setProgress(topic.getProgress());
        holder.progressPercentage.setText(topic.getProgress() + "%");
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

            // Add click listener for card view
            cardView.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, StudyTopicInFindAndFillActivity.class);
                intent.putExtra("topicName", topicNameTextView.getText().toString());
                context.startActivity(intent);
            });
        }
    }
}
