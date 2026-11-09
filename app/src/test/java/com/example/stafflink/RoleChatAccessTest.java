package com.example.stafflink;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RoleChatAccessTest {
    private Map<String, Object> role(boolean view, boolean send, boolean archived) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("viewChat", view);
        permissions.put("sendChat", send);
        data.put("permissions", permissions);
        data.put("archived", archived);
        return data;
    }

    private RoleChatAccess employee(String... ids) {
        Map<String, Object> member = new HashMap<>();
        member.put("role", "employee");
        member.put("roleIds", Arrays.asList(ids));
        return new RoleChatAccess("employee", "owner", member);
    }

    @Test
    public void assignedRoleRespectsIndependentPermissions() {
        RoleChatAccess access = employee("server", "cook");
        assertTrue(access.canView("server", role(true, false, false)));
        assertFalse(access.canSend("server", role(true, false, false)));
        assertTrue(access.canSend("cook", role(true, true, false)));
        assertFalse(access.canView("other", role(true, true, false)));
        assertFalse(access.canSend("server", role(false, true, false)));
    }

    @Test
    public void missingMembershipAndAssignmentsDenyAccess() {
        assertFalse(new RoleChatAccess("outsider", "owner", null)
                .canView("server", role(true, true, false)));
        assertFalse(employee().canView("server", role(true, true, false)));
        assertFalse(employee("server").canView("server", null));
    }

    @Test
    public void adminsBypassPermissionsButCannotOpenArchivedRoles() {
        RoleChatAccess owner = new RoleChatAccess("owner", "owner", null);
        RoleChatAccess manager = new RoleChatAccess("manager", "owner",
                Collections.singletonMap("role", "manager"));
        for (RoleChatAccess access : Arrays.asList(owner, manager)) {
            assertTrue(access.canSend("server", role(false, false, false)));
            assertFalse(access.canView("server", role(true, true, true)));
            assertFalse(access.canView("missing", null));
        }
    }

    @Test
    public void legacyPermissionsDefaultToAllowedForAssignedMembers() {
        assertTrue(employee("server").canSend("server", new HashMap<>()));
        Map<String, Object> malformed = new HashMap<>();
        malformed.put("permissions", Collections.singletonMap("viewChat", "true"));
        assertFalse(employee("server").canView("server", malformed));
    }

    @Test
    public void removalOfAssignmentOrManagerStatusRevokesAccess() {
        assertFalse(employee().canView("server", role(true, true, false)));
        assertFalse(new RoleChatAccess("formerManager", "owner",
                Collections.singletonMap("role", "employee"))
                .canSend("server", role(true, true, false)));
    }
}
