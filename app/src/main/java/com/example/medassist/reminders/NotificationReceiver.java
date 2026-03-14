package com.example.medassist.reminders;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.medassist.R;
import com.example.medassist.MedicineListActivity;
import com.example.medassist.data.Db;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        NotificationUtils.ensureReminderChannel(ctx);

        String time = intent.getStringExtra("time");
        int reqCode = intent.getIntExtra("reqCode", 0);
        if (time == null) return;

        // 1) Load all medicines due at this time
        Db db = new Db(ctx);
        List<Integer> medIds = new ArrayList<>();
        List<String> medNames = new ArrayList<>();

        Cursor c = db.rawQuery(
            "SELECT m.id, m.name " +
            "FROM medicines m " +
            "JOIN reminders r ON r.medicine_id = m.id " +
            "WHERE r.enabled=1 AND m.active=1 AND r.time = ?",
            new String[]{ time }
        );
        while (c.moveToNext()) {
            medIds.add(c.getInt(0));
            medNames.add(c.getString(1));
        }
        c.close();

        if (medIds.isEmpty()) {
            return;
        }

        String title = "Medicine reminder (" + time + ")";
        StringBuilder bigText = new StringBuilder();
        for (String n : medNames) {
            if (bigText.length() > 0) bigText.append('\n');
            bigText.append("• ").append(n);
        }

        int notificationId = Math.abs(("N:" + time).hashCode());

        Intent taken = new Intent(ctx, ReminderActionReceiver.class)
                .setAction(ReminderActionReceiver.ACTION_TAKEN)
                .putExtra("time", time)
                .putExtra("notificationId", notificationId)
                .putExtra("medIdsCsv", joinCsv(medIds))
                .putExtra("medNamesCsv", joinCsv(medNames));
        PendingIntent piTaken = PendingIntent.getBroadcast(
                ctx, notificationId + 11, taken,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent missed = new Intent(ctx, ReminderActionReceiver.class)
                .setAction(ReminderActionReceiver.ACTION_MISSED)
                .putExtra("time", time)
                .putExtra("notificationId", notificationId)
                .putExtra("medIdsCsv", joinCsv(medIds))
                .putExtra("medNamesCsv", joinCsv(medNames));
        PendingIntent piMissed = PendingIntent.getBroadcast(
                ctx, notificationId + 22, missed,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent contentPi = PendingIntent.getActivity(
                ctx, notificationId,
                new Intent(ctx, MedicineListActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, NotificationUtils.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle(title)
                .setContentText(medNames.size() == 1 ? medNames.get(0) : (medNames.size() + " medicines due"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
                .setContentIntent(contentPi)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_check, "Taken", piTaken)
                .addAction(R.drawable.ic_close, "Missed", piMissed)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificationId, n.build());

        // Start ringing once
        Intent svc = new Intent(ctx, AlarmRingingService.class).setAction(AlarmRingingService.ACTION_START);
        svc.putExtra("time", time);
        svc.putExtra("notificationId", notificationId);
        ctx.startForegroundService(svc);

        // Re-schedule next day
        AlarmHelper.scheduleNextDay(ctx, reqCode, -1, "", time);
    }

    private static String joinCsv(List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
            if (sb.length() > 0) sb.append(',');
            sb.append(o.toString());
        }
        return sb.toString();
    }
}