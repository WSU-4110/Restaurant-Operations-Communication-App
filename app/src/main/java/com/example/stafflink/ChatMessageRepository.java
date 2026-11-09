package com.example.stafflink;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatMessageRepository implements MessageSubject {

    private final CollectionReference messages;

    private final List<MessageObserver> observers =
            new CopyOnWriteArrayList<>();

    private ListenerRegistration listener;

    public ChatMessageRepository(CollectionReference messages) {
        this.messages = messages;
    }

    @Override
    public void addObserver(MessageObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(MessageObserver observer) {
        observers.remove(observer);
    }

    public void startListening() {
        if (listener != null) {
            return;
        }

        listener = messages
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null) {
                        for (MessageObserver observer : observers) {
                            observer.onMessageError(error);
                        }
                        return;
                    }

                    if (snapshot == null) {
                        return;
                    }

                    List<ChatMessage> updatedMessages =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot document : snapshot) {

                        String text = document.getString("text");
                        String senderId = document.getString("senderId");
                        String senderName = document.getString("senderName");

                        if (text == null) {
                            continue;
                        }

                        if (senderName == null) {
                            senderName = "Member";
                        }

                        ChatMessage message = new ChatMessage(
                                text,
                                senderId,
                                senderName,
                                document.getTimestamp("timestamp")
                        );

                        updatedMessages.add(message);
                    }

                    for (MessageObserver observer : observers) {
                        observer.onMessagesUpdated(updatedMessages);
                    }
                });
    }

    public void stopListening() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}