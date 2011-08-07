package nbeloglazov.geekalarm.android.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import nbeloglazov.geekalarm.android.AlarmPreference;
import nbeloglazov.geekalarm.android.DBUtils;
import nbeloglazov.geekalarm.android.R;
import nbeloglazov.geekalarm.android.Utils;
import nbeloglazov.geekalarm.android.tasks.Category;
import nbeloglazov.geekalarm.android.tasks.Configuration;
import nbeloglazov.geekalarm.android.tasks.Task;
import nbeloglazov.geekalarm.android.tasks.TaskManager;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class TaskActivity extends Activity {

    private static final int MAX_NUM_OF_TASKS = 10;
    private static final int TASKS_TO_FINISH = 3;
    private static final long BASE_PLAY_DELAY = 40 * 1000; // 40 seconds
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
        layout = (LinearLayout)inflater.inflate(R.layout.task, null);
        setContentView(layout);
        createPlayer();
        choiceListener = new ChoiceListener();
        availableTasks = new LinkedList<Task>();
        waitingForTask = true;
        loader = new TaskLoader();
        loader.execute(Configuration.getDefaultConfiguration());
        timer = new Timer();
        findViewById(R.id.mute_button).setOnClickListener(new MuteListener());
        updateStats();
        curPlayDelay = BASE_PLAY_DELAY;
    }
    
    private void createPlayer() {
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.mario);
        player = new MediaPlayer();
        try {
            player.setDataSource(afd.getFileDescriptor());
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.prepare();
            afd.close();
        } catch(Exception e) {
            Log.e(this.getClass().getName(), ":(", e);
        }
    }
    
    private boolean containsToday() {
        Log.i("", "Checking day");
        int id = Integer.parseInt(getIntent().getData().getEncodedSchemeSpecificPart());
        AlarmPreference alarm = DBUtils.getAlarmPreference(id);
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_WEEK);
        Log.i("", "Today is " + Utils.getDayOfWeek(today) + " days " + alarm.getDays());
        return (alarm.getDays() & (1 << Utils.getDayOfWeek(today))) != 0; 
    }

    private void displayTask(Task task) {
        ImageView question = (ImageView) findViewById(R.id.task_question);
        int choiceWidth = task.getChoice(0).getWidth();
        Display display = getWindowManager().getDefaultDisplay();
        boolean isTable = choiceWidth * 2 + 10 < display.getWidth();
        int minHeight = layout.getBottom() / 2 / 4;
        layout.addView(inflater.inflate(isTable ? R.layout.choices_table : R.layout.choices_list, null),
                       new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.5f));
        question.setImageBitmap(task.getQuestion());
        
        int choicesIds[] = { R.id.task_choice_1, R.id.task_choice_2,
                R.id.task_choice_3, R.id.task_choice_4 };
        correctChoiceId = choicesIds[task.getCorrect() - 1];
        currentTask = task;
        for (int i = 0; i < 4; i++) {
            ImageView choiceView = (ImageView) findViewById(choicesIds[i]);
            choiceView.setOnClickListener(choiceListener);
            choiceView.setImageBitmap(task.getChoice(i));
            if (!isTable) {
                choiceView.getLayoutParams().height = Math.max(minHeight, task.getChoice(i).getHeight());
            }
        }
    }

    private void updateStats() {
        TextView view = (TextView) findViewById(R.id.stat_text);
        view.setText(String.format("%d/%d", 2 * solved - all, TASKS_TO_FINISH));
    }
    
    private class ChoiceListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            all++;
            solved += v.getId() == correctChoiceId ? 1 : 0;
            new ResultSender(currentTask.getId(), v.getId() == correctChoiceId).execute();
            if (2 * solved - all == TASKS_TO_FINISH || all == MAX_NUM_OF_TASKS) {
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
                    loader.execute(Configuration.getDefaultConfiguration());
                }
                waitingForTask = true;
            } else {
                displayTask(availableTasks.poll());
            }
        }
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
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    private class TaskLoader extends AsyncTask<Configuration, Task, Void> {

        @Override
        protected Void doInBackground(Configuration... params) {
            Configuration conf = params[0];
            List<Map.Entry<Category, Integer>> categories = new ArrayList(conf
                    .getCategories().entrySet());
            Collections.shuffle(categories);
            for (int i = 0; i < MAX_NUM_OF_TASKS; i++) {
                Map.Entry<Category, Integer> taskType = categories.get(i
                        % categories.size());
                try {
                    Task task = TaskManager.getTask(taskType.getKey(),
                            taskType.getValue());
                    publishProgress(task);
                } catch (Exception e) {
                    Log.e(TaskLoader.class.getName(), "Something bad", e);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Task... values) {
            for (Task task : values) {
                availableTasks.add(task);
            }
            if (waitingForTask) {
                if (!started) {
                    player.start();
                }
                started = true;
                Task task = availableTasks.poll();
                displayTask(task);
                waitingForTask = false;
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
