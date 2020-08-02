package com.example.epic.ui.Activities;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.epic.R;

/*
 Activity to provide the user with the privacy policy for the application.
 */
public class PrivacyActivity extends AppCompatActivity {

    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        /**
         * Logic to display the privacy policy, stored in cloud storage.
         */
        web = findViewById(R.id.webView);
        web.loadUrl("https://firebasestorage.googleapis.com/v0/b/epic-5ae7a.appspot.com/o/PrivacyPolicy%2Fprivacy_policy.html?alt=media&token=00b3e5df-5d85-4c04-b051-622318c066f6");
    }
}
