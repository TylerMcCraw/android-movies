/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Custom GridLayoutManager class for pre-caching data (mostly for images) binded to the
 * recycler view in MainActivityFragment
 * Use of this design was based on:
 * <a href:"https://androiddevx.wordpress.com/2014/12/05/recycler-view-pre-cache-views/">
 * "Recycler View - pre-cache views" by Ovidiu</a>
 */
public class PreCachingGridLayoutManager extends GridLayoutManager {

    // A fallback pixel amount for space to handle pre-caching
    private static final int DEFAULT_EXTRA_LAYOUT_SPACE = 600;
    // The current extra layout space for pre-caching
    private int extraLayoutSpace = -1;

    public PreCachingGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreCachingGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    public void setExtraLayoutSpace(int extraLayoutSpace) {
        this.extraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        if (extraLayoutSpace > 0) {
            return extraLayoutSpace;
        }
        return DEFAULT_EXTRA_LAYOUT_SPACE;
    }
}
