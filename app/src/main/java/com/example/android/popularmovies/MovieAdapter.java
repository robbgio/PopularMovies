package com.example.android.popularmovies;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends ArrayAdapter<MovieItem> {
    private int posterWidth;
    private int posterHeight;
    private boolean mTab=false;
    private int displayWidth;

    public MovieAdapter(Activity context, List<MovieItem> movieItems) {
        super(context, 0, movieItems);

        // get width of screen in pixels
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            posterWidth = (displayWidth / 4); // four columns if landscape
        }
        else {
            posterWidth = (displayWidth / 2); // two columns if portrait
        }
        // Calculate height based on proportion of poster
        posterHeight = (int) (posterWidth * 1.5);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mTab){
            posterWidth = displayWidth/4;
            posterHeight = (int) (posterWidth * 1.5);
        }
        MovieItem movieItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_item, parent, false);
        }
        ImageView movieView = (ImageView) convertView.findViewById(R.id.movie_image_view);
        // set dimensions based on actual display size
        movieView.getLayoutParams().width= posterWidth;
        movieView.getLayoutParams().height= posterHeight;
        movieView.requestLayout();

        final String POSTER_PATH_BASE_URL = "http://image.tmdb.org/t/p/w185/";

        String myPath = POSTER_PATH_BASE_URL + movieItem.getPosterPath();
        // load poster into gridview at position
        Uri myUri = Uri.parse(myPath);
        Picasso.with(getContext())
                .load(myUri)
                .into(movieView);
        return convertView;
    }
    public void setmTab(boolean tab){
        mTab = tab;
    }
}
