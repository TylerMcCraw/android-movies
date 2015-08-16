/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.rest;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

public class TmdbRestClient {

    // Base URL for all GET/POST calls
    private static final String BASE_URL = "http://api.themoviedb.org/3/";

    // API Key to be used for all GET/POST calls
    public static final String API_KEY = "api_key";
    public static final String TMDB_APIKEY = "b135eb044beeebb67df1a9b6ee3709cf";

    // API key words to be used for relative URL additions
    public static final String SORT_BY = "sort_by";
    public static final String SORT_POPULARITY = "popularity";
    public static final String SORT_DESC = ".desc";
    public static final String DISCOVER = "discover";
    public static final String DISCOVER_MOVIE = "movie";
    public static final String DISCOVER_TV = "tv";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static RequestHandle get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static RequestHandle post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
