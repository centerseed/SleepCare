package com.barry.sleepcare.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.barry.sleepcare.event.SoundEvent;

import java.util.ArrayList;

/**
 * A view that displays audio data on the screen as a waveform.
 */
public class WaveformView extends SurfaceView {

    // The number of buffer frames to keep around (for a nice fade-out visualization).
    private static final int HISTORY_SIZE = 6;

    // To make quieter sounds still show up well on the display, we use +/- 8192 as the amplitude
    // that reaches the top/bottom of the view instead of +/- 32767. Any samples that have
    // magnitude higher than this limit will simply be clipped during drawing.
    private static final float MAX_AMPLITUDE_TO_DRAW = 32767;

    // The queue that will hold historical audio data.
    private short[] mAudioData;

    ArrayList<SoundEvent> mSoundEvents;

    private final Paint mPaint;

    int mEventDrawIndex = 0;

    public WaveformView(Context context) {
        this(context, null, 0);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(0);
        mPaint.setAntiAlias(true);
    }

    /**
     * Updates the waveform view with a new "frame" of samples and renders it. The new frame gets
     * added to the front of the rendering queue, pushing the previous frames back, causing them to
     * be faded out visually.
     *
     * @param buffer the most recent buffer of audio samples
     */
    public synchronized void setAudioData(short[] buffer) {
        mAudioData = buffer;
    }

    public synchronized void drawAudioData(short[] buffer) {
        setAudioData(buffer);
        // Update the display.
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            drawWaveform(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public synchronized void setSoundEventsAndDraw(ArrayList<SoundEvent> events) {
        this.mSoundEvents = events;

        // Update the display.
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            drawWaveform(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Repaints the view's surface.
     *
     * @param canvas the {@link Canvas} object on which to draw
     */
    private void drawWaveform(Canvas canvas) {
        // Clear the screen each time because SurfaceView won't do this for us.
        canvas.drawColor(Color.BLACK);
        mEventDrawIndex = 0;

        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;

        // We draw the history from oldest to newest so that the older audio data is further back
        // and darker than the most recent data.
        // int colorDelta = 255 / (HISTORY_SIZE + 1);
        int colorDelta = 255;
        int brightness = colorDelta;

        float lastX = -1;
        float lastY = -1;
        float scaleX = width / mAudioData.length;

        for (int x = 0; x < mAudioData.length; x += 10) {

            short sample = mAudioData[x];
            mPaint.setColor(getPaintColor(x));
            float y = (sample / MAX_AMPLITUDE_TO_DRAW) * centerY + centerY;
            if (lastX != -1) {
                canvas.drawLine(lastX, lastY, x * scaleX, y, mPaint);
            }

            lastX = x * scaleX;
            lastY = y;
        }

       /* for (short[] buffer : mAudioData) {
            mPaint.setColor(Color.argb(brightness, 128, 255, 192));

            float lastX = -1;
            float lastY = -1;

            // For efficiency, we don't draw all of the samples in the buffer, but only the ones
            // that align with pixel boundaries.
            for (int x = 0; x < width; x++) {
                int index = (int) ((x / width) * buffer.length);
                short sample = buffer[index];
                float y = (sample / MAX_AMPLITUDE_TO_DRAW) * centerY + centerY;

                if (lastX != -1) {
                    canvas.drawLine(lastX, lastY, x, y, mPaint);
                }

                lastX = x;
                lastY = y;
            }

            brightness += colorDelta;
        }
        */
    }

    private int getPaintColor(int x) {
        if (mSoundEvents == null || mSoundEvents.size() == 0) return Color.WHITE;

        for (SoundEvent event : mSoundEvents) {
            if (x / 16 > event.getStartTime() && x / 16 < event.getEndTime()) {
                return Color.RED;
            }
        }
        return Color.WHITE;
    }
}