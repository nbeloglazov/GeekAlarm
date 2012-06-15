package com.geek_alarm.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import com.geek_alarm.android.activities.TaskActivity;

import java.util.Calendar;
import java.util.Date;

public class Utils {

    private static final String LAST_TASKS_UPDATE_TIME = "lastTasksUpdateTime";
    private static final long MIN_TASKS_UPDATE_FREQUENCY = 1000 * 60 * 60 * 24; // Once a day
    
    public static int DAYS_OF_WEEK_NAMES[] = {
        R.string.monday,
        R.string.tuesday,
        R.string.wednesday,
        R.string.thursday,
        R.string.friday,
        R.string.saturday,
        R.string.sunday
    };
    
    private final static int[] DAYS_OF_WEEK = new int[8];
    
    static {
        int[] days = {Calendar.MONDAY,
                      Calendar.TUESDAY,
                      Calendar.WEDNESDAY,
                      Calendar.THURSDAY,
                      Calendar.FRIDAY,
                      Calendar.SATURDAY,
                      Calendar.SUNDAY};
        for (int i = 0; i < 7; i++) {
            DAYS_OF_WEEK[days[i]] = i;
        }
    }        
    
    private static final String ALARM_SOUND = "alarm_sound";

    /**
     * Checks if device is connected to internet.
     * @return true or false.
     */
    public static boolean isOnline() {
        Context context = Application.getContext();
        ConnectivityManager cm = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    
    /**
     * Transforms calendar day of week to number (from 0 to 7).
     * E.g. Calendar.MONDAY -> 0
     *      Calendar.TUESDAY -> 1
     * @param calendarDayOfWeek
     * @return 
     */
    public static int getDayOfWeek(int calendarDayOfWeek) {
        return DAYS_OF_WEEK[calendarDayOfWeek];
    }

    /**
     * Returns earliest date in future, which has given hour and minute. 
     * E.g now is May 1, 2000. 14:00
     * getNextTime(14, 15) -> May 1, 2000, 14:15
     * getNextTime(13, 15) -> May 2, 2000, 13:15
     * @param hour
     * @param minute
     * @return date in millis.
     */
    public static long getNextTime(int hour, int minute) {
        Calendar cur = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.SECOND, 0);
        cur.set(Calendar.SECOND, 59);
        if (next.before(cur)) {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }
        return next.getTimeInMillis();
    }
    
    private static PendingIntent buildAlarmIntent(int alarmId) {
        Intent intent = new Intent(Application.getContext(), TaskActivity.class);
        intent.setData(Uri.parse("id:" + alarmId));
        PendingIntent pending = PendingIntent.getActivity(
                Application.getContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

    /**
     * Sets alarm in AlarmManager, so TaskActivity will be launched at alarm's time every day.
     * It's TaskActivity's responsibility to check if current day is enabled in this alarm. 
     * @param alarm to be set.
     */
    public static void setAlarm(AlarmPreference alarm) {
        AlarmManager manager = (AlarmManager) Application.getContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = buildAlarmIntent(alarm.getId());
        long nextTime = getNextTime(alarm.getHour(), alarm.getMinute());
        manager.setRepeating(AlarmManager.RTC_WAKEUP, nextTime, AlarmManager.INTERVAL_DAY, intent);
    }

    public static void cancelAlarm(AlarmPreference alarm) {
        AlarmManager manager = (AlarmManager) Application.getContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = buildAlarmIntent(alarm.getId());
        manager.cancel(intent);
    }
    
    public static SharedPreferences getPreferences() {
        return Application.getContext()
            .getSharedPreferences("geekalarm", Context.MODE_PRIVATE);
    }
    
    /**
     * Returns alarm sound.
     * 1. Returns selected sound, if exists
     * 2. Returns default alarm sound, if exists.
     * 3. Returns built in mario sound, exists!
     * @return uri to sound.
     */
    public static Uri getCurrentAlarmSound() {
        Uri sound = null; 
        // Look up sound in preferences.
        SharedPreferences pref = getPreferences();
        String uriString = pref.getString(ALARM_SOUND, null);
        if (uriString != null) {
            sound = Uri.parse(uriString);
        }
        // Look up default alarm sound.
        if (sound == null) {
            sound = RingtoneManager.getActualDefaultRingtoneUri(
                        Application.getContext(),
                        RingtoneManager.TYPE_ALARM);
        }
        
        // If no sound in preferences, no default return geek alarm music.
        if (sound == null) {
            sound = getUriFromResource(R.raw.mario);
        }
        return sound;
    }   
    
    /**
     * Saves given uri as alarm sound.
     * @param uri
     */
    public static void setCurrentAlarmSound(Uri uri) {
        SharedPreferences pref = getPreferences();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(ALARM_SOUND, uri.toString());
        editor.commit();
    }
    
    /**
     * Transforms resource id to uri.
     * @param resource
     * @return uri 
     */
    public static Uri getUriFromResource(int resource) {
        String uri = String.format("android.resource://%s/%d", 
                Application.getContext().getPackageName(), 
                resource);
        return Uri.parse(uri);
    }

    /**
     * Run update task types async task. It will get latest tasks from server and update local copy.
     * By default update happens when user opens application. A
     * @param forceUpdate
     */
    public static void updateTaskTypesAsync(boolean forceUpdate) {
        long lastTime = getPreferences().getLong(LAST_TASKS_UPDATE_TIME, 0);
        long currentTime = new Date().getTime();
        if (forceUpdate || currentTime - lastTime > MIN_TASKS_UPDATE_FREQUENCY) {
            new UpdateTaskTypesAsyncTask().doInBackground();
        }
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putLong(LAST_TASKS_UPDATE_TIME, currentTime);
        editor.commit();
    }

}
