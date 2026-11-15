package com.example.stafflink;

import java.util.Map;

public interface ChatAccessPolicy {
    boolean canView(String roleId, Map<String, Object> role);

    boolean canSend(String roleId, Map<String, Object> role);

    static boolean isActive(Map<String, Object> role) {
        return role != null && !Boolean.TRUE.equals(role.get("archived"));
    }

    static boolean permission(Map<String, Object> role, String key) {
        Object permissions = role.get("permissions");
        if (!(permissions instanceof Map<?, ?>)) {
            return true;
        }

        Object value = ((Map<?, ?>) permissions).get(key);
        return value == null || Boolean.TRUE.equals(value);
    }
}
