package com.example.medassist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class Db {
    private final AppDbHelper h;
    public Db(Context c){ h = new AppDbHelper(c); }

    /**
     * Adds a new medicine with its reminders and initial stock.
     * The initial stock is set once in the medicines table and logged in stock_log (no double increment).
     */
    public long addMedicine(String name, String dosage, String freq, String timesCsv, int stock){
        SQLiteDatabase db = h.getWritableDatabase();
        int safeStock = (stock <= 0 ? 1 : stock);

        // 1) Insert into medicines table
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("dosage", dosage);
        cv.put("freq", freq);
        cv.put("times", timesCsv);
        cv.put("stock", safeStock);
        long id = db.insert("medicines", null, cv);

        // 2) Insert reminders for each time
        for (String t : timesCsv.split(",")) {
            String timeTrim = t.trim();
            if (!timeTrim.isEmpty()) {
                ContentValues r = new ContentValues();
                r.put("medicine_id", id);
                r.put("time", timeTrim);
                r.put("enabled", 1);
                db.insert("reminders", null, r);
            }
        }

        // 3) Log the initial stock (for history only, not incrementing stock again)
        logInitialStock((int) id, safeStock);

        return id;
    }

    /**
     * Adds a stock change (positive or negative) and updates current stock.
     * Used by StockTrackerActivity, reminders, etc.
     */
    public void addStockLog(int medId, int delta){
        SQLiteDatabase db = h.getWritableDatabase();

        // 1) Insert stock change into stock_log
        ContentValues cv = new ContentValues();
        cv.put("medicine_id", medId);
        cv.put("change_amount", delta);
        cv.put("ts", System.currentTimeMillis());
        db.insert("stock_log", null, cv);

        // 2) Update main stock value in medicines table
        db.execSQL("UPDATE medicines SET stock = stock + ? WHERE id = ?", new Object[]{delta, medId});
    }

    /**
     * Manually sets the total stock for a medicine (absolute override).
     * Also logs the difference in stock_log for history.
     */
    public void setStockDirect(int medId, int newStock) {
        SQLiteDatabase db = h.getWritableDatabase();

        // clamp to >= 0
        int clamped = Math.max(0, newStock);

        // read current
        int oldStock = getStock(medId);
        int delta = clamped - oldStock;

        // log delta if any
        if (delta != 0) {
            ContentValues cv = new ContentValues();
            cv.put("medicine_id", medId);
            cv.put("change_amount", delta);
            cv.put("ts", System.currentTimeMillis());
            db.insert("stock_log", null, cv);
        }

        // set absolute value
        db.execSQL("UPDATE medicines SET stock = ? WHERE id = ?", new Object[]{clamped, medId});
    }

    /**
     * Log the initial stock ONLY in stock_log, without updating stock again.
     * Prevents double-counting during medicine creation.
     */
    private void logInitialStock(int medId, int amount) {
        SQLiteDatabase db = h.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("medicine_id", medId);
        cv.put("change_amount", amount);
        cv.put("ts", System.currentTimeMillis());
        db.insert("stock_log", null, cv);
    }

    public Cursor getActiveReminders(){
        SQLiteDatabase db = h.getReadableDatabase();
        return db.rawQuery(
                "SELECT r.id, r.time, m.name, m.dosage, m.id AS medId " +
                        "FROM reminders r JOIN medicines m ON r.medicine_id = m.id " +
                        "WHERE r.enabled = 1 ORDER BY r.time", null
        );
    }

    public Cursor getMedicines(){
        return h.getReadableDatabase().rawQuery("SELECT * FROM medicines WHERE active = 1", null);
    }

    public void disableRemindersForMedicine(int medId){
        h.getWritableDatabase().execSQL("UPDATE reminders SET enabled = 0 WHERE medicine_id = ?", new Object[]{medId});
    }

    public void deleteRemindersForMedicine(int medId){
        h.getWritableDatabase().execSQL("DELETE FROM reminders WHERE medicine_id = ?", new Object[]{medId});
    }

    public void deleteMedicine(int medId){
        h.getWritableDatabase().execSQL("DELETE FROM medicines WHERE id = ?", new Object[]{medId});
    }

    public int getStock(int medId){
        Cursor c = h.getReadableDatabase().rawQuery(
                "SELECT stock FROM medicines WHERE id = ?",
                new String[]{String.valueOf(medId)}
        );
        int s = 0;
        if (c.moveToFirst()) s = c.getInt(0);
        c.close();
        return s;
    }

    public void decrementStock(int medId, int amount){
        addStockLog(medId, -amount);
    }

    public List<String> getTimesForMed(int medId){
        Cursor c = h.getReadableDatabase().rawQuery(
                "SELECT times FROM medicines WHERE id = ?",
                new String[]{String.valueOf(medId)}
        );
        ArrayList<String> t = new ArrayList<>();
        if (c.moveToFirst()) {
            for (String s : c.getString(0).split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) t.add(trimmed);
            }
        }
        c.close();
        return t;
    }

    public int findMedIdByName(String name){
        Cursor c = h.getReadableDatabase().rawQuery(
                "SELECT id FROM medicines WHERE lower(name) = ?",
                new String[]{name.toLowerCase()}
        );
        int id = -1;
        if (c.moveToFirst()) id = c.getInt(0);
        c.close();
        return id;
    }

    // delete a medicine and all linked reminders & stock logs
    public void deleteMedicineCascade(int medId) {
        SQLiteDatabase dbx = h.getWritableDatabase();
        dbx.execSQL("DELETE FROM reminders WHERE medicine_id = ?", new Object[]{medId});
        dbx.execSQL("DELETE FROM stock_log WHERE medicine_id = ?", new Object[]{medId});
        dbx.execSQL("DELETE FROM medicines WHERE id = ?", new Object[]{medId});
    }
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = h.getReadableDatabase();
        return db.rawQuery(sql, selectionArgs);
    }
}
