/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {

    String id;
    String author;
    String content;

    public Review() {
    }

    Review(Parcel in) {
        this.id = in.readString();
        this.author = in.readString();
        this.content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(author);
        dest.writeString(content);
    }

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
