package com.geekalarm.android.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;

import com.geekalarm.android.AlarmSound;
import com.geekalarm.android.R;
import com.geekalarm.android.Utils;
import com.geekalarm.android.adapters.AlarmSoundAdapter;
import com.lamerman.FileDialog;

public class AlarmSoundPickerActivity extends Activity {
    
    private static String[] GEEK_ALARMS_TITLES = {
        "Into the sun",
        "Mario"};
    
    private static int[] GEEK_ALARMS_RES = {
        R.raw.into_the_sun,
        R.raw.mario};

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
        sounds.add(new AlarmSound("Custom", Uri.parse("http://geek-alarm.com")));
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
        adapter.getItem(0).setUri(sound);
        adapter.setSelected(0);
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
            if (position == 0) {
                if (player.isPlaying()) {
                    player.stop();
                }
                Intent intent = new Intent(AlarmSoundPickerActivity.this, FileDialog.class);
                intent.putExtra(FileDialog.START_URI, 
                        adapter.getItem(0).getUri().toString());
                startActivityForResult(intent, 0);
                return;
            }
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            this.adapter.selectPrevious();
        } else if (resultCode == Activity.RESULT_OK) {
            Uri uri = Uri.parse(data.getStringExtra(FileDialog.RESULT_URI));
            this.adapter.getItem(0).setUri(uri);
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
