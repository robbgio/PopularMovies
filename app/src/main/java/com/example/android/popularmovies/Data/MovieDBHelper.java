package com.example.android.popularmovies.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by rgiordano on 8/26/2016.
 */
public class MovieDBHelper extends SQLiteOpenHelper {

    final static String DATABASE_NAME="Movies.db";
    private final static int DATABASE_VERSION=1;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE Favorites (MOVIEID INTEGER PRIMARY KEY, " +
                "MOVIETITLE TEXT NOT NULL, " +
                "MOVIEPOSTERPATH TEXT NOT NULL, " +
                "MOVIEBACKDROPPATH TEXT NOT NULL, " +
                "MOVIEDATE TEXT NOT NULL, " +
                "MOVIEOVERVIEW TEXT NOT NULL, " +
                "MOVIEVOTEAVE REAL NOT NULL);";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS FAVORITES");
        onCreate(db);

    }

    public boolean insertMovies (int movieID, String movieTitle, String moviePoster, String movieBackdrop, String movieDate,
                                 String movieOverview, Double movieVote){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("MOVIEID",movieID);
        contentValues.put("MOVIETITLE",movieTitle);
        contentValues.put("MOVIEPOSTERPATH",moviePoster);
        contentValues.put("MOVIEBACKDROPPATH",movieBackdrop);
        contentValues.put("MOVIEDATE",movieDate);
        contentValues.put("MOVIEOVERVIEW",movieOverview);
        contentValues.put("MOVIEVOTEAVE",movieVote);

        long result = db.replace("FAVORITES",null,contentValues);
        return result != -1;
    }
    public boolean deleteMovies (int movieID){
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("FAVORITES","MOVIEID=?", new String[] {Integer.toString(movieID)});
        return result > 0;
    }
    public boolean moviePresent (int movieID){
        String sMovieID = Integer.toString(movieID);
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { "MOVIEID" };
        String selection = "MOVIEID=?";
        String[] selectionArgs = {sMovieID};
        String limit = "1";

        Cursor cursor = db.query("FAVORITES", columns, selection, selectionArgs, null, null, null, limit);
        boolean present = (cursor.getCount() > 0);
        cursor.close();
        return present;
    }
}