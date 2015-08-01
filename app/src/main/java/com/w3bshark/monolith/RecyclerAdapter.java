package com.w3bshark.monolith;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by w3bshark on 7/21/2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MovieViewHolder> {

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView appPhoto;

        MovieViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            appPhoto = (ImageView)itemView.findViewById(R.id.app_photo);
        }
    }

    Context context;
    List<Movie> movies;
    View.OnClickListener clickListener;

    RecyclerAdapter(Context context, List<Movie> movies, View.OnClickListener clickListener) {
        this.context = context;
        this.movies = movies;
        this.clickListener = clickListener;
    }

//    @Override
//    public int getItemViewType(int position) {
//        switch (position) {
//            case 0:
//                return 0;
//            case 1:
//                return 1;
//            default:
//                return 2;
//        }
//    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v;
//        if (i == 0) {
//            v = LayoutInflater.from(viewGroup.getContext())
//                    .inflate(R.layout.recycler_item_first_day, viewGroup, false);
//            //TODO: change the first day to a static view, cardview isn't going to work.
//            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            Display display = wm.getDefaultDisplay();
//            Point size = new Point();
//            display.getSize(size);
//            int height = size.y;
//            if (height == 0) {
//                height = 300;
//            }
//            else {
//                height = height * 2/5;
//            }
//            DayViewHolder dvh = new DayViewHolder(v);
//            dvh.cv.setMinimumHeight(height);
//            dvh.cv.setCardBackgroundColor(context.getResources().getColor(R.color.background));
//            TextView dayOfWeek = (TextView)dvh.cv.findViewById(R.id.dayOfWeek);
//            TextView weatherType = (TextView)dvh.cv.findViewById(R.id.weatherType);
//            TextView tempMax = (TextView)dvh.cv.findViewById(R.id.tempMax);
//            TextView tempMin = (TextView)dvh.cv.findViewById(R.id.tempMin);
//            dayOfWeek.setTextColor(context.getResources().getColor(R.color.white));
//            weatherType.setTextColor(context.getResources().getColor(R.color.white));
//            tempMax.setTextColor(context.getResources().getColor(R.color.white));
//            tempMin.setTextColor(context.getResources().getColor(R.color.white));
//            return new DayViewHolder(v);
//        }
//        else {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recycler_item, viewGroup, false);
            return new MovieViewHolder(v);
//        }
    }

    @Override
    public void onBindViewHolder(MovieViewHolder movieViewHolder, int i) {
        movieViewHolder.cv.setTag(movies.get(i).getTitle());
        Picasso.with(context).load("http://image.tmdb.org/t/p/w185".concat(movies.get(i).getImageCode())).into(movieViewHolder.appPhoto);
        movieViewHolder.appPhoto.setContentDescription(movies.get(i).getDescription());
//        movieViewHolder.appPhoto.setImageResource(
//                Util.getFeaturedWeatherIcon(movies.get(i).getIconCode())
//        );

        movieViewHolder.cv.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }
}
