/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.w3bshark.monolith.R;
import com.w3bshark.monolith.fragments.DetailActivityFragment;
import com.w3bshark.monolith.fragments.MainActivityFragment;
import com.w3bshark.monolith.model.DrawerItem;
import com.w3bshark.monolith.model.Movie;
import com.w3bshark.monolith.widget.DrawerItemCustomAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * An AppCompatActivity that presents a grid of movies sorted by either "most popular" or
 * "highest rated". Movie data is pulled asynchronously from the TheMovieDatabase API.
 * Movie poser images are pre-fetched via Picasso using URLs from the TMDB API.
 */
public class MainActivity extends AppCompatActivity {

    // Used for logging against this class
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    // Key for storing shared pref setting of default sort
    public static final String PREF_SORT = "USER_PREF_SORT";
    // Currently selected sort while app is running
    private int selectedSort;
    // List of navigation drawer items
    private List<DrawerItem> mNavDrawerItems;
    // View of navigation drawer items
    private ListView mDrawerList;
    // Layout of Navigation Drawer
    private DrawerLayout mDrawerLayout;
    // Toggle for keeping track of action bar nav drawer state
    private ActionBarDrawerToggle mDrawerToggle;
    // Used for social sharing
    private ShareActionProvider mShareActionProvider;

    // Handle Master-Detail view
    public static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;
    public static final String BUNDLE_TWO_PANE = "BUNDLE_TWO_PANE";

    // Enum for keeping track of available sorts
    public enum SortType {
        MostPopular("MP"), HighestRated("HR"), Favorites("FAV");
        private String sortType;

        SortType(String s) {
            sortType = s;
        }

        public String getSortType() {
            return sortType;
        }
    }

    /**
     * Handle creation of the movie list activity, navigation drawer, and the activity's fragment
     *
     * @param savedInstanceState instance state of the application activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // TODO: Comment this out when releasing the app
//        Stetho.initializeWithDefaults(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        initializeNavDrawerData();
        DrawerItemCustomAdapter drawerAdapter =
                new DrawerItemCustomAdapter(this, R.layout.navdrawer_item_row, mNavDrawerItems);
        mDrawerList.setAdapter(drawerAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        // Set the initial sort to either "Most Popular" by default or the user's set preference
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defValPref = "";
        String userSortPref = settings.getString(PREF_SORT, defValPref);
        if (userSortPref.isEmpty() || userSortPref.equals(SortType.MostPopular.getSortType())) {
            SharedPreferences.Editor preferenceEditor = settings.edit();
            preferenceEditor.putString(PREF_SORT, SortType.MostPopular.getSortType());
            selectedSort = 0;
        } else {
            selectedSort = 1;
        }
        mDrawerList.setItemChecked(selectedSort, true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, null, R.string.nav_drawer_open, R.string.nav_drawer_close
        );

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (findViewById(R.id.detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        if (savedInstanceState == null) {
            MainActivityFragment mainActFrag = new MainActivityFragment();
            final Bundle bundle = new Bundle();
            bundle.putBoolean(BUNDLE_TWO_PANE, mTwoPane);
            mainActFrag.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, mainActFrag)
                    .commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if ( null != df ) {
//            df.onMovieChanged(location);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A listener for handling user selection of navigation drawer items
     */
    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Handle user selection of navigation drawer items
     *
     * @param position the position number of the selected item
     */
    private void selectItem(int position) {
        MainActivityFragment fragment;
        switch (position) {
            case 0:
                // This is the Sort By "Most Popular" option
                // Highlight the selected item
                mDrawerList.setItemChecked(position, true);
                selectedSort = position;
                fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
                fragment.sortByMostPopular();
                break;
            case 1:
                // This is the Sort By "Highest Rated" option
                // Highlight the selected item
                mDrawerList.setItemChecked(position, true);
                selectedSort = position;
                fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
                fragment.sortByHighestRated();
                break;
            case 2:
                // This is the Show "Favorites" option
                // Highlight the selected item
                mDrawerList.setItemChecked(position, true);
                selectedSort = position;
                fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
                fragment.showFavorites();
                break;
            case 3:
                // This is the Settings option
                mDrawerList.setItemChecked(position, false);
                mDrawerList.setItemChecked(selectedSort, true);
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
//            case 3:
//                // This is the Settings option
//                mDrawerList.setItemChecked(position, false);
//                mDrawerList.setItemChecked(selectedSort, true);
//                Intent settingsIntent = new Intent(this, SettingsActivity.class);
//                startActivity(settingsIntent);
//                break;
            default:
                break;
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * Set up the navigation drawer to display sorting options for sorting the movie list
     */
    private void initializeNavDrawerData() {
        if (mNavDrawerItems == null) {
            mNavDrawerItems = new ArrayList<>();
        }
        mNavDrawerItems.add(new DrawerItem(R.drawable.ic_action_sort_by_size, getString(R.string.nav_drawer_most_popular)));
        mNavDrawerItems.add(new DrawerItem(R.drawable.ic_action_sort_by_size, getString(R.string.nav_drawer_highest_rated)));
        mNavDrawerItems.add(new DrawerItem(R.drawable.ic_bookmark_outline_plus, getString(R.string.nav_drawer_favorites)));

        //TODO: Add Settings screen back in for later use
//        mNavDrawerItems.add(new DrawerItem(R.drawable.ic_action_settings, getString(R.string.nav_drawer_settings)));
    }


    /**
     * Overridden method to display the share action button in the options menu
     *
     * @param menu the options menu
     * @return boolean - if options were created for the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //TODO: Set up shareprovider for main activity when we're in a master-detail view
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_detail, menu);

//        Movie selectedMovie = getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE);
//        if (selectedMovie.getFavorite() != null && !selectedMovie.getFavorite().isEmpty()) {
//            //Locate MenuItem for Favorite/Bookmark button
//            MenuItem favItem = menu.findItem(R.id.menu_item_bookmark);
//            favItem.setIcon(R.drawable.bookmark_plus);
//            favItem.setChecked(true);
//        }

//        // Locate MenuItem with ShareActionProvider
//        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
//        // Fetch and store ShareActionProvider
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

//        if (mShareActionProvider != null) {
//            mShareActionProvider.setShareIntent(getDefaultShareIntent());
//        } else {
//            Log.d(LOG_TAG, "ShareActionProvider is null");
//            return false;
//        }
        return true;
    }

    /**
     * Sets up and returns the default share intent for allowing the user to share
     * the selected movie
     *
     * @return the share intent
     */
    private Intent getDefaultShareIntent() {
        //TODO: Set up shareprovider for main activity when we're in a master-detail view
        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("text/plain");
//        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//        // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET was deprecated as of API 21
//        // This intent flag is important so that the activity is cleared from recent tasks
//        //  whenever the activity is finished/closed
//        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//        } else {
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        }
//        Movie selectedMovie = getIntent().getParcelableExtra(DetailActivity.EXTRASCURRENTMOVIE);
//        if (selectedMovie != null) {
//            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.detail_share_subject).concat(" ")
//                    .concat(selectedMovie.getTitle()));
//
//            // Build text for share provider
//            String sharedText = selectedMovie.getTitle()
//                    .concat(" - ")
//                    .concat(selectedMovie.getDescription())
//                    .concat(" ")
//                    .concat(getString(R.string.detail_share_hashtag));
//
//            intent.putExtra(Intent.EXTRA_TEXT, sharedText);
//        }
        return intent;
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
}
