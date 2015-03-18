package com.samsonan.android.percussionstudio.entities;

/**
 * Sound bit entity class
 * Created by Andrey Samsonov on 10.03.2015.
 */
public class SoundInfo {

    private InstrumentSound mSound;
    private int mSoundModMask;

    public InstrumentSound getSound() {
        return mSound;
    }

    public SoundInfo(InstrumentSound sound, int soundModMask) {
        this.mSound = sound;
        this.mSoundModMask = soundModMask;
    }

    public int getSoundModMask(){
        return mSoundModMask;
    }

    @Override
    public String toString() {
        return "SoundInfo{" +
                "mSound=" + mSound +
                ", mSoundModMask=" + mSoundModMask +
                '}';
    }
}
