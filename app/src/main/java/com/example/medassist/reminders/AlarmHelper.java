package com.example.medassist.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class AlarmHelper {

    public static void scheduleDaily(Context ctx, int requestCode, int medId, String medName, String timeHHmm) {
        String[] p = timeHHmm.trim().split(":");
        int h = Integer.parseInt(p[0].trim());
        int m;
        String minStr = p[1].trim();
        if (minStr.length() > 2) {
            String onlyMin = minStr.substring(0,2);
            m = Integer.parseInt(onlyMin);
            String lower = minStr.toLowerCase();
            if (lower.contains("pm") && h < 12) h += 12;
            if (lower.contains("am") && h == 12) h = 0;
        } else {
            m = Integer.parseInt(minStr);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent i = new Intent(ctx, NotificationReceiver.class)
                .setAction("com.example.medassist.ACTION_REMINDER_FIRE");
        i.putExtra("reqCode", requestCode);
        i.putExtra("medId", medId);
        i.putExtra("medName", medName);
        i.putExtra("time", timeHHmm);

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                requestCode,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        boolean canExact = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canExact = am.canScheduleExactAlarms();
        }

        if (canExact) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), pi);
                am.setAlarmClock(info, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }
        } else {
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }
    }

    public static void scheduleNextDay(Context ctx, int requestCode, int medId, String medName, String timeHHmm) {
        scheduleDaily(ctx, requestCode, medId, medName, timeHHmm);
    }
}
