/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.w3bshark.monolith.R;
import com.w3bshark.monolith.model.Movie;

import java.util.List;

/**
 * Adapter for binding data to RecyclerView used in MainActivityFragment
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MovieViewHolder> {

    // Base URL for all images hosted on TMDB
    private static final String BASE_IMG_URL = "http://image.tmdb.org/t/p/";
    // Extension path of Base URL for selecting 342 (height) based movie posters
    private static final String IMG_MED_RES = "w342";
    // Extension path of Base URL for selecting 780 (height) based movie posters
    private static final String IMG_HIGH_RES = "w780";

    /**
     * View holder for handling subview binding in RecyclerView
     */
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView poster;

        MovieViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            poster = (ImageView) itemView.findViewById(R.id.poster);
        }
    }

    Context context;
    List<Movie> movies;
    View.OnClickListener clickListener;

    public RecyclerAdapter(Context context, List<Movie> movies, View.OnClickListener clickListener) {
        this.context = context;
        this.movies = movies;
        this.clickListener = clickListener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_item, viewGroup, false);
        return new MovieViewHolder(v);
    }

    /**
     * Bind movie data to the view holder for RecyclerView and set the
     * click listener to each card view so that user can select an individual movie view
     * @param movieViewHolder the view holder for binding data
     * @param i the position of the view holder in the RecyclerView
     */
    @Override
    public void onBindViewHolder(MovieViewHolder movieViewHolder, int i) {
        // Set the tag of the CardView to the unique movie title, in case we need this later
        movieViewHolder.cv.setTag(movies.get(i).getTitle());

        // Bind the movie data to the view elements
        if (movies.get(i).getImageCode() != null && !movies.get(i).getImageCode().isEmpty()) {
            String imgURL = BASE_IMG_URL;
            boolean isTablet = context.getResources().getBoolean(R.bool.isTablet);
            if (isTablet) {
                imgURL = imgURL.concat(IMG_HIGH_RES);
            } else {
                imgURL = imgURL.concat(IMG_MED_RES);
            }
            Picasso.with(context).load(imgURL.concat(movies.get(i).getImageCode())).into(movieViewHolder.poster);
        }

        if (movies.get(i).getDescription() != null && !movies.get(i).getDescription().isEmpty()) {
            movieViewHolder.poster.setContentDescription(movies.get(i).getDescription());
        }

        // Bind the click listener to the CardView
        movieViewHolder.cv.setOnClickListener(clickListener);
    }

    /**
     * Set the item count to the count of the movies array list
     * @return the count of items in the RecyclerView
     */
    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }
}
