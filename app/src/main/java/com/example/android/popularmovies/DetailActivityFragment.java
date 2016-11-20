package com.example.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.popularmovies.Data.MovieDBHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DetailActivityFragment extends Fragment {
    private String movieTitle;
    private String releaseDate;
    private String releaseDateFormatted;
    private Double voteAverage;
    private String overview;
    MovieItem detailMovie;
    Button toggleButton;
    private MovieDBHelper mOpenHelper;
    private int movieID;
    int mPosition;
    int displayWidth;
    ArrayList<MovieItem> movieItems;
    private boolean mTab=false;
    private LinearLayout mTrailerLayout;
    private ArrayList<Trailer> mTrailerList;
    private View.OnClickListener mTrailerListener;
    private View.OnClickListener mFavoriteListener;
    private View.OnClickListener mReviewsListener;
    private boolean mLandscape;
    private int mReviewsCount;
    private ArrayList<Review> mReviews;
    private ImageView mHeartView;
    private LinearLayout mReviewLayout;
    public static final int SHORTREVIEW=1;
    public static final int TRUNCATEDREVIEW=2;
    public static final int FULLREVIEW=3;
    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();
        if (arguments!=null) mTab=true;

        // if no movie selected, inflate blank fragment
        if (intent !=null && !intent.hasExtra("Movie Items") && arguments==null) {
            rootView = inflater.inflate(R.layout.fragment_blank, container, false);
            return rootView;
        }
        // two different layouts based on orientation
        if (!mTab && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            mLandscape = true;
            rootView = inflater.inflate(R.layout.fragment_detail_landscape, container, false);
        }
        else {
            mLandscape = false;
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        }
        toggleButton = (Button) rootView.findViewById(R.id.toggleButton);
        mFavoriteListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite(v);
            }
        };

        toggleButton.setOnClickListener(mFavoriteListener);

        mHeartView = (ImageView) rootView.findViewById(R.id.heart);
        mHeartView.setOnClickListener(mFavoriteListener);

        // get actual display dimensions in pixels and set backdrop width based on that
        Display display = this.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        displayWidth = size.x;
        int backdropWidth;
        if (mLandscape || mTab){
            backdropWidth = displayWidth /2;
        }
        else { backdropWidth = displayWidth; }

        // calculates height based on proportion of backdrop poster
        int backdropHeight = (int) (backdropWidth * 0.562);
        // receive all movie items and position of selected poster

        //for phone
        if (intent !=null && intent.hasExtra("Movie Items") && intent.hasExtra("Position")) {

            mPosition = intent.getExtras().getInt("Position");
            movieItems = intent.getExtras().getParcelableArrayList("Movie Items");
        }
        //for tablet
        if (arguments!=null) {
            mPosition=arguments.getInt("Position");
            movieItems=arguments.getParcelableArrayList("Movie Items");
        }
        //if position is -1 then no movie selected
        if (mPosition==-1) {
            rootView = inflater.inflate(R.layout.fragment_blank, container, false);
            return rootView;
        }

        detailMovie = null;
        // get selected movie
        if (movieItems != null) {
            detailMovie = movieItems.get(mPosition);
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
            ((Button) rootView.findViewById(R.id.toggleButton)).setText("Remove from Favorites");
            mHeartView.setImageResource(R.drawable.heart);
        } else {
            detailMovie.setFavorite(false);
            ((Button) rootView.findViewById(R.id.toggleButton)).setText("Save as Favorite");
            mHeartView.setImageResource(R.drawable.emptyheart);
        }

        // set backdrop size which is partly determined by orientation
        ImageView detailBackdrop = (ImageView) rootView.findViewById(R.id.backdrop_view);
        detailBackdrop.getLayoutParams().width= backdropWidth;
        detailBackdrop.getLayoutParams().height = backdropHeight;
        detailBackdrop.requestLayout();

        // load backdrop movie poster
        final String BACKDROP_PATH_BASE_URL = "http://image.tmdb.org/t/p/w342/";

        if (movieItems!=null) {
            String myPath = BACKDROP_PATH_BASE_URL + movieItems.get(mPosition).getBackdropPath();

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

        // Trailers...
        mTrailerLayout = (LinearLayout) rootView.findViewById(R.id.trailer_layout);
        mReviewLayout = (LinearLayout) rootView.findViewById(R.id.reviews);

        //mTextReviewAuthor = (TextView) rootView.findViewById(R.id.text_review_author);
        //mTextReviewContent = (TextView) rootView.findViewById(R.id.text_review_content);

        new GetTrailerList().execute(Integer.toString(movieID));
        new GetReviews().execute(Integer.toString(movieID));

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
    public void toggleFavorite (View view){
        mOpenHelper = new MovieDBHelper(getContext());
        if (!detailMovie.getFavorite()){
            detailMovie.setFavorite(true);
            toggleButton.setText("Remove from Favorites");
            mHeartView.setImageResource(R.drawable.heart);
            mOpenHelper.insertMovies(detailMovie.getMovieID(), detailMovie.getTitle(), detailMovie.getPosterPath(),
                    detailMovie.getBackdropPath(), detailMovie.getReleaseDate(),
                    detailMovie.getOverview(), detailMovie.getVoteAverage());
        }
        else {
            detailMovie.setFavorite(false);
            toggleButton.setText("Save as Favorite");
            mHeartView.setImageResource(R.drawable.emptyheart);
            mOpenHelper.deleteMovies(detailMovie.getMovieID());
            // remove movie from Grid when removed from favorites
            if (mTab) ((Callback) getActivity()).updateGrid(movieItems, mPosition);
        }
    }
    public class GetTrailerList extends AsyncTask<String, Void, ArrayList<Trailer>>{
        @Override
        protected ArrayList<Trailer> doInBackground(String... params) {
            if (params.length == 0){
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String trailersJsonStr;
            String dbID = params[0]; //video ID from The Movie database
            String apiKey = MainActivityFragment.myKey;

            try {
                final String TRAILER_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY_PARAM = "api_key";
                final String VIDEOS_PARAM = "videos";

                Uri builtUri = Uri.parse(TRAILER_BASE_URL).buildUpon()
                        .appendPath(dbID)
                        .appendPath(VIDEOS_PARAM)
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0){
                    return null;
                }
                trailersJsonStr = buffer.toString();
                try {
                    return getTrailersListFromJson(trailersJsonStr);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("Trailer", "Error", e);
                return null;
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Trailer", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        private ArrayList<Trailer> getTrailersListFromJson(String trailersJsonStr) throws JSONException{
            final String TRAILER_RESULTS = "results";
            final String TRAILER_ID = "id";
            final String TRAILER_KEY = "key";
            final String TRAILER_NAME = "name";
            final String TRAILER_TYPE = "type";

            JSONObject trailerJson = new JSONObject(trailersJsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray(TRAILER_RESULTS);

            ArrayList<Trailer> trailerList = new ArrayList<>();

            for (int i=0; i< trailerArray.length();i++){
                String trailerID;
                String trailerKey;
                String trailerName;
                String trailerType;

                JSONObject trailer = trailerArray.getJSONObject(i);
                trailerID = trailer.getString(TRAILER_ID);
                trailerKey = trailer.getString(TRAILER_KEY);
                trailerName = trailer.getString(TRAILER_NAME);
                trailerType = trailer.getString(TRAILER_TYPE);

                if (trailerType.equals("Trailer")) trailerList.add(new Trailer(trailerID,trailerKey,trailerName, trailerType));
            }
            mTrailerList = trailerList;
            return trailerList;
        }

        @Override
        protected void onPostExecute(ArrayList<Trailer> trailers) {
            int thumbWidth;
            if (mTab) {
                thumbWidth = displayWidth/4;
            }
            else if (mLandscape) {
                thumbWidth = displayWidth/4;
            }
            else {
                thumbWidth = displayWidth/2;
            }
            int thumbHeight = (int)(thumbWidth*0.5625);
            int playIconWidth = thumbWidth/4;
            int playIconHeight = (int) (playIconWidth *0.7);

            if (trailers.size()!=0) {
                mTrailerListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Trailer# clicked",v.getTag().toString());
                        int indexClicked = (int) v.getTag();
                        String videoId = mTrailerList.get(indexClicked).getTrailerKey();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" +videoId));
                        intent.putExtra("VIDEO_ID", videoId);
                        Log.d("VideoID",videoId);
                        try {
                            startActivity(intent);
                        }
                        catch (ActivityNotFoundException a){
                            Log.d("Activity not found", a.toString());
                        }
                    }
                };
                Log.d("Trailer 1 name", trailers.get(0).getTrailerName());
                for (int i = 0; i < trailers.size(); i++) {

                    View trailerView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_icon_name, mTrailerLayout, false);
                    ImageView trailer = (ImageView) trailerView.findViewById(R.id.trailer_thumbnail);
                    ImageView playIcon = (ImageView) trailerView.findViewById(R.id.play_icon);
                    RelativeLayout thumbLayout = (RelativeLayout) trailerView.findViewById(R.id.trailer_thumb_relative);
                    TextView trailerName = (TextView) trailerView.findViewById(R.id.trailer_name);

                    thumbLayout.getLayoutParams().width=thumbWidth;
                    thumbLayout.getLayoutParams().height=thumbHeight;
                    trailerView.setPadding(5, 5, 5, 5);
                    trailerView.setTag(i);
                    trailerView.setOnClickListener(mTrailerListener);

                    trailer.getLayoutParams().width=thumbWidth;
                    trailer.getLayoutParams().height=thumbHeight;

                    playIcon.getLayoutParams().width=playIconWidth;
                    playIcon.getLayoutParams().height=playIconHeight;

                    String trailerPath = "http://img.youtube.com/vi/" +
                            trailers.get(i).getTrailerKey() + "/mqdefault.jpg";

                    Uri myUri = Uri.parse(trailerPath);
                    Picasso.with(getActivity())
                            .load(myUri)
                            .into(trailer);

                    trailerName.setText(trailers.get(i).getTrailerName());

                    mTrailerLayout.addView(trailerView);
                    if (i==2) break; // limit 3 trailers shown
                }
            }
            super.onPostExecute(trailers);
        }
    }

    public class GetReviews extends AsyncTask<String, Void, ArrayList<Review>>{
        @Override
        protected ArrayList<Review> doInBackground(String... params) {
            if (params.length == 0){
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewsJsonStr;
            String dbID = params[0];
            String apiKey = MainActivityFragment.myKey;

            try {
                final String REVIEWS_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY_PARAM = "api_key";
                final String VIDEOS_PARAM = "reviews";

                Uri builtUri = Uri.parse(REVIEWS_BASE_URL).buildUpon()
                        .appendPath(dbID)
                        .appendPath(VIDEOS_PARAM)
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0){
                    return null;
                }
                reviewsJsonStr = buffer.toString();
                try {
                    return getReviewsFromJson(reviewsJsonStr);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("Review", "Error", e);
                return null;
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Review", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        private ArrayList<Review> getReviewsFromJson(String reviewsJsonStr) throws JSONException{
            final String REVIEW_RESULTS = "results";
            final String REVIEW_ID = "id";
            final String REVIEW_AUTHOR = "author";
            final String REVIEW_CONTENT = "content";
            final String REVIEW_URL = "url";

            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray reviewsArray = reviewsJson.getJSONArray(REVIEW_RESULTS);

            ArrayList<Review> reviews = new ArrayList<>();

            for (int i=0; i< reviewsArray.length();i++){
                String reviewID;
                String reviewAuthor;
                String reviewContent;
                String reviewURL;
                String reviewContentTruncated;

                JSONObject review = reviewsArray.getJSONObject(i);
                reviewID = review.getString(REVIEW_ID);
                reviewAuthor = review.getString(REVIEW_AUTHOR);
                reviewContent = review.getString(REVIEW_CONTENT);
                reviewContentTruncated = reviewContent;
                if (reviewContent.length()>200) reviewContentTruncated = reviewContent.substring(0,200);
                reviewURL = review.getString(REVIEW_URL);

                reviews.add(new Review(reviewID, reviewAuthor, reviewContent, reviewContentTruncated, reviewURL));
            }
            mReviewsCount=reviewsArray.length();
            mReviews = reviews;
            return reviews;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> reviews) {
            if (mReviewsCount>0) {
                TextView textReviewAuthor;
                TextView textReviewContent;
                mReviewsListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int indexState[] = (int[]) v.getTag();
                        int reviewClicked = indexState[0];
                        int state = indexState[1];
                        String ellipses="...[Touch to see full Review];";
                        TextView contentReview = (TextView) v.findViewById(R.id.text_review_content);
                        if (state==TRUNCATEDREVIEW) {
                            contentReview.setText(mReviews.get(reviewClicked).getReviewContent());
                            indexState[1] = FULLREVIEW;
                            v.setTag(indexState);
                        }
                        else if (state==FULLREVIEW){
                            contentReview.setText(mReviews.get(reviewClicked).getReviewContentTruncated() + ellipses);
                            indexState[1] = TRUNCATEDREVIEW;
                            v.setTag(indexState);
                        }
                    }
                };
                for (int i=0 ; i<mReviewsCount; i++){
                    int indexState[] = new int[2]; // index 0 is for index of review, index 1 is for state of review i.e. short,truncated,full
                    indexState[0]=i;
                    String ellipses = "";
                    if (reviews.get(i).getReviewContent().length()<=200) {
                        indexState[1] = SHORTREVIEW;
                    }
                    else {
                        indexState[1] = TRUNCATEDREVIEW;
                    }
                    if (indexState[1]!=SHORTREVIEW) ellipses ="...[Touch to see full Review]";
                    LinearLayout reviewLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.review_item, mReviewLayout, false);
                    textReviewAuthor = (TextView) reviewLayout.findViewById(R.id.text_review_author);
                    textReviewContent = (TextView) reviewLayout.findViewById(R.id.text_review_content);
                    textReviewAuthor.setText("Review by: " + reviews.get(i).getReviewAuthor());
                    textReviewContent.setText(reviews.get(i).getReviewContentTruncated() + ellipses);
                    reviewLayout.setTag(indexState);
                    reviewLayout.setOnClickListener(mReviewsListener);
                    mReviewLayout.addView(reviewLayout);
                }
            }
            super.onPostExecute(reviews);
        }
    }
    public interface Callback {
        public void updateGrid (ArrayList<MovieItem> movieList, int position);
    }
}
