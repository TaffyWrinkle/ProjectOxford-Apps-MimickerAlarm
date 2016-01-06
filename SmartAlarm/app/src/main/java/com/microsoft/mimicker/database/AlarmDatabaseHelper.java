package com.microsoft.mimicker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.microsoft.mimicker.database.AlarmDbSchema.AlarmTable;

public class AlarmDatabaseHelper extends SQLiteOpenHelper{
    private static final String TAG = "AlarmDatabaseHelper";
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "alarmDatabase.db";

    public AlarmDatabaseHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "create table " + AlarmTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                AlarmTable.Columns.UUID + ", " +
                AlarmTable.Columns.TITLE + ", " +
                AlarmTable.Columns.ENABLED + ", " +
                AlarmTable.Columns.HOUR + ", " +
                AlarmTable.Columns.MINUTE + ", " +
                AlarmTable.Columns.DAYS + ", " +
                AlarmTable.Columns.TONE + ", " +
                AlarmTable.Columns.VIBRATE + ", " +
                AlarmTable.Columns.TONGUE_TWISTER + ", " +
                AlarmTable.Columns.COLOR_CAPTURE + ", " +
                AlarmTable.Columns.EXPRESS_YOURSELF + ", " +
                AlarmTable.Columns.NEW +
                ")"
        );
    }

    // TODO: Clean this up before publish
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.VIBRATE);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.TONGUE_TWISTER);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.COLOR_CAPTURE);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.EXPRESS_YOURSELF);
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.NEW);
                break;
            case 2:
                db.execSQL("ALTER TABLE " + AlarmTable.NAME + " ADD COLUMN " + AlarmTable.Columns.NEW);

        }
    }
}