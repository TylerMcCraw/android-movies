/*
 * Copyright (c) 2015. Tyler McCraw
 */

package com.w3bshark.monolith.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class that represents a movie
 * Representing movie data returned from TMDB API
 * This class handles marshalling of data via inheritance from Parcelable
 */
public class Movie implements Parcelable {

    String movieId;
    String title;
    String description;
    String imageCode;
    long popularity;
    Double voteAverage;
    String releaseDate;
    // favorite will be stored as a string of a date for when the movie was favorited
    String favorite;
    Bundle trailers;
    // Bundle key for storing trailers
    public static final String MOVIE_TRAILERS = "MOVIE_TRAILERS";

    public Movie() {
    }

    Movie(Parcel in) {
        this.movieId = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.imageCode = in.readString();
        this.popularity = in.readLong();
        this.voteAverage = in.readDouble();
        this.releaseDate = in.readString();
        this.favorite = in.readString();
        this.trailers = in.readBundle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(imageCode);
        dest.writeLong(popularity);
        dest.writeDouble(voteAverage);
        dest.writeString(releaseDate);
        dest.writeString(favorite);
        dest.writeBundle(trailers);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageCode() {
        return imageCode;
    }

    public void setImageCode(String imageCode) {
        this.imageCode = imageCode;
    }

    public long getPopularity() {
        return popularity;
    }

    public void setPopularity(long popularity) {
        this.popularity = popularity;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }

    public Bundle getTrailers() {
        return trailers;
    }

    public void setTrailers(Bundle trailers) {
        this.trailers = trailers;
    }
}
