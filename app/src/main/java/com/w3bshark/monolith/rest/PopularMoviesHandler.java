package com.w3bshark.monolith.rest;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.w3bshark.monolith.Movie;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by w3bshark on 7/26/2015.
 */
public class PopularMoviesHandler extends JsonHttpResponseHandler {

    private static final String LOG_TAG = PopularMoviesHandler.class.getSimpleName();

    // This resolves to the relative URL below to sort movies by popularity in descending order
    // discover/movie?api_key=b135eb044beeebb67df1a9b6ee3709cf&sort_by=popularity.desc"
    public static final String POPULARMOVIES_POPULARITY_DESC =
        TMDBRestClient.DISCOVER.concat("/").concat(TMDBRestClient.DISCOVER_MOVIE)
                .concat("?").concat(TMDBRestClient.API_KEY).concat("=")
                .concat(TMDBRestClient.TMDB_APIKEY).concat("&").concat(TMDBRestClient.SORT_BY)
                .concat("=").concat(TMDBRestClient.SORT_POPULARITY)
                .concat(TMDBRestClient.SORT_DESC);

    public static final String POPULARMOVIES_PAGE = "page";
    public static final String POPULARMOVIES_ADDPAGE =  "&".concat(POPULARMOVIES_PAGE).concat("=");

    private JSONObject response;
    private ArrayList<Movie> movies;
    private String errorMessage;

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
        }
        catch (JSONException e) {
            // TODO: Handle exception appropriately
        }
    }

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
            // TODO: Handle exception appropriately
        }
    }

    public ArrayList<Movie> parseMovies() {
        return movies;
    }
}
