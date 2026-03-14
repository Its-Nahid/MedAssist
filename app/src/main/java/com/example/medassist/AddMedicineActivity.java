
package com.example.medassist;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medassist.data.Db;
import com.example.medassist.reminders.AlarmHelper;
import com.example.medassist.stock.DailyStockCheckReceiver;
import com.example.medassist.voice.VoiceAssistant;

public class AddMedicineActivity extends AppCompatActivity {
    EditText etName, etDosage, etTimes, etStock;
    Db db; VoiceAssistant va;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_medicine_activity);

        db = new Db(this);
        DailyStockCheckReceiver.setupDaily(this);

        etName = findViewById(R.id.etName);
        etDosage = findViewById(R.id.etDosage);
        etTimes = findViewById(R.id.etTimes);
        etStock = findViewById(R.id.etStock);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnActive = findViewById(R.id.btnActiveReminders);
        ImageButton btnMic = findViewById(R.id.btnMic);

        etTimes.setOnClickListener(v -> new TimePickerDialog(this, (d,H,M)->{
            String cur = etTimes.getText().toString().trim();
            String t = String.format("%02d:%02d", H, M);
            etTimes.setText(cur.isEmpty()? t : cur + "," + t);
        }, 8, 0, true).show());

        btnSave.setOnClickListener(v -> save());
        btnActive.setOnClickListener(v -> startActivity(new android.content.Intent(this, ActiveRemindersActivity.class)));
        btnMic.setOnClickListener(v -> {
            if(va==null) va=new VoiceAssistant(this, db);
            va.start();
        });
    }

    private void save(){
        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String times = etTimes.getText().toString().trim();
        int stock = safeInt(etStock.getText().toString().trim());
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(times)){
            Toast.makeText(this, "Name & time required", Toast.LENGTH_SHORT).show(); return;
        }
        long medId = db.addMedicine(name, dosage.isEmpty()? "1 pill": dosage, "Daily", times, stock<=0?1:stock);
        for(String t : times.split(",")){
            int req = Math.abs(("T:" + t.trim()).hashCode());
            AlarmHelper.scheduleDaily(this, req, (int)medId, name, t.trim());
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private int safeInt(String s){ try{ return Integer.parseInt(s);}catch(Exception e){ return 1; } }

    @Override protected void onActivityResult(int req, int res, android.content.Intent data){
        super.onActivityResult(req,res,data);
        if(va!=null) va.onActivityResult(req,res,data);
    }
}
