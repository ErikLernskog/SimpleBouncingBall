package com.lernskog.erik.simplebouncingball;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BounceView extends View {
    // Ball position, measured in SCREENS. Starts in the middle.
    private float ball_x = 0.5f;
    private float ball_y = 0.5f;
    // Ball movement, measured in SCREENS PER SECOND
    private float ball_dx = 0.1f;
    private float ball_dy = 0.05f;
    // Measured in pixels
    private int ball_radius = 10; // TODO 10

    private int width_pixels;
    private int height_pixels;

    private Timer the_ticker;

    public BounceView(Context context) {
        super(context);
    }

    public BounceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width_pixels = w;
        height_pixels = h;
    }

    public static final double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10, (double) places);
    }

    private int frames = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint();
        int w = width_pixels > 0 ? width_pixels : canvas.getHeight();
        int h = height_pixels > 0 ? height_pixels : canvas.getWidth();

        // The ball is green when in the crosshairs
        if (Math.sqrt(Math.pow(ball_x * w - w / 2.0f, 2) + Math.pow(ball_y * h - h / 2.0f, 2)) < ball_radius)
            p.setColor(Color.GREEN);
        else
            p.setColor(Color.BLUE);
        canvas.drawCircle(ball_x * w, ball_y * h, ball_radius, p);

        p.setColor(Color.RED);
        p.setTextSize(60.0f);
        canvas.drawText("x = " + String.format("%.2f", ball_x) +
                        ", y = " + String.format("%.2f", ball_y) +
                        ", dx = " + String.format("%.2f", ball_dx) +
                        ", dy = " + String.format("%.2f", ball_dy),
                20.0f, 60.0f, p);
        canvas.drawText("w = " + w + " (" + width_pixels + ")" +
                        ", h = " + h + " (" + height_pixels + ")",
                20.0f, 120.0f, p);
        ++frames;
        canvas.drawText("frames = " + frames,
                20.0f, 180.0f, p);

        // A crosshair at the middle of the screen
        float crosswidth = Math.min(w, h) * 0.1f;
        canvas.drawLine(w * 0.5f - crosswidth, h * 0.5f, w * 0.5f + crosswidth, h * 0.5f, p);
        canvas.drawLine(w * 0.5f, h * 0.5f - crosswidth, w * 0.5f, h * 0.5f + crosswidth, p);

        canvas.drawText("basen.oru.se/android",
                20.0f, h - 5.0f, p);
    } // onDraw

    // Called when the app is visible(possibly: again). We should start moving.
    public void resume() {
        the_ticker = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                update_simulation();
                // We get CalledFromWrongThreadException if we just just call invalidate().
                // It must be done in the right thread!
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                };
                getHandler().post(r);
            }
        };
        // Give it a full second to set things up, before we start ticking
        // (It crashed with java.lang.NullPointerException when starting after 30 ms, but worked with 40.)
        the_ticker.schedule(task, 1000, 10);
    } // resume

    // Called when the app has been hidden. We should stop moving.
    public void pause() {
        nanos_when_paused = java.lang.System.nanoTime();
        the_ticker.cancel();
        the_ticker = null;
    }

    // -1 is not guaranteed to never happen, but we ignore that
    private long previous_nanos = -1;
    private long nanos_when_paused = -1;

    // Calculate the ball's new position and speed
    private void update_simulation() {
        long now_nanos = java.lang.System.nanoTime();
        if (previous_nanos == -1 || now_nanos < previous_nanos) {
            // First time, or overflow, so don't update the game
            previous_nanos = now_nanos;
            return;
        }

        long nanos = now_nanos - previous_nanos;
        if (nanos_when_paused != -1) {
            // We have been paused!
            nanos = nanos_when_paused - previous_nanos;
            nanos_when_paused = -1;
        }

        previous_nanos = now_nanos;
        double seconds = nanos / 1e9;

        ball_x += ball_dx * seconds;
        ball_y += ball_dy * seconds;

        // Yes, this ignores that the ball has a radius.
        float ball_x_screen = ball_radius * 1.0f / width_pixels;
        float ball_y_screen = ball_radius * 1.0f / height_pixels;

        //ball_x = ball_x + ball_x_screen;
        Log.d("Bounce", "ball_x_screen " + String.format("%.2f", ball_x_screen) + " ball_y_screen " + String.format("%.2f", ball_y_screen));
        if (ball_x - ball_x_screen < 0) {
            Log.d("Bounce", "Bounce on left wall ");
            ball_x = - ball_x + ball_x_screen * 2;
            ball_dx = -ball_dx;
        }
        if (ball_x + ball_x_screen > 1) {
            Log.d("Bounce", "Bounce on right wall");
            ball_x = 2 - ball_x - ball_x_screen * 2 ;
            ball_dx = -ball_dx;
        }
        if (ball_y - ball_y_screen < 0) {
            Log.d("Bounce", "Bounce on ceiling");
            ball_y = -ball_y + ball_y_screen * 2;
            ball_dy = -ball_dy;
        }
        if (ball_y + ball_y_screen > 1) {
            Log.d("Bounce", "Bounce on floor");
            ball_y = 2 - ball_y - ball_y_screen * 2;
            ball_dy = -ball_dy;
        }
    } // update_simulation
} // class BounceView