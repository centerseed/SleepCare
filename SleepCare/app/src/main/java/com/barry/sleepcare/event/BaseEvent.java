package com.barry.sleepcare.event;

import android.util.Log;
import android.util.TimeUtils;

import com.barry.sleepcare.utils.TimeStrUtils;

abstract public class BaseEvent {

    static final String TAG = "Event";

    public enum EventType {
        Sleep,
        Snore,
        SnorePeriod,
        DogBarking,
        CarPassing,
        Talking,
        DoorOpening,
        Alarm,
        UnKnow
    }

    protected EventType mType;
    protected int mEventType;
    protected long mStartTime;
    protected long mEndTime;
    protected int mDurations;

    public BaseEvent(EventType type, long startTime) {
        this.mType = type;
        this.mStartTime = startTime;
        Log.d(TAG, "Create Event at: " + TimeStrUtils.getDateTimeStr(mStartTime));
    }

    public void setEndTime(long endTime) {
        this.mEndTime = endTime;
        Log.d(TAG, "Event Stop at " + TimeStrUtils.getDateTimeStr(mStartTime));
    }
}
