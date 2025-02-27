package com.example.vopet.model;

public class HistoryStudy {
    private String times;
    private int completedWords;
    private int memoriedWords;
    private int completedProgress;
    private int memoriedProgress;
    private String topicName;
    private String typeStudy;
    private String creatorId;
    private int score;

    // Constructor không tham số (bắt buộc)
    public HistoryStudy() {
    }

    public HistoryStudy(String times, String creatorId, String topicName, String typeStudy, int completedWords, int memoriedWords, int completedProgress, int memoriedProgress, int score) {
        this.times = times;
        this.completedWords = completedWords;
        this.memoriedWords = memoriedWords;
        this.completedProgress = completedProgress;
        this.memoriedProgress = memoriedProgress;
        this.typeStudy = typeStudy;
        this.topicName = topicName;
        this.creatorId = creatorId;
        this.score = score;
    }

    // Getter và Setter
    public String getTimes() { return times; }
    public void setTimes(String times) { this.times = times; }

    public int getCompletedWords() { return completedWords; }
    public void setCompletedWords(int completedWords) { this.completedWords = completedWords; }

    public int getMemoriedWords() { return memoriedWords; }
    public void setMemoriedWords(int memoriedWords) { this.memoriedWords = memoriedWords; }

    public int getCompletedProgress() { return completedProgress; }
    public void setCompletedProgress(int completedProgress) { this.completedProgress = completedProgress; }

    public int getMemoriedProgress() { return memoriedProgress; }
    public void setMemoriedProgress(int memoriedProgress) { this.memoriedProgress = memoriedProgress; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getTypeStudy() { return typeStudy; }
    public void setTypeStudy(String typeStudy) { this.typeStudy = typeStudy; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}

