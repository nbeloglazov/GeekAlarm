package com.geek_alarm.android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.geek_alarm.android.AlarmSound;
import com.geek_alarm.android.R;
import com.geek_alarm.android.activities.AlarmSoundPickerActivity;

/**
 * Adapter for displaying alarm sounds.
 * It also tracks current selected and previous selected sound,
 * in case we need to return to previous selected.
 */
public class AlarmSoundAdapter extends ArrayAdapter<AlarmSound> {

    private LayoutInflater inflater;
    private AlarmSoundPickerActivity.SoundClickListener clickCallback;
    private int selected = -1;
    private int previousSelected = -1;
    private OnClickListener listener;
    
    public int getSelected() {
        return selected;
    }
    public void setSelected(int selected) {
        this.previousSelected = this.selected;
        this.selected = selected;
    }
    
    public AlarmSoundAdapter(Context context, List<AlarmSound> sounds, 
            AlarmSoundPickerActivity.SoundClickListener callback) {
        super(context, -1, sounds);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.clickCallback = callback;
        listener = new SoundClickListener();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.alarm_sound, null);
            convertView.setOnClickListener(listener);
            convertView.findViewById(R.id.radio).setOnClickListener(listener);

        }
        convertView.setTag(position);
        AlarmSound sound = getItem(position);
        TextView title = (TextView)convertView.findViewById(R.id.title);
        title.setText(sound.getTitle());
        RadioButton radio = (RadioButton)convertView.findViewById(R.id.radio);
        radio.setTag(position);
        radio.setChecked(selected == position);
        return convertView;
    }
    
    public void selectPrevious() {
        this.selected = this.previousSelected;
        notifyDataSetChanged();
    }
    
    private class SoundClickListener implements OnClickListener {
        
        @Override
        public void onClick(View view) {
            int id = (Integer)view.getTag();
            setSelected(id);
            clickCallback.onClick(selected);
            notifyDataSetChanged();
        }
        
        
    }
}
