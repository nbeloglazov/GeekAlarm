package com.geekalarm.android.activities;

import java.util.List;

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

import com.geekalarm.android.AlarmPreference;
import com.geekalarm.android.DBUtils;
import com.geekalarm.android.R;
import com.geekalarm.android.Utils;
import com.geekalarm.android.adapters.AlarmPreferenceAdapter;
import com.geekalarm.android.adapters.DifficultyAdapter;
import com.geekalarm.android.tasks.Configuration;

public class AlarmsActivity extends Activity {

    private List<AlarmPreference> alarms;
    private AlarmPreferenceAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarms);
        findViewById(R.id.test_button).setOnClickListener(
                new TestButtonListener());
        findViewById(R.id.alarm_sound_picker).setOnClickListener(
                new AlarmSoundPickerListener());
        findViewById(R.id.image).setOnClickListener(
                new AnimationListener());
        alarms = DBUtils.getAlarmPreferences();
        adapter = new AlarmPreferenceAdapter(this, alarms);
        ((ListView) findViewById(R.id.alarms)).setAdapter(adapter);
        initializeDifficultySpinner();
        if (alarms.isEmpty()) {
            addAlarm();
        }
    }

    private void initializeDifficultySpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.difficulty);
        DifficultyAdapter adapter = new DifficultyAdapter(this);
        spinner.setAdapter(adapter);
        int curDifficulty = Utils.getPreferences().getInt("difficulty",
                Configuration.DEFAULT_DIFFICULTY);
        spinner.setSelection(adapter.getPosition(curDifficulty));
        spinner.setOnItemSelectedListener(new DifficultyChangedListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        AlarmPreference alarm = adapter.getItem(requestCode);
        alarm.setHour(data.getIntExtra("hour", 0));
        alarm.setMinute(data.getIntExtra("minute", 0));
        alarm.setDays(data.getIntExtra("days", 0));
        DBUtils.updateAlarmPreference(alarm);
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
        Utils.setAlarm(alarm);
        DBUtils.addAlarmPreference(alarm);
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
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void sendFeedback() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { "feedback@geek-alarm.com" });
        i.putExtra(Intent.EXTRA_SUBJECT, "GeekAlarm feedback");
        try {
            String text = getResources().getString(R.string.send_email);
            startActivity(Intent.createChooser(i, text));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.no_email_clients,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class TestButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlarmsActivity.this, TaskActivity.class);
            startActivity(intent);
        }
    }

    private class DifficultyChangedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            int difficulty = (Integer) parent.getItemAtPosition(position);
            SharedPreferences.Editor editor = Utils.getPreferences().edit();
            editor.putInt("difficulty", difficulty);
            editor.commit();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing
        }
    }

    private class AlarmSoundPickerListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlarmsActivity.this,
                    AlarmSoundPickerActivity.class);
            startActivity(intent);
        }
    }
    
    private class AnimationListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlarmsActivity.this, ResultActivity.class);
            startActivity(intent);
        }
        
    }
}
