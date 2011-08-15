package nbeloglazov.geekalarm.android.activities;

import java.util.List;

import nbeloglazov.geekalarm.android.AlarmPreference;
import nbeloglazov.geekalarm.android.DBUtils;
import nbeloglazov.geekalarm.android.R;
import nbeloglazov.geekalarm.android.Utils;
import nbeloglazov.geekalarm.android.adapters.AlarmPreferenceAdapter;
import nbeloglazov.geekalarm.android.adapters.DifficultyAdapter;
import nbeloglazov.geekalarm.android.tasks.Configuration;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class AlarmsActivity extends Activity {

    private List<AlarmPreference> alarms;
    private AlarmPreferenceAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarms);
        findViewById(R.id.test_button)
            .setOnClickListener(new TestButtonListener());
        findViewById(R.id.alarm_sound_picker)
            .setOnClickListener(new AlarmSoundPickerListener());
        alarms = DBUtils.getAlarmPreferences();
        adapter = new AlarmPreferenceAdapter(this, alarms);
        ((ListView)findViewById(R.id.alarms)).setAdapter(adapter);
        initializeDifficultySpinner();
    }
    
    private void initializeDifficultySpinner() {
        Spinner spinner = (Spinner)findViewById(R.id.difficulty);
        DifficultyAdapter adapter = new DifficultyAdapter(this);
        spinner.setAdapter(adapter);
        int curDifficulty = Utils.getPreferences()
                            .getInt("difficulty", Configuration.DEFAULT_DIFFICULTY);
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
        default:
            return super.onOptionsItemSelected(item);
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
            int difficulty = (Integer)parent.getItemAtPosition(position);
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
}
