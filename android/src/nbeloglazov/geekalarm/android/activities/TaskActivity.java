package nbeloglazov.geekalarm.android.activities;


import nbeloglazov.geekalarm.android.R;
import nbeloglazov.geekalarm.android.tasks.Category;
import nbeloglazov.geekalarm.android.tasks.Task;
import nbeloglazov.geekalarm.android.tasks.TaskManager;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

public class TaskActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task);
		Category cat = new Category("linear-algebra", "");
		try {
			Task testTask = TaskManager.getTask(cat, 3);
			loadTask(testTask);
		} catch (Exception e) {
			Log.e("TaskActivity", "Bad", e);
		}
	}
	
	private void loadTask(Task task) {
		ImageView question = (ImageView)findViewById(R.id.task_question);
		question.setImageBitmap(task.getQuestion());
		int choicesIds[] = {R.id.task_choice_1, 
				            R.id.task_choice_2,
				            R.id.task_choice_3,
				            R.id.task_choice_4};
		for (int i = 0; i < 4; i++) {
			ImageView choice = (ImageView)findViewById(choicesIds[i]);
			choice.setImageBitmap(task.getChoice(i));
		}
	}

}
