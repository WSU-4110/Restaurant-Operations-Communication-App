# Sprint 2 Planning Summary

## Sprint Goal

The primary goal of Sprint 2 was to expand StaffLink's functionality by introducing role-based organization management, group messaging, and task management. Building on the authentication, organization creation, and general communication features developed during Sprint 1, this sprint focused on providing employees and managers with tools tailored to their assigned roles.

## Planned Features and User Stories

The team selected six functional requirements for Sprint 2:

| Issue | Functional Requirement | Description |
|---|---|---|
| [FR13](https://github.com/WSU-4110/Restaurant-Operations-Communication-App/issues/13) | Role Group Creation with Permissions | Allow authorized users to create role groups and configure their permissions. |
| [FR14](https://github.com/WSU-4110/Restaurant-Operations-Communication-App/issues/14) | Assign Employees to Roles | Allow managers and owners to assign employees to role groups within their organization. |
| [FR15](https://github.com/WSU-4110/Restaurant-Operations-Communication-App/issues/15) | Access Role-Based Groups | Provide employees with access to groups corresponding to their assigned roles. |
| [FR6](https://github.com/WSU-4110/Restaurant-Operations-Communication-App/issues/6) | Role-Based Group Messaging | Enable communication within role-specific chat rooms. |
| [FR3](https://github.com/WSU-4110/Restaurant-Operations-Communication-App/issues/3) | New Task Creation | Allow tasks to be created and organized for employees within the application. |
| [FR4](https://github.com/WSU-4110/Restaurant-Operations-Communication-App/issues/4) | Task Completion | Allow employees to complete assigned tasks and track their status. |

## Sprint Planning and Implementation

Development was organized around two primary areas:

**Role Management and Communication:** The team prioritized establishing role creation and assignment functionality as the foundation for role-based access and messaging. This included configuring role permissions, assigning multiple roles to employees, and providing role-specific chat rooms that support communication between authorized organization members.

**Task Management:** The second area focused on introducing task creation and completion functionality. Tasks could be organized around employee roles, allowing organizations to coordinate responsibilities and monitor task progress.

These features were designed to integrate with the existing Firebase Authentication and Cloud Firestore infrastructure while maintaining organization-level separation and role-based access controls.

## Sprint Outcome

All six functional requirements assigned to Sprint 2 were marked as completed and closed in the GitHub repository.

The sprint successfully expanded StaffLink's functionality by introducing role creation and assignment, role-based group messaging, and task creation and completion. These features allow organizations to structure employee responsibilities, control access to role-specific communication, and coordinate daily tasks.

The completion of these requirements established the foundation for expanding StaffLink's restaurant operations functionality in Sprint 3.

**Sprint Status:** Completed — 6 of 6 planned issues closed.
