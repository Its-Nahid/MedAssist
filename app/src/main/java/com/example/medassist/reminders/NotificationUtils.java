package com.example.medassist.reminders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import com.example.medassist.R;

public class NotificationUtils {
    public static final String CHANNEL_ID = "medassist_reminders";
    public static final String SERVICE_CHANNEL_ID = "medassist_ringing";

    public static void ensureReminderChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            Uri soundUri = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + R.raw.med_alarm);
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Medicine Reminders", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Alarms and notifications for taking medicine");
            ch.setSound(soundUri, aa);
            ch.enableVibration(true);
            ch.enableLights(true);
            nm.createNotificationChannel(ch);
        }
        if (nm.getNotificationChannel(SERVICE_CHANNEL_ID) == null) {
            NotificationChannel ch2 = new NotificationChannel(
                    SERVICE_CHANNEL_ID, "Ringing Service", NotificationManager.IMPORTANCE_LOW);
            ch2.setDescription("Foreground service while alarm is ringing");
            nm.createNotificationChannel(ch2);
        }
    }
}
