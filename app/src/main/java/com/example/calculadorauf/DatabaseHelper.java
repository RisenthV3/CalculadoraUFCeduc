package com.example.calculadorauf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Userdata.db";
    public static final String TABLE_NAME = "userdata_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "TODAY_DATE";
    public static final String COL_3 = "USER_DATE";
    public static final String COL_4 = "API_VALUE";
    public static final String COL_5 = "RESULT_VALUE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,TODAY_DATE TEXT,USER_DATE TEXT,API_VALUE REAL,RESULT_VALUE REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String fechaHoy, String fechaUF, double valorUF, double valorResultado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, fechaHoy);
        contentValues.put(COL_3, fechaUF);
        contentValues.put(COL_4, valorUF);
        contentValues.put(COL_5, valorResultado);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

}
