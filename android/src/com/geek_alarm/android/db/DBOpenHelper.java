package com.geek_alarm.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.geek_alarm.android.Application;

public final class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "geekalarm";


    private static final int DATABASE_VERSION = 2;

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        AlarmPreferenceDao.INSTANCE.initialize(db);
        onUpgrade(db, 1, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 1:
                    TaskTypeDao.INSTANCE.initialize(db);
                    break;
            }
        }
    }

    private static SQLiteOpenHelper helper = new DBOpenHelper(Application.getContext());

    public static SQLiteOpenHelper getInstance() {
        return helper;
    }

}
