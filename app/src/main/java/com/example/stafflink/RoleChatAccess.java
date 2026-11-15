package com.example.stafflink;

import java.util.List;
import java.util.Map;

public final class RoleChatAccess {
    private final ChatAccessPolicy policy;

    public RoleChatAccess(String userId, String ownerId, Map<String, Object> member) {
        boolean isAdmin = userId != null && (userId.equals(ownerId)
                || (member != null && "manager".equals(member.get("role"))));
        if (isAdmin) {
            policy = new AdminChatAccess();
        } else {
            Object roleIds = member == null ? null : member.get("roleIds");
            policy = new AssignedRoleChatAccess(
                    roleIds instanceof List<?> ? (List<?>) roleIds : null);
        }
    }

    public boolean canView(String roleId, Map<String, Object> role) {
        return policy.canView(roleId, role);
    }

    public boolean canSend(String roleId, Map<String, Object> role) {
        return policy.canSend(roleId, role);
    }
}
