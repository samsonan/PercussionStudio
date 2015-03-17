package com.samsonan.android.percussionstudio.entities;

import android.graphics.Color;

import com.samsonan.android.percussionstudio.R;

/**
 * Enum of all clave sounds
 */
public class Clave {

	public static enum ClaveSound implements InstrumentSound {
		CLAVE_HIT(R.raw.clave_hit, "X", R.id.sound_hit, Color.parseColor("#0033ff"));

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

		ClaveSound(int soundResourceId, String displayText, int buttonId, int color) {
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
            return InstrumentFactory.Instruments.CLAVE;
        }

        public static InstrumentSound getSoundForButtonId(int buttonId) {
            if (buttonId == R.id.sound_blank) return null;
            if (buttonId == CLAVE_HIT.mButtonId) return CLAVE_HIT;

            throw new UnsupportedOperationException("button id " + buttonId + " for Djembe is unknown or not supported");
        }

        public static InstrumentSound getSoundForColor(int color) {
            if (color == CLAVE_HIT.mColor) return CLAVE_HIT;
            return null;
        }


	}
}