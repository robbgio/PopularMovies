package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper mOpenHelper;

    private static final int FAVORITES = 100;
    private static final int FAVORITES_ID = 200;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE, FAVORITES);
        matcher.addURI(authority, MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE + "/#", FAVORITES_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case FAVORITES:{
                return MovieContract.MovieFavoritesTable.CONTENT_DIR_TYPE;
            }
            case FAVORITES_ID:{
                return MovieContract.MovieFavoritesTable.CONTENT_ITEM_TYPE;
            }
            default:{
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
            }
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        switch(sUriMatcher.match(uri)){
            case FAVORITES:{
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return returnCursor;
            }
            case FAVORITES_ID:{
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
                        projection,
                        MovieContract.MovieFavoritesTable._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return returnCursor;
            }
            default:{
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
            }
        }

    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int recordsDeleted;
        switch (match) {
            case FAVORITES:
                recordsDeleted = db.delete(
                        MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
                        selection, selectionArgs);
                break;
            case FAVORITES_ID:
                recordsDeleted = db.delete(MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
                        MovieContract.MovieFavoritesTable._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
        return recordsDeleted;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch(match){
            case FAVORITES:
                db.beginTransaction();
                int recordsInserted = 0;
                try{
                    for(ContentValues value : values){
                        if (value == null){
                            throw new IllegalArgumentException("Null Content Values");
                        }
                        long id = -1;
                        try{
                            id = db.insertOrThrow(MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
                                    null, value);
                        }
                        catch (SQLiteConstraintException e) {
                            throw new SQLiteConstraintException("Values don't match requirements");
                        }
                        if (id != -1){
                            recordsInserted++;
                        }
                    }
                    if (recordsInserted > 0){
                        db.setTransactionSuccessful();
                    }
                } finally {
                    db.endTransaction();
                }
                if (recordsInserted > 0) {
                    if (getContext()!=null) {
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                }
                return recordsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int recordsUpdated=0;

        if (values == null){
            throw new IllegalArgumentException("Null Content Values");
        }
        switch(sUriMatcher.match(uri)){
            case FAVORITES:{
                recordsUpdated = db.update(MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case FAVORITES_ID: {
                recordsUpdated = db.update(MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
                        values,
                        MovieContract.MovieFavoritesTable._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default:{
                throw new UnsupportedOperationException("Unsupported Uri: " + uri);
            }

        }
        if (recordsUpdated > 0 ){
            if (getContext()!=null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return recordsUpdated;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        switch(sUriMatcher.match(uri)){
            case FAVORITES:{
                long id = db.insert(MovieContract.MovieFavoritesTable.MOVIE_FAVORITES_TABLE,
                        null, values);
                if (id > 0){
                    returnUri = MovieContract.MovieFavoritesTable.buildFavoritesUri(id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            default:{
                throw new UnsupportedOperationException("Unsupported Uri: " + uri);
            }
        }
        if (getContext()!=null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }
}