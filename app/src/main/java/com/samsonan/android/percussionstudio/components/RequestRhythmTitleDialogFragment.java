package com.samsonan.android.percussionstudio.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.samsonan.android.percussionstudio.R;
import com.samsonan.android.percussionstudio.entities.RhythmInfo;
import com.samsonan.android.percussionstudio.providers.PercussionDatabase;

/**
 * Request rhythm title dialog. for instance when we want to save rhythm without a name.
 * Or clone a rhythm from edit/view activity
 *
 * Created by Andrey Samsonov on 11.03.2015.
 */
public class RequestRhythmTitleDialogFragment extends DialogFragment {

    public static String TAG = "RequestRhythmTitleDialogFragment";

    public static int MODE_NEW      = 3;
    public static int MODE_EDIT     = 1;
    public static int MODE_CLONE    = 2;
    public static int MODE_VIEW     = 0; //read-only

    public static String DIALOG_ARG_MODE        = "DIALOG_ARG_MODE";
    public static String DIALOG_ARG_RHYTHM_ID   = "DIALOG_ARG_RHYTHM_ID";

    private OnRequestRhythmTitleListener mCallback; //calling fragment

    public interface OnRequestRhythmTitleListener {
        public void onRequestRhythmTitleCommit(String newTitle);
        public void onRequestRhythmCloneCommit(String newTitle, long newId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mCallback = (OnRequestRhythmTitleListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnRequestRhythmTitleListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int mode = getArguments().getInt(DIALOG_ARG_MODE);
        final long rhythmId = getArguments().getLong(DIALOG_ARG_RHYTHM_ID);

        final PercussionDatabase helper = new PercussionDatabase(getActivity());
        final RhythmInfo rhythmInfo = helper.getRhythmInfoById(rhythmId);

        Log.d(TAG, "onCreateDialog. rhythmId:"+rhythmId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (mode == MODE_EDIT) {
            builder.setTitle(R.string.rhythm_title_request);
        } else if (mode == MODE_NEW) {
            builder.setTitle(R.string.new_rhythm_title_request);
        } else if (mode == MODE_CLONE) {
            builder.setTitle(R.string.new_rhythm_title_request);
        } else if (mode == MODE_VIEW) {
            builder.setTitle(R.string.rhythm_title_request);
        } else
            throw new UnsupportedOperationException("Request Title mode=" + mode + " is unknown or not supported.");

        builder.setCancelable(true);

        final EditText input = new EditText(getActivity());
        input.setSingleLine();
        input.setEnabled(mode != MODE_VIEW);

        if (rhythmInfo != null)
            input.setText(rhythmInfo.getTitle());

        builder.setView(input);

        if (mode == MODE_NEW || mode == MODE_EDIT)
            builder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();
                    mCallback.onRequestRhythmTitleCommit(value);
                }
            });
        else if (mode == MODE_CLONE) {
            builder.setPositiveButton(R.string.btn_clone, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();
                    long newID = helper.cloneRhythm(rhythmInfo);
                    Log.d(TAG, "Rhythm ID [" + rhythmId + "] is cloned. New ID [" + newID + "]");
                    mCallback.onRequestRhythmCloneCommit(value, newID);
                }
            });
        }

        builder.setNegativeButton(R.string.btn_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        getDialog().cancel();
                    }
                });

        return builder.create();
    }
}
