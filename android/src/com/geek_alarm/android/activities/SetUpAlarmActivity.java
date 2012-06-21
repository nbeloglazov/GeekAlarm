package com.geek_alarm.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.geek_alarm.android.ActivityUtils;
import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;


/**
 * Activity for setting up alarm: set time and days.
 */
public class SetUpAlarmActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_up_alarm);
        findViewById(R.id.ok).setOnClickListener(new OkListener());
        findViewById(R.id.cancel).setOnClickListener(new CancelListener());
        initWheelTimePicker();
        initDays();
    }

    private void initWheelTimePicker() {
        Intent intent = getIntent();
        int hour = intent.getIntExtra("hour", 0);
        int minute = intent.getIntExtra("minute", 0);
        ActivityUtils.initWheelTimePicker(this, hour, minute, R.layout.wheel_text_item_small);
    }

    private void initDays() {
        Intent intent = getIntent();

        int daysCode = intent.getIntExtra("days", 0);
        TableLayout days = (TableLayout)findViewById(R.id.days);
        for (int i = 0; i < 7; i++) {
            TableRow nameRow = (TableRow)days.getChildAt(0);
            TextView dayName = new TextView(this);
            dayName.setText(Utils.DAYS_OF_WEEK_NAMES[i]);
            if (i > 4) {
                dayName.setTextAppearance(this, R.style.Red);
            }
            dayName.setGravity(Gravity.CENTER_HORIZONTAL);
            nameRow.addView(dayName);

            TableRow checkboxRow = (TableRow)days.getChildAt(1);
            CheckBox box = new CheckBox(this);
            box.setChecked(((1 << i) & daysCode) != 0);
            checkboxRow.addView(box);
        }
    }

    private class OkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();

            intent.putExtra("hour", ActivityUtils.getHour(SetUpAlarmActivity.this));
            intent.putExtra("minute", ActivityUtils.getMinute(SetUpAlarmActivity.this));
            intent.putExtra("days", getDays());
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private class CancelListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private int getDays() {
        int daysCode = 0;
        TableRow days = (TableRow)((TableLayout)findViewById(R.id.days)).getChildAt(1);
        for (int i = 0; i < days.getChildCount(); i++) {
            daysCode |= ((CheckBox)days.getChildAt(i)).isChecked() ? (1 << i) : 0;
        }
        return daysCode;
    }

}
