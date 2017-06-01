package jfkdevelopers.navdrawertestapp.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
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

import jfkdevelopers.navdrawertestapp.Adapters.BasicMovieAdapter;
import jfkdevelopers.navdrawertestapp.Adapters.MovieAdapter;
import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Interfaces.EndlessRecyclerViewScrollListener;
import jfkdevelopers.navdrawertestapp.Interfaces.RestApi;
import jfkdevelopers.navdrawertestapp.Objects.BasicMovie;
import jfkdevelopers.navdrawertestapp.Objects.Movie;
import jfkdevelopers.navdrawertestapp.Objects.MovieResponse;
import jfkdevelopers.navdrawertestapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PopularFragment extends Fragment {

    // TODO: Customize parameters
    /*private int mColumnCount = 1;*/
    private OnListFragmentInteractionListener mListener;

    private static final String TAG = "PopularFragment";
    private ArrayList<BasicMovie> popularMovies;
    private BasicMovieAdapter mAdapter;
    private DatabaseHandler db;
    private int totalPages;
    private int currPage;
    private RecyclerView rv;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private List<Integer> movieIds;
    public PopularFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        popularMovies = new ArrayList<>();
        currPage = 1;
        if(connectedToNetwork()) getMovies();
        else Toast.makeText(getActivity(),"No Internet Connection",Toast.LENGTH_LONG).show();
        db = new DatabaseHandler(getActivity());
        mAdapter = new BasicMovieAdapter(getActivity(),popularMovies, this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popular, container, false);
        view.setTag(TAG);
        Context context = view.getContext();
        rv = (RecyclerView) view.findViewById(R.id.popularRecyclerView);

        /*if (mColumnCount <= 1) {*/
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            rv.setLayoutManager(layoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),layoutManager.getOrientation());
            rv.addItemDecoration(dividerItemDecoration);
        /*} else {
            rv.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }*/
        EndlessRecyclerViewScrollListener ervsl = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.e(TAG,"currPage: " + currPage + " total pages: " + totalPages);
                if(currPage<totalPages && currPage+1<=10) {
                    currPage++;
                    if(connectedToNetwork()) getMovies();
                    else Toast.makeText(getActivity(),"No Internet Connection",Toast.LENGTH_LONG).show();
                }
            }
        };
        rv.addOnScrollListener(ervsl);
        Collections.sort(popularMovies,new Comparator<BasicMovie>(){
            @Override
            public int compare(BasicMovie m1, BasicMovie m2){
                String m1Pop = Double.toString(m1.getPopularity());
                String m2Pop = Double.toString(m2.getPopularity());
                return m2Pop.compareTo(m1Pop);
            }
        });
        mAdapter = new BasicMovieAdapter(context, popularMovies, this);
        rv.setAdapter(mAdapter);
        setHasOptionsMenu(true);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

   @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu,inflater);
       menu.clear();
   }

    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(Uri uri);
    }

    public static String getFragTag(){
        return TAG;
    }

    public void addMovie(final BasicMovie m){
        if(mDatabase!=null) {
            //check if movie exists in database, if not then add it
            mDatabase.child("movies").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.hasChild(Integer.toString(m.getId()))) {
                        getMovieDetails(m.getId());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("error adding to db", databaseError.toString());
                }
            });

            //check if user already has this movie, if not then add it
            mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieIDs").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.hasChild(Integer.toString(m.getId()))) {
                        mDatabase.child("users").child(mFirebaseUser.getUid()).child("movieIDs").child(Integer.toString(m.getId())).setValue(0);
                        Snackbar.make(rv,m.getTitle() + " added",Snackbar.LENGTH_LONG).show();
                    }
                    else{
                        Snackbar.make(rv,m.getTitle()+" is already in your library",Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("error adding to user", databaseError.toString());
                }
            });
        }

        if(!db.movieInTable(m.getId())) {
            db.addMovie(m.getId(), m.toJsonString());
        }
    }

    private void getMovies(){
        ProgressDialog pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        String BASE_URL = "http://api.themoviedb.org/3/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final RestApi service = retrofit.create(RestApi.class);
        Call<MovieResponse> call;
        call = service.getPopularList(currPage);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                try {
                    MovieResponse mr = response.body();
                    totalPages = mr.getTotal_pages();
                    currPage = mr.getPage();
                    for(BasicMovie m: mr.getResults()) {
                        popularMovies.add(m);
                    }
                    mAdapter.notifyDataSetChanged();
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

    private boolean connectedToNetwork() {
        boolean connected = false;
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ||
                    activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                connected = true;
            }
        }
        return connected;
    }

    private void getMovieDetails(final int id){
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
                mDatabase.child("movies").child(Integer.toString(id)).setValue(movie);
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(getActivity(),"Error getting movie details. Please try again.",Toast.LENGTH_LONG).show();
            }
        });
    }
}
