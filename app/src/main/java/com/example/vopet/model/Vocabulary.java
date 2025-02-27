package com.example.vopet.model;

import java.util.HashMap;
import java.util.Map;

public class Vocabulary {
    private String english;
    private String meaning;
    private String status;
    private String pronounce;
    private boolean priority;
    private int stt;
    private String id;
    private int numberOfStudy;
    private String photo;

    public Vocabulary() {
        // Constructor mặc định cho Firestore
    }

    public Vocabulary(int stt, String english, String meaning, String pronounce, String status, boolean priority, String photo) {
        this.stt = stt;
        this.english = english;
        this.meaning = meaning;
        this.status = status;
        this.priority = priority;
        this.pronounce = pronounce;
        this.numberOfStudy = 0;
        this.photo = photo;
    }
    public Vocabulary(int stt, String english, String meaning, String pronounce, String status, boolean priority) {
        this.stt = stt;
        this.english = english;
        this.meaning = meaning;
        this.status = status;
        this.priority = priority;
        this.pronounce = pronounce;
        this.numberOfStudy = 0;
    }

    public String getEnglish() {
        return english;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public String getPronounce() {
        return pronounce;
    }

    public void setPronounce(String pronounce) {
        this.pronounce = pronounce;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumberOfStudy() {
        return numberOfStudy;
    }

    public void setNumberOfStudy(int numberOfStudy) {
        this.numberOfStudy = numberOfStudy;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("stt", stt);
        map.put("english", english);
        map.put("meaning", meaning);
        map.put("pronounce", pronounce);
        map.put("status", status);
        map.put("priority", priority);
        map.put("photo", photo);
        return map;
    }
}

