package jfkdevelopers.navdrawertestapp.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jfkdevelopers.navdrawertestapp.Adapters.BasicMovieAdapter;
import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Fragments.MovieFragment;
import jfkdevelopers.navdrawertestapp.Interfaces.EndlessRecyclerViewScrollListener;
import jfkdevelopers.navdrawertestapp.Interfaces.RestApi;
import jfkdevelopers.navdrawertestapp.Objects.BasicMovie;
import jfkdevelopers.navdrawertestapp.Objects.MovieResponse;
import jfkdevelopers.navdrawertestapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity implements MovieFragment.OnListFragmentInteractionListener {
    private String searchTitle;
    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private int page = -1;
    private int totalPages;
    private BasicMovieAdapter moAdapter;
    private final ArrayList<BasicMovie> detailedSearchResults = new ArrayList<>();
    private final Context context = this;
    private DatabaseHandler db;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        db = new DatabaseHandler(this);

        searchTitle = getIntent().getStringExtra("com.jfkdevelopers.navdrawertestapp.MESSAGE").trim();
        searchTitle = searchTitle.replace(" ", "+");

        rv = (RecyclerView) findViewById(R.id.searchRecyclerView);
        rv.setHasFixedSize(true);
        rv.setSaveEnabled(true);
        LinearLayoutManager rvLM = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),rvLM.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        rv.setLayoutManager(rvLM);

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(rvLM) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if(page<totalPages && page+1<=10) {
                    page++;
                    if(connectedToNetwork()){
                        searchMovies(page);
                    }
                    else Toast.makeText(context,"No Internet Connection",Toast.LENGTH_LONG).show();
                }
            }
        };
        rv.addOnScrollListener(endlessRecyclerViewScrollListener);
        moAdapter = new BasicMovieAdapter(this, detailedSearchResults, null);
        rv.setAdapter(moAdapter);
    }

    public void addToSelected(final BasicMovie m){
//        //For later use with FireBase
//        if(mDatabase!=null) {
//            //check if movie exists in database, if not then add it
//            mDatabase.child("movies").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot snapshot) {
//                    if (!snapshot.hasChild(Integer.toString(m.getId()))) {
//                        getMovieDetails(m.getId());
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//            //check if user already has this movie, if not then add it
//            mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieIDs").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot snapshot) {
//                    if (!snapshot.hasChild(Integer.toString(m.getId()))) {
//                        mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieIDs").child(Integer.toString(m.getId())).setValue(0);
//                        Snackbar.make(rv,m.getTitle() + " added",Snackbar.LENGTH_LONG).show();
//                    }
//                    else{
//                        Snackbar.make(rv,m.getTitle()+" is already in your library",Snackbar.LENGTH_LONG).show();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
        if(!db.movieInTable(m.getId())) {
            db.addMovie(m.getId(),0);
            Snackbar.make(rv,m.getTitle() + " added",Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(rv,m.getTitle()+" is already in your library",Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed(){
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void searchMovies(int pg){
        ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final RestApi service = retrofit.create(RestApi.class);

        Call<MovieResponse> call;

        call = service.getSearchResults(searchTitle,pg);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                try {
                    MovieResponse mr = response.body();
                    for (BasicMovie m : mr.getResults()) {
                        detailedSearchResults.add(m);
                    }
                    Collections.sort(detailedSearchResults,new Comparator<BasicMovie>(){
                        @Override
                        public int compare(BasicMovie m1, BasicMovie m2){
                            String m1Pop = Double.toString(m1.getPopularity());
                            String m2Pop = Double.toString(m2.getPopularity());
                            return m2Pop.compareTo(m1Pop);
                        }
                    });
                    moAdapter.notifyDataSetChanged();
                    if(page==1) totalPages=mr.total_pages;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });

        if (pDialog.isShowing())
            pDialog.dismiss();
    }
  
    @Override
    public void onListFragmentInteraction(Uri uri) {
    }

    private boolean connectedToNetwork() {
        boolean connected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ||
                    activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                connected = true;
            }
        }
        return connected;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(page==-1) {
            page = 1;
            searchMovies(page);
        }
    }
}
