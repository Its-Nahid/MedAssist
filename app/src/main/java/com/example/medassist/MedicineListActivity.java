package com.example.medassist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MedicineListActivity extends AppCompatActivity implements MedicineAdapter.OnMedicineClickListener {

    private static final String TAG = "MedicineListActivity";

    RecyclerView recyclerView;
    MedicineAdapter adapter;
    List<Medicine> medicineList;
    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);

        recyclerView = findViewById(R.id.recyclerMedicines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicineList = new ArrayList<>();

        adapter = new MedicineAdapter(medicineList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadMedicines();
        Log.d(TAG, "onCreate finished");
    }

    private void loadMedicines() {
        Log.d(TAG, "loadMedicines called");
        db.collection("medicines")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "loadMedicines: success. Docs found: " + queryDocumentSnapshots.size());
                    medicineList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            // --- THIS IS THE FIX ---
                            // Manual parsing to prevent crashes from mixed data types.
                            // This avoids the failing doc.toObject(Medicine.class) call.
                            Medicine med = new Medicine();
                            med.setMedicineId(doc.getId());
                            med.setName(doc.getString("name"));
                            med.setDescription(doc.getString("description"));
                            med.setShortUse(doc.getString("shortUse"));
                            med.setSideEffects(doc.getString("sideEffects"));
                            med.setPrecautions(doc.getString("precautions"));
                            med.setImageUrl(doc.getString("imageUrl"));

                            // Robustly get 'price' whether it's a String or a Number in the database
                            Object priceObject = doc.get("price");
                            if (priceObject instanceof Number) {
                                // If it's a number, convert it to a String
                                med.setPrice(String.valueOf(((Number) priceObject).longValue()));
                            } else {
                                // If it's already a String (or null), use it directly
                                med.setPrice((String) priceObject);
                            }

                            medicineList.add(med);
                            Log.d(TAG, "Added medicine: " + med.getName() + " with price: " + med.getPrice());

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing document: " + doc.getId(), e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter notified with " + medicineList.size() + " items.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadMedicines: failure", e);
                    Toast.makeText(MedicineListActivity.this, "Failed to load medicines: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onMedicineClick(Medicine medicine) {
        Log.d(TAG, "Clicked on: " + medicine.getName());
        Intent intent = new Intent(MedicineListActivity.this, MedicineDetailsActivity.class);
        intent.putExtra("medicineId", medicine.getMedicineId());
        startActivity(intent);
    }
}
