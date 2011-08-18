package com.geekalarm.android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.geekalarm.android.AlarmSound;
import com.geekalarm.android.R;
import com.geekalarm.android.activities.AlarmSoundPickerActivity;

public class AlarmSoundAdapter extends ArrayAdapter<AlarmSound> {

    private LayoutInflater inflater;
    private AlarmSoundPickerActivity.SoundClickListener clickCallback;
    private int selected = -1;
    private OnClickListener listener;
    
    public int getSelected() {
        return selected;
    }
    public void setSelected(int selected) {
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
        }
        convertView.setId(position);
        AlarmSound sound = getItem(position);
        TextView title = (TextView)convertView.findViewById(R.id.title);
        title.setText(sound.getTitle());
        RadioButton radio = (RadioButton)convertView.findViewById(R.id.radio);
        radio.setChecked(selected == position);
        return convertView;
    }
    
    
    private class SoundClickListener implements OnClickListener {
        
        @Override
        public void onClick(View view) {
            selected = view.getId();
            clickCallback.onClick(selected);
            notifyDataSetChanged();
        }
        
        
    }
}
