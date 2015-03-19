package com.samsonan.android.percussionstudio.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.samsonan.android.percussionstudio.entities.RhythmInfo;

/**
 * Clave Sound Panel
 *
 * Created by Andrey Samsonov on 19.03.2015.
 */
public class ClavePanelLayout extends FrameLayout implements InstrumentPanel {

    public ClavePanelLayout(Context context) {
        super(context, null);
    }

    public ClavePanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setupPanelAtPosition(int positionIdx, int trackIdx, RhythmInfo mRhythmInfo) {
        //do nothing at a time
    }

    @Override
    public void setPanelEventListener(OnSoundInfoUpdatedListener listener) {
        //do nothing at a time
    }
}
