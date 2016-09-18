package com.example.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.example.android.popularmovies.Data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by rgiordano on 8/31/2016.
 */
public class FavoritesAdapter extends CursorAdapter {

    private int mDisplayWidth;
    private Context mContext;
    private static int sLoaderID;
    private int posterWidth;
    private int posterHeight;
    private boolean mTab=false;

    public void setmTab(boolean tab) {
        mTab = tab;
    }

    public static class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.movie_image_view);
        }
    }
        public FavoritesAdapter (Activity context, Cursor c, int flags, int loaderID){
            super(context, c, flags);
            // get width of screen in pixels
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mDisplayWidth = size.x;

            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                posterWidth = (mDisplayWidth / 4); // four columns if landscape
            }
            else {
                if (mTab) {
                    posterWidth = (mDisplayWidth / 4); // fourth of display width if portrait on tablet
                }
                else {
                    posterWidth = (mDisplayWidth / 2); // two columns if portrait on phone
                }
            }
            // Calculate height based on proportion of poster
            posterHeight = (int) (posterWidth * 1.5);
            mContext = context;
            sLoaderID = loaderID;
        }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutID = R.layout.image_item;
        View view = LayoutInflater.from(context).inflate(layoutID, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if (mTab){
            posterWidth = mDisplayWidth/4;
            posterHeight = (int) (posterWidth * 1.5);
        }

        final String POSTER_PATH_BASE_URL = "http://image.tmdb.org/t/p/w185/";

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int posterIndex = cursor.getColumnIndex(MovieContract.MovieFavoritesTable.MOVIE_POSTER_PATH);
        String posterPath = cursor.getString(posterIndex);

        String myPath = POSTER_PATH_BASE_URL + posterPath;
        Uri myUri = Uri.parse(myPath);

        viewHolder.imageView.getLayoutParams().width= posterWidth;
        viewHolder.imageView.getLayoutParams().height= posterHeight;
        viewHolder.imageView.requestLayout();
        Picasso.with(context)
                .load(myUri)
                .into(viewHolder.imageView);
    }
}
