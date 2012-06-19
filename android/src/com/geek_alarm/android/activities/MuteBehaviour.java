package com.geek_alarm.android.activities;

public enum MuteBehaviour {
    DECREASE, WITHOUT_CHANGES, INCREASE;

    public static CharSequence[] getNames() {
        return new String[] {DECREASE.name(), WITHOUT_CHANGES.name(), INCREASE.name()};
    }
}
