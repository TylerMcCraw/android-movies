/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Serves as a general purpose, static helper class
 */
public class Util {

    /**
     * Returns the current screen height of the device
     * @param context application context
     * @return the size of the screen height (integer)
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
}
