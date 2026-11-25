
package com.example.stafflink;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class ChatAccessUnitTest {

    private Map<String, Object> createRole(
            boolean archived, boolean view, boolean send) {

        Map<String, Object> role = new HashMap<>();
        Map<String, Object> permissions = new HashMap<>();

        permissions.put("viewChat", view);
        permissions.put("sendChat", send);

        role.put("archived", archived);
        role.put("permissions", permissions);

        return role;
    }

    @Test
    public void testIsActive() {
        assertTrue(ChatAccessPolicy.isActive(
                createRole(false, true, true)));

        assertFalse(ChatAccessPolicy.isActive(
                createRole(true, true, true)));

        assertFalse(ChatAccessPolicy.isActive(null));

        assertTrue(ChatAccessPolicy.isActive(
                new HashMap<>()));
    }

    @Test
    public void testPermission() {
        Map<String, Object> role =
                createRole(false, true, false);

        assertTrue(ChatAccessPolicy.permission(
                role, "viewChat"));

        assertFalse(ChatAccessPolicy.permission(
                role, "sendChat"));

        assertTrue(ChatAccessPolicy.permission(
                role, "unknown"));

        assertTrue(ChatAccessPolicy.permission(
                new HashMap<>(), "viewChat"));

        role.put("permissions", "invalid");

        assertTrue(ChatAccessPolicy.permission(
                role, "viewChat"));
    }

    @Test
    public void testAdminCanView() {
        AdminChatAccess admin = new AdminChatAccess();

        assertTrue(admin.canView("role1",
                createRole(false, false, false)));

        assertFalse(admin.canView("role1",
                createRole(true, true, true)));

        assertFalse(admin.canView("role1", null));
    }

    @Test
    public void testAdminCanSend() {
        AdminChatAccess admin = new AdminChatAccess();

        assertTrue(admin.canSend("role1",
                createRole(false, false, false)));

        assertFalse(admin.canSend("role1",
                createRole(true, true, true)));

        assertFalse(admin.canSend("role1", null));
    }

    @Test
    public void testAssignedRoleCanView() {
        AssignedRoleChatAccess access =
                new AssignedRoleChatAccess(
                        Collections.singletonList("role1"));

        assertTrue(access.canView("role1",
                createRole(false, true, true)));

        assertFalse(access.canView("role2",
                createRole(false, true, true)));

        assertFalse(access.canView("role1",
                createRole(false, false, true)));

        assertFalse(access.canView("role1",
                createRole(true, true, true)));

        assertFalse(access.canView("role1", null));
    }

    @Test
    public void testAssignedRoleCanSend() {
        AssignedRoleChatAccess access =
                new AssignedRoleChatAccess(
                        Collections.singletonList("role1"));

        assertTrue(access.canSend("role1",
                createRole(false, true, true)));

        assertFalse(access.canSend("role1",
                createRole(false, true, false)));

        assertFalse(access.canSend("role2",
                createRole(false, true, true)));

        assertFalse(access.canSend("role1",
                createRole(false, false, true)));

        assertFalse(access.canSend("role1",
                createRole(true, true, true)));

        assertFalse(access.canSend("role1", null));
    }
}
