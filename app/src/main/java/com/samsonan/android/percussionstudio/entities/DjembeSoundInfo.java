package com.samsonan.android.percussionstudio.entities;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.samsonan.android.percussionstudio.R;
import com.samsonan.android.percussionstudio.views.TrackView;

/**
 * Enum of all djembe sounds
 */
public class DjembeSoundInfo extends SoundInfo {

    public DjembeSoundInfo(InstrumentBaseSound sound, int soundModMask){
        super (sound, soundModMask);
    }

    public static enum DjembeFlamSoundMod {
        EMPTY(R.string.list_no_flam,0x0000, null, null),
        FLAM_BASS(R.string.list_flam_bass,0x0001, "G", "D"),
        FLAM_TONE(R.string.list_flam_tone,0x0002, "g", "d"),
        FLAM_SLAP(R.string.list_flam_slap,0x0004, "P", "T");

        private int mMask;
        private int mListDisplayTextRes;
        private String mRightHandSymbol;
        private String mLeftHandSymbol;

        DjembeFlamSoundMod(int listDisplayTextRes, int mask,
                           String rightHandSymbol, String leftHandSymbol){
            mListDisplayTextRes = listDisplayTextRes;
            mMask = mask;
            mRightHandSymbol = rightHandSymbol;
            mLeftHandSymbol = leftHandSymbol;
        }

        public int getMask() {
            return mMask;
        }

        public int getDisplayResource() {
            return mListDisplayTextRes;
        }

        public static String [] stringValues(Resources res){
            String values[] = new String[values().length];
            for (int i = 0; i < values.length; i++) {
                values[i] = res.getString(values()[i].getDisplayResource());
            }
            return values;
        }

        public static int getFullMask(){
            return FLAM_BASS.mMask | FLAM_SLAP.mMask | FLAM_TONE.mMask;
        }

        public String getRightHandSymbol() {
            return mRightHandSymbol;
        }

        public String getLeftHandSymbol() {
            return mLeftHandSymbol;
        }
    }

    public static enum DjembeBaseSound implements InstrumentBaseSound {
        BASS_R(R.raw.djbass, "G", R.id.sound_bass_right, Color.parseColor("#0033ff")),//blue - 0033ff
        BASS_L(R.raw.djbass, "D", R.id.sound_bass_left, Color.parseColor("#ffff00")),
        TONE_R(R.raw.djtone, "g", R.id.sound_tone_right, Color.parseColor("#009900")),//green = 009900
        TONE_L(R.raw.djtone, "d", R.id.sound_tone_left, Color.parseColor("#00ffff")),
        SLAP_R(R.raw.djslap, "P", R.id.sound_slap_right, Color.parseColor("#ed2024")),//red = ed2024
        SLAP_L(R.raw.djslap, "T", R.id.sound_slap_left, Color.parseColor("#ff00ff"));

        private int mSoundResourceId;
        private int mButtonId;
        private int mColor;
        private String mDisplayText;

        public int getSoundResourceId() {
            return mSoundResourceId;
        }

        public int getIndex() {
            return ordinal();
        }

        DjembeBaseSound(int soundResourceId, String displayText, int buttonId, int color) {
            this.mSoundResourceId = soundResourceId;
            this.mDisplayText = displayText;
            this.mButtonId = buttonId;
            mColor = color;
        }

        public boolean isRightHand() {
            return this.name().endsWith("_R");
        }

        @Override
        public String getDisplayText() {
            return mDisplayText;
        }

        @Override
        public int getButtonId() {
            return mButtonId;
        }

        @Override
        public int getHotpotColor() {
            return mColor;
        }

        @Override
        public InstrumentFactory.Instrument getInstrument() {
            return InstrumentFactory.Instrument.DJEMBE;
        }

        public static InstrumentBaseSound getSoundForButtonId(int buttonId) {
            if (buttonId == R.id.sound_blank) return null;

            for (InstrumentBaseSound s : values()) {
                if (s.getButtonId() == buttonId) {
                    return s;
                }
            }
            throw new UnsupportedOperationException("button id " + buttonId + " for Djembe is unknown or not supported");
        }

        public static InstrumentBaseSound getSoundForColor(int color) {
            for (InstrumentBaseSound s : values()) {
                if (s.getHotpotColor() == color) {
                    return s;
                }
            }
            return null;
        }
    }

    public DjembeSoundInfo.DjembeFlamSoundMod getFlamSoundMod(){

        for (DjembeSoundInfo.DjembeFlamSoundMod mod: DjembeSoundInfo.DjembeFlamSoundMod.values()) {
            if ((mSoundModMask & mod.getMask())>0)
                return mod;
        }

        return DjembeSoundInfo.DjembeFlamSoundMod.EMPTY;
    }

    public boolean setFlamMod(DjembeSoundInfo.DjembeFlamSoundMod flamMod){

        if (flamMod == DjembeSoundInfo.DjembeFlamSoundMod.EMPTY)
            return unsetFlam();

        for (DjembeSoundInfo.DjembeFlamSoundMod mod: DjembeSoundInfo.DjembeFlamSoundMod.values()) {
            if (flamMod == mod) {
                if (((mod.getMask() & mSoundModMask) == mod.getMask()))
                    return false;
                unsetFlam();
                mSoundModMask = mod.getMask() | mSoundModMask;
                return true;
            }
        }

        return false;
    }

    public boolean unsetFlam(){

        if ((mSoundModMask & DjembeSoundInfo.DjembeFlamSoundMod.getFullMask()) ==0) return false;
        mSoundModMask = removeMaskBits(DjembeSoundInfo.DjembeFlamSoundMod.getFullMask(), mSoundModMask);
        return true;
    }

    public String getFlamText() {

        boolean isRightHand = ((DjembeBaseSound) mSound).isRightHand();

        //if main sound is played with right hand, then flam sounds will be played with left hand, and vice versa
        return isRightHand ? getFlamSoundMod().getLeftHandSymbol() : getFlamSoundMod().getRightHandSymbol();
    }

    public void drawTrackSound(Canvas canvas, float leftX, float bottomY, Paint mainTextPaint, Paint smallTextPaint, float density) {

        if (getFlamSoundMod().ordinal() > 0){
            canvas.drawText(mSound == null ? "-" : mSound.getDisplayText(), leftX, bottomY - density * 4 * TrackView.SQUARE_TEXT_PADDING, mainTextPaint);
            if (mSound != null && getFlamText() != null)
                canvas.drawText(getFlamText(), leftX + density * TrackView.BIT_SQUARE_WIDTH / 2, bottomY - density * 2 * TrackView.SQUARE_TEXT_PADDING, smallTextPaint);
        } else
            super.drawTrackSound(canvas, leftX, bottomY, mainTextPaint, smallTextPaint, density);
    }


}