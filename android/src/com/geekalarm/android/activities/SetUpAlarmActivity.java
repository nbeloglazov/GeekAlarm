package com.geekalarm.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.geekalarm.android.R;
import com.geekalarm.android.Utils;

public class SetUpAlarmActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_up_alarm);
        findViewById(R.id.ok).setOnClickListener(new OkListener());
        findViewById(R.id.cancel).setOnClickListener(new CancelListener());
        initData();
    }
    
    private void initData() {
        Intent intent = getIntent();
        TimePicker picker = (TimePicker)findViewById(R.id.time);
        boolean is24hour = DateFormat.is24HourFormat(this);
        picker.setIs24HourView(is24hour);
        picker.setCurrentHour(intent.getIntExtra("hour", 0));
        picker.setCurrentMinute(intent.getIntExtra("minute", 0));
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
            TimePicker picker = (TimePicker)findViewById(R.id.time);
            intent.putExtra("hour", picker.getCurrentHour());
            intent.putExtra("minute", picker.getCurrentMinute());
            int daysCode = 0;
            TableRow days = (TableRow)((TableLayout)findViewById(R.id.days)).getChildAt(1);
            for (int i = 0; i < days.getChildCount(); i++) {
                daysCode |= ((CheckBox)days.getChildAt(i)).isChecked() ? (1 << i) : 0; 
            }
            intent.putExtra("days", daysCode);
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
}
