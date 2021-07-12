package com.android.arijit.firebase.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        boolean virgin = intent.getBooleanExtra("FirstTime", false);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_todo);
        if (virgin) {
            ProgressDialog start = new ProgressDialog(this);
            start.setMessage("Getting things ready");
            start.show();
            String user = mAuth.getCurrentUser().getEmail();

            mStore.collection(user).get();
            mStore.collection("commentCollection")
                    .whereEqualTo("user", user)
                    .get()
                    .addOnCompleteListener(task -> {
                        start.dismiss();
                    });

            SharedPreferences.Editor myEditor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            myEditor.putInt("sync", 0);
            myEditor.commit();
            Snackbar.make(fab, "Successfully logged in", Snackbar.LENGTH_SHORT)
                    .show();
        }

        (getSupportFragmentManager().beginTransaction())
                .replace(R.id.list_fragment_container, TodolistFragment.newInstance())
                .commit();

        ((FloatingActionButton) findViewById(R.id.fab_add_todo))
                .setOnClickListener(v -> {
                    startActivity(new Intent(MainActivity.this, AddTodoActivity.class));
                });

        Log.i(TAG, "onCreate: created");
    }
}