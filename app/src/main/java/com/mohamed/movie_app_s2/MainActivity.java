package com.mohamed.movie_app_s2;


import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements MoviesAdapter.OnItemClick, LoaderManager.LoaderCallbacks<ArrayList<Movie>> {
    RecyclerView mMoviesRecyclerView;
    MoviesAdapter adapter = null;
    private ArrayList<Movie> mMoviesList = new ArrayList<>();
    String type;
    public static final int MOVIE_LOADER_ID = 101;
    public static final int CURSOR_LOADER_ID = 102;
    GridLayoutManager gridLayoutManager;
    Parcelable listState;
    public static final String LIST_STATE_KEY = "recycler_state";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mMoviesRecyclerView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(this,3);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", MODE_PRIVATE);
        type = sharedPreferences.getString("type", "top_rated");
        if (savedInstanceState == null) {
            if (isOnline()) {
                if( !(type.equals("favorite")))
                    getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
            } else
                Toast.makeText(this, "Please Check Your Connection", Toast.LENGTH_SHORT).show();
        } else {
            mMoviesList = savedInstanceState.getParcelableArrayList("movies");
            adapter = new MoviesAdapter(mMoviesList, this, this);
            mMoviesRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", mMoviesList);
        listState = gridLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, listState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null)
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listState != null) {
            gridLayoutManager.onRestoreInstanceState(listState);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onItemClick(int position) {
        Movie m = mMoviesList.get(position);
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("movie", m);
        startActivity(intent);

    }

    private LoaderManager.LoaderCallbacks<Cursor> mCursorLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            return new CursorLoader(MainActivity.this, uri, null,
                    null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data == null) {
                Toast.makeText(MainActivity.this, "There is no favorite movies found", Toast.LENGTH_SHORT).show();
                mMoviesList = new ArrayList<>();
                adapter = new MoviesAdapter(mMoviesList, MainActivity.this, MainActivity.this);
                mMoviesRecyclerView.setAdapter(adapter);
            } else {
                mMoviesList.clear();
                int indexId = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID);
                int indexPath = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
                int indexOverview = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
                int indexTitle = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
                int indexVoteAvg = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
                int indexReleaseDate = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);

                while (data.moveToNext()) {
                    String id = data.getString(indexId);
                    String path = data.getString(indexPath);
                    String overview = data.getString(indexOverview);
                    String title = data.getString(indexTitle);
                    String releaseDate = data.getString(indexReleaseDate);
                    double voteAvg = data.getDouble(indexVoteAvg);
                    mMoviesList.add(new Movie(id, title, path, voteAvg, overview, releaseDate));
                }
                data.close();
                adapter = new MoviesAdapter(mMoviesList, MainActivity.this, MainActivity.this);
                mMoviesRecyclerView.setAdapter(adapter);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", MODE_PRIVATE);
        type = sharedPreferences.getString("type", "top_rated");

        if(type.equals("favorite"))
        {
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, mCursorLoader);
            adapter = new MoviesAdapter(mMoviesList, MainActivity.this, MainActivity.this);
            mMoviesRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("type", type);
        editor.commit();
    }

    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<ArrayList<Movie>>(this) {

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public ArrayList<Movie> loadInBackground() {
                final String BASE_MOVIE_URL = "https://api.themoviedb.org/3/movie/" + type;
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

                    return (ArrayList<Movie>) getMovies(jsonResult);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            private List<Movie> getMovies(String jsonResult) throws JSONException {

                JSONObject movieObject = new JSONObject(jsonResult);
                List<Movie> movies = new ArrayList<>();

                JSONArray movieArray = movieObject.getJSONArray("results");
                final String imageUrl = "http://image.tmdb.org/t/p/w185";
                for (int i = 0; i < movieArray.length(); i++) {
                    JSONObject jsonObject = movieArray.getJSONObject(i);
                    int id = jsonObject.getInt("id");
                    String mPath = jsonObject.getString("poster_path");
                    mPath = imageUrl + mPath;
                    String title = jsonObject.getString("title");
                    String release_date = jsonObject.getString("release_date");
                    String overview = jsonObject.getString("overview");
                    double vote_average = jsonObject.getDouble("vote_average");
                    Movie m = new Movie(id + "", title, mPath, vote_average, overview, release_date);
                    movies.add(m);

                }
                return movies;
            }
        };


    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {
        if (data == null)
            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();
        else {
            mMoviesList = data;
            adapter = new MoviesAdapter(mMoviesList, this, this);
            mMoviesRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_top_rated) {
            type = "top_rated";
            if (isOnline()) {
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
                adapter = new MoviesAdapter(mMoviesList, this, this);
                mMoviesRecyclerView.setAdapter(adapter);
            } else
                Toast.makeText(this, "Please Check Your Connection", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_most_popular) {
            type = "popular";
            if (isOnline()) {
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
                adapter = new MoviesAdapter(mMoviesList, this, this);
                mMoviesRecyclerView.setAdapter(adapter);
            } else
                Toast.makeText(this, "Please Check Your Connection", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_favorite) {
            type = "favorite";
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, mCursorLoader);
            adapter = new MoviesAdapter(mMoviesList, MainActivity.this, MainActivity.this);
            mMoviesRecyclerView.setAdapter(adapter);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
