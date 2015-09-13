/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;

import com.w3bshark.monolith.activities.MainActivity;
import com.w3bshark.monolith.data.MovieContract.MovieEntry;
import com.w3bshark.monolith.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieLoader extends AsyncTaskLoader<ArrayList<Movie>> {

    private static final String TAG = "MovieLoader";
    private Context mContext;
    private ArrayList<Movie> mMovies;
    private Uri mMoviesUri;

    // For the movies view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] MOVIES_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_DESCR,
            MovieEntry.COLUMN_IMAGE_CODE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_POPULARITY,
            MovieEntry.COLUMN_VOTE_AVG,
            MovieEntry.COLUMN_FAVORITE
    };

    // These indices are tied to MOVIES_COLUMNS. If MOVIES_COLUMNS changes, these must change.
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_TITLE = 2;
    public static final int COL_MOVIE_DESCR = 3;
    public static final int COL_MOVIE_IMAGE_CODE = 4;
    public static final int COL_MOVIE_RELEASE_DATE = 5;
    public static final int COL_MOVIE_POPULARITY = 6;
    public static final int COL_MOVIE_VOTE_AVG = 7;
    public static final int COL_MOVIE_FAVORITE = 8;

    public MovieLoader(Context context, Uri moviesUri) {
        // Loaders may be used across multiple Activities (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(context);
        mContext = context;
        mMoviesUri = moviesUri;
    }

    @Override
    public ArrayList<Movie> loadInBackground() {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String defValPref = "";
        String userSortPref = settings.getString(MainActivity.PREF_SORT, defValPref);
        String sortOrder;
        if (userSortPref.equals(MainActivity.SortType.HighestRated.getSortType())) {
            sortOrder = MovieEntry.COLUMN_VOTE_AVG + " DESC";
        } else {
            sortOrder = MovieEntry.COLUMN_POPULARITY + " DESC";
        }

        Cursor cursor = getContext().getContentResolver().query(
                MovieEntry.CONTENT_URI, MOVIES_COLUMNS, null, null, sortOrder);

        if (mMovies == null) {
            mMovies = new ArrayList<>();
        }

        ArrayList<Movie> entries = new ArrayList<>(mMovies.size());
        if (cursor.moveToFirst()) {
            do {
                Movie m = new Movie();
                m.setMovieId(cursor.getString(COL_MOVIE_ID));
                m.setTitle(cursor.getString(COL_MOVIE_TITLE));
                m.setDescription(cursor.getString(COL_MOVIE_DESCR));
                m.setImageCode(cursor.getString(COL_MOVIE_IMAGE_CODE));
                m.setReleaseDate(cursor.getString(COL_MOVIE_RELEASE_DATE));
                m.setPopularity(cursor.getLong(COL_MOVIE_POPULARITY));
                m.setVoteAverage(cursor.getDouble(COL_MOVIE_VOTE_AVG));
                m.setFavorite(cursor.getString(COL_MOVIE_FAVORITE));
                mMovies.add(m);
                entries.add(m);
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return entries;
    }

    /**
     * Called when there is new data to deliver to the client. The superclass will
     * deliver it to the registered listener (i.e. the LoaderManager), which will
     * forward the results to the client through a call to onLoadFinished.
     */
    @Override
    public void deliverResult(ArrayList<Movie> movies) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            // This can happen when the Loader is reset while an asynchronous query
            // is working in the background. That is, when the background thread
            // finishes its work and attempts to deliver the results to the client,
            // it will see here that the Loader has been reset and discard any
            // resources associated with the new data as necessary.
            if (movies != null) {
                releaseResources(movies);
                return;
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        ArrayList<Movie> oldMovies = mMovies;
        mMovies = movies;

        if (isStarted()) {
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(movies);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldMovies != null && oldMovies != movies) {
            releaseResources(oldMovies);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mMovies != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mMovies);
        }

        if (takeContentChanged()) {
            forceLoad();
        } else if (mMovies == null) {
            // force a load to attempt to pull data into mMovies
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // The Loader has been put in a stopped state, so we should attempt to
        // cancel the current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is; Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        // Ensure the loader is stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'movies'.
        if (mMovies != null) {
            releaseResources(mMovies);
            mMovies = null;
        }
    }

    @Override
    public void onCanceled(ArrayList<Movie> movies) {
        super.onCanceled(movies);
        releaseResources(movies);
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources(List<Movie> movies) {
        // For a simple List, there is nothing to do. For something like a Cursor,
        // we would close it in this method. All resources associated with the
        // Loader should be released here.
    }
}