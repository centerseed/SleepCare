package com.barry.sleepcare.event;

public class SoundEvent extends BaseEvent {

    private int mLevel;

    public SoundEvent(EventType type, long startTime) {
        super(type, startTime);
    }

    public void setLevel(int level) {
        this.mLevel = level;
    }

    public int getLevel() {
        return mLevel;
    }
}
