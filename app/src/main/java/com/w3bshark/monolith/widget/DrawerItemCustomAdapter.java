/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.widget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.w3bshark.monolith.R;
import com.w3bshark.monolith.model.DrawerItem;

import java.util.List;

/**
 * Adapter for Navigation Drawer used in MainActivity
 */
public class DrawerItemCustomAdapter extends ArrayAdapter<DrawerItem> {

    Context mContext;
    // Resource ID of container for navigation drawer items
    int layoutResourceId;
    List<DrawerItem> data = null;

    /**
     * View holder for navigation drawer items
     */
    public static class DrawerItemViewHolder {
        ImageView icon;
        TextView name;

        DrawerItemViewHolder(View itemView) {
            icon = (ImageView) itemView.findViewById(R.id.navdrawer_item_icon);
            name = (TextView) itemView.findViewById(R.id.navdrawer_item_name);
        }
    }

    public DrawerItemCustomAdapter(Context mContext, int layoutResourceId, List<DrawerItem> data) {
        super(mContext, layoutResourceId, data);
        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    /**
     * Bind the data with the navigation drawer item view
     *
     * @param position    the position number of the item in the navigation drawer
     * @param convertView the view of the navigation drawer item
     * @param parent      the parent view of the navigation drawer item
     * @return the manipulated view of the navigation drawer item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View drawerItem = convertView;
        DrawerItemViewHolder holder;

        if (drawerItem == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            drawerItem = inflater.inflate(layoutResourceId, null);

            holder = new DrawerItemViewHolder(drawerItem);
            drawerItem.setTag(holder);
        } else {
            // If the view already exists, then get the holder instance from the view
            holder = (DrawerItemViewHolder) drawerItem.getTag();
        }

        if (data.size() > 0 && data.get(position) != null) {
            if (holder.icon != null) {
                holder.icon.setImageResource(data.get(position).icon);
            }
            if (holder.name != null) {
                holder.name.setText(data.get(position).name);
            }
        }

        return drawerItem;
    }
}
