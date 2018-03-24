package com.mohamed.movie_app_s2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mohamed on 05/10/17.
 */

public class Trailer implements Parcelable {
    private String mKey;
    private String mName;


    public Trailer(String mKey, String mName) {
        this.mKey = mKey;
        this.mName = mName;
    }

    protected Trailer(Parcel in) {
        mKey = in.readString();
        mName = in.readString();
    }

    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mKey);
        dest.writeString(mName);
    }

    public String getmName() {
        return mName;
    }

    public String getmKey() {
        return mKey;
    }
}
