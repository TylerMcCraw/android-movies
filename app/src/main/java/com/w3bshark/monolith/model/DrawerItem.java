/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.model;

/**
 * Model class that represents an item in the Navigation Drawer
 */
public class DrawerItem {

    public int icon;
    public String name;

    public DrawerItem(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }
}
