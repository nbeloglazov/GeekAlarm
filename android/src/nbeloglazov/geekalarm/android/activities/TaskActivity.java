package nbeloglazov.geekalarm.android.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import nbeloglazov.geekalarm.android.R;
import nbeloglazov.geekalarm.android.tasks.Category;
import nbeloglazov.geekalarm.android.tasks.Configuration;
import nbeloglazov.geekalarm.android.tasks.Task;
import nbeloglazov.geekalarm.android.tasks.TaskManager;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskActivity extends Activity {

    private static final int MAX_NUM_OF_TASKS = 10;
    private static final int TASKS_TO_FINISH = 3;
    private static final long PLAY_DELAY = 60 * 1000; // 60 seconds

    private boolean isWaitingForTask;
    private Queue<Task> availableTasks;
    private ChoiceListener choiceListener;
    private int correctChoiceId;
    private TaskLoader loader;
    private MediaPlayer player;
    private int solved;
    private int all;
    private Timer timer;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);
        player = MediaPlayer.create(this, R.raw.mario);
        choiceListener = new ChoiceListener();
        availableTasks = new LinkedList<Task>();
        isWaitingForTask = true;
        loader = new TaskLoader();
        loader.execute(Configuration.getDefaultConfiguration());
        timer = new Timer();
        findViewById(R.id.mute_button).setOnClickListener(new MuteListener());
        updateStats();
    }

    private void displayTask(Task task) {
        ImageView question = (ImageView) findViewById(R.id.task_question);
        question.setImageBitmap(task.getQuestion());
        int choicesIds[] = { R.id.task_choice_1, R.id.task_choice_2,
                R.id.task_choice_3, R.id.task_choice_4 };
        correctChoiceId = choicesIds[task.getCorrect() - 1];
        for (int i = 0; i < 4; i++) {
            ImageView choiceView = (ImageView) findViewById(choicesIds[i]);
            choiceView.setOnClickListener(choiceListener);
            choiceView.setImageBitmap(task.getChoice(i));
        }
        currentTask = task;
    }

    private void updateStats() {
        TextView view = (TextView) findViewById(R.id.stat_text);
        view.setText(String.format("%d/%d", solved, all));
    }
    
    private class ChoiceListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            all++;
            solved += v.getId() == correctChoiceId ? 1 : 0;
            if (solved == TASKS_TO_FINISH || all == MAX_NUM_OF_TASKS) {
                TaskActivity.this.finish();
                return;
            }
            updateStats();
            Toast.makeText(getApplicationContext(),
                    v.getId() == correctChoiceId ? "Accepted" : "Wrong answer",
                    Toast.LENGTH_SHORT).show();
            if (availableTasks.isEmpty()) {
                if (loader.getStatus() == AsyncTask.Status.FINISHED) {
                    loader = new TaskLoader();
                    loader.execute(Configuration.getDefaultConfiguration());
                }
                isWaitingForTask = true;
            } else {
                displayTask(availableTasks.poll());
            }
        }
    }
    
    @Override
    public void onBackPressed() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
        }
        timer.cancel();
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
            if (isWaitingForTask) {
                player.start();
                Task task = availableTasks.poll();
                displayTask(task);
                isWaitingForTask = false;
            }
        }
    }

    private class MuteListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (player.isPlaying()) {
                player.pause();
                timer.schedule(new ContinuePlayTask(), PLAY_DELAY);
            }
        }
    }

    private class ContinuePlayTask extends TimerTask {

        @Override
        public void run() {
            player.start();
        }
    }

}
