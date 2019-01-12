package com.me.dictionary.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.me.dictionary.model.Word;

/**
 * not used
 */
@Deprecated
public class WordAdapter extends ArrayAdapter<Word> {

    private final int layoutResId;

    public WordAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
        this.layoutResId = layoutResId;
    }


}
