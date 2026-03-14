package com.example.medassist;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class DoctorActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    DoctorAdapter adapter;
    List<Doctor> doctorList;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors);

        recyclerView = findViewById(R.id.recyclerViewDoctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(this, doctorList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadDoctors();
    }

    private void loadDoctors() {
        db.collection("doctors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    doctorList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Doctor doctor = doc.toObject(Doctor.class);
                        doctorList.add(doctor);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
