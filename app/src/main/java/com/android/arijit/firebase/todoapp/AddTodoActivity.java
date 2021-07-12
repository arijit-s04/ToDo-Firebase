package com.android.arijit.firebase.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddTodoActivity extends AppCompatActivity {
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText et_add_todo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        String user = null;
        try {
            user = mAuth.getCurrentUser().getEmail();
        }catch (Exception e){
            user = "dummyUser";
        }
        et_add_todo = (EditText) findViewById(R.id.etNewTask);
        TodolistFragment.STATE_CHANGED = false;
        String finalUser = user;
        ((Button) findViewById(R.id.btnAddTask)).setOnClickListener(v ->{
            if(et_add_todo.getText().toString().trim().equals("")){
                Snackbar.make(v, "Cannot add empty task", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .show();
                return;
            }

            HashMap<String, Object> task = new HashMap<>();
            task.put("task", et_add_todo.getText().toString().trim());
            task.put("completed", false);
            task.put("date", new ToDo().getCdate());
            task.put("serverTimestamp", FieldValue.serverTimestamp());
            firestore.collection(finalUser)
                    .add(task)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null)
                                    .show();
                        }
                    });
            et_add_todo.setText("");
            Snackbar.make(v, "Added", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null)
                    .show();
            TodolistFragment.STATE_CHANGED = true;
        });

    }
}