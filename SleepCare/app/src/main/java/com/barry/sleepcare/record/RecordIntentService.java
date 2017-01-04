package com.barry.sleepcare.record;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.barry.sleepcare.Const;
import com.barry.sleepcare.event.SleepEvent;
import com.barry.sleepcare.sound.AudioCodec;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class RecordIntentService extends IntentService {
    private static final String TAG = "RecordIntentService";

    public static final String ACTION_START_RECORD = "_start_record";
    public static final String ACTION_STOP_RECORD = "_stop_record";
    public static final String ACTION_GET_STATUS = "_stop_record";

    public static final int STATUS_UNSTART = 0;
    public static final int STATUS_RECORDING = 1;
    public static final int STATUS_RECORD_FINISH = 2;
    public static final int STATUS_PERMISSION_ERROR = 11;
    public static final int STATUS_ERROR = 12;

    public static final int RECORD_SEC = 20;
    public static final int RECORD_INTERVAL = RECORD_SEC * 1 * 1000; // 60 seconds
    public static final int BUFFER_LEN = 16000 * RECORD_SEC;

    static Timer mTimer;
    static MediaRecorder mRecorder;
    static SleepEvent mSleepEvent = null;
    static String currFileName;
    static int mStatus = STATUS_UNSTART;
    static int mSegment = 1;

    public RecordIntentService() {
        super(RecordIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_START_RECORD.equals(intent.getAction())) {
            if (mTimer == null) mTimer = new Timer();
            mStatus = STATUS_RECORDING;
            startRecord();
        }

        if (ACTION_STOP_RECORD.equals(intent.getAction())) {
            stopRecord();
        }

        if (ACTION_GET_STATUS.equals(intent.getAction())) {
            sendBroadcast(mStatus, "");
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
        sendBroadcast(STATUS_RECORDING, "Segment: " + mSegment);
        stopRecording();
        if (mSleepEvent.getFolderPath() == null) {
            Log.e(TAG, "no folder path, skip recording");
        }
        currFileName = System.currentTimeMillis() + ".aac";
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

            sendBroadcast(STATUS_RECORD_FINISH, mSleepEvent);
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

    public static void getCurrentStatus(Context context) {
        Intent intent = new Intent(context, RecordIntentService.class);
        intent.setAction(RecordIntentService.ACTION_GET_STATUS);
        context.startService(intent);
    }

    private MediaRecorder initMediaRecorder(String filePath, String fileName) {
        MediaRecorder recorder = new MediaRecorder();

        try {
            recorder.setAudioEncodingBitRate(AudioFormat.ENCODING_PCM_16BIT);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            recorder.setAudioSamplingRate(16000);
            recorder.setAudioChannels(1);
            recorder.setOutputFile(filePath + File.separator + fileName);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        } catch (Exception e) {
            sendBroadcast(STATUS_PERMISSION_ERROR, e.getMessage());
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
            sendBroadcast(STATUS_ERROR, "Create folder fail!!");
            return null;
        }
    }

    private void listAllFile() {
        if (mSleepEvent != null && mSleepEvent.getFolderPath() != null) {

            File f = new File(mSleepEvent.getFolderPath());
            File file[] = f.listFiles();
            Log.d(TAG, "Record data:");

            for (final File ff : file) {
                String path = ff.getPath();
                Log.d(TAG, path);
            }
        }
    }

    private void stopRecording() {
        mSegment++;
        if (mRecorder != null) {
            Log.d(TAG, "Stop Record, release Media Recorder");
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

            // TODO: analysis sound data
            final AudioCodec audioCodec = AudioCodec.newInstance();
            audioCodec.setEncodeType(MediaFormat.MIMETYPE_AUDIO_AAC);
            // audioCodec.setIOPath(mSleepEvent.getFolderPath() + "/" + currFileName, mSleepEvent.getFolderPath() + "/1-" + currFileName);
            audioCodec.setIOPath(mSleepEvent.getFolderPath() + "/" + currFileName, null);
            audioCodec.prepare();
            audioCodec.startAsync();

            audioCodec.setOnCompleteListener(new AudioCodec.OnCompleteListener() {
                @Override
                public void completed() {
                    audioCodec.release();
                    short[] pcm = audioCodec.getDecodedShortArray();
                    if (pcm != null)
                        Log.d(TAG, "Get decoded pcm data, length is: " + pcm.length);
                }
            });

            mStatus = STATUS_RECORD_FINISH;
        }
    }

    private void sendBroadcast(int event, String msg) {
        Intent intent = new Intent();
        intent.putExtra(Const.ARG_STRING, msg);
        intent.setAction(String.valueOf(event));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcast(int event, SleepEvent sleepEvent) {
        Intent intent = new Intent();
        intent.putExtra(Const.ARG_EVENT, sleepEvent);
        intent.setAction(String.valueOf(event));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
