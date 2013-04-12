package com.geek_alarm.android.activities;


import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.Log;
import com.geek_alarm.android.ListPreferenceWithValue;
import com.geek_alarm.android.MuteUtils;
import com.geek_alarm.android.NumberPickerPreference;
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
        initMuteTime();
        initTaskLevels();
        initNumberOfTasks();
        initMailTo();
        Log.e("#######", Utils.getPreferences().getAll().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // We need somehow update current alarm sound after user changed in.
        // May be there is some better way to do it (e.g. with onActivityResult) but I don't know it.
        updateAlarmSoundSummary();
    }

    private void initMailTo() {
        Preference mailTo = findPreference("feedback");
        mailTo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent mailto = new Intent(Intent.ACTION_SEND);
                mailto.setType("text/plain");
                mailto.putExtra(Intent.EXTRA_EMAIL, new String[]{"feedback@geek-alarm.com"});
                mailto.putExtra(Intent.EXTRA_SUBJECT,"Feedback");
                startActivity(Intent.createChooser(mailto, "Select email application."));
                return true;
            }
        });
    }

    private void initNumberOfTasks() {
        initNumberPicker(Utils.NUMBER_OF_ATTEMPTS, Utils.DEFAULT_NUMBER_OF_ATTEMPTS, 1, 100, 1, null);
        initNumberPicker(Utils.POSITIVE_BALANCE, Utils.DEFAULT_POSITIVE_BALANCE, 1, 100, 1, null);
    }

    private void initMuteTime() {
        initNumberPicker(MuteUtils.INITIAL_MUTE_TIME, MuteUtils.DEFAULT_INITIAL_MUTE_TIME, 5, 300, 5,"%ds");

        initNumberPicker(MuteUtils.MUTE_TIME_STEP, MuteUtils.DEFAULT_MUTE_TIME_STEP, -300, 300, 5, "%ds");
    }

    private void initNumberPicker(String key, int defaultValue, int minValue, int maxValue, int step, String format) {
        NumberPickerPreference preference = (NumberPickerPreference) findPreference(key);
        preference.setDefaultValue(defaultValue);
        preference.setMinValue(minValue);
        preference.setMaxValue(maxValue);
        preference.setStep(step);
        preference.setFormat(format);
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
        allTasksPref.setPersistent(false);
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
        String title = ringtone == null ? "" : ringtone.getTitle(this);
        findPreference("alarm_sound").setSummary(title);
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
        preference.setPersistent(false);
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
