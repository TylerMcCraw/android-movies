package com.w3bshark.monolith;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3bshark.monolith.rest.PopularMoviesHandler;
import com.w3bshark.monolith.rest.TMDBRestClient;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Movie> movies;
    private View mCoordinatorLayoutView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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

        mLayoutManager = new GridLayoutManager(this.getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        initializeData();

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initializeData();
                initializeAdapter();
                mRecyclerView.refreshDrawableState();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swiperefresh);

        return rootView;
    }

    private void initializeData(){
        if (movies == null) {
            movies = new ArrayList<>();
        }

//        RequestHandle handle =
        TMDBRestClient.get( PopularMoviesHandler.POPULARMOVIES_POPULARITY_DESC, null, new PopularMoviesHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                if (this.getMovies() != null && !this.getMovies().isEmpty()) {
                    movies.clear();
                    // It is required to call addAll because this causes the
                    // recycleradapter to realize that there is new data and to refresh the view
                    movies.addAll(this.getMovies());
                }
                if (mRecyclerAdapter == null) {
                    initializeAdapter();
                }
                else {
                    mRecyclerAdapter.notifyDataSetChanged();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }

            //TODO: Handle override of onFailure event
        });

        //TODO: Handle user press of cancel or close of application
//      handle.cancel(true);
    }

    private void initializeAdapter(){
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
//                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
//                        .putExtra(DetailActivity.EXTRASCURRENTDAY, movies.get(itemPosition));
//                startActivity(detailIntent);
            }
        };

        mRecyclerAdapter = new RecyclerAdapter(getActivity(), movies, clickListener);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }
}
