package com.example.stafflink;

import com.google.firebase.Timestamp;

public class ChatMessage {

    private String text;
    private String senderId;
    private String senderName;
    private Timestamp timestamp;

    public ChatMessage(String text, String senderId,
                       String senderName, Timestamp timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}