
package com.example.stafflink;

public interface TaskState {

    void complete(Task task);

    String getStatus();
}

