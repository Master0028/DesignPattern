package com.example.vopet.model;

import java.util.List;

public class Topic1 {
    private String id;
    private List<String> folderName; // Thay đổi từ String sang List<String>
    private String creatorId;
    private String name;
    private int numberOfWord;
    private int progress;
    private long createdTime;
    private long lastVisitedTime;

    // Default constructor for Firestore
    public Topic1() {
    }

    public Topic1(List<String> folderName, String creatorId, String name, int numberOfWord, int progress, long createdTime, long lastVisitedTime) {
        this.folderName = folderName;
        this.creatorId = creatorId;
        this.name = name;
        this.numberOfWord = numberOfWord;
        this.progress = progress;
        this.createdTime = createdTime;
        this.lastVisitedTime = lastVisitedTime;
    }

    // Getter and setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Other getters and setters
    public List<String> getFolderName() {
        return folderName;
    }

    public void setFolderName(List<String> folderName) {
        this.folderName = folderName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfWord() {
        return numberOfWord;
    }

    public void setNumberOfWord(int numberOfWord) {
        this.numberOfWord = numberOfWord;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getLastVisitedTime() {
        return lastVisitedTime;
    }

    public void setLastVisitedTime(long lastVisitedTime) {
        this.lastVisitedTime = lastVisitedTime;
    }
}
