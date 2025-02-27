package com.example.vopet.model;

public class Folder {
    private String creatorId;
    private String name;
    private int progress;

    private String dateCreated;
    private int numberOfTopic;

    public Folder() {}

    public Folder(String name, int progress, String creatorId, String dateCreated) {
        this.name = name;
        this.progress = progress;
        this.creatorId = creatorId;
        this.dateCreated = dateCreated;
        this.numberOfTopic = 0;
    }

    public String getName() { return name; }
    public int getProgress() { return progress; }
    public String getCreatorId() { return creatorId; }
    public String getDateCreated() { return dateCreated; }
    public int getNumberOfTopic() {
        return numberOfTopic;
    }
    public void setNumberOfTopic(int numberOfTopic) {
        this.numberOfTopic = numberOfTopic;
    }
}
