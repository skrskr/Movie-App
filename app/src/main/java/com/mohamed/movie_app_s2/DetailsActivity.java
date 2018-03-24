package com.mohamed.movie_app_s2;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class DetailsActivity extends AppCompatActivity implements TrailerAdapter.OnItemClick {
    ImageView mPosterImageView, mAddFavoriteImagView;
    TextView mTitleTextView, mReleaseDateTextView, mOverviewTextView, mVoteAverageTextView;
    public static final int REVIEW_LOADER_ID = 105;
    public static final int TRAILER_LOADER_ID = 110;
    public static final int CURSOR_LOADER_ID = 115;

    String movieId = "";
    private ReviewAdapter mReviewAdapter;
    private TrailerAdapter mTrailerAdapter;
    private RecyclerView mReviewRecyclerView, mTrailerRecyclerView;
    private ArrayList<Review> mReviewArrayList = new ArrayList<>();
    private ArrayList<Trailer> mTrailerArrayList = new ArrayList<>();

    private boolean isAdded = false;
    private int id;

    LinearLayoutManager linearLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mPosterImageView = (ImageView) findViewById(R.id.iv_detail_poster);
        mAddFavoriteImagView = (ImageView) findViewById(R.id.im_favorite);
        mTitleTextView = (TextView) findViewById(R.id.tv_title);
        mOverviewTextView = (TextView) findViewById(R.id.tv_overview);
        mReleaseDateTextView = (TextView) findViewById(R.id.tv_release_date);
        mVoteAverageTextView = (TextView) findViewById(R.id.tv_vote_average);
        mReviewRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        mTrailerRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);


        mReviewRecyclerView.setHasFixedSize(true);
        mTrailerRecyclerView.setHasFixedSize(true);
        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true));
        linearLayoutManager = new LinearLayoutManager(this);
        mReviewRecyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        final Movie m = intent.getExtras().getParcelable("movie");
        Picasso.with(this).load(m.getmPosterPath()).into(mPosterImageView);
        mTitleTextView.setText(m.getmTitle());
        mReleaseDateTextView.setText(m.getmDateRelease());
        mOverviewTextView.setText(m.getmOverview());
        mVoteAverageTextView.setText(m.getmVoteAverage() + "");
        movieId = m.getmId();
        if (savedInstanceState == null) {
            if (isOnline()) {
                getLoaderManager().initLoader(REVIEW_LOADER_ID, null, reviewLoader);
                getLoaderManager().initLoader(TRAILER_LOADER_ID, null, trailerLoader);
                getLoaderManager().initLoader(CURSOR_LOADER_ID, null, movieLoader);
            } else
                Toast.makeText(this, "Please Check Your Connection", Toast.LENGTH_SHORT).show();
        } else {
            mReviewArrayList = savedInstanceState.getParcelableArrayList("reviews");
            mTrailerArrayList = savedInstanceState.getParcelableArrayList("trailers");

            mTrailerAdapter = new TrailerAdapter(mTrailerArrayList, DetailsActivity.this, DetailsActivity.this);
            mTrailerRecyclerView.setAdapter(mTrailerAdapter);

            mReviewAdapter = new ReviewAdapter(DetailsActivity.this, mReviewArrayList);
            mReviewRecyclerView.setAdapter(mReviewAdapter);
        }

        mAddFavoriteImagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAdded) {
                    Uri uri = Uri.parse(MovieContract.MovieEntry.CONTENT_URI + "/" + id);
                    getContentResolver().delete(uri, null, null);
                    Toast.makeText(DetailsActivity.this, "Movie deleted Successfully", Toast.LENGTH_SHORT).show();
                    mAddFavoriteImagView.setImageResource(R.mipmap.ic_add);
                    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, movieLoader);
                    isAdded = false;
                } else {
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieEntry.COLUMN_ID, m.getmId());
                    values.put(MovieContract.MovieEntry.COLUMN_TITLE, m.getmTitle());

                    values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, m.getmPosterPath());

                    values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, m.getmOverview());
                    values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, m.getmVoteAverage());
                    values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, m.getmDateRelease());
                    getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
                    Toast.makeText(DetailsActivity.this, "Movie added Successfully", Toast.LENGTH_SHORT).show();
                    mAddFavoriteImagView.setImageResource(R.mipmap.ic_remove);
                    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, movieLoader);
                    isAdded = true;
                }

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("reviews", mReviewArrayList);
        outState.putParcelableArrayList("trailers", mTrailerArrayList);

    }



    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private LoaderManager.LoaderCallbacks<Cursor> movieLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            return new CursorLoader(DetailsActivity.this, uri, null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if (data != null) {
                int indexMovieId = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID);
                int indexId = data.getColumnIndex(MovieContract.MovieEntry._ID);
                while (data.moveToNext()) {
                    if (movieId.equals(data.getString(indexMovieId))) {
                        id = data.getInt(indexId);

                        isAdded = true;
                        mAddFavoriteImagView.setImageResource(R.mipmap.ic_remove);
                        break;
                    }
                }
                data.close();
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<ArrayList<Review>> reviewLoader = new LoaderManager.LoaderCallbacks<ArrayList<Review>>() {
        @Override
        public Loader<ArrayList<Review>> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<ArrayList<Review>>(DetailsActivity.this) {
                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Override
                public ArrayList<Review> loadInBackground() {
                    final String BASE_MOVIE_URL = "https://api.themoviedb.org/3/movie/" + movieId + "/reviews";
                    final String API_KEY = "api_key";
                    Uri uri = Uri.parse(BASE_MOVIE_URL).buildUpon()
                            .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                            .build();
                    try {
                        URL url = new URL(uri.toString());
                        HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("GET");
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuffer buffer = new StringBuffer();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            buffer.append(line);
                            buffer.append("\n");
                        }
                        String jsonResult = buffer.toString();
                        if (httpURLConnection != null)
                            httpURLConnection.disconnect();
                        if (inputStream != null)
                            inputStream.close();
                        if (bufferedReader != null)
                            bufferedReader.close();

                        return (ArrayList<Review>) getReviews(jsonResult);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                private List<Review> getReviews(String jsonResult) throws JSONException {

                    JSONObject movieObject = new JSONObject(jsonResult);
                    List<Review> reviews = new ArrayList<>();

                    JSONArray reviewsArray = movieObject.getJSONArray("results");

                    for (int i = 0; i < reviewsArray.length(); i++) {
                        JSONObject jsonObject = reviewsArray.getJSONObject(i);
                        String author = jsonObject.getString("author");
                        String content = jsonObject.getString("content");
                        reviews.add(new Review(author, content));
                    }
                    return reviews;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Review>> loader, ArrayList<Review> data) {
            if (data == null)
                Toast.makeText(DetailsActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
            else {
                mReviewArrayList = data;
                mReviewAdapter = new ReviewAdapter(DetailsActivity.this, mReviewArrayList);
                mReviewRecyclerView.setAdapter(mReviewAdapter);
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Review>> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<ArrayList<Trailer>> trailerLoader = new LoaderManager.LoaderCallbacks<ArrayList<Trailer>>() {
        @Override
        public Loader<ArrayList<Trailer>> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<ArrayList<Trailer>>(DetailsActivity.this) {
                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Override
                public ArrayList<Trailer> loadInBackground() {
                    final String BASE_MOVIE_URL = "https://api.themoviedb.org/3/movie/" + movieId + "/videos";
                    final String API_KEY = "api_key";
                    Uri uri = Uri.parse(BASE_MOVIE_URL).buildUpon()
                            .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                            .build();
                    try {
                        URL url = new URL(uri.toString());
                        HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("GET");
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuffer buffer = new StringBuffer();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            buffer.append(line);
                            buffer.append("\n");
                        }
                        String jsonResult = buffer.toString();
                        if (httpURLConnection != null)
                            httpURLConnection.disconnect();
                        if (inputStream != null)
                            inputStream.close();
                        if (bufferedReader != null)
                            bufferedReader.close();

                        return (ArrayList<Trailer>) getReviews(jsonResult);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                private List<Trailer> getReviews(String jsonResult) throws JSONException {

                    JSONObject movieObject = new JSONObject(jsonResult);
                    List<Trailer> trailers = new ArrayList<>();

                    JSONArray reviewsArray = movieObject.getJSONArray("results");

                    for (int i = 0; i < reviewsArray.length(); i++) {
                        JSONObject jsonObject = reviewsArray.getJSONObject(i);
                        String key = jsonObject.getString("key");
                        String title = jsonObject.getString("name");
                        trailers.add(new Trailer(key, title));
                    }
                    return trailers;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Trailer>> loader, ArrayList<Trailer> data) {
            if (data == null)
                Toast.makeText(DetailsActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
            else {
                mTrailerArrayList = data;
                mTrailerAdapter = new TrailerAdapter(mTrailerArrayList, DetailsActivity.this, DetailsActivity.this);
                mTrailerRecyclerView.setAdapter(mTrailerAdapter);
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Trailer>> loader) {

        }
    };

    @Override
    public void setOnItemClick(int position) {
        Trailer t = mTrailerArrayList.get(position);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.youtube.com/watch?v=" + t.getmKey()));
        startActivity(intent);
    }

}
