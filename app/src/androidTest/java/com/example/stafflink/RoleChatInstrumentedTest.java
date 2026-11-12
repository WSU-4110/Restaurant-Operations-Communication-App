package com.example.stafflink;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class RoleChatInstrumentedTest {
    private void login(String account) throws Exception {
        Tasks.await(FirebaseAuth.getInstance().signInWithEmailAndPassword(
                account + "@stafflink.test", "StaffLink2026!"), 15, TimeUnit.SECONDS);
    }

    private Intent organizationIntent(Class<?> activity) {
        return new Intent(ApplicationProvider.getApplicationContext(), activity)
                .putExtra("organizationId", "demo-restaurant")
                .putExtra("organizationName", "Demo Restaurant");
    }

    private void waitForText(String text) throws Exception {
        long deadline = System.currentTimeMillis() + 15000;
        Throwable lastError = null;
        do {
            try {
                onView(withText(text)).check(matches(isDisplayed()));
                return;
            } catch (AssertionError | NoMatchingViewException error) {
                lastError = error;
            }
            Thread.sleep(150);
        } while (System.currentTimeMillis() < deadline);
        throw new AssertionError("Timed out waiting for " + text, lastError);
    }

    @Test
    public void loginScreenOpensEmployeeOrganization() throws Exception {
        FirebaseAuth.getInstance().signOut();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.emailInput)).perform(replaceText("employee@stafflink.test"));
            onView(withId(R.id.passwordInput)).perform(replaceText("StaffLink2026!"), closeSoftKeyboard());
            onView(withId(R.id.loginButton)).perform(click());
            waitForText("Demo Restaurant");
        }
    }

    @Test
    public void employeeSeesOnlyAssignedChat() throws Exception {
        login("employee");
        try (ActivityScenario<OrganizationHomeActivity> scenario = ActivityScenario.launch(
                organizationIntent(OrganizationHomeActivity.class))) {
            waitForText("Server Chat");
            onView(withText("Cook Chat")).check(doesNotExist());
            onView(withId(R.id.manageRolesButton)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    @Test
    public void unassignedEmployeeSeesAssignmentHint() throws Exception {
        login("unassigned");
        try (ActivityScenario<OrganizationHomeActivity> scenario = ActivityScenario.launch(
                organizationIntent(OrganizationHomeActivity.class))) {
            waitForText("No role chats available. Ask a manager to assign a role and enable chat access.");
            onView(withText("Server Chat")).check(doesNotExist());
        }
    }

    @Test
    public void assignedEmployeeCanReadAndSend() throws Exception {
        login("employee");
        try (ActivityScenario<GeneralChatActivity> scenario = ActivityScenario.launch(
                organizationIntent(GeneralChatActivity.class).putExtra("roleId", "server"))) {
            waitForText("Welcome to the Server role chat.");
            String message = "Android role chat test " + System.currentTimeMillis();
            onView(withId(R.id.messageInput)).perform(replaceText(message), closeSoftKeyboard());
            onView(withId(R.id.sendButton)).perform(click());
            waitForText(message);
        }
    }
}
