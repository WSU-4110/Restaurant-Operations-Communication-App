package com.example.stafflink;

public class PendingState implements TaskState {

    @Override
    public void complete(Task task) {
        task.setState(new CompletedState());
    }

    @Override
    public String getStatus() {
        return "Pending";
    }
}
