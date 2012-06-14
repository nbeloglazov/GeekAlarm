package com.geek_alarm.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.geek_alarm.android.AlarmPreference;

import java.util.ArrayList;
import java.util.List;

public enum AlarmPreferenceDao {

    INSTANCE;

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String DAYS = "days";
    private static final String ID = "id";
    private static final String ENABLED = "enabled";
    private static final String ALARM_TABLE = "alarms";

    /**
     * Creates table for alarms preferences. Must be called only once, when application starts first time.
     * @param db database that contains geekalarm's data.
     */
    public void initialize(SQLiteDatabase db) {
        String request = String.format(
                "CREATE TABLE %s " +
                        "(%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER);",
                ALARM_TABLE, ID, DAYS, HOUR, MINUTE, ENABLED);
        db.execSQL(request);
    }

    public void addAlarmPreference(AlarmPreference preference) {
        ContentValues values = new ContentValues();
        values.put(DAYS, preference.getDays());
        values.put(HOUR, preference.getHour());
        values.put(MINUTE, preference.getMinute());
        values.put(ENABLED, preference.isEnabled() ? 1 : 0);
        long id = DBOpenHelper.getInstance().getWritableDatabase().insert(ALARM_TABLE, null, values);
        preference.setId((int)id);
    }

    public void updateAlarmPreference(AlarmPreference preference) {
        ContentValues values = new ContentValues();
        values.put(DAYS, preference.getDays());
        values.put(HOUR, preference.getHour());
        values.put(MINUTE, preference.getMinute());
        values.put(ENABLED, preference.isEnabled() ? 1 : 0);
        String[] args = {String.valueOf(preference.getId())};
        DBOpenHelper.getInstance().getWritableDatabase().update(ALARM_TABLE, values,
                "id=?", args);
    }

    private AlarmPreference readAlarmPreference(Cursor cursor) {
        AlarmPreference preference = new AlarmPreference();
        preference.setId(cursor.getInt(0));
        preference.setDays(cursor.getInt(1));
        preference.setHour(cursor.getInt(2));
        preference.setMinute(cursor.getInt(3));
        preference.setEnabled(cursor.getInt(4) == 1);
        return preference;
    }

    public List<AlarmPreference> getAlarmPreferences() {
        String query = String.format("SELECT * FROM %s", ALARM_TABLE);
        Cursor cursor = DBOpenHelper.getInstance().getReadableDatabase().rawQuery(query, null);
        List<AlarmPreference> preferences = new ArrayList<AlarmPreference>(cursor.getCount());
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            preferences.add(readAlarmPreference(cursor));
        }
        cursor.close();
        return preferences;
    }

    public AlarmPreference getAlarmPreference(int id) {
        String query = String.format("SELECT * FROM %s WHERE %s = %d", ALARM_TABLE, ID, id);
        Cursor cursor = DBOpenHelper.getInstance().getReadableDatabase().rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            return null;
        }
        AlarmPreference preference = readAlarmPreference(cursor);
        cursor.close();
        return preference;
    }

    public void removeAlarmPreference(AlarmPreference preference) {
        String[] args = {String.valueOf(preference.getId())};
        DBOpenHelper.getInstance().getWritableDatabase().delete(ALARM_TABLE, "id = ?", args);
    }
}
