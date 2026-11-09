package com.example.stafflink;

import java.util.List;

public interface MessageObserver {

    void onMessagesUpdated(List<ChatMessage> messages);

    void onMessageError(Exception error);
}