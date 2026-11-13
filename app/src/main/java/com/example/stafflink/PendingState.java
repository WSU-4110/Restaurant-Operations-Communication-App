package com.example.stafflink;

import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class PendingState implements TaskState {

    @Override
    public Map<String, Object> complete(String completedBy, String completedByName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("completedBy", completedBy);
        updates.put("completedByName", completedByName);
        updates.put("completedAt", FieldValue.serverTimestamp());
        return updates;
    }

    @Override
    public String getStatus() {
        return "Open";
    }
}
