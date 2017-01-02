package com.barry.sleepcare.record;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.barry.sleepcare.NavActivity;
import com.barry.sleepcare.R;
import com.barry.sleepcare.utils.TimeStrUtils;
import com.barry.sleepcare.view.SlideToUnlock;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;


public class RecordActivity extends NavActivity {

    SlideToUnlock mSwipeBtn;
    TextView mStartTime;
    TextView mDuration;

    Timer mTimer;
    Long mTimeStamp;

    int mDurationSec = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        getSupportActionBar().setTitle("Record");

        mSwipeBtn = (SlideToUnlock) findViewById(R.id.swipe_button);
        mSwipeBtn.setOnUnlockListener(new SlideToUnlock.OnUnlockListener() {
            @Override
            public void onUnlock() {
                mTimer.cancel();
                RecordIntentService.stopRecordService(getApplicationContext());
                final ProgressDialog dialog = new ProgressDialog(RecordActivity.this);
                mStartTime.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Intent intent = new Intent(RecordActivity.this, ResultActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 3000);
                dialog.show();
            }
        });

        mStartTime = (TextView) findViewById(R.id.start_time);
        mDuration = (TextView) findViewById(R.id.duration);

        mTimeStamp = 0L;
        mStartTime.setText(TimeStrUtils.getDateTimeStr(System.currentTimeMillis()));

        mTimer = new Timer();
        setTimerTask();

        // RecordIntentService
        RecordIntentService.startRecordService(getApplicationContext());
    }

    private void setTimerTask() {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mDurationSec++;
                final String duration = getDuration(mDurationSec);
                mDuration.post(new Runnable() {
                    @Override
                    public void run() {
                        mDuration.setText(duration);
                    }
                });
            }

        }, 1000, 1000/* 表示1000毫秒之後，每隔1000毫秒執行一次 */);
    }

    private String getDuration(int duration) {
        String time = duration / 3600 + "h :" + duration / 60 + "m :" + duration % 60 + "s";
        return time;
    }
}
