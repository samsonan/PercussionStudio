package com.samsonan.android.percussionstudio.entities;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import com.samsonan.android.percussionstudio.R;


/**
 * Factory class to work with instruments and sounds
 */
public class InstrumentFactory {

	public enum Instrument {DJEMBE, CLAVE}

    public final static String TAG = "InstrumentFactory";

    private SoundPool mSoundPool;
    private Context mContext;

	private HashMap<InstrumentBaseSound, Integer> mSoundSamples = new HashMap<>();
	
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

        loadInstrument(DjembeSoundInfo.DjembeBaseSound.values());
        loadInstrument(ClaveSoundInfo.ClaveBaseSound.values());

    }

    public static int getHotspotImageId (Instrument instrument){

        switch (instrument){
            case DJEMBE:
                return R.id.djembe_hotspot_img;
            case CLAVE:
                return R.id.clave_hotspot_img;
        }
        throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
    }

	public static InstrumentBaseSound getSoundForButtonId(int buttonId, Instrument instrument){
		if (buttonId == R.id.sound_blank) return null;

        switch (instrument){
            case DJEMBE:
                return DjembeSoundInfo.DjembeBaseSound.getSoundForButtonId(buttonId);
            case CLAVE:
                return ClaveSoundInfo.ClaveBaseSound.getSoundForButtonId(buttonId);
        }
		throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
	}

    public static InstrumentBaseSound getSoundForColor(int color, Instrument instrument){
        switch (instrument){
            case DJEMBE:
                return DjembeSoundInfo.DjembeBaseSound.getSoundForColor(color);
            case CLAVE:
                return ClaveSoundInfo.ClaveBaseSound.getSoundForColor(color);
        }
        throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
    }

	public void loadInstrument(InstrumentBaseSound[] sounds) {
		for (InstrumentBaseSound s : sounds) {
			mSoundSamples.put(s,  mSoundPool.load(mContext, s.getSoundResourceId(), 1));
		}
	}

    public void playSound(SoundInfo soundInfo) {
        if (soundInfo != null) {
            playSound(soundInfo.getSound());

            if (soundInfo instanceof DjembeSoundInfo) {

                DjembeSoundInfo djembeSound = (DjembeSoundInfo) soundInfo;
                if (djembeSound.getFlamSoundMod().ordinal() > 0) {
                    try {
                        Thread.sleep(100);
                        switch (djembeSound.getFlamSoundMod()) {
                            case FLAM_BASS:
                                playSound(DjembeSoundInfo.DjembeBaseSound.BASS_L);
                                break;
                            case FLAM_TONE:
                                playSound(DjembeSoundInfo.DjembeBaseSound.TONE_L);
                                break;
                            case FLAM_SLAP:
                                playSound(DjembeSoundInfo.DjembeBaseSound.SLAP_L);
                                break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error playing flam sound.", e);
                    }
                }
            }
        }
    }

    public void playSound(InstrumentBaseSound sound) {
        if (sound != null )
            playSound(sound.getIndex(), sound.getInstrument());
    }

	private void playSound(int soundId, Instrument instrument) {

        switch (instrument){
            case DJEMBE:
                mSoundPool.play(mSoundSamples.get( DjembeSoundInfo.DjembeBaseSound.values()[soundId] ), 1, 1, 1, 0, 1);
                return;
            case CLAVE:
                mSoundPool.play(mSoundSamples.get( ClaveSoundInfo.ClaveBaseSound.values()[soundId] ), 1, 1, 1, 0, 1);
                return;
        }
        throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
	}

	public static InstrumentBaseSound getSoundById(int soundId,  Instrument instrument) {

        if (soundId == -1) return null;

        switch (instrument){
            case DJEMBE:
                return DjembeSoundInfo.DjembeBaseSound.values()[soundId];
            case CLAVE:
                return ClaveSoundInfo.ClaveBaseSound.values()[soundId];
        }
		throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
	}

    public static int getSoundPanelId(boolean isSimpleMode, Instrument instrument){

        switch (instrument) {
            case DJEMBE:
                return isSimpleMode ? R.layout.djembe_img_panel : R.layout.djembe_panel;
            case CLAVE:
                return isSimpleMode ? R.layout.clave_img_panel : R.layout.clave_panel;
        }
        throw new UnsupportedOperationException("instrument "+instrument+" is unknown or not supported");
    }

    public static SoundInfo getNewSound(InstrumentBaseSound sound, int soundModMask){
        if (sound == null) return null;
        switch (sound.getInstrument()){
            case DJEMBE:
                return new DjembeSoundInfo(sound, soundModMask);
            case CLAVE:
                return new ClaveSoundInfo(sound,soundModMask);
        }
        throw new UnsupportedOperationException("instrument "+sound.getInstrument()+" is unknown or not supported");
    }

    public static Instrument getDefaultInstrument (){
        return Instrument.DJEMBE;
    }


}
