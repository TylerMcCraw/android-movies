/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.rest;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.w3bshark.monolith.model.Movie;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Custom HTTP response handler class specifically for handling
 * transforming REST responses to/from TMDB's "discover" API for movies
 * See <a href="https://www.themoviedb.org/documentation/api"> TMDB API </a>
 *     and <a href="http://docs.themoviedb.apiary.io/#reference/discover/discovermovie/get">
 *         TMDB Discover API on Apiary </a>
 */
public class MoviesHandler extends JsonHttpResponseHandler {

    // Log tag to keep track of logs created against this class
    private static final String LOG_TAG = MoviesHandler.class.getSimpleName();
    // This resolves to the relative URL below to sort movies by popularity in descending order
    // discover/movie?api_key=b135eb044beeebb67df1a9b6ee3709cf&sort_by=popularity.desc"
    public static final String MOVIES_POPULARITY_DESC =
            TmdbRestClient.DISCOVER.concat("/").concat(TmdbRestClient.DISCOVER_MOVIE)
                    .concat("?").concat(TmdbRestClient.API_KEY).concat("=")
                    .concat(TmdbRestClient.TMDB_APIKEY).concat("&").concat(TmdbRestClient.SORT_BY)
                    .concat("=").concat(TmdbRestClient.SORT_POPULARITY)
                    .concat(TmdbRestClient.SORT_DESC);
    // This resolves to the relative URL below to sort movies by popularity in descending order
    // discover/movie?api_key=b135eb044beeebb67df1a9b6ee3709cf&sort_by=vote_average.desc"
    public static final String MOVIES_RATING_DESC =
            TmdbRestClient.DISCOVER.concat("/").concat(TmdbRestClient.DISCOVER_MOVIE)
                    .concat("?").concat(TmdbRestClient.API_KEY).concat("=")
                    .concat(TmdbRestClient.TMDB_APIKEY).concat("&").concat(TmdbRestClient.SORT_BY)
                    .concat("=").concat(TmdbRestClient.SORT_RATING)
                    .concat(TmdbRestClient.SORT_DESC);
    public static final String MOVIES_PAGE = "page";
    // This is used for the scroll listener in MainActivityFragment to determine what
    // "page" of data should be returned from the TMDB API
    public static final String MOVIES_ADDPAGE = "&".concat(MOVIES_PAGE).concat("=");
    private ArrayList<Movie> movies;
    private String errorMessage;

    /**
     * Handle successful response from the request
     * Add movie data from response JSON to movies array list
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
            movies = new ArrayList<>();
            for (int i = 0; i < results.length(); i++) {
                Movie movie = new Movie();
                JSONObject jsonMovie = results.getJSONObject(i);
                movie.setTitle(jsonMovie.getString("original_title"));
                movie.setDescription(jsonMovie.getString("overview"));
                movie.setImageCode(jsonMovie.getString("poster_path"));
                movie.setVoteAverage(jsonMovie.getDouble("vote_average"));
                movie.setReleaseDate(jsonMovie.getString("release_date"));
                movies.add(movie);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Handle failed response from the request
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
     * @return array list of movies
     */
    public ArrayList<Movie> parseMovies() {
        return movies;
    }
}
