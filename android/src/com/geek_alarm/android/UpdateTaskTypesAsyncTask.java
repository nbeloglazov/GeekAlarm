package com.geek_alarm.android;

import android.os.AsyncTask;
import android.util.Log;
import com.geek_alarm.android.db.TaskTypeDao;
import com.geek_alarm.android.tasks.TaskManager;
import com.geek_alarm.android.tasks.TaskType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs update task: get all task types from server and update local copy.
 * If we have new task - set default difficulty - MEDIUM.
 */
public class UpdateTaskTypesAsyncTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Map<String, TaskType> latestTasks = byType(TaskManager.getTaskTypes());
            Map<String, TaskType> storedTasks = byType(TaskTypeDao.INSTANCE.getAll());
            Set<String> allTypes = new HashSet<String>();
            allTypes.addAll(latestTasks.keySet());
            allTypes.addAll(storedTasks.keySet());
            for (String type : allTypes) {
                TaskType latest = latestTasks.get(type);
                TaskType stored = storedTasks.get(type);
                if (latest != null && stored == null) {
                    // Task is new. Add it to database.
                    TaskTypeDao.INSTANCE.add(latest);
                } else if (latest == null && stored != null) {
                    // Task was removed on server. Delete it.
                    TaskTypeDao.INSTANCE.delete(stored);
                } else {
                    // Task may changed. Check it and update if necessary.
                    diffAndUpdate(latest, stored);
                }
            }

        } catch (Exception e) {
            Log.e(UpdateTaskTypesAsyncTask.class.getName(), "Error during updating task", e);
        }
        return null;
    }

    private void diffAndUpdate(TaskType latest, TaskType stored) {
        boolean same = latest.getName().equals(stored.getName())
                && latest.getDescription().equals(stored.getDescription());
        if (!same) {
            latest.setLevel(stored.getLevel());
            TaskTypeDao.INSTANCE.update(latest);
        }

    }

    private Map<String, TaskType> byType(List<TaskType> taskTypes) {
        Map<String, TaskType> result = new HashMap<String, TaskType>();
        for (TaskType taskType : taskTypes) {
            result.put(taskType.getType(), taskType);
        }
        return result;
    }
}
