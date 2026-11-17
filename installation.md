# StaffLink — Installation and Build Guide

This guide explains how to obtain, configure, build, and run the StaffLink Android application using the team's existing Firebase project.

StaffLink is currently built and tested in Android Studio. 

## 1\. System requirements

|Component|Requirement|
|-|-|
|Operating system|Windows, macOS, or Linux supported by Android Studio|
|IDE|Android Studio version compatible with **Android Gradle Plugin (AGP) 9.4.1**|
|Gradle runtime JDK|**JDK 17** (or a compatible newer version); Android Studio's bundled JDK can be used|
|Project Java source level|**Java 11** (`sourceCompatibility` and `targetCompatibility`)|
|Android SDK Platform|**API 37**, installed through Android Studio's SDK Manager|
|Android SDK Build Tools|**36.0.0** (AGP 9.4 default; install if prompted)|
|Gradle|**9.6.0**, supplied by the repository's Gradle Wrapper|
|Test device|Android emulator or physical device running **Android 7.0 / API 24 or newer**|
|Other|Git, internet access for dependency downloads, and access to the configured Firebase backend|

The JDK used to **run Gradle** must be version 17 or later.

## 2\. Clone the complete repository

In a terminal, run:

```bash
git clone https://github.com/WSU-4110/Restaurant-Operations-Communication-App.git
cd Restaurant-Operations-Communication-App
```

## 3\. Open and synchronize in Android Studio

1. Launch Android Studio and choose **Open**.
2. Select the cloned `Restaurant-Operations-Communication-App` directory.
3. Allow Android Studio to trust the project and complete **Gradle Sync**.
4. If prompted, install the required **Android SDK Platform 37**, Build Tools, or other SDK packages through **Tools > SDK Manager**.
5. In **Settings > Build, Execution, Deployment > Build Tools > Gradle** (on macOS, **Android Studio > Settings/Preferences**), confirm that **Gradle JDK** points to a compatible JDK 17 or newer; the bundled JDK is usually suitable.
6. Use the included Gradle Wrapper rather than changing the project to an unrelated Gradle version.

The first sync may take longer because Gradle and Android/Firebase dependencies must be downloaded.

## 4\. Firebase configuration

StaffLink uses **Firebase Authentication (email/password)** and **Cloud Firestore**. The team currently uses a **shared Firebase project** administered by its authorized collaborators.

* The project repository includes the Android Firebase client configuration at **`app/google-services.json`**. Check that this file is present after cloning.
* The application ID is **`com.example.stafflink`**. The Firebase Android application configuration must correspond to that application ID.
* Existing team members use the same backend. A new collaborator who needs Firebase Console access should ask **Andrew Haddad (group leader)** or another authorized project administrator to grant access.
* Firebase Console access is **not** required merely to log in to the app, but an in-app account and the relevant organization membership are required to use organization features.
* If `app/google-services.json` is absent or has been replaced, obtain the correct Android configuration from the authorized Firebase project's **Project settings**, and put it at `app/google-services.json`, then synchronize Gradle again.
* The repository also contains **`firestore.rules`**. Android Studio does not automatically deploy that file: rules must be configured/deployed to the intended Firebase project separately by an authorized administrator when they change.

Do not substitute a configuration from an unrelated Firebase project. Using a new backend requires its own Authentication/Firestore setup and suitable deployed security rules; cloning the Android code alone does not copy the team's Firestore data.

**Security:** Never include Firebase service-account keys, passwords, or demonstration credentials in the documentation or repository. The Android `google-services.json` file is a client configuration, not a Firebase Admin service-account key.

## 5\. Build the application

### Android Studio

1. Wait for Gradle Sync to complete without errors.
2. Select **Build > Make Project** to compile the app, or **Build > Build App Bundle(s) / APK(s) > Build APK(s)** to produce a debug APK (menu wording may vary by Android Studio version).
3. The normal debug APK output is:

```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

## 6\. Install and run

### Run from Android Studio

1. Open **Tools > Device Manager** and create/start an Android Virtual Device with **API 24 or newer**, or connect an Android phone with **Developer options > USB debugging** enabled.
2. Select the **`app`** run configuration and the target device from the toolbar.
3. Click **Run** (green play icon). Android Studio will build, install, and launch StaffLink.
4. Ensure the emulator/device has internet connectivity so Firebase requests can complete.

## 7\. First-time application setup

1. Open StaffLink and select **Sign Up** to create an account using an email address and password, or **Log In** with an existing account.
2. On the Organizations screen, choose **Create Organization** to start a new organization, or **Join Organization** to enter a join code supplied by an owner or manager.
3. Open an organization to access **General Chat**.
4. Owners and managers can open **Manage Roles** to create or assign job roles. Owners can grant or remove Manager status.
5. Assigned job roles appear in the organization view. Open a role to access its chat and task screens, according to the applicable permissions.
6. Create or view role tasks; open a task to mark it complete when appropriate.
7. Use **Log Out** on the Organizations screen to sign out.

No shared test-account credentials are required for installation. If a demonstration organization is needed, request its join code and any approved test credentials privately from the project team.

## 8\. Relevant project files

```text
build.gradle.kts                 Root Gradle plugins
settings.gradle.kts              Project modules and repositories
gradle/libs.versions.toml        Android, Firebase, and plugin dependency versions
gradle/wrapper/                  Gradle 9.6.0 wrapper configuration
app/build.gradle.kts             Application SDK levels, Java compatibility, dependencies
app/google-services.json         Firebase Android client configuration
app/src/main/AndroidManifest.xml Android entry point and internet permission
firestore.rules                  Firestore security rules source
```

For the app's functionality, intended users, and contributor information, see [**README.md**](README.md).

