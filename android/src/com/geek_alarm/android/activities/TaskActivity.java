package com.geek_alarm.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.geek_alarm.android.AlarmPreference;
import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;
import com.geek_alarm.android.db.AlarmPreferenceDao;
import com.geek_alarm.android.db.TaskTypeDao;
import com.geek_alarm.android.tasks.Task;
import com.geek_alarm.android.tasks.TaskManager;
import com.geek_alarm.android.tasks.TaskType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity, it is place where user solves tasks. 
 */
public class TaskActivity extends Activity {

    private static final int MAX_NUM_OF_TASKS = 10;
    // This const indicates how many tasks user must 
    // solve correctly than incorrectly to dismiss alarm.
    private static final int TASKS_TO_FINISH = 3;
    // Start music mute.
    private static final long BASE_PLAY_DELAY = 40 * 1000; // 40 seconds
    // Increasing music mute each time.
    private static final long PLAY_DELAY_INCREASE = 10 * 1000; // 10 seconds

    private long curPlayDelay;
    private boolean waitingForTask;
    private boolean started;
    private boolean testTask;
    private Queue<Task> availableTasks;
    private ChoiceListener choiceListener;
    private int correctChoiceId;
    private TaskLoader loader;
    private MediaPlayer player;
    private int solved;
    // How many tasks user already tried: solved + unsolved.
    private int all;
    private Timer timer;
    private LayoutInflater inflater;
    private LinearLayout layout;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getData() == null) {
            testTask = true;
        } else if (!containsToday()) {
            finish();
            return;
        }
        inflater = getLayoutInflater();
        layout = (LinearLayout) inflater.inflate(R.layout.task, null);
        setContentView(layout);
        createPlayer();
        choiceListener = new ChoiceListener();
        availableTasks = new LinkedList<Task>();
        waitingForTask = true;
        Utils.updateTaskTypesAsync(true);
        runTaskLoader();
        timer = new Timer();
        findViewById(R.id.mute_button).setOnClickListener(new MuteListener());
        findViewById(R.id.info_button).setOnClickListener(new InfoListener());
        updateStats();
        curPlayDelay = BASE_PLAY_DELAY;
    }

    private void runTaskLoader() {
        loader = new TaskLoader();
        loader.execute();
    }

    private void createPlayer() {
        Uri music = Utils.getCurrentAlarmSound();
        player = new MediaPlayer();
        try {
            player.setDataSource(this, music);
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
        } catch (Exception e) {
            // Suppose we can't play current music, because it's renamed/removed.
            // Play standard mario theme.
            player.reset();
            try {
                player.setDataSource(this,
                        Utils.getUriFromResource(R.raw.mario));
                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.setLooping(true);
                player.prepare();
            } catch (Exception e2) {
                Log.e(this.getClass().getName(), "Now I don't know what to do.", e2);
            }
        }
    }

    /**
     * Checks, if today's day is enable in current alarm.
     * @return true or false (amazing, really?).
     */
    private boolean containsToday() {
        int id = Integer.parseInt(getIntent().getData()
                .getEncodedSchemeSpecificPart());
        AlarmPreference alarm = AlarmPreferenceDao.INSTANCE.findById(id);
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_WEEK);
        return (alarm.getDays() & (1 << Utils.getDayOfWeek(today))) != 0;
    }

    /**
     * Shows (or removes) error message with given id.
     * @param errorMessageId resource id of message, -1 if no error.
     */
    private void showErrorMessage(int errorMessageId) {
        TextView errorView = (TextView) findViewById(R.id.error_message);
        if (errorMessageId == -1) {
            errorView.setVisibility(View.GONE);
        } else {
            errorView.setText(errorMessageId);
            errorView.setVisibility(View.VISIBLE);
        }
    }

    private void toggleSpinner(boolean show) {
        findViewById(R.id.progress).setVisibility(
                show ? View.VISIBLE : View.GONE);
        findViewById(R.id.task_question).setVisibility(
                show ? View.GONE : View.VISIBLE);
    }

    private void displayTask(Task task) {
        toggleSpinner(false);
        showErrorMessage(task.getErrorMessageId());
        ImageView question = (ImageView) findViewById(R.id.task_question);
        int choiceWidth = task.getChoice(0).getWidth();
        Display display = getWindowManager().getDefaultDisplay();
        // If images is too wide, we need to use layout 4x1 instead of table 2x2.
        boolean isTable = choiceWidth * 2 + 10 < display.getWidth();
        float weight = 0.55f;
        int minHeight = (int) (layout.getBottom() * (1 - weight) / 4);
        layout.addView(inflater.inflate(isTable ? R.layout.choices_table
                : R.layout.choices_list, null), new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, weight));
        question.setImageBitmap(task.getQuestion());

        int choicesIds[] = { R.id.task_choice_1, R.id.task_choice_2,
                R.id.task_choice_3, R.id.task_choice_4 };
        correctChoiceId = choicesIds[task.getCorrect()];
        currentTask = task;
        for (int i = 0; i < 4; i++) {
            ImageView choiceView = (ImageView) findViewById(choicesIds[i]);
            choiceView.setOnClickListener(choiceListener);
            choiceView.setImageBitmap(task.getChoice(i));
            if (!isTable) {
                choiceView.getLayoutParams().height = Math.max(minHeight, task
                        .getChoice(i).getHeight());
            }
        }
    }

    private void updateStats() {
        TextView solvedView = (TextView) findViewById(R.id.solved);
        solvedView.setText(String.format("%d/%d", 2 * solved - all,
                TASKS_TO_FINISH));
        TextView leftView = (TextView) findViewById(R.id.left);
        leftView.setText(String.valueOf(MAX_NUM_OF_TASKS - all));
    }

    @Override
    public void onBackPressed() {
        if (testTask) {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Loads MAX_NUM_OF_TASKS of random tasks from server.
     */
    private class TaskLoader extends AsyncTask<Void, Task, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (Utils.isOnline()) {
                List<TaskType> taskTypes = removeDisabledTasks(TaskTypeDao.INSTANCE.getAll());
                if (!taskTypes.isEmpty()) {
                    downloadTasks(taskTypes);
                } else {
                    generateSimpleTasks(TaskType.Level.MEDIUM.getValue(), R.string.server_error);
                }
            } else {
                generateSimpleTasks(TaskType.Level.MEDIUM.getValue(), R.string.not_online);
            }
            return null;
        }

        private void generateSimpleTasks(int difficulty, int errorMessageId) {
            for (int i = 0; i < MAX_NUM_OF_TASKS; i++) {
                Task task = TaskManager.generateSimpleTask(difficulty);
                task.setErrorMessageId(errorMessageId);
                publishProgress(task);
            }
        }

        private List<TaskType> removeDisabledTasks(List<TaskType> taskTypes) {
            List<TaskType> result = new ArrayList<TaskType>();
            for (TaskType type : taskTypes) {
                if (type.getLevel() != TaskType.Level.NONE) {
                    result.add(type);
                }
            }
            return result;
        }

        private void downloadTasks(List<TaskType> taskTypes) {
            Collections.shuffle(taskTypes);
            for (int i = 0; i < MAX_NUM_OF_TASKS; i++) {
                TaskType type = taskTypes.get(i % taskTypes.size());
                Task task;
                try {
                    task = TaskManager.getTask(type);
                    task.setErrorMessageId(-1);
                } catch (Exception e) {
                    // Some error, so we show simple task.
                    Log.e(TaskLoader.class.getName(), "Something bad", e);
                    task = TaskManager.generateSimpleTask(type.getLevel().getValue());
                    task.setErrorMessageId(R.string.server_error);
                }
                publishProgress(task);
            }
        }

        @Override
        protected void onProgressUpdate(Task... values) {
            for (Task task : values) {
                availableTasks.add(task);
            }
            if (waitingForTask) {
                if (!started && player != null) {
                    player.start();
                }
                started = true;
                Task task = availableTasks.poll();
                displayTask(task);
                waitingForTask = false;
            }
        }
    }

    private class ChoiceListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            all++;
            solved += v.getId() == correctChoiceId ? 1 : 0;
            new ResultSender(currentTask.getId(), v.getId() == correctChoiceId)
                    .execute();
            if (2 * solved - all == TASKS_TO_FINISH || all == MAX_NUM_OF_TASKS) {
                boolean win = 2 * solved - all == TASKS_TO_FINISH;
                if (!testTask) {
                    Intent intent = new Intent(TaskActivity.this, ResultActivity.class);
                    intent.putExtra("win", win);
                    intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                    startActivity(intent);
                }
                TaskActivity.this.finish();
                return;
            }
            layout.removeViewAt(layout.getChildCount() - 1);
            updateStats();
            Toast.makeText(getApplicationContext(),
                    v.getId() == correctChoiceId ? "Accepted" : "Wrong answer",
                    Toast.LENGTH_SHORT).show();
            if (availableTasks.isEmpty()) {
                if (loader.getStatus() == AsyncTask.Status.FINISHED) {
                    loader = new TaskLoader();
                    loader.execute();
                }
                waitingForTask = true;
                toggleSpinner(true);
            } else {
                displayTask(availableTasks.poll());
            }
        }
    }

    private class MuteListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (player.isPlaying()) {
                player.pause();
                timer.schedule(new ContinuePlayTask(), curPlayDelay);
                curPlayDelay += PLAY_DELAY_INCREASE;
            }
        }
    }

    private class InfoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (currentTask == null) {
                return;
            }
            SpannableString info = new SpannableString(currentTask.getType().getDescription());
            Linkify.addLinks(info, Linkify.ALL);
            AlertDialog dialog = new AlertDialog.Builder(TaskActivity.this)
                .setTitle(currentTask.getType().getName())
                .setMessage(info)
                .setNeutralButton(R.string.hide, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }})
                .show();
            ((TextView)dialog.findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private class ContinuePlayTask extends TimerTask {

        @Override
        public void run() {
            player.start();
        }
    }

    private class ResultSender extends AsyncTask<Void, Void, Void> {

        private String id;
        private boolean solved;

        public ResultSender(String id, boolean solved) {
            this.id = id;
            this.solved = solved;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                TaskManager.addResult(id, solved);
            } catch (Exception e) {
                Log.e(TaskLoader.class.getName(), "Couldn't send result", e);
            }
            return null;
        }
    }

}
