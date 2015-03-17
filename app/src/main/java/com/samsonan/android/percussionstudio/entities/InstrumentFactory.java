package com.samsonan.android.percussionstudio.entities;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;

import com.samsonan.android.percussionstudio.R;


/**
 * Factory class to work with instruments and sounds
 */
public class InstrumentFactory {

	public enum Instruments {DJEMBE, CLAVE}

    private SoundPool mSoundPool;
    private Context mContext;

	private HashMap<InstrumentSound, Integer> mSoundSamples = new HashMap<>();
	
    private static final int MAX_STREAMS = 8;

    public InstrumentFactory(Context context){
        mContext = context;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSoundPool = new SoundPool.Builder().setAudioAttributes(attr).setMaxStreams(MAX_STREAMS).build();
        } else {
            //it is deprecated in API 21 (LOLLIPOP)
            mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        loadInstrument(Djembe.DjembeSound.values());
        loadInstrument(Clave.ClaveSound.values());

    }

    public static int getHotspotImageId (Instruments instrument){

        switch (instrument){
            case DJEMBE:
                return R.id.djembe_hotspot_img;
            case CLAVE:
                return R.id.clave_hotspot_img;
        }
        throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
    }

	public static InstrumentSound getSoundForButtonId(int buttonId, Instruments instrument){
		if (buttonId == R.id.sound_blank) return null;

        switch (instrument){
            case DJEMBE:
                return Djembe.DjembeSound.getSoundForButtonId(buttonId);
            case CLAVE:
                return Clave.ClaveSound.getSoundForButtonId(buttonId);
        }
		throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
	}

    public static InstrumentSound getSoundForColor(int color, Instruments instrument){
        switch (instrument){
            case DJEMBE:
                return Djembe.DjembeSound.getSoundForColor(color);
            case CLAVE:
                return Clave.ClaveSound.getSoundForColor(color);
        }
        throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");

    }

	public void loadInstrument(InstrumentSound[] sounds) {
		for (InstrumentSound s : sounds) {
			mSoundSamples.put(s,  mSoundPool.load(mContext, s.getSoundResourceId(), 1));
		}
	}

    public void playSound(InstrumentSound sound) {
        if (sound != null)
            playSound(sound.getIndex(), sound.getInstrument());
    }

	private void playSound(int soundId, Instruments instrument) {

        switch (instrument){
            case DJEMBE:
                mSoundPool.play(mSoundSamples.get( Djembe.DjembeSound.values()[soundId] ), 1, 1, 1, 0, 1);
                return;
            case CLAVE:
                mSoundPool.play(mSoundSamples.get( Clave.ClaveSound.values()[soundId] ), 1, 1, 1, 0, 1);
                return;
        }
        throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
	}

	public static InstrumentSound getSoundById(int soundId,  Instruments instrument) {

        if (soundId == -1) return null;

        switch (instrument){
            case DJEMBE:
                return Djembe.DjembeSound.values()[soundId];
            case CLAVE:
                return Clave.ClaveSound.values()[soundId];
        }
		throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
	}

    public static Instruments getDefaultInstrument (){
        return Instruments.DJEMBE;
    }


}
