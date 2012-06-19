package com.geek_alarm.android;

import com.geek_alarm.android.activities.MuteBehaviour;

public class MuteUtils {
    public static final String INITIAL_MUTE_TIME = "initialMuteTime";
    public static final String MUTE_TIME_STEP = "muteTimeStep";
    public static final String MUTE_BEHAVIOUR = "muteBehaviour";
    public static final int DEFAULT_INITIAL_MUTE_TIME = 40;
    public static final int DEFAULT_MUTE_TIME_STEP = 10;
    public static final MuteBehaviour DEFAULT_BEHAVIOUR = MuteBehaviour.WITHOUT_CHANGES;

    public static int getInitialMuteTime() {
        return Utils.getPreferences().getInt(INITIAL_MUTE_TIME, DEFAULT_INITIAL_MUTE_TIME);
    }

    public static int getMuteTimeStep() {
        return Utils.getPreferences().getInt(MUTE_TIME_STEP, DEFAULT_MUTE_TIME_STEP);
    }

    public static MuteBehaviour getMuteBehaviour() {
        String behaviour = Utils.getPreferences().getString(MUTE_BEHAVIOUR, DEFAULT_BEHAVIOUR.name());
        return MuteBehaviour.valueOf(behaviour);
    }
}
