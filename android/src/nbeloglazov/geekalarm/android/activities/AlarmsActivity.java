package nbeloglazov.geekalarm.android.activities;

import nbeloglazov.geekalarm.android.R;
import nbeloglazov.geekalarm.android.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AlarmsActivity extends Activity {

    private static String TAG = "geekalarm";

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarms);
        Button testButton = (Button)this.findViewById(R.id.test_button);
        testButton.setOnClickListener(new TestButtonListener());
		Log.i(TAG, "onCreate");
    }
    
    private class TestButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(AlarmsActivity.this, TaskActivity.class);
			startActivity(intent);
		}
    	
    }
}

