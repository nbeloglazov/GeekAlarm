package nbeloglazov.geekalarm.android.activities;

import java.text.DateFormat;
import java.util.Calendar;

import nbeloglazov.geekalarm.android.R;
import nbeloglazov.geekalarm.android.R.layout;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class AlarmsActivity extends Activity {

    private static String TAG = "geekalarm";

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
        findViewById(R.id.set_time).setOnClickListener(new SetAlarmListener());
        Log.i(TAG, "onCreate");
    }

    private class TestButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlarmsActivity.this, TaskActivity.class);
            startActivity(intent);
        }
    }

    private class SetAlarmListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            TimePicker picker = (TimePicker) findViewById(R.id.date_picker);
            Calendar cur = Calendar.getInstance();
            Calendar next = Calendar.getInstance();
            next.set(Calendar.MINUTE, picker.getCurrentMinute());
            next.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            if (next.before(cur)) {
                next.set(Calendar.DAY_OF_MONTH,
                        next.get(Calendar.DAY_OF_MONTH) + 1);
            }
            DateFormat formatter = DateFormat.getDateTimeInstance();
            AlertDialog dialog = new AlertDialog.Builder(AlarmsActivity.this)
                    .setMessage(formatter.format(next.getTime())).create();
            dialog.show();
            Intent intent = new Intent(getApplicationContext(),
                    TaskActivity.class);
            PendingIntent pending = PendingIntent.getActivity(
                    getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            manager.set(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(),
                    pending);
            finish();
        }
    }
}
