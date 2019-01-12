package com.me.dictionary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.me.dictionary.adapter.WordAdapter;
import com.me.dictionary.db.DictionaryDbHelper;
import com.me.dictionary.db.DictionarySchema;
import com.me.dictionary.model.Word;
import com.me.dictionary.widgets.EndlessScrollListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dictionary.me.com.dictionary.R;

public class ListActivity extends AppCompatActivity {


    private SQLiteDatabase db;
    private ArrayList<Word> viewModel = new ArrayList<>();
    private ArrayAdapter<Word> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ListView lvItems = (ListView) findViewById(R.id.list);


        final DictionaryDbHelper mDbHelper = new DictionaryDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * " +
                //DictionarySchema.WordTranslation.COLUMN_NAME_BG +
                " FROM " +
                DictionarySchema.WordTranslation.TABLE_NAME +
                " LIMIT ? OFFSET ?", new String[]{"5", "0"});


        //String[] items = {cursor.get};

        viewModel.addAll(
                new ArrayList<>(Arrays.asList(cursorToArray(cursor)))
        );

        /*
        adapter =
                new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1,
                        //viewModel.toArray(new String[0])
                        //cursorToArray(cursor)
                        viewModel
                );
        */


        adapter = new WordAdapter(this, viewModel);


        lvItems.setAdapter(adapter);


        // Attach the listener to the AdapterView onCreate
        lvItems.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page);
                // or loadNextDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

    }


    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyDataSetChanged()`

        System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKK " + offset);


        Cursor cursor = db.rawQuery("SELECT * " +
                //DictionarySchema.WordTranslation.COLUMN_NAME_BG +
                " FROM " +
                DictionarySchema.WordTranslation.TABLE_NAME +
                " LIMIT ? OFFSET ?", new String[]{"5", String.valueOf(offset)});


        //viewModel.addAll(
        // new ArrayList<>(Arrays.asList(cursorToArray(cursor)))
        //);

        //adapter.addAll(cursorToArray(cursor));

        Word[] buff = cursorToArray(cursor);

        for (int i = 0; i < buff.length; i++) {
            adapter.add(buff[i]);
        }


        //adapter.notifyDataSetChanged();

    }


    private Word[] cursorToArray(Cursor cursor) {
        Word id[] = new Word[cursor.getCount()];
        System.out.println("LEEEEEEEEEEEENGTH " + id.length);
        int i = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                //cursor.getString(cursor.getColumnIndex(DictionarySchema.WordTranslation.COLUMN_NAME_EN))
                Word word = new Word();
                word.en = cursor.getString(cursor.getColumnIndex(DictionarySchema.WordTranslation.COLUMN_NAME_EN));
                word.bg = cursor.getString(cursor.getColumnIndex(DictionarySchema.WordTranslation.COLUMN_NAME_BG));
                id[i] = word;
                i++;
            } while (cursor.moveToNext());
            cursor.close();
        }

        return id;
    }
}
