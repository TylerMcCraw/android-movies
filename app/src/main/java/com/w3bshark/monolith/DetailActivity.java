/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRASCURRENTMOVIE = "EXTRAS_CURRENT_MOVIE";
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    //Used for social sharing
    private ShareActionProvider mShareActionProvider;

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

    // Call to update the share intent for custom use
    public void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Returns the default share intent
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
