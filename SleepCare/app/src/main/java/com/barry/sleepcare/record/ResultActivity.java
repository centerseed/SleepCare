package com.barry.sleepcare.record;

import android.media.MediaFormat;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.barry.sleepcare.Const;
import com.barry.sleepcare.NavActivity;
import com.barry.sleepcare.R;
import com.barry.sleepcare.event.SleepEvent;
import com.barry.sleepcare.sound.AudioCodec;
import com.barry.sleepcare.utils.TimeStrUtils;
import com.barry.sleepcare.view.WaveformView;

import java.io.File;

public class ResultActivity extends NavActivity {

    TextView mDate;
    TextView mStartTime;
    TextView mEndTime;
    WaveformView mWaveForm;

    SleepEvent mSleepEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (getIntent() == null) {
            Toast.makeText(this, "No sleep events", Toast.LENGTH_SHORT).show();
            finish();
        }

        mSleepEvent = (SleepEvent) getIntent().getSerializableExtra(Const.ARG_EVENT);

        mDate = (TextView) findViewById(R.id.date);
        mStartTime = (TextView) findViewById(R.id.start_time);
        mEndTime = (TextView) findViewById(R.id.end_time);

        mDate.setText(TimeStrUtils.getDateStr(mSleepEvent.getStartTime()));
        mStartTime.setText(TimeStrUtils.getTimeStr(mSleepEvent.getStartTime()));
        mEndTime.setText(TimeStrUtils.getTimeStr(mSleepEvent.getEndTime()));

        mWaveForm = (WaveformView) findViewById(R.id.waveform);
    }

    @Override
    protected void onResume() {
        super.onResume();

        File f = new File(mSleepEvent.getFolderPath());
        File file[] = f.listFiles();

        for (final File ff : file) {
            String path = ff.getPath();
            decodeFile(path);
            return; // only show 1 file
        }


    }

    private void decodeFile(String fileName) {
        final AudioCodec audioCodec = AudioCodec.newInstance(MediaFormat.MIMETYPE_AUDIO_AAC);
        audioCodec.setIOPath(fileName, null);
        audioCodec.startAsync();

        audioCodec.setOnCompleteListener(new AudioCodec.OnCompleteListener() {
            @Override
            public void completed() {
                audioCodec.release();
                short[] pcm = audioCodec.getDecodedShortArray();
                mWaveForm.drawAudioData(pcm);
            }
        });
    }
}
