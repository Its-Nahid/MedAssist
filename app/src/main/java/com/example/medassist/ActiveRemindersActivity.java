package com.example.medassist;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medassist.data.Db;

import java.util.ArrayList;

public class ActiveRemindersActivity extends AppCompatActivity {

    ListView list;
    Db db;

    ArrayList<Integer> medIds = new ArrayList<>();
    ArrayList<String> names   = new ArrayList<>();
    ArrayList<String> doses   = new ArrayList<>();
    ArrayList<String> times   = new ArrayList<>();

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_reminders);

        list = findViewById(R.id.list);
        db = new Db(this);

        buildAdapter();
        load();
    }

    private void buildAdapter() {
        adapter = new ArrayAdapter<String>(this, R.layout.item_med_card, R.id.med_name, names) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                TextView tvName   = v.findViewById(R.id.med_name);
                TextView tvSub    = v.findViewById(R.id.med_short_use);
                TextView tvFooter = v.findViewById(R.id.med_price);
                TextView tvRightL = v.findViewById(R.id.right_label);
                TextView tvRightV = v.findViewById(R.id.right_value);
                ImageView ivIcon  = v.findViewById(R.id.med_image);
                ImageButton btnDel= v.findViewById(R.id.btnDelete);

                // Medicine name
                tvName.setText(names.get(position));

                // --- dosage line (hide if default one) ---
                String doseRaw = doses.get(position);
                String doseDisplay = formatDoseForDisplay(doseRaw);

                if (doseDisplay.isEmpty()) {
                    tvSub.setText("");
                    tvSub.setVisibility(View.GONE);   // collapse line if default one
                } else {
                    tvSub.setVisibility(View.VISIBLE);
                    tvSub.setText(doseDisplay);
                }

                // Footer: "Active reminder" in green
                tvFooter.setText("Active reminder");
                tvFooter.setTextColor(Color.parseColor("#4CAF50"));

                // Right side: "Next" time label
                tvRightL.setText("Next");
                tvRightV.setText(times.get(position));

                ivIcon.setImageResource(R.mipmap.ic_launcher);

                // 🗑️ Delete button
                if (btnDel != null) {
                    btnDel.setVisibility(View.VISIBLE);
                    btnDel.setOnClickListener(view -> {
                        int medId = medIds.get(position);
                        new AlertDialog.Builder(ActiveRemindersActivity.this)
                                .setTitle("Delete reminder?")
                                .setMessage("Remove all reminders for this medicine?")
                                .setPositiveButton("Delete", (d, w) -> {
                                    db.deleteRemindersForMedicine(medId);
                                    Toast.makeText(ActiveRemindersActivity.this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                                    load(); // refresh
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });
                }

                return v;
            }
        };
    }

    private void load() {
        medIds.clear();
        names.clear();
        doses.clear();
        times.clear();

        Cursor c = db.getActiveReminders();
        int idxName = c.getColumnIndex("name");
        int idxTime = c.getColumnIndex("time");
        int idxDose = c.getColumnIndex("dosage");
        int idxMed  = c.getColumnIndex("medId");

        while (c.moveToNext()) {
            names.add(c.getString(idxName));
            times.add(c.getString(idxTime));

            String dose = "";
            if (idxDose >= 0) {
                String raw = c.getString(idxDose);
                if (raw != null) dose = raw.trim();
            }
            doses.add(dose);
            medIds.add(c.getInt(idxMed));
        }
        c.close();

        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    // --- Helper to hide default "1" dose and format others nicely ---
    private String formatDoseForDisplay(@Nullable String raw) {
        if (raw == null) return "";

        String s = raw.trim();
        if (s.isEmpty()) return "";

        String lower = s.toLowerCase();
        lower = lower.replaceAll("\\s+", " ").trim();

        // Case A: pure number (e.g., "1", "2", "500")
        if (lower.matches("^\\d+$")) {
            int val = Integer.parseInt(lower);
            if (val == 1) return "";                 // hide default one
            if (val >= 2 && val <= 20) return val + " pills"; // plural
            if (val >= 25) return val + " mg";       // big number => mg
            return val + " pills";
        }

        // Case B: "500mg", "500 mg", "5ml", "5 ml", "100 mcg", "1 g"
        if (lower.matches("^\\d+\\s*(mg|ml|mcg|g)$")) {
            return lower.replaceAll("\\s*(mg|ml|mcg|g)$", " $1");
        }

        // Case C: "2 pill", "3 pills", "2 tablets", etc.
        if (lower.matches("^\\d+\\s*(pill|pills|tablet|tablets|cap|caps|capsule|capsules)$")) {
            String[] parts = lower.split(" ");
            int val = Integer.parseInt(parts[0]);
            if (val == 1) return "";                   // hide default one
            return val + " pills";
        }

        // Case D: "pill", "tablet", etc. without number -> treat as 1
        if (lower.matches("^(pill|pills|tablet|tablets|cap|caps|capsule|capsules)$")) {
            return ""; // treat as 1 -> hide
        }

        // Otherwise, show as-is
        return s;
    }
}
