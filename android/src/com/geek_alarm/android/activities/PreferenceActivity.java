package com.geek_alarm.android.activities;


import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import com.geek_alarm.android.ListPreferenceWithValue;
import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;
import com.geek_alarm.android.db.TaskTypeDao;
import com.geek_alarm.android.tasks.TaskType;

public class PreferenceActivity extends android.preference.PreferenceActivity {

    // Values for task level ListPreference.
    private static final CharSequence[] LEVELS = {
            TaskType.Level.NONE.name(),
            TaskType.Level.EASY.name(),
            TaskType.Level.MEDIUM.name(),
            TaskType.Level.HARD.name()
    };
    // Key for preference responsible for setting level for all tasks at the same time.
    private static final String ALL_TASKS_PREF = "all_tasks";

    private TaskLevelChanged taskLevelChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskLevelChanged = new TaskLevelChanged();
        addPreferencesFromResource(R.xml.preferences);
        updateAlarmSoundSummary();
        initTaskLevels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // We need somehow update current alarm sound after user changed in.
        // May be there is some better way to do it (e.g. with onActivityResult) but I don't know it.
        updateAlarmSoundSummary();
    }

    /**
     * Initialize preferences for setting task levels.
     * It has 1 setting that changes levels for all tasks at the same time
     * and 1 setting for each task to set it's level.
     */
    private void initTaskLevels() {
        ListPreference allTasksPref = (ListPreference) findPreference(ALL_TASKS_PREF);
        allTasksPref.setEntries(R.array.levels);
        allTasksPref.setEntryValues(LEVELS);
        allTasksPref.setOnPreferenceChangeListener(taskLevelChanged);
        PreferenceCategory tasks = (PreferenceCategory) findPreference("tasks");
        int order = 1;
        for (TaskType taskType : TaskTypeDao.INSTANCE.getAll()) {
            Preference preference = getTaskLevelPreference(taskType);
            preference.setOrder(order++);
            tasks.addPreference(preference);

        }
    }

    private void updateAlarmSoundSummary() {
        Ringtone ringtone = RingtoneManager.getRingtone(this, Utils.getCurrentAlarmSound());
        findPreference("alarm_sound").setSummary(ringtone.getTitle(this));
    }

    /**
     * Create preference for changing task level.
     * @param taskType task for which we want to create preference.
     * @return list preference where values - {@code LEVELS}
     */
    private ListPreference getTaskLevelPreference(TaskType taskType) {
        final ListPreference preference = new ListPreferenceWithValue(this);
        preference.setKey(taskType.getType());
        preference.setEntries(R.array.levels);
        preference.setEntryValues(LEVELS);
        preference.setTitle(taskType.getName());
        preference.setValue(taskType.getLevel().name());
        preference.setLayoutResource(R.layout.preference_with_value);
        preference.setOnPreferenceChangeListener(taskLevelChanged);
        return preference;
    }

    /**
     * Listener to save all changes made to task levels.
     */
    private class TaskLevelChanged implements Preference.OnPreferenceChangeListener {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals(ALL_TASKS_PREF)) {
                // Modify all tasks.
                TaskType.Level level = TaskType.Level.valueOf(newValue.toString());
                for (TaskType taskType : TaskTypeDao.INSTANCE.getAll()) {
                    taskType.setLevel(level);
                    TaskTypeDao.INSTANCE.update(taskType);
                    ListPreference listPreference = (ListPreference) findPreference(taskType.getType());
                    listPreference.setValue(newValue.toString());

                }
            } else {
                // Modify single task.
                TaskType taskType = TaskTypeDao.INSTANCE.findByType(preference.getKey());
                taskType.setLevel(TaskType.Level.valueOf(newValue.toString()));
                TaskTypeDao.INSTANCE.update(taskType);
            }
            return true;
        }
    }
}
