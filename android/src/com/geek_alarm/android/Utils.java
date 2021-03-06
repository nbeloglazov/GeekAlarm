package com.geek_alarm.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;
import com.geek_alarm.android.activities.TaskActivity;

import java.util.Calendar;
import java.util.Date;

public class Utils {

    private static final String LAST_TASKS_UPDATE_TIME = "lastTasksUpdateTime";
    private static final long MIN_TASKS_UPDATE_FREQUENCY = 1000 * 60 * 60 * 24; // Once a day
    private static final int MILLISECONDS_IN_HOUR = 1000 * 60 * 60;
    private static final int MILLISECONDS_IN_MINUTE = 1000 * 60;
    private static final int HTC_SENSATION_SCREEN_WIDTH = 540;
    private static final int HTC_SENSATION_SCREEN_HEIGHT = 960;


    public static final String NUMBER_OF_ATTEMPTS = "numberOfAttempts";
    public static final String POSITIVE_BALANCE = "positiveBalance";

    public static int DEFAULT_NUMBER_OF_ATTEMPTS = 10;
    public static int DEFAULT_POSITIVE_BALANCE = 3;

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
    
    /**
     * Returns string representation for time gap between 2 dates given in milliseconds.
     * E.g. "5 hours 2 minutes".
     * @param from
     * @param to
     * @return string representation of the time gap
     */
    public static String timeBetween(long from, long to) {
        long diff = to - from;
        long hours = diff / MILLISECONDS_IN_HOUR;
        diff %= MILLISECONDS_IN_HOUR;
        long minutes = diff / MILLISECONDS_IN_MINUTE;
        return String.format("%d hour%s %d minute%s left",
                hours, hours == 1 ? "" : "s",
                minutes, minutes == 1 ? "" : "s");
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
        return PreferenceManager.getDefaultSharedPreferences(Application.getContext());
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
     * Run update task types async task. It will get latest tasks from server and update local copy.
     * By default update happens when user opens application.
     * If user opens application too frequently we don't want to check server every time. So we check it
     * at only if last check was more then 1 day ago.
     * But sometimes we need to force update (to get latest updates if we now they are on server :)), so forceUpdate
     * can be used to achieve it.
     * @param forceUpdate - if true, then ignore last task time update and check anyway.
     */
    public static void updateTaskTypesAsync(boolean forceUpdate) {
        long lastTime = getPreferences().getLong(LAST_TASKS_UPDATE_TIME, 0);
        long currentTime = new Date().getTime();
        if (forceUpdate || currentTime - lastTime > MIN_TASKS_UPDATE_FREQUENCY) {
            new UpdateTaskTypesAsyncTask().execute();
        }
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putLong(LAST_TASKS_UPDATE_TIME, currentTime);
        editor.commit();
    }

    /**
     * Max number of tasks user can try to solve during 1 alarm session.
     * @return number of tasks user can try to solve
     */
    public static int getNumberOfAttempts() {
        return getPreferences().getInt(NUMBER_OF_ATTEMPTS, DEFAULT_NUMBER_OF_ATTEMPTS);
    }

    /**
     * User must solve NUMBER_OF_SOLVED - NUMBER_OF_FAILED tasks to dismiss alarm.
     * @return NUMBER_OF_SOLVED - NUMBER_OF_FAILED
     */
    public static int getPositiveBalance() {
        return getPreferences().getInt(POSITIVE_BALANCE, DEFAULT_POSITIVE_BALANCE);
    }

    public static Bitmap resizeImage(final Bitmap image) {
        Point screenSize = getScreenSize();
        double screenWidth = (double) screenSize.x;
        double screenHeight = (double) screenSize.y;
        double scale = Math.min(screenWidth / HTC_SENSATION_SCREEN_WIDTH, screenHeight / HTC_SENSATION_SCREEN_HEIGHT);
        return Bitmap.createScaledBitmap( image, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale),
                true);

    }

    private static Point getScreenSize() {
        Point result = new Point();
        WindowManager wm = (WindowManager) Application.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.HONEYCOMB){
            display.getSize(result);
        } else {
            result.set(display.getWidth(), display.getHeight());
        }
        return result;
    }

}
