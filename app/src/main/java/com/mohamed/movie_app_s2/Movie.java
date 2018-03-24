package com.mohamed.movie_app_s2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mohamed on 17/09/17.
 */

public class Movie implements Parcelable{


    private String mId;
    private String mTitle;
    private String mPosterPath;
    private double mVoteAverage;
    private String mOverview;
    private String mDateRelease;

    public Movie(String mId,String mTitle, String mPosterPath, double mVoteAverage, String mOverview, String mDateRelease) {
        this.mId = mId;
        this.mTitle = mTitle;
        this.mPosterPath = mPosterPath;
        this.mVoteAverage = mVoteAverage;
        this.mOverview = mOverview;
        this.mDateRelease = mDateRelease;
    }

    protected Movie(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mPosterPath = in.readString();
        mVoteAverage = in.readDouble();
        mOverview = in.readString();
        mDateRelease = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getmTitle() {
        return mTitle;
    }

    public String getmPosterPath() {
        return mPosterPath;
    }

    public double getmVoteAverage() {
        return mVoteAverage;
    }

    public String getmOverview() {
        return mOverview;
    }

    public String getmDateRelease() {
        return mDateRelease;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mPosterPath);
        dest.writeDouble(mVoteAverage);
        dest.writeString(mOverview);
        dest.writeString(mDateRelease);
    }
}
