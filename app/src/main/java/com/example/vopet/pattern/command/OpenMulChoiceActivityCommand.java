package com.example.vopet.pattern.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.vopet.pattern.SessionSingleton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenMulChoiceActivityCommand implements ICommand {
    private Context context;
    private Class<?> activityClass;
    private IBundleProvider bundleProvider;

    public OpenMulChoiceActivityCommand(Context context, Class<?> activityClass, IBundleProvider bundleProvider) {
        this.context = context;
        this.activityClass = activityClass;
        this.bundleProvider = bundleProvider;
    }

    @Override
    public void execute() {
        Bundle bundle = bundleProvider.getBundle();
        String topicName = bundle.getString("topicName");
        String selectedLanguage = bundle.getString("selection");
        boolean isOnlyPriorityWordsChecked = bundle.getBoolean("isOnlyPriorityWords");
        boolean isShuffleChecked = bundle.getBoolean("isShuffle");
        boolean isAutoSpeakingChecked = bundle.getBoolean("isAutoSpeakingChecked");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = SessionSingleton.getInstance().getUserId();

        db.collection("topics")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("name", topicName)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        List<String> wordsList = new ArrayList<>();
                        List<String> meaningsList = new ArrayList<>();
                        List<String> pronunciationList = new ArrayList<>();

                        querySnapshots.getDocuments().get(0)
                                .getReference()
                                .collection("vocabularies")
                                .get()
                                .addOnSuccessListener(vocabSnapshots -> {
                                    for (QueryDocumentSnapshot vocab : vocabSnapshots) {
                                        if (isOnlyPriorityWordsChecked && !Boolean.TRUE.equals(vocab.getBoolean("priority"))) {
                                            continue;
                                        }
                                        if ("English".equals(selectedLanguage)) {
                                            wordsList.add(vocab.getString("english"));
                                            meaningsList.add(vocab.getString("meaning"));
                                        } else {
                                            wordsList.add(vocab.getString("meaning"));
                                            meaningsList.add(vocab.getString("english"));
                                        }
                                        pronunciationList.add(vocab.getString("pronounce"));
                                    }

                                    if (isShuffleChecked) {
                                        long seed = System.nanoTime();
                                        Collections.shuffle(wordsList, new java.util.Random(seed));
                                        Collections.shuffle(meaningsList, new java.util.Random(seed));
                                        Collections.shuffle(pronunciationList, new java.util.Random(seed));
                                    }

                                    Intent intent = new Intent(context, activityClass);
                                    intent.putStringArrayListExtra("wordsList", new ArrayList<>(wordsList));
                                    intent.putStringArrayListExtra("meaningsList", new ArrayList<>(meaningsList));
                                    intent.putStringArrayListExtra("pronunciationList", new ArrayList<>(pronunciationList));
                                    intent.putExtra("selectedLanguage", selectedLanguage);
                                    intent.putExtra("isAutoSpeakingChecked", isAutoSpeakingChecked);
                                    intent.putExtra("topicName", topicName);
                                    context.startActivity(intent);

                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to load vocabularies", Toast.LENGTH_SHORT).show());

                    } else {
                        Toast.makeText(context, "No topics found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to load topics", Toast.LENGTH_SHORT).show());
    }
}