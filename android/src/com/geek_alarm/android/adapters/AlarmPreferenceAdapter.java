package com.geek_alarm.android.adapters;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geek_alarm.android.AlarmPreference;
import com.geek_alarm.android.DBUtils;
import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;
import com.geek_alarm.android.activities.SetUpAlarmActivity;

/**
 * Adapter for displaying alarms.
 */
public class AlarmPreferenceAdapter extends ArrayAdapter<AlarmPreference> {
    
    private LayoutInflater inflater;
    
    public AlarmPreferenceAdapter(Context context, List<AlarmPreference> preferences) {
        super(context, -1, preferences);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.alarm, null);
        }
        AlarmPreference preference = getItem(position);
        
        fillTime(convertView, preference);
        fillDays(convertView, preference.getDays());
        setUpCheckbox(convertView, preference);
        
        convertView.findViewById(R.id.delete).setOnClickListener(new DeleteAlarmListener(preference));
        SetUpAlarmListenter listener = new SetUpAlarmListenter(position);   
        
        convertView.findViewById(R.id.days).setOnClickListener(listener);
        convertView.findViewById(R.id.time).setOnClickListener(listener);
        return convertView;
    }

    private void setUpCheckbox(View convertView, AlarmPreference preference) {
        CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.enabled_checkbox);
        EnableStateListener checkBoxListener = new EnableStateListener(preference);
        checkBox.setOnCheckedChangeListener(checkBoxListener);
        checkBox.setChecked(preference.isEnabled());
    }
        
    /**
     * Sets time to text view.
     * @param convertView
     * @param preference
     */
    private void fillTime( View convertView, AlarmPreference preference) {
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(getContext());
        Date date = new Date(0, 0, 0, preference.getHour(), preference.getMinute());
        String time = timeFormat.format(date);
        ((TextView)convertView.findViewById(R.id.time)).setText(time);
    }
    
    /**
     * Adds days to view.
     * If day is enabled it's white, gray otherwise.
     * @param convertView
     * @param days
     */
    private void fillDays(View convertView, int days) {
        LinearLayout layout = (LinearLayout)convertView.findViewById(R.id.days);
        layout.removeAllViews();
        for (int i = 0; i < 7; i++) {
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams params = 
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1F/7);
            textView.setText(Utils.DAYS_OF_WEEK_NAMES[i]);
            if ((days & (1 << i)) == 0) {
                textView.setTextAppearance(getContext(), R.style.Fade);
            } 
            layout.addView(textView, params);
        }
    }
    
    /**
     * Called, when user taps on alarm.
     * Shows dialog with setting - SetUpAlarmActivity.
     */
    private class SetUpAlarmListenter implements View.OnClickListener {

        private int position;

        public SetUpAlarmListenter(int position) {
            super();
            this.position = position;
        }
        
        private void fillData(Intent intent) {
            AlarmPreference alarm = getItem(position);
            intent.putExtra("hour", alarm.getHour());
            intent.putExtra("minute", alarm.getMinute());
            intent.putExtra("days", alarm.getDays());
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), SetUpAlarmActivity.class);
            fillData(intent);
            ((Activity)getContext()).startActivityForResult(intent, position);
        }
    }

    /**
     * Called when user enables/disables alarm using checkbox.
     */
    private class EnableStateListener implements OnCheckedChangeListener {

        private AlarmPreference alarm;
        
        public EnableStateListener(AlarmPreference alarm) {
            super();
            this.alarm = alarm;
        }
        
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            alarm.setEnabled(isChecked);
            DBUtils.updateAlarmPreference(alarm);
            if (isChecked) {
                Utils.setAlarm(alarm);
            } else {
                Utils.cancelAlarm(alarm);
            }
        }
        
    }
    
    private class DeleteAlarmListener implements View.OnClickListener {
        
        private AlarmPreference alarm;

        public DeleteAlarmListener(AlarmPreference alarm) {
            super();
            this.alarm = alarm;
        }

        @Override
        public void onClick(View v) {
            DBUtils.removeAlarmPreference(alarm);
            if (alarm.isEnabled()) {
                Utils.cancelAlarm(alarm);
            }
            remove(alarm);
        };
    }

}
