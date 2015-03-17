package com.samsonan.android.percussionstudio.components;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.samsonan.android.percussionstudio.R;


/**
 * An activity representing a single RhythmItem detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link com.samsonan.android.percussionstudio.components.RhythmListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link RhythmEditFragment}.
 *
 *
 * RhythmViewActivity can be run in edit and create modes.
 *
 */
public class RhythmEditActivity extends Activity implements RhythmEditFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhythm_detail);

        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

           /**
            * RhythmEditActivity can be run in edit and create modes.
            * Edit mode - it receives rhythm id in intent
            * Create mode - it receives instrument id in intent
            **/

            if (getIntent().hasExtra(RhythmEditFragment.ARG_RHYTHM_ID))
                arguments.putLong(RhythmEditFragment.ARG_RHYTHM_ID,
                    getIntent().getLongExtra(RhythmEditFragment.ARG_RHYTHM_ID,-1));

            if (getIntent().hasExtra(RhythmListFragment.INSTRUMENT_ID))
                arguments.putInt(RhythmListFragment.INSTRUMENT_ID,
                    getIntent().getIntExtra(RhythmListFragment.INSTRUMENT_ID,-1));

            arguments.putInt(RhythmEditFragment.ARG_MODE, RhythmEditFragment.MODE_EDIT);

            mEditFragment = new RhythmEditFragment();
            mEditFragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.rhythm_detail_container, mEditFragment)
                    .commit();
        }
    }

    @Override
    public void setActivityTitle(String title) {
        try {
            getActionBar().setTitle(title);
        }catch(Exception e) {/* it should never happen, even if it happens - nothing serious. */}
    }

    RhythmEditFragment mEditFragment;

    private void retainFragment(){
        if (mEditFragment == null)
            mEditFragment = (RhythmEditFragment) getFragmentManager().findFragmentById(R.id.rhythm_detail_container);
    }

    /**
     * Delegate all actions to the edit fragment
     * @param button view being clicked (may be checkbox as well)
     */
    public void onSoundSelected(View button) {
        retainFragment();
        mEditFragment.onSoundSelected(button);
    }

    public void onInstrumentSelect(View button) {
        retainFragment();
        mEditFragment.onInstrumentSelect();
    }

    public void onBarAction(View button) {
        retainFragment();
        mEditFragment.onBarAction(button);
    }

    public void onTrackAction(View button) {
        retainFragment();
        mEditFragment.onTrackAction(button);
    }

    @Override
    public void onBackPressed() {
        mEditFragment.onBackPressed();
    }


}
