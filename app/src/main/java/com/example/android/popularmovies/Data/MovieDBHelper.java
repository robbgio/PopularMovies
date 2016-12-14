package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDBHelper extends SQLiteOpenHelper {

    final static String DATABASE_NAME="Movies.db";
    private final static int DATABASE_VERSION=1;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE +
                "(" + MovieContract.MovieFavoritesTable._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.MovieFavoritesTable.MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieFavoritesTable.MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                MovieContract.MovieFavoritesTable.MOVIE_BACKDROP_PATH + " TEXT NOT NULL, " +
                MovieContract.MovieFavoritesTable.MOVIE_DATE + " TEXT NOT NULL, " +
                MovieContract.MovieFavoritesTable.MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                MovieContract.MovieFavoritesTable.MOVIE_VOTE_AVE + " REAL NOT NULL, " +
                MovieContract.MovieFavoritesTable.MOVIE_POSTER_IMAGE + " BLOB, " +
                MovieContract.MovieFavoritesTable.MOVIE_BACKDROP_IMAGE + " BLOB);";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS FAVORITES");
        onCreate(db);

    }
}