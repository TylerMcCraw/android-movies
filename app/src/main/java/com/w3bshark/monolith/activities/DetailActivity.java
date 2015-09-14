/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.w3bshark.monolith.R;
import com.w3bshark.monolith.data.MovieContract.MovieEntry;
import com.w3bshark.monolith.fragments.DetailActivityFragment;
import com.w3bshark.monolith.model.Movie;

import java.util.GregorianCalendar;

/**
 * An AppCompatActivity that presents movie details to the user for a specific
 * movie that the user has selected. The activity will allow the user to view a rating,
 * watch trailers, and share the movie via a share intent
 */
public class DetailActivity extends AppCompatActivity {

    // Used for passing movie data to a share intent
    public static final String EXTRASCURRENTMOVIE = "EXTRAS_CURRENT_MOVIE";
    // Used for logging against this class
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    // Used for social sharing
    private ShareActionProvider mShareActionProvider;

    /**
     * Set up the fragment for displaying the movie details
     *
     * @param savedInstanceState the instancestate of the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, new DetailActivityFragment())
                    .commit();
        }
    }

    /**
     * Call to update the share intent for custom use
     *
     * @param shareIntent the intent used by the shareactionprovider
     */
    public void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /**
     * Overridden method to display the share action button in the options menu
     *
     * @param menu the options menu
     * @return boolean - if options were created for the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        Movie selectedMovie = getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE);
        if (selectedMovie.getFavorite() != null && !selectedMovie.getFavorite().isEmpty()) {
            //Locate MenuItem for Favorite/Bookmark button
            MenuItem favItem = menu.findItem(R.id.menu_item_bookmark);
            favItem.setIcon(R.drawable.bookmark_plus);
            favItem.setChecked(true);
        }

        // Locate MenuItem with ShareActionProvider
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(getDefaultShareIntent());
        } else {
            Log.d(LOG_TAG, "ShareActionProvider is null");
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_bookmark) {
            Movie selectedMovie = getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE);
            if (item.isChecked()) {
                item.setIcon(R.drawable.ic_bookmark_outline_plus);
                item.setChecked(false);
                if (selectedMovie != null) {
                    removeMovieFromFavs(selectedMovie);
                }
            } else {
                item.setIcon(R.drawable.bookmark_plus);
                item.setChecked(true);

                if (selectedMovie != null) {
                    addMovieToFavs(selectedMovie);
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int removeMovieFromFavs(Movie selectedMovie) {
        String[] selectionArgs = new String[]{selectedMovie.getMovieId()};
        return getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                selectionArgs
        );
    }

    public int addMovieToFavs(Movie selectedMovie) {
        // Update a movie to mark it as a favorite
        ContentValues contentValues = new ContentValues();
        GregorianCalendar cal = new GregorianCalendar();
        contentValues.put(MovieEntry.COLUMN_FAVORITE, cal.getTimeInMillis());
        String[] selectionArgs = new String[]{selectedMovie.getMovieId()};
        return getContentResolver().update(
                MovieEntry.CONTENT_URI,
                contentValues,
                MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                selectionArgs
        );
    }

    /**
     * Sets up and returns the default share intent for allowing the user to share
     * the selected movie
     *
     * @return the share intent
     */
    private Intent getDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET was deprecated as of API 21
        // This intent flag is important so that the activity is cleared from recent tasks
        //  whenever the activity is finished/closed
        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        Movie selectedMovie = getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE);
        if (selectedMovie != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.detail_share_subject).concat(" ")
                    .concat(selectedMovie.getTitle()));

            // Build text for share provider
            String sharedText = selectedMovie.getTitle()
                    .concat(" - ")
                    .concat(selectedMovie.getDescription())
                    .concat(" ")
                    .concat(getString(R.string.detail_share_hashtag));

            intent.putExtra(Intent.EXTRA_TEXT, sharedText);
        }
        return intent;
    }
}