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
import android.widget.TextView;

import com.w3bshark.monolith.R;
import com.w3bshark.monolith.model.Review;

import java.util.ArrayList;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
    /**
     * View holder for handling subview binding in RecyclerView
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView reviewAuthor;
        TextView reviewContent;

        ReviewViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.detail_review_cv);
            reviewAuthor = (TextView) itemView.findViewById(R.id.detail_review_author);
            reviewContent = (TextView) itemView.findViewById(R.id.detail_review_content);
        }
    }

    Context context;
    ArrayList<Review> reviews;
    View.OnClickListener clickListener;

    public ReviewsAdapter(Context context, ArrayList<Review> reviews, View.OnClickListener clickListener) {
        this.context = context;
        this.reviews = reviews;
        this.clickListener = clickListener;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.detail_recycler_review_item, viewGroup, false);
        return new ReviewViewHolder(v);
    }

    /**
     * Bind review data to the view holder for RecyclerView and set the
     * click listener to each card view so that user can select an individual movie view
     *
     * @param reviewViewHolder the view holder for binding data
     * @param i                 the position of the view holder in the RecyclerView
     */
    @Override
    public void onBindViewHolder(ReviewViewHolder reviewViewHolder, int i) {
        // Set the tag of the CardView to the unique movie title, in case we need this later
        reviewViewHolder.cv.setTag(reviews.get(i).getId());

        // Bind the review data to the view elements
        if (reviews.get(i).getAuthor() != null && !reviews.get(i).getAuthor().isEmpty()) {
            reviewViewHolder.reviewAuthor.setText(reviews.get(i).getAuthor());
        }
        if (reviews.get(i).getContent() != null && !reviews.get(i).getContent().isEmpty()) {
            reviewViewHolder.reviewContent.setText(reviews.get(i).getContent());

        }

        // Bind the click listener to the CardView
        reviewViewHolder.cv.setOnClickListener(clickListener);
    }

    /**
     * Set the item count to the count of the reviews
     *
     * @return the count of items in the Reviews RecyclerView
     */
    @Override
    public int getItemCount() {
        return reviews == null ? 0 : reviews.size();
    }
}
