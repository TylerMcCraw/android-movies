/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3bshark.monolith.R;
import com.w3bshark.monolith.Util;
import com.w3bshark.monolith.activities.DetailActivity;
import com.w3bshark.monolith.activities.MainActivity;
import com.w3bshark.monolith.data.MovieContract.MovieEntry;
import com.w3bshark.monolith.data.MovieLoader;
import com.w3bshark.monolith.model.Movie;
import com.w3bshark.monolith.rest.MoviesHandler;
import com.w3bshark.monolith.rest.TmdbRestClient;
import com.w3bshark.monolith.widget.MoviesAdapter;
import com.w3bshark.monolith.widget.PreCachingGridLayoutManager;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A custom fragment for displaying a grid/list of movies
 * This fragment is created within MainActivity
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<Movie>> {

    // Log tag to keep track of logs created against this class
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    // Our recycler view used to display movies as card views
    private RecyclerView mRecyclerView;
    // Adapter used to bind movie data to our recycler view
    private MoviesAdapter mMoviesAdapter;
    // Custom-built GridLayoutManager for pre-caching picasso fetching of movie poster images
    private PreCachingGridLayoutManager mLayoutManager;
    // Layout that allows users to swipe down the screen to refresh data anytime
    private SwipeRefreshLayout mSwipeRefreshLayout;
    // Keep track of the user scrolling (loading new movies)
    private boolean viewIsLoading = true;
    // Counters to keep track of displayed movies
    private int pastVisibleItems, visibleItemCount, totalItemCount, visiblePages;
    // Ever-growing list of movies displayed (binded from our adapter)
    private ArrayList<Movie> movies;
    // Movie parcelable key for saving instance state
    private static final String SAVED_MOVIES = "SAVED_MOVIES";
    // Counter key for saving instance state
    private static final String PAST_VISIBLE_ITEMS = "PAST_VISIBLE_ITEMS";
    // Counter key for saving instance state
    private static final String VISIBLE_ITEM_COUNT = "VISIBLE_ITEM_COUNT";
    // Counter key for saving instance state
    private static final String TOTAL_ITEM_COUNT = "TOTAL_ITEM_COUNT";
    // Counter key for saving instance state
    private static final String VISIBLE_PAGES = "VISIBLE_PAGES";

    private static final int MOVIES_LOADER = 0;

    public MainActivityFragment() {
    }

    /**
     * Handle creation of fragment view, fetch data via TmdbRestClient,
     * initialize our data adapter (RecylcerAdapter) and bind the data,
     * define our scroll listener, and bind the swipe refresh layout
     *
     * @param inflater           used for inflating the xml layout
     * @param container          view to inflate the xml layout into
     * @param savedInstanceState instance state of the application activity
     * @return main view displayed in the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Set up the xml layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
//        View mCoordinatorLayoutView = rootView.findViewById(R.id.main_coordinator_layout);

        // Improves performance if changes in content do not change
        // the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Set up our custom grid layout and define number of columns to display
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        int spanCount;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Display only 1 columns if phone; 2 columns if tablet
            spanCount = isTablet ? 2 : 1;
            mLayoutManager = new PreCachingGridLayoutManager(getActivity(), spanCount, GridLayoutManager.HORIZONTAL, false);
        } else {
            // Display only 2 columns if phone; 3 columns if tablet
            spanCount = isTablet ? 3 : 2;
            mLayoutManager = new PreCachingGridLayoutManager(getActivity(), spanCount, GridLayoutManager.VERTICAL, false);
        }
        mLayoutManager.setExtraLayoutSpace(Util.getScreenHeight(getActivity()));
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Attempt to restore movie data from savedInstanceState
        if (savedInstanceState != null) {
            pastVisibleItems = savedInstanceState.getInt(PAST_VISIBLE_ITEMS);
            visibleItemCount = savedInstanceState.getInt(VISIBLE_ITEM_COUNT);
            totalItemCount = savedInstanceState.getInt(TOTAL_ITEM_COUNT);
            visiblePages = savedInstanceState.getInt(VISIBLE_PAGES);
            movies = savedInstanceState.getParcelableArrayList(SAVED_MOVIES);
            if (mMoviesAdapter == null) {
                initializeAdapter();
            } else {
                mMoviesAdapter.notifyDataSetChanged();
            }
        }

        getLoaderManager().initLoader(MOVIES_LOADER, null, this);

        // Handle user continuously scrolling
        // Pages of new movies will be added in as user scrolls
        // TODO: Add inifinite scrolling back in, when we can better support it
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                visibleItemCount = mLayoutManager.getChildCount();
//                totalItemCount = mLayoutManager.getItemCount();
//                pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
//
//                if (viewIsLoading) {
//                    // We'll want to add in new movies before the user can hit a "wall"
//                    // This will allow users to continuously scroll without having to stop
//                    // and wait for the app to GET new movies
//                    if ((visibleItemCount + pastVisibleItems) >= (totalItemCount)) {
//                        viewIsLoading = false;
//
//                        String sortUrl;
//                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
//                        String defValPref = "";
//                        String userSortPref = settings.getString(MainActivity.PREF_SORT, defValPref);
//                        if (userSortPref.equals(MainActivity.SortType.HighestRated.getSortType())) {
//                            sortUrl = MoviesHandler.MOVIES_RATING_DESC;
//                        }
//                        else {
//                            sortUrl = MoviesHandler.MOVIES_POPULARITY_DESC;
//                        }
//                        String url = sortUrl
//                                .concat(MoviesHandler.MOVIES_ADDPAGE)
//                                .concat(Integer.toString(++visiblePages));
//                        getMovies(url, true, true);
//                    }
//                    // TMDB has a limit on pages that can be requested
//                    // This limit is 1000, so we should stop the user from loading more than this
//                    if (visiblePages >= 1000) {
//                        viewIsLoading = false;
//                    }
//                }
//            }
//        });

        // Handle user pull-down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                // Set visible pages back to 1, because we'll be reloading all pages
                // TODO: Add inifinite scrolling back in, when we can better support it
//                visiblePages = 1;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String defValPref = "";
                String userSortPref = settings.getString(MainActivity.PREF_SORT, defValPref);
                String getUrl;
                if (userSortPref.equals(MainActivity.SortType.HighestRated.getSortType())) {
                    getUrl = MoviesHandler.MOVIES_RATING_DESC;
                } else {
                    getUrl = MoviesHandler.MOVIES_POPULARITY_DESC;
                }
                getMovies(getUrl, false);
//                initializeAdapter();
                mRecyclerView.refreshDrawableState();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swiperefresh);
        //TODO: Set timeout on refresh in case we cannot connect to the TMDB API

        return rootView;
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }

    private void getMovies(String url, final Boolean setViewIsLoading) {
        // Fetch movie data using our rest client and bind the data
        TmdbRestClient.get(url, null, new MoviesHandler() {
            @Override
            //TODO: handle deprecation: org.apache.http.Header is deprecated in API level 22
            // https://github.com/loopj/android-async-http/issues/833
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int inserted = 0;
                if (this.getMoviesVector() != null && !this.getMoviesVector().isEmpty()) {
                    // Bulk insert fetched movies into DB
                    ContentValues[] cvArray = new ContentValues[getMoviesVector().size()];
                    getMoviesVector().toArray(cvArray);
                    inserted = getContext().getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);

                    Log.d(LOG_TAG, "MoviesHandler Complete. " + inserted + " Inserted");
                }

                // Now that we've fetched the latest movies, let's reload the data for the UI
                //  from our movies DB. If no new data was actually retrieved, don't attempt
                //  to restart the loader, because we might fall into an infinite loop that way.
                if (inserted > 0) {
                    restartLoader();
                }
                mSwipeRefreshLayout.setRefreshing(false);

                if (setViewIsLoading) {
                    viewIsLoading = true;
                }
            }

            //TODO: handle deprecation: org.apache.http.Header is deprecated in API level 22
            // https://github.com/loopj/android-async-http/issues/833
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * Set up our MoviesAdapter to bind the data to the RecyclerView and handle
     * user click of movie (CardView) in the RecyclerView
     */
    private void initializeAdapter() {
        // We must create the clicklistener here so that the adapter
        // can bind the data with the element that is clicked
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                // We must pull the most recent data for the selected movie from the database
                //  in case it has been updated to be a "favorite"
                String[] selectionArgs = new String[]{movies.get(itemPosition).getMovieId()};
                Cursor cursor = getContext().getContentResolver().query(
                        MovieEntry.CONTENT_URI,
                        MovieLoader.MOVIES_COLUMNS,
                        MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArgs,
                        null);
                Movie selectedMovie = null;
                if (cursor.moveToFirst()) {
                    selectedMovie = new Movie();
                    selectedMovie.setMovieId(cursor.getString(MovieLoader.COL_MOVIE_ID));
                    selectedMovie.setTitle(cursor.getString(MovieLoader.COL_MOVIE_TITLE));
                    selectedMovie.setDescription(cursor.getString(MovieLoader.COL_MOVIE_DESCR));
                    selectedMovie.setImageCode(cursor.getString(MovieLoader.COL_MOVIE_IMAGE_CODE));
                    selectedMovie.setReleaseDate(cursor.getString(MovieLoader.COL_MOVIE_RELEASE_DATE));
                    selectedMovie.setPopularity(cursor.getLong(MovieLoader.COL_MOVIE_POPULARITY));
                    selectedMovie.setVoteAverage(cursor.getDouble(MovieLoader.COL_MOVIE_VOTE_AVG));
                    selectedMovie.setFavorite(cursor.getString(MovieLoader.COL_MOVIE_FAVORITE));
                }
                cursor.close();

                // If, for whatever reason, we couldn't retrieve the movie from the DB,
                //  then, just use the movie from in-memory
                if (selectedMovie == null) {
                    selectedMovie = movies.get(itemPosition);
                }
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(DetailActivity.EXTRASCURRENTMOVIE, selectedMovie);
                startActivity(detailIntent);
            }
        };

        // Initialize the MoviesAdapter and bind movies
        mMoviesAdapter = new MoviesAdapter(getActivity(), movies, clickListener);
        mRecyclerView.setAdapter(mMoviesAdapter);
    }

    /**
     * Fetch movie data from TMDB that is sorted by "Most Popular"
     */
    public void sortByMostPopular() {
        // Set the user shared preference to MostPopular
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor preferenceEditor = settings.edit();
        preferenceEditor.putString(MainActivity.PREF_SORT, MainActivity.SortType.MostPopular.getSortType());
        preferenceEditor.apply();

        // There's no reason to attempt to fetch data while offline, so if app does not
        //  have connectivity, then just restart the loader so that it can sort the data from
        //  storage in the correct manner
        if (Util.isNetworkAvailable(getContext())) {
            // Fetch the movie data
            String getUrl = MoviesHandler.MOVIES_POPULARITY_DESC;
            getMovies(getUrl, false);
        } else {
            restartLoader();
        }
    }

    /**
     * Fetch movie data from TMDB that is sorted by "Highest Rated"
     */
    public void sortByHighestRated() {
        // Set the user shared preference to HighestRated
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor preferenceEditor = settings.edit();
        preferenceEditor.putString(MainActivity.PREF_SORT, MainActivity.SortType.HighestRated.getSortType());
        preferenceEditor.apply();

        // There's no reason to attempt to fetch data while offline, so if app does not
        //  have connectivity, then just restart the loader so that it can sort the data from
        //  storage in the correct manner
        if (Util.isNetworkAvailable(getContext())) {
            // Fetch the movie data
            String getUrl = MoviesHandler.MOVIES_RATING_DESC;
            getMovies(getUrl, false);
        } else {
            restartLoader();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (movies != null) {
            savedInstanceState.putParcelableArrayList(SAVED_MOVIES, movies);
        }
        savedInstanceState.putInt(PAST_VISIBLE_ITEMS, pastVisibleItems);
        savedInstanceState.putInt(VISIBLE_ITEM_COUNT, visibleItemCount);
        savedInstanceState.putInt(TOTAL_ITEM_COUNT, totalItemCount);
        savedInstanceState.putInt(VISIBLE_PAGES, visiblePages);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int i, Bundle bundle) {
        Uri moviesUri = MovieEntry.buildAllMoviesUri();
        return new MovieLoader(getActivity(), moviesUri);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {

        // If there were no movies in the database, attempt to fetch them while online
        if (data == null || data.size() == 0) {
            // Initially we'll start with 1 page of movies (paging is a TMDB term)
            // Then, we'll slowly roll in more and more pages as needed by user
            // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie
            // TODO: Add inifinite scrolling back in, when we can better support it
//            visiblePages = 1;
            // Find the user's preferred sort method and fetch the data in initializeData()
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            String defValPref = "";
            String userSortPref = settings.getString(MainActivity.PREF_SORT, defValPref);
            String getUrl;
            if (userSortPref.equals(MainActivity.SortType.HighestRated.getSortType())) {
                getUrl = MoviesHandler.MOVIES_RATING_DESC;
            } else {
                getUrl = MoviesHandler.MOVIES_POPULARITY_DESC;
            }
            getMovies(getUrl, false);

            // Otherwise, add the movies from the database
        } else {
            if (movies == null) {
                movies = new ArrayList<>();
            }
            movies.clear();
            movies.addAll(data);
            if (mMoviesAdapter == null) {
                initializeAdapter();
            } else {
                mMoviesAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
        if (movies != null) {
            this.movies.clear();
        }
        if (mMoviesAdapter != null) {
            mMoviesAdapter.notifyDataSetChanged();
        }
    }
}
