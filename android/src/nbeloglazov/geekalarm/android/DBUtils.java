package nbeloglazov.geekalarm.android;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public final class DBUtils {
    
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String DAYS = "days";
    private static final String ID = "id";
    private static final String ENABLED = "enabled";
    private static final String ALARM_TABLE = "alarms";

    private static class DBOpenHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;

        public DBOpenHelper(Context context) {
            super(context, "geekalarm", null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String request = String.format(
                    "CREATE TABLE %s " + 
                    "(%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER);",
                    ALARM_TABLE, ID, DAYS, HOUR, MINUTE, ENABLED);
            db.execSQL(request);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // No upgrading right now.
        }
    }

    private static SQLiteOpenHelper helper;

    private static SQLiteOpenHelper getHelper() {
        if (helper == null) {
            helper = new DBOpenHelper(Application.getContext());
        }
        return helper;
    }
    
    public static void addAlarmPreference(AlarmPreference preference) {
        ContentValues values = new ContentValues();
        values.put(DAYS, preference.getDays());
        values.put(HOUR, preference.getHour());
        values.put(MINUTE, preference.getMinute());
        values.put(ENABLED, preference.isEnabled() ? 1 : 0);
        long id = getHelper().getWritableDatabase().insert(ALARM_TABLE, null, values);
        preference.setId((int)id);
    }
    
    public static void updateAlarmPreference(AlarmPreference preference) {
        ContentValues values = new ContentValues();
        values.put(DAYS, preference.getDays());
        values.put(HOUR, preference.getHour());
        values.put(MINUTE, preference.getMinute());
        values.put(ENABLED, preference.isEnabled() ? 1 : 0);
        String[] args = {String.valueOf(preference.getId())};
        getHelper().getWritableDatabase().update(ALARM_TABLE, values,
                "id=?", args);
    }
    
    private static AlarmPreference readAlarmPreference(Cursor cursor) {
        AlarmPreference preference = new AlarmPreference();
        preference.setId(cursor.getInt(0));
        preference.setDays(cursor.getInt(1));
        preference.setHour(cursor.getInt(2));
        preference.setMinute(cursor.getInt(3));
        preference.setEnabled(cursor.getInt(4) == 1);
        return preference;
    }
    
    public static List<AlarmPreference> getAlarmPreferences() {
        String query = String.format("SELECT * FROM %s", ALARM_TABLE);
        Cursor cursor = getHelper().getReadableDatabase().rawQuery(query, null);
        List<AlarmPreference> preferences = new ArrayList<AlarmPreference>(cursor.getCount());
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            preferences.add(readAlarmPreference(cursor));
        }
        cursor.close();
        return preferences;
    }
    
    public static AlarmPreference getAlarmPreference(int id) {
        String query = String.format("SELECT * FROM %s WHERE %s = %d", ALARM_TABLE, ID, id);
        Cursor cursor = getHelper().getReadableDatabase().rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            return null;
        }
        AlarmPreference preference = readAlarmPreference(cursor);
        cursor.close();
        return preference;
    }
    
    public static void removeAlarmPreference(AlarmPreference preference) {
        String[] args = {String.valueOf(preference.getId())};
        getHelper().getWritableDatabase().delete(ALARM_TABLE, "id = ?", args);
    }
}
