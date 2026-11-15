package com.example.stafflink;

import java.util.Map;

public class CompletedState implements TaskState {

    @Override
    public Map<String, Object> complete(String completedBy, String completedByName) {
        // Task is already completed.
        // No additional action is needed.
        return null;
    }

    @Override
    public String getStatus() {
        return "Completed";
    }
}