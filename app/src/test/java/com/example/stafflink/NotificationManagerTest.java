
package com.example.stafflink;

import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

public class NotificationManagerTest {

    private NotificationManager manager;
    private Notification n1;
    private Notification n2;
    private Notification n3;

    @Before
    public void setUp() {
        manager = new NotificationManager();

        n1 = new Notification(
                "1", "user1", "New task assigned", "TASK");

        n2 = new Notification(
                "2", "user1", "Low stock alert", "INVENTORY");

        n3 = new Notification(
                "3", "user2", "Task completed", "TASK");
    }

    // Test 1: Add notification
    @Test
    public void testAddNotification() {
        manager.addNotification(n1);
        manager.addNotification(null);

        assertEquals(1,
                manager.getNotifications("user1").size());

        assertSame(n1,
                manager.getNotifications("user1").get(0));
    }

    // Test 2: Retrieve notifications
    @Test
    public void testGetNotifications() {
        manager.addNotification(n1);
        manager.addNotification(n2);
        manager.addNotification(n3);

        List<Notification> result =
                manager.getNotifications("user1");

        assertEquals(2, result.size());
        assertTrue(result.contains(n1));
        assertTrue(result.contains(n2));
        assertFalse(result.contains(n3));
        assertTrue(manager.getNotifications("unknown").isEmpty());
    }

    // Test 3: Mark notification as read
    @Test
    public void testMarkAsRead() {
        manager.addNotification(n1);
        manager.addNotification(n2);

        assertFalse(n1.isRead());
        assertTrue(manager.markAsRead("1"));
        assertTrue(n1.isRead());
        assertFalse(n2.isRead());
        assertFalse(manager.markAsRead("999"));
        assertTrue(manager.markAsRead("1"));
    }

    // Test 4: Count unread notifications
    @Test
    public void testGetUnreadCount() {
        assertEquals(0, manager.getUnreadCount("user1"));

        manager.addNotification(n1);
        manager.addNotification(n2);
        manager.addNotification(n3);

        assertEquals(2, manager.getUnreadCount("user1"));

        manager.markAsRead("1");

        assertEquals(1, manager.getUnreadCount("user1"));
        assertEquals(1, manager.getUnreadCount("user2"));

        manager.markAsRead("2");

        assertEquals(0, manager.getUnreadCount("user1"));
    }

    // Test 5: Filter notifications
    @Test
    public void testFilterNotifications() {
        manager.addNotification(n1);
        manager.addNotification(n2);
        manager.addNotification(n3);

        List<Notification> result =
                manager.filterNotifications("user1", "TASK");

        assertEquals(1, result.size());
        assertTrue(result.contains(n1));
        assertFalse(result.contains(n2));
        assertFalse(result.contains(n3));

        assertTrue(manager.filterNotifications(
                "user1", "UNKNOWN").isEmpty());
    }

    // Test 6: Clear notifications
    @Test
    public void testClearNotifications() {
        manager.addNotification(n1);
        manager.addNotification(n2);
        manager.addNotification(n3);

        manager.clearNotifications("user1");

        assertTrue(manager.getNotifications("user1").isEmpty());
        assertEquals(1,
                manager.getNotifications("user2").size());

        manager.clearNotifications("unknown");

        assertEquals(1,
                manager.getNotifications("user2").size());
    }
}

