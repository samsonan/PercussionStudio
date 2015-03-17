package com.samsonan.android.percussionstudio.components;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.samsonan.android.percussionstudio.providers.PercussionDatabase;
import com.samsonan.android.percussionstudio.R;
import com.samsonan.android.percussionstudio.providers.RhythmListAdapter;

/**
 * List fragment with the list of rhythms
 */
public class RhythmListFragment extends ListFragment {

    private static String TAG = "RhythmListFragment";

    //parameter name to pass instrument id between activities
    // (for instance when we add new rhythm, we first ask for the instrument and then start editor activity with this instrument)
    public final static String INSTRUMENT_ID = "INSTRUMENT_ID";

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private Callbacks mCallbacks;

    RhythmListAdapter mRhythmListAdapter;

    public interface Callbacks {
        public void onRhythmSelected(long id);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RhythmListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        PercussionDatabase databaseHelper = new PercussionDatabase(getActivity());
        Cursor cursor = databaseHelper.getAllRhythms();

        mRhythmListAdapter = new RhythmListAdapter(getActivity(), cursor, 0);
        setListAdapter(mRhythmListAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        registerForContextMenu(getListView());
    }

    /**
     * Update adapter when we potentially have new rhythm info in DB
     */
    private void updateAdapter() {

        PercussionDatabase databaseHelper = new PercussionDatabase(getActivity());
        Cursor cursor = databaseHelper.getAllRhythms();

        mRhythmListAdapter.swapCursor(cursor);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    public void onResume() {
        updateAdapter();    //we should do this in case we returned using back button
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
        mHelper = new PercussionDatabase(getActivity());

    }

    PercussionDatabase mHelper;

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = null;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        Log.d(TAG, "onListItemClick. position:" + position + "; id:" + id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onRhythmSelected(id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
//        give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        //separate menu for internal rhythms
        MenuInflater inflater = getActivity().getMenuInflater();
        if (mHelper.getRhythmInfoById(info.id).getInternalFlag()>0)
            inflater.inflate(R.menu.rhythm_int_item_context, menu);
        else
            inflater.inflate(R.menu.rhythm_item_context, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {

            case R.id.action_remove:

                mHelper.removeRhythmById(info.id);
                updateAdapter();
                return true;
            case R.id.action_clone:

                mHelper.cloneRhythmById(info.id);
                updateAdapter();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_rhythm:
                onAddRhythm();
                return true;
            case R.id.action_settings:

                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onAddRhythm() {
        Log.d(TAG, "Adding new rhythm! But first, lets ask what instrument to be used?");

        requestInstrument();
    }


    private void requestInstrument() {

        DialogFragment dialog = new RequestInstrumentDialogFragment();
        dialog.show(getFragmentManager(), "RequestInstrumentDialogFragment");
    }

}
