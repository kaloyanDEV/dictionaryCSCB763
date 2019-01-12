package com.me.dictionary;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.me.dictionary.db.DictionaryDbHelper;
import com.me.dictionary.db.DictionarySchema;
import dictionary.me.com.dictionary.R;

/**
 * provides functionality to insert or update new translation in local database
 */
public class InsertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        final Intent intent = getIntent();


        String previewText = "(" +
                intent.getStringExtra(DictionaryActivity.SOURCE_LANG).toUpperCase() +
                ") " +
                intent.getStringExtra(DictionaryActivity.WORD) +
                " > " +
                "(" +
                intent.getStringExtra(DictionaryActivity.TARGET_LANG).toUpperCase() +
                ") " +
                intent.getStringExtra(DictionaryActivity.TRANSLATION);


        TextView textView = findViewById(R.id.new_word_preview);
        textView.setText(previewText);


        final DictionaryDbHelper mDbHelper = new DictionaryDbHelper(this);

        final Button mInsertButton = (Button) findViewById(R.id.insert_button);

        mInsertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    // Gets the data repository in write mode
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();

                    // Filter results WHERE "title" = 'My Title'
                    String selection = DictionarySchema.WordTranslation.COLUMN_NAME_EN + " = ?";
                    String[] selectionArgs = {intent.getStringExtra(DictionaryActivity.WORD)};

                    // How you want the results sorted in the resulting Cursor
                    String sortOrder =
                            DictionarySchema.WordTranslation.COLUMN_NAME_EN + " DESC";


                    Cursor cursor = db.query(
                            DictionarySchema.WordTranslation.TABLE_NAME,   // The table to query
                            null,             // The array of columns to return (pass null to get all)
                            selection,              // The columns for the WHERE clause
                            selectionArgs,          // The values for the WHERE clause
                            null,                   // don't group the rows
                            null,                   // don't filter by row groups
                            sortOrder               // The sort order
                    );


                    if (cursor.getCount() == 0) {

                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(intent.getStringExtra(DictionaryActivity.SOURCE_LANG), intent.getStringExtra(DictionaryActivity.WORD));
                        values.put(intent.getStringExtra(DictionaryActivity.TARGET_LANG), intent.getStringExtra(DictionaryActivity.TRANSLATION));

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId = db.insert(DictionarySchema.WordTranslation.TABLE_NAME, null, values);

                        System.out.println("BLA " + newRowId);

                        Toast.makeText(InsertActivity.this, "Думата: \"" + intent.getStringExtra(DictionaryActivity.WORD) + "\" успешно запазена.",
                                Toast.LENGTH_LONG).show();


                    } else if (cursor.getCount() == 1) {

                        Toast.makeText(InsertActivity.this, "Думата: \"" + intent.getStringExtra(DictionaryActivity.WORD) + "\" успешно обновена.",
                                Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(InsertActivity.this, "Проблем със схемата!",
                                Toast.LENGTH_LONG).show();
                    }


                } catch (SQLiteException e) {
                    e.printStackTrace();
                    Toast.makeText(InsertActivity.this, "Има проблем с бозата данни!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
