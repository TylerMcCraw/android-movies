/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.rest;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.w3bshark.monolith.model.Trailer;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrailersHandler extends JsonHttpResponseHandler {

    // Log tag to keep track of logs created against this class
    private static final String LOG_TAG = TrailersHandler.class.getSimpleName();
    // This resolves to the relative URL below to retrieve trailer videos for a specific movie
    // movie/<MOVIE ID GOES HERE>/videos"
    public static final String TRAILERS =
            TmdbAPIRestClient.DISCOVER_MOVIE.concat("/");
    private ArrayList<Trailer> trailers;
    private String errorMessage;

    /**
     * Handle successful response from the request
     * Add movie data from response JSON to trailers array list
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
            trailers = new ArrayList<>();
            for (int i = 0; i < results.length(); i++) {
                Trailer trailer = new Trailer();
                JSONObject jsonTrailer = results.getJSONObject(i);
                trailer.setName(jsonTrailer.getString("name"));
                trailer.setVideoPath(jsonTrailer.getString("key"));
                trailer.setVideoSite(jsonTrailer.getString("site"));
                trailers.add(trailer);
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
     * Handle custom transformation of movie data
     *
     * @return hashmap of trailers (map of name to youtube video link path)
     */
    public ArrayList<Trailer> parseTrailers() {
        return trailers;
    }

    public static String buildUrl(String movieId) {
        return TRAILERS.concat(movieId)
                .concat("/").concat(TmdbAPIRestClient.VIDEOS)
                .concat("?")
                .concat(TmdbAPIRestClient.API_KEY).concat("=").concat(TmdbAPIRestClient.TMDB_APIKEY);
    }
}
