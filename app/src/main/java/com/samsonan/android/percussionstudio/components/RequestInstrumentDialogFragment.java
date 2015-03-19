package com.samsonan.android.percussionstudio.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.samsonan.android.percussionstudio.entities.InstrumentFactory;
import com.samsonan.android.percussionstudio.R;

/**
 *
 * Created by Andrey Samsonov on 10.03.2015.
 */
public class RequestInstrumentDialogFragment extends DialogFragment {

    public static String TAG = "RequestInstrumentDialogFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.instrument_request));

        builder.setCancelable(true);

        final Spinner instrumentSelector = new Spinner(getActivity());

        ArrayAdapter<InstrumentFactory.Instrument> instrumentAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                        InstrumentFactory.Instrument.values());

        instrumentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        instrumentSelector.setAdapter(instrumentAdapter);

        builder.setView(instrumentSelector);

        builder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int instrPosition = instrumentSelector.getSelectedItemPosition();

                Log.d(TAG, "Ready to create new rhythm. instrument: " + InstrumentFactory.Instrument.values()[instrPosition]);
                Intent intent = new Intent(getActivity(), RhythmEditActivity.class);
                intent.putExtra(RhythmListFragment.INSTRUMENT_ID, instrPosition);
                startActivity(intent);
            }
        });

        builder.setNegativeButton(R.string.btn_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        return builder.create();
    }

}
