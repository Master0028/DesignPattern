package com.example.vopet.model;

public class Achievement {
    private String rank;
    private String topicName;
    private String creatorName;
    private int score;
    private long timestamp;

    public Achievement() {
        // Empty constructor for Firestore
    }

    public Achievement(String rank, String topicName, String creatorName, int score, long timestamp) {
        this.rank = rank;
        this.topicName = topicName;
        this.creatorName = creatorName;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getRank() {
        return rank;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public int getScore() {
        return score;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

