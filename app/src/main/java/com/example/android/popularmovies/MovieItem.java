package com.example.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

public class MovieItem implements Parcelable {
    String title;
    private String posterPath;
    private String backdropPath;
    private String releaseDate;
    private String overview;
    private Double voteAverage;
    private Boolean favorite=false;
    ImageView posterImageView;
    private int movieID;


    public MovieItem(String path, String t){
        posterPath = path;
        title = t;
    }
    private MovieItem(Parcel in){
        title = in.readString();
        posterPath = in.readString();
        backdropPath = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readDouble();
        overview = in.readString();
        movieID = in.readInt();
    }

    public void setPosterPath(String pp){
        posterPath = pp;
    }
    public void setBackdropPath(String bp){ backdropPath = bp;}

    public void setPosterImageView(Context context){  // used for caching Picasso images before scrolling
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.image_item, null);
        posterImageView = (ImageView) ll.findViewById(R.id.movie_image_view);
        String myPath = "http://image.tmdb.org/t/p/w185/" + posterPath;
        Uri myUri = Uri.parse(myPath);
        Picasso.with(context)
              .load(myUri)
              .into(posterImageView);
    }
    public void setTitle(String t){
        title = t;
    }
    public void setReleaseDate (String date) {releaseDate = date;}
    public void setOverview (String overview) {this.overview = overview;}
    public void setVoteAverage (Double average) {this.voteAverage= average;}
    public void setFavorite(Boolean fav){
        favorite = fav;
    }
    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }
    public int getMovieID() {return movieID;}

    public Boolean getFavorite(){ return favorite;}
    public String getPosterPath (){
        return posterPath;
    }
    public String getBackdropPath () {return backdropPath; }


    public ImageView getPosterImageView() {
        return posterImageView;
    }

    public String getTitle(){
        return title;
    }
    public String getReleaseDate() {
        return releaseDate;
    }

    public String getOverview() {
        return overview;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(posterPath);
        dest.writeString(backdropPath);
        dest.writeString(releaseDate);
        dest.writeDouble(voteAverage);
        dest.writeString(overview);
        dest.writeInt(movieID);
    }

    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>(){
        @Override
        public MovieItem createFromParcel(Parcel source) {
            return new MovieItem(source);
        }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };
}