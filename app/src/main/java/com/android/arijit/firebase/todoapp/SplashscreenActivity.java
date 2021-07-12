package com.android.arijit.firebase.todoapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.arijit.firebase.todoapp.databinding.ActivitySplashscreenBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SplashscreenActivity extends AppCompatActivity {

    private View mContentView;

    private ActivitySplashscreenBinding binding;
    private Animation topDown, botUp, botUpTag;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        topDown = AnimationUtils.loadAnimation(this, R.anim.top_down);
        botUp = AnimationUtils.loadAnimation(this, R.anim.botton_up);
        botUpTag = AnimationUtils.loadAnimation(this, R.anim.bottom_up_tag);

        mContentView = binding.ivSplashLogo;

        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        binding.ivSplashLogo.startAnimation(topDown);
        binding.tvSplashName.startAnimation(botUp);
        binding.tvSplashTag.startAnimation(botUpTag);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(SplashscreenActivity.this, AccountActivity.class));
                }
                else{
                    startActivity(new Intent(SplashscreenActivity.this, MainActivity.class));
                }
                finish();
            }
        }, 2500);

    }
}