package com.geek_alarm.android;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

public class NumberPickerPreference extends DialogPreference {

    private static final int VISIBLE_ITEMS = 3;

    private int minValue;
    private int maxValue;
    private int defaultValue;
    private int step = 1;
    private WheelView wheelView;
    private TextView valueText;
    private String format;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.number_picker_preference);
        setLayoutResource(R.layout.preference_with_value);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        wheelView = (WheelView) view.findViewById(R.id.value);
        NumericWheelAdapter wheelAdapter =
                new NumericWheelAdapter(getContext(), minValue, maxValue, step, format);
        wheelAdapter.setItemResource(R.layout.wheel_text_item);
        wheelAdapter.setItemTextResource(R.id.text);
        wheelView.setViewAdapter(wheelAdapter);
        int index = (getPersistedInt(defaultValue) - minValue) / step;
        wheelView.setCurrentItem(index);
        wheelView.setVisibleItems(VISIBLE_ITEMS);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        valueText = (TextView) view.findViewById(R.id.preference_value);
        String value = String.format(format, getPersistedInt(defaultValue));
        valueText.setText(value);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int value = wheelView.getCurrentItem() * step + minValue;
            persistInt(value);
            valueText.setText(value + "s");
        }
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setFormat(String format) {
        this.format = format == null ? "%d" : format ;
    }
}
