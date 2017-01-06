package com.barry.sleepcare.sound;

import android.media.audiofx.AudioEffect;
import android.util.Log;

import com.barry.sleepcare.event.BaseEvent;
import com.barry.sleepcare.event.SoundEvent;

import java.util.ArrayList;

/***
 * Analyzer: detect pulse -> get detect descriptor -> MFCC -> DP classification -> out event
 */

public class SoundAnalyzer {
    public static final String TAG = "SoundAnalyzer";
    public static final int MIN_WINDOWS_SIZE = 16000 * 1;

    Thread mThread;
    OnAnalyzedListener mListener;

    public class AmplitudeDescriptor {
        public long start;
        public long end;
        public int level;
    }


    public interface OnAnalyzedListener {
        void onAnalyzedFinish(ArrayList<SoundEvent> soundEvents);
    }

    public static SoundAnalyzer getInstance() {
        return new SoundAnalyzer();
    }

    public void setOnAnalyzedListener(OnAnalyzedListener listener) {
        this.mListener = listener;
    }

    private SoundAnalyzer() {
    }

    public void asyncAnalyze(short[] pcm) {
        if (pcm == null || pcm.length == 0) return;
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
            mThread = null;
        }

        mThread = new Thread(new analyzeRunnable(pcm));
        mThread.start();
    }

    class analyzeRunnable implements Runnable {

        short[] data;
        ArrayList<AmplitudeDescriptor> descriptors;

        class Statistic {
            public double mean;
            public short max;
        }

        public analyzeRunnable(short[] pcm) {
            data = pcm;
        }

        @Override
        public void run() {
            Log.d(TAG, "star analyzing data, data len is: " + data.length);
            descriptors = getAmplitudeDescriptor(data);

            // TODO: DP to classify descriptor
            ArrayList<SoundEvent> events = getEventFromDescripotrs(descriptors);

            if (mListener != null) {
                mListener.onAnalyzedFinish(events);
            }
        }

        private ArrayList<AmplitudeDescriptor> getAmplitudeDescriptor(short[] pcm) {
            ArrayList<AmplitudeDescriptor> descriptors = new ArrayList<>();

            Statistic statistic = getStatistic(pcm);
            Log.d(TAG, "Get max, mean value for data: " + statistic.max + " : " + statistic.mean);

            short threshold = (short) ((statistic.max - statistic.mean) / 3);
            AmplitudeDescriptor descriptor = null;

            for (int i = 20; i < pcm.length; i += 10) { // skip some point for efficient
                short prev = (short) Math.abs(pcm[i]);
                short curr = (short) Math.abs(pcm[i - 10]);
                if ((curr - prev) > threshold) {
                    descriptor = new AmplitudeDescriptor();
                    descriptor.start = i/16;   // 16000/1000 -> 1ms contain 16 points
                    i += MIN_WINDOWS_SIZE;
                    descriptor.end =  i/16;
                    descriptors.add(descriptor);
                }
            }

            return descriptors;
        }

        public Statistic getStatistic(short[] arr) {
            Statistic statistic = new Statistic();
            long sum = 0;
            int skip = 0;

            for (short cur : arr) {
                statistic.max = (short) Math.max(statistic.max, cur);
                if (cur < 0) {
                    skip++;
                    continue;
                }
                sum += cur;
            }
            statistic.mean = sum / (double) (arr.length - skip);
            return statistic;
        }

        private ArrayList<SoundEvent> getEventFromDescripotrs(ArrayList<AmplitudeDescriptor> descriptors) {
            ArrayList<SoundEvent> events = new ArrayList<>();
            events.clear();
            for (AmplitudeDescriptor descriptor: descriptors) {
                SoundEvent event = new SoundEvent(BaseEvent.EventType.Snore, descriptor.start);
                event.setEndTime(descriptor.end);
                events.add(event);
            }
            return events;
        }
    }
}
