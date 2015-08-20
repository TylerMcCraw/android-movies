/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class Util {
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
}
