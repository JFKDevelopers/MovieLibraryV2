package jfkdevelopers.navdrawertestapp.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jfkdevelopers.navdrawertestapp.Adapters.MovieAdapter;
import jfkdevelopers.navdrawertestapp.Database.DatabaseHandler;
import jfkdevelopers.navdrawertestapp.Objects.DBMovie;
import jfkdevelopers.navdrawertestapp.R;
import jfkdevelopers.navdrawertestapp.SignInActivities.GoogleSignInActivity;

public class MovieFragment extends Fragment implements SearchView.OnQueryTextListener{
    private static final String MyPREFERENCES = "MyPrefs";
    private final CharSequence[] sortOptions = {"Title (A-Z)","Title (Z-A)","Year (1900-2017)","Year (2017-1900)","Rating (5-0)","Rating (0-5)"};
    private static final String TAG = "MovieFragment";

    private SharedPreferences sharedPreferences;
    private int sortPref;
    private DatabaseHandler db;
    private ArrayList<DBMovie> movies;
    private RecyclerView mMovieRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private OnListFragmentInteractionListener mListener;
    private MovieAdapter mAdapter;
    private int topView;

    private int positionIndex = -1;
    private final SparseArray<String> genreMap = new SparseArray<>();

    //FireBase Objects
//    private DatabaseReference mFirebaseDatabaseReference;
//    private FirebaseAuth mFirebaseAuth;
//    private FirebaseUser mFirebaseUser;
//    private ArrayList<String> movieIds;

    public MovieFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        db = new DatabaseHandler(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        sortPref = sharedPreferences.getInt("sortPref",0);
        movies = new ArrayList<>();
        //For later use with FireBase
        //mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        //mFirebaseAuth = FirebaseAuth.getInstance();
        //mFirebaseUser = mFirebaseAuth.getCurrentUser();
        //movieIds = getMovieIdsFromFireBase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        view.setTag(TAG);
        movies = getArguments().getParcelableArrayList("movies");
        int size = 0;
        if(movies!=null) size = movies.size();
        mAdapter = new MovieAdapter(view.getContext(),movies, this);

        mMovieRecyclerView = (RecyclerView) view.findViewById(R.id.movieRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(view.getContext());

        mMovieRecyclerView.setLayoutManager(mLinearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mMovieRecyclerView.getContext(),
                                                                        mLinearLayoutManager.getOrientation());
        mMovieRecyclerView.addItemDecoration(dividerItemDecoration);

        mMovieRecyclerView.setAdapter(mAdapter);

        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        final SearchView sv = new SearchView(getActivity());
        sv.setQueryHint(getActivity().getResources().getString(R.string.search_hint));
        sv.setOnQueryTextListener(this);
        menu.add("Search").setIcon(R.drawable.magnify)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(0), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                setItemsVisibility(menu);
                sv.setMaxWidth(Integer.MAX_VALUE);
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    sv.setBackgroundColor(getResources().getColor(android.R.color.white,getActivity().getTheme()));
                else
                    sv.setBackgroundColor(getResources().getColor(android.R.color.white));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onCreateOptionsMenu(menu,inflater);
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    sv.setBackgroundColor(getResources().getColor(R.color.colorPrimary,getActivity().getTheme()));
                else
                    sv.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                return true;
            }
        });
        menu.getItem(0).setActionView(sv);
    }

    private void setItemsVisibility(Menu menu){
        for(int i=0;i<menu.size();i++){
            menu.getItem(i).setVisible(false);
        }
    }

    @Override
    public boolean onQueryTextChange(String filterTxt){
        final ArrayList<DBMovie> filteredList = filter(filterTxt);
        mAdapter.updateList(filteredList);
        return true;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
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

    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(Uri uri);
    }

    public static String getFragTag(){
        return TAG;
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
                                                                        ItemTouchHelper.LEFT) {
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(getActivity(), R.drawable.delete);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getActivity().getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                MovieAdapter movieAdapter = (MovieAdapter) mMovieRecyclerView.getAdapter();
                /*if (movieAdapter.isUndoOn() && movieAdapter.isPendingRemoval(position)) {
                    return 0;
                }*/
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                //MovieAdapter adapter = (MovieAdapter) mMovieRecyclerView.getAdapter();
                mAdapter.remove(swipedPosition, db, mMovieRecyclerView);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }
                if (!initiated) {
                    init();
                }
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicHeight();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemtouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemtouchHelper.attachToRecyclerView(mMovieRecyclerView);
    }

    private void setUpAnimationDecoratorHelper() {
        mMovieRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                if (!initiated) init();
                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {
                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle
                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;
                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();
                    // this we need to find out
                    int top = 0;
                    int bottom = 0;
                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }
                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }
                    background.setBounds(left, top, right, bottom);
                    background.draw(c);
                }
                super.onDraw(c, parent, state);
            }
        });
    }

    private ArrayList<DBMovie> filter(String filter){
        ArrayList<DBMovie> temp = new ArrayList<>();
        filter = filter.toUpperCase();
        for(DBMovie m:movies){
            if(m.getTitle().toUpperCase().contains(filter)||m.getReleaseDate().toUpperCase().substring(0,4).contains(filter))
                temp.add(m);
        }
        return temp;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            final AlertDialog sortDialog;
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Sort By");

            dialog.setSingleChoiceItems(sortOptions,sortPref,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int item){
                    sortMovies(item);
                    editor.putInt("sortPref",item);
                    editor.apply();
                    dialog.dismiss();
                    mAdapter.updateList(movies);
                    mAdapter.notifyDataSetChanged();
                }
            });
            sortDialog = dialog.create();
            sortDialog.show();
        }
        else if(id==R.id.action_deleteAll){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);
            builder.setTitle(R.string.confirmDialog);
            builder.setPositiveButton(R.string.confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //For later use with FireBase
                            //mFirebaseDatabaseReference.child("users").child(mFirebaseUser.getUid()).child("movieIDs").removeValue();
                            movies.clear();
                            db.deleteAllMovies();
                            mAdapter.updateList(movies);
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            mAdapter.updateList(movies);
        }
        else if(id==R.id.action_syncGoogle){
            Intent intent = new Intent(getActivity(),GoogleSignInActivity.class);
            intent.putParcelableArrayListExtra("movies",movies);
            getActivity().startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void sortMovies(int item){
        //TODO: fix user rating sort
        switch(item){
            case 0: //Sort A-Z
                Collections.sort(movies,new Comparator<DBMovie>(){
                    @Override
                    public int compare(DBMovie m1, DBMovie m2){
                        return m1.getTitle().compareTo(m2.getTitle());
                    }
                });
                sortPref = 0;
                break;
            case 1: //Sort Z-A
                Collections.sort(movies,new Comparator<DBMovie>(){
                    @Override
                    public int compare(DBMovie m1, DBMovie m2){
                        return m2.getTitle().compareTo(m1.getTitle());
                    }
                });
                sortPref = 1;
                break;
            case 2: //Sort by year ascending
                Collections.sort(movies,new Comparator<DBMovie>(){
                    @Override
                    public int compare(DBMovie m1, DBMovie m2){
                        String y1 = m1.getReleaseDate().substring(0,4);
                        String y2 = m2.getReleaseDate().substring(0,4);
                        if(y1.equals(y2)) return m1.getTitle().compareTo(m2.getTitle());
                        else return y1.compareTo(y2);
                    }
                });
                sortPref = 2;
                break;
            case 3: //Sort by year descending
                Collections.sort(movies,new Comparator<DBMovie>(){
                    @Override
                    public int compare(DBMovie m1, DBMovie m2){
                        String y1 = m1.getReleaseDate().substring(0,4);
                        String y2 = m2.getReleaseDate().substring(0,4);
                        if(y2.equals(y1)) return m1.getTitle().compareTo(m2.getTitle());
                        return y2.compareTo(y1);
                    }
                });
                sortPref = 3;
                break;
            case 4: //Sort by rating descending
                Collections.sort(movies,new Comparator<DBMovie>(){
                    @Override
                    public int compare(DBMovie m1, DBMovie m2){
                        Float r1 = db.getRating(m1.getId()) * 10;
                        Float r2 = db.getRating(m2.getId()) * 10;
                        if(r2.equals(r1)) return m1.getTitle().compareTo(m2.getTitle());
                        else return (int) (r2-r1);
                    }
                });
                sortPref = 4;
                break;
            case 5: //Sort by rating ascending
                Collections.sort(movies,new Comparator<DBMovie>(){
                    @Override
                    public int compare(DBMovie m1, DBMovie m2){
                        Float r1 = db.getRating(m1.getId()) * 10;
                        Float r2 = db.getRating(m2.getId()) * 10;
                        if(r2.equals(r1)) return m1.getTitle().compareTo(m2.getTitle());
                        else return (int) (r1-r2);
                    }
                });
                sortPref = 5;
                break;
            default:
                break;
        }
        mAdapter.updateList(movies);
    }

    public int getSortPref(){
        return sortPref;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(movies!=null) {
            if (movies.size() > 1) sortMovies(sortPref);
            mAdapter.updateList(movies);
        }

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

    private void setGenres(){
        genreMap.put(12,"Adventure");
        genreMap.put(14,"Fantasy");
        genreMap.put(16,"Animation");
        genreMap.put(18,"Drama");
        genreMap.put(27,"Horror");
        genreMap.put(28,"Action");
        genreMap.put(35,"Comedy");
        genreMap.put(36,"History");
        genreMap.put(37,"Western");
        genreMap.put(53,"Thriller");
        genreMap.put(80,"Crime");
        genreMap.put(99,"Documentary");
        genreMap.put(878,"Science Fiction");
        genreMap.put(10402,"Music");
        genreMap.put(9648,"Mystery");
        genreMap.put(10749,"Romance");
        genreMap.put(10751,"Family");
        genreMap.put(10752,"War");
        genreMap.put(10770,"TV Movie");
    }
}
