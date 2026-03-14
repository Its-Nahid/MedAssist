package com.example.medassist.reminders;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.medassist.data.Db;

public class ReminderActionReceiver extends BroadcastReceiver {

    public static final String ACTION_TAKEN = "com.example.medassist.ACTION_TAKEN";
    public static final String ACTION_MISSED = "com.example.medassist.ACTION_MISSED";

    @Override
    public void onReceive(Context ctx, Intent i) {
        String action = i.getAction();
        int notificationId = i.getIntExtra("notificationId", -1);
        String medIdsCsv = i.getStringExtra("medIdsCsv");
        String medNamesCsv = i.getStringExtra("medNamesCsv");

        int[] ids = parseIds(medIdsCsv);
        String[] names = medNamesCsv != null ? medNamesCsv.split(",") : new String[0];

        Db db = new Db(ctx);

        if (ACTION_TAKEN.equals(action)) {
            for (int id : ids) {
                if (id > 0) db.decrementStock(id, 1); // -1 per medicine
            }
            Toast.makeText(ctx, (ids.length==1 ? "Marked taken: " + (names.length==1?names[0]:"") :
                    "Marked taken for " + ids.length + " medicines"), Toast.LENGTH_SHORT).show();
        } else if (ACTION_MISSED.equals(action)) {
            // No stock change on "Missed"
            Toast.makeText(ctx, (ids.length==1 ? "Marked missed: " + (names.length==1?names[0]:"") :
                    "Marked missed for " + ids.length + " medicines"), Toast.LENGTH_SHORT).show();
        }

        // Dismiss banner
        if (notificationId != -1) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(notificationId);
        }

        // Stop ringing
        Intent stop = new Intent(ctx, AlarmRingingService.class).setAction(AlarmRingingService.ACTION_STOP);
        ctx.startForegroundService(stop);
    }

    private int[] parseIds(String csv) {
        if (csv == null || csv.trim().isEmpty()) return new int[0];
        String[] p = csv.split(",");
        int[] out = new int[p.length];
        for (int i = 0; i < p.length; i++) {
            try { out[i] = Integer.parseInt(p[i].trim()); } catch (Exception e) { out[i] = -1; }
        }
        return out;
    }
}