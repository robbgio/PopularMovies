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
                MovieContract.MovieFavoritesTable.MOVIE_VOTE_AVE + " REAL NOT NULL);";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS FAVORITES");
        onCreate(db);

    }

    //public boolean insertMovies (int movieID, String movieTitle, String moviePoster, String movieBackdrop, String movieDate,
    //                             String movieOverview, Double movieVote){
    //    SQLiteDatabase db = this.getWritableDatabase();
    //    ContentValues contentValues = new ContentValues();
    //    contentValues.put(MovieContract.MovieFavoritesTable._ID,movieID);
    //    contentValues.put(MovieContract.MovieFavoritesTable.MOVIE_TITLE,movieTitle);
    //    contentValues.put(MovieContract.MovieFavoritesTable.MOVIE_POSTER_PATH,moviePoster);
    //    contentValues.put(MovieContract.MovieFavoritesTable.MOVIE_BACKDROP_PATH,movieBackdrop);
    //    contentValues.put(MovieContract.MovieFavoritesTable.MOVIE_DATE,movieDate);
    //    contentValues.put(MovieContract.MovieFavoritesTable.MOVIE_OVERVIEW,movieOverview);
    //    contentValues.put(MovieContract.MovieFavoritesTable.MOVIE_VOTE_AVE,movieVote);

    //    long result = db.replace(MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,null,contentValues);
    //    return result != -1;
    //}
    //public boolean deleteMovies (int movieID){
    //    SQLiteDatabase db = this.getWritableDatabase();
    //    int result = db.delete(MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
    //            MovieContract.MovieFavoritesTable._ID + "=?", new String[] {Integer.toString(movieID)});
    //   return result > 0;
    //}
    // public boolean moviePresent (int movieID){
    //    String sMovieID = Integer.toString(movieID);
    //    SQLiteDatabase db = this.getReadableDatabase();
    //    String[] columns = { MovieContract.MovieFavoritesTable._ID };
    //    String selection = MovieContract.MovieFavoritesTable._ID + "=?";
    //    String[] selectionArgs = {sMovieID};
    //    String limit = "1";
    //    Cursor cursor = db.query(MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
    //            columns, selection, selectionArgs, null, null, null, limit);
    //    boolean present = (cursor.getCount() > 0);
    //    cursor.close();
    //    return present;
    //}

    //public Cursor getAllFavorites (){
    //    SQLiteDatabase db = this.getReadableDatabase();

    //    Cursor cursor = db.rawQuery("select * from " + MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,null);
    //    return cursor;
    //}
}