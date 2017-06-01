package jfkdevelopers.navdrawertestapp.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.media.Rating;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Interfaces.RestApi;
import jfkdevelopers.navdrawertestapp.Objects.Movie;
import jfkdevelopers.navdrawertestapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity {
    private final Context context = this;
    public com.iarcuschin.simpleratingbar.SimpleRatingBar ratingBar;
    private DatabaseHandler db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final int id = getIntent().getIntExtra("id",0);
        getMovieDetails(id);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        ratingBar = (SimpleRatingBar) findViewById(R.id.userRating);
        db = new DatabaseHandler(this);
        ratingBar.setRating(db.getRating(id));
        int src = getIntent().getIntExtra("src",0);
        if (src==0) ratingBar.setVisibility(View.GONE);
        else ratingBar.setOnRatingBarChangeListener(new SimpleRatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(SimpleRatingBar simpleRatingBar, float rating, boolean fromUser) {
                mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieIDs").child(Integer.toString(id)).setValue(rating);
                db.rateMovie(id,rating);
            }
        });
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

   /* @Override
    public void onBackPressed(){
        super.onBackPressed();
    }*/

    private void getMovieDetails(int id){
        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        String BASE_URL = "http://api.themoviedb.org/3/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final RestApi service = retrofit.create(RestApi.class);
        Call<Movie> movieCall = service.getMovie(id);
        movieCall.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, retrofit2.Response<Movie> response) {
                Movie movie = response.body();
                if (movie != null) {
                    CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
                    collapsingToolbar.setTitle(movie.getTitle());

                    ImageView iv_backdrop = (ImageView) findViewById(R.id.backdrop);
                    ImageView iv_Poster = (ImageView) findViewById(R.id.movie_poster);

                    Glide.with(context)
                            .load("https://image.tmdb.org/t/p/w500" +movie.getPosterPath())
                            .override(500, 500)
                            .placeholder(R.mipmap.ic_theaters_black_24dp)
                            .error(R.mipmap.ic_theaters_black_24dp)
                            .into(iv_Poster);

                    Glide.with(context)
                            .load("https://image.tmdb.org/t/p/w500" +movie.getBackdropPath())
                            .placeholder(R.drawable.rectangle)
                            .error(iv_Poster.getDrawable())
                            .into(iv_backdrop);

                    TextView tv_tagline = (TextView) findViewById(R.id.movie_tagline);
                    TextView tv_Title = (TextView) findViewById(R.id.movie_title);
                    TextView tv_Overview = (TextView) findViewById(R.id.movie_overview);
                    TextView tv_Genres = (TextView) findViewById(R.id.movie_genres);
                    TextView tv_Runtime = (TextView) findViewById(R.id.movie_runtime);
                    TextView tv_Release = (TextView) findViewById(R.id.movie_releaseDate);
                    TextView tv_Director = (TextView) findViewById(R.id.movie_director);
                    TextView tv_Cast = (TextView) findViewById(R.id.movie_cast);

                    tv_tagline.setText(movie.getTagline());

                    String year = movie.getReleaseDate().length()>=4? " ("+movie.getReleaseDate().substring(0,4)+")":"";
                    String title = movie.getTitle()+year;
                    tv_Title.setText(title);
                    tv_Title.setTypeface(null, Typeface.BOLD);

                    tv_Overview.setText(movie.getOverview());

                    String genres = "";
                    for(Movie.Genres g: movie.getGenres()){
                        genres = genres + g.name + ", ";
                    }
                    if(genres.length()>2) genres = genres.substring(0,genres.length()-2);
                    else if(genres.equals("")) genres = "N/A";
                    tv_Genres.setText(genres);

                    String runtime = movie.getRuntime()+" min";
                    tv_Runtime.setText(runtime);

                    String releaseDate = movie.getReleaseDate().length()>0? movie.getReleaseDate().substring(5,7) + "/" + movie.getReleaseDate().substring(8) + "/" + movie.getReleaseDate().substring(0,4):"N/A";
                    tv_Release.setText(releaseDate);

                    tv_Director.setText(movie.getDirector());

                    int count = 0;
                    String cast = "";
                    for(Movie.Cast c: movie.getCast()){
                        if(count<10) { //Only show up to the first 10 cast members to avoid very long lists.
                            String temp = String.format("%s  -  %s",c.name,c.character);
                            if(c.character.equals("")) temp = c.name;
                            cast = cast + temp + "\n";
                            count++;
                        }
                    }
                    if(cast.equals("")) cast = "N/A";
                    tv_Cast.setText(cast);
                }
                if (pDialog.isShowing())
                    pDialog.dismiss();
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                Toast.makeText(context,"Error getting movie details. Please try again.",Toast.LENGTH_LONG).show();
            }
        });
    }
}

