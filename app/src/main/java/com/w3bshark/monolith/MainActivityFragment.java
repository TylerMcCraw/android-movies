/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3bshark.monolith.rest.PopularMoviesHandler;
import com.w3bshark.monolith.rest.TmdbRestClient;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mRecyclerAdapter;
    private GridLayoutManager mLayoutManager;
    private ArrayList<Movie> movies;
    private View mCoordinatorLayoutView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int pastVisibleItems, visibleItemCount, totalItemCount, visiblePages;
    private boolean viewIsLoading = true;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        mCoordinatorLayoutView = rootView.findViewById(R.id.main_coordinator_layout);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Display only 2 columns if phone; 3 columns if tablet
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            mLayoutManager = new GridLayoutManager(this.getActivity(), 3);
        } else {
            mLayoutManager = new GridLayoutManager(this.getActivity(), 2);
        }
        mRecyclerView.setLayoutManager(mLayoutManager);

        initializeData();

        // Initially we'll start with 1 page of movies (paging is a TMDB term)
        // Then, we'll slowly roll in more and more pages as needed by user
        // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie
        visiblePages = 1;
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
                        String url = PopularMoviesHandler.POPULARMOVIES_POPULARITY_DESC
                                .concat(PopularMoviesHandler.POPULARMOVIES_ADDPAGE)
                                .concat(Integer.toString(++visiblePages));
                        TmdbRestClient.get(url, null, new PopularMoviesHandler() {
                                    @Override
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
                initializeData();
                initializeAdapter();
                mRecyclerView.refreshDrawableState();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swiperefresh);
        //TODO: Set timeout on refresh in case we cannot connect to the TMDB API

        return rootView;
    }

    private void initializeData() {
        if (movies == null) {
            movies = new ArrayList<>();
        }

        //TODO: Handle user press of cancel or close of application
//        RequestHandle handle =

        TmdbRestClient.get(PopularMoviesHandler.POPULARMOVIES_POPULARITY_DESC, null, new PopularMoviesHandler() {
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

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //TODO: Handle user press of cancel or close of application
//      handle.cancel(true);
    }

    private void initializeAdapter() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(DetailActivity.EXTRASCURRENTMOVIE, movies.get(itemPosition));
                startActivity(detailIntent);
            }
        };

        mRecyclerAdapter = new RecyclerAdapter(getActivity(), movies, clickListener);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }
}
