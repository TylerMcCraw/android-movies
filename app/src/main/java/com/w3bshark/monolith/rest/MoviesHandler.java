/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.rest;

import android.content.ContentValues;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.w3bshark.monolith.data.MovieContract.MovieEntry;
import com.w3bshark.monolith.model.Movie;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Custom HTTP response handler class specifically for handling
 * transforming REST responses to/from TMDB's "discover" API for movies
 * See <a href="https://www.themoviedb.org/documentation/api"> TMDB API </a>
 * and <a href="http://docs.themoviedb.apiary.io/#reference/discover/discovermovie/get">
 * TMDB Discover API on Apiary </a>
 */
public class MoviesHandler extends JsonHttpResponseHandler {

    // Log tag to keep track of logs created against this class
    private static final String LOG_TAG = MoviesHandler.class.getSimpleName();
    // This resolves to the relative URL below to sort movies by popularity in descending order
    // discover/movie?sort_by=popularity.desc"
    public static final String MOVIES_POPULARITY_DESC =
            TmdbAPIRestClient.DISCOVER.concat("/").concat(TmdbAPIRestClient.DISCOVER_MOVIE)
                    .concat("?").concat(TmdbAPIRestClient.API_KEY).concat("=")
                    .concat(TmdbAPIRestClient.TMDB_APIKEY).concat("&").concat(TmdbAPIRestClient.SORT_BY)
                    .concat("=").concat(TmdbAPIRestClient.SORT_POPULARITY)
                    .concat(TmdbAPIRestClient.SORT_DESC);
    // This resolves to the relative URL below to sort movies by popularity in descending order
    // discover/movie?sort_by=vote_average.desc&vote_count.gte=50"
    // We're assuming here that if a movie has had less than 50 total votes, then it's not worth
    // including in this list
    public static final String MOVIES_RATING_DESC =
            TmdbAPIRestClient.DISCOVER.concat("/").concat(TmdbAPIRestClient.DISCOVER_MOVIE)
                    .concat("?").concat(TmdbAPIRestClient.API_KEY).concat("=")
                    .concat(TmdbAPIRestClient.TMDB_APIKEY).concat("&").concat(TmdbAPIRestClient.SORT_BY)
                    .concat("=").concat(TmdbAPIRestClient.SORT_RATING)
                    .concat(TmdbAPIRestClient.SORT_DESC)
                    .concat("&").concat(TmdbAPIRestClient.SORT_VOTE_COUNT)
                    .concat(TmdbAPIRestClient.MODIFIER_GTE).concat("=50");
    public static final String MOVIES_PAGE = "page";
    // This is used for the scroll listener in MainActivityFragment to determine what
    // "page" of data should be returned from the TMDB API
    public static final String MOVIES_ADDPAGE = "&".concat(MOVIES_PAGE).concat("=");
    private ArrayList<Movie> movies;
    private Vector<ContentValues> moviesValuesVector = null;
    private String errorMessage;

    /**
     * Handle successful response from the request
     * Add movie data from response JSON to movies array list
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
            movies = new ArrayList<>();
            moviesValuesVector = new Vector<>(results.length());
            for (int i = 0; i < results.length(); i++) {
                Movie movie = new Movie();
                JSONObject jsonMovie = results.getJSONObject(i);
                movie.setMovieId(jsonMovie.getString("id"));
                movie.setTitle(jsonMovie.getString("original_title"));
                movie.setDescription(jsonMovie.getString("overview"));
                movie.setImageCode(jsonMovie.getString("poster_path"));
                movie.setPopularity(jsonMovie.getLong("popularity"));
                movie.setVoteAverage(jsonMovie.getDouble("vote_average"));
                movie.setReleaseDate(jsonMovie.getString("release_date"));
                movies.add(movie);

                ContentValues moviesValues = new ContentValues();

                moviesValues.put(MovieEntry.COLUMN_MOVIE_ID, jsonMovie.getString("id"));
                moviesValues.put(MovieEntry.COLUMN_TITLE, jsonMovie.getString("original_title"));
                moviesValues.put(MovieEntry.COLUMN_DESCR, jsonMovie.getString("overview"));
                moviesValues.put(MovieEntry.COLUMN_IMAGE_CODE, jsonMovie.getString("poster_path"));
                moviesValues.put(MovieEntry.COLUMN_POPULARITY, jsonMovie.getDouble("popularity"));
                moviesValues.put(MovieEntry.COLUMN_VOTE_AVG, jsonMovie.getDouble("vote_average"));
                moviesValues.put(MovieEntry.COLUMN_RELEASE_DATE, jsonMovie.getString("release_date"));

                moviesValuesVector.add(moviesValues);
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
     * @return array list of movies
     */
    public ArrayList<Movie> parseMovies() {
        return movies;
    }

    public Vector<ContentValues> getMoviesVector() {
        return moviesValuesVector;
    }
}
