package com.samsonan.android.percussionstudio.components;

import android.app.Activity;
import android.os.Bundle;

import com.samsonan.android.percussionstudio.R;


/**
 * RhythmViewActivity can be run in view mode only.
 */
public class RhythmViewActivity extends Activity implements RhythmEditFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhythm_detail);

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
             * RhythmViewActivity can be run in view mode only.
             * it receives rhythm id in intent
             **/

            arguments.putLong(RhythmEditFragment.ARG_RHYTHM_ID,
                    getIntent().getLongExtra(RhythmEditFragment.ARG_RHYTHM_ID, -1));

            arguments.putInt(RhythmEditFragment.ARG_MODE, RhythmEditFragment.MODE_VIEW);

            RhythmEditFragment fragment = new RhythmEditFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.rhythm_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public void setActivityTitle(String title) {
        try {
            getActionBar().setTitle(title);
        }catch(Exception e) {/* it should never happen, even if it happens - nothing serious. */}
    }
}
