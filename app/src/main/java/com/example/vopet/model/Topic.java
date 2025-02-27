package com.example.vopet.model;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    private String id;
    private String name;
    private int numberOfWord;
    private int progress;
    private List<String> folderName; // Thay đổi từ String sang List<String>
    private String creatorId;
    private long createdTime;
    private long lastVisitedTime;
    private String permission;
    private int access;
    private String ownerId; //id ng tai (so huu) topic public

    public Topic() {
        folderName = new ArrayList<>(); // Khởi tạo danh sách folderName
    }

    public Topic(List<String> folderName, String creatorId, String name, int numberOfWord, int progress, long createdTime, long lastVisitedTime) {
        this.name = name;
        this.numberOfWord = numberOfWord;
        this.progress = progress;
        this.folderName = folderName != null ? folderName : new ArrayList<>(); // Đảm bảo không bị null
        this.creatorId = creatorId;
        this.createdTime = createdTime;
        this.lastVisitedTime = lastVisitedTime;
        this.permission = "Private";
        this.ownerId = creatorId;
    }

    public  String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public int getProgress() {
        return progress;
    }

    public int getNumberOfWord() {
        return numberOfWord;
    }

    public List<String> getFolderName() {
        return folderName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getLastVisitedTime() {
        return lastVisitedTime;
    }

    public String getPermission() {
        return permission;
    }
    public String getOwnerId() {
        return ownerId;
    }

    public void setFolderName(List<String> folderName) {
        this.folderName = folderName != null ? folderName : new ArrayList<>(); // Đảm bảo không bị null
    }

    public void addFolderName(String folder) {
        if (this.folderName == null) {
            this.folderName = new ArrayList<>();
        }
        this.folderName.add(folder);
    }

    public void removeFolderName(String folder) {
        if (this.folderName != null) {
            this.folderName.remove(folder);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public void setLastVisitedTime(long lastVisitedTime) {
        this.lastVisitedTime = lastVisitedTime;
    }

    public void setNumberOfWord(int numberOfWord) {
        this.numberOfWord = numberOfWord;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
