package com.parse.starter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelpper";
    private static final String TABLE_NAME = "message_table";
    private static final String COL1 = "ID";
    private static final String COL2 = "message";
    private static final String DB_NAME = "App.db";
    private static final int version = 1;

    public DatabaseHelper(Context context)
    {
        super(context , DB_NAME , null , version);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE "+ TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, "+ COL2 +" TEXT)";
        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public boolean addData(String message )
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2 , message);
        Log.i("content" , message);
        long result = db.insert( TABLE_NAME , null , contentValues);
        //check if data is inserted correctly
        if(result == -1)
        {
            // was not inserted correctly
            return false;
        }else
        {
            return true;
        }

    }
    public Cursor getAllData()
    {
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor data = database.rawQuery("SELECT * FROM "+TABLE_NAME,null);
        return data;
    }




}
