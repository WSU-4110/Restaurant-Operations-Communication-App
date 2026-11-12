package com.example.stafflink;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;

public class StaffLinkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            FirebaseApp.initializeApp(this, new FirebaseOptions.Builder()
                    .setProjectId("demo-stafflink")
                    .setApplicationId("1:1234567890:android:1234567890abcdef")
                    .setApiKey("fake-api-key-for-local-emulator")
                    .build());
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.useEmulator("10.0.2.2", 8080);
            db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build());
        }
    }
}
