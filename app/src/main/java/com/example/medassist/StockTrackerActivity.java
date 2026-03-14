package com.example.medassist;

import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

public class StockTrackerActivity extends AppCompatActivity {
    ListView list;
    Db db;

    // Backing data for adapter (keep them in sync by index)
    ArrayList<Integer> medIds = new ArrayList<>();
    ArrayList<String> names   = new ArrayList<>();
    ArrayList<String> stocks  = new ArrayList<>();
    ArrayList<String> doses   = new ArrayList<>(); // optional under title

    ArrayAdapter<String> adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_tracker_activity);
        list = findViewById(R.id.list);
        db = new Db(this);

        buildAdapter();
        load(); // populate + attach adapter

        // Whole row tap = add stock (quick add)
        list.setOnItemClickListener((a, v, pos, id) -> addStockDialog(medIds.get(pos)));

        // Long-press delete
        list.setOnItemLongClickListener((a, v, pos, id) -> {
            confirmDelete(pos);
            return true;
        });
    }

    private void buildAdapter() {
        adapter = new ArrayAdapter<String>(this, R.layout.item_med_card, R.id.med_name, names) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                TextView tvName   = v.findViewById(R.id.med_name);
                TextView tvSub    = v.findViewById(R.id.med_short_use);
                TextView tvRightL = v.findViewById(R.id.right_label);
                TextView tvRightV = v.findViewById(R.id.right_value);
                ImageView ivIcon  = v.findViewById(R.id.med_image);
                ImageButton btnDel= v.findViewById(R.id.btnDelete);

                // Bind data
                tvName.setText(names.get(position));
                tvSub.setText(safe(doses.get(position)));
                tvRightL.setText("Stock");
                tvRightV.setText(stocks.get(position));
                ivIcon.setImageResource(R.mipmap.ic_launcher);

                // Make the "Stock" label and value tappable → open edit dialog
                View.OnClickListener stockClick = vv -> addStockDialog(medIds.get(position));
                tvRightL.setOnClickListener(stockClick);
                tvRightV.setOnClickListener(stockClick);

                // Optional: also allow long-press on stock number to open edit
                tvRightV.setOnLongClickListener(vv -> {
                    addStockDialog(medIds.get(position));
                    return true;
                });

                // Delete button
                if (btnDel != null) {
                    btnDel.setVisibility(View.VISIBLE);
                    btnDel.setOnClickListener(view -> confirmDelete(position));
                } else {
                    Toast.makeText(StockTrackerActivity.this, "btnDelete not found in layout", Toast.LENGTH_SHORT).show();
                }

                return v;
            }
        };
    }

    private void load() {
        // Clear current data
        medIds.clear(); names.clear(); stocks.clear(); doses.clear();

        Cursor c = db.getMedicines();
        int idxId    = c.getColumnIndex("id");
        int idxName  = c.getColumnIndex("name");
        int idxStock = c.getColumnIndex("stock");
        int idxDose  = c.getColumnIndex("dosage"); // ok if -1

        while (c.moveToNext()) {
            medIds.add(c.getInt(idxId));
            names.add(c.getString(idxName));
            stocks.add(String.valueOf(c.getInt(idxStock)));
            doses.add(idxDose >= 0 ? safe(c.getString(idxDose)) : "");
        }
        c.close();

        // Attach (first time) or refresh
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void confirmDelete(int position) {
        final int medId = medIds.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete medicine?")
                .setMessage("This will remove the medicine, its reminders, and stock history.")
                .setPositiveButton("Delete", (d, w) -> {
                    // DB delete cascade
                    db.deleteMedicineCascade(medId);

                    // Remove from lists immediately for instant UI update
                    medIds.remove(position);
                    names.remove(position);
                    stocks.remove(position);
                    doses.remove(position);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Dialog that supports +N / -N / =TOTAL patterns */
    private void addStockDialog(int medId) {
        final EditText input = new EditText(this);
        input.setHint("Enter +10 to add, -5 to remove, or =30 to set");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this)
                .setTitle("Update stock")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    String raw = input.getText().toString().trim();
                    if (raw.isEmpty()) return;

                    try {
                        if (raw.startsWith("=")) {
                            // Manual override (set absolute total)
                            int newStock = Integer.parseInt(raw.substring(1).trim());
                            db.setStockDirect(medId, newStock);
                            Toast.makeText(this, "Stock set to " + newStock, Toast.LENGTH_SHORT).show();
                        } else {
                            // Relative change (+/- or plain number means +)
                            int delta = Integer.parseInt(raw);
                            db.addStockLog(medId, delta);
                            Toast.makeText(this,
                                    (delta >= 0 ? "Added +" + delta : "Removed " + (-delta)),
                                    Toast.LENGTH_SHORT).show();
                        }
                        load(); // refresh list
                    } catch (NumberFormatException ex) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String safe(String s) { return s == null ? "" : s; }
}
