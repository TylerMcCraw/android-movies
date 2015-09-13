/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.rest;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.w3bshark.monolith.model.Review;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReviewsHandler extends JsonHttpResponseHandler {

    // Log tag to keep track of logs created against this class
    private static final String LOG_TAG = ReviewsHandler.class.getSimpleName();
    // This resolves to the relative URL below to retrieve reviews for a specific movie
    // movie/<MOVIE ID GOES HERE>/reviews"
    public static final String REVIEWS =
            TmdbRestClient.DISCOVER_MOVIE.concat("/");
    private ArrayList<Review> reviews;
    private String errorMessage;

    /**
     * Handle successful response from the request
     * Add reviews data from response JSON to reviews array list
     *
     * @param statusCode http response status line
     * @param headers    response headers if any
     * @param response   parsed response if any
     */
    //TODO: handle deprecation: org.apache.http.Header is deprecated in API level 22
    // https://github.com/loopj/android-async-http/issues/833
    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        try {
            JSONArray results = response.getJSONArray("results");
            if (results == null || results.length() == 0) {
                return;
            }
            reviews = new ArrayList<>();
            for (int i = 0; i < results.length(); i++) {
                Review review = new Review();
                JSONObject jsonReview = results.getJSONObject(i);
                review.setId(jsonReview.getString("id"));
                review.setAuthor(jsonReview.getString("author"));
                review.setContent(jsonReview.getString("content"));
                reviews.add(review);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Handle failed response from the request
     *
     * @param statusCode    http response status line
     * @param headers       response headers if any
     * @param throwable     throwable describing the way request failed
     * @param errorResponse parsed response if any
     */
    //TODO: handle deprecation: org.apache.http.Header is deprecated in API level 22
    // https://github.com/loopj/android-async-http/issues/833
    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        // Response failed :(
        try {
            if (errorResponse == null) {
                return;
            }
            errorMessage = errorResponse.getString("status_message");
            Log.e(LOG_TAG, errorMessage);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Handle custom transformation of reviews data
     *
     * @return arraylist of reviews
     */
    public ArrayList<Review> parseReviews() {
        return reviews;
    }

    public static String buildUrl(String movieId) {
        return REVIEWS.concat(movieId)
                .concat("/").concat(TmdbRestClient.REVIEWS)
                .concat("?")
                .concat(TmdbRestClient.API_KEY).concat("=").concat(TmdbRestClient.TMDB_APIKEY);
    }
}
