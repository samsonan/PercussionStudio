package com.samsonan.android.percussionstudio.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.samsonan.android.percussionstudio.entities.InstrumentFactory;
import com.samsonan.android.percussionstudio.entities.InstrumentSound;
import com.samsonan.android.percussionstudio.R;
import com.samsonan.android.percussionstudio.entities.MeasureTypes;
import com.samsonan.android.percussionstudio.entities.RhythmInfo;
import com.samsonan.android.percussionstudio.entities.SoundInfo;
import com.samsonan.android.percussionstudio.entities.TrackInfo;
import com.samsonan.android.percussionstudio.providers.PercussionDatabase;
import com.samsonan.android.percussionstudio.views.TrackView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A fragment representing a single Rhythm edit/view screen.
 * <p/>
 * Note: is used for both editing and viewing
 */
public class RhythmEditFragment extends Fragment
        implements View.OnClickListener,  //screen buttons (play rhythm, play settings) on click listener
        View.OnTouchListener,             //instrument sound panel on click listener (simple mode)
        RequestRhythmTitleDialogFragment.OnRequestRhythmTitleListener, //on after updating rhythm title
        RhythmInfoDialogFragment.OnRequestRhythmInfoListener, // on after updating rhythm props
        TrackView.FragmentContainerListener {               // TrackView events

    private static final String TAG = "RhythmEditFragment";

    /**
     * Fragment Views
     */
    private TrackView mMusicPanel;  //main track view
    private ImageButton mMainPlayButton;    //play/stop btn
    private ImageButton mPlaySettingsButton;//play settings btn

    private TextView mPlaySettingsText;

    private ViewGroup mSoundEditorPanel;
    private ViewGroup mBarEditPanel;
    private ViewGroup mTrackEditPanel;

    private View mRootView;

    /**
     * ===== Fragment args =========================================================================
     */

    public static final String ARG_RHYTHM_ID = "ARG_RHYTHM_ID";
    public static final String ARG_MODE = "MODE"; //true if edit, false if read-only

    public static int MODE_VIEW = 0;
    public static int MODE_EDIT = 1;

    private boolean mIsAddNewBarOnTrackEnd = false;

    private boolean mIsSimpleMode = true; //true - image for instruments, otherwise - buttons

    private boolean mIsEditMode;
    private boolean mIsChangesMade;

    private InstrumentFactory mFactory;

    private RhythmInfo mRhythmInfo;
    private InstrumentFactory.Instruments mCurrentInstrument;

    private int mBpm = 120;      //default bpm number

    private Timer mPlayerTimer;
    private int mCounter = 0;
    private boolean mIsPlayed = false;
    private boolean mIsOneTrackPlaying = false;
    private int mPlayTrack = -1; //-1 for all tracks, 0 for first, etc

    //it should be declared as global variable, otherwise it will be GC
    private SharedPreferences.OnSharedPreferenceChangeListener mSharePrefListener;

    /**
     * Container activity callbacks
     */
    public interface Callbacks {
        public void setActivityTitle(String title);
    }

    private Callbacks mActivityContainerCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Callbacks)
            mActivityContainerCallbacks = (Callbacks) activity;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RhythmEditFragment() {
    }

    /**
     * TrackView Callback Methods ==================================================================
     */

    /**
     * Whether we should add new measure bar when we reach track end or not
     */
    public boolean isAddBarOnTrackEnd() {
        return mIsAddNewBarOnTrackEnd;
    }

    /**
     * Adding new Track
     */
    public void onAddTrack() {
        Log.d(TAG, "Going to add track!");

        /**
         * OK. It is not recommended to use dialogs in that way (should use dialog fragments),
         * but this dialog is very local and wont be used anywhere else
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.instrument_request));

        builder.setCancelable(true);

        final Spinner instrumentSelector = new Spinner(getActivity());

        ArrayAdapter<InstrumentFactory.Instruments> instrumentAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                        InstrumentFactory.Instruments.values());

        instrumentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSelector.setAdapter(instrumentAdapter);
        builder.setView(instrumentSelector);

        builder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int instrPosition = instrumentSelector.getSelectedItemPosition();

                Log.d(TAG, "Ready to create new track. instrument: " + InstrumentFactory.Instruments.values()[instrPosition]);

                InstrumentFactory.Instruments instrument = InstrumentFactory.Instruments.values()[instrPosition];

                TrackInfo newTrack = new TrackInfo(instrument, mRhythmInfo.getSoundNumberForBar());
                mRhythmInfo.addTrack(newTrack);

                mMusicPanel.setRhythmInfo(mRhythmInfo, true);
                mIsChangesMade = true;
            }
        });

        builder.setNegativeButton(R.string.btn_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        builder.show();
    }

    /**
     * Adding bar to the end of a track
     */
    @Override
    public void onAddBarToTrack(int trackIdx) {
        mIsChangesMade = true;
        Log.d(TAG, "onAddBarToTrack.  trackIdx:" + trackIdx);
        mRhythmInfo.getTracks().get(trackIdx).addBars(1, mRhythmInfo.getSoundNumberForBar());
        mMusicPanel.setRhythmInfo(mRhythmInfo, true);
        mIsChangesMade = true;

    }

    /**
     * Play Single track
     */
    @Override
    public void onPlayTrack(int trackIdx) {
        mIsOneTrackPlaying = true;
        onPlayStop(trackIdx);
    }

    /**
     * Sound bit is selected - need to show instrument panel
     *
     * @param positionIdx which sound bit is selected
     * @param trackIdx    selected track index
     */
    @Override
    public void onPositionSelected(int positionIdx, int trackIdx) {

        if (!mIsEditMode) return;   //view mode - do nothing

        Log.d(TAG, "onPositionSelected.  positionIdx:" + positionIdx + ", trackIdx:" + trackIdx);

        mCurrentInstrument = mRhythmInfo.getTracks().get(trackIdx).getInstrument();

        showInstrumentPanel();

        mTrackEditPanel.setVisibility(View.GONE);
        mBarEditPanel.setVisibility(View.GONE);
    }

    /**
     * Measure Bar is selected
     *
     * @param barIdx   which measure bar is selected
     * @param trackIdx selected track index
     */
    @Override
    public void onBarSelected(int barIdx, int trackIdx) {

        if (!mIsEditMode) return;   //view mode - do nothing

        Log.d(TAG, "onBarSelected.  trackIdx:" + trackIdx + ", barIdx:" + barIdx);

        if (trackIdx > -1 && barIdx > -1) {
            mSoundEditorPanel.setVisibility(View.GONE);
            mTrackEditPanel.setVisibility(View.GONE);
            mBarEditPanel.setVisibility(View.VISIBLE);

        } else {//deselect
            mSoundEditorPanel.setVisibility(View.VISIBLE);
            mTrackEditPanel.setVisibility(View.GONE);
            mBarEditPanel.setVisibility(View.GONE);
        }
    }

    /**
     * Track header is selected
     *
     * @param trackIdx selected track index
     */
    @Override
    public void onTrackHeaderSelected(int trackIdx) {

        if (!mIsEditMode) return;   //view mode - do nothing

        Log.d(TAG, "onTrackHeaderSelected.  trackIdx:" + trackIdx);

        if (trackIdx > -1) {
            mSoundEditorPanel.setVisibility(View.GONE);
            mTrackEditPanel.setVisibility(View.VISIBLE);
            mBarEditPanel.setVisibility(View.GONE);

            /**
             * Connect track to the previous track - only if there is a prev. track!
             */
            CheckBox connectedChb = (CheckBox) mRootView.findViewById(R.id.is_connected_chb);
            connectedChb.setChecked(mRhythmInfo.getTracks().get(trackIdx).isConnectedPrev());
            connectedChb.setEnabled(trackIdx > 0);

        } else {
            mSoundEditorPanel.setVisibility(View.GONE);
            mTrackEditPanel.setVisibility(View.VISIBLE);
            mBarEditPanel.setVisibility(View.GONE);
        }
    }

    /**
     * TrackView Callback Methods END ==============================================================
     */

    /**
     * Panel Action methods (instrument, bar or track panel events delegated from activity==========
     */

    /**
     * Some event happened in regards to a track (deleting, changing order , etc)
     *
     * @param button button responsible for event happened
     */
    public void onTrackAction(View button) {
        int trackIdx = mMusicPanel.getSelectedTrack();
        Log.d(TAG, "onTrackAction.  trackIdx:" + trackIdx);

        switch (button.getId()) {
            case R.id.action_track_delete:

                if (mRhythmInfo.getTracks().size() > 1) {

                    mRhythmInfo.removeTrack(trackIdx);
                    mMusicPanel.setRhythmInfo(mRhythmInfo, true);
                    mIsChangesMade = true;

                    mMusicPanel.setSelectedPosition(0, 0);
                    onPositionSelected(0, 0);//imitate position click in order to show instrument panel
                }
                break;
            case R.id.action_track_move_down:

                if (trackIdx < mRhythmInfo.getTracks().size() - 1) {

                    mRhythmInfo.swapTracks(trackIdx + 1, trackIdx);
                    mMusicPanel.setRhythmInfo(mRhythmInfo, false);
                    mIsChangesMade = true;

                    mMusicPanel.setSelectedTrack(trackIdx + 1);
                    onTrackHeaderSelected(trackIdx + 1);
                }
                break;
            case R.id.action_track_move_up:

                if (trackIdx > 0) {

                    mRhythmInfo.swapTracks(trackIdx - 1, trackIdx);
                    mMusicPanel.setRhythmInfo(mRhythmInfo, false);
                    mIsChangesMade = true;

                    mMusicPanel.setSelectedTrack(trackIdx - 1);
                    onTrackHeaderSelected(trackIdx - 1);
                }

                break;
            case R.id.is_connected_chb:

                if (((CheckBox) button).isChecked()) {
                    mRhythmInfo.setConnectedFlag(trackIdx);
                } else {
                    mRhythmInfo.getTracks().get(trackIdx).setConnectedPrev(false);
                }

                mMusicPanel.setRhythmInfo(mRhythmInfo, false);
                mIsChangesMade = true;

                break;
        }
    }

    /**
     * Some event happened in regards to a measure bar (deleting, adding, etc)
     *
     * @param button button responsible for event happened
     */
    public void onBarAction(View button) {

        int trackIdx = mMusicPanel.getSelectedTrack();
        int barIdx = mMusicPanel.getSelectedBar();

        Log.d(TAG, "onBarAction.  trackIdx:" + trackIdx + ", barIdx:" + barIdx);

        switch (button.getId()) {
            case R.id.add_bar:
                mRhythmInfo.getTracks().get(trackIdx).addBars((barIdx+1), 1, mRhythmInfo.getSoundNumberForBar());
                break;
            case R.id.clone_bar:
                mRhythmInfo.getTracks().get(trackIdx).cloneBar(barIdx, mRhythmInfo.getSoundNumberForBar());
                break;
            case R.id.delete_bar:

                //only one bar - it cannot be deleted!
                if (mRhythmInfo.getTracks().get(trackIdx).getBarCnt() <= 1)
                    return;

                mRhythmInfo.getTracks().get(trackIdx).removeBar(barIdx, mRhythmInfo.getSoundNumberForBar());
                mMusicPanel.setSelectedPosition(0, trackIdx);
                onPositionSelected(0, trackIdx);
                break;
        }

        mRhythmInfo.removeConnectedFlagCascade(trackIdx + 1);

        mMusicPanel.setRhythmInfo(mRhythmInfo, true);
        mIsChangesMade = true;

    }

    /**
     * Use selected sound from instrument panel
     */
    public void onSoundSelected(View button) {

        if (button.getId() == R.id.sound_del) { //backspace button
            int positionX = mMusicPanel.getSelectedPosition();
            if (positionX == 0)
                return;

            int trackIdx = mMusicPanel.getSelectedTrack();
            TrackInfo track = mRhythmInfo.getTracks().get(trackIdx);
            track.getSounds()[positionX - 1] = null;

            mMusicPanel.setSelectedPosition(positionX - 1, trackIdx);
            mMusicPanel.setRhythmInfo(mRhythmInfo, false);
            mIsChangesMade = true;

            return;
        }

        InstrumentSound newSound = InstrumentFactory.getSoundForButtonId(button.getId(), mCurrentInstrument);
        onSoundSelected(newSound);
    }

    public void onSoundSelected(InstrumentSound newSound) {
        TrackInfo track = mRhythmInfo.getTracks().get(mMusicPanel.getSelectedTrack());
        if (newSound == null)
            track.getSounds()[mMusicPanel.getSelectedPosition()] = null;
        else {
            track.getSounds()[mMusicPanel.getSelectedPosition()] = new SoundInfo(newSound);
        }

        mMusicPanel.setRhythmInfo(mRhythmInfo, false);
        mIsChangesMade = true;

        mMusicPanel.incrementSelectedPosition();
    }


    /**
     * ============= Dialog callbacks methods ======================================================
     */

    /**
     * Returned from Rhythm Properties Dialog. Updating rhythm properties
     */
    @Override
    public void onRequestRhythmInfoCommit(String newTitle, String newDescription, MeasureTypes newMeasure, String newCategory) {

        Log.d(TAG, "onRequestRhythmInfoCommit. newTitle:" + newTitle + ", newDescription:" + newDescription + ", " +
                "newMeasure:" + newMeasure + ", newCategory:" + newCategory);

        mActivityContainerCallbacks.setActivityTitle(newTitle);

        mRhythmInfo.setTitle(newTitle);
        mRhythmInfo.setMeasure(newMeasure);
        mRhythmInfo.setCategory(newCategory);
        mRhythmInfo.setDescription(newDescription);

        mMusicPanel.setRhythmInfo(mRhythmInfo, false);
        mIsChangesMade = true;
    }

    /**
     * Returned from RequestRhythmTitleDialogFragment after clone event
     *
     * @param newTitle title of the new cloned rhythm
     * @param newId    id of the new cloned rhythm
     */
    @Override
    public void onRequestRhythmCloneCommit(String newTitle, long newId) {

        mRhythmInfo.setTitle(newTitle);
        mRhythmInfo.setId(newId);

        if (mActivityContainerCallbacks != null)
            mActivityContainerCallbacks.setActivityTitle(newTitle);

        updateRhythmInDatabase();
    }

    /**
     * Returned from RequestRhythmTitleDialogFragment after saving event
     *
     * @param newTitle new rhythm title
     */
    @Override
    public void onRequestRhythmTitleCommit(String newTitle) {

        mRhythmInfo.setTitle(newTitle);

        if (mActivityContainerCallbacks != null)
            mActivityContainerCallbacks.setActivityTitle(newTitle);

        updateRhythmInDatabase();
    }


    /**
     * Simplified UI. Instrument image click event processing
     */
    @Override
    public boolean onTouch(View v, MotionEvent ev) {

        if (!mIsSimpleMode)
            return true;

        final int action = ev.getAction();
        final int evX = (int) ev.getX();
        final int evY = (int) ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:

                int intColor = getHotspotColor(InstrumentFactory.getHotspotImageId(mCurrentInstrument), evX, evY);

                Log.d(TAG, "Touched color is:" + intColor + ". RGB:" + String.format("#%06X", (0xFFFFFF & intColor)));

                InstrumentSound newSound = InstrumentFactory.getSoundForColor(intColor, mCurrentInstrument);
                if (newSound != null)
                    mFactory.playSound(newSound);
                onSoundSelected(newSound);

                break;
        } // end switch
        return true;
    }

    /**
     * Get color from our hotspot image
     */
    private int getHotspotColor(int hotspotId, int x, int y) {
        try {
            ImageView img = (ImageView) mRootView.findViewById(hotspotId);
            img.setDrawingCacheEnabled(true);
            Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
            img.setDrawingCacheEnabled(false);
            return hotspots.getPixel(x, y);
        }catch (Exception e){
            return 0;
        }
    }

    /**
     * ===================== Fragment lifecycle methods ============================================
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsEditMode = getArguments().getInt(ARG_MODE, MODE_VIEW) == MODE_EDIT;  //read-only by default

        setHasOptionsMenu(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mSharePrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.d(TAG, "shared preference " + key + " has changed.");

                if (SettingsActivity.KEY_PREF_UI_MODE.equals(key)) {
                    mIsSimpleMode = prefs.getBoolean(key, false);
                    if (!mIsEditMode || mMusicPanel.getSelectedPosition() < 0)
                        return;
                    showInstrumentPanel();
                } else if (SettingsActivity.KEY_PREF_AUTO_ADD_BAR.equals(key)) {
                    mIsAddNewBarOnTrackEnd = prefs.getBoolean(key, false);
                }
            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(mSharePrefListener);

        mIsSimpleMode = sharedPref.getBoolean(SettingsActivity.KEY_PREF_UI_MODE, false);
        mIsAddNewBarOnTrackEnd = sharedPref.getBoolean(SettingsActivity.KEY_PREF_AUTO_ADD_BAR, false);

        Log.d(TAG, "onCreate. args:" + getArguments() + "; Is Edit Mode:" + mIsEditMode + "; isSimpleMode:" + mIsSimpleMode);

        PercussionDatabase databaseHelper = new PercussionDatabase(getActivity());

        if (getArguments().containsKey(ARG_RHYTHM_ID)) {

            mRhythmInfo = databaseHelper.getRhythmInfoById(getArguments().getLong(ARG_RHYTHM_ID));
            if (mRhythmInfo.getBpm() > 0)
                mBpm = mRhythmInfo.getBpm();
        }
        if (getArguments().containsKey(RhythmListFragment.INSTRUMENT_ID)) {
            mCurrentInstrument = InstrumentFactory.Instruments.values()[getArguments().getInt(RhythmListFragment.INSTRUMENT_ID)];
        } else {
            mCurrentInstrument = InstrumentFactory.getDefaultInstrument();
        }

        mPlayerTimer = new Timer();
        mFactory = new InstrumentFactory(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView. mRhythmItem:" + mRhythmInfo);

        mRootView = inflater.inflate(R.layout.fragment_rhythm_detail, container, false);

        mSoundEditorPanel = (ViewGroup) mRootView.findViewById(R.id.sounds_panel);
        mBarEditPanel = (ViewGroup) mRootView.findViewById(R.id.bar_edit_panel);
        mTrackEditPanel = (ViewGroup) mRootView.findViewById(R.id.track_edit_panel);

        //play rhythm button
        mMainPlayButton = (ImageButton) mRootView.findViewById(R.id.play_stop_btn);
        mMainPlayButton.setOnClickListener(this);

        mPlaySettingsButton = (ImageButton) mRootView.findViewById(R.id.play_settings_btn);
        mPlaySettingsButton.setOnClickListener(this);

        mPlaySettingsText = (TextView) mRootView.findViewById(R.id.rhythm_settings_text);
        mPlaySettingsText.setText(mBpm + "");

        mMusicPanel = (TrackView) mRootView.findViewById(R.id.music_panel);
        mMusicPanel.setFragmentContainerListener(this);

        /**
         * By default show instrument panel and hide others. In view mode just hide everything
         */
        if (mIsEditMode)
            setSoundPanelForInstrument(mCurrentInstrument, inflater);
        else
            mSoundEditorPanel.setVisibility(View.GONE);
        mBarEditPanel.setVisibility(View.GONE);
        mTrackEditPanel.setVisibility(View.GONE);

        if (mRhythmInfo == null) {
            mRhythmInfo = new RhythmInfo(mCurrentInstrument);
        }

        if (mMusicPanel != null) {
            mMusicPanel.setRhythmInfo(mRhythmInfo, true);
            mMusicPanel.setIsEditable(mIsEditMode);
            mMusicPanel.setSelectedPosition(0, 0);
        }

        if (mActivityContainerCallbacks != null) {
            String title = mRhythmInfo.getTitle();

            if (title == null)
                title = getResources().getString(R.string.title_new_rhythm);

            mActivityContainerCallbacks.setActivityTitle(title);
        }

        EditText trackTitleEditText = (EditText) mRootView.findViewById(R.id.track_title_edit);
        trackTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mRhythmInfo.getTracks().get(mMusicPanel.getSelectedTrack()).setTitle(((EditText) v).getText().toString());
                mMusicPanel.setRhythmInfo(mRhythmInfo, false);
            }
        });

        onRestoreInstanceState(savedInstanceState);

        return mRootView;
    }


    private long mTmpRhythmId = -1;
    private static final String INSTANCE_STATE_TMP_ID = "INSTANCE_STATE_TMP_ID";
    private static final String INSTANCE_STATE_ID = "INSTANCE_STATE_ID";
    private static final String INSTANCE_STATE_BPM = "INSTANCE_STATE_BPM";
    private static final String INSTANCE_STATE_UNSAVED_CHANGES = "INSTANCE_STATE_UNSAVED_CHANGES";

    @Override
    public void onSaveInstanceState (Bundle outState) {
        Log.d(TAG, "onSaveInstanceState() start");

        if (outState != null) {
            if (mIsEditMode) {
                PercussionDatabase databaseHelper = new PercussionDatabase(getActivity());
                outState.putLong(INSTANCE_STATE_ID, mRhythmInfo.getId());
                outState.putBoolean(INSTANCE_STATE_UNSAVED_CHANGES, mIsChangesMade);
                mTmpRhythmId = databaseHelper.saveRhythmsState(mRhythmInfo, mTmpRhythmId);
                outState.putLong(INSTANCE_STATE_TMP_ID, mTmpRhythmId);
            } else
                outState.putInt(INSTANCE_STATE_BPM, mBpm);

        }

        Log.d(TAG, "onSaveInstanceState() end");
    }

    public void onRestoreInstanceState(Bundle savedState) {
        Log.d(TAG, "onRestoreInstanceState() start. savedState:" + savedState);

        if (savedState != null) {
            if (mIsEditMode) {
                mIsChangesMade = savedState.getBoolean(INSTANCE_STATE_UNSAVED_CHANGES);
                PercussionDatabase databaseHelper = new PercussionDatabase(getActivity());
                mRhythmInfo = databaseHelper.getRhythmInfoById(savedState.getLong(INSTANCE_STATE_TMP_ID));
                mRhythmInfo.setId(savedState.getLong(INSTANCE_STATE_ID));
                mMusicPanel.setRhythmInfo(mRhythmInfo, true);
                mActivityContainerCallbacks.setActivityTitle(mRhythmInfo.getTitle());
            } else
                mBpm = savedState.getInt(INSTANCE_STATE_BPM);
        }

        Log.d(TAG, "onRestoreInstanceState() end");
    }

    public void onBackPressed() {

        Log.d(TAG, "onBackPressed()");

        if (!mIsEditMode || !mIsChangesMade) {
            returnToParentActivity();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Discard?")
                    .setMessage("Your changes are not saved. Are you sure you want to discard changes?")
                    .setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //exit activity
                            returnToParentActivity();
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });

            builder.show();
        }
    }

    /**
     * Depending on the device and layout (1-pane o 2-pane, we may have different stack of activities).
     * There may be or may be not RhythmListActivity in the back stack, nut since it is in a singleTask mode,
     * we don't care
     */
    private void returnToParentActivity(){

        Intent newIntent = new Intent(getActivity(), RhythmListActivity.class);
        startActivity(newIntent);

        getActivity().finish();
    }

    /**
     * Rhythm Playing related events
     */
    @Override
    public void onClick(View button) {

        switch (button.getId()) {
            case R.id.play_stop_btn:
                mIsOneTrackPlaying = false;
                onPlayStop(-1);
                break;
            case R.id.play_settings_btn:
                onPlaySettings();
                break;
        }
    }

    /**
     * Changing play settings (both view and edit mode)
     */
    private void onPlaySettings() {

        final int MIN_BPM = 60;
        final int MAX_BPM = 500;

        /**
         * OK. It is not recommended to use dialogs in that way (should use dialog fragments),
         * but this dialog is very local and wont be used anywhere else
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View layout = inflater.inflate(R.layout.play_settings_dialog, null);

        final EditText bpmEdit = (EditText) layout.findViewById(R.id.bpm_number_edit);
        bpmEdit.setText(Integer.toString(mBpm));

        final SeekBar bpmEditBar = (SeekBar) layout.findViewById(R.id.bpm_seekbar);
        bpmEditBar.setProgress(mBpm - MIN_BPM);
        bpmEditBar.setMax(MAX_BPM - MIN_BPM);

        bpmEditBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpmEdit.setText(Integer.toString(progress + MIN_BPM));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        builder.setView(layout)
                // Add action buttons
                .setTitle(R.string.title_play_settings)
                .setPositiveButton(R.string.btn_confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mBpm = bpmEditBar.getProgress() + MIN_BPM;
                                mRhythmInfo.setBpm(mBpm);
                                mPlaySettingsText.setText(mBpm + "");
                            }
                        }
                )
                .setNegativeButton(R.string.btn_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //do nothing
                            }
                        }
                );

        builder.show();
    }

    private void onPlayStop(int trackIdx) {
        Log.d(TAG, "onClick. mRhythmInfo:" + mRhythmInfo + "; mMusicPanel:" + mMusicPanel + ";trackIdx:" + trackIdx);

        if (mIsPlayed) { //if music is being played
            mPlayerTimer.cancel();  //stop timer
            mCounter = 0;           //reset cursor position
            mMusicPanel.setCurrentPlayPosition(mCounter + 1, -1);
            mIsPlayed = false;

            changeControlsOnStop();
        } else {

            changeControlsOnPlay();

            mPlayerTimer = new Timer();

            int delay = 1000;
            try {
                delay = Double.valueOf(Math.floor(1000 / (mBpm / 60.))).intValue();
            } catch (Exception e) {
                Log.e(TAG, "Exception while trying to calculate timer delay.", e);
            }

            mPlayerTimer.schedule(new PlayerTimerTask(), 0, delay); //option 3
            mIsPlayed = true;
            mPlayTrack = trackIdx;
        }

        mMusicPanel.onPlayStopRhythm(mIsPlayed, mIsOneTrackPlaying);
        mMusicPanel.invalidate();
    }

    /**
     * Disable/hide all controls when start playing
     */
    private void changeControlsOnPlay() {
        mSoundEditorPanel.setVisibility(View.GONE);
        mTrackEditPanel.setVisibility(View.GONE);
        mBarEditPanel.setVisibility(View.GONE);
        mPlaySettingsButton.setEnabled(false);
        mMainPlayButton.setImageResource(R.drawable.ic_stop_36dp);
    }

    /**
     * Enable/show all controls when stop playing
     */
    private void changeControlsOnStop() {
        onPositionSelected(0, 0);
        mPlaySettingsButton.setEnabled(true);
        mMainPlayButton.setImageResource(R.drawable.ic_play_36dp);
    }

    /**
     * View --> Edit Rhythm
     */
    public void onEditMode() {
        Intent intent = new Intent(getActivity(), RhythmEditActivity.class);
        intent.putExtra(ARG_RHYTHM_ID, mRhythmInfo.getId());
        startActivity(intent);
        getActivity().finish();
    }

    private void onCloneRhythm() {
        requestTitle(RequestRhythmTitleDialogFragment.MODE_CLONE);
    }

    private void onSaveRhythm() {

        if (mRhythmInfo.getTitle() == null || mRhythmInfo.getTitle().trim().length() == 0) {
            requestTitle(RequestRhythmTitleDialogFragment.MODE_NEW);
            return;
        }

        updateRhythmInDatabase();
    }


    /**
     * Request rhythms title - on clone or on save track without title
     *
     * @param mode RequestRhythmTitleDialogFragment mode
     */
    private void requestTitle(int mode) {
        DialogFragment newFragment = new RequestRhythmTitleDialogFragment();

        Bundle args = new Bundle();
        args.putInt(RequestRhythmTitleDialogFragment.DIALOG_ARG_MODE, mode);
        args.putLong(RequestRhythmTitleDialogFragment.DIALOG_ARG_RHYTHM_ID, mRhythmInfo.getId());
        newFragment.setArguments(args);
        newFragment.setTargetFragment(this, 0); //requestCode doesn't matter
        newFragment.show(getFragmentManager(), "RequestRhythmTitleDialogFragment");
    }

    private void onShowInfo() {
        DialogFragment newFragment = new RhythmInfoDialogFragment();

        Bundle args = new Bundle();
        args.putInt(RhythmInfoDialogFragment.DIALOG_ARG_MODE, mIsEditMode ? RhythmInfoDialogFragment.MODE_EDIT : RhythmInfoDialogFragment.MODE_VIEW);

        args.putString(RhythmInfoDialogFragment.DIALOG_ARG_TITLE, mRhythmInfo.getTitle());
        args.putString(RhythmInfoDialogFragment.DIALOG_ARG_DESCRIPTION, mRhythmInfo.getDescription());
        args.putString(RhythmInfoDialogFragment.DIALOG_ARG_MEASURE, mRhythmInfo.getBitsPerBar() + "/" + mRhythmInfo.getBitLength());
        args.putString(RhythmInfoDialogFragment.DIALOG_ARG_CATEGORY, mRhythmInfo.getCategory());

        newFragment.setArguments(args);
        newFragment.setTargetFragment(this, 0); //requestCode doesn't matter
        newFragment.show(getFragmentManager(), "RhythmInfoDialogFragment");
    }

    private void onDeleteRhythm() {
        PercussionDatabase helper = new PercussionDatabase(getActivity());
        helper.removeRhythmById(mRhythmInfo.getId());

        returnToParentActivity();
    }

    private void updateRhythmInDatabase() {
        PercussionDatabase helper = new PercussionDatabase(getActivity());
        long newID = helper.saveRhythm(mRhythmInfo,false);

        Log.d(TAG, "Rhythm ID [" + mRhythmInfo.getId() + "] is saved. New ID [" + newID + "]");

        mRhythmInfo.setId(newID);
        mIsChangesMade = false;
    }


    /**
     * User selected another musical instrument
     */
    public void onInstrumentSelect() {

        /**
         * OK. It is not recommended to use dialogs in that way (should use dialog fragments),
         * but this dialog is very local and wont be used anywhere else
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.instrument_request));

        builder.setCancelable(true);

        final Spinner instrumentSelector = new Spinner(getActivity());

        ArrayAdapter<InstrumentFactory.Instruments> instrumentAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                        InstrumentFactory.Instruments.values());

        instrumentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        instrumentSelector.setAdapter(instrumentAdapter);

        builder.setView(instrumentSelector);

        builder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int instrPosition = instrumentSelector.getSelectedItemPosition();

                InstrumentFactory.Instruments selected = InstrumentFactory.Instruments.values()[instrPosition];
                Log.d(TAG, "New instrument selected:" + selected);

                if (mCurrentInstrument != selected) { //user selected new instrument
                    mCurrentInstrument = selected;
                    //remove all sound information from current track
                    mRhythmInfo.getTracks().get(mMusicPanel.getSelectedTrack()).discardSoundInformation();
                    //set new instrument for the track
                    mRhythmInfo.getTracks().get(mMusicPanel.getSelectedTrack()).setInstrument(selected);

                    mMusicPanel.setRhythmInfo(mRhythmInfo, false);
                    Log.d(TAG, "all sound information for track " + mMusicPanel.getSelectedTrack() + " is discarded");
                }

                showInstrumentPanel();
            }
        });

        builder.setNegativeButton(R.string.btn_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        builder.show();
    }

    private void showInstrumentPanel() {
        if (getActivity() != null)
            setSoundPanelForInstrument(mCurrentInstrument, getActivity().getLayoutInflater());
        mSoundEditorPanel.setVisibility(View.VISIBLE);
    }

    /**
     * Set instrument sound panel according to the instrument
     *
     * @param instrument which instrument to set sound panel for
     * @param inflater layout inflater
     */
    public void setSoundPanelForInstrument(InstrumentFactory.Instruments instrument, LayoutInflater inflater) {

        ViewGroup soundEditorPanelHolder = (ViewGroup) mSoundEditorPanel.getParent();
        int index = soundEditorPanelHolder.indexOfChild(mSoundEditorPanel);
        ViewGroup.LayoutParams layoutParams = mSoundEditorPanel.getLayoutParams();
        soundEditorPanelHolder.removeView(mSoundEditorPanel);

        if (instrument == InstrumentFactory.Instruments.CLAVE) {
            mSoundEditorPanel = (ViewGroup) inflater.inflate(mIsSimpleMode ? R.layout.clave_img_panel : R.layout.clave_panel, soundEditorPanelHolder, false);
        } else if (instrument == InstrumentFactory.Instruments.DJEMBE) {
            mSoundEditorPanel = (ViewGroup) inflater.inflate(mIsSimpleMode ? R.layout.djembe_img_panel : R.layout.djembe_panel, soundEditorPanelHolder, false);
        }

        soundEditorPanelHolder.addView(mSoundEditorPanel, index, layoutParams);
        mSoundEditorPanel.setOnTouchListener(this);
    }

    /**
     * Menu Related Methods ========================================================================
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mIsEditMode)
            inflater.inflate(R.menu.menu_edit_rhythm, menu);
        else
            inflater.inflate(R.menu.menu_view_rhythm, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                onSaveRhythm();
                return true;
            case R.id.action_edit:
                onEditMode();
                return true;
            case R.id.action_clone:
                onCloneRhythm();
                return true;
            case R.id.action_remove:
                onDeleteRhythm();
                return true;
            case R.id.action_info:
                onShowInfo();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:

                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class PlayerTimerTask extends TimerTask {

        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //index of the sound playing

                    if (mPlayTrack == -1) {
                        Log.d(TAG, "Start playing. play mode - all.mCounter:" + mCounter);
                        mPlayTrack = 0;
                    }

                    for (int i = mPlayTrack; i < mRhythmInfo.getTracks().size(); i++) {

                        //we are playing whole rhythm but not the current and not the connected track, move to next track
                        if (!mIsOneTrackPlaying && mPlayTrack != i && !mRhythmInfo.getTracks().get(i).isConnectedPrev())
                            break;

                        SoundInfo sound = mRhythmInfo.getTracks().get(i).getSounds()[mCounter];

                        if (sound != null) {
                            if (mIsOneTrackPlaying && mPlayTrack == i) {//playing one specific track
                                mFactory.playSound(sound.getSound());
                                break;//only one track is played, no need to go through every track
                            } else if (!mIsOneTrackPlaying && (mPlayTrack == i || mPlayTrack != i && mRhythmInfo.getTracks().get(i).isConnectedPrev()))
                                mFactory.playSound(sound.getSound());
                        }
                    }

                    mMusicPanel.setCurrentPlayPosition(mCounter, mPlayTrack);
                    mMusicPanel.invalidate();// redraw music panel

                    mCounter++;

                    mCounter = mCounter % (mRhythmInfo.getSoundNumberForBar() * mRhythmInfo.getTracks().get(mPlayTrack).getBarCnt());

                    if (!mIsOneTrackPlaying && mCounter == 0) {
                        //we should check track play mode of the NEXT track and if the mode of the NEXT track is sequential, we should change track
                        Log.d(TAG, "we reached the end of the track, current counter value is 0. switching track");
                        int newPlayTrack = 0; //if we will not find the next seq track, then move to the first one
                        for (int i = mPlayTrack + 1; i < mRhythmInfo.getTracks().size(); i++) {
                            if (!mRhythmInfo.getTracks().get(i).isConnectedPrev()) {
                                Log.d(TAG, "current playing track is " + mPlayTrack + ". found track idx " + i + " with seq playing mode. new playing track will have idx " + i);
                                newPlayTrack = i;
                                break;
                            }
                        }
                        mPlayTrack = newPlayTrack;
                    }
                }
            });
        }
    }


}
