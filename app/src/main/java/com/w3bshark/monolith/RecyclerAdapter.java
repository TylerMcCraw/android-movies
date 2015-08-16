/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MovieViewHolder> {

    // Base URL for all images hosted on TMDB
    private static final String BASE_IMG_URL = "http://image.tmdb.org/t/p/";
    private static final String IMG_MED_RES = "w342";
    private static final String IMG_HIGH_RES = "w780";

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView appPhoto;

        MovieViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            appPhoto = (ImageView) itemView.findViewById(R.id.app_photo);
        }
    }

    Context context;
    List<Movie> movies;
    View.OnClickListener clickListener;

    RecyclerAdapter(Context context, List<Movie> movies, View.OnClickListener clickListener) {
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

    @Override
    public void onBindViewHolder(MovieViewHolder movieViewHolder, int i) {
        movieViewHolder.cv.setTag(movies.get(i).getTitle());
        String imgURL = BASE_IMG_URL;
        boolean isTablet = context.getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            imgURL = imgURL.concat(IMG_HIGH_RES);
        } else {
            imgURL = imgURL.concat(IMG_MED_RES);
        }
        Picasso.with(context).load(imgURL.concat(movies.get(i).getImageCode())).into(movieViewHolder.appPhoto);
        movieViewHolder.appPhoto.setContentDescription(movies.get(i).getDescription());
        movieViewHolder.cv.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }
}
