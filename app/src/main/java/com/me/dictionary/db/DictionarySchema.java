package com.me.dictionary.db;

import android.provider.BaseColumns;

/**
 * representation of schema
 */
public final class DictionarySchema {

    private DictionarySchema() {
    }

    /**
     * represents table with different meanings on various languages
     */
    public static class WordTranslation implements BaseColumns {
        public static final String TABLE_NAME = "word";
        public static final String COLUMN_NAME_EN = "en";
        public static final String COLUMN_NAME_BG = "bg";
    }


    /**
     * note: primary key is english word
     */
    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WordTranslation.TABLE_NAME + " (" +
                    //WordTranslation._ID + " INTEGER PRIMARY KEY," +
                    WordTranslation.COLUMN_NAME_EN + " TEXT," +
                    WordTranslation.COLUMN_NAME_BG + " TEXT," +
                    "PRIMARY KEY(" + WordTranslation.COLUMN_NAME_EN + ") )";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + WordTranslation.TABLE_NAME;
}
