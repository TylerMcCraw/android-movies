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
import android.widget.TextView;

import com.w3bshark.monolith.R;
import com.w3bshark.monolith.model.Trailer;

import java.util.ArrayList;

/**
 * Adapter for binding data to RecyclerView used in MainActivityFragment
 */
public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    /**
     * View holder for handling subview binding in RecyclerView
     */
    public static class TrailerViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView trailerIcon;
        TextView trailerName;

        TrailerViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.detail_trailer_cv);
            trailerIcon = (ImageView) itemView.findViewById(R.id.detail_trailer_icon);
            trailerName = (TextView) itemView.findViewById(R.id.detail_trailer_name);
        }
    }

    Context context;
    ArrayList<Trailer> trailers;
    View.OnClickListener clickListener;

    public TrailersAdapter(Context context, ArrayList<Trailer> trailers, View.OnClickListener clickListener) {
        this.context = context;
        this.trailers = trailers;
        this.clickListener = clickListener;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.detail_recycler_trailer_item, viewGroup, false);
        return new TrailerViewHolder(v);
    }

    /**
     * Bind trailer data to the view holder for RecyclerView and set the
     * click listener to each card view so that user can select an individual movie view
     *
     * @param trailerViewHolder the view holder for binding data
     * @param i                 the position of the view holder in the RecyclerView
     */
    @Override
    public void onBindViewHolder(TrailerViewHolder trailerViewHolder, int i) {
        // Set the tag of the CardView to the unique movie title, in case we need this later
        trailerViewHolder.cv.setTag(trailers.get(i).getName());

        // Bind the trailer data to the view elements
        if (trailers.get(i).getName() != null && !trailers.get(i).getName().isEmpty()) {
            trailerViewHolder.trailerName.setText(trailers.get(i).getName());
            trailerViewHolder.trailerIcon.setContentDescription(trailers.get(i).getName());
        }

        // Bind the click listener to the CardView
        trailerViewHolder.cv.setOnClickListener(clickListener);
    }

    /**
     * Set the item count to the count of the trailers
     *
     * @return the count of items in the Trailers RecyclerView
     */
    @Override
    public int getItemCount() {
        return trailers == null ? 0 : trailers.size();
    }
}
