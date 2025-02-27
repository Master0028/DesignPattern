// File: SyncManager.java
package com.example.vopet.sync;

import android.content.Context;

import com.example.vopet.database.AppDatabase;
import com.example.vopet.model.Folder;
import com.example.vopet.model.Topic;
import com.example.vopet.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SyncManager {
    private final FirebaseFirestore firestore;
    private final AppDatabase localDb;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public SyncManager(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.localDb = AppDatabase.getInstance(context);
    }

    public void syncFromFirebase(String userId) {
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            executor.execute(() -> localDb.userDao().insertUser(user));
                        } else {
                            System.err.println("User data is null in Firestore snapshot.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    System.err.println("Error syncing from Firebase: " + e.getMessage());
                });
    }

    public void syncToFirebase(User user) {
        firestore.collection("users").document(user.userId).set(user)
                .addOnSuccessListener(aVoid -> System.out.println("Sync to Firebase successful for user: " + user.userId))
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    System.err.println("Error syncing to Firebase: " + e.getMessage());
                });
    }

    public void setupRealtimeSync(String userId) {
        firestore.collection("users").document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        System.err.println("Realtime sync error: " + error.getMessage());
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            executor.execute(() -> localDb.userDao().insertUser(user));
                        } else {
                            System.err.println("User data is null in Firestore snapshot.");
                        }
                    }
                });
    }

}
