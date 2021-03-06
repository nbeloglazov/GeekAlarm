package com.geek_alarm.android.activities;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import com.geek_alarm.android.AlarmPreference;
import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;
import com.geek_alarm.android.adapters.AlarmPreferenceAdapter;
import com.geek_alarm.android.db.AlarmPreferenceDao;

import java.util.List;

/**
 * This is main window.
 * List of alarms and option buttons
 * are displayed in this activity.
 */
public class AlarmsActivity extends ActionBarActivity {

    private AlarmPreferenceAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateTaskTypesAsync(false);
        if (SingleAlarmActivity.useSingleAlarmActivity()) {
            goToSingleAlarmActivity();
            return;
        }
        setContentView(R.layout.alarms);
        List<AlarmPreference> alarms = AlarmPreferenceDao.INSTANCE.getAll();
        adapter = new AlarmPreferenceAdapter(this, alarms);
        ((ListView) findViewById(R.id.alarms)).setAdapter(adapter);
        adapter.registerDataSetObserver(new DeleteAlarmObserver());
        // Add alarm by default, if there is no one yet.
        if (alarms.isEmpty()) {
            addAlarm();
        }
    }

    /**
     * This method called, after user modified activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        AlarmPreference alarm = adapter.getItem(requestCode);
        alarm.setHour(data.getIntExtra("hour", 0));
        alarm.setMinute(data.getIntExtra("minute", 0));
        alarm.setDays(data.getIntExtra("days", 0));
        AlarmPreferenceDao.INSTANCE.update(alarm);
        if (alarm.isEnabled()) {
            Utils.setAlarm(alarm);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarms, menu);
        return true;
    }

    private void addAlarm() {
        AlarmPreference alarm = new AlarmPreference();
        // All days by default.
        alarm.setDays(0x7F);
        alarm.setEnabled(true);
        AlarmPreferenceDao.INSTANCE.add(alarm);
        // It must be only AFTER we inserted in db, 
        // otherwise id will be empty and we won't find alarm in db, 
        // when it go off.
        Utils.setAlarm(alarm);
        adapter.add(alarm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SingleAlarmActivity.useSingleAlarmActivity()) {
            goToSingleAlarmActivity();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add:
                addAlarm();
                return true;
            case R.id.settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DeleteAlarmObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            if (SingleAlarmActivity.useSingleAlarmActivity()) {
                goToSingleAlarmActivity();
            }
        }
    }

    private void showSettings() {
        Intent intent = new Intent(AlarmsActivity.this, PreferenceActivity.class);
        startActivity(intent);
    }

    private void goToSingleAlarmActivity() {
        Intent intent = new Intent(this, SingleAlarmActivity.class);
        startActivity(intent);
        finish();
    }

}
