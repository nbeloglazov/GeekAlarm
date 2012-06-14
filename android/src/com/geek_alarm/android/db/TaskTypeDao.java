package com.geek_alarm.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.geek_alarm.android.AlarmPreference;
import com.geek_alarm.android.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;

public enum TaskTypeDao {

    INSTANCE;

    private static String TABLE_NAME = "task_types";
    private static String TYPE = "type";
    private static String NAME = "name";
    private static String LEVEL = "level";
    private static String DESCRIPTION = "description";


    /**
     * Creates table for task types. Must be called only once, when application starts first time.
     * @param db database that contains geekalarm's data.
     */
    public void initialize(SQLiteDatabase db) {
        String request = String.format(
                "CREATE TABLE %s (%s TEXT PRIMARY KEY, %s TEXT, %s TEXT, %s INTEGER);",
                TABLE_NAME, TYPE, NAME, DESCRIPTION, LEVEL);
        db.execSQL(request);
    }

    public void add(TaskType taskType) {
        ContentValues values = toContentValues(taskType);
        DBOpenHelper.getInstance().getWritableDatabase().insert(TABLE_NAME, null, values);
    }

    public void update(TaskType taskType) {
        ContentValues values = toContentValues(taskType);
        String[] args = {taskType.getType()};
        DBOpenHelper.getInstance().getWritableDatabase().update(TABLE_NAME, values, "type = ?", args);
    }

    public List<TaskType> getAll() {
        String query = String.format("SELECT * FROM %s ORDER BY %s", TABLE_NAME, NAME);
        Cursor cursor = DBOpenHelper.getInstance().getReadableDatabase().rawQuery(query, null);
        List<TaskType> taskTypes = new ArrayList<TaskType>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            taskTypes.add(read(cursor));
        }
        cursor.close();
        return taskTypes;
    }

    public void delete(TaskType taskType) {
        String[] args = {taskType.getType()};
        DBOpenHelper.getInstance().getWritableDatabase().delete(TABLE_NAME, "type = ?", args);
    }

    private TaskType read(Cursor cursor) {
        return new TaskType(
                cursor.getString(cursor.getColumnIndex(TYPE)),
                cursor.getString(cursor.getColumnIndex(NAME)),
                cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
                TaskType.Level.fromValue(cursor.getInt(cursor.getColumnIndex(LEVEL))));
    }



    private ContentValues toContentValues(TaskType taskType) {
        ContentValues values = new ContentValues();
        values.put(TYPE, taskType.getType());
        values.put(NAME, taskType.getName());
        values.put(DESCRIPTION, taskType.getDescription());
        values.put(LEVEL, taskType.getLevel().getValue());
        return values;
    }


}
