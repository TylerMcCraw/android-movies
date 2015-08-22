/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3bshark.monolith.R;
import com.w3bshark.monolith.Util;
import com.w3bshark.monolith.activities.DetailActivity;
import com.w3bshark.monolith.activities.MainActivity;
import com.w3bshark.monolith.model.Movie;
import com.w3bshark.monolith.rest.MoviesHandler;
import com.w3bshark.monolith.rest.TmdbRestClient;
import com.w3bshark.monolith.widget.PreCachingGridLayoutManager;
import com.w3bshark.monolith.widget.RecyclerAdapter;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A custom fragment for displaying a grid/list of movies
 * This fragment is created within MainActivity
 */
public class MainActivityFragment extends Fragment {

    // Our recycler view used to display movies as card views
    private RecyclerView mRecyclerView;
    // Adapter used to bind movie data to our recycler view
    private RecyclerAdapter mRecyclerAdapter;
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


    public MainActivityFragment() {
    }

    /**
     * Handle creation of fragment view, fetch data via TmdbRestClient,
     * initialize our data adapter (RecylcerAdapter) and bind the data,
     * define our scroll listener, and bind the swipe refresh layout
     * @param inflater used for inflating the xml layout
     * @param container view to inflate the xml layout into
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
        }
        else {
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
            if (mRecyclerAdapter == null) {
                initializeAdapter();
            } else {
                mRecyclerAdapter.notifyDataSetChanged();
            }
        }
        // If we couldn't retrieve movies from a saved instance state
        if (movies == null || movies.size() == 0) {
            // Find the user's preferred sort method and fetch the data in initializeData()
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            String defValPref = "";
            String userSortPref = settings.getString(MainActivity.PREF_SORT, defValPref);
            if (userSortPref.isEmpty() || userSortPref.equals(MainActivity.SortType.MostPopular.getSortType())) {
                initializeData(MainActivity.SortType.MostPopular);
            } else {
                initializeData(MainActivity.SortType.HighestRated);
            }

            // Initially we'll start with 1 page of movies (paging is a TMDB term)
            // Then, we'll slowly roll in more and more pages as needed by user
            // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie
            visiblePages = 1;
        }

        // Handle user continuously scrolling
        // Pages of new movies will be added in as user scrolls
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                if (viewIsLoading) {
                    // We'll want to add in new movies before the user can hit a "wall"
                    // This will allow users to continuously scroll without having to stop
                    // and wait for the app to GET new movies
                    if ((visibleItemCount + pastVisibleItems) >= (totalItemCount)) {
                        viewIsLoading = false;

                        String sortUrl;
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                        String defValPref = "";
                        String userSortPref = settings.getString(MainActivity.PREF_SORT, defValPref);
                        if (userSortPref.equals(MainActivity.SortType.HighestRated.getSortType())) {
                            sortUrl = MoviesHandler.MOVIES_RATING_DESC;
                        }
                        else {
                            sortUrl = MoviesHandler.MOVIES_POPULARITY_DESC;
                        }
                        String url = sortUrl
                                .concat(MoviesHandler.MOVIES_ADDPAGE)
                                .concat(Integer.toString(++visiblePages));
                        TmdbRestClient.get(url, null, new MoviesHandler() {
                                    @Override
                                    //TODO: handle deprecation: org.apache.http.Header is deprecated in API level 22
                                    // https://github.com/loopj/android-async-http/issues/833
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);
                                        if (this.parseMovies() != null && !this.parseMovies().isEmpty()) {
                                            // It is required to call addAll because this causes the
                                            // recycleradapter to realize that there is new data and to refresh the view
                                            movies.addAll(this.parseMovies());
                                        }
                                        if (mRecyclerAdapter == null) {
                                            initializeAdapter();
                                        } else {
                                            mRecyclerAdapter.notifyDataSetChanged();
                                        }
                                        mSwipeRefreshLayout.setRefreshing(false);

                                        viewIsLoading = true;
                                    }
                                });
                    }
                    // TMDB has a limit on pages that can be requested
                    // This limit is 1000, so we should stop the user from loading more than this
                    if (visiblePages >= 1000) {
                        viewIsLoading = false;
                    }
                }
            }
        });

        // Handle user pull-down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Set visible pages back to 1, because we'll be reloading all pages
                visiblePages = 1;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String defValPref = "";
                String userSortPref = settings.getString(MainActivity.PREF_SORT, defValPref);
                if (userSortPref.equals(MainActivity.SortType.HighestRated.getSortType())) {
                    initializeData(MainActivity.SortType.HighestRated);
                }
                else {
                    initializeData(MainActivity.SortType.MostPopular);
                }
                initializeAdapter();
                mRecyclerView.refreshDrawableState();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swiperefresh);
        //TODO: Set timeout on refresh in case we cannot connect to the TMDB API

        return rootView;
    }

    /**
     * Fetch data from TMDB via our rest client, TmdbRestClient, based on
     * passed sort type
     * @param sort sort type based on SortType enum
     */
    private void initializeData(MainActivity.SortType sort) {
        if (movies == null) {
            movies = new ArrayList<>();
        }

        //TODO: Handle user press of cancel or close of application
//        RequestHandle handle =

        String getUrl;
        if (sort == MainActivity.SortType.HighestRated) {
            getUrl = MoviesHandler.MOVIES_RATING_DESC;
        }
        else {
            getUrl = MoviesHandler.MOVIES_POPULARITY_DESC;
        }
        // Fetch movie data our rest client and bind the data
        TmdbRestClient.get(getUrl, null, new MoviesHandler() {
            //TODO: handle deprecation: org.apache.http.Header is deprecated in API level 22
            // https://github.com/loopj/android-async-http/issues/833
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                if (this.parseMovies() != null && !this.parseMovies().isEmpty()) {
                    movies.clear();
                    // It is required to call addAll because this causes the
                    // recycleradapter to realize that there is new data and to refresh the view
                    movies.addAll(this.parseMovies());
                }
                if (mRecyclerAdapter == null) {
                    initializeAdapter();
                } else {
                    mRecyclerAdapter.notifyDataSetChanged();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }

            //TODO: handle deprecation: org.apache.http.Header is deprecated in API level 22
            // https://github.com/loopj/android-async-http/issues/833
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //TODO: Handle user press of cancel or close of application
//      handle.cancel(true);
    }

    /**
     * Set up our RecyclerAdapter to bind the data to the RecyclerView and handle
     * user click of movie (CardView) in the RecyclerView
     */
    private void initializeAdapter() {
        // We must create the clicklistener here so that the adapter
        // can bind the data with the element that is clicked
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(DetailActivity.EXTRASCURRENTMOVIE, movies.get(itemPosition));
                startActivity(detailIntent);
            }
        };

        // Initialize the RecyclerAdapter and bind movies
        mRecyclerAdapter = new RecyclerAdapter(getActivity(), movies, clickListener);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    /**
     * Fetch movie data from TMDB that is sorted by "Most Popular"
     */
    public void sortByMostPopular() {
        // Set the user shared preference to MostPopular
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor preferenceEditor = settings.edit();
        preferenceEditor.putString(MainActivity.PREF_SORT,MainActivity.SortType.MostPopular.getSortType());
        preferenceEditor.apply();

        // Fetch the movie data
        initializeData(MainActivity.SortType.MostPopular);
    }

    /**
     * Fetch movie data from TMDB that is sorted by "Highest Rated"
     */
    public void sortByHighestRated() {
        // Set the user shared preference to HighestRated
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor preferenceEditor = settings.edit();
        preferenceEditor.putString(MainActivity.PREF_SORT,MainActivity.SortType.HighestRated.getSortType());
        preferenceEditor.apply();

        // Fetch the movie data
        initializeData(MainActivity.SortType.HighestRated);
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
}
