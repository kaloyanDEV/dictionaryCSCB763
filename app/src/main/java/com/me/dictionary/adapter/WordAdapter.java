package com.me.dictionary.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.me.dictionary.model.Word;

import java.util.ArrayList;

import dictionary.me.com.dictionary.R;


public class WordAdapter extends ArrayAdapter<Word> {


    public WordAdapter(Context context, ArrayList<Word> users) {
        super(context, 0, users);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Word word = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.translation, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.word_en);
        TextView tvHome = (TextView) convertView.findViewById(R.id.word_bg);
        // Populate the data into the template view using the data object
        tvName.setText(word.en);
        tvHome.setText(word.bg);
        // Return the completed view to render on screen
        return convertView;
    }

}
