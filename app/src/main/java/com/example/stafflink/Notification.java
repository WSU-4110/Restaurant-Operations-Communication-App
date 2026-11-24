
package com.example.stafflink;

public class Notification {

    private final String id;
    private final String recipientId;
    private final String message;
    private final String type;
    private boolean read;

    public Notification(String id, String recipientId,
                        String message, String type) {
        this.id = id;
        this.recipientId = recipientId;
        this.message = message;
        this.type = type;
        this.read = false;
    }

    public String getId() {
        return id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public void markAsRead() {
        this.read = true;
    }
}

