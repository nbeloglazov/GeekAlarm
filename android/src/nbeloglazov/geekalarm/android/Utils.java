package nbeloglazov.geekalarm.android;

import java.util.Calendar;

import nbeloglazov.geekalarm.android.activities.TaskActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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
}
