package com.samsonan.android.percussionstudio.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.samsonan.android.percussionstudio.R;
import com.samsonan.android.percussionstudio.entities.MeasureTypes;
import com.samsonan.android.percussionstudio.providers.PercussionDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * rhythm info view/edit dialog
 * Created by Andrey Samsonov on 11.03.2015.
 */
public class RhythmInfoDialogFragment extends DialogFragment{

    public static String TAG = "RhythmInfoDialogFragment";

    public static int MODE_VIEW = 0; //read-only
    public static int MODE_EDIT = 1;

    public static String DIALOG_ARG_MODE = "DIALOG_ARG_MODE";
    public static String DIALOG_ARG_MEASURE = "DIALOG_ARG_MEASURE";
    public static String DIALOG_ARG_CATEGORY = "DIALOG_ARG_CATEGORY";
    public static String DIALOG_ARG_TITLE = "DIALOG_ARG_TITLE";
    public static String DIALOG_ARG_DESCRIPTION = "DIALOG_ARG_DESCRIPTION";


    private OnRequestRhythmInfoListener mCallback; //calling fragment


    public interface OnRequestRhythmInfoListener {
        public void onRequestRhythmInfoCommit(String newTitle, String newDescription, MeasureTypes newMeasure, String newCategory);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mCallback = (OnRequestRhythmInfoListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnRequestRhythmInfoListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int mode = getArguments().getInt(DIALOG_ARG_MODE);

        String title = getArguments().getString(DIALOG_ARG_TITLE);
        String description = getArguments().getString(DIALOG_ARG_DESCRIPTION);
        String category = getArguments().getString(DIALOG_ARG_CATEGORY);
        String measure = getArguments().getString(DIALOG_ARG_MEASURE);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.rhythm_info_title));

        View rootView = inflater.inflate(R.layout.rhythm_setting, null);

        final EditText rhythmTitleEdit = (EditText) rootView.findViewById(R.id.rhythm_title);
        final EditText rhythmDescriptionText = (EditText) rootView.findViewById(R.id.rhythm_description);

        rhythmTitleEdit.setText(title);
        rhythmDescriptionText.setText(description);

        final List<String> categoryList = new ArrayList<>();

        PercussionDatabase databaseHelper = new PercussionDatabase(getActivity());
        Cursor cursor = databaseHelper.getCategories();

        int categoryNameIdx = cursor.getColumnIndex(PercussionDatabase.RhythmTable.COLUMN_NAME_CATEGORY);
        while (cursor.moveToNext()) {
            String strCate = cursor.getString(categoryNameIdx);
            if (strCate != null && strCate.trim().length()>0)
                categoryList.add(cursor.getString(categoryNameIdx));
        }
        if (category != null && category.trim().length()>0 && !categoryList.contains(category))
            categoryList.add(category);

        int categoryIdx = categoryList.indexOf(category);

        final Spinner categorySpinner = (Spinner) rootView.findViewById(R.id.rhythm_category);

        /**
         * We cannot use cursor adapter because we need to change the list of values in the code here
         */
        final ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categoryList);

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setSelection(categoryIdx);

        final Spinner measureSpinner = (Spinner) rootView.findViewById(R.id.rhythm_measure);

        ArrayAdapter<MeasureTypes> measureAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                        MeasureTypes.values());

        int measureIdx = measureAdapter.getPosition(MeasureTypes.valueOf("MEASURE_"+measure.replace("/","_".replace(" ",""))));

        measureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measureSpinner.setAdapter(measureAdapter);
        measureSpinner.setSelection(measureIdx);

        Log.d(TAG, "populating RhythmInfoDialogFragment spinners. " +
                "category:"+category+" ("+categoryIdx+"), measure:"+measure+" ("+measureIdx+")");

        ImageButton categoryBtn = (ImageButton) rootView.findViewById(R.id.add_category_btn);
        categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                final EditText categoryText = new EditText(getActivity());
                builder.setView(categoryText)
                        .setPositiveButton(R.string.btn_confirm,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        if (categoryText.getText().toString().trim().length() > 0) {
                                            categoryList.add(categoryText.getText().toString());
                                            categoryAdapter.notifyDataSetChanged();
                                            categorySpinner.setSelection(categoryList.size() - 1);
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.btn_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //do nothing
                                    }
                                });

                builder.show();
            }
        });

        if (mode == MODE_VIEW) {
            measureSpinner.setEnabled(false);
            categorySpinner.setEnabled(false);
            categoryBtn.setVisibility(View.GONE);
            rhythmTitleEdit.setEnabled(false);
            rhythmDescriptionText.setEnabled(false);
        }

        if (mode == MODE_EDIT)
            builder.setNegativeButton(R.string.btn_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                            getDialog().cancel();
                        }
                    });
            builder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mCallback.onRequestRhythmInfoCommit(rhythmTitleEdit.getText().toString(),
                            rhythmDescriptionText.getText().toString(),
                            (MeasureTypes) measureSpinner.getSelectedItem(),
                            categorySpinner.getSelectedItem()!=null?categorySpinner.getSelectedItem().toString():"");
                }
            });

        builder.setView(rootView);
        return builder.create();
    }
}
