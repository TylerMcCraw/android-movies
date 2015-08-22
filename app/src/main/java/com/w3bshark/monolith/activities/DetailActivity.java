/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.activities;

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
import com.w3bshark.monolith.fragments.DetailActivityFragment;
import com.w3bshark.monolith.model.Movie;

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
     * @param savedInstanceState the instancestate of the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailActivityFragment())
                    .commit();
        }
    }

    /**
     * Call to update the share intent for custom use
     * @param shareIntent the intent used by the shareactionprovider
     */
    public void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /**
     * Overridden method to display the share action button in the options menu
     * @param menu the options menu
     * @return boolean - if options were created for the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(getDefaultShareIntent());
        } else {
            Log.d(LOG_TAG, "ShareActionProvider is null");
            return false;
        }
        return true;
    }

    /**
     * Sets up and returns the default share intent for allowing the user to share
     * the selected movie
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
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.detail_share_subject).concat(selectedMovie.getTitle()));

        // Build text for share provider
        String sharedText = selectedMovie.getTitle()
                .concat(" - ")
                .concat(selectedMovie.getDescription())
                .concat(getString(R.string.detail_share_hashtag));

        intent.putExtra(Intent.EXTRA_TEXT, sharedText);
        return intent;
    }
}
