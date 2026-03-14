
package com.example.medassist.stock;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.medassist.R;
import com.example.medassist.data.Db;

public class DailyStockCheckReceiver extends BroadcastReceiver {
    public static void setupDaily(Context ctx){
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, DailyStockCheckReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 999999, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long now = System.currentTimeMillis();
        long oneDay = AlarmManager.INTERVAL_DAY;
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, now + oneDay, oneDay, pi);
    }

    @Override public void onReceive(Context ctx, Intent i) {
        Db db = new Db(ctx);
        Cursor meds = db.getMedicines();
        int idxId=meds.getColumnIndex("id"), idxName=meds.getColumnIndex("name"),
            idxTimes=meds.getColumnIndex("times"), idxStock=meds.getColumnIndex("stock");
        while(meds.moveToNext()){
            int medId = meds.getInt(idxId);
            String name = meds.getString(idxName);
            int perDay = meds.getString(idxTimes).split(",").length;
            int stock = meds.getInt(idxStock);
            int daysLeft = perDay==0? 0 : stock / perDay;
            if(daysLeft <= 3){
                String chId = "stock_channel";
                if(Build.VERSION.SDK_INT >= 26){
                    NotificationChannel ch = new NotificationChannel(chId, "Stock Alerts", NotificationManager.IMPORTANCE_DEFAULT);
                    ((NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);
                }
                NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, chId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Low stock: " + name)
                        .setContentText("About " + daysLeft + " day(s) left. You can add stock.")
                        .setAutoCancel(true);
                ((NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE))
                        .notify(700000 + medId, n.build());
            }
        }
        meds.close();
    }
}
