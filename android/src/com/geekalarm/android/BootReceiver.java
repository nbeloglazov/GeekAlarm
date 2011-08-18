package com.geekalarm.android;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        List<AlarmPreference> alarms = DBUtils.getAlarmPreferences();
        for (AlarmPreference alarm : alarms) {
            if (alarm.isEnabled()) {
                Utils.setAlarm(alarm);
            }
        }
    }

}
