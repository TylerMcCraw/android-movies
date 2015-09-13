/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.w3bshark.monolith";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    /* Inner class that defines the table contents of the movies table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCR = "description";
        public static final String COLUMN_IMAGE_CODE = "image_code";
        public static final String COLUMN_RELEASE_DATE = "rel_date";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_AVG = "vote_avg";
        public static final String COLUMN_FAVORITE = "favorite";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildAllMoviesUri() {
            return CONTENT_URI;
        }
    }
}