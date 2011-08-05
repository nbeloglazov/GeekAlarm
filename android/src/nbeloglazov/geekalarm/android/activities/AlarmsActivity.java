package nbeloglazov.geekalarm.android.activities;

import java.util.List;

import nbeloglazov.geekalarm.android.AlarmPreference;
import nbeloglazov.geekalarm.android.AlarmPreferenceAdapter;
import nbeloglazov.geekalarm.android.DBUtils;
import nbeloglazov.geekalarm.android.R;
import nbeloglazov.geekalarm.android.Utils;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;

public class AlarmsActivity extends Activity {

    private static String TAG = "geekalarm";
    private List<AlarmPreference> alarms;
    private AlarmPreferenceAdapter adapter;

    /**
     * Called when the activity is first created.
     * 
     * @param savedInstanceState
     *            If the activity is being re-initialized after previously being
     *            shut down then this Bundle contains the data it most recently
     *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
     *            is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarms);
        Button testButton = (Button) this.findViewById(R.id.test_button);
        testButton.setOnClickListener(new TestButtonListener());
        alarms = DBUtils.getAlarmPreferences();
        adapter = new AlarmPreferenceAdapter(this, alarms);
        ((ListView)findViewById(R.id.alarms)).setAdapter(adapter);
        Log.i(TAG, "onCreate");
    }

    private class TestButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlarmsActivity.this, TaskActivity.class);
            startActivity(intent);
        }
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
}
