package com.example.vopet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.model.Achievement;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
    private final Context context;
    private final List<Achievement> achievementList;

    public AchievementAdapter(Context context, List<Achievement> achievementList) {
        this.context = context;
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.achievement_item, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievementList.get(position);

        holder.tvRank.setText(achievement.getRank());
        holder.tvTopicName.setText("Topic: " + achievement.getTopicName());
        holder.tvCreatorName.setText("Creator: " + achievement.getCreatorName());
        holder.tvScore.setText(achievement.getScore() + "");

        // Format timestamp to date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String date = dateFormat.format(achievement.getTimestamp());
        holder.tvDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTopicName, tvCreatorName, tvScore, tvDate;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvTopicName = itemView.findViewById(R.id.tvTopicName);
            tvCreatorName = itemView.findViewById(R.id.tvCreatorName);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
