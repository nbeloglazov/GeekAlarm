package com.geek_alarm.android;

public class MuteUtils {
    public static final String INITIAL_MUTE_TIME = "initialMuteTime";
    public static final String MUTE_TIME_STEP = "muteTimeStep";
    public static final int DEFAULT_INITIAL_MUTE_TIME = 40;
    public static final int DEFAULT_MUTE_TIME_STEP = 10;

    public static int getInitialMuteTime() {
        return Utils.getPreferences().getInt(INITIAL_MUTE_TIME, DEFAULT_INITIAL_MUTE_TIME);
    }

    public static int getMuteTimeStep() {
        return Utils.getPreferences().getInt(MUTE_TIME_STEP, DEFAULT_MUTE_TIME_STEP);
    }
}
