package com.me.dictionary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.me.dictionary.adapter.WordAdapter;
import com.me.dictionary.db.DictionaryDbHelper;
import com.me.dictionary.db.DictionarySchema;
import com.me.dictionary.model.Word;
import com.me.dictionary.widgets.EndlessScrollListener;

import java.util.ArrayList;
import java.util.Arrays;

import dictionary.me.com.dictionary.R;

/**
 * lazy loading all words from database
 */
public class ListActivity extends AppCompatActivity {

    private static String ITEMS = "items";

    private SQLiteDatabase db;
    /**
     * needed so we have reference to model data
     */
    private ArrayList<Word> viewModel = new ArrayList<>();
    private ArrayAdapter<Word> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ListView lvItems = findViewById(R.id.list);


        final DictionaryDbHelper mDbHelper = new DictionaryDbHelper(this);
        db = mDbHelper.getReadableDatabase();


        if (savedInstanceState == null) {

            //System.out.println("NOOOOOOOOOOOOOOOT: ");

            Cursor cursor = db.rawQuery("SELECT * " +
                    //DictionarySchema.WordTranslation.COLUMN_NAME_BG +
                    " FROM " +
                    DictionarySchema.WordTranslation.TABLE_NAME +
                    " LIMIT ? OFFSET ?", new String[]{"5", "0"});


            viewModel.addAll(
                    new ArrayList<>(Arrays.asList(cursorToArray(cursor)))
            );
        } else {

            //System.out.println("SAAAAAAAAAAAAAAAAVED: ");

            viewModel = savedInstanceState.getParcelableArrayList(ITEMS);
        }


        adapter = new WordAdapter(this, viewModel);


        lvItems.setAdapter(adapter);


        // Attach the listener to the AdapterView onCreate
        lvItems.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                System.out.println("PAGE: " + page + " TOTAL ITEMS COUNT: " + adapter.getCount());

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

        Cursor cursor = db.rawQuery("SELECT * " +
                //DictionarySchema.WordTranslation.COLUMN_NAME_BG +
                " FROM " +
                DictionarySchema.WordTranslation.TABLE_NAME +
                " LIMIT ? OFFSET ?", new String[]{"5", String.valueOf(adapter.getCount())});

        Word[] buff = cursorToArray(cursor);

        for (int i = 0; i < buff.length; i++) {
            adapter.add(buff[i]);
        }

    }


    /**
     * construct array from cursor.
     *
     * @param cursor
     * @return
     */
    private Word[] cursorToArray(Cursor cursor) {
        Word id[] = new Word[cursor.getCount()];
        //System.out.println("LEEEEEEEEEEEENGTH " + id.length);
        int i = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
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


    @Override
    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        //System.out.println("ITEAAAMS: " + viewModel.size());

        savedState.putParcelableArrayList(ITEMS, viewModel);

    }

}
