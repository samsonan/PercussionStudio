package com.samsonan.android.percussionstudio.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rhythms entity class
 * Created by Andrey Samsonov on 10.03.2015.
 */
public class RhythmInfo {

    private String mTitle;
    private String mCategory;
    private String mDescription;
    private long mId = -1;
    private int mInternal = 0;  //internal rhythms cannot be edited or deleted
    private int mBpm = -1;      //rhythm default playing speed

    //by default it is 4/4 measure
    private MeasureTypes mMeasure = MeasureTypes.MEASURE_4_4;

    private List<TrackInfo> mTracks = new ArrayList<>();

    /**
     * Initialize new rhythm. Use default values
     */
    public RhythmInfo(InstrumentFactory.Instruments currentInstrument) {

        TrackInfo trackInfo = new TrackInfo(currentInstrument, getSoundNumberForBar());
        mTracks.add(trackInfo);
    }

    public RhythmInfo(long id, String title, short bitsPerBar, short bitLength, String description, String category, int internal) {
        this.mTitle = title;
        this.mId = id;
        this.mDescription = description;
        this.mCategory = category;
        this.mInternal = internal;

        for (MeasureTypes measure: MeasureTypes.values()){
            if (measure.getBitLength() == bitLength && measure.getBitsPerBar() == bitsPerBar) {
                mMeasure = measure;
                break;
            }
        }
    }

    public int getMaxBarCnt(){
        int max = 0;
        for (int i = 0; i < mTracks.size(); i++) {
            if (mTracks.get(i).getBarCnt()>max)
                max = mTracks.get(i).getBarCnt();
        }
        return max;
    }

    /**
     * How many sounds can we put in one bar (measure) ?
     */
    public int getSoundNumberForBar() {
        return mMeasure.getSoundNumberForBar();
    }

    public void removeTrack(int trackIdx){

        removeConnectedFlagCascade(trackIdx);

        if (mTracks.size() > trackIdx)
            mTracks.remove(trackIdx);
    }

    public void swapTracks(int trackIdx1, int trackIdx2){
        if (trackIdx1>-1 && trackIdx2>-1 && mTracks.size()>trackIdx1 && mTracks.size()>trackIdx2) {
            Collections.swap(mTracks, trackIdx1, trackIdx2);
        }
        removeConnectedFlagCascade(trackIdx1);
        removeConnectedFlagCascade(trackIdx2);
    }

    /**
     * Connect track with index trackIdx to previous track
     */
    public void setConnectedFlag(int trackIdx){

        if (trackIdx==0)
            return; //cannot connect the first track

        //max track length among all connected tracks
        int maxBarCnt = mTracks.get(trackIdx).getBarCnt();
        for (int i=trackIdx-1;i>=0;i--){
            if (mTracks.get(i).getBarCnt() > maxBarCnt)
                maxBarCnt = mTracks.get(i).getBarCnt();
            if (!mTracks.get(i).isConnectedPrev())
                break;
        }

        //now we need to extend all connected tracks to that length
        //going Up
        for (int i=trackIdx;i>=0;i--){
            if (mTracks.get(i).getBarCnt() < maxBarCnt)
                mTracks.get(i).addBars(maxBarCnt - mTracks.get(i).getBarCnt(), getSoundNumberForBar());
            if (i!=trackIdx && !mTracks.get(i).isConnectedPrev())
                break;
        }

        //going Down
        for (int i=trackIdx+1;i<mTracks.size();i++){
            if (!mTracks.get(i).isConnectedPrev())
                break;
            if (mTracks.get(i).getBarCnt() < maxBarCnt)
                mTracks.get(i).addBars(maxBarCnt - mTracks.get(i).getBarCnt(), getSoundNumberForBar());
        }

        mTracks.get(trackIdx).setConnectedPrev(true);

        //now we have to sync play times between all tracks!
        setTrackPlayTimesCnt(trackIdx, mTracks.get(trackIdx).getPlayTimes());
    }

    /**
     * Play times should be equal for all connected tracks
     */
    public void setTrackPlayTimesCnt(int trackIdx, int playTimesCnt){

        //now we need to extend all connected tracks to that length
        //going Up
        for (int i=trackIdx;i>=0;i--){
            if (!mTracks.get(i).isConnectedPrev())
                break;
            mTracks.get(i-1).setPlayTimes(playTimesCnt);
        }

        //going Down
        for (int i=trackIdx+1;i<mTracks.size();i++){
            if (!mTracks.get(i).isConnectedPrev())
                break;
            mTracks.get(i).setPlayTimes(playTimesCnt);
        }

        mTracks.get(trackIdx).setPlayTimes(playTimesCnt);
    }



    /**
     * Remove connected flag from current track and all other connected (to it) tracks.
     * Used when we change order of tracks
     *
     */
    public void removeConnectedFlagCascade(int trackIdx){
        mTracks.get(trackIdx).setConnectedPrev(false);
        for (int i = trackIdx+1; i < mTracks.size(); i++) {
            if (mTracks.get(i).isConnectedPrev())
                mTracks.get(i).setConnectedPrev(false);
            else break;//reached last connected track in this pack, exit
        }
    }

    public int getBitLength() {
        return mMeasure.getBitLength();
    }

    public int getBitsPerBar() {
        return mMeasure.getBitsPerBar();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public TrackInfo getTrackIdx(int trackIdx){
        return mTracks.get(trackIdx);
    }

    public int getTrackCnt() {
        return mTracks.size();
    }

    public void addTrack(TrackInfo newTrack) {
        mTracks.add(newTrack);
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public int getBpm() {
        return mBpm;
    }

    public void setBpm(int bpm) {
        this.mBpm = bpm;
    }

    public void setMeasure(MeasureTypes measure) {
        mMeasure = measure;
        //if measure is changed, then we have to update the lengths of all tracks
        for (int i=0;i<mTracks.size();i++){
            mTracks.get(i).updateTrackMeasureType(measure);
        }
    }

    public String getCategory() {
        return mCategory;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public int getInternalFlag() {
        return mInternal;
    }

    public void setInternalFlag(int internal) {
        mInternal = internal;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("RhythmInfo{" + "mTitle='").append(mTitle).append('\'').append(", mId=").append(mId).
                append(", mMeasure=").append(mMeasure).
                append(", mInternal=").append(mInternal).
                append(", mTracks=[");
        for (TrackInfo track : mTracks) {
            builder.append(track.toString());
        }
        builder.append("']}'");
        return builder.toString();
    }

}
