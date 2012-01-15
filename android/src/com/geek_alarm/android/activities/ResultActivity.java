package com.geek_alarm.android.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.geek_alarm.android.R;
import com.geek_alarm.android.Utils;

/**
 * Final activity. It shows users "progress".
 * Draws green rectangles for correct solutions and red for incorrect.
 */
public class ResultActivity extends Activity {

    private final static double GAP_TO_SIZE = 1 / 10.;
    private final static int[] LEVELS = { 6, 24, 60, 144, 360, 720 };

    // Current 'size' of screen. Maximum number of rectangles on screen.
    // Possible values - LEVELS.
    private int num;
    // Number of rectangles in row.
    private int inRow;
    // Number of rectangles in column.
    private int inColumn;
    // Size of rectangle.
    private int size;
    // Gap between 2 rectangles.
    private int gap;
    // Left margin of first rect.
    private int marginLeft;
    // Top margin of first rect.
    private int marginTop;
    // NUmber of green rects.
    private int green;
    // NUmber of red rects.
    private int red;
    // True if green rect tries to fill red's position 
    // or otherwise
    private boolean isFighting;
    // If user solved current "round" correctly.
    private boolean win;
    private boolean runAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        win = getIntent().getBooleanExtra("win", true);
        updateStats();
        SurfaceView view = (SurfaceView) findViewById(R.id.image);
        view.getHolder().addCallback(new SurfaceCallback());
        findViewById(R.id.exit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
                finish();
            }
        });
    }

    private void setWinsLoses(int wins, int loses) {
        TextView view = (TextView) findViewById(R.id.wins);
        view.setText(String.valueOf(wins));
        view = (TextView) findViewById(R.id.loses);
        view.setText(String.valueOf(loses));
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    private void updateStats(){
        SharedPreferences pref = Utils.getPreferences();
        int wins = pref.getInt("wins", 0);
        int loses = pref.getInt("loses", 0);
        if (win) {
            wins++;
        } else {
            loses++;
        }
        setWinsLoses(wins, loses);
        red = pref.getInt("red", 0);
        green = pref.getInt("green", 0);
        int level = pref.getInt("level", 0);
        num = LEVELS[level];
        if (green == num && win) {
            level++;
            if (level == LEVELS.length) {
                green = 0;
                red = 0;
                level = 0;
            }
            num = LEVELS[level];
        }
        isFighting = red + green == num;
        if (isFighting) {
            red -= win ? 1 : 0;
            green -= win ? 0 : 1;
        }
        green = Math.max(green, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("wins", wins);
        editor.putInt("loses", loses);
        editor.putInt("red", red + (!win && !isFighting ? 1 : 0));
        editor.putInt("green", green + (win && !isFighting ? 1 : 0));
        editor.putInt("level", level);
        editor.commit();
    }

    /**
     * Calulcates all neccessary drawing parameters: 
     * size, margins, etc...
     * @param width of drawing area.
     * @param height of drawing area.
     */
    private void initConf(int width, int height) {
        size = 0;
        gap = 0;
        // Try all possible positions, finding position where size is max.
        for (int i = 1; i < num / 2; i++) {
            if (num % i != 0) {
                continue;
            }
            int curInRow = i;
            int curInColumn = num / i;
            int curSumSize = Math.min(width / curInRow, height / curInColumn);
            if (curSumSize > size + gap) {
                size = (int) Math.ceil(curSumSize / (1 + GAP_TO_SIZE));
                gap = curSumSize - size;
                inRow = curInRow;
                inColumn = curInColumn;
            }
        }
        marginLeft = (width - inRow * (gap + size) + gap) / 2;
        marginTop = (height - inColumn * (gap + size) + gap) / 2;
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Animator animator = new Animator(holder);
            runAnimation = true;
            new Thread(animator).start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            runAnimation = false;
        }
    }

    private class Animator implements Runnable {

        private SurfaceHolder holder;
        private Paint paint;

        public Animator(SurfaceHolder holder) {
            super();
            this.holder = holder;
            paint = new Paint();
        }

        public void run() {
            Canvas canvas = holder.lockCanvas();
            initConf(canvas.getWidth(), canvas.getHeight());
            int curNum = win ? green : num - red - 1;
            int steps = size;
            int winColor = win ? Color.GREEN : Color.RED;
            int opColor = win ? Color.RED : Color.GREEN;
            drawGreen(canvas);
            drawRed(canvas);
            if (isFighting) {
                drawRect(curNum, opColor, canvas, true, size);
            }
            holder.unlockCanvasAndPost(canvas);
            int x = marginLeft + curNum % inRow * (gap + size);
            int y = marginTop + curNum / inRow * (gap + size);
            Rect dirty = new Rect(x, y, x + size, y + size);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Do nothing.
            }
            while (steps >= 0 && runAnimation) {
                canvas = holder.lockCanvas(dirty);
                drawRect(curNum, Color.BLACK, canvas, true, size);
                if (isFighting) {
                    drawRect(curNum, winColor, canvas, win,
                            Math.min(size - steps, steps));
                    drawRect(curNum, opColor, canvas, !win, steps);
                } else {
                    drawRect(curNum, winColor, canvas, win, size - steps);
                }
                holder.unlockCanvasAndPost(canvas);
                steps--;
            }
        }

        /**
         * 
         * @param num - number of rect to be drawn. 
         * @param color
         * @param canvas
         * @param fromLeft - if rect should be drawn starting from left. 
         * it matters when rect is not square. Red rects must be drawn from right.
         * @param width
         */
        private void drawRect(int num, int color, Canvas canvas,
                boolean fromLeft, int width) {
            if (width == 0) {
                return;
            }
            int i = num / inRow;
            int j = num % inRow;
            int x = marginLeft + j * (gap + size);
            int y = marginTop + i * (gap + size);
            if (!fromLeft) {
                x += size - width;
            }
            Rect rect = new Rect(x, y, x + width, y + size);
            paint.setColor(color);
            canvas.drawRect(rect, paint);
        }

        private void drawGreen(Canvas canvas) {
            for (int i = 0; i < green; i++) {
                drawRect(i, Color.GREEN, canvas, true, size);
            }
        }

        private void drawRed(Canvas canvas) {
            for (int i = 0; i < red; i++) {
                drawRect(num - i - 1, Color.RED, canvas, true, size);
            }
        }
    }

}
