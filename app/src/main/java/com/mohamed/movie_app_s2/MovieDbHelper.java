package com.mohamed.movie_app_s2;

import android.content.Context;

import com.mohamed.movie_app_s2.MovieContract.MovieEntry;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by mohamed on 30/09/17.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "movie.db";


    final String SQL_CREATE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME +
            " ( " + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + MovieEntry.COLUMN_ID + " TEXT NOT NULL," + MovieEntry.COLUMN_TITLE + " TEXT NOT NULL," +
            MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL," + MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL," +
            MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL," + MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL);";

    final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME;


    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }
}
