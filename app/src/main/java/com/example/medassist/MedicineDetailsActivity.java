package com.example.medassist;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MedicineDetailsActivity extends AppCompatActivity {
    private static final String TAG = "MedicineDetailsActivity";

    TextView name, description, uses, sideEffects, precautions;
    ImageView image;
    Button saveBtn;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_details);

        image = findViewById(R.id.detail_image);
        name = findViewById(R.id.detail_name);
        description = findViewById(R.id.detail_description);
        uses = findViewById(R.id.detail_uses);
        sideEffects = findViewById(R.id.detail_side_effects);
        precautions = findViewById(R.id.detail_precautions);
        saveBtn = findViewById(R.id.btn_save);

        db = FirebaseFirestore.getInstance();

        // Get the medicineId from the Intent
        String medicineId = getIntent().getStringExtra("medicineId");

        if (medicineId != null && !medicineId.isEmpty()) {
            Log.d(TAG, "Received medicineId: " + medicineId);
            loadMedicineDetails(medicineId);
        } else {
            Log.e(TAG, "No medicineId found in Intent.");
            Toast.makeText(this, "Error: Medicine ID not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if there's no ID
        }

        saveBtn.setOnClickListener(v -> {
            // TODO: Save medicine to user favorites or Firestore
        });
    }

    private void loadMedicineDetails(String medicineId) {
        DocumentReference docRef = db.collection("medicines").document(medicineId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());

                // Populate the views with the data from Firestore
                name.setText(documentSnapshot.getString("name"));
                description.setText(documentSnapshot.getString("description"));
                uses.setText(documentSnapshot.getString("shortUse"));
                sideEffects.setText(documentSnapshot.getString("sideEffects"));
                precautions.setText(documentSnapshot.getString("precautions"));

                // Load the image using Glide
                Glide.with(this)
                     .load(documentSnapshot.getString("imageUrl"))
                     .into(image);

            } else {
                Log.e(TAG, "No such document with ID: " + medicineId);
                Toast.makeText(this, "Error: Medicine details not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting medicine details", e);
            Toast.makeText(this, "Failed to load details.", Toast.LENGTH_SHORT).show();
        });
    }
}
