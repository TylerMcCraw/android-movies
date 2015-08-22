/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public DetailActivityFragment() {
    }

    /**
     * Handle creation of fragment view, assign proper data to elements of view, and handle
     * uncaught exceptions in displaying data from adapter
     * @param inflater used for inflating the xml layout
     * @param container view to inflate the xml layout into
     * @param savedInstanceState instance state of the application activity
     * @return main view displayed in the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Movie selectedMovie = null;
        // Attempt to restore from savedInstanceState
        if (savedInstanceState != null) {
            selectedMovie = savedInstanceState.getParcelable(SAVED_MOVIE);
        }

        // Inflate the fragment layout
        View detailFragment = inflater.inflate(R.layout.fragment_detail, container, false);
        // If we weren't given the movie data, display an error message
        if (getActivity().getIntent() == null) {
            String snackMessage;
            snackMessage = getActivity().getApplicationContext().getString(R.string.error_unexpected);
            Snackbar.make(this.getView(), snackMessage, Snackbar.LENGTH_SHORT).show();
        // Otherwise, load the movie data into each corresponding fragment view
        } else {
            if (selectedMovie == null) {
                selectedMovie = getActivity().getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE);
            }

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
            }
            catch (ParseException e) {
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
}
