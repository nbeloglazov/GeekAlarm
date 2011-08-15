package nbeloglazov.geekalarm.android.activities;

import java.util.ArrayList;
import java.util.List;

import nbeloglazov.geekalarm.android.AlarmSound;
import nbeloglazov.geekalarm.android.R;
import nbeloglazov.geekalarm.android.Utils;
import nbeloglazov.geekalarm.android.adapters.AlarmSoundAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;

public class AlarmSoundPickerActivity extends Activity {
    
    private static String[] GEEK_ALARMS_TITLES = {
        "Into the sun",
        "Mario",
        "Ultrachip set sketch",
        "Zero"};
    
    private static int[] GEEK_ALARMS_RES = {
        R.raw.into_the_sun,
        R.raw.mario,
        R.raw.ultrachip_set_sketch,
        R.raw.zero};

    private AlarmSoundAdapter adapter;
    private MediaPlayer player;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_sound_picker);
        findViewById(R.id.ok).setOnClickListener(new OkListener());
        findViewById(R.id.cancel).setOnClickListener(new CancelListener());
        setUpListView();
        setSelected(Utils.getCurrentAlarmSound());
        player = new MediaPlayer();
    }   
    
    private void setUpListView() {
        List<AlarmSound> sounds = new ArrayList<AlarmSound>();
        sounds.addAll(getGeekAlarms());
        sounds.addAll(getStandardAlarms());
        adapter = 
            new AlarmSoundAdapter(this, sounds, new SoundClickListener());
        ListView listView = (ListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);
    }
    
    private void setSelected(Uri sound) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getUri().equals(sound)) {
                adapter.setSelected(i);
                return;
            }
        }
        AlarmSound defSound = new AlarmSound("Default", sound);
        adapter.add(defSound);
    }
    
    private List<AlarmSound> getStandardAlarms() {
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_RINGTONE);
        List<AlarmSound> sounds = new ArrayList<AlarmSound>();
        Cursor cursor = manager.getCursor();
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext(), i++) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            uri += "/" + cursor.getInt(RingtoneManager.ID_COLUMN_INDEX);
            sounds.add(new AlarmSound(title, manager.getRingtoneUri(i)));
        }   
        cursor.deactivate();
        return sounds;
    }
    
    private List<AlarmSound> getGeekAlarms() {
        List<AlarmSound> sounds = new ArrayList<AlarmSound>();
        for (int i = 0; i < GEEK_ALARMS_RES.length; i++) {
            sounds.add(new AlarmSound(
                    GEEK_ALARMS_TITLES[i], 
                    Utils.getUriFromResource(GEEK_ALARMS_RES[i])));
        }
        return sounds;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    public class SoundClickListener {

        public void onClick(int position) {
            AlarmSound sound = adapter.getItem(position);
            try {
                player.reset();
                player.setDataSource(AlarmSoundPickerActivity.this, sound.getUri());
                player.prepare();
                player.start();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }     
        }
    }
    
    private class OkListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Uri uri = adapter.getItem(adapter.getSelected()).getUri();
            Utils.setCurrentAlarmSound(uri);
            finish();
        }
        
    }
    
    private class CancelListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            finish();
        }

    }
}
