package com.example.stafflink;

import java.util.Map;

public final class AdminChatAccess implements ChatAccessPolicy {
    @Override
    public boolean canView(String roleId, Map<String, Object> role) {
        return ChatAccessPolicy.isActive(role);
    }

    @Override
    public boolean canSend(String roleId, Map<String, Object> role) {
        return canView(roleId, role);
    }
}
