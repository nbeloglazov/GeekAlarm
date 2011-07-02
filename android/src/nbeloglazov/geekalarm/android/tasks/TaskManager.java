package nbeloglazov.geekalarm.android.tasks;

import nbeloglazov.geekalarm.android.R;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public final class TaskManager {
	
	private TaskManager() {} 
	
	public static Task getTestTask(Resources resources) {
		Task task = new Task();
		task.setQuestion(BitmapFactory.decodeResource(resources, R.drawable.question_test));
		int choicesIds[] = {R.drawable.choice1_test, 
				         R.drawable.choice2_test, 
				         R.drawable.choice3_test, 
				         R.drawable.choice4_test};
		Bitmap choices[] = new Bitmap[4];
		for (int i = 0; i < 4; i++) {
			choices[i] = BitmapFactory.decodeResource(resources, choicesIds[i]);
		}
		task.setChoices(choices);
		task.setCorrect(4);
		return task;
	}	

}
