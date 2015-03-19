package com.samsonan.android.percussionstudio.entities;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.samsonan.android.percussionstudio.views.TrackView;

/**
 * Sound bit entity class
 * Created by Andrey Samsonov on 10.03.2015.
 */
public abstract class SoundInfo {

    protected InstrumentBaseSound mSound;
    protected int mSoundModMask;

    public SoundInfo(InstrumentBaseSound sound, int soundModMask){
        this.mSound = sound;
        this.mSoundModMask = soundModMask;
    }

    public static int removeMaskBits(int mask, int target) {
        return target & ~mask;
    }

    public InstrumentBaseSound getSound() {
        return mSound;
    }

    public int getSoundModMask(){
        return mSoundModMask;
    }

    public static void drawEmptySound(Canvas canvas, float leftX, float bottomY, Paint mainTextPaint, float density){
        canvas.drawText("-",
                leftX + density * 3 * TrackView.SQUARE_TEXT_PADDING,
                bottomY - density * 4 * TrackView.SQUARE_TEXT_PADDING, mainTextPaint);
    }

    public void drawTrackSound(Canvas canvas, float leftX, float bottomY, Paint mainTextPaint, Paint smallTextPaint, float density){
        canvas.drawText(mSound == null ? "-" : mSound.getDisplayText(),
                leftX + density * 3 * TrackView.SQUARE_TEXT_PADDING,
                bottomY - density * 4 * TrackView.SQUARE_TEXT_PADDING, mainTextPaint);
    }


    @Override
    public String toString() {
        return "SoundInfo{" +
                "mSound=" + mSound +
                ", mSoundModMask=" + mSoundModMask +
                '}';
    }
}
