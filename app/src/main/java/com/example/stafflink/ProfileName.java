package com.example.stafflink;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;

public final class ProfileName {

    private ProfileName() {}

    public static void load(FirebaseFirestore db, String uid, Consumer<String> onLoaded) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(profile -> {
                    String first = profile.getString("firstName");
                    String last = profile.getString("lastName");
                    String name = ((first == null ? "" : first.trim()) + " " + (last == null ? "" : last.trim())).trim();
                    if (name.isEmpty()) {
                        name = "Member";
                    }
                    if (name.length() > 100) {
                        name = name.substring(0, 100);
                    }
                    onLoaded.accept(name);
                })
                .addOnFailureListener(e -> onLoaded.accept("Member"));
    }
}
