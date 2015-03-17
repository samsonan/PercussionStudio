package com.samsonan.android.percussionstudio.entities;

import android.graphics.Color;

import com.samsonan.android.percussionstudio.R;

/**
 * Enum of all djembe sounds
 */
public class Djembe {

    public static enum DjembeSound implements InstrumentSound {
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

        DjembeSound(int soundResourceId, String displayText, int buttonId, int color) {
            this.mSoundResourceId = soundResourceId;
            this.mDisplayText = displayText;
            this.mButtonId = buttonId;
            mColor = color;
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
        public InstrumentFactory.Instruments getInstrument() {
            return InstrumentFactory.Instruments.DJEMBE;
        }

        public static InstrumentSound getSoundForButtonId(int buttonId) {
            if (buttonId == R.id.sound_blank) return null;

            for (InstrumentSound s : values()) {
                if (s.getButtonId() == buttonId) {
                    return s;
                }
            }
            throw new UnsupportedOperationException("button id " + buttonId + " for Djembe is unknown or not supported");
        }

        public static InstrumentSound getSoundForColor(int color) {
            for (InstrumentSound s : values()) {
                if (s.getHotpotColor() == color) {
                    return s;
                }
            }
            return null;
        }

    }
}