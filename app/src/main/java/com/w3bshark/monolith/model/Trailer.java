/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Trailer implements Parcelable {

    String name;
    String videoPath;
    String videoSite;

    public Trailer() {
    }

    Trailer(Parcel in) {
        this.name = in.readString();
        this.videoPath = in.readString();
        this.videoSite = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(videoPath);
        dest.writeString(videoSite);
    }

    public static final Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoSite() {
        return videoSite;
    }

    public void setVideoSite(String videoSite) {
        this.videoSite = videoSite;
    }
}