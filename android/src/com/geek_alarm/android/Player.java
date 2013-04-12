package com.geek_alarm.android;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class Player implements AudioManager.OnAudioFocusChangeListener {

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
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                player.start();
                started = true;
            }
        }
    }

    public void destroy() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(this);
            player = null;
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (player != null) {
                    player.start();
                    player.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
                player = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player.isPlaying()) {
                    player.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                }
                break;
        }

    }
}
