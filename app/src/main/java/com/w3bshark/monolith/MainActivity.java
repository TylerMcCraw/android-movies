/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String PREF_SORT = "USER_PREF_SORT";
    private int selectedSort;
    private List<DrawerItem> mNavDrawerItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    public enum SortType {
        MostPopular("MP"), HighestRated("HR");

        private String sortType;

        private SortType(String s) {
            sortType = s;
        }

        public String getSortType() {
            return sortType;
        }
    }

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

    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        MainActivityFragment fragment;
        switch(position) {
            case 0:
                // Highlight the selected item
                mDrawerList.setItemChecked(position, true);
                selectedSort = position;
                fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.sortByMostPopular();
                break;
            case 1:
                // Highlight the selected item
                mDrawerList.setItemChecked(position, true);
                selectedSort = position;
                fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.sortByHighestRated();
                break;
            case 2:
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
