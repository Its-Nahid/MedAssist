package com.example.medassist;

import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;

import android.app.AlarmManager;

import android.provider.Settings;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    // Tiles & views
    private CardView tileScanMedicine, tileStockTracker, tileAddMedicine, tileViewMedicine, tileContactDoctors;
    private CardView tileSettings;
    private TextView greetingText;

    private ImageButton fabMic;       // Purple banner mic/button
    private ImageView imgSettingsTile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.main_activity);

        // Find view
        tileScanMedicine   = findViewById(R.id.tileScan);
        tileStockTracker   = findViewById(R.id.tileStock);
        tileAddMedicine    = findViewById(R.id.tileAdd);
        tileViewMedicine   = findViewById(R.id.tileView);
        tileContactDoctors = findViewById(R.id.tileContact);
        tileSettings       = findViewById(R.id.tileSettings);

        fabMic            = findViewById(R.id.fabMic);
        imgSettingsTile   = findViewById(R.id.imgSettingsTile);

        // ==== Keep existing behaviors EXACTLY as before ====-

        // Scan Medicine -> open ScanMedicineActivity
        tileScanMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanMedicineActivity.class);
            startActivity(intent);
        });

        // Stock Tracker -> open StockTrackerActivity
        tileStockTracker.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StockTrackerActivity.class);
            startActivity(intent);
        });

        // Add Medicine -> open AddMedicineActivity
        tileAddMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMedicineActivity.class);
            startActivity(intent);
        });

        // View Medicine -> open MedicineListActivity
        tileViewMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicineListActivity.class);
            startActivity(intent);
        });

        // Contact Doctor -> open DoctorActivity
        tileContactDoctors.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DoctorActivity.class);
            startActivity(intent);
        });

        // ==== New/Updated wiring per your request ====

        // Settings tile (repurposed from AI Voice) -> Toast for now
        tileSettings.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show()
        );
        if (imgSettingsTile != null) {
            imgSettingsTile.setOnClickListener(v -> tileSettings.performClick());
        }

        // Purple \"Chat with AI\" button -> open ChatActivity
        if (fabMic != null) {
            fabMic.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            });
        }

        // NOTE: The white \"Chat with professional doctor\" card is intentionally left untouched
        //       (no click action) as you said you'll use it for something else later.
    }
}
