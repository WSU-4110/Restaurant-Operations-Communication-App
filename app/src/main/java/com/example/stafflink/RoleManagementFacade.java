package com.example.stafflink;

import java.util.Locale;

public class RoleManagementFacade {

    public interface RoleCreationCallback {
        void onSuccess();
        void onFailure(String message);
    }

    private final OrganizationPermissionService permissionService;
    private final RoleRepository roleRepository;

    public RoleManagementFacade(OrganizationPermissionService permissionService,
                                RoleRepository roleRepository) {
        this.permissionService = permissionService;
        this.roleRepository = roleRepository;
    }

    public void createRole(String organizationId, String userId, String requestedName,
                           boolean viewChat, boolean sendChat,
                           RoleCreationCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("A role creation callback is required");
        }
        if (organizationId == null || organizationId.trim().isEmpty()
                || userId == null || userId.trim().isEmpty()) {
            callback.onFailure("Missing organization or user");
            return;
        }

        String normalizedName = roleName.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            callback.onFailure("Enter a name");
            return;
        }
        if (name.length() > 40) {
            callback.onFailure("Role name must be 40 characters or less");
            return;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        if (normalized.equals("owner") || normalized.equals("manager")) {
            callback.onFailure("Role name already exists or is reserved");
            return;
        }

        permissionService.canManageRoles(organizationId, userId,
                new OrganizationPermissionService.PermissionCallback() {
                    @Override
                    public void onResult(boolean canManage) {
                        if (!canManage) {
                            callback.onFailure("Only owners and managers can create roles");
                            return;
                        }
                        roleRepository.activeRoleNameExists(organizationId, name,
                                new RoleRepository.DuplicateCheckCallback() {
                                    @Override
                                    public void onResult(boolean exists) {
                                        if (exists) {
                                            callback.onFailure(
                                                    "Role name already exists or is reserved");
                                            return;
                                        }
                                        roleRepository.createRole(organizationId, userId, name,
                                                viewChat, viewChat && sendChat,
                                                new RoleRepository.SaveCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        callback.onSuccess();
                                                    }

                                                    @Override
                                                    public void onFailure(Exception error) {
                                                        callback.onFailure(messageOf(error));
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(Exception error) {
                                        callback.onFailure(messageOf(error));
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        callback.onFailure(messageOf(error));
                    }
                });
    }

    private static String messageOf(Exception error) {
        String message = error == null ? null : error.getMessage();
        return message == null || message.trim().isEmpty()
                ? "Unable to complete the operation" : message;
    }
}
