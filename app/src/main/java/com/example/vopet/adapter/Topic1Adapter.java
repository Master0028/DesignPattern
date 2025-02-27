package com.example.vopet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vopet.R;
import com.example.vopet.model.Topic1;

import java.util.List;

public class Topic1Adapter extends RecyclerView.Adapter<Topic1Adapter.ViewHolder> {
    private List<Topic1> topicList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public Topic1Adapter(Context context, List<Topic1> topicList) {
        this.context = context;
        this.topicList = topicList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.topic_item5, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Topic1 topic = topicList.get(position);
        holder.txtTopicName.setText(topic.getName());
        holder.txtNumberOfWords.setText(topic.getNumberOfWord() + " words");

        // Set onClick listener for each item (nút Add)
        holder.btnAdd.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(topic);
            } else {
                Toast.makeText(context, "Add button clicked for: " + topic.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Topic1 topic);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTopicName;
        TextView txtNumberOfWords;
        Button btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTopicName = itemView.findViewById(R.id.topicName);
            txtNumberOfWords = itemView.findViewById(R.id.numberOfWords);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
