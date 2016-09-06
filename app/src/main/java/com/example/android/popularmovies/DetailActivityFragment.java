package com.example.android.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmovies.Data.MovieDBHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailActivityFragment extends Fragment implements View.OnClickListener {
    private String movieTitle;
    private String releaseDate;
    private String releaseDateFormatted;
    private Double voteAverage;
    private String overview;
    MovieItem detailMovie;
    Button toggleButton;
    private MovieDBHelper mOpenHelper;
    private int movieID;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        boolean landscape;
        // two different layouts based on orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            landscape = true;
            rootView = inflater.inflate(R.layout.fragment_detail_landscape, container, false);
        }
        else {
            landscape = false;
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        }
        toggleButton = (Button) rootView.findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(this);
        // get actual display dimensions in pixels and set backdrop width based on that
        Display display = this.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x;
        int backdropWidth;
        if (landscape){
            backdropWidth = displayWidth /2;
        }
        else { backdropWidth = displayWidth; }

        // calculates height based on proportion of backdrop poster
        int backdropHeight = (int) (backdropWidth * 0.562);
        // receive all movie items and position of selected poster
        Intent intent = getActivity().getIntent();
        if (intent !=null && intent.hasExtra("Movie Items") && intent.hasExtra("Position")){
            int position = intent.getExtras().getInt("Position");
            ArrayList<MovieItem> movieItems = intent.getExtras().getParcelableArrayList("Movie Items");

            detailMovie = null;
            // get selected movie
            if (movieItems != null) {
                detailMovie = movieItems.get(position);
            }
            // get info from move item fields
            if (detailMovie != null) {
                movieTitle = detailMovie.getTitle();
                releaseDate = detailMovie.getReleaseDate();
                releaseDateFormatted = formatDate(releaseDate);
                voteAverage = detailMovie.getVoteAverage();
                overview = detailMovie.getOverview();
                movieID = detailMovie.getMovieID();
            }

            mOpenHelper = new MovieDBHelper(getContext());
            if (mOpenHelper.moviePresent(detailMovie.getMovieID())) {
                detailMovie.setFavorite(true);
                ((Button) rootView.findViewById(R.id.toggleButton)).setText("Favorite");
            }
            else {
                detailMovie.setFavorite(false);
                ((Button) rootView.findViewById(R.id.toggleButton)).setText("Set as Favorite");
            }
            // set backdrop size which is partly determined by orientation
            ImageView detailBackdrop = (ImageView) rootView.findViewById(R.id.backdrop_view);
            detailBackdrop.getLayoutParams().width= backdropWidth;
            detailBackdrop.getLayoutParams().height = backdropHeight;
            detailBackdrop.requestLayout();

            // load backdrop movie poster
            final String BACKDROP_PATH_BASE_URL = "http://image.tmdb.org/t/p/w342/";

            if (movieItems!=null) {
                String myPath = BACKDROP_PATH_BASE_URL + movieItems.get(position).getBackdropPath();

                Uri myUri = Uri.parse(myPath);
                Picasso.with(getContext())
                        .load(myUri)
                        .into(detailBackdrop);
            }
            // set title and release date
            ((TextView) rootView.findViewById(R.id.title_text_view)).setText(movieTitle);
            ((TextView) rootView.findViewById(R.id.release_date_text_view)).setText(releaseDateFormatted);

            // load stars base on vote average
            LinearLayout starLayout = (LinearLayout) rootView.findViewById(R.id.star_layout);

            Double voteAveAbsolute = Math.floor(voteAverage);
            Double voteAvePart=voteAverage - voteAveAbsolute;

            for (int i=1;i<=10;i++) {
                ImageView star = new ImageView(getActivity());

                star.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                star.getLayoutParams().height = getActivity().getResources().getDimensionPixelSize(R.dimen.star_height);
                star.getLayoutParams().width = getActivity().getResources().getDimensionPixelSize(R.dimen.star_width);

                if (i <= voteAverage) star.setImageResource(R.drawable.starfull);
                else if (i >= (voteAverage + 1)) star.setImageResource(R.drawable.starempty);
                else if (voteAvePart < 0.25) star.setImageResource(R.drawable.starempty);
                else if (voteAvePart >= 0.75) star.setImageResource(R.drawable.starfull);
                else star.setImageResource(R.drawable.starhalf);

                starLayout.addView(star);
            }
            // format and set vote average
            String voteAverageText = "("+ voteAverage.toString() +"/10)";
            ((TextView) rootView.findViewById(R.id.vote_average_text_view)).setText(voteAverageText);

            // indent paragraph of synopsis
            overview = "    " + overview;
            ((TextView) rootView.findViewById(R.id.overview_text_view)).setText(overview);
        }
        return rootView;
    }

    private String formatDate(String releaseDate) { // convert from yyyy-mm-dd to Month day, year
        String year;
        String monthNumber;
        String monthName="Month";
        String day;

        String[] parts = releaseDate.split("\\-");
        year = parts[0];
        monthNumber = parts[1];
        day = parts[2];

        switch (Integer.parseInt(monthNumber)){
            case 1: monthName = "January";
                break;
            case 2: monthName = "February";
                break;
            case 3: monthName = "March";
                break;
            case 4: monthName = "April";
                break;
            case 5: monthName = "May";
                break;
            case 6: monthName = "June";
                break;
            case 7: monthName = "July";
                break;
            case 8: monthName = "August";
                break;
            case 9: monthName = "September";
                break;
            case 10: monthName = "October";
                break;
            case 11: monthName = "November";
                break;
            case 12: monthName = "December";
                break;
        }
        // convert from yyyy-mm-dd to Month day, year
        releaseDate = monthName + " " + day + ", " + year;
        return releaseDate;
    }
    @Override
    public void onClick(View v) {
        toggleFavorite(v);
    }

    public void toggleFavorite (View view){
        mOpenHelper = new MovieDBHelper(getContext());
        Button b = (Button) view;
        if (!detailMovie.getFavorite()){
            detailMovie.setFavorite(true);
            b.setText("Favorite");
            mOpenHelper.insertMovies(detailMovie.getMovieID(), detailMovie.getTitle(), detailMovie.getPosterPath(),
                    detailMovie.getBackdropPath(), detailMovie.getReleaseDate(),
                    detailMovie.getOverview(), detailMovie.getVoteAverage());
        }
        else {
            detailMovie.setFavorite(false);
            b.setText("Save as Favorite");
            mOpenHelper.deleteMovies(detailMovie.getMovieID());
        }
    }
}
