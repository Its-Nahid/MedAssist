
package com.example.medassist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "medassist.db";
    private static final int DB_VER = 1;

    public AppDbHelper(Context ctx){ super(ctx, DB_NAME, null, DB_VER); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS medicines(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "dosage TEXT," +
                "freq TEXT," +
                "times TEXT," +
                "stock INTEGER DEFAULT 1," +
                "active INTEGER DEFAULT 1" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS reminders(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "medicine_id INTEGER," +
                "time TEXT," +
                "enabled INTEGER DEFAULT 1," +
                "FOREIGN KEY(medicine_id) REFERENCES medicines(id) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS stock_log(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "medicine_id INTEGER," +
                "change_amount INTEGER," +
                "ts INTEGER" +
                ");");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) { }
}
