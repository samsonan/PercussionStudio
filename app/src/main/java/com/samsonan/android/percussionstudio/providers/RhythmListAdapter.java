package com.samsonan.android.percussionstudio.providers;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.samsonan.android.percussionstudio.R;

/**
 * Concrete implementation of section cursor adpater

 * Created by Andrey Samsonov on 12.03.2015.
 */
public class RhythmListAdapter extends SectionCursorAdapter {

    public RhythmListAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    protected Object getSectionFromCursor(Cursor cursor) {
        int idx =  cursor.getColumnIndex(PercussionDatabase.RhythmTable.COLUMN_NAME_CATEGORY);
        String category = cursor.getString(idx);
        if (category == null || category.trim().length()==0)
            category = getContext().getResources().getString(R.string.no_category);

        return category;
    }

    @Override
    protected View newSectionView(Context context, Object item, ViewGroup parent) {
        return getLayoutInflater().inflate(R.layout.rhythm_list_section, parent, false);
    }

    @Override
    protected void bindSectionView(View convertView, Context context, int position, Object item) {
        ((TextView) convertView).setText((String) item);
    }

    @Override
    protected View newItemView(Context context, Cursor cursor, ViewGroup parent) {
        return getLayoutInflater().inflate(android.R.layout.simple_list_item_activated_1, parent, false);
    }

    @Override
    protected void bindItemView(View convertView, Context context, Cursor cursor) {

        int idx =  cursor.getColumnIndex(PercussionDatabase.RhythmTable.COLUMN_NAME_TITLE);
        ((TextView) convertView).setText(cursor.getString(idx));
    }

}
