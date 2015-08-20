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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    // Base URL for all images hosted on TMDB
    private static final String BASE_IMG_URL = "http://image.tmdb.org/t/p/";
    private static final String IMG_MED_RES = "w185";
    private static final String IMG_HIGH_RES = "w342";

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View detailFragment = inflater.inflate(R.layout.fragment_detail, container, false);
        if (getActivity().getIntent() == null || !getActivity().getIntent().hasExtra(DetailActivity.EXTRASCURRENTMOVIE)) {
            String snackMessage;
            snackMessage = getActivity().getApplicationContext().getString(R.string.error_unexpected);
            Snackbar.make(this.getView(), snackMessage, Snackbar.LENGTH_SHORT).show();
        } else {
            Movie selectedMovie = getActivity().getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE);

            // Title
            TextView titleView = (TextView) detailFragment.findViewById(R.id.detail_movie_title);
            titleView.setText(selectedMovie.getTitle());

            // Poster
            ImageView image = (ImageView) detailFragment.findViewById(R.id.detail_poster);

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
            ratingView.setText(selectedMovie.getVoteAverage().toString());
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
                Log.e(LOG_TAG, e.getMessage());
            }
            String convertedDateStr = displayFormat.format(convertedDate);
            releaseDateView.setText(convertedDateStr);

            // Description
            TextView descriptionView = (TextView) detailFragment.findViewById(R.id.detail_description_textview);
            descriptionView.setText(selectedMovie.getDescription());

        }

        return detailFragment;
    }
}
