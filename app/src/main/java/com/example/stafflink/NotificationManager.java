
package com.example.stafflink;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private final List<Notification> notifications =
            new ArrayList<>();

    public void addNotification(Notification notification) {
        if (notification != null) {
            notifications.add(notification);
        }
    }

    public List<Notification> getNotifications(String recipientId) {
        List<Notification> result = new ArrayList<>();

        for (Notification notification : notifications) {
            if (notification.getRecipientId().equals(recipientId)) {
                result.add(notification);
            }
        }

        return result;
    }

    public boolean markAsRead(String notificationId) {
        for (Notification notification : notifications) {
            if (notification.getId().equals(notificationId)) {
                notification.markAsRead();
                return true;
            }
        }

        return false;
    }

    public int getUnreadCount(String recipientId) {
        int count = 0;

        for (Notification notification : getNotifications(recipientId)) {
            if (!notification.isRead()) {
                count++;
            }
        }

        return count;
    }

    public List<Notification> filterNotifications(
            String recipientId, String type) {

        List<Notification> result = new ArrayList<>();

        for (Notification notification : getNotifications(recipientId)) {
            if (notification.getType().equals(type)) {
                result.add(notification);
            }
        }

        return result;
    }

    public void clearNotifications(String recipientId) {
        notifications.removeIf(notification ->
                notification.getRecipientId().equals(recipientId));
    }
}

