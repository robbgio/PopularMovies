package com.example.android.popularmovies.Data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rgiordano on 8/31/2016.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.exmample.android.popularmovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content;//" + CONTENT_AUTHORITY);

    public static final class MovieFavoritesTable implements BaseColumns{
        public static final String MOVIE_FAVORITES_TABLE = "favorites";

        public static final String _ID = "_id";
        public static final String MOVIE_TITLE = "movie_title";
        public static final String MOVIE_POSTER_PATH = "movie_poster_path";
        public static final String MOVIE_BACKDROP_PATH ="move_backdrop_path";
        public static final String MOVIE_DATE = "movie_date";
        public static final String MOVIE_OVERVIEW = "movie_overview";
        public static final String MOVIE_VOTE_AVE = "movie_vote_ave";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MOVIE_FAVORITES_TABLE).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + MOVIE_FAVORITES_TABLE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + MOVIE_FAVORITES_TABLE;
        public static Uri buildFavoritesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
