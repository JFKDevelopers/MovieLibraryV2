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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jfkdevelopers.navdrawertestapp.Adapters.BasicMovieAdapter;
import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Interfaces.EndlessRecyclerViewScrollListener;
import jfkdevelopers.navdrawertestapp.Interfaces.RestApi;
import jfkdevelopers.navdrawertestapp.Objects.BasicMovie;
import jfkdevelopers.navdrawertestapp.Objects.MovieResponse;
import jfkdevelopers.navdrawertestapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NowPlayingFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;

    private static final String TAG = "NowPlayingFragment";
    private ArrayList<BasicMovie> nowPlayingMovies;
    private BasicMovieAdapter mAdapter;
    private DatabaseHandler db;
    private int totalPages;
    private int currPage;
    private RecyclerView rv;

    private List<Integer> movieIds;

    public NowPlayingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nowPlayingMovies = new ArrayList<>();
        db = new DatabaseHandler(getActivity());
        mAdapter = new BasicMovieAdapter(getActivity(),nowPlayingMovies, this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nowplaying, container, false);
        view.setTag(TAG);
        Context context = view.getContext();

        rv = (RecyclerView) view.findViewById(R.id.nowPlayingRecyclerView);
        rv.setHasFixedSize(true);
        rv.setSaveEnabled(true);
        //int mColumnCount = 1;
        //if (mColumnCount <= 1) {
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
        Collections.sort(nowPlayingMovies,new Comparator<BasicMovie>(){
            @Override
            public int compare(BasicMovie m1, BasicMovie m2){
                return m2.getReleaseDate().compareTo(m1.getReleaseDate());
            }
        });

        mAdapter = new BasicMovieAdapter(context, nowPlayingMovies, this);
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
//                    Log.e("error adding to db", databaseError.toString());
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
//                    Log.e("error adding to user", databaseError.toString());
//                }
//            });
//        }

        if(!db.movieInTable(m.getId())) {
            db.addMovie(m.getId(), 0);
            Snackbar.make(rv,m.getTitle() + " added",Snackbar.LENGTH_LONG).show();
        }
        else{
            Snackbar.make(rv,m.getTitle()+" is already in your library",Snackbar.LENGTH_LONG).show();
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
        call = service.getNowPlayingList(currPage);

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, retrofit2.Response<MovieResponse> response) {
                try {
                    MovieResponse mr = response.body();
                    totalPages = mr.getTotal_pages();
                    currPage = mr.getPage();
                    for(BasicMovie m: mr.getResults()) {
                        nowPlayingMovies.add(m);
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

    @Override
    public void onResume(){
        super.onResume();
        Boolean connected = connectedToNetwork();
        if(connected && nowPlayingMovies.size()<=0){
            Log.e(TAG,"init getMovie called");
            currPage=1;
            getMovies();
        }
        else if(!connected) Toast.makeText(getActivity(),"No Internet Connection",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy(){
        Log.e(TAG,"onDestroy called");
        super.onDestroy();
    }
}
