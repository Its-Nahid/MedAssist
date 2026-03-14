package com.example.medassist.reminders;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.medassist.R;
import com.example.medassist.MedicineListActivity;

public class AlarmRingingService extends Service {
    public static final String ACTION_START = "com.example.medassist.ACTION_START_RING";
    public static final String ACTION_STOP  = "com.example.medassist.ACTION_STOP_RING";

    private MediaPlayer player;
    private Vibrator vibrator;
    private Handler stopHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationUtils.ensureReminderChannel(this);

        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            try { stopForeground(true); } catch (Exception ignored) { }
            stopEffects();
            stopSelf();
            return START_NOT_STICKY;
        }

        Intent open = new Intent(this, MedicineListActivity.class);
        PendingIntent contentPi = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new NotificationCompat.Builder(this, NotificationUtils.SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Alarm ringing")
                .setContentText("Tap Taken or Missed to stop")
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setContentIntent(contentPi)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1001, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1001, n);
        }

        startEffects();
        return START_STICKY;
    }

    private void startEffects() {
        stopEffects();
        boolean ok = startCustomAudio();
        if (!ok) ok = startDefaultTone();
        startVibration();
        if (stopHandler == null) stopHandler = new Handler(Looper.getMainLooper());
        stopHandler.removeCallbacksAndMessages(null);
        stopHandler.postDelayed(() -> {
            try { stopForeground(true); } catch (Exception ignored) { }
            stopEffects();
            stopSelf();
        }, 60_000);
    }

    private boolean startCustomAudio() {
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.med_alarm);
            if (afd == null) return false;
            player = new MediaPlayer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
            }
            player.setLooping(true);
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            player.prepare();
            player.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean startDefaultTone() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (uri == null) return false;
            player = new MediaPlayer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
            }
            player.setLooping(true);
            player.setDataSource(this, uri);
            player.prepare();
            player.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void startVibration() {
        try {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator == null) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = new long[] {0, 600, 300};
                VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(new long[] {0, 600, 300}, 0);
            }
        } catch (Exception ignored) {}
    }

    private void stopEffects() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) { }
            try { player.release(); } catch (Exception ignored) { }
            player = null;
        }
        if (vibrator != null) {
            try { vibrator.cancel(); } catch (Exception ignored) { }
            vibrator = null;
        }
    }

    @Override public void onDestroy() {
        stopEffects();
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
