package com.samsonan.android.percussionstudio.entities;

/**
 * Sound bit entity class
 * Created by Andrey Samsonov on 10.03.2015.
 */
public class SoundInfo {

    private InstrumentSound mSound;

    public InstrumentSound getSound() {
        return mSound;
    }

    public SoundInfo(InstrumentSound sound) {
        this.mSound = sound;
    }

    @Override
    public String toString() {
        return "SoundInfo{" +
                "mSound=" + mSound +
                '}';
    }
}
