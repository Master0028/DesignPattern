package com.example.vopet.model;

public class RankingItem {
    private String username;
    private int score;
    private String userId;

    public RankingItem(String userId, String username, int score) {
        this.userId = userId;
        this.username = username;
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }
}