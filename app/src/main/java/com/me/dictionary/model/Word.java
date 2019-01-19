package com.me.dictionary.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * implements parcelable so we can restore after configuration change
 */
public class Word implements Parcelable {

    public String en;
    public String bg;

    public Word(String en, String bg) {
        this.en = en;
        this.bg = bg;
    }

    public Word() {

    }

    protected Word(Parcel in) {
        en = in.readString();
        bg = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(en);
        dest.writeString(bg);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Word> CREATOR = new Creator<Word>() {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };
}
