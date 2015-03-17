package com.samsonan.android.percussionstudio.entities;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Track entity class
 * Created by Andrey Samsonov on 10.03.2015.
 */
public class TrackInfo {

    private static String TAG = "TrackInfo";

    private long mId = -1;
    private String mTitle = "";
    private SoundInfo[] mSounds;
    private InstrumentFactory.Instruments mInstrument; // 0 - djembe
    private boolean mIsConnectedPrev;
    private int mBarCnt = 2;          // number of bars (blocks, measures) in the rhythm


    public TrackInfo(long id, String title, InstrumentFactory.Instruments instrument, int barCnt, int connected) {
        this.mTitle = title;
        this.mInstrument = instrument;
        this.mId = id;
        this.mBarCnt = barCnt;
        mIsConnectedPrev = connected > 0;
    }

    public void appendSoundArray(SoundInfo[] sounds){
        mSounds = sounds;
    }

    public TrackInfo (InstrumentFactory.Instruments instrument, int soundsInBar){
        this.mInstrument = instrument;
        mSounds = new SoundInfo [soundsInBar * mBarCnt];
    }

    public void addBars(int barCnt, int soundsPerBar){
        SoundInfo [] newArray = new SoundInfo[mSounds.length + barCnt*soundsPerBar];
        System.arraycopy(mSounds,0,newArray,0,Math.min(mSounds.length,newArray.length));
        mSounds = newArray;

        mBarCnt+=barCnt;
    }

    /**
     * Add bars at selected position
     */
    public void addBars(int barPositionIdx, int barCnt, int soundsPerBar){
        SoundInfo [] newArray = new SoundInfo[mSounds.length + barCnt*soundsPerBar];
        int sndPositionIdx = barPositionIdx * soundsPerBar;
        for (int i = 0; i < newArray.length; i++) {
            if (i < sndPositionIdx)
                newArray[i] = mSounds[i];
            else if (i >= sndPositionIdx + (barCnt * soundsPerBar)) {
                newArray[i] = mSounds[i - barCnt * soundsPerBar];
            }
        }

        mSounds = newArray;
        mBarCnt+=barCnt;
    }

    public void cloneBar(int barNum, int soundsPerBar){

        Log.d(TAG, "Cloning Bar num:"+barNum+", soundsPerBar:"+soundsPerBar+". Source array:"+Arrays.toString(mSounds));

        //empty new array with additional space for new bar
        SoundInfo [] newArray = new SoundInfo[mSounds.length + soundsPerBar];

        //copy old array to a new one
        System.arraycopy(mSounds,0,newArray,0,mSounds.length);
        ArrayList<SoundInfo> temp = new ArrayList<>(Arrays.asList(newArray));

        //adding sounds to designated positions
        for (int i=barNum*soundsPerBar;i<(barNum+1)*soundsPerBar;i++)
            temp.add(barNum*soundsPerBar+i, mSounds[i]);

        //converting list back to array
        mSounds = temp.toArray(newArray);

        Log.d(TAG, "Cloning complete. Result:"+Arrays.toString(mSounds));

        mBarCnt++;
    }


    public void updateTrackMeasureType(MeasureTypes measure){
        //OK, lets try not to loose any information, so if the track becomes shorter, then we add additional bar
        int totalSounds = getBarCnt() * measure.getSoundNumberForBar();
        int delta = mSounds.length - totalSounds;

        if (delta > 0){ // new array is smaller. have to add additional bars
            int addBarCnt = (delta / measure.getSoundNumberForBar())+1;
            Log.d(TAG, "updateTrackMeasureType(). new measure is smaller, increasing bar count by"+addBarCnt);
            mBarCnt += addBarCnt;
            totalSounds = getBarCnt() * measure.getSoundNumberForBar();
        }
        if (delta != 0) {
            SoundInfo [] newArray = new SoundInfo[totalSounds];
            System.arraycopy(mSounds,0,newArray,0,mSounds.length);
            mSounds = newArray;

        }
    }

    public void discardSoundInformation(){
        mSounds = new SoundInfo [mSounds.length];
    }

    public void removeBar(int barNum, int soundsPerBar){

        Log.d(TAG, "Deleting Bar num:"+barNum+", soundsPerBar:"+soundsPerBar+". Source array:"+Arrays.toString(mSounds));

        SoundInfo [] newArray = new SoundInfo[mSounds.length - soundsPerBar];
        int start = barNum*soundsPerBar;
        int end = (barNum+1)*soundsPerBar;
        int j =0;
        for (int i=0;i<mSounds.length;i++)
            if (i<start || i>=end)
                newArray[j++] = mSounds[i];

        mBarCnt--;
        mSounds = newArray;

        Log.d(TAG, "Deleting complete. Result:"+Arrays.toString(mSounds));
    }

    public SoundInfo[] getSounds() {
        return mSounds;
    }

    public InstrumentFactory.Instruments getInstrument(){
        return mInstrument;
    }

    public void setInstrument(InstrumentFactory.Instruments instrument) {
        this.mInstrument = instrument;
    }

    public int getBarCnt() {
        return mBarCnt;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public boolean isConnectedPrev(){
        return mIsConnectedPrev;
    }

    public void setConnectedPrev(boolean isConnected){
        mIsConnectedPrev = isConnected;
    }

    @Override
    public String toString() {
        return "TrackInfo{" +
                "mId=" + mId +
                ", mTitle='" + mTitle + '\'' +
                ", mSounds=[" + Arrays.toString(mSounds) +"]"+
                ", mBarCnt=" + mBarCnt +
                ", mInstrument=" + mInstrument +
                '}';
    }
}
