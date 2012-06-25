package com.geek_alarm.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.geek_alarm.android.ActivityUtils;
import com.geek_alarm.android.AlarmPreference;
import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;
import com.geek_alarm.android.db.AlarmPreferenceDao;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;

import java.util.Date;
import java.util.List;

public class SingleAlarmActivity extends Activity {

    private Toast timeLeft;

    public static boolean useSingleAlarmActivity() {
        List<AlarmPreference> alarms = AlarmPreferenceDao.INSTANCE.getAll();
        if (Utils.getPreferences().getBoolean("useMultiAlarmScreen", false)) {
            return false;
        }
        if (alarms.size() == 0) {
            return true;
        }
        if (alarms.size() == 1) {
            AlarmPreference alarm = alarms.get(0);
            // Only if all days enabled for the alarm we can show it on SingleAlarm activity.
            int allDays = 0x7F;
            return alarm.getDays() == allDays;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!useSingleAlarmActivity()) {
            goToAlarmsActivity();
            return;
        }
        timeLeft = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        setContentView(R.layout.single_alarm);
        setUpAlarm();
        setUpToggleButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!useSingleAlarmActivity()) {
            goToAlarmsActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarms, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add:
                addAlarm();
                // We have 2 alarms now, so switch to AlarmsActivity.
                goToAlarmsActivity();
                return true;
            case R.id.settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void setUpToggleButton() {
        ToggleButton toggle = (ToggleButton)findViewById(R.id.enabled);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveAlarm();
            }
        });
        AlarmPreference alarm = AlarmPreferenceDao.INSTANCE.getAll().get(0);
        toggle.setChecked(alarm.isEnabled());
    }

    private void showSettings() {
        Intent intent = new Intent(this, PreferenceActivity.class);
        startActivity(intent);
    }

    private void setUpAlarm() {
        List<AlarmPreference> alarms = AlarmPreferenceDao.INSTANCE.getAll();
        if (alarms.isEmpty()) {
            alarms.add(addAlarm());
        }
        AlarmPreference alarm = alarms.get(0);
        ActivityUtils.initWheelTimePicker(this, alarm.getHour(), alarm.getMinute(), R.layout.wheel_text_item_big);
        WheelStoppedListener listener = new WheelStoppedListener();
        int[] items = {R.id.hour, R.id.mins, R.id.ampm};
        for (int item : items) {
            ((WheelView) findViewById(item)).addScrollingListener(listener);
            if (!DateFormat.is24HourFormat(this)) {
                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                        (int) getResources().getDimension(R.dimen.width_alarm_am_pm),
                        LinearLayout.LayoutParams.FILL_PARENT);
                findViewById(item).setLayoutParams(layout);
            }
        }

        ((WheelView) findViewById(R.id.mins)).addScrollingListener(listener);
        ((WheelView) findViewById(R.id.ampm)).addScrollingListener(listener);
    }

    private AlarmPreference addAlarm() {
        AlarmPreference alarm = new AlarmPreference();
        // All days by default.
        alarm.setDays(0x7F);
        alarm.setEnabled(true);
        AlarmPreferenceDao.INSTANCE.add(alarm);
        // It must be only AFTER we inserted in db,
        // otherwise id will be empty and we won't find alarm in db,
        // when it go off.
        Utils.setAlarm(alarm);
        return alarm;
    }

    private void goToAlarmsActivity() {
        Intent intent = new Intent(this, AlarmsActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveAlarm() {
        AlarmPreference alarm = AlarmPreferenceDao.INSTANCE.getAll().get(0);
        alarm.setHour(ActivityUtils.getHour(this));
        alarm.setMinute(ActivityUtils.getMinute(this));
        alarm.setEnabled(((ToggleButton) findViewById(R.id.enabled)).isChecked());
        AlarmPreferenceDao.INSTANCE.update(alarm);
        String message;
        if (alarm.isEnabled()) {
            Utils.setAlarm(alarm);
            long nextTime = Utils.getNextTime(alarm.getHour(), alarm.getMinute());
            message = Utils.timeBetween(new Date().getTime(), nextTime);
        } else {
            Utils.cancelAlarm(alarm);
            message = "Alarm disabled";
        }
        timeLeft.setText(message);
        timeLeft.show();
    }

    private class WheelStoppedListener implements OnWheelScrollListener {
        public void onScrollingFinished(WheelView wheel) {
            saveAlarm();
        }

        public void onScrollingStarted(WheelView wheel) {}
    }

}
