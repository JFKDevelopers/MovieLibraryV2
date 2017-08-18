package jfkdevelopers.navdrawertestapp.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Interfaces.RestApi;
import jfkdevelopers.navdrawertestapp.Objects.DBMovie;
import jfkdevelopers.navdrawertestapp.Objects.Movie;
import jfkdevelopers.navdrawertestapp.Objects.Movie.Genres;
import jfkdevelopers.navdrawertestapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity {
    private static final String EXTRA_MESSAGE = "com.jfkdevelopers.navdrawertestapp.MESSAGE";
    private final Context context = this;
    private com.iarcuschin.simpleratingbar.SimpleRatingBar ratingBar;
    private DatabaseHandler db;
    private Movie m;
    private int id;
    private int src;

    private ImageView iv_backdrop;
    private ImageView iv_Poster;
    private TextView tv_tagline;
    private TextView tv_Title;
    private TextView tv_Overview;
    private TextView tv_Genres;
    private TextView tv_Runtime;
    private TextView tv_Release;
    private TextView tv_Director;
    private TextView tv_Cast;
    private Toolbar toolbar;
    private TextView tv_Note;
    private ImageButton edit_Note;
    private CardView cv_note;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Intent intent = getIntent();
        if(intent.hasExtra("src"))
            src = intent.getIntExtra("src",0);
        ratingBar = (SimpleRatingBar) findViewById(R.id.userRating);
        db = new DatabaseHandler(this);
        id = getIntent().getIntExtra("id",0);

        iv_backdrop = (ImageView) findViewById(R.id.backdrop);
        iv_Poster = (ImageView) findViewById(R.id.movie_poster);
        tv_tagline = (TextView) findViewById(R.id.movie_tagline);
        tv_Title = (TextView) findViewById(R.id.movie_title);
        tv_Overview = (TextView) findViewById(R.id.movie_overview);
        tv_Genres = (TextView) findViewById(R.id.movie_genres);
        tv_Runtime = (TextView) findViewById(R.id.movie_runtime);
        tv_Release = (TextView) findViewById(R.id.movie_releaseDate);
        tv_Director = (TextView) findViewById(R.id.movie_director);
        tv_Cast = (TextView) findViewById(R.id.movie_cast);
        tv_Note = (TextView) findViewById(R.id.movie_notes);
        edit_Note = (ImageButton) findViewById(R.id.editButton);
        cv_note = (CardView) findViewById(R.id.cv_notes);
    }

    @Override
    public void onResume(){
        super.onResume();
		 if(src==1) {
            ratingBar.setRating(db.getRating(id));
            ratingBar.setOnRatingBarChangeListener(new SimpleRatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(SimpleRatingBar simpleRatingBar, float rating, boolean fromUser) {
                    db.rateMovie(id, rating);
                }
            });
            edit_Note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog noteDialog;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Add a note for " + tv_Title.getText());
                    final EditText input = new EditText(context);
                    input.setHint(R.string.note_hint);
                    dialog.setView(input);
                    dialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface di, int item) {
                            String message = input.getText().toString().trim();
                            db.addNote(id,message);
                            tv_Note.setText(message);
                        }
                    });
                    dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface di, int which) {
                            di.cancel();
                        }
                    });
                    noteDialog = dialog.create();
                    noteDialog.show();
                }
            });
        }
        else {
            ratingBar.setVisibility(View.GONE);
            cv_note.setVisibility(View.GONE);
        }
        getMovieDetails(id);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_detail, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

//    @Override
//    public void onBackPressed(){
//        super.onBackPressed();
//    }

   private void getMovieDetails(int id){
       final ProgressDialog pDialog = new ProgressDialog(context);
       pDialog.setMessage("Please wait...");
       pDialog.setCancelable(false);
       pDialog.show();
       DBMovie movie = null;
       String note = "";
       try {
           movie = db.getMovie(id);
           note = db.getNote(id);
       }catch(Exception e){
           e.printStackTrace();
       }
       if(db!=null && movie!=null) {
           fillMovieDetails(movie,note);
           if (pDialog.isShowing())
               pDialog.dismiss();
       }
       else{
           String BASE_URL = "http://api.themoviedb.org/3/";
		   try{
           Retrofit retrofit = new Retrofit.Builder()
                   .baseUrl(BASE_URL)
                   .addConverterFactory(GsonConverterFactory.create())
                   .build();
           final RestApi service = retrofit.create(RestApi.class);
           Call<Movie> call = service.getMovie(id);
           call.enqueue(new Callback<Movie>(){
               @Override
               public void onResponse(Call<Movie> call, retrofit2.Response<Movie> response){
                   m = response.body();
                   fillMovieDetails(m);
                   if (pDialog.isShowing())
                       pDialog.dismiss();
               }
               @Override
               public void onFailure(Call<Movie> call, Throwable t){
                   if (pDialog.isShowing())
                       pDialog.dismiss();
               }
           });
		   }catch(Exception e){
			   e.printStackTrace();
		   }
       }
   }

   private void fillMovieDetails(Movie movie){ //for use with search, popular, and now playing activity
       if(movie!=null) {
           toolbar.setTitle(movie.getTitle());
           try {
               Glide.with(context)
                       .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                       .override(500, 500)
                       .placeholder(R.drawable.filmstrip)
                       .error(R.drawable.filmstrip)
                       .into(iv_Poster);

               Glide.with(context)
                       .load("https://image.tmdb.org/t/p/w500" + movie.getBackdropPath())
                       .placeholder(R.drawable.rectangle)
                       .error(iv_Poster.getDrawable())
                       .into(iv_backdrop);
           }catch(Exception e){
               e.printStackTrace();
           }

           tv_tagline.setText(movie.getTagline());

           String year = movie.getReleaseDate().length() >= 4 ? " (" + movie.getReleaseDate().substring(0, 4) + ")" : "";
           String title = movie.getTitle() + year;
           tv_Title.setText(title);
           tv_Title.setTypeface(null, Typeface.BOLD);

           tv_Overview.setText(movie.getOverview());

           String genres = "";
           try {
               for (Genres g : movie.getGenres()) {
                   genres = genres + g.getName() + ", ";
               }
               if (genres.length() > 2) genres = genres.substring(0, genres.length() - 2);
               else if (genres.equals("")) genres = "N/A";
               tv_Genres.setText(genres);
           }catch(Exception e){
               e.printStackTrace();
           }
            try {
                String runtime = movie.getRuntime() + " min";
                tv_Runtime.setText(runtime);

                String releaseDate = movie.getReleaseDate().length() > 0 ? movie.getReleaseDate().substring(5, 7) + "/" + movie.getReleaseDate().substring(8) + "/" + movie.getReleaseDate().substring(0, 4) : "N/A";
                tv_Release.setText(releaseDate);

                tv_Director.setText(movie.getDirector());
            }catch(Exception e){
                e.printStackTrace();
            }
           int count = 0;
           try {
               String cast = "";
               if (movie.getCast() != null) {
                   for (Movie.Cast c : movie.getCast()) {
                       if (count < 10) { //Only show up to the first 10 cast members to avoid very long lists.
                           String temp = String.format("%s  -  %s", c.name, c.character);
                           if (c.character.equals("")) temp = c.name;
                           cast = cast + temp + "\n";
                           count++;
                       }
                   }
               }
               if (cast.equals("")) cast = "N/A";
               tv_Cast.setText(cast);
           }catch(Exception e){
               e.printStackTrace();
           }
       }
   }

    private void fillMovieDetails(DBMovie movie, String note){ //for use with user's movies
        if(movie!=null) {
            toolbar.setTitle(movie.getTitle());
            try {
                Glide.with(context)
                        .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                        .override(500, 500)
                        .placeholder(R.drawable.filmstrip)
                        .error(R.drawable.filmstrip)
                        .into(iv_Poster);

                Glide.with(context)
                        .load("https://image.tmdb.org/t/p/w500" + movie.getBackdropPath())
                        .placeholder(R.drawable.rectangle)
                        .error(iv_Poster.getDrawable())
                        .into(iv_backdrop);
            }catch(Exception e){
                e.printStackTrace();
            }

            tv_tagline.setText(movie.getTagline());
            String year = "";
            try {
                year = movie.getReleaseDate().length() >= 4 ? " (" + movie.getReleaseDate().substring(0, 4) + ")" : "";
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            String title = movie.getTitle() + year;
            tv_Title.setText(title);
            tv_Title.setTypeface(null, Typeface.BOLD);

            tv_Overview.setText(movie.getOverview());

            String genres = movie.getGenres();
            genres = genres.substring(0,genres.length()-1);
            tv_Genres.setText(genres);
            try {
                String runtime = movie.getRuntime() + " min";
                tv_Runtime.setText(runtime);

                String releaseDate = movie.getReleaseDate().length() > 0 ? movie.getReleaseDate().substring(5, 7) + "/" + movie.getReleaseDate().substring(8) + "/" + movie.getReleaseDate().substring(0, 4) : "N/A";
                tv_Release.setText(releaseDate);

                tv_Director.setText(movie.getDirector());
            }catch(Exception e){
                e.printStackTrace();
            }
            try {
                String cast = movie.getCredits();
                if (cast.equals("")) cast = "N/A";
                tv_Cast.setText(cast);
                tv_Note.setText(note);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onPause(){
        super.onPause();
    }
}