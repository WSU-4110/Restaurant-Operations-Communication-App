package com.example.stafflink;

import java.util.Map;

public interface TaskState {

    Map<String, Object> complete(String completedBy, String completedByName);

    String getStatus();
}

