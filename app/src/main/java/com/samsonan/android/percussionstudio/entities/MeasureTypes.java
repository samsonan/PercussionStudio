package com.samsonan.android.percussionstudio.entities;

/**
 * Created by Andrey Samsonov on 16.03.2015.
 */
public enum MeasureTypes {
    MEASURE_4_4 ("4/4", 4, 4, 2),
    MEASURE_3_4 ("3/4", 3, 4, 2),
    MEASURE_3_8 ("3/8", 3, 8, 1);

    private String mDisplayText;
    private int mSoundsPerBit = 2;    // how many sounds we can put in one bit. Every sound is 1/8 bit/note.
    private int mBitLength = 4;       // length/type of a note/bit, like quarter (1/4) or eights (1/8)
    private int mBitsPerBar = 4;      // number of bits in the bar (block, measure).

    MeasureTypes(String displayText, int bitsPerBar, int bitLength, int soundsPerBit){
        mDisplayText = displayText;
        mSoundsPerBit = soundsPerBit;
        mBitLength = bitLength;
        mBitsPerBar = bitsPerBar;
    }

    public int getSoundNumberForBar() {
        return getSoundsPerBit() * getBitsPerBar();
    }

    public int getSoundsPerBit() {
        return mSoundsPerBit;
    }

    public int getBitLength() {
        return mBitLength;
    }

    public int getBitsPerBar() {
        return mBitsPerBar;
    }

    @Override
    public String toString() {
        return mDisplayText;
    }
}
