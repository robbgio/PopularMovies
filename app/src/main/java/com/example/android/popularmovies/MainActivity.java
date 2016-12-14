package com.example.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback, DetailActivityFragment.Callback {
    private boolean mTab=false;
    public static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (findViewById(R.id.movie_detail_container)!=null){
            mTab=true;

            if (savedInstanceState==null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailActivityFragment(),DETAILFRAGMENT_TAG).commit();
            }

        }
        else {
            mTab=false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void movieSelected(ArrayList<MovieItem> movieList, int position) {
        if (mTab){
            Bundle args = new Bundle();
            args.putParcelableArrayList("Movie Items", movieList);
            args.putInt("Position", position);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
        else {

            byte[] imagePoster = movieList.get(position).getPosterImageBlob();
            byte[] imageBackround = movieList.get(position).getBackdropImageBlob();
            Intent detailIntent = new Intent(this, DetailActivity.class)
                    .putExtra("Movie Items", movieList).putExtra("Position", position)
                    .putExtra("Image Poster", imagePoster)
                    .putExtra("Image Background", imageBackround);
            startActivity(detailIntent);
        }
    }

    @Override
    public void updateGrid(ArrayList<MovieItem> movieList, int position) {
        MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortType = prefs.getString(getString(R.string.pref_sort_type_key), getString(R.string.pref_sort_type_popular));
        if (sortType.equals(MainActivityFragment.FAVORITES) && mTab){
            DetailActivityFragment dfrag = new DetailActivityFragment();
            Bundle args = new Bundle();
            movieList.remove(position);
            args.putParcelableArrayList("Movie Items", movieList);
            if (position==0 && movieList.size()>0) position=0;
            else position-=1;
            args.putInt("Position", position);
            dfrag.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, dfrag, DETAILFRAGMENT_TAG)
                    .commit();
        }
        fragment.updateMovieItems();
    }
    public Boolean getmTab(){
        return mTab;
    }
}
