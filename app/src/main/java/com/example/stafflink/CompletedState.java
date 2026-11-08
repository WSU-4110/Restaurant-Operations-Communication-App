package com.example.stafflink;

public class CompletedState implements TaskState {

    @Override
    public void complete(Task task) {
        // Task is already completed.
        // No additional action is needed.
    }

    @Override
    public String getStatus() {
        return "Completed";
    }
}
