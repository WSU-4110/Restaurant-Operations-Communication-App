package com.example.stafflink;

public interface MessageSubject {

    void addObserver(MessageObserver observer);

    void removeObserver(MessageObserver observer);
}