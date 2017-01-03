package com.barry.sleepcare.event;


import android.util.Log;

import java.util.ArrayList;

public class SleepEvent extends BaseEvent {
    static final String TAG = "SleepEvent";
    protected String mFolderPath;

    ArrayList<BaseEvent> mSnoreEvents = new ArrayList<>();
    ArrayList<BaseEvent> mBarkingEvents = new ArrayList<>();

    public SleepEvent(long startTime, String folderPath) {
        super(EventType.Sleep, startTime);
        Log.d(TAG, "Event -> Sleep Event");

        mFolderPath = folderPath;
    }

    public String getFolderPath() {
        return mFolderPath;
    }
}
