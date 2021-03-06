package com.samsonan.android.percussionstudio.entities;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Track entity class
 * Created by Andrey Samsonov on 10.03.2015.
 */
public class TrackInfo {

    private static String TAG = "TrackInfo";

    private long mId = -1;
    private String mTitle = "";
    private SoundInfo[] mSounds;
    private InstrumentFactory.Instrument mInstrument; // 0 - djembe
    private int mPlayTimes = 1;
    private boolean mIsConnectedPrev;
    private int mBarCnt = 2;          // number of bars (blocks, measures) in the rhythm


    public TrackInfo(long id, String title, InstrumentFactory.Instrument instrument, int barCnt, int connected, int playTimes) {
        this.mTitle = title;
        this.mInstrument = instrument;
        this.mId = id;
        this.mBarCnt = barCnt;
        this.mIsConnectedPrev = connected > 0;
        this.mPlayTimes = playTimes;
    }

    public void appendSoundArray(SoundInfo[] sounds){
        mSounds = sounds;
    }

    public TrackInfo (InstrumentFactory.Instrument instrument, int soundsInBar){
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

    public SoundInfo getSoundAtIdx(int idx) {
        return mSounds[idx];
    }

    public void setSoundAtIdx(int idx, SoundInfo soundInfo) {
        mSounds[idx] = soundInfo;
    }


    public int getSoundCnt() {
        return mSounds.length;
    }


    public InstrumentFactory.Instrument getInstrument(){
        return mInstrument;
    }

    public void setInstrument(InstrumentFactory.Instrument instrument) {
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

    public int getPlayTimes() {
        return mPlayTimes;
    }

    //package access!
    void setPlayTimes(int playTimes) {
        this.mPlayTimes = playTimes;
    }

    @Override
    public String toString() {
        return "TrackInfo{" +
                "mId=" + mId +
                ", mTitle='" + mTitle + '\'' +
                ", mSounds=[" + Arrays.toString(mSounds) +"]"+
                ", mBarCnt=" + mBarCnt +
                ", mPlayTimes=" + mPlayTimes +
                ", mInstrument=" + mInstrument +
                '}';
    }
}
