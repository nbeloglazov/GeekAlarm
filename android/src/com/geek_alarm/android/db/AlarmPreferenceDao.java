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
    private static final String TABLE_NAME = "alarms";

    /**
     * Creates table for alarms preferences. Must be called only once, when application starts first time.
     * @param db database that contains geekalarm's data.
     */
    public void initialize(SQLiteDatabase db) {
        String request = String.format(
                "CREATE TABLE %s " +
                        "(%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER);",
                TABLE_NAME, ID, DAYS, HOUR, MINUTE, ENABLED);
        db.execSQL(request);
    }

    public void add(AlarmPreference preference) {
        ContentValues values = toContentValues(preference);
        long id = DBOpenHelper.getInstance().getWritableDatabase().insert(TABLE_NAME, null, values);
        preference.setId((int)id);
    }

    public void update(AlarmPreference preference) {
        ContentValues values = toContentValues(preference);
        String[] args = {String.valueOf(preference.getId())};
        DBOpenHelper.getInstance().getWritableDatabase().update(TABLE_NAME, values,
                "id=?", args);
    }

    public List<AlarmPreference> getAll() {
        String query = String.format("SELECT * FROM %s", TABLE_NAME);
        Cursor cursor = DBOpenHelper.getInstance().getReadableDatabase().rawQuery(query, null);
        List<AlarmPreference> preferences = new ArrayList<AlarmPreference>(cursor.getCount());
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            preferences.add(read(cursor));
        }
        cursor.close();
        return preferences;
    }

    public AlarmPreference findById(int id) {
        String query = String.format("SELECT * FROM %s WHERE %s = %d", TABLE_NAME, ID, id);
        Cursor cursor = DBOpenHelper.getInstance().getReadableDatabase().rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            return null;
        }
        AlarmPreference preference = read(cursor);
        cursor.close();
        return preference;
    }

    public void delete(AlarmPreference preference) {
        String[] args = {String.valueOf(preference.getId())};
        DBOpenHelper.getInstance().getWritableDatabase().delete(TABLE_NAME, "id = ?", args);
    }

    private AlarmPreference read(Cursor cursor) {
        AlarmPreference preference = new AlarmPreference();
        preference.setId(cursor.getInt(0));
        preference.setDays(cursor.getInt(1));
        preference.setHour(cursor.getInt(2));
        preference.setMinute(cursor.getInt(3));
        preference.setEnabled(cursor.getInt(4) == 1);
        return preference;
    }

    private ContentValues toContentValues(AlarmPreference preference) {
        ContentValues values = new ContentValues();
        values.put(DAYS, preference.getDays());
        values.put(HOUR, preference.getHour());
        values.put(MINUTE, preference.getMinute());
        values.put(ENABLED, preference.isEnabled() ? 1 : 0);
        return values;
    }
}
