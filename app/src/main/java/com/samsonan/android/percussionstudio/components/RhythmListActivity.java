package com.samsonan.android.percussionstudio.components;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.samsonan.android.percussionstudio.R;

/**
 * Activity containing fragment with the list of rhythms
 * (or list and rhythm detail view fragment for wide screens)
 */
public class RhythmListActivity extends Activity
        implements RhythmListFragment.Callbacks {

    private static final String TAG = "RhythmListActivity";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhythmitem_list);

        if (findViewById(R.id.rhythm_detail_container) != null) {

            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((RhythmListFragment) getFragmentManager()
                    .findFragmentById(R.id.rhythmitem_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onRhythmSelected(long id) {

        Log.d(TAG, "onRhythmSelected. id="+id);

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(RhythmEditFragment.ARG_RHYTHM_ID, id);
            arguments.putInt(RhythmEditFragment.ARG_MODE, RhythmEditFragment.MODE_VIEW);  //in two-pane only View mode
            RhythmEditFragment fragment = new RhythmEditFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.rhythm_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, RhythmViewActivity.class);
            detailIntent.putExtra(RhythmEditFragment.ARG_RHYTHM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onRhythmDeleted() {
        if (mTwoPane) {
            Fragment currentDetailFragment = getFragmentManager().findFragmentById(R.id.rhythm_detail_container);
            if (currentDetailFragment != null)
                getFragmentManager().beginTransaction().remove(currentDetailFragment).commit();
        }
    }
}
