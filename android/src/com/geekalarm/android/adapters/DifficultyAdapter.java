package com.geekalarm.android.adapters;

import java.util.List;

import com.geekalarm.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter, for displaying 3 difficulties.
 */
public class DifficultyAdapter extends ArrayAdapter<Integer> {
    
    private static Integer[] DIFFICULTY_CODES = {1, 2, 3};
    private static int[] DIFFICULTY_NAMES = {R.string.easy,
                                             R.string.medium,
                                             R.string.hard};
    private LayoutInflater inflater;

    public DifficultyAdapter(Context context) {
        super(context, -1, DIFFICULTY_CODES);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
        }
        int resId= DIFFICULTY_NAMES[getItem(position) - 1];
        ((TextView)convertView).setText(resId);
        
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, null);
        }
        int resId= DIFFICULTY_NAMES[getItem(position) - 1];
        ((TextView)convertView).setText(resId);
        
        return convertView;
    }
}
