package com.geekalarm.android;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.geekalarm.android.activities.TaskActivity;

public class Utils {
    
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

    
    public static int getDayOfWeek(int calendarDayOfWeek) {
        return DAYS_OF_WEEK[calendarDayOfWeek];
    }

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
    
    public static PendingIntent buildAlarmIntent(int alarmId) {
        Intent intent = new Intent(Application.getContext(), TaskActivity.class);
        intent.setData(Uri.parse("id:" + alarmId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pending = PendingIntent.getActivity(
                Application.getContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }
    
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
            sound = getUriFromResource(R.raw.into_the_sun);
        }
        return sound;
    }   
    
    public static void setCurrentAlarmSound(Uri uri) {
        SharedPreferences pref = getPreferences();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(ALARM_SOUND, uri.toString());
        editor.commit();
    }
    
    public static Uri getUriFromResource(int resource) {
        String uri = String.format("android.resource://%s/%d", 
                Application.getContext().getPackageName(), 
                resource);
        return Uri.parse(uri);
    }

}
