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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jfkdevelopers.navdrawertestapp.Adapters.MovieAdapter;
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
    private final String TAG = SearchActivity.class.getSimpleName();
    private int page = -1;
    private int totalPages;
    private MovieAdapter moAdapter;
    private final ArrayList<BasicMovie> detailedSearchResults = new ArrayList<>();
    private final Context context = this;
    private DatabaseHandler db;
    private RecyclerView rv;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private List<Integer> movieIds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        searchTitle = getIntent().getStringExtra("com.jfkdevelopers.navdrawertestapp.MESSAGE").trim();
        searchTitle = searchTitle.replace(" ", "+");
        Log.e(TAG,searchTitle);
        rv = (RecyclerView) findViewById(R.id.searchRecyclerView);
        rv.setHasFixedSize(true);
        LinearLayoutManager rvLM = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),rvLM.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        rv.setLayoutManager(rvLM);
        if(page==-1) page = 1;

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(rvLM) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.e(TAG,"currPage: " + page + " total pages: " + totalPages);
                if(page<totalPages && page+1<=10) {
                    page++;
                    if(connectedToNetwork()) searchMovies(page);
                    else Toast.makeText(context,"No Internet Connection",Toast.LENGTH_LONG).show();
                }
            }
        };
        rv.addOnScrollListener(endlessRecyclerViewScrollListener);
        db = new DatabaseHandler(this);
        moAdapter = new MovieAdapter(this, detailedSearchResults, null);
        rv.setAdapter(moAdapter);
        if(connectedToNetwork()) searchMovies(page);
    }

    public void addToSelected(final BasicMovie m){
        movieIds = new ArrayList<>();
        mDatabase.child("users").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Integer>> t = new GenericTypeIndicator<List<Integer>>() {};
                if(dataSnapshot.hasChild("movieIDs"))
                    movieIds = dataSnapshot.child("movieIDs").getValue(t);

                movieIds.add(m.getId());
                mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieIDs").setValue(movieIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(!db.movieInTable(m.getId())) {
            db.addMovie(m.getId(), m.toJsonString());

            Snackbar.make(rv,m.getTitle() + " added",Snackbar.LENGTH_LONG).show();
        }
        else
            Snackbar.make(rv,m.getTitle()+" is already in your library",Snackbar.LENGTH_LONG).show();
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
                Log.e("Failure: ", t.getMessage());
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

}
