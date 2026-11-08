package com.example.stafflink;

public class Task {

    private String title;
    private TaskState state;

    public Task(String title) {
        this.title = title;
        this.state = new PendingState();
    }

    public String getTitle() {
        return title;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public void markCompleted() {
        state.complete(this);
    }

    public String getStatus() {
        return state.getStatus();
    }
}

