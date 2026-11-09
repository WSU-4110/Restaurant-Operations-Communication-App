package com.example.stafflink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class AssignedRoleChatAccess implements ChatAccessPolicy {
    private final List<?> roleIds;

    public AssignedRoleChatAccess(List<?> roleIds) {
        this.roleIds = roleIds == null ? Collections.emptyList() : new ArrayList<>(roleIds);
    }

    @Override
    public boolean canView(String roleId, Map<String, Object> role) {
        return ChatAccessPolicy.isActive(role)
                && roleIds.contains(roleId)
                && ChatAccessPolicy.permission(role, "viewChat");
    }

    @Override
    public boolean canSend(String roleId, Map<String, Object> role) {
        return canView(roleId, role) && ChatAccessPolicy.permission(role, "sendChat");
    }
}
