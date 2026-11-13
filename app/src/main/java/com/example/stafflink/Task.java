package com.example.stafflink;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Task {

    private final String id;
    private final String title;
    private final String description;
    private final String priority;
    private final String assignedRoleId;
    private final String createdBy;
    private final String createdByName;
    private final Date createdAt;
    private final String status;
    private final String completedByName;
    private final Date completedAt;

    private Task(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.priority = builder.priority;
        this.assignedRoleId = builder.assignedRoleId;
        this.createdBy = builder.createdBy;
        this.createdByName = builder.createdByName;
        this.createdAt = builder.createdAt;
        this.status = builder.status;
        this.completedByName = builder.completedByName;
        this.completedAt = builder.completedAt;
    }

    public static Task fromDocument(DocumentSnapshot document) {
        return new Builder()
                .setId(document.getId())
                .setTitle(document.getString("title"))
                .setDescription(document.getString("description"))
                .setPriority(document.getString("priority"))
                .setAssignedRoleId(document.getString("assignedRoleId"))
                .setCreatedBy(document.getString("createdBy"))
                .setCreatedByName(document.getString("createdByName"))
                .setCreatedAt(document.getDate("createdAt",
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
                .setStatus(document.getString("status"))
                .setCompletedByName(document.getString("completedByName"))
                .setCompletedAt(document.getDate("completedAt",
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
                .build();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> task = new HashMap<>();

        task.put("title", title);
        task.put("description", description);
        task.put("priority", priority);
        task.put("assignedRoleId", assignedRoleId);
        task.put("createdBy", createdBy);
        task.put("createdByName", createdByName);
        task.put("createdAt", FieldValue.serverTimestamp());
        task.put("status", status);

        return task;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getCompletedByName() {
        return completedByName;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public TaskState getState() {
        return isCompleted() ? new CompletedState() : new PendingState();
    }

    public Map<String, Object> markCompleted(String completedBy, String completedByName) {
        return getState().complete(completedBy, completedByName);
    }

    public int getPriorityRank() {
        if ("High".equals(priority)) {
            return 0;
        }
        if ("Medium".equals(priority)) {
            return 1;
        }
        return 2;
    }

    public static class Builder {

        private String id;
        private String title;
        private String description;
        private String priority;
        private String assignedRoleId;
        private String createdBy;
        private String createdByName;
        private Date createdAt;
        private String status = "open";
        private String completedByName;
        private Date completedAt;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setPriority(String priority) {
            this.priority = priority;
            return this;
        }

        public Builder setAssignedRoleId(String assignedRoleId) {
            this.assignedRoleId = assignedRoleId;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setCreatedByName(String createdByName) {
            this.createdByName = createdByName;
            return this;
        }

        public Builder setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setStatus(String status) {
            if (status != null) {
                this.status = status;
            }
            return this;
        }

        public Builder setCompletedByName(String completedByName) {
            this.completedByName = completedByName;
            return this;
        }

        public Builder setCompletedAt(Date completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }
}