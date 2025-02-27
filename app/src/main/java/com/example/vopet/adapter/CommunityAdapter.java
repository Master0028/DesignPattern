package com.example.vopet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.model.Community;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private Context context;
    private List<Community> communityList;
    private OnItemClickListener listener;

    // Constructor
    public CommunityAdapter(Context context, List<Community> communityList) {
        this.context = context;
        this.communityList = communityList;
    }

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.community_item, parent, false);
        return new CommunityViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        if (position < 0 || position >= communityList.size()) {
            return; // Bỏ qua nếu vị trí không hợp lệ
        }

        Community community = communityList.get(position);
        holder.topicName.setText(community.getTopicName());
        holder.authName.setText("Creator: " + community.getAuthName());
        holder.numberOfWords.setText("Number of words: " + community.getNumberOfWord());
        holder.ranking.setText("Access: " + community.getRanking());
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }

    public void updateData(List<Community> newCommunityList) {
        this.communityList = newCommunityList;
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    // ViewHolder Class
    public static class CommunityViewHolder extends RecyclerView.ViewHolder {

        TextView topicName, authName, numberOfWords, ranking;
        CardView cardView;

        public CommunityViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            topicName = itemView.findViewById(R.id.topicName);
            authName = itemView.findViewById(R.id.authName);
            numberOfWords = itemView.findViewById(R.id.numberOfWords);
            ranking = itemView.findViewById(R.id.ranking);
            cardView = itemView.findViewById(R.id.cardViewButton);

            // Handle item click
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
