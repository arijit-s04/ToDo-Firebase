package com.android.arijit.firebase.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

public class AccountActivity extends AppCompatActivity {

    private Button tabLogin, tabSignup;
    private boolean lastTab = true;//true means in loginTab

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        tabLogin = (Button) findViewById(R.id.btn_tab_login);
        tabSignup = (Button) findViewById(R.id.btn_tab_signup);

        tabLogin.setOnClickListener( v ->{
            if(lastTab)
                return;
            lastTab = !lastTab;
            tabSignup.setTextColor(getResources().getColor(R.color.purple_500));
            tabSignup.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            tabLogin.setTextColor(getResources().getColor(R.color.white));
            tabLogin.setBackgroundColor(getResources().getColor(R.color.purple_500));

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.accountFragmentContainer, LoginFragment.newInstance(null, null))
                    .commit();
        });

        tabSignup.setOnClickListener(v ->{
            if(!lastTab)
                return;
            lastTab = !lastTab;
            tabLogin.setTextColor(getResources().getColor(R.color.purple_500));
            tabLogin.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            tabSignup.setTextColor(getResources().getColor(R.color.white));
            tabSignup.setBackgroundColor(getResources().getColor(R.color.purple_500));

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.accountFragmentContainer, SignupFragment.newInstance(null, null))
                    .commit();
        });
    }
}