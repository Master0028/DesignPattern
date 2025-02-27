package com.example.vopet.model;

import android.os.Parcel;
import android.os.Parcelable;

public class WordLearned implements Parcelable {
    private String word;
    private String userAnswer;
    private String correctAnswer;
    private int order;

    public WordLearned(int order, String word, String userAnswer, String correctAnswer) {
        this.order = order;
        this.word = word;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
    }

    protected WordLearned(Parcel in) {
        order = in.readInt();
        word = in.readString();
        userAnswer = in.readString();
        correctAnswer = in.readString();
    }

    public static final Creator<WordLearned> CREATOR = new Creator<WordLearned>() {
        @Override
        public WordLearned createFromParcel(Parcel in) {
            return new WordLearned(in);
        }

        @Override
        public WordLearned[] newArray(int size) {
            return new WordLearned[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(order);
        dest.writeString(word);
        dest.writeString(userAnswer);
        dest.writeString(correctAnswer);
    }

    public int getOrder() {
        return order;
    }

    public String getWord() {
        return word;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
