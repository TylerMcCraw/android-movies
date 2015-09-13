/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w3bshark.monolith.R;
import com.w3bshark.monolith.activities.DetailActivity;
import com.w3bshark.monolith.model.Movie;
import com.w3bshark.monolith.model.Trailer;
import com.w3bshark.monolith.rest.TmdbAPIRestClient;
import com.w3bshark.monolith.rest.TrailersHandler;
import com.w3bshark.monolith.widget.TrailersAdapter;

import org.apache.http.Header;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A custom fragment for displaying movie details based on the user-selected movie
 * This fragment is created within DetailActivity
 */
public class DetailActivityFragment extends Fragment {

    // Log tag to keep track of logs created against this class
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    // Base URL for all images hosted on TMDB
    private static final String BASE_IMG_URL = "http://image.tmdb.org/t/p/";
    // Extension path of Base URL for selecting 185 (width) based movie posters
    private static final String IMG_MED_RES = "w185";
    // Extension path of Base URL for selecting 342 (width) based movie posters
    private static final String IMG_HIGH_RES = "w342";
    // Movie parcelable key for saving instance state
    private static final String SAVED_MOVIE = "SAVED_MOVIE";
    // YouTube url for opening YouTube links
    private static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";

    // Currently selected movie
    private Movie selectedMovie;
    // Temporary instance of trailers for the selected movie
    private ArrayList<Trailer> trailers;
    // Our recycler view used to display trailers as card views
    private RecyclerView mTrailersView;
    // Adapter used to bind trailer data to our recycler view
    private TrailersAdapter mTrailersAdapter;
    //    // Temporary instance of trailers for the selected movie
//    private ArrayList<Review> reviews;
    // Our recycler view used to display trailers as card views
    private RecyclerView mReviewsView;
    //    // Adapter used to bind trailer data to our recycler view
//    private TrailersAdapter mReviewsAdapter;
    // LayoutManager for handling layout of card views
    private LinearLayoutManager mLayoutManager;

    public DetailActivityFragment() {
    }

    /**
     * Handle creation of fragment view, assign proper data to elements of view, and handle
     * uncaught exceptions in displaying data from adapter
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

        // Attempt to restore from savedInstanceState
        if (savedInstanceState != null) {
            selectedMovie = savedInstanceState.getParcelable(SAVED_MOVIE);
        }

        // Inflate the fragment layout
        View detailFragment = inflater.inflate(R.layout.fragment_detail, container, false);
        if (getActivity().getIntent() == null) {
            String snackMessage;
            snackMessage = getActivity().getApplicationContext().getString(R.string.error_unexpected);
            Snackbar.make(this.getView(), snackMessage, Snackbar.LENGTH_SHORT).show();
            // Otherwise, load the movie data into each corresponding fragment view
        } else {
            if (selectedMovie == null) {
                selectedMovie = getActivity().getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE);
            }

            setUpTrailersView(detailFragment, inflater, container);
            retrieveTrailerData(selectedMovie.getMovieId());

//            setUpReviewsView(detailFragment, inflater, container);
//            retrieveReviewsData(selectedMovie.getMovieId());

            // Title
            TextView titleView = (TextView) detailFragment.findViewById(R.id.detail_movie_title);
            titleView.setText(selectedMovie.getTitle());

            // Poster
            ImageView image = (ImageView) detailFragment.findViewById(R.id.detail_poster);
            // Fetch the poster for the movie using Picasso
            String imgURL = BASE_IMG_URL;
            boolean isTablet = getResources().getBoolean(R.bool.isTablet);
            if (isTablet) {
                imgURL = imgURL.concat(IMG_HIGH_RES);
            } else {
                imgURL = imgURL.concat(IMG_MED_RES);
            }
            Picasso.with(getActivity().getApplicationContext()).load(imgURL.concat(selectedMovie.getImageCode())).into(image);

            // Rating
            TextView ratingView = (TextView) detailFragment.findViewById(R.id.detail_rating_textview);
            if (selectedMovie.getVoteAverage() != null) {
                ratingView.setText(new DecimalFormat("#.##").format(selectedMovie.getVoteAverage()));
            }
            // "Out of" Rating
            TextView ratingTotalView = (TextView) detailFragment.findViewById(R.id.detail_rating_total_textview);
            ratingTotalView.setText("/10");

            // Release Date
            TextView releaseDateView = (TextView) detailFragment.findViewById(R.id.detail_release_date_textview);
            DateFormat tmdbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            DateFormat displayFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            Date convertedDate = new Date();
            try {
                convertedDate = tmdbFormat.parse(selectedMovie.getReleaseDate());
            } catch (ParseException e) {
                // If we couldn't parse the date for whatever reason, log it.
                Log.e(LOG_TAG, e.getMessage());
            }
            String convertedDateStr = displayFormat.format(convertedDate);
            releaseDateView.setText(convertedDateStr);

            // Description
            TextView descriptionView = (TextView) detailFragment.findViewById(R.id.detail_description_textview);
            if (selectedMovie.getDescription() != null && !selectedMovie.getDescription().equals("null")) {
                descriptionView.setText(selectedMovie.getDescription());
            }
        }

        return detailFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (getActivity().getIntent() != null &&
                getActivity().getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE) != null) {
            savedInstanceState.putParcelable(SAVED_MOVIE,
                    getActivity().getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE));
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    private void setUpTrailersView(View detailFragment, LayoutInflater inflater, ViewGroup container) {
        // Set up the xml layout
        mTrailersView = (RecyclerView) detailFragment.findViewById(R.id.detail_rv_trailers);

        // Improves performance if changes in content do not change
        // the layout size of the RecyclerView
        mTrailersView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mTrailersView.setLayoutManager(mLayoutManager);
        // Set up initial adapter (until we retrieve our data) so there is no skipping the layout
        mTrailersView.setAdapter(new TrailersAdapter(getActivity(), new ArrayList<Trailer>(), null));
    }

    private void setUpReviewsView(View detailFragment, LayoutInflater inflater, ViewGroup container) {
        // Set up the xml layout
        mReviewsView = (RecyclerView) detailFragment.findViewById(R.id.detail_rv_reviews);

        // Improves performance if changes in content do not change
        // the layout size of the RecyclerView
        mReviewsView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mReviewsView.setLayoutManager(mLayoutManager);
//        // Set up initial adapter (until we retrieve our data) so there is no skipping the layout
//        mReviewsView.setAdapter(new TrailersAdapter(getActivity(), new ArrayList<Trailer>(), null));
    }

    /**
     * Set up our TrailersAdapter to bind the data to the RecyclerView and handle
     * user click of trailer (CardView) in the RecyclerView
     */
    private void initializeAdapter() {
        // We must create the clicklistener here so that the adapter
        // can bind the data with the element that is clicked
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mTrailersView.getChildLayoutPosition(v);
                try {
                    // Open the Youtube app if it's available
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("vnd.youtube:" + trailers.get(itemPosition).getVideoPath()));
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    // Otherwise, just open the youtube link in the default browser
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(YOUTUBE_BASE_URL
                                    .concat(trailers.get(itemPosition).getVideoPath())));
                    startActivity(intent);
                }
            }
        };

        // Initialize the TrailersAdapter and bind trailers
        mTrailersAdapter = new TrailersAdapter(getActivity(), trailers, clickListener);
        mTrailersView.setAdapter(mTrailersAdapter);
    }

    private void retrieveTrailerData(String movieId) {
        if (trailers == null) {
            trailers = new ArrayList<>();
        }

        String getUrl = TrailersHandler.buildUrl(movieId);

        // Fetch trailer data using our rest client and bind the data
        TmdbAPIRestClient.get(getUrl, null, new TrailersHandler() {
            //TODO: handle deprecation: org.apache.http.Header is deprecated in API level 22
            // https://github.com/loopj/android-async-http/issues/833
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                if (this.parseTrailers() != null && !this.parseTrailers().isEmpty()) {
                    trailers.clear();
                    // It is required to call addAll because this causes the
                    // trailersadapter to realize that there is new data and to refresh the view
                    trailers.addAll(this.parseTrailers());

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(Movie.MOVIE_TRAILERS, trailers);
                    selectedMovie.setTrailers(bundle);
                    if (trailers != null && trailers.size() >= 0) {
                        DetailActivity activity = (DetailActivity) getActivity();
                        activity.setShareIntent(getShareIntentForActivity(trailers.get(0).getVideoPath()));
                    }
                }
                if (mTrailersAdapter == null) {
                    initializeAdapter();
                } else {
                    mTrailersAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private Intent getShareIntentForActivity(String videoPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET was deprecated as of API 21
        // This intent flag is important so that the activity is cleared from recent tasks
        //  whenever the activity is finished/closed
        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.detail_share_subject).concat(" ")
                .concat(selectedMovie.getTitle()));

        String sharedText = selectedMovie.getTitle();
        // Build text for share provider
        // includes youtube trailer of first trailer for selected movie
        sharedText = sharedText
                .concat(" - ")
                .concat(YOUTUBE_BASE_URL.concat(videoPath))
                .concat(" ")
                .concat(getString(R.string.detail_share_hashtag));

        intent.putExtra(Intent.EXTRA_TEXT, sharedText);

        return intent;
    }
}
