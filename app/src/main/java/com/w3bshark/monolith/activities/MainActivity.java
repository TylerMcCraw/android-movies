/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.w3bshark.monolith.R;
import com.w3bshark.monolith.fragments.MainActivityFragment;
import com.w3bshark.monolith.model.DrawerItem;
import com.w3bshark.monolith.widget.DrawerItemCustomAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * An AppCompatActivity that presents a grid of movies sorted by either "most popular" or
 * "highest rated". Movie data is pulled asynchronously from the TheMovieDatabase API.
 * Movie poser images are pre-fetched via Picasso using URLs from the TMDB API.
 */
public class MainActivity extends AppCompatActivity {

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
    // Enum for keeping track of available sorts
    public enum SortType {
        MostPopular("MP"), HighestRated("HR");
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
     * @param savedInstanceState instance state of the application activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            preferenceEditor.putString(PREF_SORT,SortType.MostPopular.getSortType());
            selectedSort = 0;
        }
        else {
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

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainActivityFragment())
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
     * @param position the position number of the selected item
     */
    private void selectItem(int position) {
        MainActivityFragment fragment;
        switch(position) {
            case 0:
                // This is the Sort By "Most Popular" option
                // Highlight the selected item
                mDrawerList.setItemChecked(position, true);
                selectedSort = position;
                fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.sortByMostPopular();
                break;
            case 1:
                // This is the Sort By "Highest Rated" option
                // Highlight the selected item
                mDrawerList.setItemChecked(position, true);
                selectedSort = position;
                fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.sortByHighestRated();
                break;
            case 2:
                // This is the Settings option
                mDrawerList.setItemChecked(position, false);
                mDrawerList.setItemChecked(selectedSort, true);
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
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

        //TODO: Add Settings screen back in for later use
//        mNavDrawerItems.add(new DrawerItem(R.drawable.ic_action_settings, getString(R.string.nav_drawer_settings)));
    }
}
