package com.barry.sleepcare.record;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.barry.sleepcare.event.SleepEvent;
import com.barry.sleepcare.sound.MediaDecoder;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RecordIntentService extends IntentService {
    private static final String TAG = "RecordIntentService";

    public static final String ACTION_START_RECORD = "_start_record";
    public static final String ACTION_STOP_RECORD = "_stop_record";

    public static final String EVENT_RECORDING = "_event_recording";
    public static final String EVENT_PERMISSION_ERROR = "_event_error";
    public static final String EVENT_ERROR = "_event_error";

    public static final int RECORD_SEC = 20;
    public static final int RECORD_INTERVAL = RECORD_SEC * 1 * 1000; // 60 seconds
    public static final int BUFFER_LEN = 16000 * RECORD_SEC;

    static Timer mTimer;
    static MediaRecorder mRecorder;
    static SleepEvent mSleepEvent = null;
    static String currFileName;

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
        long timeStamp = System.currentTimeMillis();
        mSleepEvent = new SleepEvent(timeStamp, createFolder(timeStamp));

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                record();
            }

        }, 0, RECORD_INTERVAL);
    }

    private void record() {
        stopRecording();
        if (mSleepEvent.getFolderPath() == null) {
            Log.e(TAG, "no folder path, skip recording");
        }
        currFileName = System.currentTimeMillis() + ".m4a";
        mRecorder = initMediaRecorder(mSleepEvent.getFolderPath(), currFileName);
        if (mRecorder == null) return;

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Media Recorder prepare() failed");
            return;
        }

        Log.e(TAG, "Start record");
        mRecorder.start();
    }

    private void stopRecord() {
        if (mSleepEvent != null) {
            mSleepEvent.setEndTime(System.currentTimeMillis());
            stopRecording();
            listAllFile();
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

    private MediaRecorder initMediaRecorder(String filePath, String fileName) {
        MediaRecorder recorder = new MediaRecorder();

        try {
            recorder.setAudioEncodingBitRate(AudioFormat.ENCODING_PCM_16BIT);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioSamplingRate(16000);
            recorder.setAudioChannels(1);
            recorder.setOutputFile(filePath + File.separator + fileName);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        } catch (Exception e) {
            sendBroadcast(EVENT_PERMISSION_ERROR, e.getMessage());
            return null;
        }
        Log.d(TAG, "Init recorder -> " + fileName);
        return recorder;
    }

    private String createFolder(long timestamp) {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "SleepTie" + File.separator + timestamp + File.separator + "log");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            Log.d(TAG, "Create folder: " + folder.getAbsolutePath());
            return folder.getAbsolutePath();
        } else {
            Log.e(TAG, "Create folder fail!! ");
            sendBroadcast(EVENT_ERROR, "Create folder fail!!");
            return null;
        }
    }

    private void listAllFile() {
        if (mSleepEvent != null && mSleepEvent.getFolderPath() != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File f = new File(mSleepEvent.getFolderPath());
                    File file[] = f.listFiles();
                    Log.d(TAG, "Record data:");

                    for (final File ff : file) {
                        String path = ff.getPath();
                        Log.d(TAG, path);

                        try {
                            Thread.sleep(1000);

                            MediaDecoder decoder = new MediaDecoder(ff.getAbsolutePath());
                            short[] data;
                            while ((data = decoder.readShortData()) != null) {
                                // process data here
                                Log.d(TAG, "Get buffer");
                                for (int i = 0; i < data.length; i++) {
                                    Log.d(TAG, data[i] + "");
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            Log.d(TAG, "Stop Record, release Media Recorder");
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

            // TODO: analysis sound data

        }
    }

    private void sendBroadcast(String event, String msg) {
        Intent intent = new Intent();
        intent.putExtra("string", msg);
        intent.setAction(event);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
