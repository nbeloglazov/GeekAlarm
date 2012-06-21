package com.geek_alarm.android;

import android.app.Activity;
import android.text.format.DateFormat;
import android.view.View;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

public final class ActivityUtils {

    private static final int VISIBLE_ITEMS = 3;

    private ActivityUtils() {}

    public static void initWheelTimePicker(Activity activity, int hour, int minute, int itemResource) {
        boolean is24Hour = DateFormat.is24HourFormat(activity);
        boolean isAM = hour < 12;
        initHoursPicker(activity, hour, is24Hour, itemResource);
        initMinutesPicker(activity, minute, itemResource);
        initAMPMPicker(activity, isAM, is24Hour, itemResource);
    }

    private static void initHoursPicker(Activity activity, int hour, boolean is24Hour, int itemResource) {
        if (!is24Hour) {
            hour %= 12;
            if (hour == 0) {
                hour = 12;
            }
            hour--;
        }
        final WheelView hours = (WheelView) activity.findViewById(R.id.hour);
        NumericWheelAdapter hourAdapter = new NumericWheelAdapter(activity, is24Hour ? 0 : 1, is24Hour ? 23 : 12);
        hourAdapter.setItemResource(itemResource);
        hourAdapter.setItemTextResource(R.id.text);
        hours.setViewAdapter(hourAdapter);
        hours.setCyclic(true);
        hours.setCurrentItem(hour);
        hours.setVisibleItems(VISIBLE_ITEMS);
    }

    private static void initMinutesPicker(Activity activity, int minute, int itemResource) {
        final WheelView mins = (WheelView) activity.findViewById(R.id.mins);
        NumericWheelAdapter minAdapter = new NumericWheelAdapter(activity, 0, 59, "%02d");
        minAdapter.setItemResource(itemResource);
        minAdapter.setItemTextResource(R.id.text);
        mins.setViewAdapter(minAdapter);
        mins.setCyclic(true);
        mins.setCurrentItem(minute);
        mins.setVisibleItems(VISIBLE_ITEMS);
    }

    private static void initAMPMPicker(Activity activity, boolean isAM, boolean is24Hour, int itemResource) {
        final WheelView ampm = (WheelView) activity.findViewById(R.id.ampm);
        if (is24Hour) {
            ampm.setVisibility(View.GONE);
            return;
        }
        ArrayWheelAdapter<String> ampmAdapter =
                new ArrayWheelAdapter<String>(activity, new String[] {"AM", "PM"});
        ampmAdapter.setItemResource(itemResource);
        ampmAdapter.setItemTextResource(R.id.text);
        ampm.setViewAdapter(ampmAdapter);
        ampm.setCurrentItem(isAM ? 0 : 1);
        ampm.setVisibleItems(VISIBLE_ITEMS);
    }

    public static int getMinute(Activity activity) {
        WheelView mins = (WheelView) activity.findViewById(R.id.mins);
        return mins.getCurrentItem();
    }

    public static int getHour(Activity activity) {
        final WheelView hours = (WheelView) activity.findViewById(R.id.hour);
        boolean is24Hour = DateFormat.is24HourFormat(activity);
        if (is24Hour) {
            return hours.getCurrentItem();
        } else {
            final WheelView ampm = (WheelView) activity.findViewById(R.id.ampm);
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
