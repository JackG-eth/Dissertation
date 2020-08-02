package com.example.epic.ui.Activities;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.epic.R;

public class TermsActivity extends AppCompatActivity {

    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        /**
         * Logic to display the privacy policy, stored in cloud storage.
         */
        web = findViewById(R.id.webView_terms);
        web.loadUrl("https://firebasestorage.googleapis.com/v0/b/epic-5ae7a.appspot.com/o/PrivacyPolicy%2Fterms.html?alt=media&token=36de8064-00a8-44dd-9270-a4eae39f143f");
    }
}
