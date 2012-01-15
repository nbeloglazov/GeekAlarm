package com.geek_alarm.android.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;

import com.geek_alarm.android.AlarmSound;
import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;
import com.geek_alarm.android.adapters.AlarmSoundAdapter;
import com.lamerman.FileDialog;

/**
 * Activity for selecting sound.
 * It displays:
 * 1. option to choose custom sound.
 * 2. built in mario sound.
 * 3. all alarm and ringtone sounds available on device.
 *
 */
public class AlarmSoundPickerActivity extends Activity {

    private static String[] GEEK_ALARMS_TITLES = {
        "Mario"};

    private static int[] GEEK_ALARMS_RES = {
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
        // Add option to choose custom sound.
        sounds.add(new AlarmSound(getResources().getString(R.string.custom),
                                  null));
        // Add built in alarms.
        sounds.addAll(getGeekAlarms());
        // Add android alarms.
        sounds.addAll(getStandardAlarms());
        adapter =
            new AlarmSoundAdapter(this, sounds, new SoundClickListener());
        ListView listView = (ListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);
    }

    /**
     * Looks up sound in available sounds and selects option, if found.
     * If no option found, sets first "Custom" selected.
     * @param sound
     */
    private void setSelected(Uri sound) {
        // Skip sound at position 0, because it is custom sound
        // with uri equals to null.
        for (int i = 1; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getUri().equals(sound)) {
                adapter.setSelected(i);
                return;
            }
        }
        adapter.getItem(0).setUri(sound);
        adapter.setSelected(0);
    }

    /**
     * Retrieves list of standard android sounds.
     * It gets TYPE_ALARM and TYPE_RINGTONE sounds.
     * @return list
     */
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

    /**
     * Creates list of built in sounds.
     * @return list
     */
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

    /**
     * Callback, which passed to AlarmSoundAdapter.
     * It's invoked when user select any sound.
     * This callback starts playing selected sound or
     * open file dialog, if user selected "Custom" option.
     */
    public class SoundClickListener {

        public void onClick(int position) {
            AlarmSound sound = adapter.getItem(position);
            // Open file dialog if "Custom" option selected.
            if (position == 0) {
                if (player.isPlaying()) {
                    player.stop();
                }
                Intent intent = new Intent(AlarmSoundPickerActivity.this, FileDialog.class);
                String startUri = adapter.getItem(0).getUri() == null ? "" : adapter.getItem(0).getUri().toString();
                intent.putExtra(FileDialog.START_URI, startUri);
                startActivityForResult(intent, 0);
                return;
            }
            // Play music otherwise.
            try {
                player.reset();
                player.setDataSource(AlarmSoundPickerActivity.this, sound.getUri());
                player.prepare();
                player.start();
            } catch (Exception e) {
                // Don't know what should I do here.
                Log.e(getClass().getName(), "Can't play sound", e);
            }
        }
    }

    /**
     * Invoked after user has selected music in filedialog or canceled it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            this.adapter.selectPrevious();
        } else if (resultCode == Activity.RESULT_OK) {
            Uri uri = Uri.parse(data.getStringExtra(FileDialog.RESULT_URI));
            this.adapter.getItem(0).setUri(uri);
            saveCurrentSoundAndExit();
        }
    }

    private void saveCurrentSoundAndExit() {
        Uri uri = adapter.getItem(adapter.getSelected()).getUri();
        Utils.setCurrentAlarmSound(uri);
        finish();
    }

    private class OkListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            saveCurrentSoundAndExit();
        }

    }

    private class CancelListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            finish();
        }

    }
}
