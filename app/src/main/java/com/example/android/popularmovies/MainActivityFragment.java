package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;

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

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public final String POPULAR = "popular";
    public final String TOP_RATED = "top_rated";
    public static final String FAVORITES ="favorites";
    private MovieAdapter movieAdapter;
    private FavoritesAdapter mFavoritesAdapter;
    public String currentSortType = POPULAR;
    private GridView mGridView;
    private ArrayList<MovieItem> movieItems = new ArrayList<>();
    private static final int FAVORITES_LOADER_ID = 0;
    private Cursor currentFavoritesCursor=null;
    private Boolean prefChanged = false;
    private boolean mTab=false;
    private int mPosition=0;
    private Toolbar mToolbar;
    private boolean mOnline;
    private String mTitle;
    public static final String myKey = "";
    private View mRootView;
    private View.OnClickListener mRetryNetwork;

    public MainActivityFragment(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("moviesParcel", movieItems);
        outState.putBoolean("mTab",mTab);
        outState.putInt("mPosition", mPosition);
        outState.putString("currentSortType",currentSortType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void updateMovieItems() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((getActivity()));
        currentSortType = prefs.getString(getString(R.string.pref_sort_type_key), getString(R.string.pref_sort_type_popular));
        if (currentSortType.equals(FAVORITES)){
            showFavorites();
        }
        else { // popular or top rated
            if (mOnline) {
                View offlineImage = getActivity().findViewById(R.id.offline_image);
                TextView offlineText = (TextView) getActivity().findViewById(R.id.offline_text);
                offlineImage.setBackground(null);
                offlineImage.setLayoutParams(new LinearLayout.LayoutParams
                        (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                offlineText.setText("");
            }
            new FetchMoviesTask().execute(currentSortType);
            prefChanged=false;
        }
    }

    private boolean showFavorites() {
        if (!isOnline()) {
            View offlineImage = getActivity().findViewById(R.id.offline_image);
            TextView offlineText = (TextView) getActivity().findViewById(R.id.offline_text);
            offlineImage.setBackgroundResource(R.drawable.noconnection);
            offlineImage.getBackground().setAlpha(100);
            offlineImage.setOnClickListener(mRetryNetwork);
            offlineText.setText("");
            mOnline=false;
        }
        else {
            mOnline=true;
            View offlineImage = getActivity().findViewById(R.id.offline_image);
            TextView offlineText = (TextView) getActivity().findViewById(R.id.offline_text);
            offlineImage.setBackground(null);
            offlineImage.setLayoutParams(new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            offlineText.setText("");

        }
        Cursor cursorFavorites = getContext().getContentResolver().query(MovieContract.MovieFavoritesTable.CONTENT_URI,null,null,null,null);
        int movieCount=0;
        if (cursorFavorites!=null) {
            movieCount = cursorFavorites.getCount();
        }

        if (movieCount==0) { //nothing to show
            mFavoritesAdapter = new FavoritesAdapter(getActivity(), null, 0, FAVORITES_LOADER_ID);
            mGridView.setAdapter(mFavoritesAdapter);
            mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            mToolbar.setTitle(getString(R.string.no_favorites_title));
            return false;
        }
        // if cursor hasn't changed don't update view
        Boolean changed = false;
        if (currentFavoritesCursor!=null){
            if (currentFavoritesCursor.getCount()==movieCount){
                for (int i = 0; i < movieCount; i++) {
                    currentFavoritesCursor.moveToPosition(i);
                    cursorFavorites.moveToPosition(i);
                    if (currentFavoritesCursor.getInt(currentFavoritesCursor.getColumnIndex(MovieContract.MovieFavoritesTable._ID)) !=
                            cursorFavorites.getInt(cursorFavorites.getColumnIndex(MovieContract.MovieFavoritesTable._ID))) {
                        changed = true;
                    }
                }
                if (!changed && !prefChanged && mOnline) return false;
            }
        }

        // Update movieItems with cursor retrieved from database
        if (movieItems!=null) movieItems.clear();
        if (cursorFavorites.moveToFirst()) {
            for (int i = 0; i < movieCount; i++) {
                cursorFavorites.moveToPosition(i);
                int id = cursorFavorites.getInt(cursorFavorites.getColumnIndex(MovieContract.MovieFavoritesTable._ID));
                String title = cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieFavoritesTable.MOVIE_TITLE));
                String poster = cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieFavoritesTable.MOVIE_POSTER_PATH));
                String backdrop = cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieFavoritesTable.MOVIE_BACKDROP_PATH));
                String date = cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieFavoritesTable.MOVIE_DATE));
                String overview = cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieFavoritesTable.MOVIE_OVERVIEW));
                Double vote = cursorFavorites.getDouble(cursorFavorites.getColumnIndex(MovieContract.MovieFavoritesTable.MOVIE_VOTE_AVE));

                MovieItem movie = new MovieItem(poster, title);
                movie.setMovieID(id);
                movie.setBackdropPath(backdrop);
                movie.setReleaseDate(date);
                movie.setOverview(overview);
                movie.setVoteAverage(vote);
                movie.setFavorite(true);

                movieItems.add(movie);
            }

            if (getActivity().findViewById(R.id.movie_detail_container)!=null){
                mTab=true;
                mFavoritesAdapter.setmTab(true);
            }

            mFavoritesAdapter.swapCursor(cursorFavorites);
            mGridView.setAdapter(mFavoritesAdapter);
            currentFavoritesCursor = cursorFavorites;
            prefChanged=false;
            return true;
        }
        return false;
    }

    public interface Callback {
        void movieSelected(ArrayList<MovieItem> movieItems, int position);
    }

    @Override
    public void onStart() {
        mTab = ((MainActivity) getActivity()).getmTab();
        if (!mTab && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            mGridView.setNumColumns(4);  // four columns if landscape on phone
        }
        // only update movie items if not already loaded
        if (movieItems.isEmpty()){
            updateMovieItems();
        }
        super.onStart();
    }

    @Override
    public void onResume() {  // called after coming back from settings activity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((getActivity()));
        // detect whether sort type was changed, then update movies if true
        String prefSortType = prefs.getString(getString(R.string.pref_sort_type_key), getString(R.string.pref_sort_type_popular));
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (prefSortType.equals(this.POPULAR)) mTitle = getString(R.string.popular_title);
        if (prefSortType.equals(this.TOP_RATED)) mTitle = getString(R.string.top_rated_title);
        if (prefSortType.equals(FAVORITES)) mTitle = getString(R.string.favorites_title);
        mToolbar.setTitle(mTitle);

        if (isOnline()) {
            mOnline=true;
        }
        else {
            mOnline=false;
        }
        // sorted by Popular or Top rated after pref change
        if ((!currentSortType.equals(prefSortType) && (!prefSortType.equals("favorites"))) || !mOnline){
                prefChanged=true;
                mPosition=0;
                if (mTab) ((Callback) getActivity()).movieSelected(movieItems, mPosition);
                mGridView.setAdapter(movieAdapter);
                updateMovieItems();
        }
        else {
            if (prefSortType.equals("favorites")) {
                if (!currentSortType.equals(prefSortType)) {
                    prefChanged=true;
                    movieItems.clear();
                    mPosition=0;
                }
                if (mTab && mFavoritesAdapter!=null) ((Callback) getActivity()).movieSelected(movieItems, mPosition);
                updateMovieItems();
            }
        }
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey("moviesParcel")) {
            movieItems = savedInstanceState.getParcelableArrayList("moviesParcel");
            mTab = savedInstanceState.getBoolean("mTab", mTab);
            mPosition = savedInstanceState.getInt("mPosition", mPosition);
            currentSortType = savedInstanceState.getString("currentSortType");
        }
        mRetryNetwork = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), getString(R.string.retry_network_toast),
                        Toast.LENGTH_LONG).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateMovieItems();
                    }
                }, 4000); // Give more time for reconnect to establish
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRootView = rootView;
        setHasOptionsMenu(true);

        movieAdapter = new MovieAdapter(getActivity(), movieItems);
        movieAdapter.setmTab(mTab);

        mFavoritesAdapter = new FavoritesAdapter(getActivity(), null, 0, FAVORITES_LOADER_ID);
        mFavoritesAdapter.setmTab(mTab);

        mGridView = (GridView) rootView.findViewById(R.id.gridview_pics);

        if (currentSortType.equals(FAVORITES)) {
            mGridView.setAdapter(mFavoritesAdapter);
        }
        else {
            mGridView.setAdapter(movieAdapter);
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open detail screen if poster is selected

                ((Callback) getActivity()).movieSelected(movieItems, position);
                mPosition=position;
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MovieContract.MovieFavoritesTable.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data!=null) mFavoritesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoritesAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_fragment, menu);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {
            if (isOnline()) mOnline=true;
            else {
                mOnline = false;
                if (mTab){
                    Bundle args = new Bundle();
                    args.putString("Offline", "offline");

                    DetailActivityFragment fragment = new DetailActivityFragment();
                    fragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment, MainActivity.DETAILFRAGMENT_TAG)
                            .commit();
                }
                return null;
            }
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr;
            String type = params[0]; // popular or top_rated

            try {
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendPath(type)
                        .appendQueryParameter(API_KEY_PARAM, myKey)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                // load from buffer into json string
                moviesJsonStr = buffer.toString();
                try {
                    return getMovieListFromJson(moviesJsonStr); // extract data for each movie after successful API call
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                Log.e("Movie", "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Movie", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        private ArrayList<MovieItem> getMovieListFromJson(String jsonMovies) throws JSONException{
            final String TMDB_RESULTS = "results";
            final String POSTER_PATH = "poster_path";
            final String BACKDROP_PATH = "backdrop_path";
            final String TITLE = "title";
            final String RELEASE_DATE = "release_date";
            final String VOTE_AVERAGE = "vote_average";
            final String OVERVIEW = "overview";
            final String MOVIE_ID = "id";

            JSONObject movieJson = new JSONObject(jsonMovies);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

            ArrayList<MovieItem> movieList = new ArrayList<>();

            for (int i=0;i < movieArray.length();i++){
                String posterPath;
                String backdropPath;
                String title;
                String overview;
                double voteAverage;
                String releaseDate;
                int movieID;

                // load fields from JSON string for each movie
                JSONObject movie = movieArray.getJSONObject(i);
                posterPath = movie.getString(POSTER_PATH);
                title = movie.getString(TITLE);
                backdropPath = movie.getString(BACKDROP_PATH);
                overview = movie.getString(OVERVIEW);
                voteAverage = movie.getDouble(VOTE_AVERAGE);
                releaseDate = movie.getString(RELEASE_DATE);
                movieID = movie.getInt(MOVIE_ID);

                // load fields into each movie item
                movieList.add(new MovieItem(posterPath, title));
                movieList.get(i).setBackdropPath(backdropPath);
                movieList.get(i).setReleaseDate(releaseDate);
                movieList.get(i).setVoteAverage(voteAverage);
                movieList.get(i).setOverview(overview);
                movieList.get(i).setMovieID(movieID);
            }
            return movieList; // return ArrayList of MovieItems with all fields loaded
        }

        @Override
        protected void onPostExecute(ArrayList<MovieItem> items) {
            if (!mOnline) {
                movieAdapter.clear();
                View offlineImage = getActivity().findViewById(R.id.offline_image);
                TextView offlineText = (TextView) getActivity().findViewById(R.id.offline_text);
                offlineImage.setBackgroundResource(R.drawable.noconnection);
                offlineImage.setLayoutParams(new LinearLayout.LayoutParams(
                        (int) getResources().getDimension(R.dimen.dp100),(int) getResources().getDimension(R.dimen.dp100)
                ));
                LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) offlineImage.getLayoutParams();
                ll.gravity = Gravity.CENTER_HORIZONTAL;
                offlineImage.setLayoutParams(ll);
                offlineImage.setOnClickListener(mRetryNetwork);
                offlineImage.getBackground().setAlpha(255);
                offlineText.setText(getString(R.string.offline_text));
                offlineText.setOnClickListener(mRetryNetwork);
            }
            if (items!=null) {
                if (mOnline){
                    View offlineImage = getActivity().findViewById(R.id.offline_image);
                    TextView offlineText = (TextView) getActivity().findViewById(R.id.offline_text);
                    offlineImage.setBackground(null);
                    offlineImage.setLayoutParams(new LinearLayout.LayoutParams
                            (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    offlineText.setText("");
                }
                movieItems = items;
                movieAdapter.clear();
                if (getActivity().findViewById(R.id.movie_detail_container)!=null){
                    mTab=true;
                    movieAdapter.setmTab(true);
                }
                for (MovieItem movie : items) {
                    movie.setPosterImageView(getContext());
                    movie.setBackdropImageView(getContext());
                    // the above loading of ImageViews is so Picasso caches the images before scrolling gridview
                    // and for storing images into Favorites
                    movieAdapter.add(movie);
                }
                if (mTab) mGridView.setNumColumns(2);
                mGridView.setAdapter(movieAdapter);
                if (mTab) ((Callback) getActivity()).movieSelected(movieItems, mPosition);
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}