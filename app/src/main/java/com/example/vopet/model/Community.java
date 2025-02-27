package com.example.vopet.model;

public class Community {
    private String topicName;
    private String authName;
    private int numberOfWord;
    private int ranking;
    private String creatorId;
    private String topicId;

    public Community(String topicName, String authName, int numberOfWord, int ranking, String creatorId, String topicId) {
        this.topicName = topicName;
        this.authName = authName;
        this.numberOfWord = numberOfWord;
        this.ranking = ranking;
        this.creatorId = creatorId;
        this.topicId = topicId;
    }

    public String getTopicName() { return topicName; }
    public String getAuthName() { return authName; }
    public int getRanking() { return ranking; }
    public int getNumberOfWord() { return numberOfWord; }
    public String getCreatorId() { return creatorId; }
    public String getTopicId() { return topicId; }
}
