package com.example.medassist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GetStartedActivity extends AppCompatActivity {

    Button btnGetStarted, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnGetStarted.setOnClickListener(v -> {
            Intent i = new Intent(GetStartedActivity.this, LoginActivity.class);
            startActivity(i);
        });

        btnSignUp.setOnClickListener(v -> {
            Intent i = new Intent(GetStartedActivity.this, SignUpActivity.class);
            startActivity(i);
        });
    }
}
