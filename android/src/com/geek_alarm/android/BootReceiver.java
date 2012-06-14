package com.geek_alarm.android;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.geek_alarm.android.db.AlarmPreferenceDao;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        List<AlarmPreference> alarms = AlarmPreferenceDao.INSTANCE.getAll();
        for (AlarmPreference alarm : alarms) {
            if (alarm.isEnabled()) {
                Utils.setAlarm(alarm);
            }
        }
    }

}
