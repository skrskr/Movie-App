package com.mohamed.movie_app_s2;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by mohamed on 30/09/17.
 */

public class MovieContract {
    public static final String AUTHORITY = "com.example.mohamed.movieapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String FAVORITE_PATH = "favorites";

    public static class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(FAVORITE_PATH).build();
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_ID = "movie_id";
        public static final String COLUMN_TITLE = "movie_title";
        public static final String COLUMN_POSTER_PATH = "movie_poster_path";
        public static final String COLUMN_VOTE_AVERAGE = "movie_vote_average";
        public static final String COLUMN_OVERVIEW = "movie_overview";
        public static final String COLUMN_RELEASE_DATE = "movie_release_date";
    }
}
