package com.samsonan.android.percussionstudio.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.samsonan.android.percussionstudio.R;
import com.samsonan.android.percussionstudio.entities.DjembeSoundInfo;
import com.samsonan.android.percussionstudio.entities.RhythmInfo;

/**
 * Djembe sound panel
 *
 * Created by Andrey Samsonov on 19.03.2015.
 */
public class DjembePanelLayout extends FrameLayout implements InstrumentPanel {

    public final static String TAG = "DjembePanelLayout";

    private Context mContext;

    public DjembePanelLayout(Context context) {
        super(context, null);
        mContext = context;
    }

    public DjembePanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    OnSoundInfoUpdatedListener mCallback;

    public void setPanelEventListener(OnSoundInfoUpdatedListener listener) {
        mCallback = listener;
    }

    public void setupPanelAtPosition(final int positionIdx, final int trackIdx, final RhythmInfo rhythmInfo){

        /**
         * If position is not selected or there is no rhythm object, then what the hell are we doing here?!
         */
        if (trackIdx < 0 || positionIdx < 0 || rhythmInfo == null)
            return;

        Log.d(TAG, "setupPanelAtPosition(). Going to set up flam spinner. " +
                "positionIdx:" + positionIdx + ", trackIdx:" + trackIdx +
                ". current sound: " + rhythmInfo.getTrackAtIdx(trackIdx).getSoundAtIdx(positionIdx));


        Spinner flamSpinner = (Spinner) findViewById(R.id.flam_snd_spinner);

        if (rhythmInfo.getTrackAtIdx(trackIdx).getSoundAtIdx(positionIdx)== null) {
            flamSpinner.setVisibility(View.GONE);
            return;
        }

        flamSpinner.setVisibility(View.VISIBLE);

        final ArrayAdapter<String> flamAdapter =
                new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item,
                        DjembeSoundInfo.DjembeFlamSoundMod.stringValues(getResources()));

        flamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flamSpinner.setAdapter(flamAdapter);

        flamSpinner.setSelection(((DjembeSoundInfo) rhythmInfo.getTrackAtIdx(trackIdx).getSoundAtIdx(positionIdx))
                .getFlamSoundMod().ordinal());

        flamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSoundFlamSelected(DjembeSoundInfo.DjembeFlamSoundMod.values()[position], positionIdx, trackIdx, rhythmInfo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void onSoundFlamSelected(DjembeSoundInfo.DjembeFlamSoundMod flamMod, final int positionIdx, final int trackIdx, final RhythmInfo rhythmInfo){

        DjembeSoundInfo soundInfo = (DjembeSoundInfo) rhythmInfo.getTrackAtIdx(trackIdx).getSoundAtIdx(positionIdx);

        if (soundInfo == null)
            return;

        boolean isHasChanged = soundInfo.setFlamMod(flamMod);

        if (!isHasChanged) return;

        mCallback.onSoundUpdated(soundInfo);

    }




}
