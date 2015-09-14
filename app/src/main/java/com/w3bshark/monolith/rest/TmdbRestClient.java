/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.rest;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

/**
 * Custom client class for handling all REST (GET and POST) requests
 * specifically for calls to the open TMDB API
 * See <a href="https://www.themoviedb.org/documentation/api"> TMDB API </a>
 */
public class TmdbRestClient {

    // Base URL for all GET/POST calls
    private static final String BASE_URL = "http://api.themoviedb.org/3/";

    // API Key to be used for all GET/POST calls
    public static final String API_KEY = "api_key";
    // NOTE: You MUST generate your own API KEY at https://www.themoviedb.org/documentation/api
    // Further Terms of Use information can be found at
    //      https://www.themoviedb.org/documentation/api/terms-of-use
    // Replace your generated API key in TMDB_APIKEY below
    public static final String TMDB_APIKEY = "";

    // API key words to be used for relative URL additions
    public static final String SORT_BY = "sort_by";
    public static final String SORT_POPULARITY = "popularity";
    public static final String SORT_RATING = "vote_average";
    public static final String SORT_VOTE_COUNT = "vote_count";
    public static final String SORT_DESC = ".desc";
    public static final String MODIFIER_GTE = ".gte";
    public static final String DISCOVER = "discover";
    public static final String DISCOVER_MOVIE = "movie";
    public static final String VIDEOS = "videos";
    public static final String REVIEWS = "reviews";

    //TODO: Future enhancement? - add ability to view TV shows too?
//    public static final String DISCOVER_TV = "tv";

    //TODO: Future enhancement? - add option to discover newer movies (yet to be released)
//    /discover/movie?primary_release_date.gte=2015-09-13&primary_release_date.lte=2014-10-22

    /*
        Android Asynchronous Http Client developed by James Smith
        See <a href="http://loopj.com/android-async-http/">
        This will be used for easier handling of all REST requests
    */
    private static AsyncHttpClient client = new AsyncHttpClient();

    /**
     * Set up request URL, GET parameters, and response handler
     * AND Execute the GET request
     *
     * @param url             the URL to send the request to.
     * @param params          additional GET parameters to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     * @return RequestHandle of future request process
     */
    public static RequestHandle get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    // Right now POST is never used

    /**
     * Set up request URL, POST parameters, and response handler
     * AND Execute the GET request
     *
     * @param url             the URL to send the request to.
     * @param params          additional POST parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     * @return RequestHandle of future request process
     */
    public static RequestHandle post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * Set up the URL for GET/POST requests
     *
     * @param relativeUrl URL path of API call (beyond the TMDB base URL)
     * @return complete URL of concatenated base URL and relative URL for the TMDB API call
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
