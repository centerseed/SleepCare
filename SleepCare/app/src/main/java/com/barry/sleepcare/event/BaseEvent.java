package com.barry.sleepcare.event;

import android.util.Log;

import com.barry.sleepcare.utils.TimeStrUtils;

import java.io.Serializable;

abstract public class BaseEvent implements Serializable {

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
        this.mDurations = (int) (mEndTime - mStartTime);
        Log.d(TAG, "Event Stop at " + TimeStrUtils.getDateTimeStr(mStartTime));
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }
}
