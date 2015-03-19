package com.samsonan.android.percussionstudio.views;

import com.samsonan.android.percussionstudio.entities.RhythmInfo;
import com.samsonan.android.percussionstudio.entities.SoundInfo;

/**
 * Interface for all instrument panels
 * Created by Andrey Samsonov on 19.03.2015.
 */
public interface InstrumentPanel  {

    public void setupPanelAtPosition(int positionIdx, int trackIdx, RhythmInfo mRhythmInfo);
    public void setPanelEventListener(OnSoundInfoUpdatedListener listener);

    public interface OnSoundInfoUpdatedListener {
        public void onSoundUpdated(SoundInfo soundInfo);
    }

}