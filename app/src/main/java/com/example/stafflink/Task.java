
package com.example.stafflink;

import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.FieldValue;

public class Task {

    private final String title;
    private final String description;
    private final String priority;
    private final String assignedRoleId;
    private final String createdBy;
    private final String status;

    private Task(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.priority = builder.priority;
        this.assignedRoleId = builder.assignedRoleId;
        this.createdBy = builder.createdBy;
        this.status = builder.status;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> task = new HashMap<>();

        task.put("title", title);
        task.put("description", description);
        task.put("priority", priority);
        task.put("assignedRoleId", assignedRoleId);
        task.put("createdBy", createdBy);
        task.put("createdAt", FieldValue.serverTimestamp());
        task.put("status", status);

        return task;
    }

    public static class Builder {

        private String title;
        private String description;
        private String priority;
        private String assignedRoleId;
        private String createdBy;
        private String status = "open";

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

        public Task build() {
            return new Task(this);
        }
    }
}
