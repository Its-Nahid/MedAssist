package com.example.medassist;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try { androidx.core.splashscreen.SplashScreen.installSplashScreen(this); } catch (Throwable ignored) { }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MotionLayout motion = findViewById(R.id.motionRoot);
        motion.post(() -> motion.transitionToEnd());

        motion.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) { }
            @Override public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) { }
            @Override public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) { }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                startActivity(new Intent(SplashActivity.this, GetStartedActivity.class));
                
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }
}
