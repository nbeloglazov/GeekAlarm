package com.geek_alarm.android;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class Player {

    private static final String TAG = Player.class.getName();
    private MediaPlayer player;
    private boolean started;
    private Context context;
    private long curPlayDelay;
    private Timer timer;

    public Player(Context context) {
        this.context = context;
        initPlayer();
        timer = new Timer();
        curPlayDelay = MuteUtils.getInitialMuteTime() * 1000;
    }

    private void initPlayer() {
        Uri music = Utils.getCurrentAlarmSound();
        player = new MediaPlayer();
        try {
            player.setDataSource(context, music);
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
        } catch (Exception e) {
            // Suppose we can't play current music, because it's renamed/removed.
            // Play standard theme.
            player.reset();
            try {
                player.setDataSource(context,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.setLooping(true);
                player.prepare();
            } catch (Exception e2) {
                Log.e(this.getClass().getName(), "Now I don't know what to do.", e2);
            }
        }
    }

    public void mute() {
        boolean shouldPause = curPlayDelay > 0 || MuteUtils.getMuteTimeStep() > 0;
        if (player.isPlaying() && shouldPause) {
            Log.d(TAG, "Pausing player for " + curPlayDelay + "ms");
            player.pause();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    player.start();
                }
            }, curPlayDelay);
            curPlayDelay += MuteUtils.getMuteTimeStep() * 1000;
        }

    }

    public void start() {
        if (!started && player != null) {
            player.start();
            started = true;
        }
    }

    public void destroy() {
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
}
