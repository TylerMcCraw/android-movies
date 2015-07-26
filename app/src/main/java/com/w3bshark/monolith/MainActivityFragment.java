package com.w3bshark.monolith;

import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        movies = new ArrayList<>();

        MainActivity mainActivity = (MainActivity) getActivity();
        Movie temp = new Movie();
        movies.add(temp);
        movies.add(temp);
        movies.add(temp);
        movies.add(temp);
        movies.add(temp);
        movies.add(temp);
        movies.add(temp);
        movies.add(temp);

//        TenDayForecastHandler tenDayForecastHandler = new TenDayForecastHandler(getActivity().getApplicationContext())
//        {
//            @Override
//            protected void onPostExecute(ArrayList<Day> result) {
//                if (result != null && !result.isEmpty()) {
//                    days.clear();
//                    days.addAll(result);
//                }
//                if (mRecyclerAdapter == null) {
//                    initializeAdapter();
//                }
//                else {
//                    mRecyclerAdapter.notifyDataSetChanged();
//                }
//                mSwipeRefreshLayout.setRefreshing(false);
//            }
//        };
//        tenDayForecastHandler.execute(location);
        initializeAdapter();

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
