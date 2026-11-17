# StaffLink

**StaffLink** is an Android application for restaurant teams to communicate, organize employees by job role, and coordinate day-to-day work within a shared organization.

## Purpose

Restaurant operations depend on quick communication and clear task ownership across positions such as servers, bartenders, and hosts. StaffLink brings organization-wide messaging, role-specific conversations, and role-based task management into one application, helping employees and managers keep work organized without mixing information from different teams or organizations.

## Features

### Accounts and organizations

- Create an account, sign in, and sign out using Firebase Authentication.
- Create an organization and receive a join code to invite coworkers.
- Join an existing organization using its join code.
- View and switch between organizations associated with an account.

### Communication

- Send and receive messages in each organization's **General Chat**.
- See sender names and message timestamps.
- Access **role-specific chat rooms** for relevant job roles (for example, Server or Bartender).
- Configure whether members of a role can view its chat and send messages. Organization owners and managers have access to active role chats; employees' access depends on their assigned roles and chat permissions.
- Receive message updates through Cloud Firestore listeners.

### Role and member management

- Create custom job roles and configure their chat permissions.
- Rename, edit, or archive roles while preserving their records.
- Assign multiple job roles to an employee.
- Allow organization owners to grant or remove **Manager** status.
- Allow owners and managers to manage job roles and member assignments; only owners can change Manager status.

### Role-based tasks

- Create tasks for a specific job role, with a title, description, and **Low**, **Medium**, or **High** priority.
- Allow owners and managers to create tasks for any role; employees can create tasks for roles to which they are assigned.
- View tasks within the relevant role, sorted by priority.
- Open task details and mark tasks as completed.
- View recent completed tasks with completion information (completed tasks appear in the list for up to 24 hours after completion).

## Organization access levels

| Access level | Capabilities |
| --- | --- |
| **Owner** | Creates the organization; manages job roles, assignments, and manager status; accesses active role chats and join code. |
| **Manager** | Manages job roles and employee role assignments; accesses active role chats and join code; cannot grant or remove Manager status. |
| **Employee** | Participates in General Chat; uses role chats and role-based tasks according to assigned roles and applicable permissions. |

**Job roles** (such as Server or Bartender) are distinct from the administrative **Owner**, **Manager**, and **Employee** access levels.

## Technology stack

- **Platform:** Android (minimum API 24; compile/target API 37)
- **Language:** Java 11 source compatibility
- **User interface:** Android XML layouts and AndroidX / Material components
- **Authentication:** Firebase Authentication
- **Database and real-time updates:** Cloud Firestore
- **Build system:** Gradle with Kotlin DSL and the project Gradle Wrapper
- **Development environment:** Android Studio

## Getting started

1. Clone the repository and open its **root directory** in Android Studio.
2. Allow Gradle to sync and confirm the shared Firebase project configuration.
3. Run the `app` configuration on an Android emulator or compatible Android device.
4. Sign up or sign in, then create an organization or join one with its join code.

For system requirements, Firebase setup, and complete build steps, see **[installation.md](installation.md)**.

## Test Accounts

The following accounts have been created for testing and demonstrating StaffLink's functionality. Each account has a different permission level, allowing testers to evaluate the application's role-based access controls.

| Account Type | Email | Password |
|---|---|---|
| Organization Owner | man@man.com | Password |
| Manager | man2@man.com | Password |
| Employee | emp@emp.com | Password |

### Account Permissions

- **Owner:** Full administrative access, including organization management, role creation and assignment, manager permissions, and all organization chat rooms.
- **Manager:** Elevated permissions for managing employee roles, accessing organization chats, and coordinating tasks.
- **Employee:** Access to general chat, assigned role-specific chats, and tasks associated with their assigned roles.

These accounts belong to an existing test organization and are intended for development, testing, and project demonstrations only. They should not be used for storing real or sensitive information.

## Project structure

```text
Restaurant-Operations-Communication-App/
├── app/
│   ├── src/main/java/com/example/stafflink/  # Android activities and app logic
│   ├── src/main/res/                         # Layouts and Android resources
│   ├── src/main/AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json                  # Firebase Android client configuration
├── gradle/                                   # Wrapper and dependency version catalog
├── build.gradle.kts                          # Root build configuration
├── settings.gradle.kts
├── firestore.rules                           # Firestore rules source
├── README.md
└── installation.md
```

## Contributors

| Contributor | GitHub | Role |
| --- | --- | --- |
| Andrew Haddad | [@andrew-haddad](https://github.com/andrew-haddad) | Group leader |
| Armando Gjergji | [@armandough](https://github.com/armandough) | Team member |
| Cajoh Thomas | [@CajohT](https://github.com/CajohT) | Team member |
| Yen Vo | [@yenvo88](https://github.com/yenvo88) | Team member |
| Jaafar Yazbek | [@jaafaryazbek16](https://github.com/jaafaryazbek16) | Team member |
| Timo McGrath | [@hu1814](https://github.com/hu1814) | Team member |

## Development status

StaffLink is a course project developed and tested through Android Studio. The repository contains the Android source and build configuration; no Google Play Store distribution is required to run the project.


