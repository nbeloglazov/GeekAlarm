package com.geek_alarm.android;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * ListPreferenceWithValue adds current value to the right part of the preference.
 * http://www.androidsnippets.com/listpreference-and-edittextpreference-displaying-the-current-value
 */
public class ListPreferenceWithValue extends ListPreference {

    private TextView valueText;

    public ListPreferenceWithValue(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_with_value);
    }

    public ListPreferenceWithValue(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_with_value);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        valueText = (TextView) view.findViewById(R.id.preference_value);
        if (getValue() != null) {
            CharSequence entry = getEntries()[findIndexOfValue(getValue())];
            valueText.setText(entry);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            valueText.setText(getEntry());
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (getEntryValues() == null) {
            return;
        }
        CharSequence entry = getEntries()[findIndexOfValue(value)];
        if (valueText != null) {
            valueText.setText(entry);
        }
    }

    @Override
    public void setValueIndex(int index) {
        super.setValueIndex(index);
        CharSequence entry = getEntries()[index];
        if (valueText != null) {
            valueText.setText(entry);
        }
    }
}
