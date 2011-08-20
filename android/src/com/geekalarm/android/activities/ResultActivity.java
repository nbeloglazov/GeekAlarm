package com.geekalarm.android.activities;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.geekalarm.android.R;

public class ResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        SurfaceView view = (SurfaceView) findViewById(R.id.image);
        view.getHolder().addCallback(new SurfaceCallback());
    }

    private boolean runAnimation;

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
            // TODO Auto-generated method stub

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            runAnimation = false;
        }
    }

    private class Animator implements Runnable {

        private SurfaceHolder holder;

        public Animator(SurfaceHolder holder) {
            super();
            this.holder = holder;
        }

        public void run() {
            float x = 0F;
            while (runAnimation) {
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                Canvas canvas = holder.lockCanvas();
                drawGrid(canvas);
                canvas.drawOval(new RectF(x, x, x + 100, x + 100), paint);
                Log.e("Draw", "DRAW!!!!");
                holder.unlockCanvasAndPost(canvas);
                x++;
            }
        }
        
        private void drawGrid(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            int size = 20;
            int gap = size / 10;
            int inRow = canvas.getWidth() / (size + gap);
            int inColumn = canvas.getHeight() / (size + gap);
            for (int i = 0; i < inColumn; i++) {
                for (int j = 0; j < inRow; j++) {
                    int x = j * (gap + size);
                    int y = i * (gap + size);
                    Rect rect = new Rect(x, y, x + size, y + size);
                    canvas.drawRect(rect, paint);
                }
            }
        }
    }
}
