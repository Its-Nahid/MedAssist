package com.example.medassist.reminders;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

import com.example.medassist.data.Db;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent i) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am != null && !am.canScheduleExactAlarms()) {
                return;
            }
        }
        Db db = new Db(ctx);
        Cursor c = db.getActiveReminders();
        int idxId = c.getColumnIndex("id");
        int idxTime = c.getColumnIndex("time");
        int idxName = c.getColumnIndex("name");
        int idxMed = c.getColumnIndex("medId");
        while(c.moveToNext()){
            int reminderId = c.getInt(idxId);
            String time = c.getString(idxTime);
            String name = c.getString(idxName);
            int medId = c.getInt(idxMed);
            AlarmHelper.scheduleDaily(ctx, reminderId, medId, name, time);
        }
        c.close();
    }
}
