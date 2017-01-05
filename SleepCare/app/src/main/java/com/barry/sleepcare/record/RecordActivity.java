package com.barry.sleepcare.record;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;
import android.widget.Toast;

import com.barry.sleepcare.Const;
import com.barry.sleepcare.NavActivity;
import com.barry.sleepcare.R;
import com.barry.sleepcare.event.SleepEvent;
import com.barry.sleepcare.utils.TimeStrUtils;
import com.barry.sleepcare.view.SlideToUnlock;

import java.util.Timer;
import java.util.TimerTask;


public class RecordActivity extends NavActivity {

    SlideToUnlock mSwipeBtn;
    TextView mStartTime;
    TextView mDuration;
    TextView mStatus;

    Timer mTimer;
    Long mTimeStamp;

    RecordReceiver mReceiver;
    ProgressDialog mProgressDialog;
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
                mProgressDialog = ProgressDialog.show(RecordActivity.this,
                        "處理中", "正在結束錄音...",true);
            }
        });

        mStartTime = (TextView) findViewById(R.id.start_time);
        mDuration = (TextView) findViewById(R.id.duration);
        mStatus = (TextView) findViewById(R.id.status);

        mTimeStamp = 0L;
        mStartTime.setText(TimeStrUtils.getDateTimeStr(System.currentTimeMillis()));

        mTimer = new Timer();
        setTimerTask();

        // RecordIntentService
        RecordIntentService.startRecordService(getApplicationContext());
        mReceiver = new RecordReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(RecordIntentService.STATUS_ERROR + "");
        mFilter.addAction(RecordIntentService.STATUS_PERMISSION_ERROR + "");
        mFilter.addAction(RecordIntentService.STATUS_RECORD_FINISH + "");
        mFilter.addAction(RecordIntentService.STATUS_RECORDING + "");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
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

        }, 1000, 1000);
    }

    private String getDuration(int duration) {
        String time = duration / 3600 + "h :" + duration / 60 + "m :" + duration % 60 + "s";
        return time;
    }

    class RecordReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            int status = Integer.valueOf(intent.getAction());
            if (RecordIntentService.STATUS_ERROR == status) {
                Toast.makeText(RecordActivity.this, intent.getStringExtra("string"), Toast.LENGTH_LONG).show();
            }

            if (RecordIntentService.STATUS_PERMISSION_ERROR == status) {
                Toast.makeText(RecordActivity.this, "No Record Permission", Toast.LENGTH_LONG).show();
                finish();
            }

            if (RecordIntentService.STATUS_RECORDING == status) {
                String msg = intent.getStringExtra(Const.ARG_STRING);
                mStatus.setText("Recording, " + msg);
            }

            if (RecordIntentService.STATUS_RECORD_FINISH == status) {
                SleepEvent event = (SleepEvent) intent.getSerializableExtra(Const.ARG_EVENT);
                if (event == null) return;

                Intent i = new Intent(RecordActivity.this, ResultActivity.class);
                i.putExtra(Const.ARG_EVENT, event);
                startActivity(i);
                RecordActivity.this.finish();

                mProgressDialog.dismiss();
                finish();
            }
        }
    }
}
