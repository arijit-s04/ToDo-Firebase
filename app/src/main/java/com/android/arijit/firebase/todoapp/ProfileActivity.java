package com.android.arijit.firebase.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        ((TextView) findViewById(R.id.user_name))
                .setText(
                        mAuth.getCurrentUser().getDisplayName()
                );
        ((TextView) findViewById(R.id.user_email))
                .setText(
                  mAuth.getCurrentUser().getEmail()
                );
        ((TextView) findViewById(R.id.user_id))
                .setText(
                        mAuth.getCurrentUser().getUid()
                );
    }
}