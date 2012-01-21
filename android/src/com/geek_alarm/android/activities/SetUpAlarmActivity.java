package com.geek_alarm.android.activities;

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

import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelClickedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;


/**
 * Activity for setting up alarm: set time and days.
 */
public class SetUpAlarmActivity extends Activity {

    private final static int VISIBLE_ITEMS = 3;

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
        boolean is24Hour = DateFormat.is24HourFormat(this);
        boolean isAM = hour < 12;
        initHoursPicker(hour, is24Hour);
        initMinutesPicker(minute);
        initAMPMPicker(isAM, is24Hour);
    }

    private void initHoursPicker(int hour, boolean is24Hour) {
        if (!is24Hour) {
            hour %= 12;
            if (hour == 0) {
                hour = 12;
            }
            hour--;
        }
        final WheelView hours = (WheelView) findViewById(R.id.hour);
        NumericWheelAdapter hourAdapter = new NumericWheelAdapter(this, is24Hour ? 0 : 1, is24Hour ? 23 : 12);
        hourAdapter.setItemResource(R.layout.wheel_text_item);
        hourAdapter.setItemTextResource(R.id.text);
        hours.setViewAdapter(hourAdapter);
        hours.setCyclic(true);
        hours.setCurrentItem(hour);
        hours.setVisibleItems(VISIBLE_ITEMS);
    }

    private void initMinutesPicker(int minute) {
        final WheelView mins = (WheelView) findViewById(R.id.mins);
        NumericWheelAdapter minAdapter = new NumericWheelAdapter(this, 0, 59, "%02d");
        minAdapter.setItemResource(R.layout.wheel_text_item);
        minAdapter.setItemTextResource(R.id.text);
        mins.setViewAdapter(minAdapter);
        mins.setCyclic(true);
        mins.setCurrentItem(minute);
        mins.setVisibleItems(VISIBLE_ITEMS);
    }

    private void initAMPMPicker(boolean isAM, boolean is24Hour) {
        final WheelView ampm = (WheelView) findViewById(R.id.ampm);
        if (is24Hour) {
            ampm.setVisibility(View.GONE);
            return;
        }
        ArrayWheelAdapter<String> ampmAdapter =
            new ArrayWheelAdapter<String>(this, new String[] {"AM", "PM"});
        ampmAdapter.setItemResource(R.layout.wheel_text_item);
        ampmAdapter.setItemTextResource(R.id.text);
        ampm.setViewAdapter(ampmAdapter);
        ampm.setCurrentItem(isAM ? 0 : 1);
        ampm.setVisibleItems(VISIBLE_ITEMS);
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

            intent.putExtra("hour", getHour());
            intent.putExtra("minute", getMinute());
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


    private int getMinute() {
        WheelView mins = (WheelView) findViewById(R.id.mins);
        return mins.getCurrentItem();
    }

    private int getHour() {
        final WheelView hours = (WheelView) findViewById(R.id.hour);
        boolean is24Hour = DateFormat.is24HourFormat(this);
        if (is24Hour) {
            return hours.getCurrentItem();
        } else {
            final WheelView ampm = (WheelView) findViewById(R.id.ampm);
            boolean isAM = ampm.getCurrentItem() == 0;
            int hour = hours.getCurrentItem();
            hour++;
            hour %= 12;
            if (!isAM) {
                hour += 12;
            }
            return hour;
        }
    }

}
