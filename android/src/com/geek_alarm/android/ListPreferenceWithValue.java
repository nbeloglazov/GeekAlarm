package com.geek_alarm.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.view.View;
import android.widget.TextView;

public class ListPreferenceWithValue extends ListPreference {

    private TextView valueText;

    public ListPreferenceWithValue(Context context) {
        super(context);
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
