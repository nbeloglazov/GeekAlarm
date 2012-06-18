package com.geek_alarm.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.geek_alarm.android.AlarmPreference;
import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;
import com.geek_alarm.android.adapters.AlarmPreferenceAdapter;
import com.geek_alarm.android.adapters.DifficultyAdapter;
import com.geek_alarm.android.db.AlarmPreferenceDao;

import java.util.List;

/**
 * This is main window.
 * List of alarms and option buttons
 * are displayed in this activity.
 */
public class AlarmsActivity extends Activity {

    private List<AlarmPreference> alarms;
    private AlarmPreferenceAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateTaskTypesAsync(false);
        setContentView(R.layout.alarms);
        alarms = AlarmPreferenceDao.INSTANCE.getAll();
        adapter = new AlarmPreferenceAdapter(this, alarms);
        ((ListView) findViewById(R.id.alarms)).setAdapter(adapter);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add:
                addAlarm();
                return true;
            case R.id.feedback:
                sendFeedback();
                return true;
            case R.id.settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings() {
        Intent intent = new Intent(AlarmsActivity.this, PreferenceActivity.class);
        startActivity(intent);
    }

    private void sendFeedback() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { "feedback@geek-alarm.com" });
        i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        try {
            String text = getResources().getString(R.string.send_email);
            startActivity(Intent.createChooser(i, text));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.no_email_clients,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
