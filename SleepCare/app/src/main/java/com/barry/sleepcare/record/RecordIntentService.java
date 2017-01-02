package com.barry.sleepcare.record;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;

import com.barry.sleepcare.event.SleepEvent;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class RecordIntentService extends IntentService {
    private static final String TAG = "RecordIntentService";

    public static final String ACTION_START_RECORD = "_start_record";
    public static final String ACTION_STOP_RECORD = "_stop_record";

    public static final int RECORD_INTERVAL = 60 * 1 * 1000; // 60 seconds

    Timer mTimer;
    boolean mIsRecording = false;
    MediaRecorder mRecorder;
    static SleepEvent mSleepEvent = null;

    public RecordIntentService() {
        super(RecordIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_START_RECORD.equals(intent.getAction())) {
            if (mTimer == null) mTimer = new Timer();
            startRecord();
        }

        if (ACTION_STOP_RECORD.equals(intent.getAction())) {
            stopRecord();
        }
    }

    private void startRecord() {
        if (mRecorder != null) {
            // TODO: save data and analysis sound data
        }
        mSleepEvent = new SleepEvent(System.currentTimeMillis());

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

            }

        }, 0, RECORD_INTERVAL);
    }

    private void record() {
        mRecorder = new MediaRecorder();
    }

    private void stopRecord() {
        if (mSleepEvent != null) {
            mSleepEvent.setEndTime(System.currentTimeMillis());
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public static void startRecordService(Context context) {
        Intent intent = new Intent(context, RecordIntentService.class);
        intent.setAction(RecordIntentService.ACTION_START_RECORD);
        context.startService(intent);
    }

    public static void stopRecordService(Context context) {
        Intent intent = new Intent(context, RecordIntentService.class);
        intent.setAction(RecordIntentService.ACTION_STOP_RECORD);
        context.startService(intent);
    }

    private MediaRecorder initMediaRecoder() {
        MediaRecorder recorder = new MediaRecorder();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        // mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        return recorder;
    }

    private void createFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "TollCulator");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
        } else {
            // Do something else on failure
        }
    }
}
